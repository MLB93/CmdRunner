package data;

import general.exception.config.ConfigParameterException;
import general.exception.config.ReadConfigFileException;
import general.exception.config.ShowConfigException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Configuration {
    private static final String PROGRAM_NAME = "cmdRunner";
    private static final String FILE_NAME = "cmdRunnerConf.json";

    private enum ConfPara {
        processes, path, title, delaySecondsAutostart, notify, autostart, autostartBlockScript, autostartBlockCheckIntervalSeconds
    }

    private List<CmdProcess> processes;
    private String autostartBlockScript;
    private long autostartBlockIntervalSeconds;

    public Configuration() throws ReadConfigFileException, ConfigParameterException, ShowConfigException {
        fillConfig();
    }

    public void fillConfig() throws ReadConfigFileException, ConfigParameterException, ShowConfigException {
        File configFile = new File(getConfigFilePath());
        if (!configFile.exists()) {
            createAndEditConfig();
        }
        try {
            List<CmdProcess> readProcesses = new ArrayList<>();
            String file = readFile(configFile);
            file = removeComment(file);
            JSONObject root = new JSONObject(file);
            autostartBlockScript = root.optString(ConfPara.autostartBlockScript.name());
            autostartBlockIntervalSeconds = root.optLong(ConfPara.autostartBlockCheckIntervalSeconds.name());
            JSONArray processArray = root.getJSONArray(ConfPara.processes.name());
            for (Object obj : processArray) {
                readProcesses.add(getCmdProcess((JSONObject) obj));
            }
            processes = readProcesses;
        } catch (JSONException e) {
            JOptionPane.showMessageDialog(null, "Parameter missing or wrong JSON syntax.", "Reading Config",
                    JOptionPane.ERROR_MESSAGE);
            createAndEditConfig();
            fillConfig();
        }
    }

    private static CmdProcessImpl getCmdProcess(JSONObject obj) {
        String path = obj.getString(ConfPara.path.name());
        String title = obj.optString(ConfPara.title.name(), path);
        boolean notify = obj.optBoolean(ConfPara.notify.name(), true);
        boolean autostart = obj.optBoolean(ConfPara.autostart.name(), true);
        int delaySeconds = obj.optInt(ConfPara.delaySecondsAutostart.name(), 0);
        return new CmdProcessImpl(path, title, delaySeconds, notify, autostart);
    }

    public void createAndEditConfig() throws ShowConfigException {
        try {
            File conf = new File(getConfigFilePath());
            if (!conf.exists()) {
                generateDefaultConfigFile();
            }

            if (isWindows()) {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("notepad.exe", getConfigFilePath());
                Process process = processBuilder.start();
                process.waitFor();
            } else {
                Desktop.getDesktop().open(conf);
                JOptionPane.showMessageDialog(null, "Please confirm when you have edited the configuration.", "Edit Config",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            throw new ShowConfigException("Can not open config file");
        }
    }

    private void generateDefaultConfigFile() throws ShowConfigException {
        // build default JSON
        JSONObject root = buildDefaultJsonConfig();

        String ls = System.lineSeparator();
        StringBuilder bld = new StringBuilder("/*");
        bld.append(ls).append("Configure your programs in a JSON array.");
        bld.append(ls).append("All fields except \"path\" are optional.");
        bld.append(ls).append("*/").append(ls);
        bld.append(root.toString(2));

        File dir = new File(getProgramDir());
        if (!dir.exists()) {
            if (!dir.mkdirs()) throw new ShowConfigException("Unable to create program directory: " + getProgramDir());
        }

        // generateFile
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(getConfigFilePath()))) {
            bw.write(bld.toString());
            bw.newLine();
        } catch (IOException e) {
            throw new ShowConfigException("Unable to create config file: " + getConfigFilePath() + ls + e.getMessage());
        }
    }

    private static JSONObject buildDefaultJsonConfig() {
        JSONObject root = new JSONObject();
        root.put(ConfPara.autostartBlockScript.name(), "C:\\example.bat");
        root.put(ConfPara.autostartBlockCheckIntervalSeconds.name(), 25L);
        JSONArray programs = new JSONArray();
        JSONObject program = new JSONObject();
        program.put(ConfPara.path.name(), "C:\\example.bat");
        program.put(ConfPara.title.name(), "Example Program");
        program.put(ConfPara.notify.name(), true);
        program.put(ConfPara.autostart.name(), true);
        program.put(ConfPara.delaySecondsAutostart.name(), 0);
        programs.put(program);
        root.put(ConfPara.processes.name(), programs);
        return root;
    }

    private String getConfigFilePath() {
        return getProgramDir() + File.separator + FILE_NAME;
    }

    private String readFile(File configFile) throws ReadConfigFileException {
        List<String> lines;
        try {
            lines = Files.readAllLines(configFile.toPath());
        } catch (IOException e) {
            throw new ReadConfigFileException("Config File not readable", e);
        }

        StringBuilder configString = new StringBuilder();
        for (String line : lines) {
            configString.append(line);
        }

        return configString.toString();
    }

    public String getProgramDir() {
        if (isWindows())
            return System.getenv("APPDATA") + File.separator + PROGRAM_NAME;
        else
            return System.getProperty("user.home") + File.separator + PROGRAM_NAME;
    }

    public Stream<CmdProcess> getProcesses() {
        return processes.stream();
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private String removeComment(String file) {
        return file.replaceAll("/\\*.*\\*/", "");
    }

    public String getAutostartBlockScript() {
        return autostartBlockScript;
    }

    public long getAutostartBlockIntervalSeconds() {
        return autostartBlockIntervalSeconds;
    }

}

package data;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import general.exception.config.ConfigParameterException;
import general.exception.config.ReadConfigFileException;
import general.exception.config.ShowConfigException;

public class Configuration {
  private static final String PROGRAM_NAME = "cmdRunner";
  private static final String FILE_NAME = "cmdRunnerConf.json";

  private static enum ConfPara {
    path, title, delaySecondsAutostart, notify, autostart;
  }

  private List<CmdProcess> processes;

  public Configuration() throws ReadConfigFileException, ConfigParameterException, ShowConfigException {
    fillConfig();
  }

  public void fillConfig() throws ReadConfigFileException, ConfigParameterException, ShowConfigException {
    File configFile = new File(getConfigFilePath());
    if(!configFile.exists()) {
      createAndEditConfig();
    }
    processes = new ArrayList<>();
    try {
      String file = readFile(configFile);
      file = removeComment(file);
      JSONArray root = new JSONArray(file);
      for(Object obj : root) {
        JSONObject jobj = (JSONObject)obj;
        String path = jobj.getString(ConfPara.path.name());
        String title = jobj.optString(ConfPara.title.name(), path);
        boolean notify = jobj.optBoolean(ConfPara.notify.name(), true);
        boolean autostart = jobj.optBoolean(ConfPara.autostart.name(), true);
        int delaySeconds = jobj.optInt(ConfPara.delaySecondsAutostart.name(), 0);
        CmdProcessImpl proc = new CmdProcessImpl(path, title, delaySeconds, notify, autostart);
        processes.add(proc);
      }
    } catch(JSONException e) {
      JOptionPane.showMessageDialog(null, "Parameter missing or wrong JSON syntax.", "Reading Config",
          JOptionPane.ERROR_MESSAGE);
      createAndEditConfig();
      fillConfig();
    }
  }

  public void createAndEditConfig() throws ShowConfigException {
    try {
      File conf = new File(getConfigFilePath());
      if(!conf.exists()) {
        generateDefaultConfigFile();
      }

      if(isWindows()) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("notepad.exe", getConfigFilePath());
        Process process = processBuilder.start();
        process.waitFor();
      } else {
        Desktop.getDesktop().open(conf);
        JOptionPane.showMessageDialog(null, "Please confirm when you have edited the configuration.", "Edit Config",
            JOptionPane.INFORMATION_MESSAGE);
      }
    } catch(IOException | InterruptedException e) {
      throw new ShowConfigException("Can not open config file");
    }
  }

  private void generateDefaultConfigFile() {
    // build default JSON
    JSONArray programs = new JSONArray();
    JSONObject program = new JSONObject();
    program.put(ConfPara.path.name(), "C:\\example.bat");
    program.put(ConfPara.title.name(), "Example Program");
    program.put(ConfPara.notify.name(), true);
    program.put(ConfPara.autostart.name(), true);
    program.put(ConfPara.delaySecondsAutostart.name(), 0);
    programs.put(program);

    String ls = System.lineSeparator();
    StringBuilder bld = new StringBuilder("/*");
    bld.append(ls + "Configure your programs in a JSON array.");
    bld.append(ls + "All fields except \"path\" are optional.");
    bld.append(ls + "*/" + ls);
    bld.append(programs.toString(2));

    if(!new File(getProgramDir()).exists()) {
      (new File(getProgramDir())).mkdirs();
    }

    // generateFile
    FileWriter fw = null;
    BufferedWriter bw = null;
    try {
      fw = new FileWriter(getConfigFilePath());
      bw = new BufferedWriter(fw);

      bw.write(bld.toString());
      bw.newLine();

      bw.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  private String getConfigFilePath() {
    return getProgramDir() + File.separator + FILE_NAME;
  }

  private String readFile(File configFile) throws ReadConfigFileException {
    List<String> lines = Collections.emptyList();
    try {
      lines = Files.readAllLines(configFile.toPath());
    } catch(IOException e) {
      throw new ReadConfigFileException("Config File not readable", e);
    }

    StringBuilder configString = new StringBuilder();
    for(String line : lines) {
      configString.append(line);
    }

    return configString.toString();
  }

  public String getProgramDir() {
    if(isWindows())
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

}

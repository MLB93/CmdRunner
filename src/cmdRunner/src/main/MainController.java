package main;

import data.CmdProcess;
import data.Configuration;
import general.exception.config.ConfigException;
import general.exception.gui.NoSystemTrayException;
import general.exception.process.AlreadyRunningException;
import gui.TrayIconManager;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MainController {
    public static void main(String[] args) {
        ParseResult parseResult = parseArguments(args);

        if (parseResult.helpRequested) {
            showHelp();
            System.exit(0);
        }

        String programDir = parseResult.programDir;

        try {
            Configuration config = programDir != null ? new Configuration(programDir) : new Configuration();
            TrayIconManager tim = new TrayIconManager(config);

            MainController controller = new MainController(config, tim);
            try {
                controller.startProcesses();
            } catch (AutostartBlockException e) {
                tim.showErrorMessage(e.getClass().getSimpleName(), e.getMessage());
            }
        } catch (ConfigException | NoSystemTrayException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Structured result of argument parsing. Easy to extend with more fields later.
     */
    private static class ParseResult {
        final String programDir;
        final boolean helpRequested;

        ParseResult(String programDir, boolean helpRequested) {
            this.programDir = programDir;
            this.helpRequested = helpRequested;
        }
    }

    private static ParseResult parseArguments(String[] args) {
        String programDir = null;
        boolean helpRequested = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--help") || arg.equals("-h")) {
                helpRequested = true;
            } else if (arg.equals("--dir") || arg.equals("-d")) {
                if (i + 1 < args.length) {
                    programDir = args[++i];
                }
            }
        }

        return new ParseResult(programDir, helpRequested);
    }

    private static void showHelp() {
        String help = "CmdRunner - Command Runner and Process Management Tool\n\n" +
                "Usage: java -jar cmdRunner.jar [OPTIONS]\n\n" +
                "Options:\n" +
                "  -d, --dir <PATH>        Specify the program directory for configuration files\n" +
                "                          If not specified, the default directory will be used:\n" +
                "                          - Windows: %APPDATA%\\cmdRunner\n" +
                "                          - Linux/Mac: ~/.cmdRunner\n" +
                "  -h, --help              Show this help message\n\n" +
                "Examples:\n" +
                "  java -jar cmdRunner.jar\n" +
                "  java -jar cmdRunner.jar --dir C:\\MyConfig\n" +
                "  java -jar cmdRunner.jar -d /home/user/config\n" +
                "  java -jar cmdRunner.jar --help";

        System.out.println(help);
    }

    private final Configuration config;
    private final TrayIconManager tim;

    public MainController(Configuration config, TrayIconManager tim) throws NoSystemTrayException {
        this.config = config;
        this.tim = tim;

        tim.addEditConfigListener(getEditConfigListener());
        tim.addExitListener(getExitListener());
        tim.showGui();
    }

    private ActionListener getEditConfigListener() {
        return e -> {
            try {
                config.createAndEditConfig();
                stopProcesses();
                config.fillConfig();
                tim.generatePopupMenu();
                startProcesses();
            } catch (ConfigException | AutostartBlockException e1) {
                JOptionPane.showMessageDialog(null, e1.getMessage(), e1.getClass().getSimpleName(),
                        JOptionPane.ERROR_MESSAGE);
            }
        };
    }

    private ActionListener getExitListener() {
        return e -> {
            stopProcesses();
            tim.remove();
            System.exit(0);
        };
    }

    private void startProcesses() throws AutostartBlockException {
        blockAutostart();
        for (CmdProcess proc : config.getProcesses().collect(Collectors.toList())) {
            try {
                proc.autoStart(tim);
            } catch (AlreadyRunningException e) {
                // Nothing to do, process is maybe already started manually
            }
        }
    }

    private void stopProcesses() {
        for (CmdProcess proc : config.getProcesses().collect(Collectors.toList())) {
            proc.destroy();
        }
    }

    private void blockAutostart() throws AutostartBlockException {
        if (config.getAutostartBlockScript() == null)
            return;

        while (executeBlockScript(config.getAutostartBlockScript()) != 0) {
            if (config.getAutostartBlockIntervalSeconds() != 0)
                try {
                    TimeUnit.SECONDS.sleep(config.getAutostartBlockIntervalSeconds());
                } catch (InterruptedException ignored) {
                }
            else
                throw new AutostartBlockException("Autostart block script failed");
        }
    }

    private int executeBlockScript(String script) throws AutostartBlockException {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(script);
            Process process = processBuilder.start();
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new AutostartBlockException(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static class AutostartBlockException extends Exception {
        public AutostartBlockException(String message) {
            super(message);
        }
    }
}

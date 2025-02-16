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
        try {
            Configuration config = new Configuration();
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

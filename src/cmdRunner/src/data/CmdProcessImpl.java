package data;

import general.exception.process.AlreadyRunningException;
import gui.UserCommunicator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CmdProcessImpl implements CmdProcess {
    private final String path;
    private final String title;
    private final int delaySeconds;
    private final boolean notify;
    private final boolean autostart;

    private Process process = null;
    private final List<String> output = Collections.synchronizedList(new ArrayList<>());

    private final List<PropertyChangeListener> runningChangeListener = new ArrayList<>();

    public CmdProcessImpl(String path, String title, int delaySeconds, boolean notify, boolean autostart) {
        super();
        this.path = path;
        this.title = title;
        this.delaySeconds = delaySeconds;
        this.notify = notify;
        this.autostart = autostart;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getDelaySeconds() {
        return delaySeconds;
    }

    @Override
    public boolean isNotify() {
        return notify;
    }

    @Override
    public boolean isAutostart() {
        return autostart;
    }

    @Override
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    @Override
    public String getOutput() {
        StringBuilder bld = new StringBuilder();
        synchronized (output) {
            for (String line : output) {
                bld.append(line).append(System.lineSeparator());
            }
        }
        return bld.toString();
    }

    @Override
    public void destroy() {
        if (process != null) {
            List<ProcessHandle> subProcesses = process.descendants().collect(Collectors.toList());// TODO
            for (ProcessHandle sub : subProcesses) {
                sub.destroy();
            }
            process.destroy();
        }
    }

    @Override
    public void start(UserCommunicator comm) throws AlreadyRunningException {
        if (process != null && process.isAlive()) {
            throw new AlreadyRunningException(title);
        }

        Thread thread = new Thread(() -> {
            synchronized (runningChangeListener) {
                try {
                    if (notify)
                        comm.showInfoMessage(title + " started", "The cmd process " + title + " started");
                    callRunningPropertyChangeListener(true);
                    ProcessBuilder processBuilder = new ProcessBuilder();
                    processBuilder.command(path);
                    process = processBuilder.start();
                    if (process.getInputStream() != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                addConsoleOutput(line);
                            }
                        }
                    }
                    process.waitFor();
                    if (notify)
                        comm.showInfoMessage(title + " terminated", "The cmd process " + title + " is terminated");
                } catch (IOException | InterruptedException e) {
                    comm.showErrorMessage("Error: " + title, e.getClass().getSimpleName() + ": " + e.getMessage());
                }
                callRunningPropertyChangeListener(false);
            }
        });
        thread.start();
    }

    @Override
    public void autoStart(UserCommunicator comm) {
        if (!autostart)
            return;
        Thread waitThread = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(delaySeconds);
                start(comm);
            } catch (InterruptedException | AlreadyRunningException ignored) {
            }
        });
        waitThread.start();
    }

    @Override
    public void restart(UserCommunicator comm) {
        destroy();
        try {
            while (isAlive()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
            start(comm);
        } catch (AlreadyRunningException ignored) {
        }
    }

    @Override
    public void addRunningPropertyChangeListener(PropertyChangeListener listener) {
        runningChangeListener.add(listener);
    }

    private void callRunningPropertyChangeListener(boolean running) {
        for (PropertyChangeListener listener : runningChangeListener) {
            listener.propertyChange(new PropertyChangeEvent(title, RUNNING_PROPERTY, !running, running));
        }
    }

    private void addConsoleOutput(String line) {
        synchronized (output) {
            output.add(line);
            if (output.size() > 1000)
                output.remove(0);
        }
    }

}

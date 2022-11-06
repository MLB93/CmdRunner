package data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import gui.UserCommunicator;

public class CmdProcessImpl implements CmdProcess{
	private String path;
	private String title;
	private int delaySeconds;
	private boolean notify;
	private boolean autostart;

	private Process process = null;
	private List<String> output = Collections.synchronizedList(new ArrayList<>());

	public CmdProcessImpl(String path, String title, int delaySeconds, boolean notify, boolean autostart) {
		super();
		this.path = path;
		this.title = title;
		this.delaySeconds = delaySeconds;
		this.notify = notify;
		this.autostart = autostart;
	}

	public String getPath() {
		return path;
	}

	public String getTitle() {
		return title;
	}

	public int getDelaySeconds() {
		return delaySeconds;
	}

	public boolean isNotify() {
		return notify;
	}

	public boolean isAutostart() {
		return autostart;
	}

	public boolean isAlive() {
		return process != null ? process.isAlive() : false;
	}

	public String getOutput() {
		StringBuilder bld = new StringBuilder();
		synchronized (output) {
			for (String line : output) {
				bld.append(line + System.lineSeparator());
			}
		}
		return bld.toString();
	}

	public void destroy() {
		if (process != null)
			process.destroy();
	}

	public void start(UserCommunicator comm) throws Exception {
		if (process != null && process.isAlive()) {
			throw new Exception("Process is Running");// TODO
		}
		TimeUnit.SECONDS.sleep(delaySeconds);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ProcessBuilder processBuilder = new ProcessBuilder();
					processBuilder.command(path);
					process = processBuilder.start();
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
						String line;
						while ((line = reader.readLine()) != null) {
							addConsoleOutput(line);
						}
					}
					process.waitFor();
					if (notify)
						comm.showInfoMessage(title + " terminated", "the cmd process " + title + "is terminated");
				} catch (IOException | InterruptedException e) {
					comm.showErrorMessage("Error: " + title, e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		});
		thread.start();
		if (notify)
			comm.showInfoMessage(title + " started", "the cmd process " + title + " is stared");
	}

	private void addConsoleOutput(String line) {
		synchronized (output) {
			output.add(line);
			if (output.size() > 1000)
				output.remove(0);
		}
	}

}

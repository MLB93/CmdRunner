package main;

import data.CmdProcess;
import data.Configuration;
import general.exception.config.ConfigParameterException;
import general.exception.config.ReadConfigFileException;
import general.exception.process.AlreadyRunningException;
import gui.TrayIconManager;

public class Main {
	public static void main(String[] args)
			throws ReadConfigFileException, ConfigParameterException, AlreadyRunningException {
		Configuration config = new Configuration();
		TrayIconManager tim = new TrayIconManager(config);

		for (CmdProcess proc : config.getProcesses().toList()) {
			proc.autoStart(tim);
		}
	}
}

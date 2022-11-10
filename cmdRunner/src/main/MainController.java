package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import data.CmdProcess;
import data.Configuration;
import general.exception.config.ConfigException;
import general.exception.process.AlreadyRunningException;
import gui.TrayIconManager;

public class MainController {
	public static void main(String[] args) {
		try {
			Configuration config = new Configuration();
			TrayIconManager tim = new TrayIconManager(config);

			MainController controller = new MainController(config, tim);
			controller.startProcesses();
		} catch (ConfigException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().getSimpleName(),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private Configuration config;
	private TrayIconManager tim;

	public MainController(Configuration config, TrayIconManager tim) {
		this.config = config;
		this.tim = tim;

		tim.addEditConfigListener(getEditConfigListener());
		tim.addExitListener(getExitListener());
		tim.showGui();
	}

	private void startProcesses() {
		for (CmdProcess proc : config.getProcesses().collect(Collectors.toList())) {
			try {
				proc.autoStart(tim);
			} catch (AlreadyRunningException e) {
				// Nothing to do, process is may be already started manually
			}
		}
	}

	private void stopProcesses() {
		for (CmdProcess proc : config.getProcesses().collect(Collectors.toList())) {
			proc.destroy();
			while (proc.isAlive()) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private ActionListener getEditConfigListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					config.createAndEditConfig();
					stopProcesses();
					config.fillConfig();
					tim.generatePopupMenu();
					startProcesses();
				} catch (ConfigException e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), e1.getClass().getSimpleName(),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
	}

	private ActionListener getExitListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopProcesses();
				tim.remove();
				System.exit(0);
			}
		};
	}
}

package gui;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;

import data.CmdProcess;
import data.Configuration;

public class TrayIconManager implements UserCommunicator {

	private final TrayIcon trayIcon;
	private Configuration config;

	public TrayIconManager(Configuration config) {
		this.config = config;
		trayIcon = new TrayIcon(createImage("img/cmd.png", "tray icon"));
		showGui();
	}

	public void showGui() {
		// Check the SystemTray support
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}

		trayIcon.setImageAutoSize(true);
		trayIcon.setToolTip("cmdRunner");
		final SystemTray tray = SystemTray.getSystemTray();

		final PopupMenu popup = new PopupMenu();

		for (MenuItem item : createProcessMenuItems(config.getProcesses().toList())) {
			popup.add(item);
		}

		popup.addSeparator();
		MenuItem aboutItem = new MenuItem("About");
		aboutItem.addActionListener(getAboutListener());
		popup.add(aboutItem);
		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(getExitListener(trayIcon, tray));
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);
		trayIcon.addActionListener(getAboutListener());

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
			return;
		}

	}

	public void showErrorMessage(String title, String message) {
		trayIcon.displayMessage(title, message, TrayIcon.MessageType.ERROR);
	}

	public void showWarnMessage(String title, String message) {
		trayIcon.displayMessage(title, message, TrayIcon.MessageType.WARNING);
	}

	public void showInfoMessage(String title, String message) {
		trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
	}

	private ActionListener getAboutListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "CmdRunner by MLB");
			}
		};
	}

	private ActionListener getExitListener(final TrayIcon trayIcon, final SystemTray tray) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tray.remove(trayIcon);
				System.exit(0);
			}
		};
	}

	// Obtain the image URL
	private Image createImage(String path, String description) {
		URL imageURL = TrayIconManager.class.getResource(path);

		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}

	private java.util.List<MenuItem> createProcessMenuItems(java.util.List<CmdProcess> processes) {
		java.util.List<MenuItem> items = new ArrayList<MenuItem>();
		for (CmdProcess proc : processes) {
			Menu menu = new Menu(proc.getTitle()) {
				private static final long serialVersionUID = 1L;

				@Override
				public String getLabel() {
					return super.getLabel() + (proc.isAlive() ? " 🟢" : " 🔴");
				}
			};

			MenuItem startItem = new MenuItem("start") {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isEnabled() {
					return super.isEnabled() && !proc.isAlive();
				}
			};
			startItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						proc.start(TrayIconManager.this);
					} catch (Exception e1) {
						e1.printStackTrace();// TODO
					}
				}
			});
			menu.add(startItem);

			MenuItem stopItem = new MenuItem("stop") {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isEnabled() {
					return super.isEnabled() && proc.isAlive();
				}
			};
			stopItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					proc.destroy();
				}
			});
			menu.add(stopItem);

			MenuItem outputItem = new MenuItem("show output");
			outputItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println(proc.getOutput());// TODO
				}
			});
			menu.add(outputItem);
			items.add(menu);
		}
		return items;
	}
}

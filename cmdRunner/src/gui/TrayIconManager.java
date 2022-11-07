package gui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import data.CmdProcess;
import data.Configuration;
import general.exception.process.AlreadyRunningException;

public class TrayIconManager implements UserCommunicator {

	private final TrayIcon trayIcon;
	private SystemTray tray;
	private Configuration config;

	public TrayIconManager(Configuration config) {
		this.config = config;
		trayIcon = new TrayIcon(createImage("img/cmd.png", "tray icon"));
		if (SystemTray.isSupported()) {
			this.tray = SystemTray.getSystemTray();
		}
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

		generatePopupMenu();
		trayIcon.addActionListener(getAboutListener());

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
			return;
		}

	}

	private void generatePopupMenu() {
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
	}

	@Override
	public void showErrorMessage(String title, String message) {
		trayIcon.displayMessage(title, message, TrayIcon.MessageType.ERROR);
	}

	@Override
	public void showWarnMessage(String title, String message) {
		trayIcon.displayMessage(title, message, TrayIcon.MessageType.WARNING);
	}

	@Override
	public void showInfoMessage(String title, String message) {
		trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
	}

	private ActionListener getAboutListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "CmdRunner by MLB");
			}
		};
	}

	private ActionListener getExitListener(final TrayIcon trayIcon, final SystemTray tray) {
		return new ActionListener() {
			@Override
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
			Menu menu = new Menu(proc.getTitle() + (proc.isAlive() ? " ∞" : ""));

			MenuItem startItem = new MenuItem("start");
			startItem.setEnabled(!proc.isAlive());
			startItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						proc.start(TrayIconManager.this);
					} catch (AlreadyRunningException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			menu.add(startItem);

			MenuItem stopItem = new MenuItem("stop");
			stopItem.setEnabled(proc.isAlive());
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

			PropertyChangeListener runningListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (CmdProcess.RUNNING_PROPERTY.equals(evt.getPropertyName())) {
						menu.setLabel(proc.getTitle() + ((boolean) evt.getNewValue() ? " ∞" : ""));
						startItem.setEnabled(!(boolean) evt.getNewValue());
						stopItem.setEnabled((boolean) evt.getNewValue());
					}
				}
			};
			proc.addRunningPropertyChangeListener(runningListener);
			items.add(menu);
		}
		return items;
	}
}

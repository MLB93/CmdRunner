package gui;

import java.awt.AWTException;
import java.awt.Desktop;
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
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import data.CmdProcess;
import data.Configuration;
import general.exception.gui.NoSystemTrayException;
import general.exception.process.AlreadyRunningException;

public class TrayIconManager implements UserCommunicator {

  private static final String GUI_IMG_ICON_PNG = "/gui/img/icon.png";

  public static final Image IMAGE = (new ImageIcon(
      TrayIconManager.class.getResource(GUI_IMG_ICON_PNG) != null ? TrayIconManager.class.getResource(GUI_IMG_ICON_PNG)
          : TrayIconManager.class.getResource("/src" + GUI_IMG_ICON_PNG))).getImage();

  private final TrayIcon trayIcon;
  private SystemTray tray;
  private Configuration config;
  private java.util.List<ActionListener> exitListeners = new ArrayList<>();
  private java.util.List<ActionListener> editConfigListeners = new ArrayList<>();

  public TrayIconManager(Configuration config) {
    this.config = config;
    trayIcon = new TrayIcon(IMAGE);
    if(SystemTray.isSupported()) {
      this.tray = SystemTray.getSystemTray();
    }
  }

  public void showGui() throws NoSystemTrayException {
    // Check the SystemTray support
    if(!SystemTray.isSupported()) {
      throw new NoSystemTrayException();
    }

    trayIcon.setImageAutoSize(true);
    trayIcon.setToolTip("cmdRunner");

    generatePopupMenu();
    try {
      tray.add(trayIcon);
    } catch(AWTException e) {
      throw new NoSystemTrayException();
    }

  }

  public void generatePopupMenu() {
    final PopupMenu popup = new PopupMenu();

    for(MenuItem item : createProcessMenuItems(config.getProcesses().collect(Collectors.toList()))) {
      popup.add(item);
    }

    popup.addSeparator();
    MenuItem editConfigItem = new MenuItem("Edit Config");
    for(ActionListener listener : editConfigListeners) {
      editConfigItem.addActionListener(listener);
    }
    popup.add(editConfigItem);

    MenuItem aboutItem = new MenuItem("About");
    aboutItem.addActionListener(getAboutListener());
    popup.add(aboutItem);

    MenuItem exitItem = new MenuItem("Exit");
    for(ActionListener listener : exitListeners) {
      exitItem.addActionListener(listener);
    }
    popup.add(exitItem);

    trayIcon.setPopupMenu(popup);
  }

  @Override
  public void showErrorMessage(String title, String message) {
    trayIcon.displayMessage(title, message, TrayIcon.MessageType.ERROR);
    System.out.println("ERROR: " + title + ": " + message);
  }

  @Override
  public void showWarnMessage(String title, String message) {
    trayIcon.displayMessage(title, message, TrayIcon.MessageType.WARNING);
    System.out.println("WARN: " + title + ": " + message);
  }

  @Override
  public void showInfoMessage(String title, String message) {
    trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
    System.out.println("INFO: " + title + ": " + message);
  }

  private ActionListener getAboutListener() {
    return new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          Desktop.getDesktop().browse(URI.create("https://github.com/MLB93/CmdRunner"));
        } catch(IOException e1) {
          JOptionPane.showMessageDialog(null, "https://github.com/MLB93/CmdRunner", "About",
              JOptionPane.INFORMATION_MESSAGE);
        }
      }
    };
  }

  private java.util.List<MenuItem> createProcessMenuItems(java.util.List<CmdProcess> processes) {
    java.util.List<MenuItem> items = new ArrayList<MenuItem>();
    for(CmdProcess proc : processes) {
      Menu menu = new Menu(proc.getTitle() + (proc.isAlive() ? " ∞" : ""));

      MenuItem startItem = new MenuItem("start");
      startItem.setEnabled(!proc.isAlive());
      startItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          try {
            proc.start(TrayIconManager.this);
          } catch(AlreadyRunningException e1) {
            showWarnMessage("Already running", "The process " + proc.getTitle() + " is already running.");
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

      MenuItem restartItem = new MenuItem("restart");
      restartItem.setEnabled(!proc.isAlive());
      restartItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          proc.restart(TrayIconManager.this);
        }
      });
      menu.add(restartItem);

      MenuItem outputItem = new MenuItem("show output");
      outputItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          OutputFrame.show(proc.getOutput(), proc.getTitle());
        }
      });
      menu.add(outputItem);

      PropertyChangeListener runningListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          if(CmdProcess.RUNNING_PROPERTY.equals(evt.getPropertyName())) {
            menu.setLabel(proc.getTitle() + ((boolean)evt.getNewValue() ? " ∞" : ""));
            startItem.setEnabled(!(boolean)evt.getNewValue());
            stopItem.setEnabled((boolean)evt.getNewValue());
            restartItem.setEnabled((boolean)evt.getNewValue());
          }
        }
      };
      proc.addRunningPropertyChangeListener(runningListener);
      items.add(menu);
    }
    return items;
  }

  public void addExitListener(ActionListener listener) {
    exitListeners.add(listener);
  }

  public void addEditConfigListener(ActionListener listener) {
    editConfigListeners.add(listener);
  }

  public void remove() {
    tray.remove(trayIcon);
  }
}

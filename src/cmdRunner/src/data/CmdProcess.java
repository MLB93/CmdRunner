package data;

import java.beans.PropertyChangeListener;

import general.exception.process.AlreadyRunningException;
import gui.UserCommunicator;

public interface CmdProcess {
  public static final String RUNNING_PROPERTY = "RUNNING";

  public String getPath();

  public String getTitle();

  public int getDelaySeconds();

  public boolean isNotify();

  public boolean isAutostart();

  public boolean isAlive();

  public String getOutput();

  public void destroy();

  public void start(UserCommunicator comm) throws AlreadyRunningException;

  public void autoStart(UserCommunicator comm) throws AlreadyRunningException;

  public void restart(UserCommunicator comm);

  public void addRunningPropertyChangeListener(PropertyChangeListener listener);
}

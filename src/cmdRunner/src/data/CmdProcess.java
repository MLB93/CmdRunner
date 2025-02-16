package data;

import general.exception.process.AlreadyRunningException;
import gui.UserCommunicator;

import java.beans.PropertyChangeListener;

public interface CmdProcess {
    String RUNNING_PROPERTY = "RUNNING";

    String getTitle();

    boolean isRunningInRepeatMode();

    boolean isAlive();

    String getOutput();

    void destroy();

    void start(UserCommunicator comm) throws AlreadyRunningException;

    void autoStart(UserCommunicator comm) throws AlreadyRunningException;

    void restart(UserCommunicator comm);

    void addRunningPropertyChangeListener(PropertyChangeListener listener);
}

package data;

import gui.UserCommunicator;

public interface CmdProcess {
	public String getPath();

	public String getTitle();

	public int getDelaySeconds();

	public boolean isNotify();

	public boolean isAutostart();

	public boolean isAlive();

	public String getOutput();

	public void destroy();

	public void start(UserCommunicator comm) throws Exception;
}

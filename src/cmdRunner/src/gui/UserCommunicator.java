package gui;

public interface UserCommunicator {
	public void showErrorMessage(String title, String message);

	public void showWarnMessage(String title, String message);

	public void showInfoMessage(String title, String message);

}

package gui;

public interface UserCommunicator {
    void showErrorMessage(String title, String message);

    void showWarnMessage(String title, String message);

    void showInfoMessage(String title, String message);

}

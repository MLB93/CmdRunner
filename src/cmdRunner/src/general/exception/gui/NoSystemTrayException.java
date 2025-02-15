package general.exception.gui;

public class NoSystemTrayException extends Exception {

    private static final long serialVersionUID = 1L;

    public String getMessage() {
        return "System tray is not supported";
    }
}

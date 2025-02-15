package general.exception.process;

public class AlreadyRunningException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String processName;

    public AlreadyRunningException(String processName) {
        this.processName = processName;
    }

    @Override
    public String getMessage() {
        return "CmdProcess " + processName + " is already running";
    }
}

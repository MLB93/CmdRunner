package general.exception.config;

public abstract class ConfigException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String message;

    public ConfigException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

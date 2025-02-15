package general.exception.config;

import org.json.JSONException;

public class ConfigParameterException extends ConfigException {

    private static final long serialVersionUID = 1L;

    private final JSONException exception;

    public ConfigParameterException(String message, JSONException exception) {
        super(message);
        this.exception = exception;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        message = message + System.lineSeparator() + exception.getClass().getSimpleName() + " occured:";
        message = message + System.lineSeparator() + exception.getMessage();
        return message;
    }

}

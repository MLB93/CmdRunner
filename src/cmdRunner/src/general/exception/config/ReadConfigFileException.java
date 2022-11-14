package general.exception.config;

import java.io.IOException;

public class ReadConfigFileException extends ConfigException {

	private static final long serialVersionUID = 1L;

	private IOException exception;

	public ReadConfigFileException(String message, IOException exception) {
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

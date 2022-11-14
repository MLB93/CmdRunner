package general.exception.config;

public class NoConfigFileException extends ConfigException {

	private static final long serialVersionUID = 1L;

	public NoConfigFileException(String message) {
		super(message);
	}

}

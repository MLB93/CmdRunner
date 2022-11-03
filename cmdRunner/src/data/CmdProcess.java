package data;

public class CmdProcess {
	private String path;
	private String title;
	private int delaySeconds;
	private boolean notify;
	private boolean autostart;

	public CmdProcess(String path, String title, int delaySeconds, boolean notify, boolean autostart) {
		super();
		this.path = path;
		this.title = title;
		this.delaySeconds = delaySeconds;
		this.notify = notify;
		this.autostart = autostart;
	}

	public String getPath() {
		return path;
	}

	public String getTitle() {
		return title;
	}

	public int getDelaySeconds() {
		return delaySeconds;
	}

	public boolean isNotify() {
		return notify;
	}

	public boolean isAutostart() {
		return autostart;
	}

}

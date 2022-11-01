package data;

public class CmdProcess {
	private String path;
	private String title;
	private int delaySeconds;
	private boolean notify;
	
	
	public CmdProcess(String path, String title, int delaySeconds, boolean notify) {
		super();
		this.path = path;
		this.title = title;
		this.delaySeconds = delaySeconds;
		this.notify = notify;
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
	
	
}

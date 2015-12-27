package com.coleman.utilities.http;

public abstract class DownloadProgressHandler {
	private boolean kill = false;
	public void kill() {
		kill = true;
	}
	public boolean isAlive() {
		return !kill;
	}
	public abstract void sizeDetermined(long maxBytes);
	public abstract void progressUpdate(long bytesRead);
	public abstract void downloadComplete(byte[] bytes);
	public abstract void downloadFailed(String reason);
	public abstract void updateStatus(String status);
	

	public abstract void uploadSizeDetermined(long maxBytes);
	public abstract void uploadProgressUpdate(long bytesRead);
	public abstract void uploadComplete();
	public abstract void uploadFailed(String reason);
}

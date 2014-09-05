package com.quku.entity;

public class FileSelect {

	private String path;
	private boolean isSlect;

	public String getPath() {
		return path;
	}

	public FileSelect(String path, boolean isSlect) {
		this.path = path;
		this.isSlect = isSlect;
	}

	@Override
	public String toString() {
		return "FileSelect [path=" + path + ", isSlect=" + isSlect + "]";
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isSlect() {
		return isSlect;
	}

	public void setSlect(boolean isSlect) {
		this.isSlect = isSlect;
	}
}

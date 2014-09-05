package com.quku.entity;

import java.io.Serializable;

public class MyFileInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String fileIndex;
	private String filePath;

	public String getFileIndex() {
		return fileIndex;
	}

	public MyFileInfo(String fileIndex, String filePath) {
		super();
		this.fileIndex = fileIndex;
		this.filePath = filePath;
	}

	public void setFileIndex(String fileIndex) {
		this.fileIndex = fileIndex;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}

package com.quku.entity;

/**
 * 录音文件对象
 * 
 * @author Administrator
 * 
 */
public class Record {

	private String filePath;
	private String fileName;
	private String fileSize;//KB/MB
	private int type;//文件类型,0为文件、1为目录
	private boolean isSeleted;
	public boolean isSeleted() {
		return isSeleted;
	}
	public void setSeleted(boolean isSeleted) {
		this.isSeleted = isSeleted;
	}
	public Record(){
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileSize() {
		return fileSize;
	}
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public boolean isSelect() {
		return isSelect;
	}
	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}
	private boolean isSelect;
	

}

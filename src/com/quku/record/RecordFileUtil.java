package com.quku.record;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.quku.Utils.FileUtil;
import com.quku.entity.Record;

/**
 * 录音文件目录加载
 * @author Administrator
 *
 */
public class RecordFileUtil {

	/*
	 * 获取当前目录下的文件（目录或者文件）
	 * @param path
	 */
	public static List<Record> loadFileList(String path,List<Record> currentFileList) {
		currentFileList = new ArrayList<Record>();
		File file = new File(path);
		if (file.isDirectory() && file.listFiles().length > 0) {
			for (File file1 : file.listFiles()) {
				if(file1.getPath().endsWith(".nomedia")){
					continue;
				}
				Record record = new Record();
				record.setFileName(file1.getName());
				record.setFilePath(file1.getPath());
				record.setSeleted(false);
				if(file1.isDirectory()){
					record.setType(1);
					record.setFileSize("");
				}
				else if(file1.isFile()){
					record.setType(0);
					if(file1.length() > 0){
						record.setFileSize(FileUtil.convertFileSize(file1.length()));
					}
					else{
						record.setFileSize("0 KB");
					}
				}
				currentFileList.add(record);
			}
		}
		return currentFileList;
	}
}

package com.quku.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.quku.entity.Record;

public class FileUtil {

	public static void copyFolder(String oldPath, String newPath) {
		File oldFile = new File(oldPath);
		File newFile = new File(newPath);

		if (oldFile.exists() && oldFile.isDirectory()) {
			File[] files = oldFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					String old = files[i].getName();
					File newF = new File(newFile.getPath() + File.separator
							+ old);
					if (!newFile.exists())
						newFile.mkdirs();
					copyFile(files[i].getPath(), newF.getPath());
				} else {
					String name = files[i].getName();
					copyFolder(files[i].getPath(), newPath + File.separator
							+ name);
				}
			}
		}
	}

	/**
	 * 拷贝单个文件
	 * 
	 */
	public static void copyFile(String oldPathFile, String newPathFile) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(oldPathFile);
			out = new FileOutputStream(newPathFile);
			byte[] buf = new byte[1024 * 100];
			int b = 0;
			while ((b = in.read(buf)) != -1) {
				out.write(buf);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 精确计算出文件大小
	 * @param filePath
	 * @return
	 */
	public static String convertFileSize(long filesize)
	{
		String strUnit = " Bytes";
		String strAfterComma = "";
		int intDivisor = 1;
		if (filesize >= 1024 * 1024) {
			strUnit = " MB";
			intDivisor = 1024 * 1024;
		} else if (filesize >= 1024) {
			strUnit = " KB";
			intDivisor = 1024;
		}
		if (intDivisor == 1)
			return filesize + " " + strUnit;
		strAfterComma = "" + 100 * (filesize % intDivisor) / intDivisor;
		if (strAfterComma == "")
			strAfterComma = ".0";
		return filesize / intDivisor + "." + strAfterComma + " " + strUnit;
	}
	
	
	/**
	 * 递归获取录音文件
	 * @param path
	 */
	public static List<Record> getRecursionFile(String path){
		
		return null;
	}
}

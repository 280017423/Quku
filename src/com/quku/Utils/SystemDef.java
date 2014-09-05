package com.quku.Utils;

import android.os.Environment;

public class SystemDef {

	public static class System{
		public static final String FLUSHCARD = Environment.getExternalStorageDirectory().getPath();
		public static final String ROOTDIR = "/tflash/musicpaper";
		public static final String CUSTOMPATH = "自拍乐谱";
		public static final String NOTEPATH = "原创乐谱";
		public static final String PREF_VERSION_NAME = "version";
		public static final String URL_APK = "http://hhwlkj.cn1.0623.com.cn/MusicView/apk/MusicView.apk";
		public static final String URL_VERSION = "http://hhwlkj.cn1.0623.com.cn/MusicView/apk/version.txt";
		public static final String URL_VERSION_INFO = "http://hhwlkj.cn1.0623.com.cn/MusicView/apk/versionInfo.txt";
		public static final int IMG_PICTURE_WIDTH = 768;
		public static final int IMG_PICTURE_HEIGHT = 1240;
	}
	public static class Debug{
		public static final String TAG = "MusicView";
	}
	
	public static class Database{
		public static final String DATABASE_MYNOTE = "myNote";
		public static final String TABLE_MYNOTE = "tb_myNote";
		public static final String TABLE_MYNOTELIST = "tb_myNoteList";
		public static final int VERSION = 1;
	}
	
	public static class NoteWrite{
		public static final int NOTE_WIRTE_TOTAL_NUMBER_MAX = 10;
		public static final String NOTE_EDITE = "com.musicview.note2.edite";
		public static final String NOTE_INSERT = "com.musicview.note2.insert";
		public static final int TYPE_MAKEMUSIC = 1;
		public static final int TYPE_EMPTY = 2;
		public static final int TYPE_GRID = 3;
		public static final int TYPE_STREAK = 4;
	}
	
	public static class FileManager{
		public static final String FM_ACTION_SAVE_FILE = "com.musicview.filemanager.savefile";
		public static final String FM_ACTION_FILE_BROWSE = "com.musicview.filemanager.filebrowse";
	}
	
	public static class DateFormact{
		public static final String FORMACT_YMD = "yyyy-MM-dd";
		public static final String FORMACT_YMDHMS = "yyyy-MM-dd hh:mm:ss";
	}
	public static class Record{
		public static final String RECORD_DIR = "我的录音";
		public static final String RECORD_SUFFIX = ".amr";
		
	}
}

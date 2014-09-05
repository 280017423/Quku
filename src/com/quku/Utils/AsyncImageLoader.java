//package com.musicview.Utils;
//
//import java.lang.ref.SoftReference;
//import java.util.HashMap;
//
//import android.graphics.drawable.Drawable;
//import android.graphics.drawable.LayerDrawable;
//import android.os.Handler;
//import android.os.Message;
//
///**
// * 异步加载图片缩略图处理类
// * @author Administrator
// *
// */
//public class AsyncImageLoader {
//	
//	public SoftReference<String> _fileName;
//	public HashMap<String, SoftReference<String>> fileCache;
//
//	public AsyncImageLoader() {
//		fileCache = new HashMap<String, SoftReference<String>>();
//	}
//
//	public String loadFileName(final String fileName,
//			final FileCallback fileCallback) {
//		if (fileCache.containsKey(fileName)) {
//			_fileName = fileCache.get(fileName);
//			String fileName1 = _fileName.get();
//			if (fileName1 != null) {
//				return fileName1;
//			}
//			
//		}
//		final Handler handler = new Handler() {
//			public void handleMessage(Message message) {
//				fileCallback
//						.fileNameLoad((String) message.obj, fileName);
//			}
//		};
//		new Thread() {
//			@Override
//			public void run() {
//				System.out.println( " AsyncImageLoader imageUrl : " + fileName);
//				LayerDrawable ld = thumUtils.fillImageView2(fileName);
//				SoftReference<LayerDrawable> sr = new SoftReference<LayerDrawable>(ld);
//				fileCache.put(fileName, sr);
//				Message message = handler.obtainMessage(0, ld);
//				handler.sendMessage(message);
//			}
//		}.start();
//		return null;
//	}
//
//	public interface FileCallback {
//		public void fileNameLoad(String path, String fileName);
//	}
//}

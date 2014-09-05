package com.quku.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.quku.R;
import com.quku.Utils.SystemDef;
import com.quku.adapter.PreViewImgActivity;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

	private final static String TAG = "camera";;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Camera mCam;
	private boolean hasStartPreview = false;
	private Button btnTakePicture;
	private String path;
	private File fileDirectory;
	private boolean auto = false;
	private ImageView autoImage;
	private Button backHome;//退出
	MediaPlayer shootMP = null;
	private String fileName;
	AudioManager meng = null;
//	 private ProgressDialog progressDialog = null; 
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().getDecorView().setSystemUiVisibility(4); //屏蔽状态栏
		setContentView(R.layout.camera_activity);
		path = "/mnt/sdcard/cameratest/"; // 指定照片存放的路径;
		// 取得surfaceView的引用,surface在surfaceView中展现
		// 当surfaceView可见时，surface会被创建，实现surfaceCreated进行特定操作
		surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		btnTakePicture = (Button) findViewById(R.id.cameraButton);
//		photoExplorer = (Button) findViewById(R.id.explorerImageBtn);
//		toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
		autoImage = (ImageView)findViewById(R.id.autofoucs);
		backHome = (Button)findViewById(R.id.backhome);
		backHome.setOnClickListener(new MyButtonListener());
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	class MyButtonListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.backhome:
//				Intent intent = new Intent();
//				intent.setClass(CameraActivity.this, DanteFirstPage.class);
//				CameraActivity.this.startActivity(intent);
				CameraActivity.this.finish();
//				mCam = null;
//				hasStartPreview = false;
				break;
			}
		}
		
	}
	
	// surface只能够由一个线程操作，一旦被操作，其他线程就无法操作surface
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
		if(null != mCam){
			Camera.Parameters parameters = mCam.getParameters();   
			mCam.setParameters(parameters);   
			mCam.startPreview();   
			autoImage.setBackgroundResource(R.drawable.cam_focus_indicator_white_icn);
		}
         //以下注释掉的是设置预览时的图像以及拍照的一些参数   
         // parameters.setPictureFormat(PixelFormat.JPEG);   
         // parameters.setPreviewSize(parameters.getPictureSize().width,   
         // parameters.getPictureSize().height);   
         // parameters.setFocusMode("auto");   
         // parameters.setPictureSize(width, height);   
	}

	// 在surfaceCreated后调用，当surface发生变化也会触发该方法
	// 这个方法一般至少被调用一次
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i(TAG, "surfaceChanged");
         try {
			mCam.setPreviewDisplay(surfaceHolder);
		} catch (IOException e) {
			 mCam.release();   
             mCam = null;   
             e.printStackTrace(); 
		} 
	}

	// 释放surface
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		if(null != mCam){
			//顾名思义可以看懂   
			mCam.stopPreview();   
			mCam.release();   
			mCam = null;   
		}
	}

	 @Override    
	 public boolean onTouchEvent(MotionEvent event) {
		 if(mCam == null)return true;
		 //屏幕触摸事件
		 if (event.getAction() == MotionEvent.ACTION_DOWN) {
			 //按下时自动对焦            
			 mCam.autoFocus(new AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean success, Camera arg1) {
					// TODO Auto-generated method stub
					if(success){
						auto = true;
						autoImage.setBackgroundResource(R.drawable.cam_focus_indicator_green_icn);
					}
				}
			});       
			 
		 }        
		 return true;  
	 }
	
	@Override
	protected void onStart() {
		super.onStart();
		// 注册按钮实现拍照
		btnTakePicture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnTakePicture.setEnabled(false);
				if (mCam == null) {
					mCam = Camera.open();
				}
				if (mCam != null) {
					// 调用mCam进行拍照
					shootSound();
					mCam.takePicture(null, null, pictureCallBack);
				}
			}
		});
	}

	// onPause比surfaceDestroyed() 先调用
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		if (hasStartPreview) {
			mCam.stopPreview();
		}
		try {
			mCam.release();
			mCam = null;
			hasStartPreview = false;
		} catch (Exception e) {
			Log.d(SystemDef.Debug.TAG, TAG + " onPause exception = 	" + e.getMessage());
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		if (mCam == null)
			mCam = Camera.open();
		super.onResume();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (mCam == null) {
			mCam = Camera.open();
		}
	}
	
	Handler myHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 1:
				// 处理完照片,直接调转，预览
				Intent intent = new Intent();
				intent.setClass(CameraActivity.this, PreViewImgActivity.class);
				intent.putExtra("photoPath", (String)msg.obj);
				CameraActivity.this.startActivity(intent);
				CameraActivity.this.finish();
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	
	class SavePictureTask extends AsyncTask<byte[], String, String> {

        @Override
        protected String doInBackground(byte[]... params) {
        	if (mCam != null) {
				FileOutputStream fout = null;
				fileDirectory = new File(path);
				String filePath = path+getFileName();
				try {
					if (!fileDirectory.exists()) {
						fileDirectory.mkdirs();
					}
					fout = new FileOutputStream(filePath); 
					Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(params[0], 0, params[0].length),
															 SystemDef.System.IMG_PICTURE_HEIGHT, SystemDef.System.IMG_PICTURE_WIDTH, false);
					//创建新bitmap
					Matrix m = new Matrix();
					int width = bitmap.getWidth();
					int height = bitmap.getHeight();
					m.setRotate(90); // 旋转90度
					Bitmap newBitMap =  Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);// 从新生成图片
					bitmap.recycle();
					newBitMap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
					Message msg = new Message();
					msg.what = 1;
					msg.obj = filePath;
					myHandler.sendMessage(msg);
				} catch (IOException e) {
					e.printStackTrace();
				} 
				finally {
					try {
						fout.close();
						btnTakePicture.setEnabled(true);
						/*if (hasStartPreview) {
							mCam.startPreview();
							autoImage.setBackgroundResource(R.drawable.cam_focus_indicator_white_icn);
						}*/
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
//                // 创建文件
//                File picture = new File(Environment.getExternalStorageDirectory(), "picture.jpg");
//                // 如果文件存在删除掉
//                if (picture.exists())
//                        picture.delete();
//                try {
//                        // 获得文件输出流
//                        FileOutputStream fos = new FileOutputStream(picture.getPath());
//                        // 写入文件
//                        fos.write(params[0]);
//                        // 关闭文件流
//                        fos.close();
//                } catch (Exception e) {
//                        e.printStackTrace();
//                }
//                return null;
        	return null;
        }
}
	private PictureCallback pictureCallBack = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			setFileName(System.currentTimeMillis()+".jpg");
//			 progressDialog = ProgressDialog.show(CameraActivity.this, null, "处理中");
			 new SavePictureTask().execute(data);
		}
	};
	
	/** 
	 *   播放系统拍照声音 
	. */  
	public void shootSound(){  
	    meng = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);  
	    int volume = meng.getStreamVolume( AudioManager.STREAM_NOTIFICATION);  
	    try {
			if (volume != 0) {
				if (shootMP == null)
					shootMP = MediaPlayer
							.create(getBaseContext(),
									Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
				if (shootMP != null)
					shootMP.start();
			}
		} catch (Exception e) {
			Log.d(SystemDef.Debug.TAG, TAG + " shootSound exception = " + e.getMessage());
		}  
	}  


}
package com.quku.adapter;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.quku.R;
import com.quku.Utils.SystemDef;
import com.quku.activity.FileManagerActivity;
import com.quku.camera.CameraActivity;
import com.quku.camera.DrawableAnimation;

public class PreViewImgActivity extends Activity implements OnClickListener {
	private static final int MESSAE_START_SCAN_ANIMATION = 1;// 扫描动画
	private static final int MESSAGE_START_CUT_ANIMATION = 2;// 图片切边动画
	// private ImageView image;
	private Button cancel;
	private Button save;
	String filePath = null;
	Bitmap imageBitmap;
	LinearLayout imageLayout;
	DrawableAnimation drawableAnimation;// 自定义drawableAnimation动画
	private Handler mHandler;// 启动动画消息的handler对象
	ProgressBar progressbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 没有标题
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 高亮
		getWindow().getDecorView().setSystemUiVisibility(4); // 屏蔽状态栏
		setContentView(R.layout.previewlayout);
		filePath = getIntent().getStringExtra("photoPath");
		initView();
	}

	private void initView() {
		// image = (ImageView) this.findViewById(R.id.imageid);
		imageLayout = (LinearLayout) this.findViewById(R.id.imageLayout);
		progressbar = (ProgressBar) this.findViewById(R.id.progressbar);
		cancel = (Button) this.findViewById(R.id.cancel);
		save = (Button) this.findViewById(R.id.save);
		cancel.setOnClickListener(this);
		save.setOnClickListener(this);
		imageBitmap = this.scalePicture(filePath, SystemDef.System.IMG_PICTURE_HEIGHT,
				SystemDef.System.IMG_PICTURE_WIDTH);
		// drawableAnimation = new
		// DrawableAnimation(this,bitmap2Drawable(imageBitmap),
		// this.getResources().getDrawable(R.drawable.line1),mHandler);
		drawableAnimation = new DrawableAnimation(this, bitmap2Drawable(imageBitmap), this.getResources().getDrawable(
				R.drawable.like_line8), mHandler);
		imageLayout.addView(drawableAnimation);
		// handler.post(r);
		// Matrix m = new Matrix();
		// int width = bitmap.getWidth();
		// int height = bitmap.getHeight();
		// m.setRotate(90); // 旋转90度
		// bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);//
		// 从新生成图片
		// image.setImageBitmap(imageBitmap);
	}

	/**
	 * 处理进度条handler
	 */
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			progressbar.setProgress(msg.what);
			if (msg.what == 100) {
				progressbar.setVisibility(View.GONE);
				drawableAnimation.mHandler.sendEmptyMessage(MESSAE_START_SCAN_ANIMATION);
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * 处理进度条线程
	 */
	Runnable r = new Runnable() {

		int i = 10;

		@Override
		public void run() {
			if (i == 10) {
				drawableAnimation.myHandler();
				drawableAnimation.mHandler.sendEmptyMessage(MESSAGE_START_CUT_ANIMATION);
				progressbar.setVisibility(View.VISIBLE);
			}
			i = i + 10;
			if (i >= 110) {
				handler.removeCallbacks(r);
			} else {
				handler.sendEmptyMessage(i);
				handler.postDelayed(r, 1000);
			}

		}
	};

	private void viewAnimation() {
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation left2Right = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 100f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f);
		left2Right.setDuration(1000);
		left2Right.setStartTime(1000);

		// TranslateAnimation right2Left = new TranslateAnimation(
		// Animation.RELATIVE_TO_SELF, 0f,
		// Animation.RELATIVE_TO_SELF, 0f,
		// Animation.RELATIVE_TO_SELF, 768f,
		// Animation.RELATIVE_TO_SELF, 760f);
		// right2Left.setDuration(1000);
		// right2Left.setStartTime(1000);
		// AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
		// alphaAnimation.setDuration(3000);
		// animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(left2Right);
		// animationSet.addAnimation(right2Left);
		animationSet.setFillAfter(true);
		// imageLayout.getChildAt(0);
		imageLayout.getChildAt(0).startAnimation(animationSet);
		// imageView.setVisibility(View.GONE);
		// imageView.invalidate();
		animationSet.setFillAfter(false);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.cancel:
				File file = new File(filePath);
				if (null != file && file.exists()) {
					file.delete();
				}
				Intent intent1 = new Intent();
				intent1.setClass(PreViewImgActivity.this, CameraActivity.class);
				PreViewImgActivity.this.startActivity(intent1);
				PreViewImgActivity.this.finish();
				break;
			case R.id.save:
				if (imageBitmap != null) {
					try {
						Intent intent = new Intent();
						intent.setClass(PreViewImgActivity.this, FileManagerActivity.class);
						intent.setAction(SystemDef.FileManager.FM_ACTION_SAVE_FILE);
						intent.putExtra("photoPath", filePath);
						PreViewImgActivity.this.startActivity(intent);
						PreViewImgActivity.this.finish();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
		}

	}

	/**
	 * bitmap转化为drawable
	 * 
	 * @param bitmap
	 * @return
	 */
	private Drawable bitmap2Drawable(Bitmap bitmap) {
		return new BitmapDrawable(bitmap);
	}

	/**
	 * drawble转化为bitmap
	 * 
	 * @param drawable
	 * @return
	 */
	public Bitmap drawable2Bitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		} else if (drawable instanceof NinePatchDrawable) {
			Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
					drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			drawable.draw(canvas);
			return bitmap;
		} else {
			return null;
		}
	}

	/**
	 * 图片缩放处理
	 * 
	 * @param filename
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	public Bitmap scalePicture(String filename, int maxWidth, int maxHeight) {
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			BitmapFactory.decodeFile(filename, opts);
			int srcWidth = opts.outWidth;
			int srcHeight = opts.outHeight;
			int desWidth = 0;
			int desHeight = 0;
			// 缩放比例
			double ratio = 0.0;
			if (srcWidth > srcHeight) {
				ratio = srcWidth / maxWidth;
				desWidth = maxWidth;
				desHeight = (int) (srcHeight / ratio);
			} else {
				ratio = srcHeight / maxHeight;
				desHeight = maxHeight;
				desWidth = (int) (srcWidth / ratio);
			}
			// 设置输出宽度、高度
			BitmapFactory.Options newOpts = new BitmapFactory.Options();
			newOpts.inSampleSize = (int) (ratio);
			newOpts.inJustDecodeBounds = false;
			newOpts.outWidth = desWidth;
			newOpts.outHeight = desHeight;
			bitmap = BitmapFactory.decodeFile(filename, newOpts);

		} catch (Exception e) {
			// TODO: handle exception
		}
		return bitmap;
	}

	@Override
	protected void onStop() {
		// if (bitmap != null) {
		// bitmap.recycle();
		// }
		super.onStop();
	}

}

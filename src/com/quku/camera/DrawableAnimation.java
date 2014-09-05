package com.quku.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

import com.quku.Utils.Utils;

public class DrawableAnimation extends View {
	private static final int MESSAE_START_SCAN_ANIMATION = 1;// 扫描动画
	private static final int MESSAGE_START_CUT_ANIMATION = 2;// 图片切边动画
	private static final int MESSAGE_PROCESS_FINISH = 3;// 处理完毕通知
	private String TAG = "DrawableAnimation";
	private Context mContext;
	private Drawable animaDrawable;
	private Animation mAiAnimation;
	private Transformation mTransformation = new Transformation();
	Drawable backgroundDrawable;
	private Bitmap mBitmap;
	public Handler mHandler;// handler对象，用于启动动画

	/**
	 * 动画初始化方法
	 */
	private void init() {
		setFocusable(true);
		setFocusableInTouchMode(true);
		// onstartAiniation2();
	}

	public Drawable getAnimaDrawable() {
		return animaDrawable;
	}

	public void setAnimaDrawable(Drawable animaDrawable) {
		this.animaDrawable = animaDrawable;
	}

	public void myHandler() {
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MESSAE_START_SCAN_ANIMATION:
						setAnimaDrawable(mContext.getResources().getDrawable(com.quku.R.drawable.like_line8));
						onstartAiniation();
						break;
					case MESSAGE_START_CUT_ANIMATION:
						Utils.getToast(mContext, "正在切边...");
						break;
					case MESSAGE_PROCESS_FINISH:
						Utils.getToast(mContext, "处理完毕");
						break;
				}

				super.handleMessage(msg);
			}

		};
	}

	/**
	 * 开始动画
	 */
	private void onstartAiniation() {
		mAiAnimation = new TranslateAnimation(0, 0, 0, 1240);
		mAiAnimation.initialize(10, 10, 10, 10);
		mAiAnimation.setDuration(3000);
		// mAiAnimation.setRepeatCount(-1);
		animaDrawable.setBounds(0, 0, animaDrawable.getIntrinsicWidth(), animaDrawable.getIntrinsicHeight());
		mAiAnimation.startNow();
		// mHandler.sendEmptyMessage(MESSAGE_PROCESS_FINISH);
	}

	/**
	 * 开始动画
	 */
	private void onstartAiniation2() {
		mAiAnimation = new TranslateAnimation(10, 0, 0, 0);
		mAiAnimation.initialize(10, 10, 10, 10);
		mAiAnimation.setDuration(3000);
		mAiAnimation.setRepeatCount(2);
		// getResources().getDrawable()
		Drawable d = getResources().getDrawable(com.quku.R.drawable.border_line3);
		animaDrawable = d;
		animaDrawable.setBounds(0, 0, animaDrawable.getIntrinsicWidth(), animaDrawable.getIntrinsicHeight());
		mAiAnimation.startNow();
		// mHandler.sendEmptyMessage(MESSAGE_PROCESS_FINISH);
	}

	public DrawableAnimation(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	public DrawableAnimation(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	/**
	 * 构造方法
	 * 
	 * @param context
	 *            上下文
	 * @param backgroundDrawable
	 *            背景drawable对象
	 * @param animaDrawable
	 *            动画图标的drawable对象
	 */
	public DrawableAnimation(Context context, Drawable backgroundDrawable, Drawable animaDrawable, Handler mHandler) {
		super(context);
		mContext = context;
		this.animaDrawable = animaDrawable;
		BitmapDrawable bm = (BitmapDrawable) backgroundDrawable;
		mBitmap = bm.getBitmap();
		init();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// canvas.drawColor(Color.WHITE);
		// 给画板绘制传送过来的mbitmap对象当作底图
		canvas.drawBitmap(mBitmap, 0, 0, null);
		if (animaDrawable != null) {
			int sc = canvas.save();
			if (mAiAnimation != null) {
				mAiAnimation.getTransformation(AnimationUtils.currentAnimationTimeMillis(), mTransformation);
				canvas.concat(mTransformation.getMatrix());
			}
			animaDrawable.draw(canvas);
			canvas.restoreToCount(sc);
		}
		invalidate();
	}

}

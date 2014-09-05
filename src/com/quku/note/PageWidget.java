package com.quku.note;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PageWidget extends View {

	private int mWidth = 640;
	private int mHeight = 910;
	private int mCornerX = 640; // 拖拽点对应的页脚
	private int mCornerY = 910;
	private Path mPath0;
	private Path mPath1;
	Bitmap mCurPageBitmap = null; // 当前页
	Bitmap mCurPageBackBitmap = null;
	Bitmap mNextPageBitmap = null;

	PointF mTouch = new PointF(); // 拖拽点
	PointF mBezierStart1 = new PointF(); // 贝塞尔曲线起始点
	PointF mBezierControl1 = new PointF(); // 贝塞尔曲线控制点
	PointF mBeziervertex1 = new PointF(); // 贝塞尔曲线顶点
	PointF mBezierEnd1 = new PointF(); // 贝塞尔曲线结束点

	PointF mBezierStart2 = new PointF(); // 另一条贝塞尔曲线
	PointF mBezierControl2 = new PointF();
	PointF mBeziervertex2 = new PointF();
	PointF mBezierEnd2 = new PointF();

	float mMiddleX;
	float mMiddleY;
	float mDegrees;
	float mTouchToCornerDis;

	boolean mIsRTandLB; // 是否属于右上左下
	// for test
	float mMaxLength = (float) Math.hypot(mWidth, mHeight);
	int[] mBackShadowColors;
	int[] mFrontShadowColors;
	GradientDrawable mBackShadowDrawableLR;
	GradientDrawable mBackShadowDrawableRL;
	GradientDrawable mFolderShadowDrawableLR;
	GradientDrawable mFolderShadowDrawableRL;

	GradientDrawable mFrontShadowDrawableHBT;
	GradientDrawable mFrontShadowDrawableHTB;
	GradientDrawable mFrontShadowDrawableVLR;
	GradientDrawable mFrontShadowDrawableVRL;

	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Paint mBitmapPaint;
	Paint paint;
	ColorMatrixColorFilter mColorMatrixFilter;
	Matrix mMatrix;
	float[] mMatrixArray = { 0, 0, 0, 0, 0, 0, 0, 0, 1.0f };

	private OnPageScrollFinishedListener mOnPageScrollFinishedListener;

	public interface OnPageScrollFinishedListener {
		void onPageScrollFinished(View v);
	}

	public void setOnPageScrollFinishedListener(OnPageScrollFinishedListener l) {
		mOnPageScrollFinishedListener = l;
	}

	public PageWidget(Context context, Bitmap bitmap) {
		super(context);
		mPath0 = new Path();
		mPath1 = new Path();

		createDrawable();

		mBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
		// mBitmap = Bitmap.createBitmap(640, 960, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);

		// mCurPageBitmap = Bitmap.createBitmap(640,960,
		// Bitmap.Config.ARGB_8888);
		mCurPageBitmap = Bitmap.createBitmap(mBitmap);
		Canvas canvas = new Canvas(mCurPageBitmap);
		paint = new Paint();
		// canvas.drawColor(Color.YELLOW);
		canvas.drawBitmap(mCurPageBitmap, 0, 0, paint);

		// mNextPageBitmap = Bitmap
		// .createBitmap(640, 960, Bitmap.Config.ARGB_8888);
		mNextPageBitmap = Bitmap.createBitmap(mBitmap);
		;
		canvas = new Canvas(mNextPageBitmap);
		// canvas.drawColor(Color.GREEN);
		canvas.drawBitmap(mNextPageBitmap, 0, 0, paint);
		ColorMatrix cm = new ColorMatrix();
		float array[] = { 0.55f, 0, 0, 0, 80.0f, 0, 0.55f, 0, 0, 80.0f, 0, 0, 0.55f, 0, 80.0f, 0, 0, 0, 0.2f, 0 };
		cm.set(array);
		mColorMatrixFilter = new ColorMatrixColorFilter(cm);
		mMatrix = new Matrix();
	}

	/**
	 * Author : hmg25 Version: 1.0 Description : 计算拖拽点对应的拖拽脚
	 */
	private void calcCornerXY(float x, float y) {
		// if (x <= mWidth / 2)
		// mCornerX = 0;
		// else
		// mCornerX = mWidth;
		// if (y <= mHeight / 2)
		// mCornerY = 0;
		// else
		// mCornerY = mHeight;
		// if ((mCornerX == 0 && mCornerY == mHeight)
		// || (mCornerX == mWidth && mCornerY == 0))
		// mIsRTandLB = true;
		// else
		mIsRTandLB = false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// System.out.println("onTouchEvent----------------------->");
		// // TODO Auto-generated method stub
		// if (event.getAction() == MotionEvent.ACTION_MOVE) {
		// mCanvas.drawColor(0xFFAAAAAA);
		// // mTouch.x = event.getX();
		// // mTouch.y = event.getY();
		// mTouch.x = 300.0f;
		// mTouch.y = 400.0f;
		// this.postInvalidate();
		// }
		// if (event.getAction() == MotionEvent.ACTION_DOWN) {
		// mCanvas.drawColor(0xFFAAAAAA);
		// // mTouch.x = event.getX();
		// // mTouch.y = event.getY();
		// mTouch.x = 600.0f;
		// mTouch.y = 800.0f;
		// calcCornerXY(mTouch.x, mTouch.y);
		// this.postInvalidate();
		// }
		if (event.getAction() == MotionEvent.ACTION_UP) {
			mCanvas.drawColor(0xAAAAAAAA);
			// autoPaly();
			// mTouch.x = mCornerX;
			// mTouch.y = mCornerY;
			if (event.getX() < 100 && event.getY() > 800) {
				System.out.println("autoDisPaly=====================");
				autoDisPaly();
			} else if (event.getX() > 500 && event.getY() > 800) {
				System.out.println("autoPaly========================");
				autoPaly();
			}
			this.postInvalidate();
		}
		// return super.onTouchEvent(event);
		return true;
	}

	private static final int MSG_PAGE_SCROLL_FINISHED = 1;
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_PAGE_SCROLL_FINISHED:
					if (mOnPageScrollFinishedListener != null) {
						mOnPageScrollFinishedListener.onPageScrollFinished(PageWidget.this);
					}
					handler.removeMessages(MSG_PAGE_SCROLL_FINISHED);
					break;

				default:
					break;
			}
		};
	};

	public void autoPaly() {
		mTouch.x = 640.0f;
		mTouch.y = 910.0f;
		handler.post(escr);
	}

	public void autoDisPaly() {
		mTouch.x = 640.0f;
		mTouch.y = -910.0f;
		handler.post(descr);
		// handler.sendEmptyMessage(1);
	}

	Runnable escr = new Runnable() {
		public void run() {
			mCanvas.drawColor(0xAAAAAAAA);
			handler.removeCallbacks(descr);
			if (mTouch.y >= 0) {
				mTouch.x -= 10;
				mTouch.y -= 40;
				PageWidget.this.postInvalidate();
				handler.postDelayed(escr, 25);
			} else if (mTouch.y > -910) {
				mTouch.x += 25;
				mTouch.y -= 160;
				PageWidget.this.postInvalidate();
				handler.postDelayed(escr, 25);
			} else {
				handler.sendEmptyMessage(MSG_PAGE_SCROLL_FINISHED);
				handler.removeCallbacks(escr);
			}
		}

	};

	Runnable descr = new Runnable() {
		public void run() {
			handler.removeCallbacks(descr);
			mCanvas.drawColor(0xAAAAAAAA);
			if (mTouch.y >= 910) {
				handler.sendEmptyMessage(MSG_PAGE_SCROLL_FINISHED);
				handler.removeCallbacks(descr);
			} else if (mTouch.y > 0) {
				mTouch.x += 10;
				mTouch.y += 40;
				postInvalidate();
				handler.postDelayed(descr, 25);
			} else {
				mTouch.x -= 10;
				mTouch.y += 40;
				postInvalidate();
				handler.postDelayed(descr, 25);
			}
		}

	};

	/**
	 * Author : hmg25 Version: 1.0 Description : 求解直线P1P2和直线P3P4的交点坐标
	 */
	public PointF getCross(PointF P1, PointF P2, PointF P3, PointF P4) {
		PointF CrossP = new PointF();
		// 二元函数通式： y=ax+b
		float a1 = (P2.y - P1.y) / (P2.x - P1.x);
		float b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x);

		float a2 = (P4.y - P3.y) / (P4.x - P3.x);
		float b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x);
		CrossP.x = (b2 - b1) / (a1 - a2);
		CrossP.y = a1 * CrossP.x + b1;
		return CrossP;
	}

	private void calcPoints() {
		// System.out.println("calcPoints---------------->");
		mMiddleX = (mTouch.x + mCornerX) / 2;
		mMiddleY = (mTouch.y + mCornerY) / 2;

		mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
		mBezierControl1.y = mCornerY;
		mBezierControl2.x = mCornerX;
		mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);

		// Log.i("hmg", "mTouchX  " + mTouch.x + "  mTouchY  " + mTouch.y);
		// Log.i("hmg", "mBezierControl1.x  " + mBezierControl1.x
		// + "  mBezierControl1.y  " + mBezierControl1.y);
		// Log.i("hmg", "mBezierControl2.x  " + mBezierControl2.x
		// + "  mBezierControl2.y  " + mBezierControl2.y);

		mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2;
		mBezierStart1.y = mCornerY;

		mBezierStart2.x = mCornerX;
		mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y) / 2;

		// Log.i("hmg", "mBezierStart1.x  " + mBezierStart1.x
		// + "  mBezierStart1.y  " + mBezierStart1.y);
		// Log.i("hmg", "mBezierStart2.x  " + mBezierStart2.x
		// + "  mBezierStart2.y  " + mBezierStart2.y);

		mTouchToCornerDis = (float) Math.hypot((mTouch.x - mCornerX), (mTouch.y - mCornerY));

		mBezierEnd1 = getCross(mTouch, mBezierControl1, mBezierStart1, mBezierStart2);
		mBezierEnd2 = getCross(mTouch, mBezierControl2, mBezierStart1, mBezierStart2);

		Log.i("hmg", "mBezierEnd1.x  " + mBezierEnd1.x + "  mBezierEnd1.y  " + mBezierEnd1.y);
		Log.i("hmg", "mBezierEnd2.x  " + mBezierEnd2.x + "  mBezierEnd2.y  " + mBezierEnd2.y);

		/*
		 * mBeziervertex1.x 推导
		 * ((mBezierStart1.x+mBezierEnd1.x)/2+mBezierControl1.x)/2 化简等价于
		 * (mBezierStart1.x+ 2*mBezierControl1.x+mBezierEnd1.x) / 4
		 */
		mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4;
		mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4;
		mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4;
		mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4;

		// Log.i("hmg", "mBeziervertex1.x  " + mBeziervertex1.x
		// + "  mBeziervertex1.y  " + mBeziervertex1.y);
		// Log.i("hmg", "mBeziervertex2.x  " + mBeziervertex2.x
		// + "  mBeziervertex2.y  " + mBeziervertex2.y);

	}

	private void drawCurrentPageArea(Canvas canvas, Bitmap bitmap, Path path) {
		// System.out.println("drawCurrentPageArea------------------------>");
		mPath0.reset();
		mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x, mBezierEnd1.y);
		mPath0.lineTo(mTouch.x, mTouch.y);
		mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x, mBezierStart2.y);
		mPath0.lineTo(mCornerX, mCornerY);
		mPath0.close();

		canvas.save();
		canvas.clipPath(path, Region.Op.XOR);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.restore();
	}

	private void drawNextPageAreaAndShadow(Canvas canvas, Bitmap bitmap) {
		// System.out.println("drawNextPageAreaAndShadow------------------------------>");
		mPath1.reset();
		mPath1.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
		mPath1.lineTo(mBeziervertex2.x, mBeziervertex2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.lineTo(mCornerX, mCornerY);
		mPath1.close();

		mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x - mCornerX, mBezierControl2.y - mCornerY));
		float f5 = mTouchToCornerDis / 4;
		int leftx;
		int rightx;
		GradientDrawable mBackShadowDrawable;
		if (mIsRTandLB) {
			leftx = (int) (mBezierStart1.x);
			rightx = (int) (mBezierStart1.x + mTouchToCornerDis / 4);
			mBackShadowDrawable = mBackShadowDrawableLR;
		} else {
			leftx = (int) (mBezierStart1.x - mTouchToCornerDis / 4);
			rightx = (int) mBezierStart1.x;
			mBackShadowDrawable = mBackShadowDrawableRL;
		}

		// Log.i("hmg", "leftx  " + leftx + "   rightx  " + rightx);
		canvas.save();
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
		mBackShadowDrawable.setBounds(leftx, (int) mBezierStart1.y, rightx, (int) (mMaxLength + mBezierStart1.y));
		mBackShadowDrawable.draw(canvas);
		canvas.restore();
	}

	public void setBitmaps(Bitmap bm1, Bitmap bm3) {
		mCurPageBitmap = Bitmap.createBitmap(bm1, 0, 0, bm1.getWidth(), bm1.getHeight());
		mNextPageBitmap = Bitmap.createBitmap(bm3, 0, 0, bm3.getWidth(), bm3.getHeight());
		// mCurPageBackBitmap = bm2;
		// mNextPageBitmap = bm3;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// System.out.println("onDraw------------------------------------>");
		mCanvas.drawColor(0xAAAAAAAA);
		calcPoints();
		drawCurrentPageArea(mCanvas, mCurPageBitmap, mPath0);
		drawNextPageAreaAndShadow(mCanvas, mNextPageBitmap);
		drawCurrentPageShadow(mCanvas);
		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
		drawCurrentBackArea(canvas, mCurPageBitmap);
	}

	/**
	 * Author : hmg25 Version: 1.0 Description : 创建阴影的GradientDrawable
	 */
	private void createDrawable() {
		// System.out.println("createDrawable-------------------------->");
		int[] color = { 0x333333, 0xB0333333 };
		mFolderShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, color);
		mFolderShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFolderShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, color);
		mFolderShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mBackShadowColors = new int[] { 0x80111111, 0x111111 };
		mBackShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
		mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mBackShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
		mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowColors = new int[] { 0x80888888, 0x888888 };
		mFrontShadowDrawableVLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);
		mFrontShadowDrawableVLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mFrontShadowDrawableVRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);
		mFrontShadowDrawableVRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableHTB = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);
		mFrontShadowDrawableHTB.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableHBT = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);
		mFrontShadowDrawableHBT.setGradientType(GradientDrawable.LINEAR_GRADIENT);
	}

	/**
	 * Author : hmg25 Version: 1.0 Description : 绘制翻起页背面
	 */
	private void drawCurrentBackArea(Canvas canvas, Bitmap bitmap) {
		int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
		float f1 = Math.abs(i - mBezierControl1.x);
		int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
		float f2 = Math.abs(i1 - mBezierControl2.y);
		float f3 = Math.min(f1, f2);
		mPath1.reset();
		mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
		mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
		mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath1.close();
		GradientDrawable mFolderShadowDrawable;
		int left;
		int right;
		if (mIsRTandLB) {
			left = (int) (mBezierStart1.x - 1);
			right = (int) (mBezierStart1.x + f3 + 1);
			mFolderShadowDrawable = mFolderShadowDrawableLR;
		} else {
			left = (int) (mBezierStart1.x - f3 - 1);
			right = (int) (mBezierStart1.x + 1);
			mFolderShadowDrawable = mFolderShadowDrawableRL;
		}
		canvas.save();
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);

		paint.setColorFilter(mColorMatrixFilter);

		float dis = (float) Math.hypot(mCornerX - mBezierControl1.x, mBezierControl2.y - mCornerY);
		float f8 = (mCornerX - mBezierControl1.x) / dis;
		float f9 = (mBezierControl2.y - mCornerY) / dis;
		mMatrixArray[0] = 1 - 2 * f9 * f9;
		mMatrixArray[1] = 2 * f8 * f9;
		mMatrixArray[3] = mMatrixArray[1];
		mMatrixArray[4] = 1 - 2 * f8 * f8;
		mMatrix.reset();
		mMatrix.setValues(mMatrixArray);
		mMatrix.preTranslate(-mBezierControl1.x, -mBezierControl1.y);
		mMatrix.postTranslate(mBezierControl1.x, mBezierControl1.y);
		canvas.drawBitmap(bitmap, mMatrix, paint);
		// canvas.drawBitmap(bitmap, mMatrix, null);
		paint.setColorFilter(null);
		canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
		mFolderShadowDrawable.setBounds(left, (int) mBezierStart1.y, right, (int) (mBezierStart1.y + mMaxLength));
		mFolderShadowDrawable.draw(canvas);
		canvas.restore();
	}

	public void drawCurrentPageShadow(Canvas canvas) {
		double degree;
		if (mIsRTandLB) {
			degree = Math.PI / 4 - Math.atan2(mBezierControl1.y - mTouch.y, mTouch.x - mBezierControl1.x);
		} else {
			degree = Math.PI / 4 - Math.atan2(mTouch.y - mBezierControl1.y, mTouch.x - mBezierControl1.x);
		}
		// 翻起页阴影顶点与touch点的距离
		double d1 = (float) 20 * 1.414 * Math.cos(degree);
		double d2 = (float) 20 * 1.414 * Math.sin(degree);
		float x = (float) (mTouch.x + d1);
		float y;
		if (mIsRTandLB) {
			y = (float) (mTouch.y + d2);
		} else {
			y = (float) (mTouch.y - d2);
		}

		mPath1.reset();
		mPath1.moveTo(x, y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
		mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.close();
		float rotateDegrees;
		canvas.save();

		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		int leftx;
		int rightx;
		GradientDrawable mCurrentPageShadow;
		if (mIsRTandLB) {
			leftx = (int) (mBezierControl1.x);
			rightx = (int) mBezierControl1.x + 20;
			mCurrentPageShadow = mFrontShadowDrawableVLR;
		} else {
			leftx = (int) (mBezierControl1.x - 20);
			rightx = (int) mBezierControl1.x;
			mCurrentPageShadow = mFrontShadowDrawableVRL;
		}
		rotateDegrees = (float) Math.toDegrees(Math.atan2(mTouch.x - mBezierControl1.x, mBezierControl1.y - mTouch.y));
		canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y);

		mCurrentPageShadow.setBounds(leftx, (int) (mBezierControl1.y - mMaxLength), rightx, (int) (mBezierControl1.y));
		mCurrentPageShadow.draw(canvas);
		canvas.restore();

		mPath1.reset();
		mPath1.moveTo(x, y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl2.x, mBezierControl2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.close();
		canvas.save();
		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);

		if (mIsRTandLB) {
			leftx = (int) (mBezierControl2.y);
			rightx = (int) (mBezierControl2.y + 20);
			mCurrentPageShadow = mFrontShadowDrawableHTB;
		} else {
			leftx = (int) (mBezierControl2.y - 20);
			rightx = (int) (mBezierControl2.y);
			mCurrentPageShadow = mFrontShadowDrawableHBT;
		}
		rotateDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl2.y - mTouch.y, mBezierControl2.x - mTouch.x));
		canvas.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y);
		// mCurrentPageShadow.setBounds((int) (mBezierControl2.x - 500), leftx,
		// (int) (mBezierControl2.x), rightx);

		float temp;
		if (mBezierControl2.y < 0)
			temp = mBezierControl2.y - mHeight;
		else
			temp = mBezierControl2.y;

		int hmg = (int) Math.hypot(mBezierControl2.x, temp);
		if (hmg > mMaxLength)
			mCurrentPageShadow.setBounds((int) (mBezierControl2.x - 20) - hmg, leftx,
					(int) (mBezierControl2.x + mMaxLength) - hmg, rightx);
		else
			mCurrentPageShadow.setBounds((int) (mBezierControl2.x - mMaxLength), leftx, (int) (mBezierControl2.x),
					rightx);

		mCurrentPageShadow.draw(canvas);
		canvas.restore();
	}

}
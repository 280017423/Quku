package com.quku.note;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.FloatMath;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;

public class LijieSurfaceView extends SurfaceView implements Callback {

	private static final int WIDTH = 8;
	private static final int HEIGHT = 20;
	private static final int COUNT = (WIDTH + 1) * (HEIGHT + 1);
	private float mfDurationTime = -1; // Unit is ms
	private float mfCurrentTime = 0;
	private float mfTimeStep = 25;
	private float mfMoveSpeed = 500; // Unit is pixel/s
	private long mlLastFrameTimeMs = 0;
	private boolean mbRepeatForever = false;
	private boolean mbTryBestToDraw = false;
	private int miPreDrawFrameAmount = 12;
	private Bitmap mPreDrawFrame[] = new Bitmap[miPreDrawFrameAmount];
	private int miNextPreDrawFrame = 0;
	private boolean mbPreDrawComplete = false;

	private float mfInitialMoveSpeedFactor = 0.5f;
	private float mfFinalMoveSpeedFactor = 2 - mfInitialMoveSpeedFactor;

	private long mlFrameInterval[] = new long[512];
	private int miFrameIntervalAmount = 0;

	private int iApX = 0;
	private int iApY = 0;

	int iFarestCornerX = 0;
	int iFarestCornerY = 0;

	private final Bitmap mBitmap;
	private final Bitmap mBitmapBg;
	private final float[] mVerts = new float[COUNT * 2];
	private final float[] mOrig = new float[COUNT * 2];
	private final int[] mLock = new int[COUNT];
	private final int[] mXPhase = new int[COUNT];
	private final int[] mYPhase = new int[COUNT];

	private static final int MSG_MESH = 1;

	private WindowManager mWindowManager;

	private MeshFinishListener mListener;
	LoopThread thread;
	PreDrawThread thread2;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message message) {
			switch (message.what) {
				case MSG_MESH:
					// long befoer = System.currentTimeMillis();
					// System.out.println("handleMessage befor time=============================="
					// + befoer);
					// System.out.println("MSG_MESH....mfDurationTime=" +
					// mfDurationTime);
					// System.out.println("MSG_MESH....mfCurrentTime=" +
					// mfCurrentTime);
					// System.out.println("MSG_MESH....mfTimeStep=" +
					// mfTimeStep);
					if (mfCurrentTime >= mfDurationTime) {
						mHandler.removeMessages(MSG_MESH);
						if (mListener != null) {
							mListener.onFinished();
						}
						// reset();
						break;
					}
					mHandler.removeMessages(MSG_MESH);
					// mHandler.sendEmptyMessageDelayed(MSG_MESH, 1);
					mHandler.sendEmptyMessageDelayed(MSG_MESH, (int) mfTimeStep);

					warp_lijie();
					invalidate();

					mfCurrentTime += mfTimeStep;
					// long after = System.currentTimeMillis();
					// System.out.println("handleMessage after time========================="
					// + after);
					// System.out.println("handleMessage dele time=========================="
					// + (after - befoer));
					break;

				default:
					break;
			}
		};
	};

	public interface MeshFinishListener {
		void onFinished();
	}

	public void setOnMeshFinishListener(MeshFinishListener l) {
		mListener = l;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	public LijieSurfaceView(Context context, Bitmap bitmap, Bitmap bitmapBg) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
		// mBitmap = Bitmap.createBitmap(bitmap);
		mBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
		// mBitmapBg = Bitmap.createBitmap(bitmapBg);
		mBitmapBg = bitmapBg.copy(Bitmap.Config.ARGB_8888, false);
		// mWindowManager = WindowManagerImpl.getDefault();

		float w = mBitmap.getWidth();
		float h = mBitmap.getHeight();

		int index = 0;
		for (int y = 0; y <= HEIGHT; y++) {
			float fy = h * y / HEIGHT;
			for (int x = 0; x <= WIDTH; x++) {
				float fx = w * x / WIDTH;
				setXY(mVerts, index, fx, fy);
				setXY(mOrig, index, fx, fy);
				index += 1;
			}
		}

	}

	public void init() {
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		thread = new LoopThread(holder, getContext());
	}

	public void doPreDraw() {

		float CurrentTimeTemp = mfCurrentTime;
		for (int i = 0; i < miPreDrawFrameAmount; i++) {
			// mPreDrawFrame[i] = Bitmap.createBitmap(540, 960,
			// Bitmap.Config.ARGB_8888);
			mPreDrawFrame[i] = Bitmap.createBitmap(640, 960, Bitmap.Config.RGB_565);
			Canvas cvFrame = new Canvas(mPreDrawFrame[i]);
			// cvFrame.drawColor(0x00FFFFFF);
			cvFrame.drawBitmap(mBitmapBg, 0, 0, null);
			warp_lijie();
			cvFrame.drawBitmapMesh(mBitmap, WIDTH, HEIGHT, mVerts, 0, null, 0, null);
			mfCurrentTime += mfTimeStep;
		}
		mfCurrentTime = CurrentTimeTemp;
		mbPreDrawComplete = true;
	}

	// @Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	// @Override
	public void surfaceCreated(SurfaceHolder holder) {
		// if ( miPreDrawFrameAmount > 0 ) {
		// doPreDraw();
		// }

		thread.isRunning = true;
		thread.start();
	}

	// @Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		thread.isRunning = false;
		// for ( int i=0; i < miFrameIntervalAmount; i++)
		// {
		// // System.out.println("LIJIE ========  ========  frame interval " +
		// mlFrameInterval [i] + "ms");
		// }
		// System.out.println("LIJIE ========  ========  totally " +
		// miFrameIntervalAmount + " frames");
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	class PreDrawThread extends Thread {
		Context context;
		boolean isRunning;
		Paint paint;

		public PreDrawThread(Context context) {

			this.context = context;
			isRunning = false;

			paint = new Paint();

		}

		@Override
		public void run() {
			while (isRunning) {
				while (!mbPreDrawComplete) {
					doPreDraw();
				}
			}
		}
	}

	/**
	 * ִ�л��ƵĻ����߳�
	 * 
	 * @author Administrator
	 * 
	 */
	class LoopThread extends Thread {

		SurfaceHolder surfaceHolder;
		Context context;
		boolean isRunning;
		Paint paint;

		public LoopThread(SurfaceHolder surfaceHolder, Context context) {

			this.surfaceHolder = surfaceHolder;
			this.context = context;
			isRunning = false;

			paint = new Paint();
			paint.setColor(Color.YELLOW);
			paint.setStyle(Paint.Style.STROKE);
		}

		@Override
		public void run() {

			Canvas c = null;

			while (isRunning) {
				if (mlLastFrameTimeMs == 0) {
					mlLastFrameTimeMs = System.currentTimeMillis();

					synchronized (surfaceHolder) {
						c = surfaceHolder.lockCanvas(new Rect(0, 0, 640, 910));
						// c = surfaceHolder.lockCanvas(null);
						c.drawBitmap(mBitmap, 0, 0, null);
					}
					if (c != null) {
						surfaceHolder.unlockCanvasAndPost(c);
					}

					synchronized (surfaceHolder) {
						c = surfaceHolder.lockCanvas(new Rect(0, 0, 640, 910));
						c.drawBitmap(mBitmap, 0, 0, null);
					}
					// c = surfaceHolder.lockCanvas(new Rect(0,0,540,960));
					if (c != null) {
						surfaceHolder.unlockCanvasAndPost(c);
					}

					continue;
				}

				try {
					synchronized (surfaceHolder) {
						// c = surfaceHolder.lockCanvas(null);
						c = surfaceHolder.lockCanvas(new Rect(0, 0, 640, 910));
						long mlCurrentFrameTimeMs = System.currentTimeMillis();
						long mlFrameDelay = mlCurrentFrameTimeMs - mlLastFrameTimeMs;
						mlFrameInterval[miFrameIntervalAmount] = mlFrameDelay;
						miFrameIntervalAmount++;
						if (mbTryBestToDraw) { // ��f������֡������
							mfCurrentTime += (float) mlFrameDelay;
							doDraw(c);
						} else { // ׷��̶�֡������
							mlFrameDelay = (long) mfTimeStep - mlFrameDelay;
							if (mlFrameDelay > 0) {
								Thread.sleep(mlFrameDelay); // ͨ����4����֡��ִ��һ�λ��ƺ���Ϣ??ms
							}
							if (miNextPreDrawFrame < miPreDrawFrameAmount) // ���������δDraw��Ԥ����֡�����Ȼ�Ԥ����֡
							{
								// c.drawColor(Color.BLACK);
								if (mPreDrawFrame[miNextPreDrawFrame] != null) {
									c.drawBitmap(mPreDrawFrame[miNextPreDrawFrame], 0, 0, null);
									mPreDrawFrame[miNextPreDrawFrame].recycle();
								}
								mPreDrawFrame[miNextPreDrawFrame] = null;
								miNextPreDrawFrame++;
							} else {
								doDraw(c);
							}
							mfCurrentTime += mfTimeStep;
						}
						mlLastFrameTimeMs = mlCurrentFrameTimeMs;

					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					surfaceHolder.unlockCanvasAndPost(c);
				}

			}

		}

		public void doDraw(Canvas c) {

			// ������Ҫ�����r���������ϴλ��ƵĲ���ͼ��
			// c.drawColor(Color.BLACK);

			// ���������ԭ���ķ���һ��Բ��
			/*
			 * c.translate(200, 200); c.drawCircle(0,0, radius++, paint);
			 * 
			 * if(radius > 100){ radius = 10f; }
			 */

			// c.drawColor(0x00000000);
			if (mfCurrentTime < mfDurationTime) {
				warp_lijie();
				c.drawBitmap(mBitmapBg, 0, 0, null);
				c.drawBitmapMesh(mBitmap, WIDTH, HEIGHT, mVerts, 0, null, 0, null);
			} else {
				mfCurrentTime = 0;
				if (mbRepeatForever) {
					c.drawBitmap(mBitmapBg, 0, 0, null);
					warp_lijie();
					c.drawBitmapMesh(mBitmap, WIDTH, HEIGHT, mVerts, 0, null, 0, null);
				} else {
					if (mListener != null) {
						mListener.onFinished();
					}
				}
			}
		}

	}

	private static void setXY(float[] array, int index, float x, float y) {
		array[index * 2 + 0] = x;
		array[index * 2 + 1] = y;
	}

	public void setApXYDuration(int iApX, int iApY, int iDurationMs, int iTimeStepMs) {
		// ����Լ��
		if (iApX < 0) {
			iApX = 0;
		}
		if (iApX > WIDTH) {
			iApX = WIDTH;
		}
		if (iApY < 0) {
			iApY = 0;
		}
		if (iApY > HEIGHT) {
			iApY = HEIGHT;
		}

		if (iDurationMs < 0) {
			iDurationMs = 0;
		}

		// �����MoveSpeed
		iFarestCornerX = (iApX >= (WIDTH - iApX)) ? 0 : WIDTH;
		iFarestCornerY = (iApY >= (HEIGHT - iApY)) ? 0 : HEIGHT;
		float fDX = mOrig[(iFarestCornerY * (WIDTH + 1) + iFarestCornerX) * 2] - mOrig[(iApY * (WIDTH + 1) + iApX) * 2];
		float fDY = mOrig[(iFarestCornerY * (WIDTH + 1) + iFarestCornerX) * 2 + 1]
				- mOrig[(iApY * (WIDTH + 1) + iApX) * 2 + 1];
		float fDistanceFarestCornerToAp = (float) Math.sqrt(((fDX * fDX) + (fDY * fDY)));
		mfMoveSpeed = (fDistanceFarestCornerToAp + 100) * 1000 / iDurationMs;
		mfTimeStep = iTimeStepMs;
		mfDurationTime = iDurationMs;
		this.iApX = iApX;
		this.iApY = iApY;
		// System.out.println("fDistanceFarestCornerToAp=" +
		// fDistanceFarestCornerToAp);
		// System.out.println("mfMoveSpeed=" + mfMoveSpeed);
		// System.out.println("mfTimeStep=" + mfTimeStep);
		// System.out.println("iApX=" + iApX);
		// System.out.println("iApY=" + iApY);
		// System.out.println("iDurationMs=" + iDurationMs);

		// ���ApXY������ÿ���������������
		for (int y = 0; y < HEIGHT + 1; y++) {
			for (int x = 0; x < WIDTH + 1; x++) {
				mXPhase[y * (WIDTH + 1) + x] = (x < iApX) ? (-1) : 1;
				mYPhase[y * (WIDTH + 1) + x] = (y < iApY) ? (-1) : 1;
				mLock[y * (WIDTH + 1) + x] = 0;
			}
		}
		mLock[iApY * (WIDTH + 1) + iApX] = 1;
	}

	public void startMesh(IBinder windowToken) {
		WindowManager.LayoutParams lp;
		// int pixelFormat;
		// pixelFormat = PixelFormat.OPAQUE;

		// show
		/*
		 * mHandler.removeMessages(MSG_MESH);
		 * mHandler.sendEmptyMessage(MSG_MESH);
		 */

	}

	private void warp_lijie() {
		float[] src = mOrig;
		float[] dst = mVerts;

		for (int y = 0; y < HEIGHT + 1; y++) {
			for (int x = 0; x < WIDTH + 1; x++) {
				if (mLock[y * (WIDTH + 1) + x] != 1) {
					float fOrigDeltaX = src[(y * (WIDTH + 1) + x) * 2] - src[(iApY * (WIDTH + 1) + iApX) * 2];
					float fOrigDeltaY = src[(y * (WIDTH + 1) + x) * 2 + 1] - src[(iApY * (WIDTH + 1) + iApX) * 2 + 1];
					float fCurrentDeltaX = dst[(y * (WIDTH + 1) + x) * 2] - src[(iApY * (WIDTH + 1) + iApX) * 2];
					float fCurrentDeltaY = dst[(y * (WIDTH + 1) + x) * 2 + 1]
							- src[(iApY * (WIDTH + 1) + iApX) * 2 + 1];

					float fOrigDistance = (float) Math.sqrt(fOrigDeltaX * fOrigDeltaX + fOrigDeltaY * fOrigDeltaY);
					// float fCurrentDistance =
					// FloatMath.sqrt(fCurrentDeltaX*fCurrentDeltaX +
					// fCurrentDeltaY*fCurrentDeltaY);

					/*
					 * if ( fCurrentDistance <= fEpsilon ) {
					 * dst[(y*(WIDTH+1)+x)*2] = src[(iApY*(WIDTH+1)+iApX)*2];
					 * dst[(y*(WIDTH+1)+x)*2+1] =
					 * src[(iApY*(WIDTH+1)+iApX)*2+1]; } else
					 */
					// {
					float fSin = -fOrigDeltaY / fOrigDistance;
					float fCos = -fOrigDeltaX / fOrigDistance;
					float F = mfFinalMoveSpeedFactor;
					float I = mfInitialMoveSpeedFactor;
					float fCurrentMoveSpeedFactor = I + (2 - 2 * I) * mfCurrentTime / mfDurationTime;
					dst[(y * (WIDTH + 1) + x) * 2] = fCos * mfCurrentTime * mfMoveSpeed * fCurrentMoveSpeedFactor
							/ 1000 + src[(y * (WIDTH + 1) + x) * 2];
					dst[(y * (WIDTH + 1) + x) * 2 + 1] = fSin * mfCurrentTime * mfMoveSpeed * fCurrentMoveSpeedFactor
							/ 1000 + src[(y * (WIDTH + 1) + x) * 2 + 1];
					// dst[(y*(WIDTH+1)+x)*2] =
					// fCos*mfCurrentTime*mfMoveSpeed/1000 +
					// src[(y*(WIDTH+1)+x)*2];
					// dst[(y*(WIDTH+1)+x)*2+1] =
					// fSin*mfCurrentTime*mfMoveSpeed/1000 +
					// src[(y*(WIDTH+1)+x)*2+1];
					// }

					fCurrentDeltaX = dst[(y * (WIDTH + 1) + x) * 2] - src[(iApY * (WIDTH + 1) + iApX) * 2];
					fCurrentDeltaY = dst[(y * (WIDTH + 1) + x) * 2 + 1] - src[(iApY * (WIDTH + 1) + iApX) * 2 + 1];
					if (((fCurrentDeltaX * mXPhase[y * (WIDTH + 1) + x]) <= 0)
							&& (fCurrentDeltaY * mYPhase[y * (WIDTH + 1) + x] <= 0)) {
						dst[(y * (WIDTH + 1) + x) * 2] = src[(iApY * (WIDTH + 1) + iApX) * 2];
						dst[(y * (WIDTH + 1) + x) * 2 + 1] = src[(iApY * (WIDTH + 1) + iApX) * 2 + 1];
						mLock[y * (WIDTH + 1) + x] = 1;
					}
				}
			}
		}
	}

	private void reset() {
		for (int i = 0; i < COUNT * 2; i += 2) {
			float[] src = mOrig;
			float[] dst = mVerts;
			dst[i] = src[i];
			dst[i + 1] = src[i + 1];
		}

		mfCurrentTime = 0;
		mlLastFrameTimeMs = 0;
	}

	void remove() {
		mWindowManager.removeView(this);
	}

}

package com.quku;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.quku.Utils.SystemDef;
import com.quku.note2.MyDialog;

public class PaintLine extends GraphicsActivity implements
		ColorPickerDialog.OnColorChangedListener {
	int m = Color.BLUE;
	float f;
	private RelativeLayout layoutPic;
	private LinearLayout layoutBtn;
	private ImageButton btnColor;
	private ImageButton btnExit;
	private ImageButton btnSetPen;
	private ImageButton btnSetEra;
	private ImageButton btnErase;
	private ImageButton btnSave;
	private ImageButton btnClear;
	private MyView myView;
	private MaskFilter mEmboss;
	private MaskFilter mBlur;
	int F = LayoutParams.MATCH_PARENT;
	int W = LayoutParams.WRAP_CONTENT;
	public static int picAllViewWide = SystemDef.System.IMG_PICTURE_WIDTH;
	public static int picAllViewHigh = SystemDef.System.IMG_PICTURE_HEIGHT;
	// 新增替换绘画
	private Bitmap _mBitmap;
	private Canvas _mCanvas;
	private Path _mPath;
	private Paint _mBitmapPaint;// 画布的画笔
	private Paint _mPaint;// 真实的画笔
	private float _mX, _mY;// 临时点坐标
	private static final float TOUCH_TOLERANCE = 4;
	private int _myColor;
	private boolean isClear = false;
	// 保存Path路径的集合,用List集合来模拟栈
	private static List<DrawPath> savePath;
	// 记录Path路径的对象
	private DrawPath dp;

	private class DrawPath {
		public Path path;// 路径
		public Paint paint;// 画笔
	}

	RelativeLayout.LayoutParams lp1;
	RelativeLayout.LayoutParams lp2;
	MyDialog myAlertDialog;
	Context myContext;
	private int paintSize = 5;// 字体粗细

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myContext = this;
		_mPaint = new Paint();
		_mPaint.setAntiAlias(true);
		_mPaint.setStyle(Paint.Style.STROKE);
		_mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
		_mPaint.setStrokeCap(Paint.Cap.SQUARE);// 形状
		_mPaint.setStrokeWidth(5);// 画笔宽度
		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);
		mBlur = new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL);
		/* 获取当前待编辑的图片名 */
		final String editFilename = this.getIntent().getStringExtra(
				"currentfilename");
		layoutPic = new RelativeLayout(getBaseContext());

		layoutBtn = new LinearLayout(this.getBaseContext());
		layoutBtn.setOrientation(LinearLayout.HORIZONTAL);

		btnColor = new ImageButton(this.getBaseContext());
		btnColor.setBackgroundResource(R.drawable.color);
		btnSetPen = new ImageButton(this.getBaseContext());
		btnSetPen.setBackgroundResource(R.drawable.cuoxi);
		btnErase = new ImageButton(this.getBaseContext());
		btnErase.setBackgroundResource(R.drawable.erase);
		btnSetEra = new ImageButton(this.getBaseContext());
		btnSetEra.setBackgroundResource(R.drawable.cuoxi);
		btnSave = new ImageButton(this.getBaseContext());
		btnSave.setBackgroundResource(R.drawable.save);
		btnClear = new ImageButton(this.getBaseContext());
		btnClear.setBackgroundResource(R.drawable.clear);
		btnExit = new ImageButton(this.getBaseContext());
		btnExit.setBackgroundResource(R.drawable.iamges2);

		LinearLayout.LayoutParams ButtonParam = new LinearLayout.LayoutParams(
				126, 86, 1);
		btnColor.setLayoutParams(ButtonParam);
		btnSetPen.setLayoutParams(ButtonParam);
		btnErase.setLayoutParams(ButtonParam);
		btnSetEra.setLayoutParams(ButtonParam);
		btnSave.setLayoutParams(ButtonParam);
		btnExit.setLayoutParams(ButtonParam);
		btnClear.setLayoutParams(ButtonParam);

		layoutBtn.addView(btnSetPen);
		layoutBtn.addView(btnColor);
		layoutBtn.addView(btnErase);
		layoutBtn.addView(btnSave);
		layoutBtn.addView(btnClear);
		layoutBtn.addView(btnExit);

		lp1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp1.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

		myView = new MyView(getBaseContext(), editFilename);

		lp2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp2.addRule(RelativeLayout.BELOW, myView.getId());

		layoutPic.addView(myView, lp1);
		layoutPic.addView(layoutBtn, lp2);
		setContentView(layoutPic);

		btnExit.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				isClear = false;
				if (myView != null) {
					myView = null;
				}
				finish();
			}
		});
		btnSetEra.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				_mPaint.setStrokeWidth(50);
				isClear = false;
				if (_mPaint.getMaskFilter() != mBlur) {
					_mPaint.setMaskFilter(mBlur);
				} else {
					_mPaint.setMaskFilter(null);
				}
			}
		});
		btnErase.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				_mPaint.setStrokeWidth(50);
				_mPaint.setColor(Color.WHITE);
				_mPaint.setXfermode(new PorterDuffXfermode(
						PorterDuff.Mode.DST_OUT));
			}
		});

		btnColor.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				_mPaint.setXfermode(null);
				_mPaint.setAlpha(0xFF);
				isClear = false;
				showColorDialog();
			}
		});

		btnSetPen.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				_mPaint.setXfermode(null);
				_mPaint.setAlpha(0xFF);
				if (_mPaint.getMaskFilter() != mEmboss) {
					_mPaint.setMaskFilter(mEmboss);
				} else {
					_mPaint.setMaskFilter(null);
				}

				final SeekBar seekBar = new SeekBar(PaintLine.this);
				seekBar.setMax(10);// 设置最大刻度
				seekBar.setProgress(paintSize);// 设置当前刻度
				seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromTouch) {
						paintSize = progress;
					}

					public void onStartTrackingTouch(SeekBar seekBar) {// 开始拖动
					}

					public void onStopTrackingTouch(SeekBar seekBar) {// 结束拖动
					}
				});
				myAlertDialog = (MyDialog) new MyDialog(myContext);
				myAlertDialog.setTitle("选择笔迹粗细");
				myAlertDialog.setView(seekBar);
				myAlertDialog.setButton("确定",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (_mBitmapPaint != null) {
									_mBitmapPaint.reset();
								}
								_mPaint = new Paint();
								_mPaint.setAntiAlias(true);
								_mPaint.setStyle(Paint.Style.STROKE);
								_mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
								_mPaint.setStrokeCap(Paint.Cap.SQUARE);// 形状
								if (_myColor != 0) {
									_mPaint.setColor(_myColor);
								}
								_mPaint.setStrokeWidth(paintSize);
								_mPaint.setXfermode(null);
								System.out.println(" setPen isClear = "
										+ (isClear));
								if (isClear) {
									_mBitmap = Bitmap.createBitmap(
											picAllViewWide, picAllViewHigh,
											Bitmap.Config.ARGB_8888);
								}
								_mCanvas = new Canvas(_mBitmap);
								_mBitmapPaint = new Paint(Paint.DITHER_FLAG);

							}
						});
				myAlertDialog.setButton2("取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}

						});
				myAlertDialog.show();
			}
		});

		btnSave.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				isClear = false;
				File fOri = new File(editFilename);
				int subIndex = editFilename.indexOf(".");
				String newFilenameStoreOri = editFilename
						.substring(0, subIndex)
						+ "_origin"
						+ editFilename.subSequence(subIndex,
								editFilename.length());
				File outF = new File(newFilenameStoreOri);
				copyfile(fOri, outF);

				/* 临时画布用来保存图片和笔记 */
				Canvas mCanvasTmp = null;
				Bitmap mBitmapTmp;
				Bitmap bit;
				/* 保存画布 */
				_mCanvas.save(Canvas.ALL_SAVE_FLAG);
				_mCanvas.restore();
				/* 创建临时画布 */
				mBitmapTmp = Bitmap.createBitmap(picAllViewWide,
						picAllViewHigh, Bitmap.Config.ARGB_8888);
				mCanvasTmp = new Canvas(mBitmapTmp);
				/* 创建要保存的图片mBitmap */
				bit = BitmapFactory.decodeFile(editFilename);
				/* 画图片 */
				mCanvasTmp.drawBitmap(bit, 0, 0, null);
				/* 画笔迹 */
				mCanvasTmp.drawBitmap(_mBitmap, 0, 0, null);
				String newFilename = editFilename;

				/* 使用临时画布mBitmapTmp生成文件 */
				File f = new File(newFilename);
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(f);
					mBitmapTmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
					Toast.makeText(PaintLine.this, "修改后的图片文件保存成功",
							Toast.LENGTH_LONG).show();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		});

		/* 清空按钮的功能是把之前预留的原始图片覆盖掉已经做处理后的图片 */
		btnClear.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				isClear = false;
				File fOri = new File(editFilename);
				int subIndex = editFilename.indexOf(".");
				String trueFilenameStoreOri = editFilename.substring(0,
						subIndex)
						+ "_origin"
						+ editFilename.subSequence(subIndex,
								editFilename.length());
				File trueFile = new File(trueFilenameStoreOri);
				if (trueFile.exists()) {
					layoutPic.removeAllViews();
					copyfile(trueFile, fOri);
					boolean b = copyfile(trueFile, fOri);
					System.out.println(b);

					myView = new MyView(PaintLine.this.getBaseContext(),
							editFilename);

					layoutPic.addView(myView, lp1);
					layoutPic.addView(layoutBtn, lp2);
					layoutPic.postInvalidate();
					setContentView(layoutPic);
					if (trueFile.delete()) {
						Toast.makeText(PaintLine.this, "图片已经复原，原始图片备份删除",
								Toast.LENGTH_LONG).show();
					}
				} else {
					_mPaint = new Paint();
					_mPaint.setAntiAlias(true);
					_mPaint.setStyle(Paint.Style.STROKE);
					_mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
					_mPaint.setStrokeCap(Paint.Cap.SQUARE);// 形状
					_mPaint.setStrokeWidth(5);// 画笔宽度
					if (_myColor != 0) {
						_mPaint.setColor(_myColor);
					}
					layoutPic.removeAllViews();
					myView = new MyView(PaintLine.this.getBaseContext(),
							editFilename);
					layoutPic.addView(myView, lp1);
					layoutPic.addView(layoutBtn, lp2);
					setContentView(layoutPic);
					Toast.makeText(PaintLine.this, "本文件已经是原始图片",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	public void showColorDialog() {
		isClear = false;
		new ColorPickerDialog(this, this, _mPaint.getColor()).show();
	}

	public void colorChanged(int color) {
		isClear = false;
		_myColor = color;
		_mPaint.setColor(color);
	}

	public boolean copyfile(File originPath, File finalPath) {
		FileInputStream read = null;
		FileOutputStream write = null;
		BufferedInputStream bread = null;
		BufferedOutputStream bwrite = null;
		try {
			read = new FileInputStream(originPath);
			bread = new BufferedInputStream(read);
			write = new FileOutputStream(finalPath);
			bwrite = new BufferedOutputStream(write);
			int index = 0;
			while ((index = bread.read()) != -1) {
				bwrite.write(index);
			}
			return true;
		} catch (IOException ex) {
			return false;
		} finally {
			try {
				bread.close();
				bwrite.close();
			} catch (IOException ex) {
				return false;
			}
		}
	}

	public class MyView extends View {
		public MyView(Context c, String eidtFilename) {
			super(c);
			BitmapDrawable mBit = new BitmapDrawable(eidtFilename);
			this.setBackgroundDrawable(mBit);
			_mBitmapPaint = new Paint(Paint.DITHER_FLAG);
			_mBitmap = Bitmap.createBitmap(picAllViewWide, picAllViewHigh,
					Bitmap.Config.ARGB_8888);
			// 保存一次一次绘制出来的图形
			_mCanvas = new Canvas(_mBitmap);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
		}

		protected void onDraw(Canvas mbCanvas) {
			mbCanvas.drawBitmap(_mBitmap, 0, 0, _mBitmapPaint);
			if (_mPath != null) {
				// 实时的显示
				mbCanvas.drawPath(_mPath, _mPaint);
			}
		}

		private void touch_start(float x, float y) {
			_mPath.moveTo(x, y);
			_mX = x;
			_mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - _mX);
			float dy = Math.abs(_mY - y);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				// 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也是可以的)
				_mPath.quadTo(_mX, _mY, (x + _mX) / 2, (y + _mY) / 2);
				_mX = x;
				_mY = y;
			}
		}

		private void touch_up() {
			_mPath.lineTo(_mX, _mY);
			_mCanvas.drawPath(_mPath, _mPaint);
			// 将一条完整的路径保存下来(相当于入栈操作)
			// savePath.add(dp);
			_mPath = null;// 重新置空
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 每次down下去重新new一个Path
				_mPath = new Path();
				// 每一次记录的路径对象是不一样的
				dp = new DrawPath();
				dp.path = _mPath;
				dp.paint = _mPaint;
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				break;
			}
			return true;
		}

	}
}

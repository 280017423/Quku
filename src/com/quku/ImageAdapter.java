package com.quku;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<String> picList = null;
	private PageWidget pageWidget;
	Bitmap curBitmap, nextBitmap;
	Canvas curCanvas, nextCanvas;
	public ImageView imageview;
	// private DetialGallery g;
	public static int pposition;
	int i = 0;
	boolean isMove = false;

	public static int getPposition() {
		return pposition;
	}

	public static void setPposition(int pposition) {
		ImageAdapter.pposition = pposition;
	}

	public int page = 0;
	private int doPositionType;

	public int getDoPositionType() {
		return doPositionType;
	}

	public void setDoPositionType(int doPositionType) {
		this.doPositionType = doPositionType;
	}

	public ImageAdapter(Context c, ArrayList<String> picList) {
		mContext = c;
		this.picList = picList;
		this.pageWidget = new PageWidget(c);
	}

	public int getCount() {
		if (null == picList)
			return 0;
		return picList.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public void toRightPager() {
		Log.d("aaa", "上一页");
		if (pposition >= 1) {
			curBitmap = BitmapFactory.decodeFile(picList.get(pposition)
					.toString());
			nextBitmap = BitmapFactory.decodeFile(picList.get(pposition - 1)
					.toString());
			pposition--;
		} else {
			curBitmap = BitmapFactory.decodeFile(picList.get(pposition)
					.toString());
			nextBitmap = BitmapFactory.decodeFile(picList.get(pposition)
					.toString());
			Toast.makeText(mContext, "第一页", Toast.LENGTH_SHORT).show();
		}

		pageWidget.setBitmaps(curBitmap, nextBitmap);
		pageWidget.invalidate();
	}

	public void toLeftPager() {
		Log.d("aaa", "下一页");
		if (pposition < (picList.size() - 1)) {
			curBitmap = BitmapFactory.decodeFile(picList.get(pposition)
					.toString());
			System.out.println(" next position path = "
					+ picList.get(pposition + 1).toString() + "\t"
					+ "ppostion = " + pposition);
			nextBitmap = BitmapFactory.decodeFile(picList.get(pposition + 1)
					.toString());
			pposition++;
		} else {
			curBitmap = BitmapFactory.decodeFile(picList.get(pposition)
					.toString());
			nextBitmap = BitmapFactory.decodeFile(picList.get(pposition)
					.toString());
			Toast.makeText(mContext, "最后一页", Toast.LENGTH_SHORT).show();
		}
		pageWidget.setBitmaps(curBitmap, nextBitmap);
		pageWidget.invalidate();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		pposition = position;
		pageWidget.setLayoutParams(new Gallery.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		String path = picList.get(pposition);
		curBitmap = BitmapFactory.decodeFile(path);
		nextBitmap = BitmapFactory.decodeFile(path);
		if (null == curBitmap || null == nextBitmap) {
			BitmapDrawable bd = (BitmapDrawable) mContext.getResources()
					.getDrawable(R.drawable.music_file_default);
			curBitmap = bd.getBitmap();
			nextBitmap = bd.getBitmap();
		}
		pageWidget.setBitmaps(curBitmap, curBitmap);
		if (picList.size() != 1) {

			pageWidget.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// boolean res = false;
					if (v == pageWidget) {
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							pageWidget.abortAnimation();
							pageWidget.calcCornerXY(event.getX(), event.getY());
							if (null == curBitmap || null == nextBitmap) {
								BitmapDrawable bd = (BitmapDrawable) mContext
										.getResources().getDrawable(
												R.drawable.music_file_default);
								curBitmap = bd.getBitmap();
								nextBitmap = bd.getBitmap();
							}
							if (pageWidget.DragToRight()) {

								if (pposition >= 1) {
									curBitmap = BitmapFactory
											.decodeFile(picList.get(pposition)
													.toString());
									nextBitmap = BitmapFactory
											.decodeFile(picList.get(
													pposition - 1).toString());
									pposition--;
								} else {
									curBitmap = BitmapFactory
											.decodeFile(picList.get(pposition)
													.toString());
									nextBitmap = BitmapFactory
											.decodeFile(picList.get(pposition)
													.toString());
									Toast.makeText(mContext, "第一页",
											Toast.LENGTH_SHORT).show();
									return false;
								}

							} else {
								if (pposition < (picList.size() - 1)) {
									curBitmap = BitmapFactory
											.decodeFile(picList.get(pposition)
													.toString());
									System.out.println(" next position path = "
											+ picList.get(pposition + 1)
													.toString() + "\t"
											+ "ppostion = " + pposition);
									nextBitmap = BitmapFactory
											.decodeFile(picList.get(
													pposition + 1).toString());
									pposition++;
								} else {
									curBitmap = BitmapFactory
											.decodeFile(picList.get(pposition)
													.toString());
									nextBitmap = BitmapFactory
											.decodeFile(picList.get(pposition)
													.toString());
									Toast.makeText(mContext, "最后一页",
											Toast.LENGTH_SHORT).show();
									return false;
								}
							}
							pageWidget.setBitmaps(curBitmap, nextBitmap);
							// if (page == 0) {
							// int dx, dy;
							// if (pageWidget.mCornerX > 0) {
							// dx = -(int) (768 + event.getX());
							// } else {
							// dx = (int) (768 + 768 - event.getX());
							// }
							// if (pageWidget.mCornerY > 0) {
							// dy = (int) (1318 - event.getY());
							// } else {
							// dy = (int) (1 - event.getY()); //
							// ��ֹmTouch.y���ձ�Ϊ0
							// }
							// pageWidget.mScroller.startScroll(
							// (int) event.getX(), (int) event.getY(),
							// dx, dy, 1000);
							// page++;
							// }
						} else if (event.getAction() == MotionEvent.ACTION_MOVE) {

						}
						pageWidget.doTouchEvent(event);
						return true;
					}
					return false;
				}
			});
		}
		return pageWidget;
	}

}
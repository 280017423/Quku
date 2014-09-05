package com.quku.adapter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.quku.R;

public class CameraImageAdapter extends BaseAdapter {

	private Context mContext; // 瀹氫箟Context
	private Vector<Bitmap> mImageIds; // 瀹氫箟涓?釜鍚戦噺浣滀负鍥剧墖婧?
	private Vector<Boolean> mImage_bs = new Vector<Boolean>(); // 瀹氫箟涓?釜鍚戦噺浣滀负閫変腑涓庡惁瀹瑰櫒
	private boolean multiChoose; // 琛ㄧず褰撳墠閫傞厤鍣ㄦ槸鍚﹀厑璁稿閫?
	private List<String> pathList = null;
	// private List<Integer> imageIndex = new ArrayList<Integer>();
	private Set<Integer> imageIndex = null;

	// private Map<String,Integer> imageIndex = new HashMap<String,Integer>();

	public CameraImageAdapter(Context c, boolean isMulti, String path) {
		mContext = c;
		multiChoose = isMulti;
		mImageIds = this.readSDCard(path);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mImageIds.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(mContext); // 缁橧mageView璁剧疆璧勬簮
			imageView.setLayoutParams(new GridView.LayoutParams(100, 100)); // 璁剧疆甯冨眬鍥剧墖
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER); // 璁剧疆鏄剧ず姣斾緥绫诲瀷
		} else {
			imageView = (ImageView) convertView;
		}
		imageView.setImageDrawable(makeBmp(mImageIds.elementAt(position), mImage_bs.elementAt(position), position));
		return imageView;
	}

	private LayerDrawable makeBmp(Bitmap mainBmp, boolean isChosen, int position) {
		// 鏍规嵁isChosen鏉ラ?鍙栧鍕剧殑鍥剧墖
		Bitmap seletedBmp;
		if (imageIndex == null) {
			imageIndex = new TreeSet<Integer>();
		}
		if (isChosen == true) {
			seletedBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.grid_check_on);
			imageIndex.add(position);
			// Log.i("閫変腑",
			// "绗? + position + "椤圭洰琚?涓? + "set澶у皬 锛? + imageIndex.size());
		} else {
			seletedBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.grid_check_off);
			if (imageIndex.size() > 0) {
				imageIndex.remove(position);
				// Log.i("鍙栨秷",
				// "绗? + position + "椤归?涓鍙栨秷" + "set澶у皬 锛?
				// + imageIndex.size());
			}
		}
		// 浜х敓鍙犲姞鍥?
		Drawable[] array = new Drawable[2];
		array[0] = new BitmapDrawable(mainBmp);
		array[1] = new BitmapDrawable(seletedBmp);
		LayerDrawable la = new LayerDrawable(array);
		la.setLayerInset(0, 0, 0, 0, 0);
		la.setLayerInset(1, 0, 0, 100, 100); // int index, int l, int t, int r,
												// int b
		return la; // 杩斿洖鍙犲姞鍚庣殑鍥?
	}

	// 淇敼閫変腑鐨勭姸鎬?
	public void changeState(int position) {
		// 澶氶?鏃?
		if (multiChoose == true) {
			mImage_bs.setElementAt(!mImage_bs.elementAt(position), position); // 鐩存帴鍙栧弽鍗冲彲
		}
		notifyDataSetChanged(); // 閫氱煡閫傞厤鍣ㄨ繘琛屾洿鏂?
	}

	/**
	 * 璇诲彇SD鍗′笂鎸囧畾鐩綍涓嬫寚瀹氭墿灞曞悕鐨勫浘鐗囨枃浠?
	 * 
	 * @param path
	 * @return
	 */
	public Vector<Bitmap> readSDCard(String path) {
		Vector<Bitmap> vector = new Vector<Bitmap>();
		BitmapFactory.Options options = new BitmapFactory.Options();
		pathList = new ArrayList<String>();
		options.inSampleSize = 10;
		File file = new File(path);
		if (file != null) {
			File[] files = file.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					if (filename.endsWith(".jpg")) {
						return true;
					}
					return false;
				}
			});
			if (files != null) {
				for (File f : files) {
					Bitmap bitmap = BitmapFactory.decodeFile(f.getPath(), options);
					vector.add(zoomBitmap(bitmap, 200, 200));
					pathList.add(f.getAbsolutePath());
					mImage_bs.add(false);
				}
			}
		}
		return vector;
	}

	/**
	 * 澶勭悊鍥剧墖鐨勭缉鐣ュ浘澶у皬
	 * 
	 * @param bitmap
	 * @param w
	 * @param h
	 * @return
	 */
	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return newbmp;
	}

	/**
	 * 鍒犻櫎鍥剧墖
	 * 
	 * @Title: deleteImage
	 * @Description: TODO(杩欓噷鐢ㄤ竴鍙ヨ瘽鎻忚堪杩欎釜鏂规硶鐨勪綔鐢?
	 * @param @param p
	 * @param @return 璁惧畾鏂囦欢
	 * @return boolean 杩斿洖绫诲瀷
	 * @throws
	 */
	public int deleteImage() {
		int i = 0;
		@SuppressWarnings("rawtypes")
		Iterator it = imageIndex.iterator();
		try {
			while (it.hasNext()) {
				int index = (Integer) it.next();
				if (i > 0) {
					index = index - i;
				}
				if (mImageIds != null && index < mImageIds.size() && mImageIds.size() > 0) {
					mImageIds.remove(index); // 灏嗗浘鐗囨簮涓殑鍥剧墖鍒犻櫎
					File file = new File(pathList.get(index));
					if (file.exists()) {
						file.delete(); // 鏍规嵁璺緞灏哠D鍗′笂鐨勫浘鐗囧垹闄?
						pathList.remove(index); // 灏嗚矾寰勯泦鍚堜腑鐩稿簲鐨勫唴瀹瑰垹闄?
						i++;
					}
					mImage_bs.remove(index); // 灏嗛?涓笌鍚﹀鍣ㄤ腑鐩稿簲鐨勯」鐩垹闄?
					// Log.i("imageIndex---->","鍒犻櫎鍚巌mageIndex澶у皬
					// 锛?+imageIndex.size());
				}
			}
			imageIndex.clear();
		} catch (Exception ex) {
			Log.i("ex.printStackTrace", ex.toString());
		}
		return i;
	}

	/**
	 * 鑾峰彇瑕佷笂浼犲浘鐗囩殑璧勬簮
	 * 
	 * @return
	 */
	public List<File> getUploadImage() {
		List<File> fileList = new ArrayList<File>();
		@SuppressWarnings("rawtypes")
		Iterator it = imageIndex.iterator();
		while (it.hasNext()) {
			int index = (Integer) it.next();
			File file = new File(pathList.get(index));
			fileList.add(file);
		}
		return fileList;
	}

}

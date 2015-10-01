package com.quku;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;

import com.quku.activity.MyMusicSheetActivity;
import com.quku.activity.PictureBrowers;

public class MyBook extends Activity {

	private BookLayout blo;
	private SinglePage page;
	public ArrayList<String> picList = new ArrayList<String>();
	public static String selectFilename;

	@Override
	protected void onDestroy() {
		if (null != page)
			page.destory();
		if (null != blo)
			blo.destory();
		if (null != picList)
			picList.clear();
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().getDecorView().setSystemUiVisibility(4);
		selectFilename = this.getIntent().getStringExtra("filename");
		picList = getPiclList(selectFilename);
		String pictureNameIndex = this.getIntent().getStringExtra("nameIndex");
		int pictureIndex;
		try {
			pictureIndex = Integer.parseInt(pictureNameIndex) - 1;
			if (pictureIndex % 2 == 0) {
				pictureIndex = pictureIndex - 1;
			}
		} catch (java.lang.NumberFormatException e) {
			pictureIndex = 0;
		}

		blo = new BookLayout(this);
		blo.setBackgroundResource(R.drawable.blockbg);
		List<SinglePage> singlePageList = new ArrayList<SinglePage>();
		boolean isLeft = false;
		int num = 0;
		for (int i = (picList.size() - 1); i >= 0; i--) {
			PageContent content = new PageContent(this, picList.get(i), blo);
			// if(i==0||i==imgs.length-1){
			// content.setCover(true);
			// }else if(i!=1){
			content.setContentPage(true);
			content.setContentId(num++);

			// }
			page = new SinglePage(this, isLeft, i, blo, content);
			singlePageList.add(page);
			page.destory();
			isLeft = !isLeft;
		}
		if (picList.size() % 2 != 0) {
			PageContent content = new PageContent(this,
					String.valueOf(R.drawable.icon), blo);
			content.setContentPage(true);
			content.setContentId(num++);
			page = new SinglePage(this, isLeft, picList.size(), blo, content);
			singlePageList.add(page);
			page.destory();
		}
		// Paint m_paint = new Paint();
		// blo.setContentList(AndroidUtils.getPageContentStringInfo(m_paint,
		// content, PageContent.getPageContentLine(BookLayout.PAGE_HEIGHT),
		// PageContent.getPageContentWidth(BookLayout.PAGE_WIDTH)));
		blo.setPageList(singlePageList, pictureIndex);
		setContentView(blo);
		// ����������ת
		Configuration cf = MyBook.this.getResources().getConfiguration();
		int ori = cf.orientation;
		if (ori == Configuration.ORIENTATION_PORTRAIT) {
			Intent intent = new Intent();
			intent.setClass(MyBook.this, PictureBrowers.class);
			intent.putExtra("filename", selectFilename);
			int index = BookLayout.position;
			index = index + 1;
			String nameIndex = String.valueOf(index);
			intent.putExtra("nameIndex", nameIndex);
			for (SinglePage p : singlePageList) {
				// if(blo.showList.contains(p)) continue;
				p.getPageContent().destory();
			}
			if (null != blo.showList)
				blo.showList.clear();

			// if ( null != blo. || null != blo.leftShowPage || null !=
			// blo.leftMaskPage){
			// try{
			// blo.leftDownPage.getPageContent().destory();
			// blo.leftShowPage.getPageContent().destory();
			// blo.leftShowPage.getPageContent().destory();
			// }catch (Exception e){
			//
			// }
			// }
			blo.removeAllViews();
			startActivity(intent);
			this.finish();
		}
	}

	public ArrayList<String> getPiclList(String filepath) {
		File file = new File(filepath);
		int i = 0;
		for (File currentFile : file.getParentFile().listFiles()) {
			if ((currentFile.getName().endsWith(".jpg"))
					|| (currentFile.getName().endsWith(".JPG"))
					|| (currentFile.getName().endsWith(".png"))
					|| (currentFile.getName().endsWith(".gif"))
					|| (currentFile.getName().endsWith(".bmp"))) {
				if (!(currentFile.getName().endsWith("_origin.JPG"))) {
					if (!(currentFile.getName().endsWith("_origin.jpg"))) {
						picList.add(currentFile.getAbsolutePath());
					}
				}
			}
		}
		return picList;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setClass(MyBook.this, MyMusicSheetActivity.class);
		startActivity(intent);
		this.finish();
		return super.onKeyDown(keyCode, event);
	}
}

package com.quku.camera;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.quku.R;
import com.quku.adapter.CameraImageAdapter;

/**
 * 图片浏览器
 * 
 * @author Administrator
 */
public class GalleryActivity extends Activity {

	CameraImageAdapter ia_check = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		Bundle bundle = getIntent().getExtras();
		String path = bundle.getString("path");
		ia_check = new CameraImageAdapter(this, true, path);
		GridView gridView = (GridView) findViewById(R.id.gridView);
		gridView.setAdapter(ia_check);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long arg3) {
				// TODO Auto-generated method stub
				ia_check.changeState(position);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "删除选中图片");
		menu.add(0, 2, 0, "返回");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case 1: {
				int returns = ia_check.deleteImage();
				if (returns > 0) {
					Toast.makeText(GalleryActivity.this, returns + "张图片删除成功！", Toast.LENGTH_SHORT).show();
					ia_check.notifyDataSetChanged();
				} else {
					Toast.makeText(GalleryActivity.this, "图片删除失败，请重试！", Toast.LENGTH_SHORT).show();
				}
				break;
			}

			case 2: {
				finish();
				break;
			}
		}
		return false;
	}

}

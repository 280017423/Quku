package com.quku;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

public class DetialGallery extends Gallery {

	public DetialGallery(Context context) {
		super(context);
	}

	public DetialGallery(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public DetialGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false; // 这个语句代表每次只滑动一张图片

		// 每次滑动的比较快
		// if (velocityX > 1200.0f)
		// {
		// velocityX = 1200.0f;
		// }
		// else if(velocityX < 1200.0f)
		// {
		// velocityX = -1200.0f;
		// }
		//
		// return super.onFling(e1, e2, velocityX, velocityY);

	}
}

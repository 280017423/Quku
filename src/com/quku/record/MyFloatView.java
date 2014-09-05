package com.quku.record;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.quku.Utils.Utils;

/**
 * 录音悬浮框
 * 
 * @author Administrator
 * 
 */
public class MyFloatView {

	private Context mContext = null;
	WindowManager wm = null;

	public MyFloatView(Context context) {
		this.mContext = context;
		initView();
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		final Button bb = new Button(mContext);
		// View _myView = new View(mContext);
		// LinearLayout.LayoutParams myLayout= new
		// LinearLayout.LayoutParams(400, 300);
		// myLayout.gravity = Gravity.RIGHT;
		// bb.setLayoutParams(myLayout);
		bb.setText("测试");
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		// wmParams.type = 2002;//这些窗口通常放在上面所有的应用程序，但后面的状态栏。在多用户系统上显示所有用户的窗口。
		wmParams.type = 2003; // 这里是关键，你也可以试试2003
								// 系统窗口，如低功耗警报。这些窗口的应用程序窗口的顶部。在多用户系统中只显示拥有用户的窗口。
		// wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY; ;
		wmParams.format = 1;
		/**
		 * 这里的flags也很关键 代码实际是wmParams.flags |= FLAG_NOT_FOCUSABLE;
		 * 40的由来是wmParams的默认属性（32）+ FLAG_NOT_FOCUSABLE（8）
		 */
		// wmParams.flags=40;

		wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		wmParams.gravity = Gravity.TOP | Gravity.RIGHT;
		wmParams.width = 200;
		wmParams.height = 150;
		wmParams.x = 0;
		wmParams.y = 0;
		bb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Utils.getToast(mContext, "on click...");
			}
		});
		wm.addView(bb, wmParams); // 创建View
	}

	/**
	 * 自定义按钮监听类
	 * 
	 * @author Administrator
	 */
	class MyButtonListenner implements OnClickListener {

		@Override
		public void onClick(View v) {

		}

	}

	/**
	 * 移除view
	 */
	private void removeView() {

	}
}

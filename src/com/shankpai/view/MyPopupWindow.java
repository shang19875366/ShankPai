package com.shankpai.view;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;


public class MyPopupWindow {

	private static final String TAG = "MyPopupWindow";
	private PopupWindow mPopupWindow;
	private int xPos = 0;
	private int yPos = 10;
	
	public MyPopupWindow(){
//		mContext = context;
//		mAnchorView = anchorView;
//		mListItems = listItem;
//		mPopupWindow = new PopupWindow(450, LayoutParams.WRAP_CONTENT);
//		mPopupWindow = new PopupWindow();
	}
	
	public void setPostion(int x, int y){
		xPos = x;
		yPos = y;
	}
	
	public void showPopupWindow(View anchorView, View view) {
		
		if(mPopupWindow == null){
			mPopupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
//		if (mPopupWindow != null) {
//			Log.v(TAG, "mPopupWindow = null");
//			mPopupView = LayoutInflater.from(mContext).inflate(R.layout.popupwindow, null);
//			mListView = (ListView) mPopupView.findViewById(R.id.listview);
//			MyAdapter myAdapter = new MyAdapter(mContext);
//			mListView.setAdapter(myAdapter);
//			mListView.setOnItemClickListener(this);
//			mPopupWindow = new PopupWindow(450, LayoutParams.WRAP_CONTENT);
			mPopupWindow.setContentView(view);
			mPopupWindow.setFocusable(true);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopupWindow.update();
			mPopupWindow.showAsDropDown(anchorView, xPos, yPos);
//		}
/*		
		else{
			mPopupWindow = new PopupWindow(view, 450, LayoutParams.WRAP_CONTENT);
		}
*/		
	}
	
	public void dismissWindow(){
		if(mPopupWindow != null){
			mPopupWindow.dismiss();
		}
	}
}



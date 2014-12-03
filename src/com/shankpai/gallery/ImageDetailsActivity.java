package com.shankpai.gallery;

import java.io.File;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher.ViewFactory;

import com.shankpai.R;

/**
 * 查看大图的Activity界面�?
 * 
 * @author alex
 */
public class ImageDetailsActivity extends Activity implements OnGestureListener, ViewFactory {

	private static final String TAG = ImageDetailsActivity.class.getSimpleName();
	private ImageSwitcher mSwitcher;
	private GestureDetector mDetector;
	private ArrayList<String> mAllPaths;
	private int mIndex;
	private ActionBar mActionBar; 
	
	/**
	 * 待展示的图片
	 */
	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_view);
		mActionBar = this.getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		mDetector = new GestureDetector(this);
		mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
		mSwitcher.setFactory(this);
		mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
		// 取出图片路径，并解析成Bitmap对象，然后在ZoomImageView中显�?
		String imagePath = getIntent().getStringExtra("image_path");
//		System.out.println("image_path = " + imagePath);
		mAllPaths = Images.getPathsList();
//		System.out.println("allpath = " + mAllPaths);
		mIndex = mAllPaths.indexOf(imagePath);
//		System.out.println("mIndex = " + mIndex);
		setImageBitmap(imagePath);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		case R.id.action_share:
//			openShareBoard();
			doShare();
			break;
			
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void setImageBitmap(String imagePath) {
		bitmap = BitmapFactory.decodeFile(imagePath);
		mSwitcher.setImageDrawable(new BitmapDrawable(bitmap));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mDetector.onTouchEvent(event);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mIndex = -1;
		// 记得将Bitmap对象回收�?
		if (bitmap != null) {
			bitmap.recycle();
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if(e1.getX() - e2.getX() > 100) {
			showNext();
			return true;
		} else if(e1.getX() - e2.getX() < -100) {
			showPrevious();
			return true;
		}
		return false;
	}
	
	public void showPrevious() {
		if(mIndex > 0) {
			mIndex --;
//			System.out.println("mIndex = " + mIndex);
			String imagePath = mAllPaths.get(mIndex);
			setImageBitmap(imagePath);
		}
	}
	
	public void showNext() {
		if(mIndex < (mAllPaths.size() -1)) {
			mIndex ++;
//			System.out.println("mIndex = " + mIndex);
			String imagePath = mAllPaths.get(mIndex);
			setImageBitmap(imagePath);
		}
	}

	@Override
	public View makeView() {
		ImageView image = new ImageView(this);
		image.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return image;
	}
	
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	};
	
	private void doShare() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		File file = new File(mAllPaths.get(mIndex));
		if(file != null && file.exists() && file.isFile()) {
			intent.setType("image/*");
			Uri uri = Uri.fromFile(file);
			intent.putExtra(Intent.EXTRA_STREAM, uri);
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
		startActivity(Intent.createChooser(intent, getTitle()));
	}
}
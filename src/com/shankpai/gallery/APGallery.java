package com.shankpai.gallery;

import java.util.ArrayList;

import com.shankpai.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class APGallery extends Activity {

	private ActionBar mActionBar;
	private GridView mGridView;
	private ArrayList<String> mPathList;
	private ProgressDialog mDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		setContentView(R.layout.ap_gallery2);
		mActionBar = this.getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		mGridView = (GridView) findViewById(R.id.grid);
		mDialog = new ProgressDialog(this);
		mDialog.setMessage("Loading....");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		new LoadImageTask().execute();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
	
	class LoadImageTask extends AsyncTask<Void, Void, ArrayList<String>> {

		protected void onPreExecute() {
			super.onPreExecute();
			mDialog.show();
		};

		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			return Images.getPathsList();
		}

		protected void onPostExecute(final java.util.ArrayList<String> result) {
			mDialog.dismiss();
			mGridView.setAdapter(new GalleryAdapter(APGallery.this, Images
					.getPathsList(), mGridView));
			mGridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent = new Intent(APGallery.this,
							ImageDetailsActivity.class);
					intent.putExtra("image_path", result.get(position));
					startActivity(intent);
				}
			});
			mGridView.setSelection(result.size() - 1);
		};

	}
}

package com.shankpai.gallery;

import java.util.ArrayList;

import com.shankpai.R;
import com.shankpai.gallery.AsyncImageLoader.ImageCallback;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GalleryAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<String> mLists;
	private LayoutInflater mInflater;
	private AsyncImageLoader asyncImageLoader;
	private GridView mGridView;
	
	public GalleryAdapter(Context context, ArrayList<String> lists, GridView gridView) {
		this.mContext = context;
		this.mLists = lists;
		mInflater = LayoutInflater.from(context);
		asyncImageLoader = new AsyncImageLoader();
		this.mGridView = gridView;
	}
	
	@Override
	public int getCount() {
		return mLists.size();
	}

	@Override
	public Object getItem(int position) {
		return mLists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.grid_adapter, null);
			holder.imageView = (ImageView) convertView.findViewById(R.id.img);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String path = mLists.get(position);
		ImageView imageView = holder.imageView;
		imageView.setTag(path);
		Drawable cachedImg = asyncImageLoader.loadDrawable(path, new ImageCallback() {
			
			@Override
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				ImageView imageViewByTag = (ImageView) mGridView.findViewWithTag(imageUrl);  
				if (imageViewByTag != null) {  
                    imageViewByTag.setImageDrawable(imageDrawable);  
                }
			}
		});
		if (cachedImg != null) {  
            imageView.setImageDrawable(cachedImg);
        }
		return convertView;
	}
	
	class ViewHolder {
		private ImageView imageView;
	}
}

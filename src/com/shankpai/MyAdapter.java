package com.shankpai;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

	public class MyAdapter extends BaseAdapter{
		
		private static final String TAG = "MyAdapter";
		private Context mContext;
		private List<HashMap<String, Object>> mListItems;
		
		public MyAdapter(Context context, List<HashMap<String, Object>> list){
			mContext = context;
			mListItems = list;
		}
		
		public void setListItem(List<HashMap<String, Object>> listitem){
			mListItems = listitem;
		}
		
		@Override
		public int getCount() {
			return mListItems.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ContentViewHolder viewHolder = null;
			if(convertView == null){
//				Log.v(TAG, "convertView = null");
				viewHolder = new ContentViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem, null);
				viewHolder.image = (ImageView) convertView.findViewById(R.id.iv_img);
				//viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);
				viewHolder.check = (RadioButton)convertView.findViewById(R.id.radio_button);
				convertView.setTag(viewHolder);
			}
			else{
//				Log.v(TAG, "convertView != null");
				viewHolder = (ContentViewHolder) convertView.getTag();
			}
			viewHolder.image.setBackgroundResource((Integer)mListItems.get(position).get("image"));
			//viewHolder.title.setText((String)mListItems.get(position).get("title"));
			viewHolder.check.setChecked((Boolean)mListItems.get(position).get("status"));
			return convertView;
		}
		
		public class ContentViewHolder {
			public ImageView image;
			public TextView title;
			public TextView value;
			public RadioButton check;
		}   
		
	}
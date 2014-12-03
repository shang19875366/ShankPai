package com.shankpai.utils;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera.Size;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
//import android.util.Log;

public class MediaManager {
	
	private static final String TAG = "imageManager";
	public static final String DCIM =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
	public static final String DIRECTORY = DCIM + "/Camera";
    public static final String BUCKET_ID =
            String.valueOf(DIRECTORY.toLowerCase().hashCode());
	public static final String Looq_BUCKET_ID = String
			.valueOf((Environment.getExternalStorageDirectory()
					.getAbsoluteFile() + "/Shankpai/.TempData").toLowerCase()
					.hashCode());
	private static final int THUMBNAILW = 96;
	private static final int THUMBNAILH = 96;
    
	public static Media getLastImageMedia(ContentResolver resolver){
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;

        Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1").build();

        String[] projection = new String[] {ImageColumns._ID, ImageColumns.ORIENTATION,
        									ImageColumns.DATE_TAKEN};     
        String selection = ImageColumns.MIME_TYPE + "='image/jpeg' AND " +
                ImageColumns.BUCKET_ID + '=' + BUCKET_ID;
        String order = ImageColumns.DATE_TAKEN + " DESC," + ImageColumns._ID + " DESC";

        Cursor cursor = null;
        try {
            cursor = resolver.query(query, projection, selection, null, order);
            if (cursor != null && cursor.moveToFirst()) {
            	long id = cursor.getLong(0);
            	int orientation = cursor.getInt(1);
            	Bitmap ThumbnailImageBm = Images.Thumbnails.getThumbnail(resolver, id, Images.Thumbnails.MICRO_KIND, null);
//            	Log.v(TAG, "ThumbnailBm size = " + ThumbnailImageBm.getWidth() + " * " + ThumbnailImageBm.getHeight());
            	return new Media(id, orientation, ThumbnailImageBm,
            						ContentUris.withAppendedId(baseUri, id));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
	
	public static Media getLastImageMedia(Context context){
		ArrayList<String> paths = ShankPaiUtils.getPathsList();
		if(paths.size() > 0) {
			String path = paths.get(paths.size()-1);
			File file = new File(path);
			Uri uri = Uri.fromFile(file);
			Bitmap bitmap = ShankPaiUtils.getImageThumbnail(path, THUMBNAILW, THUMBNAILH);
			int orientation = 0;
			Media media = new Media(-1, orientation, bitmap, uri);
			return media;
		} else {
			return null;
		}
	}
	
	public static Media getLastVideoMedia(ContentResolver resolver) {
		Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;
		Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1")
				.build();
		String[] projection = new String[] { VideoColumns._ID,
				MediaColumns.DATA, VideoColumns.DATE_TAKEN };
		String selection = VideoColumns.BUCKET_ID + '=' + BUCKET_ID;
		String order = VideoColumns.DATE_TAKEN + " DESC," + VideoColumns._ID
				+ " DESC";

		Cursor cursor = null;
		try {
			cursor = resolver.query(query, projection, selection, null, order);
			if (cursor != null && cursor.moveToFirst()) {
				// String videoPath = cursor.getString(1);
				// Log.v(TAG, "videoPath = " + videoPath);
				long id = cursor.getLong(0);
				Bitmap ThumbnailVideoBm = Video.Thumbnails.getThumbnail(
						resolver, id, Video.Thumbnails.MICRO_KIND, null);
				return new Media(id, 0, ThumbnailVideoBm,
						ContentUris.withAppendedId(baseUri, id));
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}
	
	public static Bitmap createPictureBitmap(byte[] jpeg, Size pics, Size pres){
		
		int ratio = (int)Math.ceil((double)pics.width / pres.width);
		int inSampleSize = Integer.highestOneBit(ratio);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		Bitmap originalBitmap =  BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length, options);
		Bitmap thumbnailBmp = ThumbnailUtils.extractThumbnail(originalBitmap, THUMBNAILW, THUMBNAILH);
		if(!originalBitmap.isRecycled()){
			originalBitmap.recycle();
			System.gc();
		}
		return thumbnailBmp;
	}
	
	public static Bitmap createVideoBitmap(String filepath){
		Bitmap originalBitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try{
			retriever.setDataSource(filepath);
			originalBitmap = retriever.getFrameAtTime(-1);
		}
		catch (IllegalArgumentException ex){			
		}
		catch (RuntimeException ex){
		}
		finally{
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
		}
		Bitmap thumbnailBmp = ThumbnailUtils.extractThumbnail(originalBitmap, THUMBNAILW, THUMBNAILH);
		return thumbnailBmp;	
	}
	
	public static class Media{
		
		private long id;
		private int orientation;
		private Bitmap thumbnailBm;
		private Uri uri;
		
		public Media(long id, int orientation, Bitmap bitmap, Uri uri){
			this.id = id;
			this.orientation = orientation;
			this.thumbnailBm = bitmap;
			this.uri = uri;
		}
		
		public long getID(){
			return id;
		}
		
		public int getOrientation(){
			return orientation;
		}
		
		public Bitmap getBitmap(){
			return thumbnailBm;
		}
		
		public Uri getUri(){
			return uri;
		}
	}
	
	
	public static Media getLastImageMedia(ContentResolver resolver,Context context){
		String bucketid = String.valueOf(SharedPreferencesHelper.getStorePath(context).toLowerCase().hashCode());
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;

        Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1").build();

        String[] projection = new String[] {ImageColumns._ID, ImageColumns.ORIENTATION,
        									ImageColumns.DATE_TAKEN};     
        String selection = ImageColumns.MIME_TYPE + "='image/jpeg' AND " +
                ImageColumns.BUCKET_ID + '=' + bucketid;
        String order = ImageColumns.DATE_TAKEN + " DESC," + ImageColumns._ID + " DESC";

        Cursor cursor = null;
        try {
            cursor = resolver.query(query, projection, selection, null, order);
            if (cursor != null && cursor.moveToFirst()) {
            	long id = cursor.getLong(0);
            	int orientation = cursor.getInt(1);
            	Bitmap ThumbnailImageBm = Images.Thumbnails.getThumbnail(resolver, id, Images.Thumbnails.MICRO_KIND, null);
//            	Log.v(TAG, "ThumbnailBm size = " + ThumbnailImageBm.getWidth() + " * " + ThumbnailImageBm.getHeight());
            	return new Media(id, orientation, ThumbnailImageBm,
            						ContentUris.withAppendedId(baseUri, id));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
	
	public static Media getLastVideoMedia(ContentResolver resolver,Context context) {
		String bucketid = String.valueOf(SharedPreferencesHelper.getStorePath(context).toLowerCase().hashCode());
		Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;
		Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1")
				.build();
		String[] projection = new String[] { VideoColumns._ID,
				MediaColumns.DATA, VideoColumns.DATE_TAKEN };
		String selection = VideoColumns.BUCKET_ID + '=' + bucketid;
		String order = VideoColumns.DATE_TAKEN + " DESC," + VideoColumns._ID
				+ " DESC";

		Cursor cursor = null;
		try {
			cursor = resolver.query(query, projection, selection, null, order);
			if (cursor != null && cursor.moveToFirst()) {
				// String videoPath = cursor.getString(1);
				// Log.v(TAG, "videoPath = " + videoPath);
				long id = cursor.getLong(0);
				Bitmap ThumbnailVideoBm = Video.Thumbnails.getThumbnail(
						resolver, id, Video.Thumbnails.MICRO_KIND, null);
				return new Media(id, 0, ThumbnailVideoBm,
						ContentUris.withAppendedId(baseUri, id));
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}
	
}

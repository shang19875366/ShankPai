package com.shankpai.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.media.CamcorderProfile;

public class SharedPreferencesHelper {
	
	private static final String PreferenceName = "SP"; 
	
	public static String getFocus(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		String focus = sp.getString("focus", "auto");
		return focus;
	}
	
	public static void setFocus(Context context,String focus){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putString("focus", focus).commit();
	}
	
	public static int getPictureExposure(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		int code = sp.getInt("exposure", 0);
		return code;
	}
	
	public static void setPictureExposure(Context context,int exposure){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putInt("exposure", exposure).commit();
	}
	
	public static String getPictureFlash(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		String code = sp.getString("flash", "off");
		return code;
	}
	
	public static void setPictureFlash(Context context,String flash){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putString("flash", flash).commit();
	}
	
	public static String getPictureWB(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		String code = sp.getString("whitebalance", "Auto");
		return code;
	}
	
	public static void setPictureWB(Context context,String wb){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putString("whitebalance", wb).commit();
	}
	
	public static int getPictureWidth(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		int code = sp.getInt("picturewidht", 0);
		return code;
	}
	
	public static void setPictureWidth(Context context,int width){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putInt("picturewidht", width).commit();
	}
	
	public static int getPictureHeight(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		int code = sp.getInt("pictureheight", 0);
		return code;
	}
	
	public static void setPictureHeight(Context context,int height){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putInt("pictureheight", height).commit();
	}
	
	public static int getShotMode(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		int code = sp.getInt("shotmode", 0);
		return code;
	}
	
	public static void setShotMode(Context context,int shot){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putInt("shotmode", shot).commit();
	}
	
	public static String getVideoFlash(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		String code = sp.getString("videoflash", "off");
		return code;
	}
	
	public static void setVideoFlash(Context context,String flash){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putString("videoflash", flash).commit();
	}
	
	public static int getVideoExposure(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		int code = sp.getInt("videoexposure", 0);
		return code;
	}
	
	public static void setVideoExposure(Context context,int exposure){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putInt("videoexposure", exposure).commit();
	}
	
	public static String getVideoWB(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		String code = sp.getString("videowhitebalance", "Auto");
		return code;
	}
	
	public static void setVideoWB(Context context,String wb){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putString("videowhitebalance", wb).commit();
	}
	
	public static int getVideoQulity(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		int code = sp.getInt("videoqulity", CamcorderProfile.QUALITY_HIGH);
		return code;
	}
	
	public static void setVideoQulity(Context context,int qulity){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putInt("videoqulity", qulity).commit();
	}
	
/*	
	public static int getVideoWidth(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		int code = sp.getInt("videowidht", 0);
		return code;
	}
	
	public static void setVideoWidth(Context context,int width){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putInt("picturewidht", width).commit();
	}
	
	public static int getVideoHeight(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		int code = sp.getInt("videoheight", 0);
		return code;
	}
	
	public static void setVideoHeight(Context context,int height){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putInt("videoheight", height).commit();
	}
*/	
	public static String getSn(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		String code = sp.getString("sn", null);
		return code;
	}
	
	public static void setSn(Context context,String sn){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putString("sn", sn).commit();
	}
	
	public static String getLimit(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		String code = sp.getString("limit", null);
		return code;
	}
	
	public static void setLimit(Context context,String limit){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putString("limit", limit).commit();
	}
	
	public static String getStorePath(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		String code = sp.getString("storepath", null);
		return code;
	}
	 
	public static void setStorePath(Context context,String storepath){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putString("storepath", storepath).commit();
	}
	
	public static int getFilter(Context context){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_READABLE);
		int filterindex = sp.getInt("filterindex", 0);
		return filterindex;
	}
	
	public static void setFilter(Context context,int filterindex){
		SharedPreferences sp = context.getSharedPreferences(PreferenceName,Context.MODE_WORLD_WRITEABLE);
		sp.edit().putInt("filterindex", filterindex).commit();
	}
	
}

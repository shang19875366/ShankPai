package com.shankpai.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class Storage {

	private static final String TAG = "Storage";
	public static final String DCIM = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
			.toString();

	public static final String DIRECTORY = DCIM + "/Camera";
	public static final long UNAVAILABLE = -1L;
	public static final long PREPARING = -2L;
	public static final long UNKNOWN_SIZE = -3L;
	public static final long LOW_STORAGE_THRESHOLD = 50000000;
	public static final long PICTURE_SIZE = 1500000;

	public static long getAvailableSpace() {
		String state = Environment.getExternalStorageState();
//		Log.d(TAG, "External storage state=" + state);
		if (Environment.MEDIA_CHECKING.equals(state)) {
			return PREPARING;
		}
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			return UNAVAILABLE;
		}

		File dir = new File(DIRECTORY);
		dir.mkdirs();
		if (!dir.isDirectory() || !dir.canWrite()) {
			return UNAVAILABLE;
		}

		try {
			StatFs stat = new StatFs(DIRECTORY);
			return stat.getAvailableBlocks() * (long) stat.getBlockSize();
		} catch (Exception e) {
			Log.i(TAG, "Fail to access external storage", e);
		}
		return UNKNOWN_SIZE;
	}

//	public static String getSDPath() {
//		Dev_MountInfo dev = Dev_MountInfo.getInstance();
//		DevInfo info = dev.getInternalInfo();
//		try {
//			File internalfile = new File(info.getPath() + "/readme.txt");
//			if (internalfile.createNewFile()) {
//				internalfile.delete();
//				return info.getPath();
//			}
//			// info = dev.getExternalInfo();
//		} catch (Exception e) {
//			e.printStackTrace();
//			try {
//				File externalfile = new File(Environment
//						.getExternalStorageDirectory().getAbsolutePath()
//						+ "/readme.txt");
//				if (externalfile.createNewFile()) {
//					externalfile.delete();
//					return Environment.getExternalStorageDirectory().getAbsolutePath();
//				}
//			} catch (IOException e1) {
//				e1.printStackTrace();
//				return null;
//			}
//		}
//		return null;
//	}
	
	
	public static long getDirAvailableSpace(String path) {
		File dir;
		try {
			dir = new File(path);
			dir.mkdirs();
			if (!dir.isDirectory() || !dir.canWrite()) {
				return UNAVAILABLE;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			return UNAVAILABLE;
		}

		try {
			StatFs stat = new StatFs(path);
			return stat.getAvailableBlocks() * (long) stat.getBlockSize();
		} catch (Exception e) {
			Log.i(TAG, "Fail to access external storage", e);
		}
		return UNKNOWN_SIZE;
	}
	
	public static List<File> findFile(File path) {
		List<File> list = new ArrayList<File>();
        if(path.isDirectory()) {
        	File[] allFiles = path.listFiles();
        	if(allFiles != null) {
        		for(File tempFile: allFiles) {
        			if(tempFile.isDirectory()) {
//        				if(tempFile.getName().toLowerCase().lastIndexOf(".jpg") > -1) {
//        					list.add(tempFile);
//        				}
        				list.addAll(findFile(tempFile));
        			} else {
        				if((tempFile.getName().toLowerCase().lastIndexOf(".jpg") > -1) && (!tempFile.getName().toLowerCase().startsWith("ORI"))) {
        					list.add(tempFile);
        				}
        			}
        		}
        	}
        }
        return list;
	}
}

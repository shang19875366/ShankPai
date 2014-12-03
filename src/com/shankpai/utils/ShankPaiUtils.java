package com.shankpai.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

public class ShankPaiUtils {

	private static final String TAG = ShankPaiUtils.class.getSimpleName();
	public static final String REVIEW_ACTION = "com.shankpai.action.REVIEW";

	public static boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	public static Camera getCameraInstance() {
		Camera camera = null;
		try {
			camera = Camera.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return camera;
	}

	public static boolean checkFlashLight(Context context) {
		PackageManager pm = context.getPackageManager();
		FeatureInfo[] featureInfos = pm.getSystemAvailableFeatures();
		for (FeatureInfo fi : featureInfos) {
			if (!TextUtils.isEmpty(fi.name)
					&& fi.name.equals(PackageManager.FEATURE_CAMERA_FLASH)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是否为平板
	 * 
	 * @return
	 */
	public static boolean isPad(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static void viewUri(Uri uri, Context context, boolean isVideo) {
		if (!isUriValid(uri, context.getContentResolver())) {
			// Log.e(TAG, "Uri invalid. uri=" + uri);
			return;
		}
		try {
			if (isVideo) {
				Intent intent = new Intent("com.android.camera.action.REVIEW");
				intent.setData(uri);
				context.startActivity(intent);
			} else {
				context.startActivity(new Intent(REVIEW_ACTION));
			}
		} catch (ActivityNotFoundException ex) {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				context.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "review image fail. uri=" + uri, e);
			}
		}
	}

	public static boolean isUriValid(Uri uri, ContentResolver resolver) {
		if (uri == null)
			return false;

		try {
			ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
			if (pfd == null) {
				// Log.e(TAG, "Fail to open URI. URI=" + uri);
				return false;
			}
			pfd.close();
		} catch (IOException ex) {
			return false;
		}
		return true;
	}

	public static String[] getPathsArray() {
		ArrayList<String> files = findFile(new File(Environment
				.getExternalStorageDirectory().getAbsoluteFile()
				+ "/Shankpai/.TempData/"));
		Collections.sort(files);
		// System.out.println("files = " + files);
		// System.out.println("size = " + files.size());
		return files.toArray(new String[files.size()]);
	}

	public static ArrayList<String> getPathsList() {
		ArrayList<String> list = findFile(new File(Environment
				.getExternalStorageDirectory().getAbsoluteFile()
				+ "/Shankpai/.TempData/"));
		Collections.sort(list);
		return list;
	}

	private static ArrayList<String> findFile(File path) {
		ArrayList<String> list = new ArrayList<String>();
		if (path.isDirectory()) {
			File[] allFiles = path.listFiles();
			if (allFiles != null) {
				for (File tempFile : allFiles) {
					if (tempFile.isDirectory()) {
						// if(tempFile.getName().toLowerCase().lastIndexOf(".jpg")
						// > -1) {
						// list.add(tempFile);
						// }
						list.addAll(findFile(tempFile));
					} else {
						// System.out.println("tempFile name = " +
						// tempFile.getAbsolutePath());
						// System.out.println("index -1 = " +
						// (tempFile.getName().indexOf("ORI_") == -1));
						if ((tempFile.getName().toLowerCase()
								.lastIndexOf(".jpg") > -1)
								&& (tempFile.getName().indexOf("ORI_") == -1)
						/*
						 * && findDCIMFile( new File( Environment
						 * .getExternalStoragePublicDirectory(
						 * Environment.DIRECTORY_DCIM) .getAbsolutePath() +
						 * "/Camera")).contains( tempFile.getName())
						 */) {
							list.add(tempFile.getAbsolutePath());
						}
					}
				}
			}
		}
		return list;
	}

	private static ArrayList<String> findDCIMFile(File path) {
		ArrayList<String> list = new ArrayList<String>();
		if (path.isDirectory()) {
			File[] allFiles = path.listFiles();
			if (allFiles != null) {
				for (File tempFile : allFiles) {
					if (tempFile.isDirectory()) {
						// if(tempFile.getName().toLowerCase().lastIndexOf(".jpg")
						// > -1) {
						// list.add(tempFile);
						// }
						list.addAll(findFile(tempFile));
					} else {
						// System.out.println("tempFile name = " +
						// tempFile.getAbsolutePath());
						// System.out.println("index -1 = " +
						// (tempFile.getName().indexOf("ORI_") == -1));
						if ((tempFile.getName().toLowerCase()
								.lastIndexOf(".jpg") > -1)) {
							list.add(tempFile.getName());
						}
					}
				}
			}
		}
		return list;
	}

	/**
	 * 根据指定的图像路径和大小来获取缩略图 此方法有两点好处： 1.
	 * 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	 * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。 2.
	 * 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 用这个工具生成的图像不会被拉伸。
	 * 
	 * @param imagePath
	 *            图像的路径
	 * @param width
	 *            指定输出图像的宽度
	 * @param height
	 *            指定输出图像的高度
	 * @return 生成的缩略图
	 */
	public static Bitmap getImageThumbnail(String imagePath, int width,
			int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	// 矩阵相乘
	public static float[][] Mul(float[][] a, float[][] b) {
		// 确保矩阵a的列数和b的行数相等
		if (a[0].length != b.length) {
			return null;
		}
		// 用来存放结果的矩阵，axb的结果为a的行数和b的列数
		float[][] result = new float[a.length][b[0].length];
		// 对a的每行进行遍历
		for (int i = 0; i < a.length; i++) {
			// 对b的每列进行遍历
			for (int j = 0; j < b[0].length; j++) {
				// c为每一个点的值
				float c = 0;
				// 第i行j列的值为a的第i行上的n个数和b的第j列上的n个数对应相乘之和，其中n为a的列数，也是b的行数，a的列数和b的行数相等
				for (int k = 0; k < a[0].length; k++) {
					c += (a[i][k] * b[k][j]);
				}
				result[i][j] = c;
			}
		}
		return result;
	}
}

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
	 * �ж��Ƿ�Ϊƽ��
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
	 * ����ָ����ͼ��·���ʹ�С����ȡ����ͼ �˷���������ô��� 1.
	 * ʹ�ý�С���ڴ�ռ䣬��һ�λ�ȡ��bitmapʵ����Ϊnull��ֻ��Ϊ�˶�ȡ��Ⱥ͸߶ȣ�
	 * �ڶ��ζ�ȡ��bitmap�Ǹ��ݱ���ѹ������ͼ�񣬵����ζ�ȡ��bitmap����Ҫ������ͼ�� 2.
	 * ����ͼ����ԭͼ������û�����죬����ʹ����2.2�汾���¹���ThumbnailUtils��ʹ ������������ɵ�ͼ�񲻻ᱻ���졣
	 * 
	 * @param imagePath
	 *            ͼ���·��
	 * @param width
	 *            ָ�����ͼ��Ŀ��
	 * @param height
	 *            ָ�����ͼ��ĸ߶�
	 * @return ���ɵ�����ͼ
	 */
	public static Bitmap getImageThumbnail(String imagePath, int width,
			int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// ��ȡ���ͼƬ�Ŀ�͸ߣ�ע��˴���bitmapΪnull
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // ��Ϊ false
		// �������ű�
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
		// ���¶���ͼƬ����ȡ���ź��bitmap��ע�����Ҫ��options.inJustDecodeBounds ��Ϊ false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// ����ThumbnailUtils����������ͼ������Ҫָ��Ҫ�����ĸ�Bitmap����
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	// �������
	public static float[][] Mul(float[][] a, float[][] b) {
		// ȷ������a��������b���������
		if (a[0].length != b.length) {
			return null;
		}
		// ������Ž���ľ���axb�Ľ��Ϊa��������b������
		float[][] result = new float[a.length][b[0].length];
		// ��a��ÿ�н��б���
		for (int i = 0; i < a.length; i++) {
			// ��b��ÿ�н��б���
			for (int j = 0; j < b[0].length; j++) {
				// cΪÿһ�����ֵ
				float c = 0;
				// ��i��j�е�ֵΪa�ĵ�i���ϵ�n������b�ĵ�j���ϵ�n������Ӧ���֮�ͣ�����nΪa��������Ҳ��b��������a��������b���������
				for (int k = 0; k < a[0].length; k++) {
					c += (a[i][k] * b[k][j]);
				}
				result[i][j] = c;
			}
		}
		return result;
	}
}

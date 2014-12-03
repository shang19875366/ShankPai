package com.shankpai.gallery;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.os.Environment;

public class Images {

	public final static String[] imageUrls = new String[] {
			"http://img.my.csdn.net/uploads/201309/01/1378037235_3453.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037235_7476.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037235_9280.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037234_3539.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037234_6318.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037194_2965.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037193_1687.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037193_1286.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037192_8379.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037178_9374.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037177_1254.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037177_6203.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037152_6352.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037151_9565.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037151_7904.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037148_7104.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037129_8825.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037128_5291.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037128_3531.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037127_1085.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037095_7515.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037094_8001.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037093_7168.jpg",
			"http://img.my.csdn.net/uploads/201309/01/1378037091_4950.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949643_6410.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949642_6939.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949630_4505.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949630_4593.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949629_7309.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949629_8247.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949615_1986.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949614_8482.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949614_3743.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949614_4199.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949599_3416.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949599_5269.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949598_7858.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949598_9982.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949578_2770.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949578_8744.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949577_5210.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949577_1998.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949482_8813.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949481_6577.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949480_4490.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949455_6792.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949455_6345.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949442_4553.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949441_8987.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949441_5454.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949454_6367.jpg",
			"http://img.my.csdn.net/uploads/201308/31/1377949442_4562.jpg" };
	
	public static String[] getPathsArray() {
		ArrayList<String> files = findFile(new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Shankpai/.TempData/"));
		Collections.sort(files);
//		System.out.println("files = " + files);
//		System.out.println("size = " + files.size());
		return files.toArray(new String[files.size()]);
	}
	
	public static ArrayList<String> getPathsList() {
		ArrayList<String> list =  findFile(new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Shankpai/.TempData/"));
		Collections.sort(list);
		return list;
	}
	
	private static ArrayList<String> findFile(File path) {
		ArrayList<String> list = new ArrayList<String>();
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
//        				System.out.println("tempFile name = " + tempFile.getAbsolutePath());
//        				System.out.println("index -1 = " + (tempFile.getName().indexOf("ORI_") == -1));
						if ((tempFile.getName().toLowerCase()
								.lastIndexOf(".jpg") > -1)
								&& (tempFile.getName().indexOf("ORI_") == -1)
								/*&& findDCIMFile(
										new File(
												Environment
														.getExternalStoragePublicDirectory(
																Environment.DIRECTORY_DCIM)
														.getAbsolutePath()
														+ "/Camera")).contains(
										tempFile.getName())*/) {
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
//        				System.out.println("tempFile name = " + tempFile.getAbsolutePath());
//						System.out.println("index -1 = " + (tempFile.getName().indexOf("ORI_") == -1));
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
}

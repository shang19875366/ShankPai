package com.shankpai.service;

import java.io.File;
import java.util.ArrayList;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener2;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import com.shankpai.model.PhotoInfo;

public class SaverService extends Service {

	private GPUImage mGpuImage;
	private static final ArrayList<Thread> list = new ArrayList<Thread>();
	public static final int MSG_SAVE = 1; 
	public static final int MSG_DO = 2; 
//	private boolean flag = true;
//	private int index = 0;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	private Object obj = new Object();
	private static DCIMObserver dcimObserver;
//	private SaveThread thread;
	
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	class IncomingHandler extends Handler {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SAVE:
//				System.out.println("save --------------------------------");
				Bundle bundle = msg.getData();
				PhotoInfo info = new PhotoInfo();
				info.setTime(bundle.getString("time"));
				info.setPath(bundle.getString("path"));
				info.setPosition(bundle.getInt("position"));
				info.setDegree(bundle.getInt("degree"));
				synchronized (obj) {
					SaveThread thread = new SaveThread(info);
					list.add(thread);
//					index ++;
					if (list.size() == 1) {
						// doSave();
						list.get(0).run();
					}
				}
//				list.add(thread);
//				thread.start();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		dcimObserver = new DCIMObserver(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera");
		dcimObserver.startWatching();
		mHandler.sendEmptyMessageDelayed(0, 1000);
//		System.out.println("onCreate----------------------");
		mGpuImage = new GPUImage(this);
		mGpuImage.setFilters();
//		thread = new SaveThread();
//		thread.start();
//		synchronized (list) {
//			while(true) {
//				if(flag && !list.isEmpty()) {
//					flag = false;
//					doSave();
//				}
//			}
//		}
	}
	
	private Handler mHandler = new Handler() {
		
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if(dcimObserver != null) {
					dcimObserver = new DCIMObserver(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera");
					dcimObserver.startWatching();
				}
				mHandler.sendEmptyMessageDelayed(0, 1000);
				break;

			case MSG_DO:
				doSave((PhotoInfo) msg.obj);
				break;
				
			default:
				break;
			}
		};
	};
	
	private class SaveThread extends Thread {

		private PhotoInfo info;
		
		public SaveThread(PhotoInfo info) {
			this.info = info;
		}
		
		@Override
		public void run() {
			// if (list.size() > 0) {
			// PhotoInfo info = list.remove(0);
			Message msg = new Message();
			msg.what = MSG_DO;
			msg.obj = info;
			mHandler.sendMessage(msg);
			// }
		}
	}
	
	public void doSave(PhotoInfo holder) {
//		System.out.println("doSave -------------------");
//		if (list.size() <= 0) {
//			return;
//		}
		// if(list.size() == 1) {
		// synchronized (obj) {
//		PhotoInfo holder = list.get(0);
		int position = holder.getPosition();
		final String path = holder.getPath();
		String time = holder.getTime();
		int degree = holder.getDegree();
		mGpuImage.setFilter(mGpuImage.getFilters().get(position));
		mGpuImage.saveToPictures(path, time, "APP_" + time + ".jpg", degree,
				new OnPictureSavedListener2() {

					@Override
					public void onPictureSaved(Uri uri, String path1) {
						File file = new File(path);
						if (file.isFile() && file.exists()) {
							file.delete();
						}
						synchronized (obj) {
							list.remove(0);
							if(list.size() > 0) {
								list.get(0).run();
							}
						}
					}
				});
		// }
		// }
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	class DCIMObserver extends FileObserver {

		public DCIMObserver(String path) {
			super(path);
		}

		@Override
		public void onEvent(int event, String path) {
			switch (event) {
			case FileObserver.CREATE:
				break;
				
			case FileObserver.DELETE:
				File file = new File(Environment.getExternalStorageDirectory() + "/Shankpai/.TempData/" + path);
				if(file != null && file.exists()) {
					file.delete();
				}
				break;

			default:
				break;
			}
		}
	}
}

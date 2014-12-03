package com.shankpai;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener2;
import jp.co.cyberagent.android.gpuimage.GPUImageFilmFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSketchFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageTwoInputFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageVignetteFilter;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shankpai.service.SaverService;
import com.shankpai.utils.CameraHelper;
import com.shankpai.utils.CameraHelper.CameraInfo2;
import com.shankpai.utils.MediaManager;
import com.shankpai.utils.MediaManager.Media;
import com.shankpai.utils.RecognitionListener;
import com.shankpai.utils.ShankPaiUtils;
import com.shankpai.utils.SharedPreferencesHelper;
import com.shankpai.utils.Storage;
import com.shankpai.view.MyPopupWindow;
import com.shankpai.view.RotateImageView;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends Activity implements OnTouchListener, OnItemClickListener, AutoFocusCallback, PictureCallback, RecognitionListener {

	private final static int BACKCAMERA = Camera.CameraInfo.CAMERA_FACING_BACK;
	private final static int FRONTCAMERA = Camera.CameraInfo.CAMERA_FACING_FRONT;
	private final static String TAG = MainActivity.class.getSimpleName();
	private GLSurfaceView mGlSurfaceView;
//	private RelativeLayout mCameraButton;
	private GPUImage mGpuImage;
	private int mFilterPosition = 0;
	private GPUImageFilter mFilter;
	private CameraLoader mLoader;
	private CameraHelper mCameraHelper;
	private int mCameraNum = 0;
	private Parameters mParameters;
	private boolean mSupportAutoFocus = false;
	private String mFlashValue = Parameters.FLASH_MODE_OFF;
	private List<String> mSupportFlash;
	private boolean mFirstTimeInitialized;
	private MyPopupWindow mMyPopupWindow = null;
	private List<HashMap<String, Object>> mListitemFlash;
	private LinearLayout mLayoutTop;
	private RotateImageView mImgBtFlash;
	private RotateImageView mImgBtCameraSwap;
	private RotateImageView mImgBtThumbnail;
	private LinearLayout mInstroLayout;
	private RotateImageView imgbt_light;
	private RotateImageView mImgBtCapture; 
	private RelativeLayout mImgBtCaptureBackground;
	private ImageButton mImagePreviewButton;
	private boolean mCaptureProcessing = false;
	private boolean mCanTakePicture = false;
	private ListView mListViewFlash;
	private TextView mPopupTitle;
	private long mPicturesRemaining;
	private Timer timer;
	private static final int CANTAKEPIC = 1800;
	private Uri mCurrentImageUri;
	private Bitmap mCurrentThumbnailBm;
	private int mThumbnailOrientation = 0;
	private int mScreenWidth;
	private int mScreenHeight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MobclickAgent.setDebugMode(true);
		MobclickAgent.openActivityDurationTrack(false);
		MobclickAgent.updateOnlineConfig(this);
		initView();
		mGpuImage = new GPUImage(this);
		mCameraHelper = new CameraHelper(this);
		mLoader = new CameraLoader();
		mGpuImage.setGLSurfaceView(mGlSurfaceView);
		mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
		mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();
		mGpuImage.setScal(mScreenHeight);
		mGpuImage.setFilters();
		mFilterPosition = SharedPreferencesHelper.getFilter(this);
		mFilter = mGpuImage.getFilters().get(mFilterPosition);
		mGpuImage.setFilter(mFilter);
		if(mService == null) {
			bindService(new Intent(this, SaverService.class), conn, BIND_AUTO_CREATE);
		}
	}
	
	private void initView() {
		mGlSurfaceView = (GLSurfaceView) findViewById(R.id.cameraView);
//		mCameraButton = (RelativeLayout) findViewById(R.id.imb_bk);
		initData();
	}

	@Override
	protected void onResume() {
		mLoader.onResume();
		MobclickAgent.onPageStart("MainActivity");
		MobclickAgent.onResume(this);
		initializeFirstTime();
		updatePicture();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("MainActivity");
		MobclickAgent.onPause(this);
		mLoader.onPause();
	}
	
	private class CameraLoader {
		private int mCurrentCameraId = BACKCAMERA;
		private Camera mCameraInstance;

		public void onResume() {
			setUpCamera(mCurrentCameraId);
		}

		public void onPause() {
			releaseCamera();
		}

		public void switchCamera() {
//			releaseCamera();
			if (mCurrentCameraId == BACKCAMERA) {
				mCameraInstance.cancelAutoFocus();
			}
			if (mCameraInstance != null) {
				mCameraInstance.setPreviewCallback(null);
				mCameraInstance.stopPreview();
				mGpuImage.deleteImage();
				mCameraInstance.release();
				mCameraInstance = null;
			}
			mCurrentCameraId = (mCurrentCameraId + 1)
					% mCameraHelper.getNumberOfCameras();
			if (mCurrentCameraId == BACKCAMERA) {
//				setBackCameraUI();
			} else if (mCurrentCameraId == FRONTCAMERA) {
//				setFrontCameraUI();
//				mFocusView.clear();
			}
			setUpCamera(mCurrentCameraId);
		}

		private void setUpCamera(int id) {
			mCameraNum = Camera.getNumberOfCameras();
			if (mCameraNum == 1) {
				mCurrentCameraId = mCameraNum - 1;
			}
			mCameraInstance = getCameraInstance(mCurrentCameraId);
			if(mCameraInstance == null) {
				Toast.makeText(MainActivity.this, "Open Camera Failed.", Toast.LENGTH_SHORT).show();
				return;
			}
			mParameters = mCameraInstance.getParameters();
			// TODO adjust by getting supportedPreviewSizes and then choosing
			// the best one for screen size (best fill screen)
			// parameters.setPreviewSize(720, 480);
			// if (parameters.getSupportedFocusModes().contains(
			// Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			// parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			// }
//			mSupportFlash = mParameters.getSupportedFlashModes();
			if (mParameters.getSupportedFocusModes().contains(
					Camera.Parameters.FOCUS_MODE_AUTO)) {
				mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				mSupportAutoFocus = true;
			}
			mFlashValue = SharedPreferencesHelper
					.getPictureFlash(MainActivity.this);
			mSupportFlash = mParameters.getSupportedFlashModes();
			if (isSupported(mFlashValue, mSupportFlash)) {
//				Log.v(TAG, "mFlashValue = " + mFlashValue);
				mParameters.setFlashMode(mFlashValue);
			}
//			List<Size> sizes = mParameters.getSupportedPreviewSizes()
//			for(int i=0; i<sizes.size(); i++) {
//			}
			setParameters();

			int orientation = mCameraHelper.getCameraDisplayOrientation(
					MainActivity.this, mCurrentCameraId);
			CameraInfo2 cameraInfo = new CameraInfo2();
			mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
			boolean flipHorizontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT ? true
					: false;
			mGpuImage.setUpCamera(mCameraInstance, orientation, flipHorizontal,
					false);
		}

		public void setParameters() {
			if(mFilterPosition == 4) {
				if(mGpuImage.isBigPicture()) {
					Size pictureSize = getMaxSizeWithRatio(mLoader.mCameraInstance);
					mParameters.setPictureSize(pictureSize.width, pictureSize.height);
//					Size optimalSize = getOptimalPreviewSize(APCamera.this,
//							mParameters.getSupportedPreviewSizes(),
//							(double) pictureSize.width / pictureSize.height);
//					Size original = mParameters.getPreviewSize();
//			        if (!original.equals(optimalSize)) {
//			            mParameters.setPreviewSize(optimalSize.width, optimalSize.height);
//			        }
			        mParameters.setPreviewSize(640, 480);
				} else {
					mParameters.setPictureSize(640, 480);
					mParameters.setPreviewSize(640, 480);
				}
			} else {
				if(mGpuImage.isBigPicture()) {
//					Size pictureSize = mCameraInstance.new Size(1600, 1200);
//					Size pictureSize = getMaxSizeWithRatio(mLoader.mCameraInstance);
//					if(!mParameters.getSupportedPictureSizes().contains(pictureSize)) {
						Size pictureSize = mCameraInstance.new Size(1280, 960);
						if(!mParameters.getSupportedPictureSizes().contains(pictureSize)) {
							pictureSize = mCameraInstance.new Size(1024, 768);
							if(!mParameters.getSupportedPictureSizes().contains(pictureSize)) {
								pictureSize = mCameraInstance.new Size(960, 720);
								if(!mParameters.getSupportedPictureSizes().contains(pictureSize)) {
									pictureSize = mCameraInstance.new Size(640, 480);
								}
							}
						}
						mParameters.setPictureSize(pictureSize.width, pictureSize.height);
					}
////					Size optimalSize = getOptimalPreviewSize(APCamera.this,
////							mParameters.getSupportedPreviewSizes(),
////							(double) pictureSize.width / pictureSize.height);
////					Size original = mParameters.getPreviewSize();
////			        if (!original.equals(optimalSize)) {
////			            mParameters.setPreviewSize(optimalSize.width, optimalSize.height);
////			        }
////					mParameters.setPictureSize(1280, 720);
//			        mParameters.setPreviewSize(640, 480);
//				} else {
//					mParameters.setPictureSize(640, 480);
//					mParameters.setPreviewSize(640, 480);
//				}
			}
			mCameraInstance.setParameters(mParameters);
		}

		/** A safe way to get an instance of the Camera object. */
		private Camera getCameraInstance(final int id) {
			Camera c = null;
			try {
				c = mCameraHelper.openCamera(id);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return c;
		}

		private void releaseCamera() {
			if (mCameraInstance != null) {
				if (mCurrentCameraId == BACKCAMERA) {
					mCameraInstance.cancelAutoFocus();
				}
				mCameraInstance.setPreviewCallback(null);
				mCameraInstance.stopPreview();
//				if(mFilterPosition == 6) {
//					mGPUImage.deleteImage();
//				}
				mCameraInstance.release();
				mCameraInstance = null;
			}
		}
	}
	
	private static boolean isSupported(String value, List<String> supported) {
		return supported == null ? false : supported.indexOf(value) >= 0;
	}
	
	private Size getMaxSizeWithRatio(Camera camera) {
		Parameters mParameters = camera.getParameters();
		List<Size> mSupportPictureSize = mParameters.getSupportedPictureSizes();
		Size mPictureSize = null;
		for (int i = 0; i < mSupportPictureSize.size(); i++) {
			if ((double)mSupportPictureSize.get(i).width
					/ mSupportPictureSize.get(i).height == (double)(4.0 / 3.0)) {
				mPictureSize = mSupportPictureSize.get(i);
				break;
			}
		}
//		Log.v(TAG, "mSupportPicture size = " + mPictureSize.width);
		int max = 0;
		for (int i = 0; i < mSupportPictureSize.size(); i++) {
//			Log.v(TAG,
//					"mSupportPicture" + i + " = "
//							+ mSupportPictureSize.get(i).width + ", "
//							+ mSupportPictureSize.get(i).height);
			if (mSupportPictureSize.get(i).height > max
					&& ((double)mSupportPictureSize.get(i).width
							/ mSupportPictureSize.get(i).height == (4.0 / 3.0))) {
				max = mSupportPictureSize.get(i).height;
				mPictureSize = mSupportPictureSize.get(i);
			}
		}
		return mPictureSize;
	}
	
	private void initUI() {
		mLayoutTop.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		if (mFlashValue.equals(Parameters.FLASH_MODE_AUTO)) {
			mImgBtFlash.setImageResource(R.drawable.flash_auto);
		} else if (mFlashValue.equals(Parameters.FLASH_MODE_OFF)) {
			mImgBtFlash.setImageResource(R.drawable.flash_off);
		} else if (mFlashValue.equals(Parameters.FLASH_MODE_ON)) {
			mImgBtFlash.setImageResource(R.drawable.flash_on);
		}
		mImgBtFlash.setClickable(true);
		if (mCameraNum > 1) {
			mImgBtCameraSwap.setImageResource(R.drawable.capture_swap);
		} else {
			mImgBtCameraSwap
					.setImageResource(R.drawable.camera_mode_self_shot_forbid);
			mImgBtCameraSwap.setClickable(false);
		}
		if (!ShankPaiUtils.checkFlashLight(this) || ShankPaiUtils.isPad(this)) {
			mImgBtFlash.setEnabled(false);
			imgbt_light.setEnabled(false);
			mImgBtFlash
					.setImageResource(R.drawable.camera_mode_flash_off_forbid);
			imgbt_light.setImageResource(R.drawable.flashlight_disabled);
		} else {
			if (mLoader.mCurrentCameraId == FRONTCAMERA) {
				mImgBtFlash.setEnabled(false);
				mImgBtFlash
						.setImageResource(R.drawable.camera_mode_flash_off_forbid);
			} else {
				mImgBtFlash.setClickable(true);
			}
		}
		mImgBtCapture.setImageResource(R.drawable.camera_mode_shutter);
	}
	
	private void initData() {
//		mImgRecorder = (RotateImageView) this
//				.findViewById(R.id.iv_status_recorder);
//		mImgCapture = (RotateImageView) this
//				.findViewById(R.id.iv_status_capture);
		mInstroLayout = (LinearLayout) findViewById(R.id.instro);
		mLayoutTop = (LinearLayout) this.findViewById(R.id.ll_topbar);
		mGlSurfaceView = (GLSurfaceView) this.findViewById(R.id.cameraView);
		mImgBtFlash = (RotateImageView) this.findViewById(R.id.imgbt_flash);
		mImgBtThumbnail = (RotateImageView) this
				.findViewById(R.id.image_thumbnail);
		mImgBtCapture = (RotateImageView) this.findViewById(R.id.imb_start);
		mImgBtCaptureBackground = (RelativeLayout) findViewById(R.id.imb_bk);
		imgbt_light = (RotateImageView) this.findViewById(R.id.imgbt_light);
//		mBtSlip = (SlipButton) this.findViewById(R.id.sliperbtn);
//		mFocusView = (FocusView) this.findViewById(R.id.focusview);
		mImagePreviewButton = (ImageButton) findViewById(R.id.imgbt_preview);
		mImagePreviewButton.setOnClickListener(mListener);
//		mBtSlip.setStatus(true);
		mGlSurfaceView.setOnTouchListener(this);
//		mBtSlip.SetOnChangedListener(this);
		mImgBtFlash.setOnClickListener(mListener);
		mImgBtThumbnail.setOnClickListener(mListener);
		mImgBtCaptureBackground.setOnClickListener(mListener);
		imgbt_light.setOnClickListener(mListener);
		mImgBtCameraSwap = (RotateImageView) this
				.findViewById(R.id.imgbt_camera_swap);
		mImgBtCameraSwap.setOnClickListener(mListener);
//		filterbtn = (Button) findViewById(R.id.filterbtn);
//		filterbtn.setOnClickListener(mListener);
//		galleryFilter = (LinearLayout) findViewById(R.id.galleryFilter);
//		for(int i=0; i<images.length; i++){
//        	galleryFilter.addView(insertImage(i));
//        }
	}
	
	View.OnClickListener mListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			if (mCaptureProcessing) {
				return;
			}
			switch (view.getId()) {
			case R.id.imgbt_light:
				Intent flashlight_intent = new Intent(MainActivity.this,
						FlashLight.class);
				startActivity(flashlight_intent);
				MainActivity.this.finish();
				break;
			case R.id.imgbt_flash:
				View popupViewFlash = LayoutInflater.from(MainActivity.this)
						.inflate(R.layout.popupwindow, null);
				mListViewFlash = (ListView) popupViewFlash
						.findViewById(R.id.listview);
				mPopupTitle = (TextView) popupViewFlash
						.findViewById(R.id.tv_title);
				mPopupTitle.setText(R.string.flash_title);
				MyAdapter myAdapterFlash = new MyAdapter(MainActivity.this,
						mListitemFlash);
				mListViewFlash.setAdapter(myAdapterFlash);
				mListViewFlash.setOnItemClickListener(MainActivity.this);
				mMyPopupWindow.showPopupWindow(view, popupViewFlash);
				break;
			case R.id.imgbt_camera_swap:
				mLoader.switchCamera();
				break;
			case R.id.imb_bk:
				MobclickAgent.onEvent(MainActivity.this, "Cheese");
				if(mLoader.mCameraInstance == null) {
					return;
				}
				if(!mCaptureProcessing && timer!=null){
					timer.cancel();
				}
				if (mPicturesRemaining <= 0) {
					Toast.makeText(MainActivity.this,
							getString(R.string.no_storage), Toast.LENGTH_SHORT)
							.show();
					return;
				}
				mCaptureProcessing = true;
//				mBtSlip.setPictureProcessing(true);
				if (mLoader.mCurrentCameraId == BACKCAMERA) {
					autoFocus2Capture();
				} else {
					startCapture();
				}
				//1S���Զ�ֹͣ¼��
				timer = new Timer();
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						mHandler.obtainMessage(CANTAKEPIC).sendToTarget();
					}
				},2000);
				break;
			case R.id.image_thumbnail:
				ShankPaiUtils.viewUri(mCurrentImageUri, MainActivity.this, false);
				break;
			case R.id.imgbt_preview:
				if (!mGpuImage.isBigPicture()) {
					mGpuImage.setFilter(mFilter);
					mGpuImage.setBigPicture(!mGpuImage.isBigPicture());
					mImgBtCaptureBackground.setEnabled(true);
					mInstroLayout.setVisibility(View.GONE);
//					autoFocus();
				} else {
					mGpuImage.setFilters();
					mGpuImage.setBigPicture(!mGpuImage.isBigPicture());
					mImgBtCaptureBackground.setEnabled(false);
					mHandler.sendEmptyMessageDelayed(3, 300);
//					mInstroLayout.setVisibility(View.VISIBLE);
				}
				break;

//			case R.id.filterbtn:
//				if(!isopen){
////					galleryFilter.setVisibility(View.VISIBLE);
//					filterbtn.setBackgroundResource(R.drawable.pretty_down);
//					galleryFilter.setVisibility(View.VISIBLE);
////					galleryFilterName.setVisibility(View.VISIBLE);
////					filters_layout.addView(galleryFilter);
//					isopen = true;
//				}else{
////					galleryFilter.setVisibility(View.INVISIBLE);
//					filterbtn.setBackgroundResource(R.drawable.pretty_up);
//					galleryFilter.setVisibility(View.GONE);
////					galleryFilterName.setVisibility(View.GONE);
////					filters_layout.removeView(galleryFilter);
//					isopen = false;
//				}
//				break;

			default:
				break;
			}
		}
	};
	
	private void initializeFirstTime() {
		if (mFirstTimeInitialized) {
			return;
		}
		initPopupWindow();
		initUI();
		checkStorage();
		Media media = null;
		try {
			media = MediaManager.getLastImageMedia(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (media != null) {
			mCurrentThumbnailBm = media.getBitmap();
			int orientation = media.getOrientation();
			mCurrentImageUri = media.getUri();
			if (mCurrentThumbnailBm != null) {
				updateThumbnail(orientation);
				mImgBtThumbnail.setOrientation(orientation);
			}
		} else {
			mImgBtThumbnail.setImageBitmap(null);
		}
//		mOrientationEventListener = new OrientationEventListener(this) {
//   
//			@Override
//			public void onOrientationChanged(int orientation) {
//				if (orientation == ORIENTATION_UNKNOWN) {
//					return;
//				}
//				Message msg = new Message();
//				msg.what = 2;
//				msg.obj = orientation;
//				mHandler.sendMessage(msg);
//			}
//		};
//		if (mOrientationEventListener.canDetectOrientation()) {
//			mOrientationEventListener.enable();
//		} else {
//			mOrientationEventListener.disable();
//		}
		mFirstTimeInitialized = true;
	}
	
	private void updatePicture() {
		if (!mGpuImage.isBigPicture()) {
			mImgBtCaptureBackground.setEnabled(false);
			mInstroLayout.setVisibility(View.VISIBLE);
		} else {
			mImgBtCaptureBackground.setEnabled(true);
			mInstroLayout.setVisibility(View.GONE);
		}
	}
	
	/**
	 * this must call after startPreview()
	 */
	private void initPopupWindow() {
		mMyPopupWindow = new MyPopupWindow();
		mListitemFlash = initFlashResource();
	}
	
	private List<HashMap<String, Object>> initFlashResource() {
		String[] title = { "Off", "On", "Auto flash" };
		Integer[] image = { R.drawable.camera_mode_flash_off_nor,
				R.drawable.camera_mode_flash_on_nor,
				R.drawable.camera_mode_flash_auto_nor };
		Boolean[] status = { false, false, false };
		if (mFlashValue.equals(Parameters.FLASH_MODE_OFF)) {
			status[0] = true;
		}
		if (mFlashValue.equals(Parameters.FLASH_MODE_ON)) {
			status[1] = true;
		}
		if (mFlashValue.equals(Parameters.FLASH_MODE_AUTO)) {
			status[2] = true;
		}
		final int size = title.length;
		List<HashMap<String, Object>> listItems = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < size; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("image", image[i]);
			map.put("title", title[i]);
			map.put("status", status[i]);
			listItems.add(map);
		}
		return listItems;
	}

	private void checkStorage() {
		mPicturesRemaining = Storage.getAvailableSpace();
		if (mPicturesRemaining > Storage.LOW_STORAGE_THRESHOLD) {
			mPicturesRemaining = (mPicturesRemaining - Storage.LOW_STORAGE_THRESHOLD)
					/ Storage.PICTURE_SIZE;
		} else if (mPicturesRemaining > 0) {
			mPicturesRemaining = 0;
		}
		String noStorageText = null;
		if (mPicturesRemaining == Storage.UNAVAILABLE) {
			noStorageText = getString(R.string.no_storage);
		} else if (mPicturesRemaining == Storage.PREPARING) {
			noStorageText = getString(R.string.preparing_sd);
		} else if (mPicturesRemaining == Storage.UNKNOWN_SIZE) {
			noStorageText = getString(R.string.access_sd_fail);
		} else if (mPicturesRemaining < 1L) {
			noStorageText = getString(R.string.not_enough_space);
		}
		if (noStorageText != null) {
			Toast.makeText(this, noStorageText, Toast.LENGTH_LONG).show();
		}
	}
	
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
//				if (mFocusView != null
//						&& mFocusView.getVisibility() == View.VISIBLE) {
//					mFocusView.clear();
//				}
				break;

			case 2:
				int orientation = (Integer) msg.obj;
				if (orientation == -1) {
					// �ֻ�ƽ��
				} else if (orientation < 30 || orientation > 330) {
					// �ֻ�����
					rotate(0);
				} else if (orientation < 330 && orientation > 230) {
					// �ֻ��ұ�����
					rotate(270);
				} else if (orientation < 230 && orientation > 130) {
					// �ֻ�ײ�����
					rotate(180);
				} else if (orientation < 130 && orientation > 30) {
					// �ֻ��������
					rotate(90);
				}
				break;

			case 3:
				mInstroLayout.setVisibility(View.VISIBLE);
				break;
				
			case CANTAKEPIC:
				mCaptureProcessing = false;
//				mBtSlip.setPictureProcessing(false);
				mImgBtCaptureBackground.setEnabled(true);
				break;
				
			case 4:
				if(!mCaptureProcessing) {
					capture();
				}
				break;
				
			default:
				break;
			}
		};
	};
	
	private void rotate(float degree) {
		mThumbnailOrientation = (int) degree;
		imgbt_light.setOrientation((int) degree);
		mImgBtFlash.setOrientation((int) degree);
		mImgBtCameraSwap.setOrientation((int) degree);
		mImgBtThumbnail.setOrientation((int) degree);
		mImgBtCapture.setOrientation((int) degree);
//		mImgCapture.setOrientation((int) degree);
//		mImgRecorder.setOrientation((int) degree);
	}
	
	private void autoFocus2Capture() {
		if (mLoader.mCameraInstance == null) {
			mCaptureProcessing = false;
//			mBtSlip.setPictureProcessing(false);
			return;
		}
		mCanTakePicture = true;
//		if (isSupported(mFlashValue, mSupportFlash)) {
//			Log.v(TAG, "mFlashValue = " + mFlashValue); 
//			mParameters.setFlashMode(mFlashValue);
//		}
//		mLoader.mCameraInstance.setParameters(mParameters);
		autoFocus();
	}

	private void startCapture() {
		mCanTakePicture = false;
//		mLoader.mCameraInstance.setParameters(mParameters);
//		mPictureStartTime = System.currentTimeMillis();
		if(mLoader.mCameraInstance == null) {
			mCaptureProcessing = false;
//			mBtSlip.setPictureProcessing(false);
			return;
		}
//		if(mFilterPosition == 4) {
//			int degree = updateDegree();
//			if(mLoader.mCurrentCameraId == BACKCAMERA) {
//				mParameters.setRotation(degree);
//			} else if(mLoader.mCurrentCameraId == FRONTCAMERA) {
//				mParameters.setRotation(degree);
//			}
//		}
		mLoader.setParameters();
		if(mLoader.mCurrentCameraId == FRONTCAMERA && ("SAMSUNG-SGH-I747".equals(android.os.Build.MODEL))) {
			mLoader.mCameraInstance.setPreviewCallback(null);
		}
		mImgBtCaptureBackground.setEnabled(false);
		try {
			mLoader.mCameraInstance.takePicture(null, null, null,
					this);
		} catch (Exception e) {
			//
		}
	}
	
	private Messenger mService = null;
	private boolean mBound;
	
	private ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			mBound = false;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = new Messenger(service);
			mBound = true;
		}
	};
	
	@Override
	public void onPictureTaken(byte[] data, final Camera camera) {
//		long pictureTime = System.currentTimeMillis() - mPictureStartTime;
//		Log.v(TAG, "pictureTime = " + pictureTime);

		final File pictureFile = getOutputMediaFile();
		if (pictureFile == null) {
//			Log.d("ASDF", "Error creating media file, check storage permissions");
			return;
		}
		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		}
		data = null;
		// Bitmap bitmap =
		// BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
		int degree = updateDegree();
//		System.out.println("degree = " + degree);
//		if (mFilterPosition == 4) {
//			if(mProgressDialog != null && !mProgressDialog.isShowing()) {
////				mProgressDialog.show();
//			}
//			savePicture(data, degree);
//			data = null;
//			return;
//		}
		// mLoader.mCameraInstance.getParameters().getPictureSize().width +
		// ", height === " +
		// mLoader.mCameraInstance.getParameters().getPictureSize().height);
//		if(mProgressDialog != null && !mProgressDialog.isShowing()) {
//			mProgressDialog.show();
//		}
//		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//		String fileName = "APP_" + getFormatTime() + ".jpg";
//		File file = new File(path.getAbsoluteFile() + "/Camera/" + fileName);
//		Size size = mParameters.getPictureSize();
//		FileOutputStream fos = new FileOutputStream(file);
//		Options opts = new Options();
//		opts.inSampleSize= 6;
//		Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), opts);
//		System.out.println("width = " + bitmap.getWidth());
//		System.out.println("height = " + bitmap.getHeight());
//		data = null;
//		Matrix m = new Matrix();
//		m.postRotate(degree);
//		m.postScale((float)640/bitmap.getWidth(), (float)480/bitmap.getHeight());
//		Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
//				bitmap.getHeight(), m, true);
		// mGPUImage.setImage(bitmap);
		mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		String time = getFormatTime();
//		final Intent intent = new Intent(this, SaverService.class);
//		intent.putExtra("path", pictureFile.getAbsolutePath());
//		intent.putExtra("time", time);
//		intent.putExtra("position", mFilterPosition);
//		intent.putExtra("degree", degree);
//		PhotoInfo obj = new PhotoInfo();
//		obj.setTime(time);
//		obj.setPath(pictureFile.getAbsolutePath());
//		obj.setPosition(mFilterPosition);
//		obj.setDegree(degree);
		final Message msg = new Message();
		msg.what = SaverService.MSG_SAVE;
		Bundle bundle = new Bundle();
		bundle.putString("time", time);
		bundle.putString("path", pictureFile.getAbsolutePath());
		bundle.putInt("position", mFilterPosition);
		bundle.putInt("degree", degree);
		msg.setData(bundle);
//		System.out.println("mfilterposition = " + mFilterPosition);
		mGpuImage.saveToPictures(pictureFile.getAbsolutePath(), degree, time, "APP_" + time + ".jpg",
				new OnPictureSavedListener2() {

					@Override
					public void onPictureSaved(Uri uri, String path) {
//						Bitmap result;
						try {
							// pictureFile.delete();
							mCurrentImageUri = /*Uri.fromFile(new File(path))*/uri;
							// new UpdateThumbnailAsynTask().execute();
//							result = android.provider.MediaStore.Images.Media
//									.getBitmap(getContentResolver(), uri);
							mCurrentThumbnailBm = ShankPaiUtils
									.getImageThumbnail(path, 96, 96);
							// result.recycle();
							updateThumbnail(0);
//							if (mLoader.mCurrentCameraId == BACKCAMERA) {
//								camera.startPreview();
//							} else { 
//							}
//							mImgBtThumbnail.setOrientation(mThumbnailOrientation);
//						} catch (FileNotFoundException e) {
//							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (mLoader.mCurrentCameraId == FRONTCAMERA) {
							mLoader.releaseCamera();
							mLoader.onResume();
						} else {
							camera.startPreview();
						}
						mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
						if(timer!=null){
							timer.cancel();
						}
						mCaptureProcessing = false;
//						mBtSlip.setPictureProcessing(false);
//						TipHelper.Vibrate(APCamera.this, 500);
//						startService(intent);
						try {
							if(mService != null) {
								mService.send(msg);
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						mImgBtCaptureBackground.setEnabled(true);
//						if(mProgressDialog != null && mProgressDialog.isShowing()) {
//							mProgressDialog.dismiss();
//						}
					}
				});
//		mLoader.releaseCamera();
//		mLoader.onResume();
	}
	
	private String getFormatTime() {
		Time t = new Time();
		t.setToNow();
		String year = t.year + "";
		String month = "";
		int monthValue = t.month + 1;
		if (monthValue < 10) {
			month = "0" + monthValue;
		} else {
			month = monthValue + "";
		}
		int dateValue = t.monthDay;
		String date = "";
		if (dateValue < 10) {
			date = "0" + dateValue;
		} else {
			date = dateValue + "";
		}
		int hourValue = t.hour;
		String hour = "";
		if (hourValue < 10) {
			hour = "0" + hourValue;
		} else {
			hour = hourValue + "";
		}
		int minuteValue = t.minute;
		String minute = "";
		if (minuteValue < 10) {
			minute = "0" + minuteValue;
		} else {
			minute = minuteValue + "";
		}
		int secondValue = t.second;
		String second = "";
		if (secondValue < 10) {
			second = "0" + secondValue;
		} else {
			second = secondValue + "";
		}
		String time = year + month + date + "_" + hour + minute + second;
		return time;
	}
	
	private void updateThumbnail(int orientation) {
		if (mCurrentThumbnailBm == null) {
			return;
		}
		if (orientation != 0) {
			Matrix m = new Matrix();
			m.setRotate(orientation, mCurrentThumbnailBm.getWidth() * 0.5f,
					mCurrentThumbnailBm.getHeight() * 0.5f);
			try {
				mCurrentThumbnailBm = Bitmap.createBitmap(mCurrentThumbnailBm,
						0, 0, mCurrentThumbnailBm.getWidth(),
						mCurrentThumbnailBm.getHeight(), m, true);
				mImgBtThumbnail.setImageBitmap(mCurrentThumbnailBm);
			} catch (Throwable t) {
				Log.w(TAG, "Failed to rotate thumbnail", t);
			}
		} else {
			mImgBtThumbnail.setImageBitmap(mCurrentThumbnailBm);
		}
	}
	
	private void capture() {
		if(!mCaptureProcessing&&timer!=null){
			timer.cancel();
		}
		if (mCaptureProcessing || mLoader.mCameraInstance == null) {
			return;
		}
		if (mPicturesRemaining <= 0) {
			Toast.makeText(MainActivity.this, getString(R.string.no_storage),
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(!mImgBtCaptureBackground.isEnabled()) {
			return;
		}
		mCaptureProcessing = true;
//		mBtSlip.setPictureProcessing(true);
		if (mLoader.mCurrentCameraId == BACKCAMERA) {
			autoFocus2Capture();
		} else {
			startCapture();
		}
		//1S���Զ�ֹͣ¼��
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				mHandler.obtainMessage(CANTAKEPIC).sendToTarget();
			}
		},2000);
	}
	
	private void autoFocus() {
		if (mLoader.mCameraInstance == null) {
			mCaptureProcessing = false;
//			mBtSlip.setPictureProcessing(false);
			return;
		}
//		if (isSupported(mFlashValue, mSupportFlash)) {
//
//			mParameters.setFlashMode(mFlashValue);
//		}
//		mLoader.mCameraInstance.setParameters(mParameters);
		if (mSupportAutoFocus && mLoader.mCurrentCameraId == BACKCAMERA && !"MI 2SC".equals(android.os.Build.MODEL)) {
//			mFocusView.showStart();
			mLoader.mCameraInstance.autoFocus(this);
			mHandler.sendEmptyMessageDelayed(1, 2000);
		} else {
			if (mCanTakePicture) {
				startCapture();
			}
		}
	}
	
	private File getOutputMediaFile() {
		File nomediaFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/ShankPai/.nomedia");
		if(!nomediaFile.exists()) {
			try {
				nomediaFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        File cameraPath = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/ShankPai/.TempData/");
		String formatTime = getFormatTime();
		String ImageTitle = "ORI_" + formatTime;
		String imagePath = cameraPath.getAbsolutePath() + "/" + ImageTitle + ".jpg";
		if (!cameraPath.exists()) {
			if (!cameraPath.mkdirs()) {
//				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}
		return new File(imagePath);
	}
	
	private int updateDegree() {
		int degree = 90;
		System.out.println("mThumbnailOrientation = " + mThumbnailOrientation);
		if (mThumbnailOrientation == -1) {
			// �ֻ�ƽ��
			degree = 90;
		} else if (mThumbnailOrientation < 30 || mThumbnailOrientation > 330) {
			// �ֻ�����
			if (mLoader.mCurrentCameraId == BACKCAMERA) {
				degree = 90;
			} else if (mLoader.mCurrentCameraId == FRONTCAMERA) {
				degree = 270;
			}
		} else if (mThumbnailOrientation < 330 && mThumbnailOrientation > 230) {
			// �ֻ��ұ�����
			degree = 0;
		} else if (mThumbnailOrientation < 230 && mThumbnailOrientation > 130) {
			// �ֻ�ײ�����
			if (mLoader.mCurrentCameraId == BACKCAMERA) {
				degree = 270;
			} else if (mLoader.mCurrentCameraId == FRONTCAMERA) {
				degree = 90;
			}
		} else if (mThumbnailOrientation < 130 && mThumbnailOrientation > 30) {
			// �ֻ��������
			degree = 180; 
		}
		return degree;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int type = event.getAction();
		if(type==MotionEvent.ACTION_DOWN){
			mInstroLayout.setVisibility(View.GONE);
//			if (!mGPUImage.isBigPicture()&&"SAMSUNG-SGH-I747".equals(android.os.Build.MODEL)) {
//				mCameraView.startAnimation(animation);
//			}
		}
		
		if(type==MotionEvent.ACTION_UP){
			float x = event.getX();
			float y = event.getY();
			if (!mGpuImage.isBigPicture()) {
				if (x < ((mScreenWidth / 3) - (mScreenWidth / 50))
						&& y < ((mScreenHeight / 3) + (mScreenHeight / 50))
						&& y > 80) {
					//����
					mFilter = new GPUImageGammaFilter(0.5f);
					mFilterPosition = 0;
					mGpuImage.setFilter(mFilter);
//				mGPUImage.setBigPicture(!mGPUImage.isBigPicture());
				} else if (x >= ((mScreenWidth / 3) + (mScreenWidth / 50))
						&& x <= ((mScreenWidth * 2 / 3) - (mScreenWidth / 50))
						&& y < ((mScreenHeight / 3) + (mScreenHeight / 50))
						&& y > 80) {
					//Film
					mFilter = new GPUImageFilmFilter(0.3f, 0.75f);
					mFilterPosition = 1;
					mGpuImage.setFilter(mFilter);
//				mGPUImage.setBigPicture(!mGPUImage.isBigPicture());
				} else if (x > ((mScreenWidth * 2 / 3) + (mScreenWidth / 50))
						&& x < mScreenWidth
						&& y < ((mScreenHeight / 3) + (mScreenHeight / 50))
						&& y > 80) {
					//LOMO
					PointF centerPoint = new PointF();
					centerPoint.x = 0.5f;
					centerPoint.y = 0.5f;
					mFilter = new GPUImageVignetteFilter(centerPoint, new float[] {0.0f, 0.0f, 0.0f}, 0.0f, 0.65f);
					mFilterPosition = 2;
					mGpuImage.setFilter(mFilter);
//				mGPUImage.setBigPicture(!mGPUImage.isBigPicture());
				} else if (x < ((mScreenWidth / 3) - (mScreenWidth / 50))
						&& y >= ((mScreenHeight / 3) - (mScreenHeight / 50))
						&& y <= ((mScreenHeight * 2 / 3) + (mScreenHeight / 50))) {
					//�ڰ�
					mFilter = new GPUImageGrayscaleFilter();
					mFilterPosition = 3;
					mGpuImage.setFilter(mFilter);
//				mGPUImage.setBigPicture(!mGPUImage.isBigPicture());
				} else if (x >= ((mScreenWidth / 3) + (mScreenWidth / 50))
						&& x <= ((mScreenWidth * 2 / 3) - (mScreenWidth / 50))
						&& y >= ((mScreenHeight / 3) - (mScreenHeight / 50))
						&& y <= ((mScreenHeight * 2 / 3) + (mScreenHeight / 50))) {
					//ԭͼ
					mFilter = new GPUImageFilter();
					mFilterPosition = 4;
					mGpuImage.setFilter(mFilter);
//				mGPUImage.setBigPicture(!mGPUImage.isBigPicture());
				} else if (x > ((mScreenWidth * 2 / 3) + (mScreenWidth / 50))
						&& x < mScreenWidth
						&& y >= ((mScreenHeight / 3) - (mScreenHeight / 50))
						&& y <= ((mScreenHeight * 2 / 3) + (mScreenHeight / 50))) {
					//����
					mFilter = new GPUImageSketchFilter();
					mFilterPosition = 5;
					mGpuImage.setFilter(mFilter);
//				mGPUImage.setBigPicture(!mGPUImage.isBigPicture());
				} else if (x < ((mScreenWidth / 3) - (mScreenWidth / 50))
						&& y > ((mScreenHeight * 2 / 3) - (mScreenHeight / 50))
						&& y < (mScreenHeight - 80)) {
					//��ӡ
					mFilter = new GPUImageTwoInputFilter(this);
					mFilterPosition = 6;
//					((GPUImageTwoInputFilter) mFilter).setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.lookup_amatorka));
					mGpuImage.setFilter(mFilter);
//				mGPUImage.setBigPicture(!mGPUImage.isBigPicture());
				} else if (x >= ((mScreenWidth / 3) + (mScreenWidth / 50))
						&& x <= ((mScreenWidth * 2 / 3) - (mScreenWidth / 50))
						&& y > ((mScreenHeight * 2 / 3) - (mScreenHeight / 50))
						&& y < (mScreenHeight - 80)) {
					//����
					float[] f = new float[] {
							0.3588f, 0.7044f, 0.1368f, 0.0f,
							0.2990f, 0.5870f, 0.1140f, 0.0f,
							0.2392f, 0.4696f, 0.0912f, 0.0f,
							0f, 0f, 0f, 1.0f
					};
					mFilter = new GPUImageSepiaFilter(f);
					mFilterPosition = 7;
					mGpuImage.setFilter(mFilter);
//				mGPUImage.setBigPicture(!mGPUImage.isBigPicture());
				} else if (x > ((mScreenWidth * 2 / 3) + (mScreenWidth / 50))
						&& x < mScreenWidth
						&& y > ((mScreenHeight * 2 / 3) - (mScreenHeight / 50))
						&& y < (mScreenHeight - 80)) {
					//����
					mFilter = new GPUImageSepiaFilter();
					mFilterPosition = 8;
					mGpuImage.setFilter(mFilter);
//				mGPUImage.setBigPicture(!mGPUImage.isBigPicture());
				}
//				if(mFilterPosition == 4) {
//					Size pictureSize = getMaxSize(mLoader.mCameraInstance);
//					mParameters.setPictureSize(pictureSize.width, pictureSize.height);
//				} else {
//					mParameters.setPictureSize(640, 480);
//				}
//				mLoader.mCameraInstance.setParameters(mParameters);
				mGpuImage.setBigPicture(!mGpuImage.isBigPicture());
//				if("SAMSUNG-SGH-I747".equals(android.os.Build.MODEL)){
//					mLoader.releaseCamera();
//					mLoader.onResume();
//				}
			}
			SharedPreferencesHelper.setFilter(this, mFilterPosition);
			if (mGpuImage.isBigPicture()) {
				mImgBtCaptureBackground.setEnabled(true);
				if (mCameraNum > 1) {
					mImgBtCameraSwap.setEnabled(true);
					mImgBtCameraSwap.setImageResource(R.drawable.capture_swap);
				} else {
					mImgBtCameraSwap.setEnabled(false);
					mImgBtCameraSwap.setImageResource(R.drawable.camera_mode_self_shot_forbid);
				}
//				mInstroLayout.setVisibility(View.GONE);
			}
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if ((ListView) parent == mListViewFlash) {
			mListitemFlash.get(position).put("status", true);
			for (int i = 0; i < mListitemFlash.size(); i++) {
				if (position != i) {
					mListitemFlash.get(i).put("status", false);
				}
			}
			setFlashPrameter(position);
			mMyPopupWindow.dismissWindow();
		}
	}

	private void setFlashPrameter(int value) {
		if (mLoader.mCameraInstance == null) {
			return;
		}
		if (value == 0) {
			mImgBtFlash.setImageResource(R.drawable.camera_mode_flash_off_nor);
			mFlashValue = Parameters.FLASH_MODE_OFF;
		} else if (value == 1) {
			mImgBtFlash.setImageResource(R.drawable.camera_mode_flash_on_nor);
			mFlashValue = Parameters.FLASH_MODE_ON;
		} else if (value == 2) {
			mImgBtFlash.setImageResource(R.drawable.camera_mode_flash_auto_nor);
			mFlashValue = Parameters.FLASH_MODE_AUTO;
		}

		List<String> supportFlashMode = mParameters.getSupportedFlashModes();
		if (isSupported(mFlashValue, supportFlashMode)) {
			SharedPreferencesHelper.setPictureFlash(this, mFlashValue);
//			Log.v(TAG, "mFlashValue = " + mFlashValue);
			mParameters.setFlashMode(mFlashValue);
			mLoader.mCameraInstance.setParameters(mParameters);
		}
	}
	
	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if (mCanTakePicture) {
			startCapture();
		}
	}

	@Override
	public void onPartialResults(Bundle b) {
		
	}

	@Override
	public void onResults(Bundle b) {
		boolean capture = b.getBoolean("capture");
		if(capture && !mCaptureProcessing) {
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					capture();
				}
			});
		}
	}

	@Override
	public void onError(int err) {
		
	}
}

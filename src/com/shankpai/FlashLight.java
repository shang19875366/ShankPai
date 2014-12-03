package com.shankpai;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.shankpai.model.FlashlightSurface;
import com.umeng.analytics.MobclickAgent;

/**
 * Flash Light UI
 * 
 * @author Alex
 * 
 */
public class FlashLight extends Activity implements OnClickListener {
	private PowerLED powerLED;
	private Button powerbtn;
	private RelativeLayout layout;
	private boolean ispowered = true;
	private FlashlightSurface mSurface; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flashlight);
		MobclickAgent.setDebugMode(true);
		MobclickAgent.openActivityDurationTrack(false);
		MobclickAgent.updateOnlineConfig(this);
		layout = (RelativeLayout) findViewById(R.id.layout);
		powerbtn = (Button) findViewById(R.id.powerbtn);
		powerbtn.setOnClickListener(this);
		mSurface = (FlashlightSurface) findViewById(R.id.surfaceview);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		powerLED.Destroy();
		mSurface.destory();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("FlashLight");
		MobclickAgent.onPause(this);
//		powerLED.Destroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("FlashLight");
		MobclickAgent.onResume(this);
//		powerLED = new PowerLED();
//		ispowered = false;
//		powerbtn.setBackgroundResource(R.drawable.flashlight_button_off);
//		layout.setBackgroundResource(R.drawable.flashlight_off);
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mSurface.setFlashlightSwitch(true);  
//				powerLED.turnOn();
				ispowered = true;
			}
		}, 800);
		powerbtn.setBackgroundResource(R.drawable.flashlight_button_on);
		layout.setBackgroundResource(R.drawable.flashlight_on);
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.powerbtn:
			MobclickAgent.onEvent(FlashLight.this, "FlashLight open");
			if (!ispowered) {
				mSurface.setFlashlightSwitch(true);  
//				powerLED.turnOn();
				ispowered = true;
				powerbtn.setBackgroundResource(R.drawable.flashlight_button_on);
				layout.setBackgroundResource(R.drawable.flashlight_on);
			} else {
				mSurface.setFlashlightSwitch(false);  
//				powerLED.turnOff();
				ispowered = false;
				powerbtn.setBackgroundResource(R.drawable.flashlight_button_off);
				layout.setBackgroundResource(R.drawable.flashlight_off);
			}
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_HEADSETHOOK:
			if (!ispowered) {
//				powerLED.turnOn();
				mSurface.setFlashlightSwitch(true); 
				ispowered = true;
				powerbtn.setBackgroundResource(R.drawable.flashlight_button_on);
				layout.setBackgroundResource(R.drawable.flashlight_on);
			} else {
//				powerLED.turnOff();
				mSurface.setFlashlightSwitch(false);
				ispowered = false;
				powerbtn.setBackgroundResource(R.drawable.flashlight_button_off);
				layout.setBackgroundResource(R.drawable.flashlight_off);
			}
			break;
		case KeyEvent.KEYCODE_BACK:
//			powerLED.Destroy();
			mSurface.destory();
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
			break;
		default:
			break;
		}
		return false;
	}
}

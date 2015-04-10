package com.sensetoolbox.six.utils;

import com.sensetoolbox.six.mods.XMain;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class ShakeManager implements SensorEventListener {
	
	private float xAccel;
	private float yAccel;
	private float zAccel;

	private float xPreviousAccel;
	private float yPreviousAccel;
	private float zPreviousAccel;

	private boolean firstUpdate = true;

	private final float shakeThresholdX = 7.5f;
	private final float shakeThresholdY = 7.5f;
	private final float shakeThresholdZ = 10f;
	
	private boolean shakeInitiated = false;
	
	private final int shakeEventThrottle = 750;
	private long lastShakeEvent = System.currentTimeMillis();
	
	private Context helperContext;
	
	public ShakeManager(Context helpercontext) {
		this.helperContext = helpercontext;
	}
	
	public void reset() {
		xAccel = 0;
		yAccel = 0;
		zAccel = 0;
		xPreviousAccel = 0;
		yPreviousAccel = 0;
		zPreviousAccel = 0;
		firstUpdate = true;
		shakeInitiated = false;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//Don't care...
	}

	@Override
	public void onSensorChanged(SensorEvent se) {
		updateAccelParameters(se.values[0], se.values[1], se.values[2]);
		if ((!shakeInitiated) && isAccelerationChanged())
			shakeInitiated = true;
		else if ((shakeInitiated) && isAccelerationChanged())
			executeShakeActionDelayed();
		else if ((shakeInitiated) && (!isAccelerationChanged()))
			shakeInitiated = false;
	}

	private void updateAccelParameters(float xNewAccel, float yNewAccel, float zNewAccel) {
		if (firstUpdate) {
			xPreviousAccel = xNewAccel;
			yPreviousAccel = yNewAccel;
			zPreviousAccel = zNewAccel;
			firstUpdate = false;
		} else {
			xPreviousAccel = xAccel;
			yPreviousAccel = yAccel;
			zPreviousAccel = zAccel;
		}
		xAccel = xNewAccel;
		yAccel = yNewAccel;
		zAccel = zNewAccel;
	}
	
	private boolean isAccelerationChanged() {
		float deltaX = Math.abs(xPreviousAccel - xAccel);
		float deltaY = Math.abs(yPreviousAccel - yAccel);
		float deltaZ = Math.abs(zPreviousAccel - zAccel);
		return (deltaX > shakeThresholdX && deltaY > shakeThresholdY)
				|| (deltaX > shakeThresholdX && deltaZ > shakeThresholdZ)
				|| (deltaY > shakeThresholdY && deltaZ > shakeThresholdZ);
	}
	
	private void executeShakeActionDelayed() {
		long now = System.currentTimeMillis();
		if (now - lastShakeEvent > shakeEventThrottle) {
			lastShakeEvent = now;
			executeShakeAction();
		}
	}
	
	private void executeShakeAction() {
		switch (Integer.parseInt(XMain.pref.getString("pref_key_prism_shakeaction", "1"))) {
			case 2: GlobalActions.expandNotifications(helperContext); return;
			case 3: GlobalActions.expandEQS(helperContext); return;
			case 4: GlobalActions.lockDevice(helperContext); return;
			case 5: GlobalActions.goToSleep(helperContext); return;
			case 6: GlobalActions.takeScreenshot(helperContext); return;
			case 7: GlobalActions.launchApp(helperContext, 7); return;
			case 8: GlobalActions.toggleThis(helperContext, Integer.parseInt(XMain.pref.getString("pref_key_prism_shake_toggle", "0"))); return;
			case 12: GlobalActions.launchShortcut(helperContext, 7); return;
			case 14: GlobalActions.openAppDrawer(helperContext); return;
			default: return;
		}
	}
}

package com.langerhans.one.utils;

import com.langerhans.one.R;

import android.app.Service;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class LockHelper extends Service {
	public static class LockHelperReceiver extends DeviceAdminReceiver {	
		@Override
		public void onEnabled(Context context, Intent intent) {
			Toast.makeText(context, R.string.admin_enabled, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onDisabled(Context context, Intent intent) {
			Toast.makeText(context, R.string.admin_disabled, Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		DevicePolicyManager mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName mDeviceAdmin = new ComponentName(this, LockHelperReceiver.class);
		if (mDPM.isAdminActive(mDeviceAdmin)) mDPM.lockNow();
		
		this.stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}

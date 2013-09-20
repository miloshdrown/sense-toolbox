package com.langerhans.one.utils;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;

import com.langerhans.one.R;
import com.langerhans.one.mods.XMain;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class GlobalActions {

	public static Object mPWM = null;
	
	private static BroadcastReceiver mBR = new BroadcastReceiver() {      
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action.equals("com.langerhans.one.mods.action.GoToSleep")) {
				((PowerManager)context.getSystemService(Context.POWER_SERVICE)).goToSleep(SystemClock.uptimeMillis());
			}
			if (action.equals("com.langerhans.one.mods.action.LockDevice")) {
				try {
					final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);
					Method lockNow = XposedHelpers.findMethodExact(clsPWM, "lockNow", Bundle.class);
					Object[] params = new Object[1];
					params[0] = null;
					lockNow.invoke(mPWM, params);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (action.equals("com.langerhans.one.mods.action.TakeScreenshot")) {
				try {
					final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);
					Method takeScreenshot = XposedHelpers.findMethodExact(clsPWM, "takeScreenshot");
					takeScreenshot.invoke(mPWM);					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (action.equals("com.langerhans.one.mods.action.ToggleWiFi")) {
				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				boolean wifiEnabled = wifiManager.isWifiEnabled();
				if (wifiEnabled)
					wifiManager.setWifiEnabled(false);
				else
					wifiManager.setWifiEnabled(true);
			}
		}
	};
	
	public static void setupPWM() {
		try {
			final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);

			findAndHookMethod(clsPWM, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					mPWM = param.thisObject;
					Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
		            IntentFilter intentfilter = new IntentFilter();
		            
		            // Actions
		            intentfilter.addAction("com.langerhans.one.mods.action.GoToSleep");
		            intentfilter.addAction("com.langerhans.one.mods.action.LockDevice");
		            intentfilter.addAction("com.langerhans.one.mods.action.TakeScreenshot");
		            
		            // Toggles
		            intentfilter.addAction("com.langerhans.one.mods.action.ToggleWiFi");
		            intentfilter.addAction("com.langerhans.one.mods.action.ToggleBluetooth");
		            intentfilter.addAction("com.langerhans.one.mods.action.ToggleGPS");
		            intentfilter.addAction("com.langerhans.one.mods.action.ToggleNFC");
		            intentfilter.addAction("com.langerhans.one.mods.action.ToggleSoundProfile");
		            intentfilter.addAction("com.langerhans.one.mods.action.ToggleAutoBrightness");
		            intentfilter.addAction("com.langerhans.one.mods.action.ToggleAutoRotation");
		            intentfilter.addAction("com.langerhans.one.mods.action.ToggleFlashlight");
		            intentfilter.addAction("com.langerhans.one.mods.action.ToggleMobileData");

		            mPWMContext.registerReceiver(mBR, intentfilter);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Actions
	public static boolean expandNotifications(Context context) {
		try {
			Object sbservice = context.getSystemService("statusbar");
			Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
			Method showsb;
			if (Build.VERSION.SDK_INT >= 17) {
				showsb = statusbarManager.getMethod("expandNotificationsPanel");
			} else {
				showsb = statusbarManager.getMethod("expand");
			}
			showsb.setAccessible(true);
			showsb.invoke(sbservice);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean expandEQS(Context context) {
		try {
			Object sbservice = context.getSystemService("statusbar");
			Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
			if (Build.VERSION.SDK_INT >= 17) {
				Method showeqs;
				showeqs = statusbarManager.getMethod("expandSettingsPanel");
				showeqs.invoke(sbservice);
				return true;
			} else return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean lockDevice(Context context) {
		try {
        	Intent intent = new Intent();
            intent.setAction("com.langerhans.one.mods.action.LockDevice");
            context.sendBroadcast(intent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean goToSleep(Context context) {
        try {
        	Intent intent = new Intent();
            intent.setAction("com.langerhans.one.mods.action.GoToSleep");
            context.sendBroadcast(intent);
        	return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
	
	public static boolean launchApp(Context context, int action) {
        try {
        	PackageManager manager = context.getPackageManager();
        	Resources toolboxRes = manager.getResourcesForApplication("com.langerhans.one");
        	
        	String not_selected = toolboxRes.getString(R.string.notselected);
        	String pkgAppName = "";
        	if (action == 1) pkgAppName = XMain.pref.getString("pref_key_prism_swipedown_app", not_selected);
        	else if (action == 2) pkgAppName = XMain.pref.getString("pref_key_prism_swipeup_app", not_selected);
        	else if (action == 3) pkgAppName = XMain.pref.getString("pref_key_controls_backlongpress_app", not_selected);
        	else if (action == 4) pkgAppName = XMain.pref.getString("pref_key_controls_homeassist_app", not_selected);
        	String[] pkgAppArray = pkgAppName.split("\\|");
        	
        	ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
        	Intent intent = new Intent(Intent.ACTION_MAIN);
        	intent.addCategory(Intent.CATEGORY_LAUNCHER);
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        	intent.setComponent(name);
        	context.startActivity(intent);
        	
        	return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
	
	public static boolean takeScreenshot(Context context) {
        try {
        	Intent intent = new Intent();
            intent.setAction("com.langerhans.one.mods.action.TakeScreenshot");
            context.sendBroadcast(intent);
        	return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
	
	public static boolean toggleThis(Context context, int what) {
        try {
        	String whatStr = "WiFi";
        	switch (what) {
        		case 1: whatStr = "WiFi"; break;
        		case 2: whatStr = "Bluetooth"; break;
        		case 3: whatStr = "GPS"; break;
        		case 4: whatStr = "NFC"; break;
        		case 5: whatStr = "SoundProfile"; break;
        		case 6: whatStr = "AutoBrightness"; break;
        		case 7: whatStr = "AutoRotation"; break;
        		case 8: whatStr = "Flashlight"; break;
        		case 9: whatStr = "MobileData"; break;
        		default: return false;
        	}
        	Intent intent = new Intent();
            intent.setAction("com.langerhans.one.mods.action.Toggle" + whatStr);
            context.sendBroadcast(intent);
        	return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
}
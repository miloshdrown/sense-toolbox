package com.sensetoolbox.six.utils;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setStaticBooleanField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;

import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.graphics.ColorFilter;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.sensetoolbox.six.PrefsFragment;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.mods.XMain;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class GlobalActions {

	public static Object mPWM = null;
	//public static Handler mHandler = null;
	private static int mCurrentLEDLevel = 0;
	
	private static BroadcastReceiver mBR = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent)
		{
			try {
			
			String action = intent.getAction();
			// Actions
			if (action.equals("com.sensetoolbox.six.mods.action.GoToSleep")) {
				((PowerManager)context.getSystemService(Context.POWER_SERVICE)).goToSleep(SystemClock.uptimeMillis());
			}
			if (action.equals("com.sensetoolbox.six.mods.action.LockDevice")) {
				if (mPWM != null) XposedHelpers.callMethod(mPWM, "lockNow", new Object[]{ null });
			}
			if (action.equals("com.sensetoolbox.six.mods.action.TakeScreenshot")) {
				if (mPWM != null) XposedHelpers.callMethod(mPWM, "takeScreenshot");
			}
			/*
			if (action.equals("com.sensetoolbox.six.mods.action.killForegroundAppShedule")) {
				if (mHandler == null) return;
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						removeTask(context, true);
					}
				}, 1000);
			}
			*/
			if (action.equals("com.sensetoolbox.six.mods.action.killForegroundApp")) {
				removeTask(context);
			}
			
			if (action.equals("com.sensetoolbox.six.mods.action.SimulateMenu")) {
				new Thread(new Runnable() {         
			        public void run() {
			            Instrumentation inst = new Instrumentation();  
			            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
			        }   
			    }).start();
			}
			
			if (action.equals("com.sensetoolbox.six.mods.action.OpenRecents")) {
				if (mPWM != null) XposedHelpers.callMethod(mPWM, "toggleRecentApps");
			}
			
			final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
			
			// Toggles
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleWiFi")) {
				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				if (wifiManager.isWifiEnabled()) {
					wifiManager.setWifiEnabled(false);
					Toast.makeText(context, modRes.getString(R.string.toggle_wifi_off), Toast.LENGTH_SHORT).show();
				} else {
					wifiManager.setWifiEnabled(true);
					Toast.makeText(context, modRes.getString(R.string.toggle_wifi_on), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleBluetooth")) {
				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    
				if (mBluetoothAdapter.isEnabled()) {
					mBluetoothAdapter.disable();
					Toast.makeText(context, modRes.getString(R.string.toggle_bt_off), Toast.LENGTH_SHORT).show();
				} else {
					mBluetoothAdapter.enable();
					Toast.makeText(context, modRes.getString(R.string.toggle_bt_on), Toast.LENGTH_SHORT).show();
				}
			}
			
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleGPS")) {
				LocationManager locManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
				if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					turnGPSOff(context);
					Toast.makeText(context, modRes.getString(R.string.toggle_gps_off), Toast.LENGTH_SHORT).show();
				} else {
					turnGPSOn(context);
					Toast.makeText(context, modRes.getString(R.string.toggle_gps_on), Toast.LENGTH_SHORT).show();
				}
			}
			
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleNFC")) {
				Class<?> clsNfcAdapter = XposedHelpers.findClass("android.nfc.NfcAdapter", null);
				NfcAdapter mNfcAdapter = (NfcAdapter) XposedHelpers.callStaticMethod(clsNfcAdapter, "getNfcAdapter", context);
				if (mNfcAdapter == null) return;

				Method enableNFC = clsNfcAdapter.getDeclaredMethod("enable");
				Method disableNFC = clsNfcAdapter.getDeclaredMethod("disable");
				enableNFC.setAccessible(true);
				disableNFC.setAccessible(true);
				
				if (mNfcAdapter.isEnabled()) {
					disableNFC.invoke(mNfcAdapter);
					Toast.makeText(context, modRes.getString(R.string.toggle_nfc_off), Toast.LENGTH_SHORT).show();
				} else {
					enableNFC.invoke(mNfcAdapter);
					Toast.makeText(context, modRes.getString(R.string.toggle_nfc_on), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleSoundProfile")) {
				AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
				int currentMode = am.getRingerMode();
				if (currentMode == 0) {
					am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
					Toast.makeText(context, modRes.getString(R.string.toggle_sound_vibrate), Toast.LENGTH_SHORT).show();
				} else if (currentMode == 1) {
					am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					Toast.makeText(context, modRes.getString(R.string.toggle_sound_normal), Toast.LENGTH_SHORT).show();
				} else if (currentMode == 2) {
					am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					Toast.makeText(context, modRes.getString(R.string.toggle_sound_silent), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleAutoBrightness")) {
				if (Settings.System.getInt(context.getContentResolver(), "screen_brightness_mode", 0) == 0) {
					Settings.System.putInt(context.getContentResolver(), "screen_brightness_mode", 1);
					Toast.makeText(context, modRes.getString(R.string.toggle_autobright_on), Toast.LENGTH_SHORT).show();
				} else {
					Settings.System.putInt(context.getContentResolver(), "screen_brightness_mode", 0);
					Toast.makeText(context, modRes.getString(R.string.toggle_autobright_off), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleAutoRotation")) {
				if (Settings.System.getInt(context.getContentResolver(), "accelerometer_rotation", 0) == 0) {
					Settings.System.putInt(context.getContentResolver(), "accelerometer_rotation", 1);
					Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_on), Toast.LENGTH_SHORT).show();
				} else {
					Settings.System.putInt(context.getContentResolver(), "accelerometer_rotation", 0);
					Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_off), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleFlashlight")) {
				if (mCurrentLEDLevel == 0) {
					mCurrentLEDLevel = 125;
					Toast.makeText(context, modRes.getString(R.string.toggle_flash_low), Toast.LENGTH_SHORT).show();
				} else if (mCurrentLEDLevel == 125) {
					mCurrentLEDLevel = 126;
					Toast.makeText(context, modRes.getString(R.string.toggle_flash_med), Toast.LENGTH_SHORT).show();
				} else if (mCurrentLEDLevel == 126) {
					mCurrentLEDLevel = 127;
					Toast.makeText(context, modRes.getString(R.string.toggle_flash_high), Toast.LENGTH_SHORT).show();
				} else if (mCurrentLEDLevel == 127) {
					mCurrentLEDLevel = 0;
					Toast.makeText(context, modRes.getString(R.string.toggle_flash_off), Toast.LENGTH_SHORT).show();
				}
				setFlashlight(mCurrentLEDLevel);
			}
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleMobileData")) {
				ConnectivityManager dataManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
				Method setMTE = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
				Method getMTE = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
				setMTE.setAccessible(true);
				getMTE.setAccessible(true);
					
				if ((Boolean)getMTE.invoke(dataManager)) {
					setMTE.invoke(dataManager, false);
					Toast.makeText(context, modRes.getString(R.string.toggle_mobiledata_off), Toast.LENGTH_SHORT).show();
				} else {
					setMTE.invoke(dataManager, true);
					Toast.makeText(context, modRes.getString(R.string.toggle_mobiledata_on), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.sensetoolbox.six.mods.action.APMReboot")) {
				setStaticObjectField(findClass("com.android.server.power.ShutdownThread", null), "mRebootReason", "oem-11");
				setStaticBooleanField(findClass("com.android.server.power.ShutdownThread", null), "mReboot", true);
				setStaticBooleanField(findClass("com.android.server.power.ShutdownThread", null), "mRebootSafeMode", false);
				callStaticMethod(findClass("com.android.server.power.ShutdownThread", null), "shutdownInner", context, false);
			}
			if (action.equals("com.sensetoolbox.six.mods.action.APMRebootRecovery")) {
				setStaticObjectField(findClass("com.android.server.power.ShutdownThread", null), "mRebootReason", "recovery");
				setStaticBooleanField(findClass("com.android.server.power.ShutdownThread", null), "mReboot", true);
				setStaticBooleanField(findClass("com.android.server.power.ShutdownThread", null), "mRebootSafeMode", false);
				callStaticMethod(findClass("com.android.server.power.ShutdownThread", null), "shutdownInner", context, false);
			}
			if (action.equals("com.sensetoolbox.six.mods.action.APMRebootBootloader")) {
				setStaticObjectField(findClass("com.android.server.power.ShutdownThread", null), "mRebootReason", "bootloader");
				setStaticBooleanField(findClass("com.android.server.power.ShutdownThread", null), "mReboot", true);
				setStaticBooleanField(findClass("com.android.server.power.ShutdownThread", null), "mRebootSafeMode", false);
				callStaticMethod(findClass("com.android.server.power.ShutdownThread", null), "shutdownInner", context, false);
			}
			
			} catch(Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
	
	public static void setFlashlight(int level) {
		try {
			Method setFlashlightBrightness = null;
			Object svc = null;
			Object HTCHW = Class.forName("android.os.ServiceManager").getMethod("getService", new Class[] { String.class }).invoke(null, new Object[] { "htchardware" });
			Method HTCHWInterface = Class.forName("android.os.IHtcHardwareService$Stub").getMethod("asInterface", new Class[] { IBinder.class });
			Object[] paramArr = new Object[1];
			paramArr[0] = ((IBinder)HTCHW);
			svc = HTCHWInterface.invoke(null, paramArr);
			Class<?> svcClass = svc.getClass();
			Class<?>[] paramArray2 = new Class[1];
			paramArray2[0] = Integer.TYPE;
			setFlashlightBrightness = svcClass.getMethod("setFlashlightBrightness", paramArray2);
			Object[] paramArray = new Object[1];
			paramArray[0] = level;
			setFlashlightBrightness.invoke(svc, paramArray);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static String beforeEnable;
	
	private static void removeTask(Context context) {
		try {
			final ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
			final List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
			final Method removeTask = am.getClass().getMethod("removeTask", new Class[] { int.class, int.class });
			final Method forceStopPackage = am.getClass().getMethod("forceStopPackage", new Class[] { String.class });
			removeTask.setAccessible(true);
			forceStopPackage.setAccessible(true);
			String thisPkg = taskInfo.get(0).topActivity.getPackageName();
			
			boolean isLauncher = false;
			PackageManager pm = context.getPackageManager();
			Intent intent_home = new Intent(Intent.ACTION_MAIN);
			intent_home.addCategory(Intent.CATEGORY_HOME);
			intent_home.addCategory(Intent.CATEGORY_DEFAULT);
			List<ResolveInfo> launcherList = pm.queryIntentActivities(intent_home, 0);
			
			for (ResolveInfo launcher: launcherList)
			if (launcher.activityInfo.packageName.equals(thisPkg)) isLauncher = true;

			if (thisPkg.equalsIgnoreCase("com.htc.android.worldclock")) isLauncher = true;
			
			if (isLauncher) return;

			// Removes from recents also
			removeTask.invoke(am, Integer.valueOf(taskInfo.get(0).id), Integer.valueOf(1));
			// Force closes all package parts 
			forceStopPackage.invoke(am, thisPkg);
			
			// Getting HTC Power Saver vibration state
			Class<?> clsSP = Class.forName("android.os.SystemProperties");
			Method getFunc = clsSP.getDeclaredMethod("get", String.class);
			String haptic = (String)getFunc.invoke(null, "sys.psaver.haptic");
			
			if (haptic.equals("false")) {			
				Vibrator vibe = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
				vibe.vibrate(50);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void turnGPSOn(Context context) {
		beforeEnable = Settings.Secure.getString (context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		String newSet;
		if (beforeEnable.equals(""))
			newSet = LocationManager.GPS_PROVIDER;
		else
			newSet = String.format("%s,%s", beforeEnable, LocationManager.GPS_PROVIDER);
		
		try {
			Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newSet);	
		} catch(Throwable t) {
			XposedBridge.log(t);
		}
	}

	@SuppressWarnings("deprecation")
	private static void turnGPSOff(Context context) {
		if (beforeEnable == null) {
			String str = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			if (str == null)
				str = "";
			else {				
				String[] list = str.split (",");
				str = "";

				int j = 0;
				for (int i = 0; i < list.length; i++) {
					if (!list[i].equals(LocationManager.GPS_PROVIDER)) {
						if (j > 0) str += ",";
						str += list[i];
						j++;
					}
				}
				beforeEnable = str;
			}
		}
		try {
			Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, beforeEnable);
		} catch(Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void toolboxInit(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.sensetoolbox.six.PrefsFragment", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.setBooleanField(param.thisObject, "toolboxModuleActive", true);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static BroadcastReceiver mBREgg = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			try {
				Intent intentEgg = new Intent(Intent.ACTION_MAIN);
				intentEgg.setClassName("android", "com.android.internal.app.PlatLogoActivity");
				intentEgg.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				context.startActivity(intentEgg);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
	
	public static void easterEgg() {
		try {
			final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);
			findAndHookMethod(clsPWM, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
		            IntentFilter intentfilter = new IntentFilter();
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.StartEasterEgg");
		            mPWMContext.registerReceiver(mBREgg, intentfilter);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void setupPWM() {
		try {
			final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);
			findAndHookMethod(clsPWM, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					mPWM = param.thisObject;
					//mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
					Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
		            IntentFilter intentfilter = new IntentFilter();
		            
		            // Actions
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.GoToSleep");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.LockDevice");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.TakeScreenshot");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.killForegroundApp");
		            //intentfilter.addAction("com.sensetoolbox.six.mods.action.killForegroundAppShedule");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.SimulateMenu");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.OpenRecents");
		            
		            // Toggles
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.ToggleWiFi");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.ToggleBluetooth");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.ToggleGPS");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.ToggleNFC");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.ToggleSoundProfile");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.ToggleAutoBrightness");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.ToggleAutoRotation");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.ToggleFlashlight");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.ToggleMobileData");
		            
		            //APM
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.APMReboot");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.APMRebootRecovery");
		            intentfilter.addAction("com.sensetoolbox.six.mods.action.APMRebootBootloader");
		            
		            mPWMContext.registerReceiver(mBR, intentfilter);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
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
		} catch (Throwable t) {
			XposedBridge.log(t);
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
				showeqs.setAccessible(true);
				showeqs.invoke(sbservice);
				return true;
			} else return false;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean lockDevice(Context context) {
		try {
        	Intent intent = new Intent();
            intent.setAction("com.sensetoolbox.six.mods.action.LockDevice");
            context.sendBroadcast(intent);
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean goToSleep(Context context) {
        try {
        	Intent intent = new Intent();
            intent.setAction("com.sensetoolbox.six.mods.action.GoToSleep");
            context.sendBroadcast(intent);
        	return true;
        } catch (Throwable t) {
        	XposedBridge.log(t);
            return false;
        }
	}
	
	public static boolean launchApp(Context context, int action) {
        try {
        	PackageManager manager = context.getPackageManager();
        	Resources toolboxRes = manager.getResourcesForApplication("com.sensetoolbox.six");
        	
        	String not_selected = toolboxRes.getString(R.string.notselected);
        	String pkgAppName = "";
        	if (action == 1) pkgAppName = XMain.pref.getString("pref_key_prism_swipedown_app", not_selected);
        	else if (action == 2) pkgAppName = XMain.pref.getString("pref_key_prism_swipeup_app", not_selected);
        	else if (action == 3) pkgAppName = XMain.pref.getString("pref_key_controls_backlongpress_app", not_selected);
        	else if (action == 4) pkgAppName = XMain.pref.getString("pref_key_controls_homeassist_app", not_selected);
        	else if (action == 5) pkgAppName = XMain.pref.getString("pref_key_prism_swiperight_app", not_selected);
        	else if (action == 6) pkgAppName = XMain.pref.getString("pref_key_prism_swipeleft_app", not_selected);
        	else if (action == 7) pkgAppName = XMain.pref.getString("pref_key_prism_shake_app", not_selected);
        	String[] pkgAppArray = pkgAppName.split("\\|");
        	
        	ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
        	Intent intent = new Intent(Intent.ACTION_MAIN);
        	intent.addCategory(Intent.CATEGORY_LAUNCHER);
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        	intent.setComponent(name);
        	context.startActivity(intent);
        	
        	return true;
        } catch (Throwable t) {
        	XposedBridge.log(t);
            return false;
        }
	}
	
	public static boolean takeScreenshot(Context context) {
        try {
        	Intent intent = new Intent();
            intent.setAction("com.sensetoolbox.six.mods.action.TakeScreenshot");
            context.sendBroadcast(intent);
        	return true;
        } catch (Throwable t) {
        	XposedBridge.log(t);
            return false;
        }
	}
	
	public static boolean killForegroundApp(Context context) {
        try {
        	Intent intent = new Intent();
            intent.setAction("com.sensetoolbox.six.mods.action.killForegroundApp");
            context.sendBroadcast(intent);
        	return true;
        } catch (Throwable t) {
        	XposedBridge.log(t);
            return false;
        }
	}
	
	public static boolean simulateMenu(Context context) {
        try {
        	Intent intent = new Intent();
            intent.setAction("com.sensetoolbox.six.mods.action.SimulateMenu");
            context.sendBroadcast(intent);
        	return true;
        } catch (Throwable t) {
        	XposedBridge.log(t);
            return false;
        }
	}
	
	public static boolean openRecents(Context context) {
        try {
        	Intent intent = new Intent();
            intent.setAction("com.sensetoolbox.six.mods.action.OpenRecents");
            context.sendBroadcast(intent);
        	return true;
        } catch (Throwable t) {
        	XposedBridge.log(t);
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
            intent.setAction("com.sensetoolbox.six.mods.action.Toggle" + whatStr);
            context.sendBroadcast(intent);
        	return true;
        } catch (Throwable t) {
        	XposedBridge.log(t);
            return false;
        }
	}
	
	public static ColorFilter createColorFilter(boolean fromModule) {
		int brightness = 0;
		int saturation = 0;
		int hue = 0;
		
		if (fromModule) {
			if (XMain.pref != null) {
				brightness = XMain.pref.getInt("pref_key_colorfilter_brightValue", 100) - 100;
				saturation = XMain.pref.getInt("pref_key_colorfilter_satValue", 100) - 100;
				hue = XMain.pref.getInt("pref_key_colorfilter_hueValue", 180) - 180;
			}
		} else {
			if (PrefsFragment.prefs != null) {
				brightness = PrefsFragment.prefs.getInt("pref_key_colorfilter_brightValue", 100) - 100;
				saturation = PrefsFragment.prefs.getInt("pref_key_colorfilter_satValue", 100) - 100;
				hue = PrefsFragment.prefs.getInt("pref_key_colorfilter_hueValue", 180) - 180;		
			}
		}
		
		if (brightness == 0 && saturation == 0 && hue == 0)
			return null;
		else if (brightness == 100 && saturation == -100)
			return ColorFilterGenerator.adjustColor(100, 100, -100, -180);
		else
			return ColorFilterGenerator.adjustColor(brightness, 0, saturation, hue);
	}
	
	public static void sendMediaButton(KeyEvent keyEvent)
	{
		try {
	        // Get binder from ServiceManager.checkService(String)
	        IBinder iBinder  = (IBinder) Class.forName("android.os.ServiceManager")
	        .getDeclaredMethod("checkService", String.class)
	        .invoke(null, Context.AUDIO_SERVICE);

	        // get audioService from IAudioService.Stub.asInterface(IBinder)
	        Object audioService  = Class.forName("android.media.IAudioService$Stub")
	        .getDeclaredMethod("asInterface", IBinder.class)
	        .invoke(null, iBinder);

	        // Dispatch keyEvent using IAudioService.dispatchMediaKeyEvent(KeyEvent)
	        Class.forName("android.media.IAudioService")
	        .getDeclaredMethod("dispatchMediaKeyEvent", KeyEvent.class)
	        .invoke(audioService, keyEvent);            
	    }  catch (Throwable t) {
	        XposedBridge.log(t);
	    }
	}
	
	private static BroadcastReceiver mScrOff = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			Intent intent2 = new Intent();
	        intent2.setAction("com.sensetoolbox.six.UPDATEBACKLIGHT");
			intent2.putExtra("forceDisableBacklight", true);
		}
	};
	
	public static void buttonBacklight(){
		try {
			findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					mPWMContext.registerReceiver(mScrOff, new IntentFilter(Intent.ACTION_SCREEN_OFF));
				}
			});
			
			findAndHookMethod("com.android.server.wm.WindowManagerService", null, "statusBarVisibilityChanged", int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					Intent intent = new Intent();
			        intent.setAction("com.sensetoolbox.six.UPDATEBACKLIGHT");
			        
					int sysUiVis = (Integer)param.args[0];
					if (sysUiVis == 67108864 || sysUiVis == 0) return;
					//XposedBridge.log("statusBarVisibilityChanged: " + String.valueOf(sysUiVis));
					if (sysUiVis != 0 && ((sysUiVis & View.SYSTEM_UI_FLAG_FULLSCREEN) == View.SYSTEM_UI_FLAG_FULLSCREEN
						|| (sysUiVis & View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
						|| (sysUiVis & View.SYSTEM_UI_FLAG_LOW_PROFILE) == View.SYSTEM_UI_FLAG_LOW_PROFILE))
						intent.putExtra("forceDisableBacklight", true);
					
					mContext.sendBroadcast(intent);
				}
			});
			
			findAndHookMethod("android.view.Window", null, "setFlags", int.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Window wnd = (Window)param.thisObject;
					if (wnd != null && wnd.getContext().getPackageName().equals("com.google.android.youtube")) {
						WindowManager.LayoutParams mWindowAttributes = (WindowManager.LayoutParams)XposedHelpers.getObjectField(param.thisObject, "mWindowAttributes");
						if (mWindowAttributes == null) return;
						int i = (Integer)param.args[0];
						int j = (Integer)param.args[1];
						int newFlags = mWindowAttributes.flags & ~j | i & j;
						
						if (newFlags != 0 &&
						   (newFlags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != WindowManager.LayoutParams.FLAG_FULLSCREEN &&
						   (newFlags & WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN) == WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN &&
						   (newFlags & WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR) == WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR) {
								//XposedBridge.log("setFlags FLAG_LAYOUT_*: " + String.valueOf(newFlags));
								Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
								Intent intent = new Intent();
								intent.setAction("com.sensetoolbox.six.UPDATEBACKLIGHT");
								mContext.sendBroadcast(intent);
						}
					}
				}
			});
			
			findAndHookMethod("android.app.Activity", null, "onResume", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Activity act = (Activity)param.thisObject;
					if (act == null) return;
					int newFlags = act.getWindow().getAttributes().flags;
					//XposedBridge.log("onResume flags: " + String.valueOf(newFlags));
					Intent intent = new Intent();
			        intent.setAction("com.sensetoolbox.six.UPDATEBACKLIGHT");
			        if (newFlags != 0 && (newFlags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN && !act.getPackageName().equals("com.android.systemui"))
					intent.putExtra("forceDisableBacklight", true);
					act.sendBroadcast(intent);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
}
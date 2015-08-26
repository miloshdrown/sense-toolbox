package com.sensetoolbox.six.utils;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setStaticBooleanField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;

import java.lang.reflect.Method;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.Application;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XModuleResources;
import android.graphics.ColorFilter;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.Toast;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.mods.XMain;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class GlobalActions {

	public static Object mPWM = null;
	public static Object mDMS = null;
	public static Object mPSB = null;
	public static FloatingSelector floatSel = null;
	//public static Handler mHandler = null;
	private static int mCurrentLEDLevel = 0;
	
	private static BroadcastReceiver mBRLock = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			try {
				if (mDMS != null) XposedHelpers.callMethod(mDMS, "lockNowUnchecked");
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
	
	private static BroadcastReceiver mBRDrawer = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			try {
				String action = intent.getAction();
				if (action.equals("com.sensetoolbox.six.mods.action.ShowQuickRecents")) {
					if (floatSel == null) floatSel = new FloatingSelector(context);
					floatSel.show();
				} else if (action.equals("com.sensetoolbox.six.mods.action.HideQuickRecents")) {
					if (floatSel != null) floatSel.hide();
				}
				
				if (mPSB != null) {
					if (action.equals("com.sensetoolbox.six.mods.action.ExpandNotifications"))
						XposedHelpers.callMethod(mPSB, "animateExpandNotificationsPanel");
					else if (action.equals("com.sensetoolbox.six.mods.action.ExpandSettings")) {
						if (Helpers.isLP()) {
							if (XMain.pref.getBoolean("pref_key_other_secureeqs", false)) {
								KeyguardManager kgMgr = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
								if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) return;
							}
							XposedHelpers.callMethod(mPSB, "animateExpandSettingsPanelInternal");
						} else {
							XposedHelpers.callMethod(mPSB, "animateExpandSettingsPanel");
						}
					}
				}
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
			
	private static BroadcastReceiver mBR = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			try {
			
			String action = intent.getAction();
			// Actions
			if (action.equals("com.sensetoolbox.six.mods.action.GoToSleep")) {
				XposedHelpers.callMethod(((PowerManager)context.getSystemService(Context.POWER_SERVICE)), "goToSleep", SystemClock.uptimeMillis());
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
			
			if (action.equals("com.sensetoolbox.six.mods.action.SwitchToPrevApp")) {
				PackageManager pm = context.getPackageManager();
				Intent intent_home = new Intent(Intent.ACTION_MAIN);
				intent_home.addCategory(Intent.CATEGORY_HOME);
				intent_home.addCategory(Intent.CATEGORY_DEFAULT);
				List<ResolveInfo> launcherList = pm.queryIntentActivities(intent_home, 0);
				
				ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
				@SuppressWarnings("deprecation")
				List<RecentTaskInfo> rti = am.getRecentTasks(Integer.MAX_VALUE, 0);
				
				Intent recentIntent;
				boolean isFirstRecent = true;
				for (RecentTaskInfo rtitem: rti) try {
					if (isFirstRecent) {
						isFirstRecent = false;
						continue;
					}
					
					boolean isLauncher = false;
					recentIntent = new Intent(rtitem.baseIntent);
					if (rtitem.origActivity != null) recentIntent.setComponent(rtitem.origActivity);
					ComponentName resolvedAct = recentIntent.resolveActivity(pm);
					
					if (resolvedAct != null)
					for (ResolveInfo launcher: launcherList)
					if (launcher.activityInfo.packageName.equals(resolvedAct.getPackageName())) {
						isLauncher = true;
						break;
					}
					
					if (!isLauncher) {
						if (Helpers.getHtcHaptic(context)) {
							Vibrator vibe = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
							if (XMain.pref.getBoolean("pref_key_controls_longpresshaptic_enable", false))
								vibe.vibrate(XMain.pref.getInt("pref_key_controls_longpresshaptic", 21));
							else
								vibe.vibrate(21);
						}
						if (rtitem.id >= 0)
							am.moveTaskToFront(rtitem.id, 0);
						else
							context.startActivity(recentIntent);
						break;
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
			
			final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
			
			// Toggles
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleWiFi")) {
				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				if (wifiManager.isWifiEnabled()) {
					wifiManager.setWifiEnabled(false);
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_wifi_off), Toast.LENGTH_SHORT).show();
				} else {
					wifiManager.setWifiEnabled(true);
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_wifi_on), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleBluetooth")) {
				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if (mBluetoothAdapter.isEnabled()) {
					mBluetoothAdapter.disable();
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_bt_off), Toast.LENGTH_SHORT).show();
				} else {
					mBluetoothAdapter.enable();
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_bt_on), Toast.LENGTH_SHORT).show();
				}
			}
			
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleGPS")) {
				LocationManager locManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
				if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					turnGPSOff(context);
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_gps_off), Toast.LENGTH_SHORT).show();
				} else {
					turnGPSOn(context);
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_gps_on), Toast.LENGTH_SHORT).show();
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
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_nfc_off), Toast.LENGTH_SHORT).show();
				} else {
					enableNFC.invoke(mNfcAdapter);
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_nfc_on), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleSoundProfile")) {
				AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
				int currentMode = am.getRingerMode();
				if (currentMode == 0) {
					am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_sound_vibrate), Toast.LENGTH_SHORT).show();
				} else if (currentMode == 1) {
					am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_sound_normal), Toast.LENGTH_SHORT).show();
				} else if (currentMode == 2) {
					am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_sound_silent), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleAutoBrightness")) {
				if (Settings.System.getInt(context.getContentResolver(), "screen_brightness_mode", 0) == 0) {
					Settings.System.putInt(context.getContentResolver(), "screen_brightness_mode", 1);
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_autobright_on), Toast.LENGTH_SHORT).show();
				} else {
					Settings.System.putInt(context.getContentResolver(), "screen_brightness_mode", 0);
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_autobright_off), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleAutoRotation")) {
				if (Settings.System.getInt(context.getContentResolver(), "accelerometer_rotation", 0) == 0) {
					Settings.System.putInt(context.getContentResolver(), "accelerometer_rotation", 1);
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_autorotate_on), Toast.LENGTH_SHORT).show();
				} else {
					Settings.System.putInt(context.getContentResolver(), "accelerometer_rotation", 0);
					Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_autorotate_off), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.htc.intent.action.STATUS_BAR_FLASHLIGHT_RESULT")) {
				mCurrentLEDLevel = intent.getIntExtra("com.htc.flashlight.state", 0);
			} else if (action.equals("com.sensetoolbox.six.mods.action.ToggleFlashlight")) {
				if (Helpers.getHtcFlashlight()) {
					if (mCurrentLEDLevel == 0) {
						mCurrentLEDLevel = 125;
						Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_flash_low), Toast.LENGTH_SHORT).show();
					} else if (mCurrentLEDLevel == 125) {
						mCurrentLEDLevel = 126;
						Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_flash_med), Toast.LENGTH_SHORT).show();
					} else if (mCurrentLEDLevel == 126) {
						mCurrentLEDLevel = 127;
						Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_flash_high), Toast.LENGTH_SHORT).show();
					} else if (mCurrentLEDLevel == 127) {
						mCurrentLEDLevel = 0;
						Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_flash_off), Toast.LENGTH_SHORT).show();
					}
					setHtcFlashlight(mCurrentLEDLevel);
				} else {
					if (mCurrentLEDLevel == 0) {
						mCurrentLEDLevel = 127;
						Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_flash_high), Toast.LENGTH_SHORT).show();
					} else {
						mCurrentLEDLevel = 0;
						Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_flash_off), Toast.LENGTH_SHORT).show();
					}
					setStockFlashlight(context, mCurrentLEDLevel);
				}
			}
			if (action.equals("com.sensetoolbox.six.mods.action.ToggleMobileData")) {
				if (Helpers.isLP()) {
					TelephonyManager telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
					Method setMTE = TelephonyManager.class.getDeclaredMethod("setDataEnabled", boolean.class);
					Method getMTE = TelephonyManager.class.getDeclaredMethod("getDataEnabled");
					setMTE.setAccessible(true);
					getMTE.setAccessible(true);
					
					if ((Boolean)getMTE.invoke(telManager)) {
						setMTE.invoke(telManager, false);
						Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_mobiledata_off), Toast.LENGTH_SHORT).show();
					} else {
						setMTE.invoke(telManager, true);
						Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_mobiledata_on), Toast.LENGTH_SHORT).show();
					}
				} else {
					ConnectivityManager dataManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
					Method setMTE = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
					Method getMTE = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
					setMTE.setAccessible(true);
					getMTE.setAccessible(true);
					
					if ((Boolean)getMTE.invoke(dataManager)) {
						setMTE.invoke(dataManager, false);
						Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_mobiledata_off), Toast.LENGTH_SHORT).show();
					} else {
						setMTE.invoke(dataManager, true);
						Toast.makeText(context, Helpers.xl10n(modRes, R.string.toggle_mobiledata_on), Toast.LENGTH_SHORT).show();
					}
				}
			}
			
			String className = "com.htc.app.HtcShutdownThread";
			if (Helpers.isLP()) className = "com.android.internal.policy.impl.HtcShutdown.HtcShutdownThread";
			
			if (action.equals("com.sensetoolbox.six.mods.action.APMReboot")) {
				setStaticObjectField(findClass(className, null), "mRebootReason", "oem-11");
				setStaticBooleanField(findClass(className, null), "mReboot", true);
				setStaticBooleanField(findClass(className, null), "mRebootSafeMode", false);
				callStaticMethod(findClass(className, null), "shutdownInner", context, false);
			}
			if (action.equals("com.sensetoolbox.six.mods.action.APMRebootRecovery")) {
				setStaticObjectField(findClass(className, null), "mRebootReason", "recovery");
				setStaticBooleanField(findClass(className, null), "mReboot", true);
				setStaticBooleanField(findClass(className, null), "mRebootSafeMode", false);
				callStaticMethod(findClass(className, null), "shutdownInner", context, false);
			}
			if (action.equals("com.sensetoolbox.six.mods.action.APMRebootBootloader")) {
				setStaticObjectField(findClass(className, null), "mRebootReason", "bootloader");
				setStaticBooleanField(findClass(className, null), "mReboot", true);
				setStaticBooleanField(findClass(className, null), "mRebootSafeMode", false);
				callStaticMethod(findClass(className, null), "shutdownInner", context, false);
			}
			
			} catch(Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
	
	public static void setHtcFlashlight(int level) {
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
	
	public static void setStockFlashlight(Context mContext, int level) {
		try {
			Intent intent = new Intent("com.htc.intent.action.STATUS_BAR_FLASHLIGH");
			if (level > 0)
				intent.putExtra("com.htc.flashlight.state", 1);
			else
				intent.putExtra("com.htc.flashlight.state", 0);
			intent.setPackage("com.android.systemui");
			mContext.sendBroadcast(intent);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static String beforeEnable;
	
	private static void removeTask(Context context) {
		try {
			final ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
			@SuppressWarnings("deprecation")
			final List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
			final Method removeTask;
			if (Helpers.isLP2())
				removeTask = am.getClass().getMethod("removeTask", new Class[] { int.class });
			else
				removeTask = am.getClass().getMethod("removeTask", new Class[] { int.class, int.class });
			final Method forceStopPackage = am.getClass().getMethod("forceStopPackage", new Class[] { String.class });
			removeTask.setAccessible(true);
			forceStopPackage.setAccessible(true);
			String thisPkg = taskInfo.get(0).topActivity.getPackageName();
			
			boolean isLauncher = false;
			boolean isAllowed = true;
			PackageManager pm = context.getPackageManager();
			Intent intent_home = new Intent(Intent.ACTION_MAIN);
			intent_home.addCategory(Intent.CATEGORY_HOME);
			intent_home.addCategory(Intent.CATEGORY_DEFAULT);
			List<ResolveInfo> launcherList = pm.queryIntentActivities(intent_home, 0);
			
			for (ResolveInfo launcher: launcherList)
			if (launcher.activityInfo.packageName.equals(thisPkg)) isLauncher = true;
			if (thisPkg.equalsIgnoreCase("com.htc.android.worldclock")) isAllowed = false;
			
			if (isLauncher) {
				XposedHelpers.callMethod(((PowerManager)context.getSystemService(Context.POWER_SERVICE)), "goToSleep", SystemClock.uptimeMillis());
			} else if (isAllowed) {
				// Removes from recents also
				if (Helpers.isLP2())
					removeTask.invoke(am, Integer.valueOf(taskInfo.get(0).id));
				else
					removeTask.invoke(am, Integer.valueOf(taskInfo.get(0).id), Integer.valueOf(1));
				// Force closes all package parts
				forceStopPackage.invoke(am, thisPkg);
			}
			
			if (isLauncher || isAllowed) {
				if (Helpers.getHtcHaptic(context)) {
					Vibrator vibe = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
					if (XMain.pref.getBoolean("pref_key_controls_longpresshaptic_enable", false))
						vibe.vibrate(XMain.pref.getInt("pref_key_controls_longpresshaptic", 30));
					else
						vibe.vibrate(30);
				}
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void turnGPSOn(Context context) {
		beforeEnable = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
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
				String[] list = str.split(",");
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
			XposedHelpers.findAndHookMethod("com.sensetoolbox.six.htc.HMainFragment", lpparam.classLoader, "onActivityCreated", Bundle.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.setBooleanField(param.thisObject, "toolboxModuleActive", true);
				}
			});
			
			XposedHelpers.findAndHookMethod("com.sensetoolbox.six.material.MMainFragment", lpparam.classLoader, "onActivityCreated", Bundle.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.setBooleanField(param.thisObject, "toolboxModuleActive", true);
				}
			});
			
			Helpers.emptyFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SenseToolbox/uncaught_exceptions", false);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static BroadcastReceiver mBRTools = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			try {
				String action = intent.getAction();
				ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
				Method forceStopPackage = am.getClass().getMethod("forceStopPackage", new Class[] { String.class });
				forceStopPackage.setAccessible(true);

				if (action.equals("com.sensetoolbox.six.mods.action.StartEasterEgg")) {
					Intent intentEgg = new Intent(Intent.ACTION_MAIN);
					intentEgg.setClassName("android", "com.android.internal.app.PlatLogoActivity");
					intentEgg.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					context.startActivity(intentEgg);
				} else if (action.equals("com.sensetoolbox.six.mods.action.RestartMessages")) {
					forceStopPackage.invoke(am, "com.htc.sense.mms");
				} else if (action.equals("com.sensetoolbox.six.mods.action.RestartPrism")) {
					forceStopPackage.invoke(am, "com.htc.launcher");
					forceStopPackage.invoke(am, "com.htc.widget.weatherclock");
				}
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
	
	public static void toolboxStuff() {
		try {
			final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);
			findAndHookMethod(clsPWM, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					IntentFilter intentfilter = new IntentFilter();
					intentfilter.addAction("com.sensetoolbox.six.mods.action.StartEasterEgg");
					intentfilter.addAction("com.sensetoolbox.six.mods.action.RestartMessages");
					intentfilter.addAction("com.sensetoolbox.six.mods.action.RestartPrism");
					mPWMContext.registerReceiver(mBRTools, intentfilter);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		
		try {
			findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					try {
						final Context ctx = (Application)param.thisObject;
						if (ctx == null || ctx.getPackageName().equals("com.sensetoolbox.six")) return;
						Class<?> clsUEH = Thread.getDefaultUncaughtExceptionHandler().getClass();
						XposedHelpers.findAndHookMethod(clsUEH, "uncaughtException", Thread.class, Throwable.class, new XC_MethodHook() {
							@Override
							protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
								if (param.args[1] != null) {
									Intent intent = new Intent("com.sensetoolbox.six.SAVEEXCEPTION");
									intent.putExtra("throwable", (Throwable)param.args[1]);
									ctx.sendBroadcast(intent);
								}
							}
						});
					} catch (Throwable t) {}
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
					intentfilter.addAction("com.sensetoolbox.six.mods.action.TakeScreenshot");
					intentfilter.addAction("com.sensetoolbox.six.mods.action.killForegroundApp");
					//intentfilter.addAction("com.sensetoolbox.six.mods.action.killForegroundAppShedule");
					intentfilter.addAction("com.sensetoolbox.six.mods.action.SimulateMenu");
					intentfilter.addAction("com.sensetoolbox.six.mods.action.OpenRecents");
					intentfilter.addAction("com.sensetoolbox.six.mods.action.SwitchToPrevApp");
					
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
					
					if (Helpers.isLP2())
					intentfilter.addAction("com.htc.intent.action.STATUS_BAR_FLASHLIGHT_RESULT");
					
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
	
	public static void setupDMS(LoadPackageParam lpparam) {
		String className = "com.android.server.DevicePolicyManagerService";
		if (Helpers.isLP()) className = "com.android.server.devicepolicy.DevicePolicyManagerService";
		XposedBridge.hookAllConstructors(findClass(className, lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				mDMS = param.thisObject;
				Context mDMSContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				IntentFilter intentfilter = new IntentFilter();
				intentfilter.addAction("com.sensetoolbox.six.mods.action.LockDevice");
				mDMSContext.registerReceiver(mBRLock, intentfilter);
			}
		});
	}
	
	public static void setupPSB(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				mPSB = param.thisObject;
				Context mPSBContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				IntentFilter intentfilter = new IntentFilter();
				intentfilter.addAction("com.sensetoolbox.six.mods.action.ExpandNotifications");
				intentfilter.addAction("com.sensetoolbox.six.mods.action.ExpandSettings");
				intentfilter.addAction("com.sensetoolbox.six.mods.action.ShowQuickRecents");
				intentfilter.addAction("com.sensetoolbox.six.mods.action.HideQuickRecents");
				mPSBContext.registerReceiver(mBRDrawer, intentfilter);
			}
		});
	}

	// Actions
	public static boolean expandNotifications(Context context) {
		try {
			context.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.ExpandNotifications"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean expandEQS(Context context) {
		try {
			context.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.ExpandSettings"));
			return false;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean lockDevice(Context context) {
		try {
			context.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.LockDevice"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean goToSleep(Context context) {
		try {
			context.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.GoToSleep"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean launchApp(Context context, int action) {
		try {
			String pkgAppName = null;
			switch (action) {
				case 1: pkgAppName = XMain.pref.getString("pref_key_prism_swipedown_app", null); break;
				case 2: pkgAppName = XMain.pref.getString("pref_key_prism_swipeup_app", null); break;
				case 3: pkgAppName = XMain.pref.getString("pref_key_controls_backlongpress_app", null); break;
				case 4: pkgAppName = XMain.pref.getString("pref_key_controls_homeassist_app", null); break;
				case 5: pkgAppName = XMain.pref.getString("pref_key_prism_swiperight_app", null); break;
				case 6: pkgAppName = XMain.pref.getString("pref_key_prism_swipeleft_app", null); break;
				case 7: pkgAppName = XMain.pref.getString("pref_key_prism_shake_app", null); break;
				case 8: pkgAppName = XMain.pref.getString("pref_key_prism_appslongpress_app", null); break;
				case 9: pkgAppName = XMain.pref.getString("pref_key_controls_wiredheadseton_app", null); break;
				case 10: pkgAppName = XMain.pref.getString("pref_key_controls_wiredheadsetoff_app", null); break;
				case 11: pkgAppName = XMain.pref.getString("pref_key_controls_btheadseton_app", null); break;
				case 12: pkgAppName = XMain.pref.getString("pref_key_controls_btheadsetoff_app", null); break;
				case 13: pkgAppName = XMain.pref.getString("pref_key_controls_clock_app", null); break;
				case 14: pkgAppName = XMain.pref.getString("pref_key_controls_date_app", null); break;
			}
			
			if (pkgAppName != null) {
				String[] pkgAppArray = pkgAppName.split("\\|");
				
				ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				intent.setComponent(name);
				context.startActivity(intent);
				
				return true;
			} else return false;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean launchShortcut(Context context, int action) {
		try {
			String intentString = null;
			switch (action) {
				case 1: intentString = XMain.pref.getString("pref_key_prism_swipedownaction_shortcut_intent", null); break;
				case 2: intentString = XMain.pref.getString("pref_key_prism_swipeupaction_shortcut_intent", null); break;
				case 3: intentString = XMain.pref.getString("pref_key_controls_backlongpressaction_shortcut_intent", null); break;
				case 4: intentString = XMain.pref.getString("pref_key_controls_homeassistaction_shortcut_intent", null); break;
				case 5: intentString = XMain.pref.getString("pref_key_prism_swiperightaction_shortcut_intent", null); break;
				case 6: intentString = XMain.pref.getString("pref_key_prism_swipeleftaction_shortcut_intent", null); break;
				case 7: intentString = XMain.pref.getString("pref_key_prism_shakeaction_shortcut_intent", null); break;
				case 8: intentString = XMain.pref.getString("pref_key_prism_appslongpressaction_shortcut_intent", null); break;
				case 9: intentString = XMain.pref.getString("pref_key_controls_wiredheadsetonaction_shortcut_intent", null); break;
				case 10: intentString = XMain.pref.getString("pref_key_controls_wiredheadsetoffaction_shortcut_intent", null); break;
				case 11: intentString = XMain.pref.getString("pref_key_controls_btheadsetonaction_shortcut_intent", null); break;
				case 12: intentString = XMain.pref.getString("pref_key_controls_btheadsetoffaction_shortcut_intent", null); break;
				case 13: intentString = XMain.pref.getString("pref_key_controls_clockaction_shortcut_intent", null); break;
			}
			
			if (intentString != null) {
				Intent shortcutIntent = Intent.parseUri(intentString, 0);
				shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				context.startActivity(shortcutIntent);
				return true;
			} else return false;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean takeScreenshot(Context context) {
		try {
			context.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.TakeScreenshot"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean killForegroundApp(Context context) {
		try {
			context.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.killForegroundApp"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean simulateMenu(Context context) {
		try {
			context.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.SimulateMenu"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean openRecents(Context context) {
		try {
			context.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.OpenRecents"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean switchToPrevApp(Context context) {
		try {
			context.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.SwitchToPrevApp"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean openAppDrawer(Context context) {
		try {
			context.startActivity(new Intent("com.htc.intent.action.HTC_Prism_AllApps").addCategory(Intent.CATEGORY_DEFAULT).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean showQuickRecents(Context context) {
		try {
			context.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.ShowQuickRecents"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}
	
	public static boolean hideQuickRecents(Context context) {
		try {
			context.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.HideQuickRecents"));
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
			context.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.Toggle" + whatStr));
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
		
		if (Helpers.isLP()) {
			brightness = 100;
			saturation = -100;
			hue = 0;
		} else if (fromModule) {
			if (XMain.pref != null) {
				brightness = XMain.pref.getInt("pref_key_colorfilter_brightValue", 100) - 100;
				saturation = XMain.pref.getInt("pref_key_colorfilter_satValue", 100) - 100;
				hue = XMain.pref.getInt("pref_key_colorfilter_hueValue", 180) - 180;
			}
		} else {
			if (Helpers.prefs != null) {
				brightness = Helpers.prefs.getInt("pref_key_colorfilter_brightValue", 100) - 100;
				saturation = Helpers.prefs.getInt("pref_key_colorfilter_satValue", 100) - 100;
				hue = Helpers.prefs.getInt("pref_key_colorfilter_hueValue", 180) - 180;
			}
		}
		
		if (brightness == 0 && saturation == 0 && hue == 0)
			return null;
		else if (brightness == 100 && saturation == -100)
			return ColorFilterGenerator.adjustColor(100, 100, -100, -180);
		else
			return ColorFilterGenerator.adjustColor(brightness, 0, saturation, hue);
	}
	
	public static void sendMediaButton(Context mContext, KeyEvent keyEvent) {
		try {
			if (Build.VERSION.SDK_INT >= 19) {
				AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
				if (mContext != null) am.dispatchMediaKeyEvent(keyEvent);
			} else {
				// Get binder from ServiceManager.checkService(String)
				IBinder iBinder  = (IBinder) Class.forName("android.os.ServiceManager").getDeclaredMethod("checkService", String.class).invoke(null, Context.AUDIO_SERVICE);
				// Get audioService from IAudioService.Stub.asInterface(IBinder)
				Object audioService  = Class.forName("android.media.IAudioService$Stub").getDeclaredMethod("asInterface", IBinder.class).invoke(null, iBinder);
				// Dispatch keyEvent using IAudioService.dispatchMediaKeyEvent(KeyEvent)
				Class.forName("android.media.IAudioService").getDeclaredMethod("dispatchMediaKeyEvent", KeyEvent.class).invoke(audioService, keyEvent);
			}
		}  catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void collapseDrawer(Context mContext) {
		try {
			Object sbservice = mContext.getSystemService("statusbar");
			Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
			Method hidesb;
			if (Build.VERSION.SDK_INT >= 17) {
				hidesb = statusbarManager.getMethod("collapsePanels");
			} else {
				hidesb = statusbarManager.getMethod("collapse");
			}
			hidesb.setAccessible(true);
			hidesb.invoke(sbservice);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void dismissKeyguard() {
		Object amn = XposedHelpers.callStaticMethod(findClass("android.app.ActivityManagerNative", null), "getDefault");
		if (Helpers.isLP())
			XposedHelpers.callMethod(amn, "keyguardWaitingForActivityDrawn");
		else
			XposedHelpers.callMethod(amn, "dismissKeyguardOnNextActivity");
	}
	
	public static boolean isMediaActionsAllowed(Context mContext) {
		AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		boolean isMusicActive = am.isMusicActive();
		boolean isMusicActiveRemotely  = (Boolean)XposedHelpers.callMethod(am, "isMusicActiveRemotely");
		boolean isAllowed = isMusicActive || isMusicActiveRemotely;
		if (!isAllowed) {
			long mCurrentTime = System.currentTimeMillis();
			long mLastPauseTime = Settings.System.getLong(mContext.getContentResolver(), "last_music_paused_time", mCurrentTime);
			if (mCurrentTime - mLastPauseTime < 10 * 60 * 1000) isAllowed = true;
		}
		return isAllowed;
	}
}
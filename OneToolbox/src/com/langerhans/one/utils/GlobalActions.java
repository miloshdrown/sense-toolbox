package com.langerhans.one.utils;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import java.lang.reflect.Method;
import java.util.List;

import android.app.ActivityManager;
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
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.widget.Toast;

import com.langerhans.one.R;
import com.langerhans.one.mods.XMain;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class GlobalActions {

	public static Object mPWM = null;
	public static Handler mHandler = null;
	private static int mCurrentLEDLevel = 0;
	
	private static BroadcastReceiver mBR = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent)
		{
			String action = intent.getAction();
			// Actions
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
			if (action.equals("com.langerhans.one.mods.action.killForegroundAppShedule")) {
				if (mHandler == null) return;
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						removeTask(context, true);
					}
				}, 1000);
			}
			if (action.equals("com.langerhans.one.mods.action.killForegroundApp")) {
				removeTask(context, false);
			}
			
			final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
			
			// Toggles
			if (action.equals("com.langerhans.one.mods.action.ToggleWiFi")) {
				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				if (wifiManager.isWifiEnabled()) {
					wifiManager.setWifiEnabled(false);
					Toast.makeText(context, modRes.getString(R.string.toggle_wifi_off), Toast.LENGTH_SHORT).show();
				} else {
					wifiManager.setWifiEnabled(true);
					Toast.makeText(context, modRes.getString(R.string.toggle_wifi_on), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.langerhans.one.mods.action.ToggleBluetooth")) {
				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    
				if (mBluetoothAdapter.isEnabled()) {
					mBluetoothAdapter.disable();
					Toast.makeText(context, modRes.getString(R.string.toggle_bt_off), Toast.LENGTH_SHORT).show();
				} else {
					mBluetoothAdapter.enable();
					Toast.makeText(context, modRes.getString(R.string.toggle_bt_on), Toast.LENGTH_SHORT).show();
				}
			}
			
			if (action.equals("com.langerhans.one.mods.action.ToggleGPS")) {
				LocationManager locManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
				if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					turnGPSOff(context);
					Toast.makeText(context, modRes.getString(R.string.toggle_gps_off), Toast.LENGTH_SHORT).show();
				} else {
					turnGPSOn(context);
					Toast.makeText(context, modRes.getString(R.string.toggle_gps_on), Toast.LENGTH_SHORT).show();
				}
			}
			
			if (action.equals("com.langerhans.one.mods.action.ToggleNFC")) {
				try {
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
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			if (action.equals("com.langerhans.one.mods.action.ToggleSoundProfile")) {
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
			if (action.equals("com.langerhans.one.mods.action.ToggleAutoBrightness")) {
				if (Settings.System.getInt(context.getContentResolver(), "screen_brightness_mode", 0) == 0) {
					Settings.System.putInt(context.getContentResolver(), "screen_brightness_mode", 1);
					Toast.makeText(context, modRes.getString(R.string.toggle_autobright_on), Toast.LENGTH_SHORT).show();
				} else {
					Settings.System.putInt(context.getContentResolver(), "screen_brightness_mode", 0);
					Toast.makeText(context, modRes.getString(R.string.toggle_autobright_off), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("com.langerhans.one.mods.action.ToggleAutoRotation")) {
				if (Settings.System.getInt(context.getContentResolver(), "accelerometer_rotation", 0) == 0) {
					Settings.System.putInt(context.getContentResolver(), "accelerometer_rotation", 1);
					Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_on), Toast.LENGTH_SHORT).show();
				} else {
					Settings.System.putInt(context.getContentResolver(), "accelerometer_rotation", 0);
					Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_off), Toast.LENGTH_SHORT).show();
				}
			}
			
			if (action.equals("com.langerhans.one.mods.action.ToggleFlashlight")) {
				Method setFlashlightBrightness = null;
				Object svc = null;
				
				try {
					Object HTCHW = Class.forName("android.os.ServiceManager").getMethod("getService", new Class[] { String.class }).invoke(null, new Object[] { "htchardware" });
					Method HTCHWInterface = Class.forName("android.os.IHtcHardwareService$Stub").getMethod("asInterface", new Class[] { IBinder.class });
					Object[] paramArray = new Object[1];
					paramArray[0] = ((IBinder)HTCHW);
					svc = HTCHWInterface.invoke(null, paramArray);
					Class<?> svcClass = svc.getClass();
					Class<?>[] paramArray2 = new Class[1];
					paramArray2[0] = Integer.TYPE;
					setFlashlightBrightness = svcClass.getMethod("setFlashlightBrightness", paramArray2);
				} catch (Exception e) {
					e.printStackTrace();
					setFlashlightBrightness = null;
				}
				
				if (setFlashlightBrightness != null)
				try {
					Object[] paramArray = new Object[1];
					if (mCurrentLEDLevel == 0) {
						paramArray[0] = 125;
						mCurrentLEDLevel = 125;
						Toast.makeText(context, modRes.getString(R.string.toggle_flash_low), Toast.LENGTH_SHORT).show();
					} else if (mCurrentLEDLevel == 125) {
						paramArray[0] = 126;
						mCurrentLEDLevel = 126;
						Toast.makeText(context, modRes.getString(R.string.toggle_flash_med), Toast.LENGTH_SHORT).show();
					} else if (mCurrentLEDLevel == 126) {
						paramArray[0] = 127;
						mCurrentLEDLevel = 127;
						Toast.makeText(context, modRes.getString(R.string.toggle_flash_high), Toast.LENGTH_SHORT).show();
					} else if (mCurrentLEDLevel == 127) {
						paramArray[0] = 0;
						mCurrentLEDLevel = 0;
						Toast.makeText(context, modRes.getString(R.string.toggle_flash_off), Toast.LENGTH_SHORT).show();
					}
					setFlashlightBrightness.invoke(svc, paramArray);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (action.equals("com.langerhans.one.mods.action.ToggleMobileData")) {
				try {
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
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	private static String beforeEnable;
	
	private static void removeTask(Context context, boolean isKillWithoutDialog) {
		try {
			final ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
			final List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
			final Method removeTask = am.getClass().getMethod("removeTask", new Class[] { int.class, int.class });
			final Method forceStopPackage = am.getClass().getMethod("forceStopPackage", new Class[] { String.class });
			removeTask.setAccessible(true);
			forceStopPackage.setAccessible(true);
			String thisPkg = taskInfo.get(0).topActivity.getPackageName();
			
			boolean isLauncher = false;
			if (!isKillWithoutDialog) {
				PackageManager pm = context.getPackageManager();
				Intent intent_home = new Intent(Intent.ACTION_MAIN);
				intent_home.addCategory(Intent.CATEGORY_HOME);
				intent_home.addCategory(Intent.CATEGORY_DEFAULT);
				List<ResolveInfo> launcherList = pm.queryIntentActivities(intent_home, 0);
				
				for (ResolveInfo launcher: launcherList)
				if (launcher.activityInfo.packageName.equals(thisPkg)) isLauncher = true;
			}
			
			if (isLauncher && !isKillWithoutDialog) {
				Intent intent_dilaog = new Intent();
				intent_dilaog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent_dilaog.setClassName("com.langerhans.one", "com.langerhans.one.DimmedActivity");
				intent_dilaog.putExtra("dialogType", 2);
				context.startActivity(intent_dilaog);
			} else {
				// Removes from recents also
				removeTask.invoke(am, Integer.valueOf(taskInfo.get(0).id), Integer.valueOf(1));
				// Force closes all package parts 
				forceStopPackage.invoke(am, thisPkg);				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void turnGPSOn(Context context) {
		beforeEnable = Settings.Secure.getString (context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		String newSet;
		if (beforeEnable.equals(""))
			newSet = LocationManager.GPS_PROVIDER;
		else
			newSet = String.format("%s,%s", beforeEnable, LocationManager.GPS_PROVIDER);
		
		try {
			Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newSet);	
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

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
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setupPWM() {
		try {
			final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);

			findAndHookMethod(clsPWM, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					mPWM = param.thisObject;
					mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
					Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
		            IntentFilter intentfilter = new IntentFilter();
		            
		            // Actions
		            intentfilter.addAction("com.langerhans.one.mods.action.GoToSleep");
		            intentfilter.addAction("com.langerhans.one.mods.action.LockDevice");
		            intentfilter.addAction("com.langerhans.one.mods.action.TakeScreenshot");
		            intentfilter.addAction("com.langerhans.one.mods.action.killForegroundApp");
		            intentfilter.addAction("com.langerhans.one.mods.action.killForegroundAppShedule");
		            
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
				showeqs.setAccessible(true);
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
        	else if (action == 5) pkgAppName = XMain.pref.getString("pref_key_prism_swiperight_app", not_selected);
        	else if (action == 6) pkgAppName = XMain.pref.getString("pref_key_prism_swipeleft_app", not_selected);
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
	
	public static boolean killForegroundApp(Context context) {
        try {
        	Intent intent = new Intent();
            intent.setAction("com.langerhans.one.mods.action.killForegroundApp");
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
package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.findClass;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import com.sensetoolbox.six.utils.GlobalActions;
import com.sensetoolbox.six.utils.Helpers;
import com.sensetoolbox.six.utils.StructInputEvent;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class WakeGesturesMods {
	private static Object mPauseLock = new Object();
	private static boolean mPaused = false;
	private static Object mEasyAccessCtrl = null;
	private static ClassLoader mLSClassLoader = null;
	private static int mCurrentLEDLevel = 0;
	private static WakeLock mWakeLock;
	private static BroadcastReceiver mBRLS = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			try {
				String action = intent.getAction();
				if (action.equals("com.sensetoolbox.six.MotionGesture")) {
					int gesture = intent.getIntExtra("motion_gesture", 0);
					if (mEasyAccessCtrl != null && mLSClassLoader != null)
					switch (gesture) {
						case 1:
							XposedHelpers.callMethod(mEasyAccessCtrl, "snapToPage", XposedHelpers.getStaticObjectField(findClass("com.htc.lockscreen.keyguard.SlidingChallengeLayout.ScrollDirection", mLSClassLoader), "ScrollToUp"));
							break;
						case 2:
							XposedHelpers.callMethod(mEasyAccessCtrl, "dismissKeyguard");
							XposedHelpers.callMethod(mEasyAccessCtrl, "snapToPage", XposedHelpers.getStaticObjectField(findClass("com.htc.lockscreen.keyguard.SlidingChallengeLayout.ScrollDirection", mLSClassLoader), "ScrollToRight"));
							break;
						case 3:
							//XposedHelpers.callMethod(mEasyAccessCtrl, "snapToPage", XposedHelpers.getStaticObjectField(findClass("com.htc.lockscreen.keyguard.SlidingChallengeLayout.ScrollDirection", mLSClassLoader), "ScrollToLeft"));
							XposedHelpers.callMethod(mEasyAccessCtrl, "dismissKeyguard");
							Intent i = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("action", -1);
							XposedHelpers.callMethod(mEasyAccessCtrl, "launchActivityfromEasyAccess", i, true);
							break;
						case 4:
							boolean isCT = (Boolean)XposedHelpers.callStaticMethod(findClass("com.htc.lockscreen.util.MyProjectSettings", mLSClassLoader), "isCT");
							boolean isCU = (Boolean)XposedHelpers.callStaticMethod(findClass("com.htc.lockscreen.util.MyProjectSettings", mLSClassLoader), "isCU");
							boolean isCHS = (Boolean)XposedHelpers.callStaticMethod(findClass("com.htc.lockscreen.util.MyProjectSettings", mLSClassLoader), "isCHS");
							if (!isCT && !isCU && !isCHS)
								XposedHelpers.callMethod(mEasyAccessCtrl, "startHtcSpeakerLaucher");
							else
								XposedHelpers.callMethod(mEasyAccessCtrl, "snapToPage", XposedHelpers.getStaticObjectField(findClass("com.htc.lockscreen.keyguard.SlidingChallengeLayout.ScrollDirection", mLSClassLoader), "ScrollToBottom"));
							break;
						case 5:
							launchApp(context, intent.getIntExtra("launch_app", 0));
							break;
						case 6:
							XposedHelpers.callMethod(mEasyAccessCtrl, "dismissKeyguard");
							Intent i2 = new Intent("com.htc.intent.action.HTC_Prism_AllApps").addCategory("android.intent.category.DEFAULT").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							XposedHelpers.callMethod(mEasyAccessCtrl, "launchActivityfromEasyAccess", i2, true);
							break;
						case 7:
							launchShortcut(context, intent.getIntExtra("launch_shortcut", 0));
							break;
					}
				}
			} catch(Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
	
	private static void doWakeUp(Object thisObject, long atTime) {
		PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(thisObject, "mPowerManager");
		if (mPowerManager != null) {
			mPowerManager.wakeUp(atTime);
			WakeLock wl = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "S6T WakeUpSleepy");
			wl.acquire(1000);
		}
	}
	
	private static void sendLockScreenIntent(Context mContext, int action) {
		if (mContext != null) {
			Intent intent = new Intent("com.sensetoolbox.six.MotionGesture");
			intent.putExtra("motion_gesture", action);
			mContext.sendBroadcast(intent);
		}
	}
	
	private static void sendLockScreenIntentLaunchApp(Context mContext, int input_val) {
		if (mContext != null) {
			Intent intent = new Intent("com.sensetoolbox.six.MotionGesture");
			intent.putExtra("motion_gesture", 5);
			intent.putExtra("launch_app", input_val);
			mContext.sendBroadcast(intent);
		}
	}
	
	private static void sendLockScreenIntentLaunchShortcut(Context mContext, int input_val) {
		if (mContext != null) {
			Intent intent = new Intent("com.sensetoolbox.six.MotionGesture");
			intent.putExtra("motion_gesture", 7);
			intent.putExtra("launch_shortcut", input_val);
			mContext.sendBroadcast(intent);
		}
	}
	
	private static void sendLockScreenIntentOpenAppDrawer(Context mContext) {
		if (mContext != null) {
			Intent intent = new Intent("com.sensetoolbox.six.MotionGesture");
			intent.putExtra("motion_gesture", 6);
			mContext.sendBroadcast(intent);
		}
	}
	
	private static String getPkgAppName(int action) {
		XMain.pref.reload();
		if (Helpers.isM8() || Helpers.isE8()) {
			switch (action) {
				case 2: case 24: return XMain.pref.getString("pref_key_wakegest_swipeup_app", null);
				case 3: case 25: return XMain.pref.getString("pref_key_wakegest_swipedown_app", null);
				case 4: case 26: return XMain.pref.getString("pref_key_wakegest_swipeleft_app", null);
				case 5: case 27: return XMain.pref.getString("pref_key_wakegest_swiperight_app", null);
				case 6: return XMain.pref.getString("pref_key_wakegest_logo2wake_app", null); //volume keys
				case 15: return XMain.pref.getString("pref_key_wakegest_dt2w_app", null);
			}
		} else {
			switch (action) {
				case 1: return XMain.pref.getString("pref_key_wakegest_swiperight_app", null);
				case 2: return XMain.pref.getString("pref_key_wakegest_swipeleft_app", null);
				case 3: return XMain.pref.getString("pref_key_wakegest_swipeup_app", null);
				case 4: return XMain.pref.getString("pref_key_wakegest_swipedown_app", null);
				case 5: return XMain.pref.getString("pref_key_wakegest_dt2w_app", null);
				case 6: return XMain.pref.getString("pref_key_wakegest_logo2wake_app", null);
			}
		}
		return null;
	}
	
	public static void launchApp(Context ctx, int action) {
		try {
			String pkgAppName = getPkgAppName(action);
			if (pkgAppName != null) {
				String[] pkgAppArray = pkgAppName.split("\\|");
				
				if (mEasyAccessCtrl == null) XposedBridge.log("Failed to start app using wake gesture!"); else
				if (pkgAppArray[0].equals("com.htc.camera")) {
					XposedHelpers.callMethod(mEasyAccessCtrl, "launchCamera", ctx, true);
				} else {
					Intent appIntent = new Intent();
					appIntent.setClassName(pkgAppArray[0], pkgAppArray[1]);
					appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					XposedHelpers.callMethod(mEasyAccessCtrl, "dismissKeyguard");
					XposedHelpers.callMethod(mEasyAccessCtrl, "launchActivityfromEasyAccess", appIntent, true);
				}
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static String getShortcutIntent(int action) {
		XMain.pref.reload();
		if (Helpers.isM8() || Helpers.isE8()) {
			switch (action) {
				case 2: case 24: return XMain.pref.getString("pref_key_wakegest_swipeup_shortcut_intent", null);
				case 3: case 25: return XMain.pref.getString("pref_key_wakegest_swipedown_shortcut_intent", null);
				case 4: case 26: return XMain.pref.getString("pref_key_wakegest_swipeleft_shortcut_intent", null);
				case 5: case 27: return XMain.pref.getString("pref_key_wakegest_swiperight_shortcut_intent", null);
				case 6: return XMain.pref.getString("pref_key_wakegest_logo2wake_shortcut_intent", null); //volume keys
				case 15: return XMain.pref.getString("pref_key_wakegest_dt2w_shortcut_intent", null);
			}
		} else {
			switch (action) {
				case 1: return XMain.pref.getString("pref_key_wakegest_swiperight_shortcut_intent", null);
				case 2: return XMain.pref.getString("pref_key_wakegest_swipeleft_shortcut_intent", null);
				case 3: return XMain.pref.getString("pref_key_wakegest_swipeup_shortcut_intent", null);
				case 4: return XMain.pref.getString("pref_key_wakegest_swipedown_shortcut_intent", null);
				case 5: return XMain.pref.getString("pref_key_wakegest_dt2w_shortcut_intent", null);
				case 6: return XMain.pref.getString("pref_key_wakegest_logo2wake_shortcut_intent", null);
			}
		}
		return null;
	}
	
	public static void launchShortcut(Context ctx, int action) {
		try {
			if (mEasyAccessCtrl == null) XposedBridge.log("Failed to start app using wake gesture!"); else {
				String intentString = getShortcutIntent(action);
				if (intentString != null) {
					Intent shortcutIntent = Intent.parseUri(intentString, 0);
					shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					XposedHelpers.callMethod(mEasyAccessCtrl, "dismissKeyguard");
					XposedHelpers.callMethod(mEasyAccessCtrl, "launchActivityfromEasyAccess", shortcutIntent, true);
				}
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	@SuppressLint("Wakelock")
	public static void executeActionFor(MethodHookParam param, String prefName, long event_time, int action) {
		if (prefName != null) {
			Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
			long curTime = SystemClock.uptimeMillis();
			long event_time_local = event_time;
			if (event_time_local > curTime) event_time_local = curTime;
			boolean isHaptic = true;
			switch (Integer.parseInt(XMain.pref.getString(prefName, "0"))) {
				case 0: isHaptic = false; break;
				case 1: doWakeUp(param.thisObject, event_time_local); break;
				case 2: doWakeUp(param.thisObject, event_time_local); sendLockScreenIntent(mContext, 1); break;
				case 3: doWakeUp(param.thisObject, event_time_local); sendLockScreenIntent(mContext, 2); break;
				case 4: doWakeUp(param.thisObject, event_time_local); sendLockScreenIntent(mContext, 3); break;
				case 5: doWakeUp(param.thisObject, event_time_local); sendLockScreenIntent(mContext, 4); break;
				case 6: doWakeUp(param.thisObject, event_time_local); sendLockScreenIntentOpenAppDrawer(mContext); break;
				case 7:
					PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
					if (mWakeLock == null) mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "S6T GestureFlash");
					if (mCurrentLEDLevel == 0) {
						mCurrentLEDLevel = 127;
						if (!mWakeLock.isHeld()) mWakeLock.acquire(600000);
					} else {
						mCurrentLEDLevel = 0;
						if (mWakeLock.isHeld()) mWakeLock.release();
					}
					GlobalActions.setFlashlight(mCurrentLEDLevel);
					break;
				case 8: doWakeUp(param.thisObject, event_time_local); GlobalActions.expandNotifications(mContext); break;
				case 9: doWakeUp(param.thisObject, event_time_local); GlobalActions.expandEQS(mContext); break;
				case 10: doWakeUp(param.thisObject, event_time_local); sendLockScreenIntentLaunchApp(mContext, action); break;
				case 14: doWakeUp(param.thisObject, event_time_local); sendLockScreenIntentLaunchShortcut(mContext, action); break;
				case 11:
					GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_DOWN, 85));
					GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_UP, 85));
					break;
				case 12:
					GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_DOWN, 87));
					GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_UP, 87));
					break;
				case 13:
					GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_DOWN, 88));
					GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_UP, 88));
					break;
			}

			if (isHaptic && XMain.pref.getBoolean("pref_key_wakegest_haptic", false) && Helpers.getHTCHaptic(mContext)) {
				Vibrator vibe = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
				vibe.vibrate(30);
			}
		}
	}
	
	static final char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	
	private static String getEventDevice() {
		String eventnum = "4";
		File[] files = (new File("/sys/class/input")).listFiles();
		for (File fl: files)
		if (fl.getName().contains("input")) try {
			File inputname = new File(fl.getAbsolutePath() + "/name");
			if (inputname.exists())
			try (BufferedReader br = new BufferedReader(new FileReader(inputname))) {
				String line = br.readLine();
				if (line != null && line.trim().equals("wake_gesture")) {
					String tmp = fl.getName().replace("input", "");
					Integer.parseInt(tmp);
					eventnum = tmp;
					break;
				}
			}
		} catch (Throwable t) {}
		return eventnum;
	}
	
	public static Thread createThread(final MethodHookParam param) throws Throwable {
		Thread th = new Thread(new Runnable() {
			File file = new File("/dev/input/event" + getEventDevice());
			final byte[] event = new byte[4 * 2 + 2 + 2 + 4];
			BufferedInputStream bfin = new BufferedInputStream(new FileInputStream(file));
			StructInputEvent input_event = null;
			
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
				while (true) try {
					if (bfin.read(event) > 0) {
						input_event = new StructInputEvent(event);
						//XposedBridge.log("event3: " + bytesToHex(event));
						//XposedBridge.log("[S6T @ " + String.valueOf(SystemClock.uptimeMillis()) + "] input_event: type " + input_event.type_name + " code " + input_event.code_name + " value " + String.valueOf(input_event.value));
						if (input_event != null && input_event.type == 0x02 && input_event.code == 0x0b) {
							XMain.pref.reload();
							if (XMain.pref.getBoolean("wake_gestures_active", false)) {
								String prefName = null;
								switch (input_event.value) {
									case 1: prefName = "pref_key_wakegest_swiperight"; break;
									case 2: prefName = "pref_key_wakegest_swipeleft"; break;
									case 3: prefName = "pref_key_wakegest_swipeup"; break;
									case 4: prefName = "pref_key_wakegest_swipedown"; break;
									case 5: prefName = "pref_key_wakegest_dt2w"; break;
									case 6: prefName = "pref_key_wakegest_logo2wake"; break;
								}
								executeActionFor(param, prefName, Math.round(1000 * input_event.timeval_sec + input_event.timeval_usec / 1000), input_event.value);
							}
						}
					} else Thread.sleep(100);
					
					synchronized (mPauseLock) {
						while (mPaused) try {
							mPauseLock.wait();
						} catch (Exception e) {}
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
					try { if (bfin != null) bfin.close(); } catch (Exception e) {}
					break;
				}
			}
		});
		th.setPriority(Thread.MAX_PRIORITY);
		th.setName("S6T_WakeGestures");
		XposedHelpers.setAdditionalInstanceField(param.thisObject, "eventXthread", th);
		return th;
	}
	
	public static void execHook_InitListener() {
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "screenTurnedOff", int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Thread th = (Thread)XposedHelpers.getAdditionalInstanceField(param.thisObject, "eventXthread");
				PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
				if (th != null) {
					if (!th.isAlive()) {
						try {
							th.start();
						} catch (Exception e) {
							th.interrupt();
							XposedBridge.log("Resetting gesture listener thread...");
							createThread(param).start();
						}
					} else if (!mPowerManager.isScreenOn()) synchronized (mPauseLock) {
						mPaused = false;
						mPauseLock.notifyAll();
					}
				} else createThread(param).start();
			}
		});
		
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "finishScreenTurningOn", "android.view.WindowManagerPolicy.ScreenOnListener", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				if (mCurrentLEDLevel > 0) {
					mCurrentLEDLevel = 0;
					GlobalActions.setFlashlight(0);
				}
				if (mWakeLock != null && mWakeLock.isHeld()) mWakeLock.release();
				Thread th = (Thread)XposedHelpers.getAdditionalInstanceField(param.thisObject, "eventXthread");
				if (th != null) {
					PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
					if (mPowerManager.isScreenOn()) synchronized (mPauseLock) {
						mPaused = true;
					}
				}
			}
		});
		
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				createThread(param);
			}
		});
	}
	
	public static void execHook_LockScreenGestures(final LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.htc.lockscreen.ctrl.LSState", lpparam.classLoader, "init", Context.class, Context.class, "com.htc.lockscreen.util.LockUtils", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					mEasyAccessCtrl = XposedHelpers.getObjectField(param.thisObject, "mEasyAccessCtrl");
					Context mSysContext = (Context)param.args[0];
					mLSClassLoader = lpparam.classLoader;
					if (mSysContext != null) {
						IntentFilter intentfilter = new IntentFilter();
						intentfilter.addAction("com.sensetoolbox.six.MotionGesture");
						mSysContext.registerReceiver(mBRLS, intentfilter);
						if (!Helpers.isM8() && !Helpers.isE8()) {
							XposedHelpers.setBooleanField(mEasyAccessCtrl, "mIsEnableEasyAccess", true);
							XposedHelpers.setBooleanField(mEasyAccessCtrl, "mIsEnableQuickCall", true);
						}
					} else XposedBridge.log("[S6T] mSysContext == null");
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		
		if (!Helpers.isM8() && !Helpers.isE8()) {
			XposedHelpers.findAndHookMethod("com.htc.lockscreen.ctrl.SettingObserver", lpparam.classLoader, "isEnableEasyAccess", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					param.setResult(true);
				}
			});
			
			XposedHelpers.findAndHookMethod("com.htc.lockscreen.ctrl.SettingObserver", lpparam.classLoader, "isEnableQuickCall", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					param.setResult(true);
				}
			});
		}
	}
	
	public static void execHook_EasyAccessService(LoadPackageParam lpparam) {
		XposedHelpers.findAndHookMethod("com.htc.sense.easyaccessservice.SensorHubService", lpparam.classLoader, "onHtcGestureMotion", int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				int j = (Integer)param.args[1];
				XMain.pref.reload();
				if (XMain.pref.getBoolean("wake_gestures_active", false)) {
					String prefName = null;
					switch (j) {
						case 3: case 25: prefName = "pref_key_wakegest_swipedown"; break;
						case 15: prefName = "pref_key_wakegest_dt2w"; break;
						case 6: prefName = "pref_key_wakegest_logo2wake"; break; // this is a volume keys
						case 5: case 27: prefName = "pref_key_wakegest_swiperight"; break;
						case 4: case 26: prefName = "pref_key_wakegest_swipeleft"; break;
						case 2: case 24: prefName = "pref_key_wakegest_swipeup"; break;
					}
					executeActionFor(param, prefName, SystemClock.uptimeMillis(), j);
					param.setResult(null);
				}
			}
		});
	}
}

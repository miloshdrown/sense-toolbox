package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.htc.preference.HtcPreferenceFrameLayout.LayoutParams;
import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcListItem;
import com.htc.widget.HtcListItem2LineText;
import com.htc.widget.HtcListItemColorIcon;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.GlobalActions;
import com.sensetoolbox.six.utils.Helpers;
import com.sensetoolbox.six.utils.StructInputEvent;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XModuleResources;
import android.os.Binder;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
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
	private static BroadcastReceiver mBRLS = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			try {
				String action = intent.getAction();
				if (action.equals("com.sensetoolbox.six.MotionGesture")) {
					int gesture = intent.getIntExtra("motion_gesture", 0);
					String sdClass = "com.htc.lockscreen.keyguard.SlidingChallengeLayout.ScrollDirection";
					if (Helpers.isLP()) sdClass = "com.htc.lockscreen.ctrl.EasyAccessCtrl.ScrollDirection";
					if (mEasyAccessCtrl != null && mLSClassLoader != null)
					switch (gesture) {
						case 1:
							XposedHelpers.callMethod(mEasyAccessCtrl, "snapToPage", XposedHelpers.getStaticObjectField(findClass(sdClass, mLSClassLoader), "ScrollToUp"));
							break;
						case 2:
							XposedHelpers.callMethod(mEasyAccessCtrl, "dismissKeyguard");
							XposedHelpers.callMethod(mEasyAccessCtrl, "snapToPage", XposedHelpers.getStaticObjectField(findClass(sdClass, mLSClassLoader), "ScrollToRight"));
							break;
						case 3:
							//XposedHelpers.callMethod(mEasyAccessCtrl, "snapToPage", XposedHelpers.getStaticObjectField(findClass(sdClass, mLSClassLoader), "ScrollToLeft"));
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
								XposedHelpers.callMethod(mEasyAccessCtrl, "snapToPage", XposedHelpers.getStaticObjectField(findClass(sdClass, mLSClassLoader), "ScrollToBottom"));
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
			XposedHelpers.callMethod(mPowerManager, "wakeUp", atTime);
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
		if (Helpers.isEight()) {
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
					XposedHelpers.callMethod(mEasyAccessCtrl, "launchCamera", ctx, false);
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
		if (Helpers.isEight()) {
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
					if (Helpers.mWakeLock == null) Helpers.mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "S6T Flashlight");
					if (Helpers.mFlashlightLevel == 0 || !Helpers.mWakeLock.isHeld()) {
						Helpers.mFlashlightLevel = 127;
						if (!Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.acquire(600000);
					} else {
						Helpers.mFlashlightLevel = 0;
						if (Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.release();
					}
					GlobalActions.setFlashlight(Helpers.mFlashlightLevel);
					break;
				case 8: doWakeUp(param.thisObject, event_time_local); GlobalActions.expandNotifications(mContext); break;
				case 9: doWakeUp(param.thisObject, event_time_local); GlobalActions.expandEQS(mContext); break;
				case 10: doWakeUp(param.thisObject, event_time_local); sendLockScreenIntentLaunchApp(mContext, action); break;
				case 14: doWakeUp(param.thisObject, event_time_local); sendLockScreenIntentLaunchShortcut(mContext, action); break;
				case 11:
					GlobalActions.sendMediaButton(mContext, new KeyEvent(KeyEvent.ACTION_DOWN, 85));
					GlobalActions.sendMediaButton(mContext, new KeyEvent(KeyEvent.ACTION_UP, 85));
					break;
				case 12:
					GlobalActions.sendMediaButton(mContext, new KeyEvent(KeyEvent.ACTION_DOWN, 87));
					GlobalActions.sendMediaButton(mContext, new KeyEvent(KeyEvent.ACTION_UP, 87));
					break;
				case 13:
					GlobalActions.sendMediaButton(mContext, new KeyEvent(KeyEvent.ACTION_DOWN, 88));
					GlobalActions.sendMediaButton(mContext, new KeyEvent(KeyEvent.ACTION_UP, 88));
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
					if (Thread.currentThread().isInterrupted()) break;
					if (bfin.read(event) > 0 && !isOnLockdown) {
						input_event = new StructInputEvent(event);
						//XposedBridge.log("event: " + bytesToHex(event));
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
					try {
						if (bfin != null) bfin.close();
					} catch (Exception e) {}
					break;
				}
			}
		});
		th.setPriority(Thread.MAX_PRIORITY);
		th.setName("S6T_WakeGestures");
		XposedHelpers.setAdditionalInstanceField(param.thisObject, "eventXthread", th);
		return th;
	}
	
	public static boolean isOnLockdown = false;
	public static boolean lockOnNextScrOff = false;
	public static List<Integer> sequence = new ArrayList<Integer>();
	public static int touchScreenWidth = 1620;
	public static int touchScreenHeight = 2880;
	private static BroadcastReceiver mBRLD = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			goToSleep(context);
		}
	};
	
	public static void setLockdown(Context ctx, boolean isOn) {
		isOnLockdown = isOn;
		if (ctx != null) try {
			long ident = Binder.clearCallingIdentity();
			try {
				Settings.System.putString(ctx.getContentResolver(), "device_locked_state", Boolean.toString(isOn));
			} finally {
				Binder.restoreCallingIdentity(ident);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		
		String val;
		if (isOn) val = "1"; else val = "0";
		CommandCapture command = new CommandCapture(0, "echo " + val + " > /sys/android_touch/knockcode");
		try {
			RootTools.getShell(true).add(command);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void goToSleep(Context mContext) {
		try {
			lockOnNextScrOff = true;
			XposedHelpers.callMethod((PowerManager)mContext.getSystemService(Context.POWER_SERVICE), "goToSleep", SystemClock.uptimeMillis());
		} catch(Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void fillTouchscreenDimen(String device, final String event) {
		CommandCapture command = new CommandCapture(0, "getevent -p /dev/input/event" + device + " 2>/dev/null | grep " + event + " | cut -d ',' -f 3 | cut -d ' ' -f 3") {
			int lineCount = 0;
			
			@Override
			public void commandOutput(int id, String line) {
				if (lineCount > 0) return;
				try {
					if (event.equals("0035")) touchScreenWidth = Integer.parseInt(line.trim());
					if (event.equals("0036")) touchScreenHeight = Integer.parseInt(line.trim());
				} catch (Exception e) {
					e.printStackTrace();
				}
				lineCount++;
			}
		};
		try {
			RootTools.getShell(false).add(command);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static String getTouchscreenDevice() {
		String eventnum = "3";
		File[] files = (new File("/sys/class/input")).listFiles();
		for (File fl: files)
		if (fl.getName().contains("input")) try {
			File inputname = new File(fl.getAbsolutePath() + "/name");
			if (inputname.exists())
			try (BufferedReader br = new BufferedReader(new FileReader(inputname))) {
				String line = br.readLine();
				if (line != null && line.trim().contains("touchscreen")) {
					String tmp = fl.getName().replace("input", "");
					Integer.parseInt(tmp);
					eventnum = tmp;
					break;
				}
			}
		} catch (Throwable t) {}
		return eventnum;
	}
	
	public static Runnable clearSequence = new Runnable() {
		@Override
		public void run() {
			sequence.clear();
		}
	};
	
	public static Thread createTouchscreenThread(final MethodHookParam param) throws Throwable {
		sequence.clear();
		String device = getTouchscreenDevice();
		fillTouchscreenDimen(device, "0035");
		fillTouchscreenDimen(device, "0036");

		Thread th_touch = new Thread(new Runnable() {
			final File file_touch = new File("/dev/input/event" + getTouchscreenDevice());
			final byte[] event_touch = new byte[4 * 2 + 2 + 2 + 4];
			BufferedInputStream bfin_touch = new BufferedInputStream(new FileInputStream(file_touch));
			StructInputEvent input_event_touch = null;
			int slot = 0;
			int tapX = 0;
			int tapY = 0;
			boolean isTapped = false;
			
			@Override
			@SuppressWarnings("deprecation")
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				setLockdown(mContext, true);
				Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
				PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
				while (true) try {
					if (Thread.currentThread().isInterrupted()) break;
					if (bfin_touch.read(event_touch) > 0 && !pm.isScreenOn()) {
						input_event_touch = new StructInputEvent(event_touch);
						//XposedBridge.log("event_touch: " + bytesToHex(event_touch));
						//XposedBridge.log("[S6T @ " + String.valueOf(SystemClock.uptimeMillis()) + "] input_event: type " + input_event_touch.type_name + " code " + input_event_touch.code_name + " value " + String.valueOf(input_event_touch.value));
					
						if (input_event_touch.type == 0x03 && input_event_touch.code == 0x2f) slot = input_event_touch.value;
						if (slot == 0) {
							if (input_event_touch.type == 0x03 && input_event_touch.code == 0x39)
								if (input_event_touch.value == -1)
									isTapped = false;
								else
									isTapped = true;
							
							if (isTapped) {
								if (input_event_touch.type == 0x03) {
									if (input_event_touch.code == 0x35) tapX = input_event_touch.value;
									if (input_event_touch.code == 0x36) tapY = input_event_touch.value;
								}
								if (input_event_touch.type == 0x00 && input_event_touch.code == 0x00) {
									isTapped = false;
									XposedBridge.log(String.valueOf(tapX) + ":" + String.valueOf(tapY) + " max " + String.valueOf(touchScreenWidth) + ":" + String.valueOf(touchScreenHeight));
									if (tapX < touchScreenWidth/2 && tapY < touchScreenHeight/2) sequence.add(1);
									else if (tapX > touchScreenWidth/2 && tapY < touchScreenHeight/2) sequence.add(2);
									else if (tapX < touchScreenWidth/2 && tapY > touchScreenHeight/2) sequence.add(3);
									else if (tapX > touchScreenWidth/2 && tapY > touchScreenHeight/2) sequence.add(4);
									
									ArrayList<String> prefSequence = new ArrayList<String>(Arrays.asList(XMain.pref.getString("touch_lock_sequence", "").split(",")));
									String seq;
									String seqPart;
									if (sequence.size() >= prefSequence.size()) {
										seq = TextUtils.join(" ", sequence.subList(sequence.size() - prefSequence.size(), sequence.size())).trim();
										seqPart = TextUtils.join(" ", sequence.subList(sequence.size() - prefSequence.size() + 1, sequence.size())).trim();
									} else {
										seq = TextUtils.join(" ", sequence).trim();
										seqPart = seq;
									}
									
									String prefSeq = TextUtils.join(" ", prefSequence).trim();
									String prefSeqPart = TextUtils.join(" ", prefSequence.subList(0, prefSequence.size() - 1)).trim();
									XposedBridge.log(seq + "   !   " + prefSeq);
									if (seqPart.equals(prefSeqPart)) {
										WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "S6T TouchLockAttempt");
										wl.acquire(2000);
									}
									
									if (seq.equals(prefSeq)) {
										setLockdown(mContext, false);
										doWakeUp(param.thisObject, SystemClock.uptimeMillis());
										if (Helpers.getHTCHaptic(mContext)) {
											Vibrator vibe = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
											vibe.vibrate(50);
										}
									} else if (mHandler != null) {
										mHandler.removeCallbacks(clearSequence);
										mHandler.postDelayed(clearSequence, 2000);
									}
								}
							}
						}
					} else Thread.sleep(100);
				} catch (Throwable t) {
					try {
						if (bfin_touch != null) bfin_touch.close();
					} catch (Exception e) {}
					break;
				}
			}
		});
		th_touch.setPriority(Thread.MAX_PRIORITY);
		th_touch.setName("S6T_TouchLock");
		XposedHelpers.setAdditionalInstanceField(param.thisObject, "eventXthreadtouch", th_touch);
		return th_touch;
	}
	
	@SuppressWarnings("deprecation")
	public static void execHook_InitListener() {
		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Thread th = (Thread)XposedHelpers.getAdditionalInstanceField(param.thisObject, "eventXthread");
				if (th != null) {
					PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
					if (!th.isAlive()) {
						try {
							th.start();
						} catch (Exception e) {
							th.interrupt();
							th = null;
							XposedBridge.log("Resetting gesture listener thread...");
							createThread(param).start();
						}
					} else if (!mPowerManager.isScreenOn()) synchronized (mPauseLock) {
						mPaused = false;
						mPauseLock.notifyAll();
					}
				} else createThread(param).start();
			}
		};
		
		Object[] argsAndHook = { int.class, hook };
		if (Helpers.isLP()) argsAndHook = new Object[] { hook };
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "screenTurnedOff", argsAndHook);
		
		XC_MethodHook hook2 = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				if (Helpers.mFlashlightLevel > 0) {
					Helpers.mFlashlightLevel = 0;
					GlobalActions.setFlashlight(0);
				}
				if (Helpers.mWakeLock != null && Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.release();
				
				PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
				Thread th = (Thread)XposedHelpers.getAdditionalInstanceField(param.thisObject, "eventXthread");
				if (th != null && mPowerManager.isScreenOn()) synchronized (mPauseLock) {
					mPaused = true;
				}
			}
		};
		
		Object[] argsAndHook2 = { "android.view.WindowManagerPolicy.ScreenOnListener", hook2 };
		if (Helpers.isLP()) argsAndHook2 = new Object[] { hook2 };
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "finishScreenTurningOn", argsAndHook2);
		
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				createThread(param);
			}
		});
		
		if (Helpers.isLP())
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "systemBooted", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				XMain.pref.reload();
				XposedBridge.log("[S6T] Wake gestures activate on boot...");
				if (XMain.pref.getBoolean("wake_gestures_active", false)) {
					final Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
					if (mHandler != null)
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							XposedBridge.log("[S6T] Wake gestures activated!");
							Helpers.setWakeGestures(true);
							if (!Helpers.getWakeGestures().equals("1")) mHandler.postDelayed(this, 1000);
						}
					});
				}
			}
		});
	}
	
	public static void execHook_InitTouchLockListener() {
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				IntentFilter intentfilter = new IntentFilter();
				intentfilter.addAction("com.sensetoolbox.six.mods.action.LockDownDevice");
				mContext.registerReceiver(mBRLD, intentfilter);
			}
		});
		
		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Thread th_touch = (Thread)XposedHelpers.getAdditionalInstanceField(param.thisObject, "eventXthreadtouch");
				if (th_touch != null && !th_touch.isInterrupted()) {
					th_touch.interrupt();
					th_touch = null;
				}
				if (lockOnNextScrOff) {
					lockOnNextScrOff = false;
					XMain.pref.reload();
					if (XMain.pref.getBoolean("touch_lock_active", false) && !XMain.pref.getString("touch_lock_sequence", "").trim().equals("")) {
						createTouchscreenThread(param).start();
					} else
						setLockdown((Context)XposedHelpers.getObjectField(param.thisObject, "mContext"), false);
				}
			}
		};
		Object[] argsAndHook = { int.class, hook };
		if (Helpers.isLP()) argsAndHook = new Object[] { hook };
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "screenTurnedOff", argsAndHook);
		
		XC_MethodHook hook2 = new XC_MethodHook() {
			@Override
			@SuppressWarnings("deprecation")
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
				
				Thread th_touch = (Thread)XposedHelpers.getAdditionalInstanceField(param.thisObject, "eventXthreadtouch");
				if (th_touch != null && !th_touch.isInterrupted() && mPowerManager.isScreenOn()) {
					th_touch.interrupt();
					th_touch = null;
				}
				sequence.clear();
				lockOnNextScrOff = false;
				
				if (mPowerManager.isScreenOn()) {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					setLockdown(mContext, false);
				}
			}
		};
		Object[] argsAndHook2 = { "android.view.WindowManagerPolicy.ScreenOnListener", hook2 };
		if (Helpers.isLP()) argsAndHook2 = new Object[] { hook2 };
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "finishScreenTurningOn", argsAndHook2);
		
		XC_MethodHook hook3 = new XC_MethodHook() {
			@Override
			@SuppressWarnings("deprecation")
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
				if (isOnLockdown && !mPowerManager.isScreenOn()) param.setResult(0);
			}
		};
		Object[] argsAndHook3 = { KeyEvent.class, int.class, boolean.class, hook3 };
		if (Helpers.isLP()) argsAndHook3 = new Object[] { KeyEvent.class, int.class, hook3 };
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "interceptKeyBeforeQueueing", argsAndHook3);
		
		findAndHookMethod("com.android.internal.policy.impl.GlobalActions", null, "handleShow", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				final Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (mContext == null) return;
				
				XMain.pref.reload();
				if (!XMain.pref.getBoolean("touch_lock_active", false)) return;

				AbsListView.LayoutParams lp1 = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				HtcListItem listitem = new HtcListItem(mContext);
				listitem.setEnabled(true);
				listitem.setClickable(true);
				listitem.setLayoutParams(lp1);
				listitem.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						goToSleep(mContext);
					}
				});
				
				HtcListItemColorIcon lockImg = new HtcListItemColorIcon(mContext);
				//AbsListView.LayoutParams lp2 = new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				//lockImg.setLayoutParams(lp2);
				//lockImg.setPadding(0, 0, 0, Math.round(mContext.getResources().getDisplayMetrics().density * 40));
				XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
				lockImg.setColorIconImageDrawable(modRes.getDrawable(R.drawable.apm_touchlock));
				lockImg.setEnabled(true);
				
				HtcListItem2LineText lockTitle = new HtcListItem2LineText(mContext);
				lockTitle.setPrimaryText(Helpers.xl10n(modRes, R.string.various_touchlock_title));
				lockTitle.setSecondaryText(Helpers.xl10n(modRes, R.string.touchlock_power_summary));
				
				listitem.addView(lockImg);
				listitem.addView(lockTitle);
				HtcAlertDialog mDialog = (HtcAlertDialog)XposedHelpers.getObjectField(param.thisObject, "mDialog");
				mDialog.getListView().addFooterView(listitem);
			}
		});
		
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "systemBooted", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				XposedBridge.log("[S6T] Touch lock activate on boot!");
				final Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (mContext != null) {
					long ident = Binder.clearCallingIdentity();
					boolean shouldBeLocked = false;
					try {
						shouldBeLocked = Boolean.parseBoolean(Settings.System.getString(mContext.getContentResolver(), "device_locked_state"));
					} finally {
						Binder.restoreCallingIdentity(ident);
					}
					if (shouldBeLocked) {
						final Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
						if (mHandler != null) mHandler.post(new Runnable() {
							@Override
							@SuppressWarnings("deprecation")
							public void run() {
								XposedBridge.log("[S6T] Touch lock activated!");
								PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
								if (pm.isScreenOn()) {
									goToSleep(mContext);
									mHandler.postDelayed(this, 1000);
								}
							}
						});
					}
				}
			}
		});
	}
	
	public static void initGestures(LoadPackageParam lpparam, MethodHookParam param) {
		try {
			mEasyAccessCtrl = XposedHelpers.getObjectField(param.thisObject, "mEasyAccessCtrl");
			Context mSysContext = (Context)param.args[0];
			mLSClassLoader = lpparam.classLoader;
			if (mSysContext != null) {
				IntentFilter intentfilter = new IntentFilter();
				intentfilter.addAction("com.sensetoolbox.six.MotionGesture");
				mSysContext.registerReceiver(mBRLS, intentfilter);
				if (!Helpers.isEight()) {
					XposedHelpers.setBooleanField(mEasyAccessCtrl, "mIsEnableEasyAccess", true);
					XposedHelpers.setBooleanField(mEasyAccessCtrl, "mIsEnableQuickCall", true);
				}
			} else XposedBridge.log("[S6T] mSysContext == null");
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_InitTouchServerListener(LoadPackageParam lpparam) {
		try {
			XC_MethodHook hook = new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					boolean isScreenOn = false;
					if (Helpers.isLP())
						isScreenOn = (Boolean)XposedHelpers.callMethod(param.thisObject, "isInteractiveInternal");
					else
						isScreenOn = (Boolean)XposedHelpers.callMethod(param.thisObject, "isScreenOn");
					if (isOnLockdown && !isScreenOn) param.setResult(null);
				}
			};
			Object[] argsAndHook = { long.class, hook };
			if (Helpers.isLP()) argsAndHook = new Object[] { long.class, int.class, hook };
			findAndHookMethod("com.android.server.power.PowerManagerService", lpparam.classLoader, "wakeUpInternal", argsAndHook);
			
			XC_MethodHook hook2 = new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					boolean isScreenOn = false;
					if (Helpers.isLP())
						isScreenOn = (Boolean)XposedHelpers.callMethod(param.thisObject, "isInteractiveInternal");
					else
						isScreenOn = (Boolean)XposedHelpers.callMethod(param.thisObject, "isScreenOn");
					if (isOnLockdown && !isScreenOn) param.setResult(false);
				}
			};
			Object[] argsAndHook2 = { long.class, hook2 };
			if (Helpers.isLP()) argsAndHook2 = new Object[] { long.class, int.class, hook2 };
			findAndHookMethod("com.android.server.power.PowerManagerService", lpparam.classLoader, "wakeUpNoUpdateLocked", argsAndHook2);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_LockScreenGestures(final LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.htc.lockscreen.ctrl.LSState", lpparam.classLoader, "init", Context.class, Context.class, "com.htc.lockscreen.util.LockUtils", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					initGestures(lpparam, param);
				}
			});
		} catch (Throwable t1) {
			try {
				XposedHelpers.findAndHookMethod("com.htc.lockscreen.ctrl.LSState", lpparam.classLoader, "init", Context.class, Context.class, "com.android.internal.widget.LockPatternUtils", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
						initGestures(lpparam, param);
					}
				});
			} catch (Throwable t2) {
				XposedBridge.log("Both lockscreen init hooks failed");
			}
		}
		
		if (!Helpers.isEight()) {
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

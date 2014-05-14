package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.findClass;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.sensetoolbox.six.utils.GlobalActions;
import com.sensetoolbox.six.utils.StructInputEvent;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.Process;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;

import de.robv.android.xposed.XC_MethodHook;
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
							XposedHelpers.callMethod(mEasyAccessCtrl, "snapToPage", XposedHelpers.getStaticObjectField(findClass("com.htc.lockscreen.keyguard.SlidingChallengeLayout.ScrollDirection", mLSClassLoader), "ScrollToRight"));
							break;
						case 3:
							//XposedHelpers.callMethod(mEasyAccessCtrl, "snapToPage", XposedHelpers.getStaticObjectField(findClass("com.htc.lockscreen.keyguard.SlidingChallengeLayout.ScrollDirection", mLSClassLoader), "ScrollToLeft"));
							Intent i = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addFlags(0x10000000).putExtra("action", -1);
							XposedHelpers.callMethod(mEasyAccessCtrl, "launchActivityfromEasyAccess", i, false);
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
							launchApp(intent.getIntExtra("launch_app", 0));
							break;
						case 6:
							Intent i2 = new Intent("com.htc.intent.action.HTC_Prism_AllApps").addCategory("android.intent.category.DEFAULT").addFlags(0x10000000);
							XposedHelpers.callMethod(mEasyAccessCtrl, "launchActivityfromEasyAccess", i2, false);
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
			WakeLock wl = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "S6T WakeUpSleepy");
			wl.acquire(2000);
			mPowerManager.wakeUp(atTime);
		}
	}
	
	private static void sendLockScreenIntent(Context mContext, int action) {
		if (mContext != null) {
			Intent intent = new Intent("com.sensetoolbox.six.MotionGesture");
			intent.putExtra("motion_gesture", action);
			mContext.sendBroadcast(intent);
		}
	}
	
	private static void sendLockScreenIntentLauchApp(Context mContext, int input_val) {
		if (mContext != null) {
			Intent intent = new Intent("com.sensetoolbox.six.MotionGesture");
			intent.putExtra("motion_gesture", 5);
			intent.putExtra("launch_app", input_val);
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
	
	public static boolean launchApp(int action) {
        try {
        	String pkgAppName = "";

        	switch (action) {
    			case 0x02: pkgAppName = XMain.pref.getString("pref_key_wakegest_swipeup_app", ""); break;
    			case 0x03: pkgAppName = XMain.pref.getString("pref_key_wakegest_swipedown_app", ""); break;
    			case 0x04: pkgAppName = XMain.pref.getString("pref_key_wakegest_swipeleft_app", ""); break;
    			case 0x05: pkgAppName = XMain.pref.getString("pref_key_wakegest_swiperight_app", ""); break;
    			case 0x06: pkgAppName = XMain.pref.getString("pref_key_wakegest_logo2wake_app", ""); break;
    			case 0x0f: pkgAppName = XMain.pref.getString("pref_key_wakegest_dt2w_app", ""); break;
        	}
        	
        	if (pkgAppName != "") {
        		String[] pkgAppArray = pkgAppName.split("\\|");
        		
        		ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
        		Intent appIntent = new Intent(Intent.ACTION_MAIN);
        		appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        		appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        		appIntent.setComponent(name);
        		if (mEasyAccessCtrl != null)
        		XposedHelpers.callMethod(mEasyAccessCtrl, "launchActivityfromEasyAccess", appIntent, false);
        	
        		return true;
        	} else return false;
        } catch (Throwable t) {
        	XposedBridge.log(t);
            return false;
        }
	}
	
	public static void doHaptic(Context context) {
		String haptic = "false";
		try {
			Class<?> clsSP = Class.forName("android.os.SystemProperties");
			Method getFunc = clsSP.getDeclaredMethod("get", String.class);
			haptic = (String)getFunc.invoke(null, "sys.psaver.haptic");
		} catch (Exception e) {}
		
		if (haptic.equals("false")) {			
			Vibrator vibe = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
			vibe.vibrate(50);
		}
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static void execHook_InitListener() {
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "screenTurnedOff", int.class, new XC_MethodHook() {
	        @Override
	        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
	        	Thread th = (Thread)XposedHelpers.getAdditionalInstanceField(param.thisObject, "event4thread");
	        	if (th != null) {
	        		if (!th.isAlive()) th.start(); else
	        		synchronized (mPauseLock) {
	        			mPaused = false;
	        			mPauseLock.notifyAll();
	        		}
	        	}
	        }
		});
		
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "screenTurningOn", "android.view.WindowManagerPolicy.ScreenOnListener", new XC_MethodHook() {
	        @Override
	        protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
	        	if (mCurrentLEDLevel > 0) {
	        		mCurrentLEDLevel = 0;
					GlobalActions.setFlashlight(0);
				};
				if (mWakeLock != null && mWakeLock.isHeld()) mWakeLock.release();
	        	Thread th = (Thread)XposedHelpers.getAdditionalInstanceField(param.thisObject, "event4thread");
	        	if (th != null) {
	        		synchronized (mPauseLock) {
	        			mPaused = true;
	        		}
	        	}
	        }
		});
		
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
	        @Override
	        protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Thread th = new Thread(new Runnable(){
					File file = new File("/dev/input/event4");
		        	final byte event[] = new byte[4 * 2 + 2 + 2 + 4];
					BufferedInputStream bfin = new BufferedInputStream(new FileInputStream(file));
					StructInputEvent input_event = null;
					
					@Override
					@SuppressLint("Wakelock")
					public void run() {
						Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
						while (true) try {
							if (bfin.read(event) > 0) {
								input_event = new StructInputEvent(event);
								//XposedBridge.log("event3: " + bytesToHex(event));
								SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
								Date d = new Date();
								//XposedBridge.log("Event time: " + String.valueOf(Math.round(1000 * input_event.timeval_sec + input_event.timeval_usec / 1000)) + " <> " + String.valueOf(SystemClock.uptimeMillis()));
								XposedBridge.log("[S6T @ " + sdf.format(d) + "] input_event: type " + input_event.type_name + " code " + input_event.code_name + " value " + String.valueOf(input_event.value));
								if (input_event != null && input_event.type == 0x02 && input_event.code == 0x0b) {
									XMain.pref.reload();
									if (XMain.pref.getBoolean("wake_gestures_active", false)) {
										Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
										String prefName = null;
										switch (input_event.value) {
											case 0x02: prefName = "pref_key_wakegest_swipeup"; break;
											case 0x03: prefName = "pref_key_wakegest_swipedown"; break;
											case 0x04: prefName = "pref_key_wakegest_swipeleft"; break;
											case 0x05: prefName = "pref_key_wakegest_swiperight"; break;
											case 0x06: prefName = "pref_key_wakegest_logo2wake"; break;
											case 0x0f: prefName = "pref_key_wakegest_dt2w"; break;
										}
										
										if (prefName != null) {
											long event_time = Math.round(1000 * input_event.timeval_sec + input_event.timeval_usec / 1000);
											boolean isHaptic = true;
											switch (Integer.parseInt(XMain.pref.getString(prefName, "1"))) {
												case 0: isHaptic = false; break;
												case 1: doWakeUp(param.thisObject, event_time); break;
												case 2: doWakeUp(param.thisObject, event_time); sendLockScreenIntent(mContext, 1); break;
												case 3: doWakeUp(param.thisObject, event_time); sendLockScreenIntent(mContext, 2); break;
												case 4: doWakeUp(param.thisObject, event_time); sendLockScreenIntent(mContext, 3); break;
												case 5: doWakeUp(param.thisObject, event_time); sendLockScreenIntent(mContext, 4); break;
												case 6: doWakeUp(param.thisObject, event_time); sendLockScreenIntentOpenAppDrawer(mContext); break;
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
												case 8: doWakeUp(param.thisObject, event_time); GlobalActions.expandNotifications(mContext); break;
												case 9: doWakeUp(param.thisObject, event_time); GlobalActions.expandEQS(mContext); break;
												case 10: doWakeUp(param.thisObject, event_time); sendLockScreenIntentLauchApp(mContext, input_event.value); break;
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
											};
											
											if (isHaptic && XMain.pref.getBoolean("pref_key_wakegest_haptic", false)) doHaptic(mContext);
										}
									}
								}
							} else Thread.sleep(100);
							
							synchronized (mPauseLock) {
								while (mPaused) try {
									mPauseLock.wait();
									Thread.sleep(100);
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
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "event4thread", th);
	        }
	    });
	}
	
	public static void execHook_LockScreenGestures(final LoadPackageParam lpparam) {
		XposedHelpers.findAndHookMethod("com.htc.lockscreen.ctrl.LSState", lpparam.classLoader, "init", Context.class, Context.class, "com.android.internal.widget.LockPatternUtils", new XC_MethodHook() {
	        @Override
	        protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
	        	mEasyAccessCtrl = XposedHelpers.getObjectField(param.thisObject, "mEasyAccessCtrl");
	        	Context mSysContext = (Context)param.args[0];
	        	mLSClassLoader = lpparam.classLoader;
	        	if (mSysContext != null) {
	        		IntentFilter intentfilter = new IntentFilter();
	        		intentfilter.addAction("com.sensetoolbox.six.MotionGesture");
	        		mSysContext.registerReceiver(mBRLS, intentfilter);
	        	} else XposedBridge.log("[S6T] mSysContext == null");
	        }
		});
		
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

package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.findClass;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;

import com.sensetoolbox.six.utils.GlobalActions;
import com.sensetoolbox.six.utils.StructInputEvent;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;

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
	
	private static void doWakeUp(Object thisObject) {
		PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(thisObject, "mPowerManager");
		if (mPowerManager != null) mPowerManager.wakeUp(SystemClock.uptimeMillis());
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
    			case 1: pkgAppName = XMain.pref.getString("pref_key_wakegest_swiperight_app", ""); break;
    			case 2: pkgAppName = XMain.pref.getString("pref_key_wakegest_swipeleft_app", ""); break;
    			case 3: pkgAppName = XMain.pref.getString("pref_key_wakegest_swipeup_app", ""); break;
    			case 4: pkgAppName = XMain.pref.getString("pref_key_wakegest_swipedown_app", ""); break;
    			case 5: pkgAppName = XMain.pref.getString("pref_key_wakegest_dt2w_app", ""); break;
    			case 6: pkgAppName = XMain.pref.getString("pref_key_wakegest_logo2wake_app", ""); break;
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
	        	File file = new File("/dev/input/event4");
	        	final byte event[] = new byte[4 * 2 + 2 + 2 + 4];
	        	final FileInputStream fin = new FileInputStream(file);
				
				Thread th = new Thread(new Runnable(){
					@Override
					@SuppressLint("Wakelock")
					public void run() {
						while (true) try {
							if (fin.read(event) > 0) {
								StructInputEvent input_event = new StructInputEvent(event);
								//XposedBridge.log("event3: " + bytesToHex(event));
								//XposedBridge.log("[S6T] input_event: type " + input_event.type_name + " code " + input_event.code_name + " value " + String.valueOf(input_event.value));
								if (input_event.type == 0x02 && input_event.code == 0x0b) {
									XMain.pref.reload();
									if (XMain.pref.getBoolean("wake_gestures_active", false)) {
										Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
										String prefName = null;
										switch (input_event.value) {
											case 0x01: prefName = "pref_key_wakegest_swiperight"; break;
											case 0x02: prefName = "pref_key_wakegest_swipeleft"; break;
											case 0x03: prefName = "pref_key_wakegest_swipeup"; break;
											case 0x04: prefName = "pref_key_wakegest_swipedown"; break;
											case 0x05: prefName = "pref_key_wakegest_dt2w"; break;
											case 0x06: prefName = "pref_key_wakegest_logo2wake"; break;
										}
										
										boolean isHaptic = true;
										if (prefName != null)
										switch (Integer.parseInt(XMain.pref.getString(prefName, "1"))) {
											case 0: isHaptic = false; break;
											case 1: doWakeUp(param.thisObject); break;
											case 2: doWakeUp(param.thisObject); sendLockScreenIntent(mContext, 1); break;
											case 3: doWakeUp(param.thisObject); sendLockScreenIntent(mContext, 2); break;
											case 4: doWakeUp(param.thisObject); sendLockScreenIntent(mContext, 3); break;
											case 5: doWakeUp(param.thisObject); sendLockScreenIntent(mContext, 4); break;
											case 6: doWakeUp(param.thisObject); sendLockScreenIntentOpenAppDrawer(mContext); break;
											case 7:
												PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
												if (mWakeLock == null) mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "S6T PowerFlash");
												if (mCurrentLEDLevel == 0) {
													mCurrentLEDLevel = 127;
													if (!mWakeLock.isHeld()) mWakeLock.acquire(600000);
												} else { 
													mCurrentLEDLevel = 0;
													if (mWakeLock.isHeld()) mWakeLock.release();
												}
												GlobalActions.setFlashlight(mCurrentLEDLevel);
												break;
											case 8: doWakeUp(param.thisObject); GlobalActions.expandNotifications(mContext); break;
											case 9: doWakeUp(param.thisObject); GlobalActions.expandEQS(mContext); break;
											case 10: doWakeUp(param.thisObject); sendLockScreenIntentLauchApp(mContext, input_event.value); break;
										};
										
										if (isHaptic && XMain.pref.getBoolean("pref_key_wakegest_haptic", false)) doHaptic(mContext);
									}
								}
							}
							
							synchronized (mPauseLock) {
								while (mPaused) try { mPauseLock.wait(); } catch (Exception e) {}
			        		}
						} catch (Throwable t) {
							XposedBridge.log(t);
							try { if (fin != null) fin.close(); } catch (Exception e) {}
							break;
						}
					}
				});
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

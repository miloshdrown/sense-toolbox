package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.langerhans.one.utils.GlobalActions;
import com.langerhans.one.utils.Version;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ControlsMods {
	
	private static boolean isBackLongPressed = false;
	private static boolean isPowerPressed = false;
	private static boolean isPowerLongPressed = false;
	private static boolean isVolumePressed = false;
	private static boolean isVolumeLongPressed = false;
	private static boolean isWaitingForPowerLongPressed = false;
	private static boolean isWaitingForVolumeLongPressed = false;
	private static int mFlashlightLevel = 0;
	
	public static void setupPWMKeys() {
		try {
			final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);

			findAndHookMethod(clsPWM, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					KeyEvent keyEvent = (KeyEvent)param.args[0];
					
					int keycode = keyEvent.getKeyCode();
					int action = keyEvent.getAction();
					int flags = keyEvent.getFlags();
					
					//XposedBridge.log("interceptKeyBeforeQueueing: KeyCode: " + String.valueOf(keyEvent.getKeyCode()) + " | Action: " + String.valueOf(keyEvent.getAction()) + " | RepeatCount: " + String.valueOf(keyEvent.getRepeatCount())+ " | Flags: " + String.valueOf(keyEvent.getFlags()));
					int pref_backlongpress = Integer.parseInt(XMain.pref.getString("pref_key_controls_backlongpressaction", "1"));
					if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM) {
						// Back long press
						if (pref_backlongpress != 1 && keycode == KeyEvent.KEYCODE_BACK) {
							if (action == KeyEvent.ACTION_DOWN) isBackLongPressed = false;
							if (action == KeyEvent.ACTION_UP && isBackLongPressed == true) param.setResult(0);
						}
					}
				}
			});
			
			findAndHookMethod(clsPWM, "interceptKeyBeforeDispatching", "android.view.WindowManagerPolicy$WindowState", KeyEvent.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					KeyEvent keyEvent = (KeyEvent)param.args[1];
					
					int keycode = keyEvent.getKeyCode();
					int action = keyEvent.getAction();
					int repeats = keyEvent.getRepeatCount();
					int flags = keyEvent.getFlags();

					//XposedBridge.log("interceptKeyBeforeDispatching: KeyCode: " + String.valueOf(keyEvent.getKeyCode()) + " | Action: " + String.valueOf(keyEvent.getAction()) + " | RepeatCount: " + String.valueOf(keyEvent.getRepeatCount())+ " | Flags: " + String.valueOf(keyEvent.getFlags()));
					int pref_backlongpress = Integer.parseInt(XMain.pref.getString("pref_key_controls_backlongpressaction", "1"));
					if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM) {
						// Back long press
						if (pref_backlongpress != 1 && keycode == KeyEvent.KEYCODE_BACK) {
							if (action == KeyEvent.ACTION_DOWN && repeats >= 5) {
								if (isBackLongPressed == false) {
									Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
									switch (pref_backlongpress) {
										case 2: GlobalActions.expandNotifications(mContext); break;
										case 3: GlobalActions.expandEQS(mContext); break;
										case 4: GlobalActions.lockDevice(mContext); break;
										case 5: GlobalActions.goToSleep(mContext); break;
										case 6: GlobalActions.takeScreenshot(mContext); break;
										case 7: XposedHelpers.callMethod(param.thisObject, "dismissKeyguardLw"); GlobalActions.launchApp(mContext, 3); break;
										case 8: GlobalActions.toggleThis(mContext, Integer.parseInt(XMain.pref.getString("pref_key_controls_backlongpress_toggle", "0"))); break;
										case 9: GlobalActions.killForegroundApp(mContext); break;
									}
								}
								isBackLongPressed = true;
								param.setResult(-1L);
								return;
							}
							if (action == KeyEvent.ACTION_UP) {
								if (isBackLongPressed == true) {
									isBackLongPressed = false;
									param.setResult(-1L);
								}
							}
						}
					}
				}
			});
			
			findAndHookMethod(clsPWM, "launchAssistAction", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					int pref_homeassist = Integer.parseInt(XMain.pref.getString("pref_key_controls_homeassistaction", "1"));
					if (pref_homeassist != 1) {
						Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
						param.setResult(null);
						switch (pref_homeassist) {
							case 2: GlobalActions.expandNotifications(mContext); break;
							case 3: GlobalActions.expandEQS(mContext); break;
							case 4: GlobalActions.lockDevice(mContext); break;
							case 5: GlobalActions.goToSleep(mContext); break;
							case 6: GlobalActions.takeScreenshot(mContext); break;
							case 7: XposedHelpers.callMethod(param.thisObject, "dismissKeyguardLw"); GlobalActions.launchApp(mContext, 4); break;
							case 8: GlobalActions.toggleThis(mContext, Integer.parseInt(XMain.pref.getString("pref_key_controls_homeassist_toggle", "0"))); break;
							case 9: GlobalActions.killForegroundApp(mContext); break;
						}
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void execHook_dieGoogleNow(LoadPackageParam lpparam) {
		if (XMain.senseVersion.compareTo(new Version("5.5")) >= 0) {
			findAndHookMethod("com.htc.lockscreen.HtcKeyguardHostViewImpl", lpparam.classLoader, "launchGoogleNow", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(null);
				}
			});
		} else {
			findAndHookMethod("com.htc.lockscreen.HtcLockScreen", lpparam.classLoader, "launchGoogleNow", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(null);
				}
			});
		}
	}

	public static void execHook_Vol2Wake(final LoadPackageParam lpparam) {
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader, "isWakeKeyWhenScreenOff", int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				int keyCode = (Integer) param.args[0];
				//XposedBridge.log("Pressed button! Keycode = " + String.valueOf(keyCode));
				if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
					param.setResult(true);
			}
		});
	}
	
	static Handler mHandler;
	static WakeLock mWakeLock;
	
	// Release wakelock and turn off flashlight on screen on
	private static BroadcastReceiver mScrOn = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			if (mFlashlightLevel > 0) {
				mFlashlightLevel = 0;
				GlobalActions.setFlashlight(0);
				if (mWakeLock != null && mWakeLock.isHeld()) mWakeLock.release();
			};
		}
	};
	
	public static void execHook_PowerFlash(LoadPackageParam lpparam) {
	    final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);
		findAndHookMethod(clsPWM, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				mPWMContext.registerReceiver(mScrOn, new IntentFilter(Intent.ACTION_SCREEN_ON));
			}
		});
	    
		findAndHookMethod(clsPWM, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				KeyEvent keyEvent = (KeyEvent)param.args[0];
				
				int keycode = keyEvent.getKeyCode();
				int action = keyEvent.getAction();
				int flags = keyEvent.getFlags();
				
				// Ignore repeated KeyEvents simulated on Power Key Up
				if ((flags & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == KeyEvent.FLAG_VIRTUAL_HARD_KEY) return;
				if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM) {
					// Power long press
					final PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
					if (keycode == KeyEvent.KEYCODE_POWER && !mPowerManager.isScreenOn()) {
						//XposedBridge.log("interceptKeyBeforeQueueing: KeyCode: " + String.valueOf(keyEvent.getKeyCode()) + " | Action: " + String.valueOf(keyEvent.getAction()) + " | RepeatCount: " + String.valueOf(keyEvent.getRepeatCount())+ " | Flags: " + String.valueOf(keyEvent.getFlags()));
						if (action == KeyEvent.ACTION_DOWN) {
							isPowerPressed = true;
							isPowerLongPressed = false;
							
							mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
							// Post only one delayed runnable that waits for long press timeout
							if (!isWaitingForPowerLongPressed)
							mHandler.postDelayed(new Runnable(){
								@SuppressLint("Wakelock")
								@Override
								public void run() {
									if (isPowerPressed) {
										isPowerLongPressed = true;
										
										if (mWakeLock == null)
										mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "S5T PowerFlash");
											
										if (mFlashlightLevel == 0) {
											mFlashlightLevel = 127;
											if (!mWakeLock.isHeld()) mWakeLock.acquire(600000);
										} else {
											mFlashlightLevel = 0;
											if (mWakeLock.isHeld()) mWakeLock.release();
										}
										
										GlobalActions.setFlashlight(mFlashlightLevel);
									}
									isPowerPressed = false;
									isWaitingForPowerLongPressed = false;
								}
							}, ViewConfiguration.getLongPressTimeout() + 200);
							isWaitingForPowerLongPressed = true;
							param.setResult(0);
						}
						if (action == KeyEvent.ACTION_UP) {
							if (isPowerPressed && !isPowerLongPressed) try {
								mFlashlightLevel = 0;
								GlobalActions.setFlashlight(0);
								if (mWakeLock != null && mWakeLock.isHeld()) mWakeLock.release();
								XposedHelpers.callMethod(param.thisObject, "sendEvent", KeyEvent.KEYCODE_POWER, 0, 0);
								XposedHelpers.callMethod(param.thisObject, "sendEvent", KeyEvent.KEYCODE_POWER, 1, 0);
								param.setResult(0);
							} catch (Exception e) {
								e.printStackTrace();
							}
							isPowerPressed = false;
						}
					}
				}
			}
		});
	}
	
	//Shameless copypasta ;)
	public static void execHook_VolumeMediaButtons(LoadPackageParam lpparam, final int upAction, final int downAction, final boolean vol2wakeEnabled) {
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				final KeyEvent keyEvent = (KeyEvent)param.args[0];
				
				int keycode = keyEvent.getKeyCode();
				int action = keyEvent.getAction();
				int flags = keyEvent.getFlags();
				
				// Ignore repeated KeyEvents simulated on volume Key Up
				if ((flags & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == KeyEvent.FLAG_VIRTUAL_HARD_KEY) return;
				if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM) {
					// Volume long press
					PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
					if ((keycode == KeyEvent.KEYCODE_VOLUME_UP || keycode == KeyEvent.KEYCODE_VOLUME_DOWN) && !mPowerManager.isScreenOn()) {
						//XposedBridge.log("interceptKeyBeforeQueueing: KeyCode: " + String.valueOf(keyEvent.getKeyCode()) + " | Action: " + String.valueOf(keyEvent.getAction()) + " | RepeatCount: " + String.valueOf(keyEvent.getRepeatCount())+ " | Flags: " + String.valueOf(keyEvent.getFlags()));
						if (action == KeyEvent.ACTION_DOWN) {
							isVolumePressed = true;
							isVolumeLongPressed = false;
							
							Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
							// Post only one delayed runnable that waits for long press timeout
							if (!isWaitingForVolumeLongPressed)
							mHandler.postDelayed(new Runnable(){
								@Override
								public void run() {
									if (isVolumePressed) {
										isVolumeLongPressed = true;
										switch (keyEvent.getKeyCode()) {
										case KeyEvent.KEYCODE_VOLUME_UP:
											if (upAction == 0)
												break;
											GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_DOWN, upAction));
											GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_UP, upAction));
											break;
										case KeyEvent.KEYCODE_VOLUME_DOWN:
											if (downAction == 0)
												break;
											GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_DOWN, downAction));
											GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_UP, downAction));
											break;

										default:
											break;
										}
									}
									isVolumePressed = false;
									isWaitingForVolumeLongPressed = false;
								}
							}, ViewConfiguration.getLongPressTimeout());
							isWaitingForVolumeLongPressed = true;
							param.setResult(0);
						}
						if (action == KeyEvent.ACTION_UP) {
							if (isVolumePressed && !isVolumeLongPressed) {
								if (vol2wakeEnabled)
								{
									XposedHelpers.callMethod(param.thisObject, "sendEvent", KeyEvent.KEYCODE_POWER, 0, 0);
									XposedHelpers.callMethod(param.thisObject, "sendEvent", KeyEvent.KEYCODE_POWER, 1, 0);
								}
								param.setResult(0);
							}
							isVolumePressed = false;
						}
					}
				}
			}
		});
	}
	
	public static void exec_SwapVolumeCCWLand(LoadPackageParam lpparam) {
		try {
			if (Build.VERSION.SDK_INT >= 19) {
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustMasterVolume", int.class, int.class, String.class, hook_adjustMasterVolume);
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustSuggestedStreamVolume", int.class, int.class, int.class, String.class, hook_adjustSuggestedStreamVolume);
			} else {
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustMasterVolume", int.class, int.class, hook_adjustMasterVolume);
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustSuggestedStreamVolume", int.class, int.class, int.class, hook_adjustSuggestedStreamVolume);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static XC_MethodHook hook_adjustMasterVolume = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			hook_modifyOrientation(param);
		}
	};

	public static XC_MethodHook hook_adjustSuggestedStreamVolume = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			hook_modifyOrientation(param);
		}
	};
	
	private static void hook_modifyOrientation(MethodHookParam param) {
		if ((Integer)param.args[0] != 0) try {
			Context context = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
			int rotation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            if (rotation == Surface.ROTATION_90) param.args[0] = -1 * (Integer)param.args[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;

import com.sensetoolbox.six.utils.GlobalActions;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
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
										case 10: GlobalActions.simulateMenu(mContext); break;
										case 11: GlobalActions.openRecents(mContext); break;
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
					assistAndSearchPanelOverride(param);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void assistAndSearchPanelOverride(final MethodHookParam param) {
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
				case 7: Object amn = XposedHelpers.callStaticMethod(findClass("android.app.ActivityManagerNative", null), "getDefault");
						XposedHelpers.callMethod(amn, "dismissKeyguardOnNextActivity");
						GlobalActions.launchApp(mContext, 4); break;
				case 8: GlobalActions.toggleThis(mContext, Integer.parseInt(XMain.pref.getString("pref_key_controls_homeassist_toggle", "0"))); break;
				case 9: GlobalActions.killForegroundApp(mContext); break;
				case 10: GlobalActions.simulateMenu(mContext); break;
				case 11: GlobalActions.openRecents(mContext); break;
			}
		}
	}
	
	public static void execHook_dieGoogleNow(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardHostView", lpparam.classLoader, "showAssistant", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(null);
			}
		});
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
										mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "S6T PowerFlash");
											
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
							} catch (Throwable t) {
								XposedBridge.log(t);
							}
							isPowerPressed = false;
						}
					}
				}
			}
		});
	}
	
	public static void execHook_VolumeMediaButtons(LoadPackageParam lpparam, final boolean vol2wakeEnabled) {
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
							mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
							// Post only one delayed runnable that waits for long press timeout
							if (mHandler != null && !isWaitingForVolumeLongPressed) {
								mHandler.postDelayed(new Runnable(){
									public void run() {
										if (isVolumePressed) {
											isVolumeLongPressed = true;
											switch (keyEvent.getKeyCode()) {
												case KeyEvent.KEYCODE_VOLUME_UP:
													if (XMain.pref_mediaUp == 0) break;
													GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_DOWN, XMain.pref_mediaUp));
													GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_UP, XMain.pref_mediaUp));
													break;
												case KeyEvent.KEYCODE_VOLUME_DOWN:
													if (XMain.pref_mediaDown == 0) break;
													GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_DOWN, XMain.pref_mediaDown));
													GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_UP, XMain.pref_mediaDown));
													break;
												default:
													break;
											}
										}
										isVolumePressed = false;
										isWaitingForVolumeLongPressed = false;
							        }
								}, ViewConfiguration.getLongPressTimeout());
							}
							isWaitingForVolumeLongPressed = true;
							param.setResult(0);
						}
						if (action == KeyEvent.ACTION_UP) {
							isVolumePressed = false;
							// Kill all callbacks (removing only posted Runnable is not working... no idea)
							if (mHandler != null) mHandler.removeCallbacksAndMessages(null);
							if (!isVolumeLongPressed) {
								boolean isMusicActive = (Boolean)XposedHelpers.callMethod(param.thisObject, "isMusicActive");
								boolean isInCall = (Boolean)XposedHelpers.callMethod(param.thisObject, "isInCall");
								// If music stream is playing, adjust its volume
								if (isMusicActive) XposedHelpers.callMethod(param.thisObject, "handleVolumeKey", AudioManager.STREAM_MUSIC, keycode);
								// If voice call is active while screen off by proximity sensor, adjust its volume
								else if (isInCall) XposedHelpers.callMethod(param.thisObject, "handleVolumeKey", AudioManager.STREAM_VOICE_CALL, keycode);
								// Use vol2wake in other cases 	
								else if (vol2wakeEnabled) {
									XposedHelpers.callMethod(param.thisObject, "sendEvent", KeyEvent.KEYCODE_POWER, 0, 0);
									XposedHelpers.callMethod(param.thisObject, "sendEvent", KeyEvent.KEYCODE_POWER, 1, 0);
								}
								param.setResult(0);
							}
							isWaitingForVolumeLongPressed = false;
						}
					}
				}
			}
		});
	}
	
	public static void exec_SwapVolumeCCWLand(LoadPackageParam lpparam) {
		try {
			if (Build.VERSION.SDK_INT >= 19) {
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustMasterVolume", int.class, int.class, String.class, hook_adjustVolumeParam0);
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustSuggestedStreamVolume", int.class, int.class, int.class, String.class, hook_adjustVolumeParam0);
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustLocalOrRemoteStreamVolume", int.class, int.class, String.class, hook_adjustVolumeParam1);
			} else {
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustMasterVolume", int.class, int.class, hook_adjustVolumeParam0);
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustSuggestedStreamVolume", int.class, int.class, int.class, hook_adjustVolumeParam0);
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustLocalOrRemoteStreamVolume", int.class, int.class, hook_adjustVolumeParam1);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static XC_MethodHook hook_adjustVolumeParam0 = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			hook_modifyOrientation(param, 0);
		}
	};

	public static XC_MethodHook hook_adjustVolumeParam1 = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			hook_modifyOrientation(param, 1);
		}
	};
	
	private static void hook_modifyOrientation(MethodHookParam param, int paramNum) {
		if ((Integer)param.args[paramNum] != 0) try {
			Context context = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
			int rotation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            if (rotation == Surface.ROTATION_90) param.args[paramNum] = -1 * (Integer)param.args[paramNum];
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void execHook_M8BackLongpress(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				final ImageView backButton = (ImageView) callMethod(param.thisObject, "getBackButton");
				if(backButton!=null){
					setObjectField(backButton, "mCheckLongPress", new Runnable() {
						@Override
						public void run() {
							if(backButton.isPressed()) {
								backButton.setPressed(false);
								backButton.performLongClick();
							}
						}
					});
					backButton.setLongClickable(true);
					backButton.setOnLongClickListener(new OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							int pref_backlongpress = Integer.parseInt(XMain.pref.getString("pref_key_controls_backlongpressaction", "1"));
							Context mContext = backButton.getContext();
							switch (pref_backlongpress) {
								case 2: GlobalActions.expandNotifications(mContext); break;
								case 3: GlobalActions.expandEQS(mContext); break;
								case 4: GlobalActions.lockDevice(mContext); break;
								case 5: GlobalActions.goToSleep(mContext); break;
								case 6: GlobalActions.takeScreenshot(mContext); break;
								case 7: GlobalActions.launchApp(mContext, 3); break; // No back key on lock screen
								case 8: GlobalActions.toggleThis(mContext, Integer.parseInt(XMain.pref.getString("pref_key_controls_backlongpress_toggle", "0"))); break;
								case 9: GlobalActions.killForegroundApp(mContext); break;
								case 10: GlobalActions.simulateMenu(mContext); break;
								case 11: GlobalActions.openRecents(mContext); break;
							}
							return true;
						}
					});
				}
			}
		});
	}
}
package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.sensetoolbox.six.R;
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
					if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM) {
						// Back long press
						XMain.pref.reload();
						if (Integer.parseInt(XMain.pref.getString("pref_key_controls_backlongpressaction", "1")) != 1 && keycode == KeyEvent.KEYCODE_BACK) {
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
					if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM) {
						// Back long press
						XMain.pref.reload();
						int pref_backlongpress = Integer.parseInt(XMain.pref.getString("pref_key_controls_backlongpressaction", "1"));
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
										case 12: XposedHelpers.callMethod(param.thisObject, "dismissKeyguardLw"); GlobalActions.launchShortcut(mContext, 3); break;
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
		XMain.pref.reload();
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
				case 12: Object amn2 = XposedHelpers.callStaticMethod(findClass("android.app.ActivityManagerNative", null), "getDefault");
						XposedHelpers.callMethod(amn2, "dismissKeyguardOnNextActivity");
						GlobalActions.launchShortcut(mContext, 4); break;
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
			}
			if (mWakeLock != null && mWakeLock.isHeld()) mWakeLock.release();
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
				// Power and volkeys are pressed at the same time
				if (isVolumePressed) return;
				
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
								//XposedHelpers.callMethod(param.thisObject, "sendEvent", KeyEvent.KEYCODE_POWER, 0, 0);
								//XposedHelpers.callMethod(param.thisObject, "sendEvent", KeyEvent.KEYCODE_POWER, 1, 0);
								mPowerManager.wakeUp(SystemClock.uptimeMillis());
								param.setResult(0);
							} catch (Throwable t) {
								XposedBridge.log(t);
							}
							isPowerPressed = false;
							isWaitingForPowerLongPressed = false;
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
				// Power and volkeys are pressed at the same time
				if (isPowerPressed) return;
				
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
													XMain.pref.reload();
													int pref_mediaUp = Integer.parseInt(XMain.pref.getString("pref_key_controls_mediaupaction", "0"));
													if (pref_mediaUp == 0) break;
													GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_DOWN, pref_mediaUp));
													GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_UP, pref_mediaUp));
													break;
												case KeyEvent.KEYCODE_VOLUME_DOWN:
													XMain.pref.reload();
													int pref_mediaDown = Integer.parseInt(XMain.pref.getString("pref_key_controls_mediadownaction", "0"));
													if (pref_mediaDown == 0) break;
													GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_DOWN, pref_mediaDown));
													GlobalActions.sendMediaButton(new KeyEvent(KeyEvent.ACTION_UP, pref_mediaDown));
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
									mPowerManager.wakeUp(SystemClock.uptimeMillis());
									//XposedHelpers.callMethod(param.thisObject, "sendEvent", KeyEvent.KEYCODE_POWER, 0, 0);
									//XposedHelpers.callMethod(param.thisObject, "sendEvent", KeyEvent.KEYCODE_POWER, 1, 0);
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

	private static boolean isBackPressed = false;
	private static void assignBackLongPress(final MethodHookParam param) {
		final ImageView backButton = (ImageView) callMethod(param.thisObject, "getBackButton");
		if (backButton != null) {
			setObjectField(backButton, "mSupportsLongpress", true);
			setObjectField(backButton, "mCheckLongPress", new Runnable() {
				@Override
				public void run() {
					if (backButton.isPressed()) {
						backButton.setPressed(false);
						if (XposedHelpers.getIntField(backButton, "mCode") != 0) {
							XposedHelpers.callMethod(backButton, "sendEvent", 1, 32);
							XposedHelpers.callMethod(backButton, "sendAccessibilityEvent", 2);
						}
						XMain.pref.reload();
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
							case 12: GlobalActions.launchShortcut(mContext, 3); break; // No back key on lock screen
						}
					}
				}
			});
		}
	}
	public static void execHook_M8BackLongpress(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				assignBackLongPress(param);
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader, "onSizeChanged", int.class, int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				assignBackLongPress(param);
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.policy.KeyButtonView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				if (XposedHelpers.getIntField(param.thisObject, "mCode") == 4) {
					MotionEvent mev = (MotionEvent)param.args[0];
					int mevact = mev.getAction();
					if (mevact == 0) isBackPressed = true;
					if (mevact == 2 && isBackPressed) param.setResult(true);
					if (mevact == 1) isBackPressed = false;
				}
			}
		});
	}
	
	public static boolean isHomePressed = false;
	public static void execHook_M8HomeLongpress(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				final ImageView homeButton = (ImageView) callMethod(param.thisObject, "getHomeButton");
				if (homeButton != null)
				setObjectField(homeButton, "mCheckLongPress", new Runnable() {
					@Override
					public void run() {
						if (homeButton.isPressed()) {
							homeButton.setPressed(false);
							if (XposedHelpers.getIntField(homeButton, "mCode") != 0) {
								XposedHelpers.callMethod(homeButton, "sendEvent", 1, 32);
								XposedHelpers.callMethod(homeButton, "sendAccessibilityEvent", 2);
							}
							homeButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
							GlobalActions.simulateMenu(homeButton.getContext());
						}
					}
				});
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.policy.KeyButtonView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				if (XposedHelpers.getIntField(param.thisObject, "mCode") == 3) {
					MotionEvent mev = (MotionEvent)param.args[0];
					int mevact = mev.getAction();
					if (mevact == 0) isHomePressed = true;
					if (mevact == 2 && isHomePressed) param.setResult(true);
					if (mevact == 1) isHomePressed = false;
				}
			}
		});
		
		XposedBridge.hookAllConstructors(findClass("com.android.systemui.statusbar.policy.KeyButtonView", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Integer mCode = (Integer) getObjectField(param.thisObject, "mCode");
				if (mCode == KeyEvent.KEYCODE_HOME)
					setObjectField(param.thisObject, "mSupportsLongpress", true);
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "prepareNavigationBarView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				View v = (View) getObjectField(param.thisObject, "mNavigationBarView");
				if (v != null) {
					View b = (View) callMethod(v, "getHomeButton");
					if (b != null) callMethod(b, "setOnTouchListener", new Object[]{ null });
				}
			}
		});
	}
	
	public static boolean isRecentsPressed = false;
	public static void execHook_M8RecentsLongpress(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				final ImageView recentsButton = (ImageView) callMethod(param.thisObject, "getRecentsButton");
				if (recentsButton != null)
				setObjectField(recentsButton, "mCheckLongPress", new Runnable() {
					@Override
					public void run() {
						if (recentsButton.isPressed()) {
							recentsButton.setPressed(false);
							if (XposedHelpers.getIntField(recentsButton, "mCode") != 0) {
								XposedHelpers.callMethod(recentsButton, "sendEvent", 1, 32);
								XposedHelpers.callMethod(recentsButton, "sendAccessibilityEvent", 2);
							}
							recentsButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
							assistAndSearchPanelOverride(param);
						}
					}
				});
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.policy.KeyButtonView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				if (XposedHelpers.getIntField(param.thisObject, "mCode") == 187) {
					MotionEvent mev = (MotionEvent)param.args[0];
					int mevact = mev.getAction();
					if (mevact == 0) isRecentsPressed = true;
					if (mevact == 2 && isRecentsPressed) param.setResult(true);
					if (mevact == 1) isRecentsPressed = false;
				}
			}
		});
	}

	public static void execHook_SmallNavbar() {
		XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
		XResources.setSystemWideReplacement("android", "dimen", "navigation_bar_height", modRes.fwd(R.dimen.navigation_bar_height));
		XResources.setSystemWideReplacement("android", "dimen", "navigation_bar_height_landscape", modRes.fwd(R.dimen.navigation_bar_height_landscape));
		XResources.setSystemWideReplacement("android", "dimen", "navigation_bar_width", modRes.fwd(R.dimen.navigation_bar_width));
		XResources.setSystemWideReplacement("android", "dimen", "system_bar_height", modRes.fwd(R.dimen.navigation_bar_height));
	}
	
	public static void execHook_FixDialer(LoadPackageParam lpparam) {
		XC_MethodHook hook =  new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				RelativeLayout thisView = (RelativeLayout) param.thisObject;
				AbsListView.LayoutParams params = (AbsListView.LayoutParams) thisView.getLayoutParams();
				params.height = 503; // 449 stock + 54 pixel white bar
				thisView.setLayoutParams(params);
				thisView.invalidate();
			}
		};
		findAndHookMethod("com.htc.htcdialer.widget.ContactDetailPhotoView", lpparam.classLoader, "initContactDetailPhotoView", Context.class, hook);
		findAndHookMethod("com.htc.htcdialer.widget.ContactDetailPhotoView", lpparam.classLoader, "onConfigurationChanged", Configuration.class, hook);
		findAndHookMethod("com.htc.htcdialer.widget.ContactDetailPhotoView", lpparam.classLoader, "onLayout", boolean.class, int.class, int.class, int.class, int.class, hook);
		findAndHookMethod("com.htc.htcdialer.widget.ContactDetailPhotoView", lpparam.classLoader, "dispatchDraw", Canvas.class, hook);
	}
}
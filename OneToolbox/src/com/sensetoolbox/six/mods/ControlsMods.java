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
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings;
import android.telecom.TelecomManager;
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
import com.sensetoolbox.six.utils.Helpers;

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
	
	public static void setupPWMKeys() {
		try {
			final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);

			XC_MethodHook hook = new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					KeyEvent keyEvent = (KeyEvent)param.args[0];
					
					int keycode = keyEvent.getKeyCode();
					int action = keyEvent.getAction();
					int flags = keyEvent.getFlags();
					
					//XposedBridge.log("interceptKeyBeforeQueueing: KeyCode: " + String.valueOf(keyEvent.getKeyCode()) + " | Action: " + String.valueOf(keyEvent.getAction()) + " | RepeatCount: " + String.valueOf(keyEvent.getRepeatCount())+ " | Flags: " + String.valueOf(keyEvent.getFlags()));
					if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM && keycode == KeyEvent.KEYCODE_BACK) {
						// Back long press
						XMain.pref.reload();
						if (Integer.parseInt(XMain.pref.getString("pref_key_controls_backlongpressaction", "1")) != 1) {
							if (action == KeyEvent.ACTION_DOWN) isBackLongPressed = false;
							if (action == KeyEvent.ACTION_UP && isBackLongPressed) param.setResult(0);
						}
					}
				}
			};
			Object[] argsAndHook = { KeyEvent.class, int.class, boolean.class, hook };
			if (Helpers.isLP()) argsAndHook = new Object[] { KeyEvent.class, int.class, hook };
			findAndHookMethod(clsPWM, "interceptKeyBeforeQueueing", argsAndHook);
			
			findAndHookMethod(clsPWM, "interceptKeyBeforeDispatching", "android.view.WindowManagerPolicy$WindowState", KeyEvent.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					KeyEvent keyEvent = (KeyEvent)param.args[1];
					
					int keycode = keyEvent.getKeyCode();
					int action = keyEvent.getAction();
					int repeats = keyEvent.getRepeatCount();
					int flags = keyEvent.getFlags();

					//XposedBridge.log("interceptKeyBeforeDispatching: KeyCode: " + String.valueOf(keyEvent.getKeyCode()) + " | Action: " + String.valueOf(keyEvent.getAction()) + " | RepeatCount: " + String.valueOf(keyEvent.getRepeatCount())+ " | Flags: " + String.valueOf(keyEvent.getFlags()));
					if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM && keycode == KeyEvent.KEYCODE_BACK) {
						// Back long press
						XMain.pref.reload();
						int pref_backlongpress = Integer.parseInt(XMain.pref.getString("pref_key_controls_backlongpressaction", "1"));
						if (pref_backlongpress != 1) {
							if (action == KeyEvent.ACTION_DOWN && repeats >= 5) {
								if (!isBackLongPressed) {
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
										case 13: GlobalActions.switchToPrevApp(mContext); break;
										case 14: GlobalActions.openAppDrawer(mContext); break;
									}
								}
								isBackLongPressed = true;
								param.setResult(-1L);
								return;
							}
							if (action == KeyEvent.ACTION_UP) {
								if (isBackLongPressed) {
									isBackLongPressed = false;
									param.setResult(-1L);
								}
							}
						}
					}
				}
			});
			
			XC_MethodHook assistHook = new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					assistAndSearchPanelOverride(param);
				}
			};
			
			Object[] assistArgsAndHook = { assistHook };
			if (Helpers.isLP()) assistArgsAndHook = new Object[] { String.class, assistHook };
			findAndHookMethod(clsPWM, "launchAssistAction", assistArgsAndHook);
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
						if (Helpers.isLP())
							XposedHelpers.callMethod(amn, "keyguardWaitingForActivityDrawn");
						else
							XposedHelpers.callMethod(amn, "dismissKeyguardOnNextActivity");
						GlobalActions.launchApp(mContext, 4); break;
				case 8: GlobalActions.toggleThis(mContext, Integer.parseInt(XMain.pref.getString("pref_key_controls_homeassist_toggle", "0"))); break;
				case 9: GlobalActions.killForegroundApp(mContext); break;
				case 10: GlobalActions.simulateMenu(mContext); break;
				case 11: GlobalActions.openRecents(mContext); break;
				case 12: Object amn2 = XposedHelpers.callStaticMethod(findClass("android.app.ActivityManagerNative", null), "getDefault");
						if (Helpers.isLP())
							XposedHelpers.callMethod(amn2, "keyguardWaitingForActivityDrawn");
						else
							XposedHelpers.callMethod(amn2, "dismissKeyguardOnNextActivity");
						GlobalActions.launchShortcut(mContext, 4); break;
				case 13: GlobalActions.switchToPrevApp(mContext); break;
				case 14: GlobalActions.openAppDrawer(mContext); break;
				case 15: GlobalActions.showQuickRecents(mContext); break;
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

	public static void execHook_Vol2Wake() {
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "isWakeKeyWhenScreenOff", int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				int keyCode = (Integer)param.args[0];
				if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
					param.setResult(true);
			}
		});
	}
	
	static Handler mHandler;
	
	// Release wakelock and turn off flashlight on screen on
	private static BroadcastReceiver mScrOn = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			if (Helpers.mFlashlightLevel > 0) {
				Helpers.mFlashlightLevel = 0;
				GlobalActions.setFlashlight(0);
			}
			if (Helpers.mWakeLock != null && Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.release();
		}
	};
	
	public static void execHook_PowerFlash() {
		final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);
		findAndHookMethod(clsPWM, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				mPWMContext.registerReceiver(mScrOn, new IntentFilter(Intent.ACTION_SCREEN_ON));
			}
		});
		
		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			@SuppressWarnings("deprecation")
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				// Power and volkeys are pressed at the same time
				if (isVolumePressed) return;
				
				KeyEvent keyEvent = (KeyEvent)param.args[0];
				
				int keycode = keyEvent.getKeyCode();
				int action = keyEvent.getAction();
				int flags = keyEvent.getFlags();
				
				// Ignore repeated KeyEvents simulated on Power Key Up
				if ((flags & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == KeyEvent.FLAG_VIRTUAL_HARD_KEY) return;
				if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM && keycode == KeyEvent.KEYCODE_POWER) {
					// Power long press
					final PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
					if (!mPowerManager.isScreenOn()) {
						//XposedBridge.log("interceptKeyBeforeQueueing: KeyCode: " + String.valueOf(keyEvent.getKeyCode()) + " | Action: " + String.valueOf(keyEvent.getAction()) + " | RepeatCount: " + String.valueOf(keyEvent.getRepeatCount())+ " | Flags: " + String.valueOf(keyEvent.getFlags()));
						if (action == KeyEvent.ACTION_DOWN) {
							isPowerPressed = true;
							isPowerLongPressed = false;
							
							mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
							int longPressDelay = (XMain.pref.getBoolean("pref_key_controls_powerflash_delay", false) ? ViewConfiguration.getLongPressTimeout() * 3 : ViewConfiguration.getLongPressTimeout()) + 500;
							// Post only one delayed runnable that waits for long press timeout
							if (!isWaitingForPowerLongPressed)
							mHandler.postDelayed(new Runnable(){
								@Override
								@SuppressLint("Wakelock")
								public void run() {
									if (isPowerPressed) {
										isPowerLongPressed = true;
										
										if (Helpers.mWakeLock == null)
											Helpers.mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "S6T Flashlight");
											
										if (Helpers.mFlashlightLevel == 0 || !Helpers.mWakeLock.isHeld()) {
											Helpers.mFlashlightLevel = 127;
											if (!Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.acquire(600000);
										} else {
											Helpers.mFlashlightLevel = 0;
											if (Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.release();
										}
										
										GlobalActions.setFlashlight(Helpers.mFlashlightLevel);
									}
									isPowerPressed = false;
									isWaitingForPowerLongPressed = false;
								}
							}, longPressDelay);
							isWaitingForPowerLongPressed = true;
							param.setResult(0);
						}
						if (action == KeyEvent.ACTION_UP) {
							if (isPowerPressed && !isPowerLongPressed) try {
								Helpers.mFlashlightLevel = 0;
								GlobalActions.setFlashlight(0);
								if (Helpers.mWakeLock != null && Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.release();
								XposedHelpers.callMethod(mPowerManager, "wakeUp", SystemClock.uptimeMillis());
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
		};
		
		Object[] argsAndHook = { KeyEvent.class, int.class, boolean.class, hook };
		if (Helpers.isLP()) argsAndHook = new Object[] { KeyEvent.class, int.class, hook };
		findAndHookMethod(clsPWM, "interceptKeyBeforeQueueing", argsAndHook);
	}
	
	public static void execHook_VolumeMediaButtons(final boolean vol2wakeEnabled) {
		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				// Power and volkeys are pressed at the same time
				if (isPowerPressed) return;
				
				final KeyEvent keyEvent = (KeyEvent)param.args[0];
				
				int keycode = keyEvent.getKeyCode();
				int action = keyEvent.getAction();
				int flags = keyEvent.getFlags();
				
				// Ignore repeated KeyEvents simulated on volume Key Up
				if ((flags & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == KeyEvent.FLAG_VIRTUAL_HARD_KEY) return;
				if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM && (keycode == KeyEvent.KEYCODE_VOLUME_UP || keycode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
					// Volume long press
					PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
					final Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					if (!mPowerManager.isScreenOn()) {
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
											AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
											boolean isMusicActive = am.isMusicActive();
											boolean isMusicActiveRemotely  = (Boolean)XposedHelpers.callMethod(am, "isMusicActiveRemotely");
											boolean isAllowed = isMusicActive || isMusicActiveRemotely;
											if (!isAllowed) {
												long mCurrentTime = System.currentTimeMillis();
												long mLastPauseTime = Settings.System.getLong(mContext.getContentResolver(), "last_music_paused_time", mCurrentTime);
												if (mCurrentTime - mLastPauseTime < 10 * 60 * 1000) isAllowed = true;
											}
											if (isAllowed)
											switch (keyEvent.getKeyCode()) {
												case KeyEvent.KEYCODE_VOLUME_UP:
													XMain.pref.reload();
													int pref_mediaUp = Integer.parseInt(XMain.pref.getString("pref_key_controls_mediaupaction", "0"));
													if (pref_mediaUp == 0) break;
													GlobalActions.sendMediaButton(mContext, new KeyEvent(KeyEvent.ACTION_DOWN, pref_mediaUp));
													GlobalActions.sendMediaButton(mContext, new KeyEvent(KeyEvent.ACTION_UP, pref_mediaUp));
													break;
												case KeyEvent.KEYCODE_VOLUME_DOWN:
													XMain.pref.reload();
													int pref_mediaDown = Integer.parseInt(XMain.pref.getString("pref_key_controls_mediadownaction", "0"));
													if (pref_mediaDown == 0) break;
													GlobalActions.sendMediaButton(mContext, new KeyEvent(KeyEvent.ACTION_DOWN, pref_mediaDown));
													GlobalActions.sendMediaButton(mContext, new KeyEvent(KeyEvent.ACTION_UP, pref_mediaDown));
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
								if (Helpers.isLP()) {
									AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
									TelecomManager tm = (TelecomManager)mContext.getSystemService(Context.TELECOM_SERVICE);
									WakeLock mBroadcastWakeLock = (WakeLock)XposedHelpers.getObjectField(param.thisObject, "mBroadcastWakeLock");
									int k = AudioManager.ADJUST_RAISE;
									if (keycode != KeyEvent.KEYCODE_VOLUME_UP) k = AudioManager.ADJUST_LOWER;
									// If music stream is playing, adjust its volume
									mBroadcastWakeLock.acquire();
									if (am.isMusicActive())
										am.adjustStreamVolume(AudioManager.STREAM_MUSIC, k, 0);
									// If voice call is active while screen off by proximity sensor, adjust its volume
									else if (tm.isInCall())
										am.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, k, 0);
									// Use vol2wake in other cases
									else if (vol2wakeEnabled)
										XposedHelpers.callMethod(mPowerManager, "wakeUpFromPowerKey", SystemClock.uptimeMillis());
									if (mBroadcastWakeLock.isHeld()) mBroadcastWakeLock.release();
								} else {
									boolean isMusicActive = (Boolean)XposedHelpers.callMethod(param.thisObject, "isMusicActive");
									boolean isInCall = (Boolean)XposedHelpers.callMethod(param.thisObject, "isInCall");
									// If music stream is playing, adjust its volume
									if (isMusicActive) XposedHelpers.callMethod(param.thisObject, "handleVolumeKey", AudioManager.STREAM_MUSIC, keycode);
									// If voice call is active while screen off by proximity sensor, adjust its volume
									else if (isInCall) XposedHelpers.callMethod(param.thisObject, "handleVolumeKey", AudioManager.STREAM_VOICE_CALL, keycode);
									// Use vol2wake in other cases
									else if (vol2wakeEnabled)
										XposedHelpers.callMethod(mPowerManager, "wakeUp", SystemClock.uptimeMillis());
								}
								param.setResult(0);
							}
							isWaitingForVolumeLongPressed = false;
						}
					}
				}
			}
		};
				
		Object[] argsAndHook = { KeyEvent.class, int.class, boolean.class, hook };
		if (Helpers.isLP()) argsAndHook = new Object[] { KeyEvent.class, int.class, hook };
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "interceptKeyBeforeQueueing", argsAndHook);
		
		findAndHookMethod("android.media.MediaPlayer", null, "pause", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					Context mContext = (Context)XposedHelpers.findMethodExact(findClass("android.media.MediaPlayer", null), "getContext").invoke(param.thisObject);
					int mStreamType = 0;
					if (Helpers.isLP())
						mStreamType = (Integer)XposedHelpers.findMethodExact(findClass("android.media.MediaPlayer", null), "getAudioStreamType").invoke(param.thisObject);
					else
						mStreamType = (Integer)XposedHelpers.getObjectField(param.thisObject, "mStreamType");
					if (mContext != null && (mStreamType == AudioManager.STREAM_MUSIC || mStreamType == 0x80000000))
					mContext.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.SaveLastMusicPausedTime"));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
		
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				IntentFilter intentfilter = new IntentFilter();
				intentfilter.addAction("com.sensetoolbox.six.mods.action.SaveLastMusicPausedTime");
				mContext.registerReceiver(new BroadcastReceiver() {
					public void onReceive(final Context context, Intent intent) {
						try {
							Settings.System.putLong(context.getContentResolver(), "last_music_paused_time", System.currentTimeMillis());
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				}, intentfilter);
			}
		});
	}

	public static void exec_SwapVolumeCCWLand() {
		try {
			if (Helpers.isLP()) {
				findAndHookMethod("android.media.AudioService", null, "adjustMasterVolume", int.class, int.class, String.class, hook_adjustVolumeParam0);
				findAndHookMethod("android.media.AudioService", null, "adjustSuggestedStreamVolume", int.class, int.class, int.class, String.class, int.class, hook_adjustVolumeParam0);
			} else if (Build.VERSION.SDK_INT >= 19) {
				findAndHookMethod("android.media.AudioService", null, "adjustMasterVolume", int.class, int.class, String.class, hook_adjustVolumeParam0);
				findAndHookMethod("android.media.AudioService", null, "adjustSuggestedStreamVolume", int.class, int.class, int.class, String.class, hook_adjustVolumeParam0);
				findAndHookMethod("android.media.AudioService", null, "adjustLocalOrRemoteStreamVolume", int.class, int.class, String.class, hook_adjustVolumeParam1);
			} else {
				findAndHookMethod("android.media.AudioService", null, "adjustMasterVolume", int.class, int.class, hook_adjustVolumeParam0);
				findAndHookMethod("android.media.AudioService", null, "adjustSuggestedStreamVolume", int.class, int.class, int.class, hook_adjustVolumeParam0);
				findAndHookMethod("android.media.AudioService", null, "adjustLocalOrRemoteStreamVolume", int.class, int.class, hook_adjustVolumeParam1);
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
							case 13: GlobalActions.switchToPrevApp(mContext); break;
							case 14: GlobalActions.openAppDrawer(mContext); break;
						}
					}
				}
			});
		}
	}
	public static void execHook_BackLongpressEight(LoadPackageParam lpparam) {
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
	private static void assignHomeLongPress(final MethodHookParam param) {
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
	public static void execHook_HomeLongpressEight(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				assignHomeLongPress(param);
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader, "onSizeChanged", int.class, int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				assignHomeLongPress(param);
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
	private static void assignRecentsLongpress(final MethodHookParam param) {
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
	public static void execHook_RecentsLongpressEight(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				assignRecentsLongpress(param);
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader, "onSizeChanged", int.class, int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				assignRecentsLongpress(param);
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
		if (!Helpers.isLP()) try {
			XResources.setSystemWideReplacement("android", "dimen", "system_bar_height", modRes.fwd(R.dimen.navigation_bar_height));
		} catch (Throwable t) {}
	}
	
	public static void execHook_FixDialer(LoadPackageParam lpparam) {
		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				RelativeLayout thisView = (RelativeLayout) param.thisObject;
				AbsListView.LayoutParams params = (AbsListView.LayoutParams) thisView.getLayoutParams();
				params.height = 503; // 449 stock + 54 pixel white bar
				thisView.setLayoutParams(params);
				thisView.invalidate();
			}
		};
		try {
			findAndHookMethod("com.htc.htcdialer.widget.ContactDetailPhotoView", lpparam.classLoader, "initContactDetailPhotoView", Context.class, hook);
			findAndHookMethod("com.htc.htcdialer.widget.ContactDetailPhotoView", lpparam.classLoader, "onConfigurationChanged", Configuration.class, hook);
			findAndHookMethod("com.htc.htcdialer.widget.ContactDetailPhotoView", lpparam.classLoader, "onLayout", boolean.class, int.class, int.class, int.class, int.class, hook);
			findAndHookMethod("com.htc.htcdialer.widget.ContactDetailPhotoView", lpparam.classLoader, "dispatchDraw", Canvas.class, hook);
		} catch (Throwable t) {}
	}
	
	public static void execHook_KeysHapticFeedback() {
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					if (XMain.pref.getBoolean("pref_key_controls_keyshaptic_enable", false)) {
						int duration_keys = XMain.pref.getInt("pref_key_controls_keyshaptic", 20);
						if (Helpers.isLP())
							XposedHelpers.setObjectField(param.thisObject, "mVirtualKeyVibePattern", new long[] { duration_keys });
						else
							XposedHelpers.setObjectField(param.thisObject, "mVirtualKeyVibePattern", new long[] { 0, duration_keys, 0, 0 });
					}
					if (XMain.pref.getBoolean("pref_key_controls_longpresshaptic_enable", false)) {
						int duration_long = XMain.pref.getInt("pref_key_controls_longpresshaptic", 21);
						if (Helpers.isLP())
							XposedHelpers.setObjectField(param.thisObject, "mLongPressVibePattern", new long[] { duration_long });
						else
							XposedHelpers.setObjectField(param.thisObject, "mLongPressVibePattern", new long[] { 0, 1, 20, duration_long });
					}
					if (XMain.pref.getBoolean("pref_key_controls_keyboardhaptic_enable", false)) {
						int duration_keyb = XMain.pref.getInt("pref_key_controls_keyboardhaptic", 20);
						XposedHelpers.setObjectField(param.thisObject, "mKeyboardTapVibePattern", new long[] { duration_keyb });
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
	
	public static void execHook_KeyboardHapticFeedback(final LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.sense.ime.HTCIMMData", lpparam.classLoader, "getACCvalue", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					int duration_keyb = XMain.pref.getInt("pref_key_controls_keyboardhaptic", 20);
					XposedHelpers.setStaticIntField(findClass("com.htc.sense.ime.HTCIMMData", lpparam.classLoader), "sVibrationDuration", duration_keyb);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
	
	public static boolean isWiredHeadsetConnected = false;
	public static boolean isBluetoothHeadsetConnected = false;
	
	public static void executeAction(Context mContext, int action, int app) {
		if (action != 1) {
			Object amn = XposedHelpers.callStaticMethod(findClass("android.app.ActivityManagerNative", null), "getDefault");
			if (Helpers.isLP())
				XposedHelpers.callMethod(amn, "keyguardWaitingForActivityDrawn");
			else
				XposedHelpers.callMethod(amn, "dismissKeyguardOnNextActivity");
			
			switch (action) {
				case 2: GlobalActions.launchApp(mContext, app); break;
				case 3: GlobalActions.launchShortcut(mContext, app); break;
			}
		}
	}
	
	public static void executeEffect(Context mContext, int effect, Object thisObject) {
		switch (effect) {
			case 2: XposedHelpers.callMethod(thisObject, "setGlobalEffect", 800, "Sense 6 Toolbox"); break;
			case 3: XposedHelpers.callMethod(thisObject, "setGlobalEffect", 900, "Sense 6 Toolbox"); break;
			case 4: XposedHelpers.callMethod(thisObject, "setGlobalEffect", 902, "Sense 6 Toolbox"); break;
		}
	}
	
	public static void handleDeviceTypeChange(MethodHookParam param) {
		int deviceType = (Integer)param.args[0];
		switch (deviceType) {
			case 128:
			case 256:
			case 512:
				boolean newBtState = XposedHelpers.getBooleanField(param.thisObject, "mBluetoothHeadsetConnected");
				if (newBtState != isBluetoothHeadsetConnected) {
					isBluetoothHeadsetConnected = newBtState;
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					XMain.pref.reload();
					int pref_btheadsetaction = 1;
					int pref_btheadsetapp = 0;
					int pref_btheadseteffect = 1;
					if (isBluetoothHeadsetConnected) {
						XposedBridge.log("[S6T] Bluetooth headset connected");
						pref_btheadsetaction = Integer.parseInt(XMain.pref.getString("pref_key_controls_btheadsetonaction", "1"));
						pref_btheadsetapp = 11;
						pref_btheadseteffect = Integer.parseInt(XMain.pref.getString("pref_key_controls_btheadsetoneffect", "1"));
					} else {
						XposedBridge.log("[S6T] Bluetooth headset disconnected");
						pref_btheadsetaction = Integer.parseInt(XMain.pref.getString("pref_key_controls_btheadsetoffaction", "1"));
						pref_btheadsetapp = 12;
						pref_btheadseteffect = Integer.parseInt(XMain.pref.getString("pref_key_controls_btheadsetoffeffect", "1"));
					}
					executeAction(mContext, pref_btheadsetaction, pref_btheadsetapp);
					executeEffect(mContext, pref_btheadseteffect, param.thisObject);
				}
				break;
			case 4:
			case 8:
				boolean newWiredState = XposedHelpers.getIntField(param.thisObject, "mHeadsetState") > 0;
				if (newWiredState != isWiredHeadsetConnected) {
					isWiredHeadsetConnected = newWiredState;
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					XMain.pref.reload();
					int pref_wiredheadsetaction = 1;
					int pref_wiredheadsetapp = 0;
					int pref_wiredheadseteffect = 1;
					if (isWiredHeadsetConnected) {
						XposedBridge.log("[S6T] Wired headset connected");
						pref_wiredheadsetaction = Integer.parseInt(XMain.pref.getString("pref_key_controls_wiredheadsetonaction", "1"));
						pref_wiredheadsetapp = 9;
						pref_wiredheadseteffect = Integer.parseInt(XMain.pref.getString("pref_key_controls_wiredheadsetoneffect", "1"));
					} else {
						XposedBridge.log("[S6T] Wired headset disconnected");
						pref_wiredheadsetaction = Integer.parseInt(XMain.pref.getString("pref_key_controls_wiredheadsetoffaction", "1"));
						pref_wiredheadsetapp = 10;
						pref_wiredheadseteffect = Integer.parseInt(XMain.pref.getString("pref_key_controls_wiredheadsetoffeffect", "1"));
					}
					executeAction(mContext, pref_wiredheadsetaction, pref_wiredheadsetapp);
					executeEffect(mContext, pref_wiredheadseteffect, param.thisObject);
				}
				break;
		}
	}
	
	public static void execHook_AccessoriesActions() {
		try {
			XposedBridge.hookAllConstructors(findClass("android.media.AudioService", null), new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					isWiredHeadsetConnected = XposedHelpers.getIntField(param.thisObject, "mHeadsetState") > 0;
					isBluetoothHeadsetConnected = XposedHelpers.getBooleanField(param.thisObject, "mBluetoothHeadsetConnected");
				}
			});
			
			findAndHookMethod("android.media.AudioService", null, "onDeviceConnected", int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					handleDeviceTypeChange(param);
				}
			});
			
			findAndHookMethod("android.media.AudioService", null, "onDeviceDisconnected", int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					handleDeviceTypeChange(param);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	/*
	public static void execHook_VolumeCaret() {
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				
				final KeyEvent keyEvent = (KeyEvent)param.args[0];
				int keycode = keyEvent.getKeyCode();
				int action = keyEvent.getAction();
				int flags = keyEvent.getFlags();
				
				if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM) {
					if (keycode == KeyEvent.KEYCODE_VOLUME_UP || keycode == KeyEvent.KEYCODE_VOLUME_DOWN) {
						InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
						Object mService = XposedHelpers.getObjectField(imm, "mService");
						boolean mInputShown = XposedHelpers.getBooleanField(mService, "mInputShown");
						if (mInputShown) {
							if (action == KeyEvent.ACTION_DOWN)
							if (keycode == KeyEvent.KEYCODE_VOLUME_UP) {
								(new Instrumentation()).sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
							} else if (keycode == KeyEvent.KEYCODE_VOLUME_DOWN) {
								(new Instrumentation()).sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
							}
							param.setResult(0);
						}
					}
				}
			}
		});
		
		findAndHookMethod("android.inputmethodservice.InputMethodService", null, "onKeyDown", int.class, KeyEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				XposedBridge.log("onKeyDown: " + String.valueOf((Integer)param.args[0]) + " | " + String.valueOf((KeyEvent)param.args[1]));
			}
		});
		
		findAndHookMethod("android.inputmethodservice.InputMethodService", null, "onKeyUp", int.class, KeyEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				XposedBridge.log("onKeyUp: " + String.valueOf((Integer)param.args[0]) + " | " + String.valueOf((KeyEvent)param.args[1]));
			}
		});
	}
	*/
}
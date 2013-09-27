package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import android.content.Context;
import android.view.KeyEvent;

import com.langerhans.one.utils.GlobalActions;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ControlsMods {
	
	private static boolean isBackLongPressed = false;
	
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
						if (pref_backlongpress != 1 && keycode == 4) {
							if (action == 0) {
								isBackLongPressed = false;
							}
							if (action == 1 && isBackLongPressed == true) {
								param.setResult(0);
							}
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
						if (pref_backlongpress != 1 && keycode == 4) {
							if (action == 0 && repeats >= 5) {
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
							if (action == 1) {
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
		final Class<?> clsHLS = findClass("com.htc.lockscreen.HtcLockScreen", lpparam.classLoader);
		findAndHookMethod(clsHLS, "launchGoogleNow", new XC_MethodHook() {
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
				if (keyCode == 24 || keyCode == 25)
					param.setResult(true);
			}
		});
	}
	
	/**
	 * Enables or diables the init script for vol2wake
	 * @param newState true to enable, false to disable
	 */
	public static void initScriptHandler(Boolean newState)
	{
		if(newState)
		{
			CommandCapture command = new CommandCapture(0,
					"mount -o rw,remount /system",
					"echo \"#!/system/bin/sh\n\necho 1 > /sys/keyboard/vol_wakeup\nchmod 444 /sys/keyboard/vol_wakeup\" > /etc/init.d/89s5tvol2wake",
					"sed -i 's/\\(key [0-9]\\+\\s\\+VOLUME_\\(DOWN\\|UP\\)$\\)/\\1   WAKE_DROPPED/gw /system/usr/keylayout/Generic.kl' /system/usr/keylayout/Generic.kl",
					"mount -o ro,remount /system");
			try {
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
		{
			CommandCapture command = new CommandCapture(0,
					"mount -o rw,remount /system",
					"rm -f /etc/init.d/89s5tvol2wake",
					"sed -i 's/\\(key [0-9]\\+\\s\\+VOLUME_\\(DOWN\\|UP\\)\\)\\s\\+WAKE_DROPPED/\\1/gw /system/usr/keylayout/Generic.kl' /system/usr/keylayout/Generic.kl",
					"mount -o ro,remount /system");
		    try {
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
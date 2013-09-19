package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import com.langerhans.one.utils.GlobalActions;

import android.content.Context;
import android.view.KeyEvent;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

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
					if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM) {
						// Back long press
						if (XMain.pref_backlongpress != 1 && keycode == 4) {
							if (action == 0) {
								isBackLongPressed = false;
							}
							if (action == 1 && isBackLongPressed == true) {
								param.setResult(-1L);
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
					if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM) {
						// Back long press
						if (XMain.pref_backlongpress != 1 && keycode == 4) {
							if (action == 0 && repeats >= 5) {
								if (isBackLongPressed == false) {
									Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
									switch (XMain.pref_backlongpress) {
										case 2: GlobalActions.expandNotifications(mContext); break;
										case 3: GlobalActions.expandEQS(mContext); break;
										case 4: GlobalActions.lockDevice(mContext); break;
										case 5: GlobalActions.goToSleep(mContext); break;
										case 6: GlobalActions.launchApp(mContext, 3); break;
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
					if (XMain.pref_homeassist != 1) {
						Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
						switch (XMain.pref_homeassist) {
							case 2: GlobalActions.expandNotifications(mContext); break;
							case 3: GlobalActions.expandEQS(mContext); break;
							case 4: GlobalActions.lockDevice(mContext); break;
							case 5: GlobalActions.goToSleep(mContext); break;
							case 6: GlobalActions.launchApp(mContext, 4); break;
						}
						param.setResult(null);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
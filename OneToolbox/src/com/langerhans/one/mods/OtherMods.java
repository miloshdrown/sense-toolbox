package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import java.lang.reflect.Method;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class OtherMods{

	public static void execHook_APM(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.htc.app.HtcShutdownThread", lpparam.classLoader, "reboot", Context.class, String.class, boolean.class, new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					startAPM((Context)param.args[0]);
					return null;
				}
			});
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static void startAPM(Context ctx){
		try {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClassName("com.langerhans.one", "com.langerhans.one.DimmedActivity");
			intent.putExtra("dialogType", 1);
			ctx.startActivity(intent);
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	public static void execHook_VolSound(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.view.VolumePanel", lpparam.classLoader, "onPlaySound", int.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(null);
			}
		});
	}
	
	public static void execHook_MTPNotif(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.providers.media.MtpService", lpparam.classLoader, "onStartCommand", Intent.class, int.class, int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					((Service)param.thisObject).stopForeground(true);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void execHook_MoveVolume(StartupParam startparam) {
		try {
			final Class<?> clsVP = XposedHelpers.findClass("com.htc.view.VolumePanel", null);
			XposedHelpers.findAndHookMethod(clsVP, "updatePanelRotationPosition", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Dialog dlg = (Dialog)XposedHelpers.getObjectField(param.thisObject, "mDialog");
					Dialog dlgEx = (Dialog)XposedHelpers.getObjectField(param.thisObject, "mDialogEx");
					
					float density = dlg.getContext().getResources().getDisplayMetrics().density;
					int orientation = dlg.getContext().getResources().getConfiguration().orientation;
					int bottomMargin = 75;
					if (orientation == 2) bottomMargin = 40;
					
					Window dlgWin = dlg.getWindow();
					Window dlgExWin = dlgEx.getWindow();
					WindowManager.LayoutParams dlgWinAttrs = dlgWin.getAttributes();
					WindowManager.LayoutParams dlgExWinAttrs = dlgExWin.getAttributes();
					dlgWinAttrs.gravity = Gravity.BOTTOM;
					dlgExWinAttrs.gravity = Gravity.BOTTOM;
					dlgWinAttrs.y = Math.round(bottomMargin * density);
					dlgExWinAttrs.y = Math.round(bottomMargin * density);
					dlgWin.setAttributes(dlgWinAttrs);
					dlgExWin.setAttributes(dlgExWinAttrs);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean updateScreenOn = false;
	private static boolean updateScreenOff = false;
	
	public static void ScreenAnim() {
		try {
			XposedHelpers.findAndHookMethod("com.android.server.power.DisplayPowerController", null, "setScreenOn", boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					if (updateScreenOff && XMain.pref_screenoff != 0) {
						param.setResult(null);
						updateScreenOff = false;
						
						ObjectAnimator mElectronBeamOffAnimator = (ObjectAnimator)XposedHelpers.getObjectField(param.thisObject, "mElectronBeamOffAnimator");
						Object mPowerState = XposedHelpers.getObjectField(param.thisObject, "mPowerState");
						
						if (!mElectronBeamOffAnimator.isStarted()) {
							float beamLvl = (Float)XposedHelpers.callMethod(mPowerState, "getElectronBeamLevel");
							if (beamLvl == 0.0F) {
								XposedHelpers.callMethod(param.thisObject, "setScreenOn", false);
							} else {
								Object displaypowerstate = mPowerState;
									if ((Boolean)XposedHelpers.callMethod(displaypowerstate, "prepareElectronBeam", XMain.pref_screenoff) && (Boolean)XposedHelpers.callMethod(mPowerState, "isScreenOn"))
									mElectronBeamOffAnimator.start();
								else
									mElectronBeamOffAnimator.end();
							}
						}
					}
				}
				
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					if ((Boolean)param.args[0] && XMain.pref_screenon != 0) {
						updateScreenOn = true;
						
						Object mPowerState = XposedHelpers.getObjectField(param.thisObject, "mPowerState");
						ObjectAnimator mElectronBeamOnAnimator = (ObjectAnimator)XposedHelpers.getObjectField(param.thisObject, "mElectronBeamOnAnimator");
					
						if (!mElectronBeamOnAnimator.isStarted()) {
							float beamLvl = (Float)XposedHelpers.callMethod(mPowerState, "getElectronBeamLevel");
							if (beamLvl == 1.0F) {
								XposedHelpers.callMethod(mPowerState, "dismissElectronBeam");
							} else {
								Object displaypowerstate = mPowerState;
								if ((Boolean)XposedHelpers.callMethod(displaypowerstate, "prepareElectronBeam", XMain.pref_screenon) && (Boolean)XposedHelpers.callMethod(mPowerState, "isScreenOn"))
									mElectronBeamOnAnimator.start();
								else
									mElectronBeamOnAnimator.end();
							}
						}
					}
				}
			});
			
			if (XMain.pref_screenoff != 0)
			XposedHelpers.findAndHookMethod("com.android.server.power.DisplayPowerController", null, "animateScreenBrightness", int.class, int.class, int.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					if ((Integer)param.args[0] == 0 && (Integer)param.args[1] == 0 && (Integer)param.args[2] == 0 && (Integer)param.args[3] == -1) {
						updateScreenOff = true;
						param.setResult(null);
					}
				}
			});
			
			if (XMain.pref_screenon != 0) {
				XposedHelpers.findAndHookMethod("com.android.server.power.DisplayPowerState", null, "dismissElectronBeam", new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
						if (updateScreenOn) param.setResult(null);
					}
				});
				
				XposedHelpers.findAndHookMethod("com.android.server.power.DisplayPowerController", null, "updatePowerState", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
						updateScreenOn = false;
	                }
	            });
			}
			
			XposedHelpers.findAndHookMethod("com.android.server.power.DisplayPowerState", null, "setElectronBeamLevel", float.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					if (updateScreenOn || updateScreenOff) param.setResult(null);
				}
			});
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void execHook_fastUnlock(final LoadPackageParam lpparam)
	{
		findAndHookMethod("com.htc.lockscreen.unlockscreen.HtcKeyInputUnlockView", lpparam.classLoader, "initView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				final Object mLockPatternUtils = getObjectField(param.thisObject, "mLockPatternUtils");
				final AutoCompleteTextView mPasswordEntry = (AutoCompleteTextView) getObjectField(param.thisObject, "mPasswordEntry");
				XposedBridge.log(mLockPatternUtils.toString() + mPasswordEntry.toString());
				if (mLockPatternUtils != null && mPasswordEntry != null)
				{
					mPasswordEntry.removeTextChangedListener((TextWatcher) getObjectField(param.thisObject, "mTextChangeListener"));
					mPasswordEntry.addTextChangedListener(new TextWatcher() {
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {}
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
						
						@Override
						public void afterTextChanged(Editable s) {
							if (mPasswordEntry.getText().length() > 3) //No need to check PINs shorter that 4
							{
								Boolean isPinCorrect = (Boolean) callMethod(mLockPatternUtils, "checkPassword", mPasswordEntry.getText().toString());
								if (isPinCorrect)
								{
									Method onEditorAction = findMethodExact("com.htc.lockscreen.unlockscreen.HtcKeyInputUnlockView", lpparam.classLoader, "onEditorAction", TextView.class, int.class, KeyEvent.class);
									try {
										onEditorAction.invoke(param.thisObject, null, 0, null);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
							if (mPasswordEntry != null && !mPasswordEntry.isEnabled() && !TextUtils.isEmpty(mPasswordEntry.getText()))
				                mPasswordEntry.setText("");
							Object mCallback = getObjectField(param.thisObject, "mCallback");
				            if (mCallback != null)
				            	callMethod(mCallback, "userActivity", 0L);
						}
					});
				}
            }
		});
	}
}

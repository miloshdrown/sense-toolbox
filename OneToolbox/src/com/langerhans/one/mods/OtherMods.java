package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
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
					if (orientation == 2) bottomMargin = 10;
					
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
	/*
	public static void CRTOff() {
        try {
            final Class<?> clsDisplayPowerState = XposedHelpers.findClass("com.android.server.power.DisplayPowerState", null);
            XposedBridge.hookAllConstructors(clsDisplayPowerState, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                	   XposedHelpers.setStaticBooleanField(param.thisObject.getClass(), "DEBUG_ON", true);
                	   XposedHelpers.setStaticBooleanField(param.thisObject.getClass(), "DEBUG", true);
                	   
                	   //Object mElectronBeam = XposedHelpers.getObjectField(param.thisObject, "mElectronBeam");
	                   //XposedHelpers.callMethod(param.thisObject, "prepareElectronBeam", 1);
	                   //boolean res = (Boolean)XposedHelpers.callMethod(mElectronBeam, "draw", 0.7f);
	                   //if (res) XposedBridge.log("draw!!!"); else XposedBridge.log("fuck no draw :(");
	                   //param.args[0] = 1;
	               }	               
	           });

            XposedHelpers.findAndHookMethod(clsDisplayPowerState, "setElectronBeamLevel", float.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                	XposedHelpers.callMethod(param.thisObject, "prepareElectronBeam", 1);
                	XposedBridge.log("setElectronBeamLevel: " + String.valueOf(param.args[0]));
                	boolean mScreenOn = (Boolean) XposedHelpers.getObjectField(param.thisObject, "mScreenOn");
                	boolean mElectronBeamPrepared = (Boolean) XposedHelpers.getObjectField(param.thisObject, "mElectronBeamPrepared");
                	if (mScreenOn) XposedBridge.log("mScreenOn = true"); else XposedBridge.log("mScreenOn = false");
                	if (mElectronBeamPrepared) XposedBridge.log("mElectronBeamPrepared = true"); else XposedBridge.log("mElectronBeamPrepared = false");
                    //param.setResult(null);
                }
            	
            });
            
            final Class<?> clsDisplaPowerController = XposedHelpers.findClass("com.android.server.power.DisplayPowerController", null);
            XposedHelpers.findAndHookMethod(clsDisplaPowerController, "initialize", new XC_MethodHook() {
            	
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                	XposedBridge.log("initialize1");
                    XposedHelpers.setStaticBooleanField(param.thisObject.getClass(), "DEBUG_ON", true);
                }
            	
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                	XposedBridge.log("initialize2");
                    ObjectAnimator oa = (ObjectAnimator) XposedHelpers.getObjectField(param.thisObject, "mElectronBeamOffAnimator");
                    if (oa != null) {
                        oa.setDuration(400);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
	} */
}

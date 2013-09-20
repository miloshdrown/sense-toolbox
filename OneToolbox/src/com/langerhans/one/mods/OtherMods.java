package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setStaticBooleanField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class OtherMods{

	public static void execHook_APM(LoadPackageParam lpparam) {
		final ClassLoader cl = lpparam.classLoader;
		XC_MethodReplacement mr = new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(final MethodHookParam param) throws Throwable 
			{
				try
				{
					final Context ctx = (Context) param.args[0];
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setClassName("com.langerhans.one", "com.langerhans.one.ApmActivity");
					
					IntentFilter filter = new IntentFilter();
					filter.addAction("ONETB_REBOOT");
					filter.addAction("ONETB_RECOVERY");
					filter.addAction("ONETB_BOOTLOADER");
					BroadcastReceiver receiver = new BroadcastReceiver() {
					   @Override
					   public void onReceive(Context context, Intent intent) {
					     if(intent.getAction().equals("ONETB_REBOOT"))
					     {
				    		setStaticObjectField(findClass("com.android.server.power.ShutdownThread", cl), "mRebootReason", "oem-11");
				    		setStaticBooleanField(findClass("com.android.server.power.ShutdownThread", cl), "mReboot", true);
				    		setStaticBooleanField(findClass("com.android.server.power.ShutdownThread", cl), "mRebootSafeMode", false);
							callStaticMethod(findClass("com.android.server.power.ShutdownThread", cl), "shutdownInner", (Context) param.args[0], false);
					     }
					     if(intent.getAction().equals("ONETB_RECOVERY"))
					     {
					    	 setStaticObjectField(findClass("com.android.server.power.ShutdownThread", cl), "mRebootReason", "recovery");
					    	 setStaticBooleanField(findClass("com.android.server.power.ShutdownThread", cl), "mReboot", true);
					    	 setStaticBooleanField(findClass("com.android.server.power.ShutdownThread", cl), "mRebootSafeMode", false);
					    	 callStaticMethod(findClass("com.android.server.power.ShutdownThread", cl), "shutdownInner", (Context) param.args[0], false);
					     }
					     if(intent.getAction().equals("ONETB_BOOTLOADER"))
					     {
					    	 setStaticObjectField(findClass("com.android.server.power.ShutdownThread", cl), "mRebootReason", "bootloader");
					    	 setStaticBooleanField(findClass("com.android.server.power.ShutdownThread", cl), "mReboot", true);
					    	 setStaticBooleanField(findClass("com.android.server.power.ShutdownThread", cl), "mRebootSafeMode", false);
					    	 callStaticMethod(findClass("com.android.server.power.ShutdownThread", cl), "shutdownInner", (Context) param.args[0], false);
					     }
					   }
					};

					ctx.registerReceiver(receiver, filter);
					
					ctx.startActivity(intent);
				}catch(Throwable t)
				{
					Log.e("AAAAA", t.toString());
				}
				return null;
			}
		};
		findAndHookMethod("com.htc.app.HtcShutdownThread", cl, "reboot", Context.class, String.class, boolean.class, mr);
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

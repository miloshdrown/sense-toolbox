package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SettingsMods {

	public static void execHook_ScreenOn(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.settings.framework.flag.feature.HtcDisplayFeatureFlags", lpparam.classLoader, "supportStayAwake", Context.class, new XC_MethodHook(){
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
		/*Unlock Sense 5.5 features in settings
		findAndHookMethod("com.android.settings.framework.flag.feature.HtcFeatureFlags", lpparam.classLoader, "getSenseVersion", new XC_MethodHook(){
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(5.5F);
			}
		});
		findAndHookMethod("com.android.settings.framework.flag.feature.HtcFeatureFlags", lpparam.classLoader, "getSenseVersionInString", new XC_MethodHook(){
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult("5.5");
			}
		});
		*/
		
		// Some additional settings.
		findAndHookMethod("com.android.settings.framework.flag.feature.HtcAboutPhoneFeatureFlags", lpparam.classLoader, "supportROMVersion", new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
		findAndHookMethod("com.android.settings.framework.flag.feature.HtcAboutPhoneFeatureFlags", lpparam.classLoader, "supportDistributionTime", new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
	}
}

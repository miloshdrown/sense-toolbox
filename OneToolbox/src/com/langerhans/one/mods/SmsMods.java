package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SmsMods{
	
	public static void execHook_smsscreenon(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.mms.MmsConfig", lpparam.classLoader, "supportBrightScreenOnNewSMS", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
	}

	public static void execHook_SmsMmsConv(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.mms.MmsConfig", lpparam.classLoader, "getMaxSMSConcatenatedNumber", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(9999);
			}
		});
	}

}

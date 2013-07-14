package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodReplacement;

public class OtherMods implements IXposedHookLoadPackage{

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.processName.equals("android"))
	        return;
		XposedBridge.log(lpparam.packageName);
		ClassLoader cl = lpparam.classLoader;
		XC_MethodReplacement mr = new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable 
			{
				try
				{
					XposedBridge.log("will call some intent now");
					Context ctx = (Context) param.args[0];
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setClassName("com.langerhans.one", "com.langerhans.one.ApmActivity");
					ctx.startActivity(intent);
				}catch(Throwable t)
				{
					Log.e("AAAAA", t.toString());
				}
				return null;
			}
		};
		findAndHookMethod("com.htc.app.HtcShutdownThread", cl, "reboot", Context.class, String.class, boolean.class, mr);
		XposedBridge.log("I hooked");
	}

}

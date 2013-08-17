package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.content.Context;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SettingsMods implements IXposedHookLoadPackage {

	private static XSharedPreferences pref;
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("com.android.settings"))
	        return;
		
		pref = new XSharedPreferences("com.langerhans.one", "one_toolbox_prefs");
		final boolean keepScreenOn = pref.getBoolean("pref_key_other_keepscreenon", false);
		
		if(keepScreenOn)
		{
			findAndHookMethod("com.android.settings.framework.flag.feature.HtcDisplayFeatureFlags", lpparam.classLoader, "supportStayAwake", Context.class, new XC_MethodHook(){
				@Override
	    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(true);
				}
			});
		}
	}

}

package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setStaticBooleanField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class OtherMods implements IXposedHookLoadPackage{

	private static XSharedPreferences pref;
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.processName.equals("android"))
	        return;
		
		pref = new XSharedPreferences("com.langerhans.one", "one_toolbox_prefs");
		final boolean apm = pref.getBoolean("pref_key_other_apm", false);
		if(!apm)
			return;
		
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

}

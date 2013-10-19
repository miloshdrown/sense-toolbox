package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import android.content.Context;
import android.net.Uri;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
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

	public static void execHook_ToastNotification(final LoadPackageParam lpparam) {
		//Sending reports
		findAndHookMethod("com.android.mms.transaction.MessagingNotification", lpparam.classLoader, "showSendNotification", Context.class, Uri.class, long.class, new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param)	throws Throwable {
				String toastMsg = ((Context) param.args[0]).getString(((Context) param.args[0]).getResources().getIdentifier("message_sent_notification", "string", "com.android.mms"));
				callStaticMethod(findClass("com.android.mms.transaction.MessagingNotification", lpparam.classLoader), "showToast", param.args[1], toastMsg);
				return null;
			}
		});
		
		//Delivery reports
		findAndHookMethod("com.android.mms.transaction.MessagingNotification", lpparam.classLoader, "showReportNotification", Context.class, int.class, int.class, long.class, long.class, boolean.class, new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param)	throws Throwable {
				String toast1 = ((Context) param.args[0]).getString((Integer) param.args[1]) + "\n";
				String toast2 = ((Context) param.args[0]).getString((Integer) param.args[2]);
				String uri;
				if((Boolean) param.args[5])
					uri = "content://mms/" + Long.toString((Long) param.args[4]);
				else
					uri = "content://sms/" + Long.toString((Long) param.args[4]);
				callStaticMethod(findClass("com.android.mms.transaction.MessagingNotification", lpparam.classLoader), "showToast", Uri.parse(uri), toast1 + toast2);
				return null;
			}
		});
	}

	public static void execHook_MmsSize(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.mms.util.SettingsManager", lpparam.classLoader, "convertMaxMmsSize", Context.class, String.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				String s = (String) param.args[1];
				if(s.equals("1"))
					param.setResult(307200);
				if(s.equals("2"))
					param.setResult(614400);
				if(s.equals("3"))
					param.setResult(1024000);
			}
		});
		if(XMain.senseVersion.equals("5.0"))
		{
			findAndHookMethod("com.android.mms.ui.MessagingPreferenceActivity", lpparam.classLoader, "convertMaxMmsSize", Context.class, String.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					String s = (String) param.args[1];
					if(s.equals("1"))
						param.setResult(307200);
					if(s.equals("2"))
						param.setResult(614400);
					if(s.equals("3"))
						param.setResult(1024000);
				}
			});
		}
		findAndHookMethod("com.android.mms.ui.MessageUtils", lpparam.classLoader, "getMMSLimit", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(new String[]{"300k", "600k", "1000k"});
			}
		});
	}
	
	public static void execHook_SmsAccents(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.mms.MmsConfig", lpparam.classLoader, "isSupportAccentConvert", new XC_MethodReplacement() {
	        @Override
	        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
	            return Boolean.TRUE;
	        }
	    });
	}
}

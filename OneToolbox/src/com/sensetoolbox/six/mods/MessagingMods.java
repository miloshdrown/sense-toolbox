package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findConstructorExact;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MessagingMods{
	
	public static void execHook_smsscreenon(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.sense.mms.MmsConfig", lpparam.classLoader, "supportBrightScreenOnNewSMS", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
	}

	public static void execHook_SmsMmsConv(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.sense.mms.MmsConfig", lpparam.classLoader, "getMaxSMSConcatenatedNumber", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(9999);
			}
		});
	}

	private static void changeToastReport(LoadPackageParam lpparam, MethodHookParam param) {
		Object sm = XposedHelpers.callStaticMethod(findClass("com.htc.sense.mms.util.SettingsManager", lpparam.classLoader), "getInstance");
		boolean isEnabled = (Boolean)XposedHelpers.callMethod(sm, "getBoolean", "pref_key_enable_received_notifications", true);
		if (isEnabled) {
			String toast1 = ((Context) param.args[0]).getString((Integer) param.args[1]) + "\n";
			String toast2 = ((Context) param.args[0]).getString((Integer) param.args[2]);
			String uri;
			if ((Boolean) param.args[5])
				uri = "content://mms/" + Long.toString((Long) param.args[4]);
			else
				uri = "content://sms/" + Long.toString((Long) param.args[4]);
			callStaticMethod(findClass("com.htc.sense.mms.transaction.MessagingNotification", lpparam.classLoader), "showToast", Uri.parse(uri), toast1 + toast2);
		}
	}
	
	public static void execHook_ToastNotification(final LoadPackageParam lpparam) {
		//Sending reports
		findAndHookMethod("com.htc.sense.mms.transaction.MessagingNotification", lpparam.classLoader, "showSendNotification", Context.class, Uri.class, long.class, new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param)	throws Throwable {
				Object sm = XposedHelpers.callStaticMethod(findClass("com.htc.sense.mms.util.SettingsManager", lpparam.classLoader), "getInstance");
				boolean isEnabled = (Boolean)XposedHelpers.callMethod(sm, "getBoolean", "pref_key_enable_sent_notifications", false);
				if (isEnabled) {
					String toastMsg = ((Context) param.args[0]).getString(((Context) param.args[0]).getResources().getIdentifier("message_sent_notification", "string", "com.htc.sense.mms"));
					callStaticMethod(findClass("com.htc.sense.mms.transaction.MessagingNotification", lpparam.classLoader), "showToast", param.args[1], toastMsg);
				}
				return null;
			}
		});
		
		//Delivery reports
		try {
			findAndHookMethod("com.htc.sense.mms.transaction.MessagingNotification", lpparam.classLoader, "showReportNotification", Context.class, int.class, int.class, long.class, long.class, boolean.class, new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param)	throws Throwable {
					changeToastReport(lpparam, param);
					return null;
				}
			});
		} catch (Throwable t) {
			findAndHookMethod("com.htc.sense.mms.transaction.MessagingNotification", lpparam.classLoader, "showReportNotification", Context.class, int.class, int.class, long.class, long.class, boolean.class, int.class, new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param)	throws Throwable {
					changeToastReport(lpparam, param);
					return null;
				}
			});
		}
	}

	public static void execHook_MmsSize(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.sense.mms.util.SettingsManager", lpparam.classLoader, "convertMaxMmsSize", Context.class, String.class, new XC_MethodHook() {
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
		findAndHookMethod("com.htc.sense.mms.ui.MessageUtils", lpparam.classLoader, "getMMSLimit", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(new String[]{"300k", "600k", "1000k"});
			}
		});
	}
	
	public static void execHook_SmsAccents(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.sense.mms.MmsConfig", lpparam.classLoader, "isSupportAccentConvert", new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
				return Boolean.TRUE;
			}
		});
	}
	
	public static void execHook_EASSecurityPartOne() {
		findAndHookMethod("android.app.admin.DevicePolicyManager", null, "isAdminActive", ComponentName.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				ComponentName who = (ComponentName) param.args[0];
				XposedBridge.log("isAdminActive() for class: " + who.getClassName() + " in package: " + who.getPackageName());
				if(who.getClassName().equalsIgnoreCase("com.htc.android.mail.eassvc.provision.EASDeviceAdmin"))
					param.setResult(true);
			}
		});
	}
	
	public static void execHook_EASSecurityPartTwo(LoadPackageParam lpparam) {
		Class<?> exchangesyncsources = findClass("com.htc.android.mail.eassvc.common.ExchangeSyncSources", lpparam.classLoader);
		Class<?> easpolicyset = findClass("com.htc.android.mail.eassvc.provision.EASPolicySet", lpparam.classLoader);
		final Class<?> easprovisiondoc = findClass("com.htc.android.mail.eassvc.provision.EASProvisionDoc", lpparam.classLoader);
		findAndHookMethod("com.htc.android.mail.eassvc.core.SyncManager", lpparam.classLoader, "downloadPolicy", exchangesyncsources, easpolicyset, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				Object provDoc = getObjectField(param.args[1], "provisionDoc");
				XposedBridge.log("Old provisionDoc: " + provDoc.toString());
			}
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Object provisionDoc = findConstructorExact(easprovisiondoc).newInstance();
				XposedBridge.log("Constructed new provisionDoc: " + provisionDoc);
				setObjectField(param.args[1], "provisionDoc", provisionDoc);
				Object provDoc = getObjectField(param.args[1], "provisionDoc");
				XposedBridge.log("New provisionDoc: " + provDoc.toString());
			}
		});
	}
}

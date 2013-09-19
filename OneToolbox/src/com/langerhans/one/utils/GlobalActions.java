package com.langerhans.one.utils;

import java.lang.reflect.Method;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;

import com.langerhans.one.R;
import com.langerhans.one.mods.XMain;

public class GlobalActions {

	public static boolean expandNotifications(Context context) {
		try {
			Object sbservice = context.getSystemService("statusbar");
			Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
			Method showsb;
			if (Build.VERSION.SDK_INT >= 17) {
				showsb = statusbarManager.getMethod("expandNotificationsPanel");
			} else {
				showsb = statusbarManager.getMethod("expand");
			}
			showsb.setAccessible(true);
			showsb.invoke(sbservice);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean expandEQS(Context context) {
		try {
			Object sbservice = context.getSystemService("statusbar");
			Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
			if (Build.VERSION.SDK_INT >= 17) {
				Method showeqs;
				showeqs = statusbarManager.getMethod("expandSettingsPanel");
				showeqs.invoke(sbservice);
				return true;
			} else return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean lockDevice(Context context) {
		try {
        	Intent intent = new Intent();
            intent.setAction("com.langerhans.one.mods.PrismMods.LockDevice");
            context.sendBroadcast(intent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean goToSleep(Context context) {
        try {
        	Intent intent = new Intent();
            intent.setAction("com.langerhans.one.mods.PrismMods.GoToSleep");
            context.sendBroadcast(intent);
        	return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
	
	public static boolean launchApp(Context context, int action) {
        try {
        	PackageManager manager = context.getPackageManager();
        	Resources toolboxRes = manager.getResourcesForApplication("com.langerhans.one");
        	
        	String not_selected = toolboxRes.getString(R.string.notselected);
        	String pkgAppName = "";
        	if (action == 1) pkgAppName = XMain.pref.getString("pref_key_prism_swipedown_app", not_selected);
        	else if (action == 2) pkgAppName = XMain.pref.getString("pref_key_prism_swipeup_app", not_selected);
        	else if (action == 3) pkgAppName = XMain.pref.getString("pref_key_controls_backlongpress_app", not_selected);
        	else if (action == 4) pkgAppName = XMain.pref.getString("pref_key_controls_homeassist_app", not_selected);
        	String[] pkgAppArray = pkgAppName.split("\\|");
        	
        	ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
        	Intent intent = new Intent(Intent.ACTION_MAIN);
        	intent.addCategory(Intent.CATEGORY_LAUNCHER);
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        	intent.setComponent(name);
        	context.startActivity(intent);
        	
        	return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
}
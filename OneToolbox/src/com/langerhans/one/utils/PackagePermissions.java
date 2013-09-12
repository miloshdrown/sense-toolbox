package com.langerhans.one.utils;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import java.util.ArrayList;
import com.langerhans.one.mods.XMain;
import de.robv.android.xposed.XC_MethodHook;

public class PackagePermissions {
	public static void initHooks() {
		try {
			final Class<?> clsPMS = findClass("com.android.server.pm.PackageManagerService", XMain.class.getClassLoader());

			findAndHookMethod(clsPMS, "grantPermissionsLPw", "android.content.pm.PackageParser$Package", boolean.class, new XC_MethodHook() {
				@SuppressWarnings("unchecked")
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					String pkgName = (String) getObjectField(param.args[0], "packageName");
					if (pkgName.equalsIgnoreCase("com.htc.launcher")){
						ArrayList<String> requestedPermissions = (ArrayList<String>) getObjectField(param.args[0], "requestedPermissions");
						param.setObjectExtra("orig_requested_permissions", requestedPermissions);
						requestedPermissions.add("android.permission.EXPAND_STATUS_BAR");
						setObjectField(param.args[0], "requestedPermissions", requestedPermissions);
					}
					
				}
				@SuppressWarnings("unchecked")
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					ArrayList<String> origRequestedPermissions = (ArrayList<String>) param.getObjectExtra("orig_requested_permissions");
					if (origRequestedPermissions != null)
					setObjectField(param.args[0], "requestedPermissions", origRequestedPermissions);
				}
			});
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}

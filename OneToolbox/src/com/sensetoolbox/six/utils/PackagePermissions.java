package com.sensetoolbox.six.utils;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import java.util.ArrayList;
import java.util.HashSet;

import com.sensetoolbox.six.mods.XMain;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class PackagePermissions {
	public static void init() {
		try {
			final Class<?> clsPMS = findClass("com.android.server.pm.PackageManagerService", XMain.class.getClassLoader());

			findAndHookMethod(clsPMS, "grantSignaturePermission", String.class, "android.content.pm.PackageParser.Package", "com.android.server.pm.BasePermission", HashSet.class,  new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Object pkg = param.args[1];
					String pkgName = (String)XposedHelpers.getObjectField(pkg, "packageName");
					if (pkgName.equalsIgnoreCase("com.sensetoolbox.six")) param.setResult(true);
				}
			});
			
			findAndHookMethod(clsPMS, "grantPermissionsLPw", "android.content.pm.PackageParser$Package", boolean.class, new XC_MethodHook() {
				@SuppressWarnings("unchecked")
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					try {
						ArrayList<String> requestedPermissions = (ArrayList<String>)getObjectField(param.args[0], "requestedPermissions");
						param.setObjectExtra("orig_requested_permissions", requestedPermissions);
						ArrayList<Boolean> requestedPermissionsRequired = (ArrayList<Boolean>)getObjectField(param.args[0], "requestedPermissionsRequired");
						param.setObjectExtra("orig_requested_permissions_required", requestedPermissionsRequired);
						
						String pkgName = (String) getObjectField(param.args[0], "packageName");
						if (pkgName.equalsIgnoreCase("com.htc.launcher")) {
							requestedPermissions.add("android.permission.EXPAND_STATUS_BAR");
							requestedPermissionsRequired.add(true);
							requestedPermissions.add("com.htc.home.personalize.permission.LAUNCH_MAIN");
							requestedPermissionsRequired.add(true);
						} else if (pkgName.equalsIgnoreCase("com.sensetoolbox.six")) {
							requestedPermissions.add("com.htc.permission.APP_DEFAULT");
							requestedPermissionsRequired.add(true);
							requestedPermissions.add("com.htc.permission.APP_PLATFORM");
							requestedPermissionsRequired.add(true);
						} else if (pkgName.equalsIgnoreCase("com.htc.sense.ime")) {
							requestedPermissions.add("android.permission.GET_TASKS");
							requestedPermissionsRequired.add(true);
						}
						
						setObjectField(param.args[0], "requestedPermissions", requestedPermissions);
						setObjectField(param.args[0], "requestedPermissionsRequired", requestedPermissionsRequired);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
				@SuppressWarnings("unchecked")
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					ArrayList<String> origRequestedPermissions = (ArrayList<String>) param.getObjectExtra("orig_requested_permissions");
					if (origRequestedPermissions != null) setObjectField(param.args[0], "requestedPermissions", origRequestedPermissions);
					ArrayList<Boolean> origRequestedPermissionsRequired = (ArrayList<Boolean>) param.getObjectExtra("orig_requested_permissions_required");
					if (origRequestedPermissionsRequired != null) setObjectField(param.args[0], "requestedPermissionsRequired", origRequestedPermissionsRequired);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
}

package com.sensetoolbox.six.utils;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import java.util.ArrayList;
import java.util.HashSet;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class PackagePermissions {
	@SuppressWarnings("unchecked")
	private static void doBefore(MethodHookParam param) {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void doAfter(MethodHookParam param) {
		try {
			ArrayList<String> origRequestedPermissions = (ArrayList<String>) param.getObjectExtra("orig_requested_permissions");
			if (origRequestedPermissions != null) setObjectField(param.args[0], "requestedPermissions", origRequestedPermissions);
			ArrayList<Boolean> origRequestedPermissionsRequired = (ArrayList<Boolean>) param.getObjectExtra("orig_requested_permissions_required");
			if (origRequestedPermissionsRequired != null) setObjectField(param.args[0], "requestedPermissionsRequired", origRequestedPermissionsRequired);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void init(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader,
								"grantSignaturePermission",
								String.class,
								"android.content.pm.PackageParser.Package",
								"com.android.server.pm.BasePermission",
								HashSet.class,
			new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Object pkg = param.args[1];
					String pkgName = (String)XposedHelpers.getObjectField(pkg, "packageName");
					if (pkgName.equalsIgnoreCase("com.sensetoolbox.six")) param.setResult(true);
				}
			});
			
			findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader,
								"verifySignaturesLP",
								"com.android.server.pm.PackageSetting",
								"android.content.pm.PackageParser.Package",
			new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Object pkg = param.args[1];
					String pkgName = (String)XposedHelpers.getObjectField(pkg, "packageName");
					if (pkgName.equalsIgnoreCase("com.sensetoolbox.six")) param.setResult(true);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		
		try {
			findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader,
								"grantPermissionsLPw",
								"android.content.pm.PackageParser$Package",
								boolean.class,
								String.class,
			new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					doBefore(param);
				}
				
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					doAfter(param);
				}
			});
		} catch (Throwable t1) {
			try {
				findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader,
									"grantPermissionsLPw",
									"android.content.pm.PackageParser$Package",
									boolean.class,
				new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						doBefore(param);
					}

					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						doAfter(param);
					}
				});
			} catch (Throwable t2) {
				XposedBridge.log(t2);
			}
		}
		
		if (Helpers.isLP())
		findAndHookMethod("com.android.server.usage.UsageStatsService.BinderService", lpparam.classLoader, "hasPermission", String.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				String pkgName = (String)param.args[0];
				if (pkgName != null && pkgName.equals("com.android.systemui")) param.setResult(true);
			}
		});
	}
}

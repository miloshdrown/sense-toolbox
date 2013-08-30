package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setIntField;
import static de.robv.android.xposed.XposedHelpers.setStaticIntField;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.drawable.Drawable;

import com.langerhans.one.R;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class PrismMods {

	public static void execHook_InvisiNav(final InitPackageResourcesParam resparam, final int transparency, String MODULE_PATH) {
		
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.htc.launcher", "drawable", "home_nav_bg", new XResources.DrawableLoader() {
			@Override
			public Drawable newDrawable(XResources res, int id) throws Throwable {
				Drawable bg = modRes.getDrawable(R.drawable.home_nav_bg);
				bg.setAlpha(transparency);
				return bg;
			}
		});
	}
	
	public static void execHook_InvisiWidget(final InitPackageResourcesParam resparam, final int transparency, String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.htc.widget.weatherclock", "drawable", "clock_weather_panel", new XResources.DrawableLoader() {
			@Override
			public Drawable newDrawable(XResources res, int id) throws Throwable {
				Drawable bg = modRes.getDrawable(R.drawable.clock_weather_panel);
				bg.setAlpha(transparency);
				return bg;
			}
		});
	}

	public static void execHook_20Folder_code(final LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.folder.Folder", lpparam.classLoader, "isFull", new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(false);
			}
		});
		
		findAndHookMethod("com.htc.launcher.folder.Folder", lpparam.classLoader, "isFull", findClass("com.htc.launcher.folder.FolderInfo", lpparam.classLoader), new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(false);
			}
		});
		
		XposedBridge.hookAllConstructors(findClass("com.htc.launcher.folder.Folder", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				setBooleanField(param.thisObject, "m_bMultiplePage", true);		
				setStaticIntField(param.thisObject.getClass(), "FOLDER_MAX_COUNT", 9999);
			}
		});
		
		findAndHookMethod("com.htc.launcher.folder.Folder", lpparam.classLoader, "setMultiplePage", boolean.class, new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
		
		findAndHookMethod("com.htc.launcher.pageview.CheckedAppsDataManager", lpparam.classLoader, "setMaxCheckedAmount", int.class, new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				setIntField(param.thisObject, "m_MaxCheckedAmount", 9999);
			}
		});
		
		
//		findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
//			@Override
//    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//				ViewGroup m_workspace = (ViewGroup) findField(findClass("com.htc.launcher.Launcher", lpparam.classLoader), "m_workspace").get(param.thisObject);
//				FrameLayout m_feedScrollView = (FrameLayout) findField(findClass("com.htc.launcher.Launcher", lpparam.classLoader), "m_feedScrollView").get(param.thisObject);
////				Method removeView = findMethodExact(findClass("com.htc.launcher.Workspace", lpparam.classLoader), "removeView");
////				removeView.invoke(m_workspace, m_feedScrollView);
//				m_workspace.removeView(m_feedScrollView);
//				findField(findClass("com.htc.launcher.Launcher", lpparam.classLoader), "m_feedScrollView").set(param.thisObject, null);
//			}
//			
//			@Override
//    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//				findField(findClass("com.htc.launcher.Launcher", lpparam.classLoader), "m_feedScrollView").set(param.thisObject, null);
//			}
//		});
		
//		findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "setupViews", new XC_MethodHook() {
//			@Override
//    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//				ViewGroup m_workspace = (ViewGroup) findField(findClass("com.htc.launcher.Launcher", lpparam.classLoader), "m_workspace").get(param.thisObject);
//				FrameLayout m_feedScrollView = (FrameLayout) m_workspace.findViewById(0x7f070070); //findField(findClass("com.htc.launcher.Launcher", lpparam.classLoader), "m_feedScrollView").get(param.thisObject);
////				Method removeView = findMethodExact(findClass("com.htc.launcher.Workspace", lpparam.classLoader), "removeView");
////				removeView.invoke(m_workspace, m_feedScrollView);
//				m_workspace.removeView(m_feedScrollView);
//				findField(findClass("com.htc.launcher.Launcher", lpparam.classLoader), "m_feedScrollView").set(param.thisObject, null);
//			}
//		});
	}

	public static void execHookTSBFix(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.bar.BarController", lpparam.classLoader, "setStatusBarTransparent", boolean.class, new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(null);
			}
		});
	}

}

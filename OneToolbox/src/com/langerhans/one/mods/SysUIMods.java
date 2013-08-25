package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.content.res.XModuleResources;
import android.view.View;
import android.widget.ImageView;

import com.langerhans.one.R;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SysUIMods{

	public static void execHook_InvisiBar(final InitPackageResourcesParam resparam, String MODULE_PATH, final int transparency) {
		resparam.res.hookLayout("com.android.systemui", "layout", "super_status_bar", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				View bg = liparam.view.findViewById(resparam.res.getIdentifier("status_bar", "id", "com.android.systemui"));
				bg.getBackground().setAlpha(transparency);
			}
		});
	}

	public static void execHook_BatteryIcon(InitPackageResourcesParam resparam, String MODULE_PATH, int battIcon) {
		XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		if (battIcon == 2) //2=b=percentage
		{
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery", modRes.fwd(R.drawable.b_stat_sys_battery));
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery_charge", modRes.fwd(R.drawable.b_stat_sys_battery_charge));
		}if (battIcon == 3) //No icon
		{
			resparam.res.hookLayout("com.android.systemui", "layout", "super_status_bar", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					ImageView batt1 = (ImageView)liparam.view.findViewById(liparam.res.getIdentifier("battery", "id", "com.android.systemui"));
					batt1.setVisibility(View.GONE);
				}
			}); 
		}
	}

	public static void execHook_MinorEQS(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.StatusBarFlag", lpparam.classLoader, "loadMinorQuickSetting", new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
	}
}

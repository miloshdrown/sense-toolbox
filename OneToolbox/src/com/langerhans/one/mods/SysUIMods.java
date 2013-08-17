package com.langerhans.one.mods;

import com.langerhans.one.R;

import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class SysUIMods implements IXposedHookZygoteInit, IXposedHookInitPackageResources{

	private static XSharedPreferences pref;
	private static String MODULE_PATH = null;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		MODULE_PATH = startupParam.modulePath;
		//XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);
		//XResources.setSystemWideReplacement("framework-htc-res", "color", "overlay_color", "#ffff0000");
	}
	
	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		if (!resparam.packageName.equals("com.android.systemui"))
	        return;
		
		pref = new XSharedPreferences("com.langerhans.one", "one_toolbox_prefs");
		
		final boolean invisibar = pref.getBoolean("pref_key_sysui_invisibar", false);
		if(invisibar)
		{
			resparam.res.setReplacement("com.android.systemui", "drawable", "status_bar_background", new XResources.DrawableLoader() {
				@Override
				public Drawable newDrawable(XResources res, int id) throws Throwable {
					return new ColorDrawable(Color.parseColor("#00000000"));
				}
			});
			
			resparam.res.setReplacement("com.android.systemui", "drawable", "super_status_bar", new XResources.DrawableLoader() {
				@Override
				public Drawable newDrawable(XResources res, int id) throws Throwable {
					return new ColorDrawable(Color.parseColor("#00000000"));
				}
			});
		}
		
		final int battIcon = Integer.parseInt(pref.getString("pref_key_sysui_battery", "1"));
		if (battIcon == 1) //Default
	    	return;
		XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		if (battIcon == 2) //2=b=percentage
		{
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery", modRes.fwd(R.drawable.b_stat_sys_battery));
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery_charge", modRes.fwd(R.drawable.b_stat_sys_battery_charge));
		}if (battIcon == 3) //No icon
		{
			resparam.res.hookLayout("com.android.systemui", "layout", "status_bar", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					ImageView batt1 = (ImageView)liparam.view.findViewById(liparam.res.getIdentifier("battery", "id", "com.android.systemui"));
					batt1.setVisibility(View.GONE);
				}
			}); 
			resparam.res.hookLayout("com.android.systemui", "layout", "super_status_bar", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					ImageView batt1 = (ImageView)liparam.view.findViewById(liparam.res.getIdentifier("battery", "id", "com.android.systemui"));
					batt1.setVisibility(View.GONE);
				}
			}); 
		}
	}
}

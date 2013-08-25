package com.langerhans.one.mods;

import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.drawable.Drawable;

import com.langerhans.one.R;

import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

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

}

package com.langerhans.one.mods;

import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

public class PrismMods implements IXposedHookInitPackageResources {
	
	private static XSharedPreferences pref;

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		if (!resparam.packageName.equals("com.htc.launcher"))
	        return;
		
		pref = new XSharedPreferences("com.langerhans.one", "one_toolbox_prefs");
		final boolean invisinav = pref.getBoolean("pref_key_prism_invisinav", false);
//		final boolean invisifolderclosed = pref.getBoolean("pref_key_prism_invisifolderclosed", false);
//		final boolean invisifolderopen = pref.getBoolean("pref_key_prism_invisifolderopen", false);
		
		if(invisinav)
		{
			resparam.res.setReplacement("com.htc.launcher", "drawable", "home_nav_bg", new XResources.DrawableLoader() {
				@Override
				public Drawable newDrawable(XResources res, int id) throws Throwable {
					return new ColorDrawable(Color.parseColor("#00000000"));
				}
			});
		}
		//For later use...
//		if(invisifolderclosed)
//		{
//			resparam.res.setReplacement("com.htc.launcher", "drawable", "home_folder_base", new XResources.DrawableLoader() {
//				@Override
//				public Drawable newDrawable(XResources res, int id) throws Throwable {
//					return new ColorDrawable(Color.parseColor("#00000000"));
//				}
//			});
//		}
//		if(invisifolderopen)
//		{
//			resparam.res.setReplacement("com.htc.launcher", "drawable", "home_expanded_panel", new XResources.DrawableLoader() {
//				@Override
//				public Drawable newDrawable(XResources res, int id) throws Throwable {
//					return new ColorDrawable(Color.parseColor("#00000000"));
//				}
//			});
//		}
	}

}

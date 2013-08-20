package com.langerhans.one.mods;

import android.widget.FrameLayout;
import android.widget.ImageView;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class PrismMods {


//		final boolean invisifolderclosed = pref.getBoolean("pref_key_prism_invisifolderclosed", false);
//		final boolean invisifolderopen = pref.getBoolean("pref_key_prism_invisifolderopen", false);
		
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

	public static void execHook_InvisiNav(final InitPackageResourcesParam resparam, final int transparency) {
		
		resparam.res.hookLayout("com.htc.launcher", "layout", "launcher", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				FrameLayout hotseat = (FrameLayout) liparam.view.findViewById(resparam.res.getIdentifier("hotseat", "id", "com.htc.launcher"));
				ImageView bg = (ImageView) hotseat.getChildAt(0);
				bg.setImageAlpha(transparency);
			}
		});
	}

}

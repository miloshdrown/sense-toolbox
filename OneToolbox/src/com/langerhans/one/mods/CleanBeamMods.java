package com.langerhans.one.mods;

import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.langerhans.one.R;

import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class CleanBeamMods{

	public static void execHook_BatteryIcon(InitPackageResourcesParam resparam, String MODULE_PATH, int battIcon) {
		XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		if (battIcon == 2) //2=b=percentage
		{
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery", modRes.fwd(R.drawable.b_stat_sys_battery));
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery_charge", modRes.fwd(R.drawable.b_stat_sys_battery_charge));
		}
		if (battIcon == 3) //3=c=circle
		{
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery", modRes.fwd(R.drawable.c_stat_sys_battery));
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery_charge", modRes.fwd(R.drawable.c_stat_sys_battery_charge));
		}
		if (battIcon == 4) //No icon
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

	public static void execHook_SignalIcon(InitPackageResourcesParam resparam, String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_5signal_0", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_5signal_0);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_5signal_1", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_5signal_1);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_5signal_2", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_5signal_2);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_5signal_3", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_5signal_3);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_5signal_4", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_5signal_4);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_5signal_5", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_5signal_5);
			}	
		});
	}

	public static void execHook_HeadphoneIcon(InitPackageResourcesParam resparam, String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_headphones", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_headphones);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_headphone_no_mic", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_headphone_no_mic);
			}	
		});
	}

	public static void execHook_BeatsIcon(String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);
		XResources.setSystemWideReplacement("com.htc.framework", "drawable", "stat_notify_beats_red", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_notify_beats_red);
			}	
		});
		XResources.setSystemWideReplacement("com.htc.framework", "drawable", "stat_sys_beats", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_beats);
			}	
		});
	}

	public static void execHook_AlarmIcon(InitPackageResourcesParam resparam, String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_notify_alarm", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_notify_alarm);
			}	
		});
	}

	public static void execHook_WiFiIcon(InitPackageResourcesParam resparam, String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_wifi_signal_0", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_wifi_signal_0);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_wifi_signal_1", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_wifi_signal_1);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_wifi_signal_2", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_wifi_signal_2);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_wifi_signal_3", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_wifi_signal_3);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_wifi_signal_4", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_wifi_signal_4);
			}	
		});
	}

	public static void execHook_ProfileIcon(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_ringer_silent", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_ringer_silent);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_ringer_vibrate", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_ringer_vibrate);
			}	
		});
	}

	public static void execHook_SyncIcon(InitPackageResourcesParam resparam, String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_sync", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_sync);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_sync_anim0", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_sync_anim0);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_sync_error", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_sync_error);
			}	
		});
	}

	public static void execHook_GpsIcon(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_gps_acquiring", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_gps_acquiring);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_gps_on", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_gps_on);
			}	
		});
	}

	public static void execHook_BtIcon(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_data_bluetooth", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_data_bluetooth);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_data_bluetooth_connected", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return modRes.getDrawable(R.drawable.stat_sys_data_bluetooth_connected);
			}	
		});
	}
}

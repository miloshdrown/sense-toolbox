package com.sensetoolbox.six.mods;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.GlobalActions;
import com.sensetoolbox.six.utils.Helpers;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class CleanBeamMods{
	private static String wifiBase = "";
	
	private static Drawable applyTheme(Drawable icon, boolean useOriginal) {
		ColorFilter cf = GlobalActions.createColorFilter(true);
		icon.clearColorFilter();
		if (!useOriginal && cf != null) icon.setColorFilter(cf);
		return icon;
	}
	
	private static Drawable applyTheme(Drawable icon) {
		return applyTheme(icon, false);
	}

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
		if (battIcon == 5) //5=kitkat_nopercent
		{
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery", modRes.fwd(R.drawable.d_stat_sys_battery));
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery_charge", modRes.fwd(R.drawable.d_stat_sys_battery_charge));
		}
		if (battIcon == 6) //6=kitkat_percent
		{
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery", modRes.fwd(R.drawable.e_stat_sys_battery));
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery_charge", modRes.fwd(R.drawable.e_stat_sys_battery_charge));
		}
		if (battIcon == 7) //7=sense6_nopercent
		{
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery", modRes.fwd(R.drawable.f_stat_sys_battery));
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery_charge", modRes.fwd(R.drawable.f_stat_sys_battery_charge));
		}
		if (battIcon == 8) //8=sense6_percent
		{
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery", modRes.fwd(R.drawable.g_stat_sys_battery));
			resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_battery_charge", modRes.fwd(R.drawable.g_stat_sys_battery_charge));
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
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_5signal_0));
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_5signal_1", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_5signal_1));
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_5signal_2", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_5signal_2));
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_5signal_3", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_5signal_3));
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_5signal_4", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_5signal_4));
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_5signal_5", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_5signal_5));
			}	
		});
		
		//Roaming
	    resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_r_5signal_0", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_r_5signal_0));
			}	
		});
	    resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_r_5signal_1", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_r_5signal_1));
			}	
		});
	    resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_r_5signal_2", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_r_5signal_2));
			}	
		});
	    resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_r_5signal_3", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_r_5signal_3));
			}	
		});
	    resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_r_5signal_4", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_r_5signal_4));
			}	
		});
	    resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_r_5signal_5", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_r_5signal_5));
			}	
		});
	}

	public static void execHook_HeadphoneIcon(InitPackageResourcesParam resparam, String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_headphones", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_headphones));
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_headphone_no_mic", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_headphone_no_mic));
			}	
		});
	}

	public static void execHook_BeatsIcon(String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);
		if (!Helpers.isM8()) {
			XResources.setSystemWideReplacement("com.htc.framework", "drawable", "stat_notify_beats_red", new XResources.DrawableLoader(){
				@Override
				public Drawable newDrawable(XResources res, int id)	throws Throwable {
					return modRes.getDrawable(R.drawable.stat_notify_beats_red);
					//applyTheme(..., (Integer.parseInt(XMain.pref.getString("pref_key_colortheme", "1")) == 5)?false:true);
				}	
			});
			XResources.setSystemWideReplacement("com.htc.framework", "drawable", "stat_sys_beats", new XResources.DrawableLoader(){
				@Override
				public Drawable newDrawable(XResources res, int id)	throws Throwable {
					return modRes.getDrawable(R.drawable.stat_sys_beats);
				}	
			});
		} else {
			XResources.setSystemWideReplacement("com.htc.framework", "drawable", "stat_sys_boomsound", new XResources.DrawableLoader(){
				@Override
				public Drawable newDrawable(XResources res, int id)	throws Throwable {
					return applyTheme(modRes.getDrawable(R.drawable.stat_sys_boomsound));
				}	
			});
		}
	}

	public static void execHook_AlarmIcon(InitPackageResourcesParam resparam, String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_notify_alarm", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_notify_alarm));
			}	
		});
	}

	public static void execHook_WiFiIcon(InitPackageResourcesParam resparam, String MODULE_PATH, final int i) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		if (i==2)
		{
			wifiBase = "stat_sys_wifi_signal_";
		} else if (i==3) {
			wifiBase = "b_stat_sys_wifi_signal_";
		}
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_wifi_signal_0", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(modRes.getIdentifier(wifiBase + "0", "drawable", "com.sensetoolbox.six")), (i == 2)?true:false);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_wifi_signal_1", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(modRes.getIdentifier(wifiBase + "1", "drawable", "com.sensetoolbox.six")), (i == 2)?true:false);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_wifi_signal_2", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(modRes.getIdentifier(wifiBase + "2", "drawable", "com.sensetoolbox.six")), (i == 2)?true:false);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_wifi_signal_3", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(modRes.getIdentifier(wifiBase + "3", "drawable", "com.sensetoolbox.six")), (i == 2)?true:false);
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_wifi_signal_4", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(modRes.getIdentifier(wifiBase + "4", "drawable", "com.sensetoolbox.six")), (i == 2)?true:false);
			}
		});
	}

	public static void execHook_ProfileIcon(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_ringer_silent", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_ringer_silent));
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_ringer_vibrate", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_ringer_vibrate));
			}	
		});
	}

	public static void execHook_SyncIcon(InitPackageResourcesParam resparam, String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_sync", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_sync));
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_sync_anim0", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_sync_anim0));
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_sync_error", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_sync_error));
			}	
		});
	}

	public static void execHook_GpsIcon(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_gps_acquiring", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_gps_acquiring));
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_gps_on", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_gps_on));
			}	
		});
	}

	public static void execHook_BtIcon(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_data_bluetooth", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_data_bluetooth));
			}	
		});
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_data_bluetooth_connected", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_data_bluetooth_connected));
			}	
		});
	}

	public static void execHook_DataIcon(InitPackageResourcesParam resparam, String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		Field[] fields = R.drawable.class.getFields();
	    HashMap<String, Integer> dataIcons = new HashMap<String, Integer>();
	    for (Field field : fields) {
	        if (field.getName().startsWith("stat_sys_data_") && !field.getName().contains("bluetooth") && !field.getName().contains("usb")) { //Because bluetooth is seperate but also stat_sys_data_*. Meh!
	            try {
					dataIcons.put(field.getName(), field.getInt(null));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
	        }
	    }
	    
	    for(final Entry<String, Integer> icon : dataIcons.entrySet())
	    {
	    	resparam.res.setReplacement("com.android.systemui", "drawable", icon.getKey(), new XResources.DrawableLoader(){
				@Override
				public Drawable newDrawable(XResources res, int id)	throws Throwable {
					return applyTheme(modRes.getDrawable(icon.getValue()));
				}	
			});
	    }
	    
	    resparam.res.setReplacement("com.android.systemui", "drawable", "stat_sys_signal_flightmode", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_signal_flightmode));
			}	
		});
	}
	
	public static void execHook_PowerSaveIcon(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.htc.htcpowermanager", "drawable", "stat_notify_power_saver", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_notify_power_saver));
			}	
		});
	}
	
	public static void execHook_ScreenshotIcon(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "stat_notify_image", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_notify_image));
			}	
		});
	}
	
	public static void execHook_USBIcon(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.settings", "drawable", "stat_sys_data_usb", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_data_usb));
			}	
		});
	}
	
	public static void execHook_NFCIcon(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.nfc", "drawable", "stat_sys_nfc_vzw", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_nfc_vzw));
			}	
		});
	}

	public static void execHook_DNDIcon(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		try {
			final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
			resparam.res.setReplacement("com.android.settings", "drawable", "stat_notify_dnd", new XResources.DrawableLoader(){
				@Override
				public Drawable newDrawable(XResources res, int id)	throws Throwable {
					return applyTheme(modRes.getDrawable(R.drawable.stat_notify_dnd));
				}	
			});
		} catch(Throwable t) {}
	}
	
	public static void execHook_MTPIcon(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		try {
			final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
			resparam.res.setReplacement("com.android.providers.media", "drawable", "stat_notify_running_services", new XResources.DrawableLoader(){
				@Override
				public Drawable newDrawable(XResources res, int id)	throws Throwable {
					return applyTheme(modRes.getDrawable(R.drawable.stat_notify_running_services));
				}	
			});
		} catch(Throwable t) {}
	}

	public static void execHook_PhoneIcons(InitPackageResourcesParam resparam,	String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.phone", "drawable", "stat_sys_phone_call", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_phone_call));
			}	
		});
		resparam.res.setReplacement("com.android.phone", "drawable", "stat_sys_speakerphone", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_sys_speakerphone));
			}	
		});
	}

	public static void execHook_TvIcon(InitPackageResourcesParam resparam, String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.htc.videohub.ui", "drawable", "stat_notify_tv", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				return applyTheme(modRes.getDrawable(R.drawable.stat_notify_tv));
			}	
		});
	}
	
	private static void hideSystemIcons(MethodHookParam param, Set<String> iconsToHide) {
		LinearLayout mStatusIcons = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mStatusIcons");
		if (mStatusIcons == null) return;
		
		for (int i = 0; i < mStatusIcons.getChildCount(); i++) {
			View statusIcon = mStatusIcons.getChildAt(i);
			if (statusIcon != null) {
				String mSlot = (String)XposedHelpers.getObjectField(statusIcon, "mSlot"); 
				if (mSlot != null) {
					if (iconsToHide.contains("1") && mSlot.equals("headset_plug") ||
						iconsToHide.contains("2") && mSlot.equals("beats_effect") ||
						iconsToHide.contains("3") && mSlot.equals("alarm_clock") ||
						iconsToHide.contains("4") && mSlot.equals("sync_active") ||
						iconsToHide.contains("5") && mSlot.equals("gps") ||
						iconsToHide.contains("6") && mSlot.equals("bluetooth") ||
						iconsToHide.contains("10") && mSlot.equals("nfc")) statusIcon.setVisibility(View.GONE);
				}
			}
		}
	}
	
	public static void execHook_HideIcons(LoadPackageParam lpparam) {
		final Set<String> iconsToHide = XMain.pref.getStringSet("pref_key_hide_icons", null);
		if (iconsToHide == null || iconsToHide.isEmpty()) return;
		
		XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "addIcon", String.class, int.class, int.class, "com.android.internal.statusbar.StatusBarIcon", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				hideSystemIcons(param, iconsToHide);
			}
		});
		
		XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "updateIcon", String.class, int.class, int.class, "com.android.internal.statusbar.StatusBarIcon", "com.android.internal.statusbar.StatusBarIcon", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				hideSystemIcons(param, iconsToHide);
			}
		});
		
		XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "updateNotificationIcons", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				LinearLayout mNotificationIcons = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mNotificationIcons");
				for (int i = 0; i < mNotificationIcons.getChildCount(); i++) {
					View notifIcon = mNotificationIcons.getChildAt(i);
					if (notifIcon != null) {
						String mSlot = (String)XposedHelpers.getObjectField(notifIcon, "mSlot");
						if (mSlot != null && iconsToHide != null) {
							if (iconsToHide.contains("7") && mSlot.equals("com.android.systemui/0x315") ||
								iconsToHide.contains("8") && mSlot.equals("com.android.settings/0x7f0201b4") ||
								iconsToHide.contains("9") && mSlot.equals("com.htc.htcpowermanager/0x3e8") ||
								iconsToHide.contains("11") && mSlot.equals("com.android.settings/0x1")) notifIcon.setVisibility(View.GONE);
						}
					}
				}
			}
		});
	}
}

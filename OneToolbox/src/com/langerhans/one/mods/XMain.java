package com.langerhans.one.mods;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XMain implements IXposedHookInitPackageResources, IXposedHookZygoteInit, IXposedHookLoadPackage {

	private static String MODULE_PATH = null;
	private static XSharedPreferences pref;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		MODULE_PATH = startupParam.modulePath;

		pref = new XSharedPreferences("com.langerhans.one", "one_toolbox_prefs");

		if(pref.getBoolean("pref_key_cb_beats", false))
			CleanBeamMods.execHook_BeatsIcon(MODULE_PATH);
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		String pkg = resparam.packageName;
		
		if (pkg.equals("com.htc.launcher"))
		{
			if(pref.getInt("pref_key_prism_invisinav_new", 101) != 101)
			{
				int transparency = pref.getInt("pref_key_prism_invisinav_new", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				PrismMods.execHook_InvisiNav(resparam, transparency, MODULE_PATH);
			}
			
			if(pref.getInt("pref_key_prism_invisidrawer", 100) != 100)
			{
				int transparency = pref.getInt("pref_key_prism_invisidrawer", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				PrismMods.execHook_InvisiDrawerLayout(resparam, transparency, MODULE_PATH);
			}
			
			PrismMods.execHook_AppDrawerGridSizesLayout(resparam, MODULE_PATH);
		}
		
		if (pkg.equals("com.android.systemui"))
		{
			if(pref.getInt("pref_key_sysui_invisibar_new", 101) != 101)
			{
				int transparency = pref.getInt("pref_key_sysui_invisibar_new", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				SysUIMods.execHook_InvisiBar(resparam, MODULE_PATH, transparency);
			}
			
			if(pref.getInt("pref_key_sysui_invisinotify", 100) != 100)
			{
				int transparency = pref.getInt("pref_key_sysui_invisinotify", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				SysUIMods.execHook_InvisiNotify(resparam, MODULE_PATH, transparency);
			}
			
			if(Integer.parseInt(pref.getString("pref_key_sysui_battery", "1")) != 1)
				CleanBeamMods.execHook_BatteryIcon(resparam, MODULE_PATH, Integer.parseInt(pref.getString("pref_key_sysui_battery", "1")));
			
			if(pref.getBoolean("pref_key_cb_signal", false))
				CleanBeamMods.execHook_SignalIcon(resparam, MODULE_PATH);
			
			if(pref.getBoolean("pref_key_cb_headphone", false))
				CleanBeamMods.execHook_HeadphoneIcon(resparam, MODULE_PATH);
						
			if(pref.getBoolean("pref_key_cb_alarm", false))
				CleanBeamMods.execHook_AlarmIcon(resparam, MODULE_PATH);

			if(pref.getBoolean("pref_key_cb_wifi", false))
				CleanBeamMods.execHook_WiFiIcon(resparam, MODULE_PATH);
			
			if(pref.getBoolean("pref_key_sysui_centerclock", false))
				SysUIMods.execHook_CenterClockLayout(resparam, MODULE_PATH);
			
			if(pref.getBoolean("pref_key_sysui_noeqs", false))
				SysUIMods.execHook_DisableEQS(resparam);
		}
		
		if (pkg.equals("com.htc.widget.weatherclock"))
		{
			if(pref.getInt("pref_key_prism_invisiwidget", 100) != 100)
			{
				int transparency = pref.getInt("pref_key_prism_invisiwidget", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				PrismMods.execHook_InvisiWidget(resparam, transparency, MODULE_PATH);
			}
		}
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		String pkg = lpparam.packageName;
		
		if(pkg.equals("com.android.mms"))
		{
			if(pref.getBoolean("pref_key_other_smscreenon", false))
				SmsMods.execHook_smsscreenon(lpparam);
			
			if(pref.getBoolean("pref_key_sms_smsmmsconv", false))
				SmsMods.execHook_SmsMmsConv(lpparam);
		}
		
		if(pkg.equals("com.htc.launcher"))
		{
			if(pref.getBoolean("pref_key_prism_folder20", false))
				PrismMods.execHook_20Folder_code(lpparam);
			
			if(pref.getInt("pref_key_sysui_invisibar_new", 100) != 100)
			{
				PrismMods.execHookTSBFix(lpparam);
			}
			
			if(pref.getInt("pref_key_prism_invisidrawer", 100) != 100)
			{
				PrismMods.execHook_InvisiDrawerCode(lpparam);
			}
			
			if(pref.getBoolean("pref_key_prism_bfremove", false))
				PrismMods.execHook_BfRemove(lpparam);
			
			if(pref.getBoolean("pref_key_prism_infiniscroll", false))
				PrismMods.execHook_InfiniScroll(lpparam);
			
			if(pref.getBoolean("pref_key_prism_adnoclock", false))
				PrismMods.execHook_AppDrawerNoClock(lpparam);
			
			PrismMods.execHook_AppDrawerGridSizes(lpparam);
		}
		
		if (pkg.equals("com.android.settings"))
		{
			if(pref.getBoolean("pref_key_other_keepscreenon", false))
				SettingsMods.execHook_ScreenOn(lpparam);
		}
		
		if (pkg.equals("com.android.camera"))
		{
			int voldown = Integer.parseInt(pref.getString("pref_key_cam_voldown", "4"));
		    int volup = Integer.parseInt(pref.getString("pref_key_cam_volup", "4"));
		    if (!(voldown == 4 && volup == 4))
		    	CamMods.execHook_VolKey(lpparam, volup, voldown);
		}
		
		if (pkg.equals("com.android.systemui"))
		{
			if(pref.getBoolean("pref_key_sysui_minorqs", false))
				SysUIMods.execHook_MinorEQS(lpparam);
			
			if(pref.getBoolean("pref_key_sysui_aosprecent", false))
				SysUIMods.execHook_AospRecent(lpparam);
			
			if(pref.getBoolean("pref_key_sysui_recentappsclear", false))
				SysUIMods.execHook_RecentAppsClear(lpparam);
			
			if(pref.getBoolean("pref_key_sysui_centerclock", false))
				SysUIMods.execHook_CenterClockAnimation(lpparam);
		}
		
		if (lpparam.processName.equals("android"))
		{
			if(pref.getBoolean("pref_key_other_apm", false))
				OtherMods.execHook_APM(lpparam);
			
			if(pref.getBoolean("pref_key_other_volsound", false))
				OtherMods.execHook_VolSound(lpparam);
		}
	}

}
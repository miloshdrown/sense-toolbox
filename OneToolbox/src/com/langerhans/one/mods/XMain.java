package com.langerhans.one.mods;

import com.langerhans.one.utils.GlobalActions;
import com.langerhans.one.utils.PackagePermissions;
import com.langerhans.one.utils.Version;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XMain implements IXposedHookInitPackageResources, IXposedHookZygoteInit, IXposedHookLoadPackage {

	public static XSharedPreferences pref;
	public static String MODULE_PATH = null;
	private static int pref_swipedown = 1;
	private static int pref_swipeup = 1;
	private static int pref_swiperight = 1;
	private static int pref_swipeleft = 1;
	private static int pref_backlongpress = 1;
	private static int pref_homeassist = 1;
	public static Version senseVersion;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		MODULE_PATH = startupParam.modulePath;

		pref = new XSharedPreferences("com.langerhans.one", "one_toolbox_prefs");
		
		senseVersion = new Version(pref.getString("pref_sense_version", "5.0"));
		
		if(pref.getBoolean("pref_key_cb_beats", false))
			CleanBeamMods.execHook_BeatsIcon(MODULE_PATH);
		
		if(pref.getBoolean("pref_key_other_movevol", false))
			OtherMods.execHook_MoveVolume(startupParam);
		
		pref_swipedown = Integer.parseInt(pref.getString("pref_key_prism_swipedownaction", "1"));
		pref_swipeup = Integer.parseInt(pref.getString("pref_key_prism_swipeupaction", "1"));
		pref_swiperight = Integer.parseInt(pref.getString("pref_key_prism_swiperightaction", "1"));
		pref_swipeleft = Integer.parseInt(pref.getString("pref_key_prism_swipeleftaction", "1"));
		pref_backlongpress = Integer.parseInt(pref.getString("pref_key_controls_backlongpressaction", "1"));
		pref_homeassist = Integer.parseInt(pref.getString("pref_key_controls_homeassistaction", "1"));
		
		if (pref.getBoolean("pref_key_other_apm", false) || pref_swipedown != 1 || pref_swipeup != 1 || pref_swiperight != 1 || pref_swipeleft != 1 || pref_backlongpress != 1 || pref_homeassist != 1) {
			PackagePermissions.initHooks();
			GlobalActions.setupPWM();
		}
		
		if (pref_backlongpress != 1 || pref_homeassist != 1)
			ControlsMods.setupPWMKeys();
		
		//For CRT
		//CrtTest.CrtAOSP(startupParam);
		//XResources.setSystemWideReplacement("android", "bool", "config_animateScreenLights", false);
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		String pkg = resparam.packageName;
		
		if (pkg.equals("com.htc.launcher"))
		{
			if(pref.getBoolean("pref_key_prism_invisinav_enable", false))
			{
				int transparency = pref.getInt("pref_key_prism_invisinav_new", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				PrismMods.execHook_InvisiNav(resparam, transparency, MODULE_PATH);
			}

			if(pref.getBoolean("pref_key_prism_invisifolders_enable", false))
			{
				int transparency = pref.getInt("pref_key_prism_invisifolders", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				PrismMods.execHook_InvisiFolder(resparam, transparency);
			}

			if(pref.getBoolean("pref_key_prism_invisifoldersbkg_enable", false))
			{
				int transparency = pref.getInt("pref_key_prism_invisifoldersbkg", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				PrismMods.execHook_InvisiFolderBkg(resparam, transparency, MODULE_PATH);
			}
			
			PrismMods.execHook_AppDrawerGridSizesLayout(resparam, MODULE_PATH);
		}
		
		if (pkg.equals("com.android.systemui"))
		{
			if(pref.getBoolean("pref_key_sysui_invisibar_enable", false))
			{
				int transparency = pref.getInt("pref_key_sysui_invisibar_new", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				SysUIMods.execHook_InvisiBar(resparam, MODULE_PATH, transparency);
			}
			
			if(pref.getBoolean("pref_key_sysui_invisinotify_enable", false))
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
			
			if(pref.getBoolean("pref_key_cb_profile", false))
				CleanBeamMods.execHook_ProfileIcon(resparam, MODULE_PATH);
			
			if(pref.getBoolean("pref_key_cb_sync", false))
				CleanBeamMods.execHook_SyncIcon(resparam, MODULE_PATH);
			
			if(pref.getBoolean("pref_key_cb_gps", false))
				CleanBeamMods.execHook_GpsIcon(resparam, MODULE_PATH);
			
			if(pref.getBoolean("pref_key_cb_bt", false))
				CleanBeamMods.execHook_BtIcon(resparam, MODULE_PATH);

			if(pref.getBoolean("pref_key_cb_data", false))
				CleanBeamMods.execHook_DataIcon(resparam, MODULE_PATH);
			
			if(pref.getBoolean("pref_key_cb_screenshot", false))
				CleanBeamMods.execHook_ScreenshotIcon(resparam, MODULE_PATH);
						
			if(Integer.parseInt(pref.getString("pref_key_sysui_clockstyle", "1")) == 2)
				SysUIMods.execHook_CenterClockLayout(resparam, MODULE_PATH);
		}
		
		if (pkg.equals("com.android.settings"))
		{
			if(pref.getBoolean("pref_key_cb_usb", false))
				CleanBeamMods.execHook_USBIcon(resparam, MODULE_PATH);
		}
		
		if (pkg.equals("com.htc.htcpowermanager"))
		{
			if(pref.getBoolean("pref_key_cb_powersave", false))
				CleanBeamMods.execHook_PowerSaveIcon(resparam, MODULE_PATH);
		}
		
		if (pkg.equals("com.android.nfc"))
		{
			if(pref.getBoolean("pref_key_cb_nfc", false))
				CleanBeamMods.execHook_NFCIcon(resparam, MODULE_PATH);
		}
		
		if (pkg.equals("com.htc.widget.weatherclock"))
		{
			if(pref.getBoolean("pref_key_prism_invisiwidget_enable", false))
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
		
		if(pkg.equals("com.android.providers.media")) {
			if(pref.getBoolean("pref_key_other_mtpnotif", false))
				OtherMods.execHook_MTPNotif(lpparam);
		}
		
		if(pkg.equals("com.android.mms"))
		{
			if(pref.getBoolean("pref_key_other_smscreenon", false))
				SmsMods.execHook_smsscreenon(lpparam);
			
			if(pref.getBoolean("pref_key_sms_smsmmsconv", false))
				SmsMods.execHook_SmsMmsConv(lpparam);
			
			if(pref.getBoolean("pref_key_sms_toastnotification", false))
				SmsMods.execHook_ToastNotification(lpparam);
			
			if(pref.getBoolean("pref_key_sms_mmssize", false))
				SmsMods.execHook_MmsSize(lpparam);
			
			if(pref.getBoolean("pref_key_sms_accents", false))
				SmsMods.execHook_SmsAccents(lpparam);
		}
		
		if(pkg.equals("com.htc.launcher"))
		{
			if(pref.getBoolean("pref_key_prism_invisinav_enable", false))
				PrismMods.execHook_PreserveWallpaper(lpparam);

			if(pref.getBoolean("pref_key_prism_folder20", false))
				PrismMods.execHook_20Folder_code(lpparam);
			
			if(pref.getBoolean("pref_key_sysui_invisibar_enable", false))
				PrismMods.execHookTSBFix(lpparam);
			
			if(pref.getBoolean("pref_key_prism_invisidrawer_enable", false))
			{
				int transparency = pref.getInt("pref_key_prism_invisidrawer", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				PrismMods.execHook_InvisiDrawerCode(lpparam, transparency);
			}
			
			if(pref.getBoolean("pref_key_prism_bfremove", false))
				PrismMods.execHook_BfRemove(lpparam);
			
			if(pref.getBoolean("pref_key_prism_infiniscroll", false))
				PrismMods.execHook_InfiniScroll(lpparam);
			
			if(pref.getBoolean("pref_key_prism_adnoclock", false))
				PrismMods.execHook_AppDrawerNoClock(lpparam);
			
			if (pref_swipedown != 1 || pref_swipeup != 1)
				PrismMods.execHook_SwipeActions(lpparam);
			
			PrismMods.execHook_AppDrawerGridSizes(lpparam);
			
			if(pref.getBoolean("pref_key_prism_gridtinyfont", false))
				PrismMods.execHook_AppDrawerGridTinyText(lpparam);
			
			if (pref_swiperight != 1 || pref_swipeleft != 1 || pref.getBoolean("pref_key_prism_homemenu", false))
				PrismMods.execHook_DockSwipe(lpparam);
			
			if(pref.getBoolean("pref_key_prism_homemenu", false))
				PrismMods.execHook_HomeMenu(lpparam);
			
			if(pref.getBoolean("pref_key_prism_sevenscreens", false))
				PrismMods.execHook_SevenScreens(lpparam);
		}
		
		if (pkg.equals("com.htc.lockscreen")) {
			if (pref_homeassist != 1)
				ControlsMods.execHook_dieGoogleNow(lpparam);
		}		
		
		if (pkg.equals("com.android.settings"))
		{
			if(pref.getBoolean("pref_key_other_keepscreenon", false))
				SettingsMods.execHook_ScreenOn(lpparam);

			if(pref.getBoolean("pref_key_other_appdetails", false))
				SettingsMods.execHook_Apps(lpparam);
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
			if(pref.getBoolean("pref_key_sysui_noeqs", false))
				SysUIMods.execHook_DisableEQS(lpparam);
			
			if(pref.getBoolean("pref_key_sysui_minorqs", false))
				SysUIMods.execHook_MinorEQS(lpparam, pref.getBoolean("pref_key_sysui_minorqs_notext", false));
			
			if(pref.getBoolean("pref_key_sysui_aosprecent", false))
				SysUIMods.execHook_AospRecent(lpparam);
			
			if(pref.getBoolean("pref_key_sysui_recentappsclear", false))
				SysUIMods.execHook_RecentAppsClear(lpparam);
			
			if(Integer.parseInt(pref.getString("pref_key_sysui_clockstyle", "1")) == 2)
				SysUIMods.execHook_CenterClockAnimation(lpparam);
			if(Integer.parseInt(pref.getString("pref_key_sysui_clockstyle", "1")) == 3)
				SysUIMods.execHook_ClockRemove(lpparam);
			
			if(pref.getBoolean("pref_key_sysui_ampmremove", false))
				SysUIMods.execHook_removeAMPM(lpparam);
			
			if(pref.getBoolean("pref_key_sysui_brightslide", false))
				SysUIMods.execHook_BrightnessSlider(lpparam, MODULE_PATH);
			
			if(pref.getBoolean("pref_key_sysui_dataratestatus", false))
				SysUIMods.execHook_DataRateStatus(lpparam);
		}
		
		if (lpparam.processName.equals("android"))
		{
			if(pref.getBoolean("pref_key_other_apm", false))
				OtherMods.execHook_APM(lpparam);
			
			if(pref.getBoolean("pref_key_other_volsound", false))
				OtherMods.execHook_VolSound(lpparam);
			
			if (pref.getBoolean("pref_key_controls_vol2wake", false))
				ControlsMods.execHook_Vol2Wake(lpparam);
		}
	}
}

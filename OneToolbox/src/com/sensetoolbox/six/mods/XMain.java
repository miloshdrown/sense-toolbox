package com.sensetoolbox.six.mods;

import android.content.res.XResources;
import android.os.Build;

import com.sensetoolbox.six.utils.GlobalActions;
import com.sensetoolbox.six.utils.PackagePermissions;
import com.sensetoolbox.six.utils.Version;

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
	private static int pref_shake = 1;
	public static int pref_screenon = 0;
	public static int pref_screenoff = 0;
	public static int pref_mediaDown = 0;
	public static int pref_mediaUp = 0;
	public static Version senseVersion;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		MODULE_PATH = startupParam.modulePath;

		pref = new XSharedPreferences("com.sensetoolbox.six", "one_toolbox_prefs");
		
		senseVersion = new Version(pref.getString("pref_sense_version", "5.0"));
		
		PackagePermissions.init();
		
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
		pref_shake = Integer.parseInt(pref.getString("pref_key_prism_shakeaction", "1"));
		
		if (pref.getBoolean("pref_key_other_apm", false) || pref.getBoolean("pref_key_prism_homemenu", false) || pref_swipedown != 1 || pref_swipeup != 1 || pref_swiperight != 1 || pref_swipeleft != 1 || pref_backlongpress != 1 || pref_homeassist != 1 || pref_shake != 1)
			GlobalActions.setupPWM();
		
		if (pref_backlongpress != 1 || pref_homeassist != 1)
			ControlsMods.setupPWMKeys();
		
		pref_screenon = Integer.parseInt(pref.getString("pref_key_other_screenon", "0"));
		pref_screenoff = Integer.parseInt(pref.getString("pref_key_other_screenoff", "0"));
		
		if (pref_screenon != 0 || pref_screenoff != 0) {
			XResources.setSystemWideReplacement("android", "bool", "config_animateScreenLights", false);
			OtherMods.ScreenAnim();
		}
		
		if (pref.getBoolean("pref_key_other_volsafe", false))
			XResources.setSystemWideReplacement("android", "bool", "config_safe_media_volume_enabled", false);
		
		if (pref.getBoolean("pref_key_other_oldtoasts", false))
			OtherMods.exec_OldStyleToasts(MODULE_PATH);
		
		if (pref.getBoolean("pref_key_other_securelock", false))
			OtherMods.execHook_EnhancedSecurity();
		
		if (pref.getBoolean("pref_key_other_keyslight_auto", false))
			GlobalActions.buttonBacklight();
		
		if (pref.getBoolean("themes_active", false))
			SysUIMods.execHook_Sense6ColorControl();
		
		GlobalActions.easterEgg();
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		String pkg = resparam.packageName;
		
		if (pkg.equals("com.htc.launcher"))
		{
			if(pref.getBoolean("pref_key_prism_invisidrawer_enable", false))
				PrismMods.execHook_InvisiDrawerRes(resparam);
			
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
			
			if(pref.getBoolean("pref_key_prism_4x5homescreen", false))
				PrismMods.execHook_HomeScreenGridSize(resparam, MODULE_PATH);
			
			PrismMods.execHook_AppDrawerGridSizesLayout(resparam, MODULE_PATH);
		}
		
		if (pkg.equals("com.android.systemui"))
		{
			if(pref.getBoolean("pref_key_sysui_invisinotify_enable", false))
			{
				int transparency = pref.getInt("pref_key_sysui_invisinotify", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				SysUIMods.execHook_InvisiNotify(resparam, transparency);
			}
			
			if(pref.getBoolean("pref_key_sysui_invisibar_enable", false))
			{
				int transparency = pref.getInt("pref_key_sysui_invisibar_new", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				SysUIMods.execHook_InvisiBar(resparam, MODULE_PATH, transparency);
			}
			
			if(Integer.parseInt(pref.getString("pref_key_sysui_battery", "1")) != 1)
				CleanBeamMods.execHook_BatteryIcon(resparam, MODULE_PATH, Integer.parseInt(pref.getString("pref_key_sysui_battery", "1")));
			
			if(pref.getBoolean("pref_key_cb_signal", false))
				CleanBeamMods.execHook_SignalIcon(resparam, MODULE_PATH);
			
			if(pref.getBoolean("pref_key_cb_headphone", false))
				CleanBeamMods.execHook_HeadphoneIcon(resparam, MODULE_PATH);
						
			if(pref.getBoolean("pref_key_cb_alarm", false))
				CleanBeamMods.execHook_AlarmIcon(resparam, MODULE_PATH);

			if(Integer.parseInt(pref.getString("pref_key_cb_wifi_multi", "1")) != 1)
				CleanBeamMods.execHook_WiFiIcon(resparam, MODULE_PATH, Integer.parseInt(pref.getString("pref_key_cb_wifi_multi", "1")));
			
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
			
			int headerClock = Integer.parseInt(pref.getString("pref_key_sysui_headerclick", "1"));
			if(headerClock >= 2)
				SysUIMods.execHook_NotifDrawerHeaderClock(resparam, headerClock);
		}
		
		if (pkg.equals("com.android.settings"))
		{
			if(pref.getBoolean("pref_key_cb_usb", false))
				CleanBeamMods.execHook_USBIcon(resparam, MODULE_PATH);
			
			if(pref.getBoolean("pref_key_cb_dnd", false))
				CleanBeamMods.execHook_DNDIcon(resparam, MODULE_PATH);
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
		
		if (pkg.equals("com.android.providers.media")) 
		{
			if(pref.getBoolean("pref_key_cb_mtp", false))
				CleanBeamMods.execHook_MTPIcon(resparam, MODULE_PATH);
		}
		
		if (pkg.equals("com.android.phone"))
		{
			if(pref.getBoolean("pref_key_cb_phone", false))
				CleanBeamMods.execHook_PhoneIcons(resparam, MODULE_PATH);
			
			int largePhoto = Integer.parseInt(pref.getString("pref_key_other_largephoto", "1"));
			if (largePhoto > 1)
				OtherMods.execHook_LargePhoto(resparam, largePhoto);
		}
		
		if (pkg.equals("com.htc.videohub.ui")) 
		{
			if(pref.getBoolean("pref_key_cb_tv", false))
				CleanBeamMods.execHook_TvIcon(resparam, MODULE_PATH);
		}
		
		if (pkg.equals("com.google.android.youtube"))
		{
			if (pref.getBoolean("pref_key_other_ytwatermark", false))
				OtherMods.execHook_YouTubeNoWatermark(resparam);
		}
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		String pkg = lpparam.packageName;
		
		if(pkg.equals("com.sensetoolbox.six")) {
			GlobalActions.toolboxInit(lpparam);
		}
		
		if(pkg.equals("com.android.providers.media")) {
			if(pref.getBoolean("pref_key_other_mtpnotif", false))
				OtherMods.execHook_MTPNotif(lpparam);
		}
		
		if(pkg.equals("com.htc.sense.mms"))
		{
			if(pref.getBoolean("pref_key_other_smscreenon", false))
				MessagingMods.execHook_smsscreenon(lpparam);
			
			if(pref.getBoolean("pref_key_sms_smsmmsconv", false))
				MessagingMods.execHook_SmsMmsConv(lpparam);
			
			if(pref.getBoolean("pref_key_sms_toastnotification", false))
				MessagingMods.execHook_ToastNotification(lpparam);
			
			if(pref.getBoolean("pref_key_sms_mmssize", false))
				MessagingMods.execHook_MmsSize(lpparam);
			
			if(pref.getBoolean("pref_key_sms_accents", false))
				MessagingMods.execHook_SmsAccents(lpparam);
		}
		
		if(pkg.equals("com.htc.launcher"))
		{
			if(pref.getBoolean("pref_key_prism_invisiwidget_enable", false))
			{
				int transparency = pref.getInt("pref_key_prism_invisiwidget", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				PrismMods.execHook_InvisiWidget(lpparam, transparency);
			}
			
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
			
			if(pref_swipedown != 1 || pref_swipeup != 1)
				PrismMods.execHook_SwipeActions(lpparam);
			
			PrismMods.execHook_AppDrawerGridSizes(lpparam);
			
			if(pref.getBoolean("pref_key_prism_gridtinyfont", false))
				PrismMods.execHook_AppDrawerGridTinyText(lpparam);
			
			if(pref_swiperight != 1 || pref_swipeleft != 1 || pref.getBoolean("pref_key_prism_homemenu", false))
				PrismMods.execHook_DockSwipe(lpparam);
			
			if(pref.getBoolean("pref_key_prism_homemenu", false)) {
				PrismMods.execHook_HomeMenu(lpparam);
				PrismMods.execHook_LauncherLock(lpparam);
			}
			
			if(pref.getBoolean("pref_key_prism_sevenscreens", false))
				PrismMods.execHook_SevenScreens(lpparam);
			
			if(pref.getBoolean("pref_key_prism_4x5homescreen", false))
				PrismMods.execHook_HomeScreenResizableWidgets(lpparam);
			
			if(pref.getBoolean("pref_key_prism_invisilabels", false))
				PrismMods.execHook_invisiLabels(lpparam);
			
			if(Build.VERSION.SDK_INT >= 19 && pref.getBoolean("pref_key_sysui_invisibar_enable", false)) //Le KitKat
				PrismMods.fixInvisibarKitKat(lpparam);
			
			if(pref_shake != 1)
				PrismMods.execHook_ShakeAction(lpparam);
			
			if(pref.getBoolean("themes_active", false))
				PrismMods.execHook_Sense6ColorControlLauncher(lpparam);

			//PrismMods.execHook_hotseatToggleBtn(lpparam);
		}
		
		if (pkg.equals("com.htc.lockscreen")) {
			if (pref_homeassist != 1)
				ControlsMods.execHook_dieGoogleNow(lpparam);
			
			if (pref.getBoolean("pref_key_other_fastunlock", false))
				OtherMods.execHook_fastUnlock(lpparam);
		}
		
		if (pkg.equals("com.android.settings"))
		{
			if(pref.getBoolean("pref_key_other_keepscreenon", false))
				SettingsMods.execHook_ScreenOn(lpparam);

			if(pref.getBoolean("pref_key_other_appdetails", false))
				SettingsMods.execHook_Apps(lpparam);
			
			SettingsMods.execHook_AppFilter(lpparam);
			SettingsMods.execHook_UnhidePrefs(lpparam);
		}
		
		if (pkg.equals("com.android.camera"))
		{
			int voldown = Integer.parseInt(pref.getString("pref_key_controls_camdownaction", "4"));
		    int volup = Integer.parseInt(pref.getString("pref_key_controls_camupaction", "4"));
		    if (!(voldown == 4 && volup == 4))
		    	CamMods.execHook_VolKey(lpparam, volup, voldown);
		}
		
		if (pkg.equals("com.android.systemui"))
		{
			if(pref.getBoolean("pref_key_sysui_invisinotify_enable", false))
			{
				int transparency = pref.getInt("pref_key_sysui_invisinotify", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				SysUIMods.execHook_InvisiNotifyCode(lpparam, transparency);
			}
			
			if(pref.getBoolean("pref_key_sysui_noeqs", false))
				SysUIMods.execHook_DisableEQS(lpparam);
			
			if(pref.getBoolean("pref_key_sysui_minorqs", false))
			{
				SysUIMods.execHook_MinorEQS(lpparam, pref.getBoolean("pref_key_sysui_minorqs_notext", false));
				SysUIMods.execHook_hEQSLongClick(lpparam);
			}
			
			if(pref.getBoolean("pref_key_sysui_aosprecent", false))
				SysUIMods.execHook_AospRecent(lpparam);
			
			SysUIMods.execHook_RecentAppsInit(lpparam);
			
			if(pref.getBoolean("pref_key_sysui_recentappsclear", false)) {
				SysUIMods.execHook_RecentAppsClearTouch(lpparam);	
			}
			
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
			
			if(pref.getBoolean("pref_key_sysui_recentram", false))
				SysUIMods.execHook_RAMInRecents(lpparam);
			
			if(pref.getBoolean("pref_key_sysui_alarmnotify", false))
				SysUIMods.execHook_AlarmNotification(lpparam);
			
			if (Build.VERSION.SDK_INT >= 19 && pref.getBoolean("pref_key_sysui_invisibar_enable", false))
				SysUIMods.execHookTSB442Fix(lpparam);

			if(Integer.parseInt(pref.getString("pref_key_sysui_headerclick", "1")) == 3)
				SysUIMods.execHook_NotifDrawerHeaderSysInfo(lpparam);
			
			if (pref_homeassist != 1)
				SysUIMods.execHook_OverrideAssist(lpparam);
			
			SysUIMods.execHook_RecentsLongTap(lpparam);
			CleanBeamMods.execHook_HideIcons(lpparam);
		}
		
		if (pkg.equals("com.android.packageinstaller"))
		{
			OtherMods.execHook_EnhancedInstaller(lpparam);
		}
		
		if(pkg.equals("com.htc.android.mail"))
		{
			if (pref.getBoolean("pref_key_messaging_eassecurity", false))
		    	MessagingMods.execHook_EASSecurityPartTwo(lpparam);
		}
		
		if (pkg.equals("com.android.phone")) {
			int largePhoto = Integer.parseInt(pref.getString("pref_key_other_largephoto", "1"));
			if (largePhoto > 1)
				OtherMods.execHook_LargePhotoCode(lpparam, largePhoto);
			
			if (pref.getBoolean("pref_key_other_rejectedcall", false))
				OtherMods.execHook_RejectCallSilently(lpparam);
		}
		
		if (lpparam.processName.equals("android"))
		{
			if (pref.getBoolean("pref_key_other_apm", false))
				OtherMods.execHook_APM(lpparam);
			
			if (pref.getBoolean("pref_key_other_volsound", false))
				OtherMods.execHook_VolSound(lpparam);
			
			boolean vol2wakeEnabled = pref.getBoolean("pref_key_controls_vol2wake", false);
			if (vol2wakeEnabled)
				ControlsMods.execHook_Vol2Wake(lpparam);
			
			if (pref.getBoolean("pref_key_controls_powerflash", false))
				ControlsMods.execHook_PowerFlash(lpparam);
			
			pref_mediaDown = Integer.parseInt(pref.getString("pref_key_controls_mediadownaction", "0"));
		    pref_mediaUp = Integer.parseInt(pref.getString("pref_key_controls_mediaupaction", "0"));
		    if (pref_mediaDown != 0 || pref_mediaUp != 0)
		    	ControlsMods.execHook_VolumeMediaButtons(lpparam, vol2wakeEnabled);
		    
		    if (pref.getBoolean("pref_key_messaging_eassecurity", false))
		    	MessagingMods.execHook_EASSecurityPartOne(lpparam);
		    
		    if (pref.getBoolean("pref_key_other_volsafe", false))
		    	OtherMods.execHook_SafeVolume(lpparam);
		    
		    if (pref.getBoolean("pref_key_controls_swapvolume", false))
		    	ControlsMods.exec_SwapVolumeCCWLand(lpparam);
		    
		    if (pref.getBoolean("pref_key_sysui_invisibar_enable", false) && Build.VERSION.SDK_INT >= 19)
		    	SysUIMods.execHook_anotherTSB44Fix(lpparam);
		}
	}
}

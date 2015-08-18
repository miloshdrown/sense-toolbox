package com.sensetoolbox.six.mods;

import android.content.res.XResources;

import com.sensetoolbox.six.utils.GlobalActions;
import com.sensetoolbox.six.utils.Helpers;
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
	private static int pref_shake = 1;
	private static int pref_appslongpress = 1;
	public static int pref_homeassist = 1;
	public static int pref_screenon = 0;
	public static int pref_screenoff = 0;
	public static boolean pref_alarmnotify = false;
	public static boolean pref_signalnotify = false;
	public static boolean prefs_pwm = false;
	public static boolean prefs_icons_lp = false;
	public static Version senseVersion;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		MODULE_PATH = startupParam.modulePath;
		pref = new XSharedPreferences("com.sensetoolbox.six", "one_toolbox_prefs");
		
		GlobalActions.toolboxStuff();
		
		pref_swipedown = Integer.parseInt(pref.getString("pref_key_prism_swipedownaction", "1"));
		pref_swipeup = Integer.parseInt(pref.getString("pref_key_prism_swipeupaction", "1"));
		pref_swiperight = Integer.parseInt(pref.getString("pref_key_prism_swiperightaction", "1"));
		pref_swipeleft = Integer.parseInt(pref.getString("pref_key_prism_swipeleftaction", "1"));
		pref_backlongpress = Integer.parseInt(pref.getString("pref_key_controls_backlongpressaction", "1"));
		pref_homeassist = Integer.parseInt(pref.getString("pref_key_controls_homeassistaction", "1"));
		pref_shake = Integer.parseInt(pref.getString("pref_key_prism_shakeaction", "1"));
		pref_appslongpress = Integer.parseInt(pref.getString("pref_key_prism_appslongpressaction", "1"));
		pref_screenon = Integer.parseInt(pref.getString("pref_key_other_screenon", "0"));
		pref_screenoff = Integer.parseInt(pref.getString("pref_key_other_screenoff", "0"));
		prefs_pwm = pref.getBoolean("pref_key_controls_extendedpanel", false) ||
					pref.getBoolean("popup_notify_active", false) ||
					pref.getBoolean("better_headsup_active", false) ||
					pref.getBoolean("pref_key_other_apm", false) ||
					pref.getBoolean("pref_key_prism_homemenu", false) ||
					pref_swipedown != 1 || pref_swipeup != 1 || pref_swiperight != 1 || pref_swipeleft != 1 ||
					pref_backlongpress != 1 || pref_homeassist != 1 || pref_shake != 1 || pref_appslongpress != 1;
		
		if (prefs_pwm)
			GlobalActions.setupPWM();
		
		if ((pref_backlongpress != 1 || pref_homeassist != 1) && !Helpers.isEight())
			ControlsMods.setupPWMKeys();
		
		if ((pref_screenon != 0 || pref_screenoff != 0)) {
			if (Helpers.isLP())
				OtherMods.execHook_ScreenColorFadeFix();
			else
				OtherMods.execHook_ScreenAnim();
		}
		
		if (pref.getBoolean("pref_key_cb_beats", false))
			StatusbarMods.execHook_BeatsIcon();
		
		if (!Helpers.isLP() && pref.getBoolean("pref_key_other_movevol", false))
			OtherMods.execHook_MoveVolume();
		
		if (pref.getBoolean("pref_key_other_volsafe", false))
			XResources.setSystemWideReplacement("android", "bool", "config_safe_media_volume_enabled", false);
		
		if (pref.getBoolean("pref_key_other_oldtoasts", false))
			OtherMods.exec_OldStyleToasts();
		
		if (pref.getBoolean("pref_key_other_securelock", false))
			OtherMods.execHook_EnhancedSecurity();
		
		if (pref.getBoolean("pref_key_other_keyslight_auto", false))
			OtherMods.buttonBacklightSystem();
		
		if (pref.getBoolean("themes_active", false))
			SysUIMods.execHook_Sense6ColorControl();
		
		if (pref.getBoolean("pref_key_controls_smallsoftkeys", false))
			ControlsMods.execHook_SmallNavbar();
		
		if (pref.getBoolean("wake_gestures_active", false) && Helpers.isWakeGesturesAvailable() && !Helpers.isEight())
			WakeGesturesMods.execHook_InitListener();
		
		if (pref.getBoolean("touch_lock_active", false) && Helpers.isTouchscreenEventsAvailable())
			WakeGesturesMods.execHook_InitTouchLockListener();
		
		if (pref.getBoolean("pref_key_controls_extendedpanel", false))
			SysUIMods.execHook_SearchGlowPad();
		
		if (pref.getBoolean("pref_key_other_allrotations", false))
			OtherMods.execHook_AllRotations();
		
		if (pref.getBoolean("popup_notify_active", false))
			OtherMods.execHook_PopupNotify();
		
		if (pref.getBoolean("better_headsup_active", false))
			SysUIMods.execHook_BetterHeadsUpSystem();
		
		if (pref.getBoolean("pref_key_sysui_tnsb", false))
			SysUIMods.execHook_TranslucentNotificationsDividers();
		
		if (pref.getBoolean("pref_key_other_contactsnocorner", false))
			OtherMods.execHook_ContactsNoCornerSystem();
		
		if (Integer.parseInt(pref.getString("pref_key_other_iconlabletoasts", "1")) != 1)
			SysUIMods.execHook_IconLabelToasts();
		
		if (pref.getBoolean("pref_key_other_vzwnotif", false))
			OtherMods.execHook_VZWWiFiNotif();
		
		if (pref.getBoolean("pref_key_controls_keyshaptic_enable", false) ||
			pref.getBoolean("pref_key_controls_longpresshaptic_enable", false) ||
			pref.getBoolean("pref_key_controls_keyboardhaptic_enable", false))
			ControlsMods.execHook_KeysHapticFeedback();
		
		if (pref.getBoolean("pref_key_other_apm", false))
			OtherMods.execHook_APM();
		
		if (!Helpers.isLP() && pref.getBoolean("pref_key_other_volsound", false))
			OtherMods.execHook_VolSound();
		
		boolean vol2wakeEnabled = pref.getBoolean("pref_key_controls_vol2wake", false);
		if (vol2wakeEnabled)
			ControlsMods.execHook_Vol2Wake();
		
		if (pref.getBoolean("pref_key_controls_powerflash", false))
			ControlsMods.execHook_PowerFlash();
		
		if (Integer.parseInt(pref.getString("pref_key_controls_mediadownaction", "0")) != 0 || Integer.parseInt(pref.getString("pref_key_controls_mediaupaction", "0")) != 0)
			ControlsMods.execHook_VolumeMediaButtons(vol2wakeEnabled);
		
		if (pref.getBoolean("pref_key_other_volsafe", false))
			OtherMods.execHook_SafeVolume();
		
		if (pref.getBoolean("pref_key_controls_swapvolume", false))
			ControlsMods.exec_SwapVolumeCCWLand();
		
		if (pref.getBoolean("fleeting_glance_active", false))
			WakeGesturesMods.execHook_FleetingGlance();
		
		if (Integer.parseInt(pref.getString("pref_key_prism_transitions", "1")) == 2)
			PrismMods.execHook_StockTransitions();
		
		if (pref.getBoolean("pref_key_other_noautoime", false))
			OtherMods.execHook_NoAutoIME();
		
		if (Integer.parseInt(pref.getString("pref_key_controls_wiredheadsetonaction", "1")) != 1 ||
			Integer.parseInt(pref.getString("pref_key_controls_wiredheadsetoffaction", "1")) != 1 ||
			Integer.parseInt(pref.getString("pref_key_controls_wiredheadsetoneffect", "1")) != 1 ||
			Integer.parseInt(pref.getString("pref_key_controls_wiredheadsetoffeffect", "1")) != 1 ||
			Integer.parseInt(pref.getString("pref_key_controls_btheadsetonaction", "1")) != 1 ||
			Integer.parseInt(pref.getString("pref_key_controls_btheadsetoffaction", "1")) != 1 ||
			Integer.parseInt(pref.getString("pref_key_controls_btheadsetoneffect", "1")) != 1 ||
			Integer.parseInt(pref.getString("pref_key_controls_btheadsetoffeffect", "1")) != 1)
			ControlsMods.execHook_AccessoriesActions();

		if (pref.getBoolean("pref_key_other_beatsnotif", false))
			OtherMods.execHook_GlobalEffectNotification();
		
		if (pref.getBoolean("pref_key_prism_homemenu", false) && Helpers.isEight())
			ControlsMods.execHook_HomeLongpressAssistEight();
		
		//OtherMods.execHook_HapticNotify();
	}
	
	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		String pkg = resparam.packageName;
		
		if (pkg.equals("com.htc.launcher")) {
			pref.reload();
			if (pref.getBoolean("pref_key_prism_invisidrawer_enable", false))
				PrismMods.execHook_InvisiDrawerRes(resparam);
			
			if (pref.getBoolean("pref_key_prism_invisifolders_enable", false)) {
				int transparency = pref.getInt("pref_key_prism_invisifolders", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				PrismMods.execHook_InvisiFolder(resparam, transparency);
			}
			
			if (pref.getBoolean("pref_key_prism_invisifoldersbkg_enable", false)) {
				int transparency = pref.getInt("pref_key_prism_invisifoldersbkg", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				PrismMods.execHook_InvisiFolderBkg(resparam, transparency);
			}
			
			if (pref.getBoolean("pref_key_prism_invisihotseat", false))
				PrismMods.execHook_HotSeatNoBkg(resparam);
			
			if (pref.getBoolean("pref_key_prism_4x5homescreen", false))
				PrismMods.execHook_HomeScreenGridSize(resparam);
			
			if (pref.getBoolean("pref_key_prism_gapfix", false) && !pref.getBoolean("pref_key_prism_4x5homescreen", false))
				PrismMods.execHook_HomeScreenGapFix(resparam);
			
			if (Integer.parseInt(pref.getString("pref_key_prism_transitions", "1")) == 3)
				PrismMods.execHook_StockTransitionsAnim(resparam);
			
			if (pref.getBoolean("pref_key_persist_appdrawer_grid", false))
				PrismMods.execHook_AppDrawerGridSizesLayout(resparam);
		}
		
		if (pkg.equals("com.android.systemui")) {
			if (!Helpers.isLP() && pref.getBoolean("pref_key_sysui_invisinotify_enable", false)) {
				int transparency = pref.getInt("pref_key_sysui_invisinotify", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				SysUIMods.execHook_InvisiNotify(resparam, transparency);
			}
			
			if (Integer.parseInt(pref.getString("pref_key_sysui_battery", "1")) != 1)
				StatusbarMods.execHook_BatteryIcon(resparam, Integer.parseInt(pref.getString("pref_key_sysui_battery", "1")));
			
			if (pref.getBoolean("pref_key_cb_signal", false))
				StatusbarMods.execHook_SignalIcon(resparam);
			
			if (pref.getBoolean("pref_key_cb_headphone", false))
				StatusbarMods.execHook_HeadphoneIcon(resparam);
			
			if (pref.getBoolean("pref_key_cb_alarm", false))
				StatusbarMods.execHook_AlarmIcon(resparam);
			
			if (Helpers.isLP()) {
				if (pref.getBoolean("pref_key_cb_wifi", false))
					StatusbarMods.execHook_WiFiIcon(resparam, 3);
			} else {
				if (Integer.parseInt(pref.getString("pref_key_cb_wifi_multi", "1")) != 1)
					StatusbarMods.execHook_WiFiIcon(resparam, Integer.parseInt(pref.getString("pref_key_cb_wifi_multi", "1")));
			}
			
			if (pref.getBoolean("pref_key_cb_profile", false))
				StatusbarMods.execHook_ProfileIcon(resparam);
			
			if (pref.getBoolean("pref_key_cb_sync", false))
				StatusbarMods.execHook_SyncIcon(resparam);
			
			if (pref.getBoolean("pref_key_cb_gps", false))
				StatusbarMods.execHook_GpsIcon(resparam);
			
			if (pref.getBoolean("pref_key_cb_bt", false))
				StatusbarMods.execHook_BtIcon(resparam);
			
			if (pref.getBoolean("pref_key_cb_data", false))
				StatusbarMods.execHook_DataIcon(resparam);
			
			if (pref.getBoolean("pref_key_cb_screenshot", false))
				StatusbarMods.execHook_ScreenshotIcon(resparam);
			
			if (Integer.parseInt(pref.getString("pref_key_sysui_clockstyle", "1")) == 2)
				SysUIMods.execHook_CenterClockLayout(resparam);
			
			int headerClock = Integer.parseInt(pref.getString("pref_key_sysui_headerclick", "1"));
			if (headerClock >= 2)
				SysUIMods.execHook_NotifDrawerHeaderClock(resparam, headerClock);
			
			if (pref.getBoolean("pref_key_sysui_tsb", false))
				SysUIMods.execHook_NoStatusBarBackground(resparam);
			
			if (pref.getBoolean("pref_key_sysui_teqs", false))
				SysUIMods.execHook_TranslucentEQS(resparam);
			
			if (pref.getBoolean("pref_key_sysui_theqs", false))
				SysUIMods.execHook_TranslucentHorizEQS(resparam);
			
			if (pref.getBoolean("pref_key_other_musicchannel", false))
				OtherMods.execHook_MusicChannelEQSTileIcon(resparam);
			
			if (pref.getBoolean("pref_key_sysui_compacteqs", false))
				SysUIMods.execHook_EQSGrid(resparam);
		}
		
		if (pkg.equals("com.android.settings")) {
			if (pref.getBoolean("pref_key_cb_usb", false))
				StatusbarMods.execHook_USBIcon(resparam);
			
			if (pref.getBoolean("pref_key_cb_dnd", false))
				StatusbarMods.execHook_DNDIcon(resparam);
		}
		
		if (pkg.equals("com.htc.htcpowermanager")) {
			if (pref.getBoolean("pref_key_cb_powersave", false))
				StatusbarMods.execHook_PowerSaveIcon(resparam);
		}
		
		if (pkg.equals("com.android.nfc")) {
			if (pref.getBoolean("pref_key_cb_nfc", false))
				StatusbarMods.execHook_NFCIcon(resparam);
		}
		
		if (pkg.equals("com.android.providers.media")) {
			if (pref.getBoolean("pref_key_cb_mtp", false))
				StatusbarMods.execHook_MTPIcon(resparam);
		}
		
		if (pkg.equals("com.android.phone")) {
			if (pref.getBoolean("pref_key_cb_phone", false))
				StatusbarMods.execHook_PhoneIcons(resparam);
			
			int largePhoto = Integer.parseInt(pref.getString("pref_key_other_largephoto", "1"));
			if (largePhoto > 1)
				OtherMods.execHook_LargePhoto(resparam, largePhoto);
		}
		
		if (pkg.equals("com.htc.videohub.ui")) {
			if (pref.getBoolean("pref_key_cb_tv", false))
				StatusbarMods.execHook_TvIcon(resparam);
			
			if (pref.getBoolean("pref_key_sysui_tnsb", false))
				SysUIMods.execHook_TranslucentNotificationsTV(resparam);
		}
		
		if (pkg.equals("com.google.android.youtube")) {
			if (pref.getBoolean("pref_key_other_ytwatermark", false))
				OtherMods.execHook_YouTubeNoWatermark(resparam);
		}
		
		if (pkg.equals("com.htc.contacts") || pkg.equals("com.htc.sense.mms")) {
			if (pref.getBoolean("pref_key_other_contactsnocorner", false))
				OtherMods.execHook_ContactsNoCorner(resparam);
		}
		
		if (pkg.equals("com.google.android.googlequicksearchbox")) {
			pref.reload();
			int option = Integer.parseInt(pref.getString("pref_key_prism_gappwidget", "1"));
			if (option > 1)
				PrismMods.execHook_googleSearchWidget(resparam, option);
		}
		
		if (pkg.equals("com.htc.MusicWidget")) {
			if (pref.getBoolean("pref_key_prism_invisimusicwidget", false))
				PrismMods.execHook_InvisiMusicWidget(resparam);
		}
		
		if (pkg.equals("com.htc.htccontactwidgets")) {
			if (pref.getBoolean("pref_key_prism_invisipeoplewidget", false))
				PrismMods.execHook_InvisiPeopleWidget(resparam);
		}
	}
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		String pkg = lpparam.packageName;
		
		if (pkg.equals("com.sensetoolbox.six")) {
			GlobalActions.toolboxInit(lpparam);
		}
		
		if (pkg.equals("android") && lpparam.processName.equals("android")) {
			PackagePermissions.init(lpparam);
			
			if (prefs_pwm)
				GlobalActions.setupDMS(lpparam);
			
			if ((pref_screenon != 0 || pref_screenoff != 0) && Helpers.isLP())
				OtherMods.execHook_ScreenColorFade(lpparam);
			
			if (pref.getBoolean("pref_key_other_keyslight_auto", false))
				OtherMods.buttonBacklightService(lpparam);
			
			if (pref.getBoolean("pref_key_other_imenotif", false))
				OtherMods.execHook_InputMethodNotif(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_hqthumbs", false))
				SysUIMods.execHook_HDThumbnails(lpparam);
			
			if (pref.getBoolean("pref_key_other_ledtimeout", false))
				OtherMods.execHook_LEDNotifyTimeout(lpparam);
			
			if (pref.getBoolean("pref_key_other_ledoncharge", false))
				OtherMods.execHook_LEDOnCharge(lpparam);
			
			if (pref.getBoolean("touch_lock_active", false) && Helpers.isTouchscreenEventsAvailable())
				WakeGesturesMods.execHook_InitTouchServerListener(lpparam);
			
			if (pref.getBoolean("better_headsup_active", false))
				SysUIMods.execHook_BetterHeadsUpNotifications(lpparam);
			
			if (pref.getBoolean("fleeting_glance_active", false))
				WakeGesturesMods.execHook_FleetingGlanceService(lpparam);
			
			if (pref.getBoolean("pref_key_other_noautoime", false))
				OtherMods.execHook_NoAutoIMEService(lpparam);
			
			if (pref.getBoolean("pref_key_other_bindhtcwidgets", false))
				OtherMods.execHook_CanBindHtcAppWigdet(lpparam);
		}
		
		if (pkg.equals("com.android.providers.media")) {
			if (pref.getBoolean("pref_key_other_mtpnotif", false))
				OtherMods.execHook_MTPNotif(lpparam);
		}
		
		if (pkg.equals("com.htc.htcpowermanager")) {
			if (pref.getBoolean("pref_key_other_powersavenotif", false) && !Helpers.isLP2())
				OtherMods.execHook_PowerSaverNotif(lpparam);
		}
		
		if (pkg.equals("com.htc.powersavinglauncher")) {
			if (pref.getBoolean("eps_remap_active", false))
				OtherMods.execHook_ExtremePowerSaverRemap(lpparam);
		}
		
		if (pkg.equals("com.htc.sense.mms")) {
			pref.reload();
			if (pref.getBoolean("pref_key_other_smscreenon", false))
				MessagingMods.execHook_smsscreenon(lpparam);
			
			if (pref.getBoolean("pref_key_sms_smsmmsconv", false))
				MessagingMods.execHook_SmsMmsConv(lpparam);
			
			if (pref.getBoolean("pref_key_sms_toastnotification", false))
				MessagingMods.execHook_ToastNotification(lpparam);
			
			if (pref.getBoolean("pref_key_sms_mmssize", false))
				MessagingMods.execHook_MmsSize(lpparam);
			
			if (pref.getBoolean("pref_key_sms_accents", false))
				MessagingMods.execHook_SmsAccents(lpparam);
		}
		
		if (pkg.equals("com.htc.launcher")) {
			pref.reload();
			if (pref.getBoolean("pref_key_prism_invisiwidget_enable", false)) {
				int transparency = pref.getInt("pref_key_prism_invisiwidget", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				PrismMods.execHook_InvisiWidget(lpparam, transparency);
			}
			
			if (pref.getBoolean("pref_key_prism_folder20", false))
				PrismMods.execHook_20Folder_code(lpparam);
			
			if (pref.getBoolean("pref_key_prism_invisidrawer_enable", false)) {
				int transparency = pref.getInt("pref_key_prism_invisidrawer", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				PrismMods.execHook_InvisiDrawerCode(lpparam, transparency);
			}
			
			if (pref_swipedown != 1 || pref_swipeup != 1)
				PrismMods.execHook_SwipeActions(lpparam);
			
			if (pref.getBoolean("pref_key_persist_appdrawer_grid", false))
				PrismMods.execHook_AppDrawerGridSizes(lpparam);
			
			if (pref.getBoolean("pref_key_prism_gridtinyfont", false))
				PrismMods.execHook_AppDrawerGridTinyText(lpparam);
			
			if (pref_swiperight != 1 || pref_swipeleft != 1 || pref.getBoolean("pref_key_prism_homemenu", false))
				PrismMods.execHook_DockSwipe(lpparam);
			
			if (pref.getBoolean("pref_key_prism_homemenu", false)) {
				PrismMods.execHook_HomeMenu(lpparam);
				PrismMods.execHook_LauncherLock(lpparam);
			}
			
			if (pref.getBoolean("pref_key_prism_sevenscreens", false))
				PrismMods.execHook_SevenScreens(lpparam);
			
			if (pref.getBoolean("pref_key_prism_4x5homescreen", false))
				PrismMods.execHook_HomeScreenResizableWidgets(lpparam);
			
			if (pref.getBoolean("pref_key_prism_invisilabels", false))
				PrismMods.execHook_invisiLabels(lpparam);
			
			if (pref.getBoolean("pref_key_prism_blinkfeednodock", false))
				PrismMods.execHook_BlinkFeedNoDock(lpparam);
			
			if (pref.getBoolean("pref_key_prism_blinkfeedimmersive", false))
				PrismMods.execHook_BlinkFeedImmersive(lpparam);
			
			if (pref_shake != 1)
				PrismMods.execHook_ShakeAction(lpparam);
			
			if (pref.getBoolean("pref_key_prism_invisiactionbar", false))
				PrismMods.execHook_ActionBarNoBkg(lpparam);
			
			if (pref_appslongpress != 1)
				PrismMods.execHook_hotseatToggleBtn(lpparam);
			
			if (Integer.parseInt(pref.getString("pref_key_prism_transitions", "1")) == 2)
				PrismMods.execHook_StockTransitionsLauncher(lpparam);
		}
		
		if (pkg.equals("com.htc.lockscreen")) {
			if (pref_homeassist != 1 && !Helpers.isLP() && !Helpers.isEight())
				ControlsMods.execHook_dieGoogleNow(lpparam);
			
			if (pref.getBoolean("pref_key_other_fastunlock", false))
				OtherMods.execHook_fastUnlock(lpparam);
			
			if (pref.getBoolean("wake_gestures_active", false))
				WakeGesturesMods.execHook_LockScreenGestures(lpparam);
			
			if (pref.getBoolean("pref_key_other_scramblepin", false))
				OtherMods.execHook_scramblePIN(lpparam);
		}
		
		if (pkg.equals("com.android.settings")) {
			if (pref.getBoolean("pref_key_other_keepscreenon", false))
				SettingsMods.execHook_ScreenOn(lpparam);
			
			if (pref.getBoolean("pref_key_other_appdetails", false))
				if (Helpers.isNewSense())
					SettingsMods.execHook_AppsM(lpparam);
				else
					SettingsMods.execHook_Apps(lpparam);
			
			if (pref.getBoolean("pref_key_other_nochargerwarn", false))
				OtherMods.execHook_NoChargerWarning(lpparam);
			
			if (pref.getBoolean("pref_key_other_musicchannel", false))
				OtherMods.execHook_MusicChannel(lpparam, false);
			
			if (pref.getBoolean("pref_key_other_dndnotif", false))
				OtherMods.execHook_DNDNotif(lpparam);
			
			if (pref.getBoolean("pref_key_other_nofliptomute", false))
				OtherMods.execHook_NoFlipToMuteSetting(lpparam);
			
			if (pref.getBoolean("pref_key_persist_unhideprefs", false))
				SettingsMods.execHook_UnhidePrefs(lpparam);
			
			if (!Helpers.isLP() && pref.getBoolean("pref_key_persist_appfilter", false))
				SettingsMods.execHook_AppFilter(lpparam);
		}
		
		if (pkg.equals("com.htc.musicenhancer")) {
			if (pref.getBoolean("pref_key_other_musicchannel", false))
				OtherMods.execHook_MusicChannel(lpparam, true);
		}
		
		if (pkg.equals("com.android.camera")) {
			int voldown = Integer.parseInt(pref.getString("pref_key_controls_camdownaction", "4"));
			int volup = Integer.parseInt(pref.getString("pref_key_controls_camupaction", "4"));
			if (!(voldown == 4 && volup == 4))
				CamMods.execHook_VolKey(lpparam, volup, voldown);
		}
		
		if (pkg.equals("com.android.systemui")) {
			if (prefs_pwm)
				GlobalActions.setupPSB(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_invisinotify_enable", false)) {
				int transparency = pref.getInt("pref_key_sysui_invisinotify", 100);
				transparency = (int) Math.floor(transparency*2.55f);
				SysUIMods.execHook_InvisiNotifyCode(lpparam, transparency);
			}
			
			if (pref.getBoolean("pref_key_sysui_noeqs", false))
				SysUIMods.execHook_DisableEQS(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_minorqs", false)) {
				SysUIMods.execHook_MinorEQS(lpparam, pref.getBoolean("pref_key_sysui_minorqs_notext", false));
				SysUIMods.execHook_hEQSLongClick(lpparam);
			}
			
			if (pref.getBoolean("pref_key_sysui_aosprecent", false))
				SysUIMods.execHook_AospRecent(lpparam);
			
			if (Integer.parseInt(pref.getString("pref_key_sysui_clockstyle", "1")) == 2)
				SysUIMods.execHook_CenterClockAnimation(lpparam);
			
			if (Integer.parseInt(pref.getString("pref_key_sysui_clockstyle", "1")) == 3)
				SysUIMods.execHook_ClockRemove(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_ampmremove", false))
				SysUIMods.execHook_removeAMPM(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_brightslide", false))
				SysUIMods.execHook_BrightnessSlider(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_dataratestatus", false))
				SysUIMods.execHook_DataRateStatus(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_recentram", false))
				SysUIMods.execHook_RAMInRecents(lpparam);
			
			pref_alarmnotify = pref.getBoolean("pref_key_sysui_alarmnotify", false);
			if (pref_alarmnotify && !Helpers.isDualSIM())
				SysUIMods.execHook_AlarmNotification(lpparam);
			
			pref_signalnotify = pref.getBoolean("pref_key_sysui_signalnotify", false);
			if (pref_signalnotify && !Helpers.isDualSIM())
				SysUIMods.execHook_SignalNotification(lpparam);
			
			if ((pref_alarmnotify || pref_signalnotify)  && !Helpers.isDualSIM())
				SysUIMods.execHook_LabelsUpdate(lpparam);
			
			if (Integer.parseInt(pref.getString("pref_key_sysui_headerclick", "1")) == 3)
				SysUIMods.execHook_NotifDrawerHeaderSysInfo(lpparam);
			
			if (pref_homeassist != 1) {
				if (Helpers.isEight())
					ControlsMods.execHook_RecentsLongpressEight(lpparam);
				else
					SysUIMods.execHook_OverrideAssist(lpparam);
			}
			
			if (pref.getBoolean("pref_key_sysui_brightqs", false))
				SysUIMods.execHook_ChangeBrightnessQSTile(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_timeoutqs", false))
				SysUIMods.execHook_ChangeTimeoutQSTile(lpparam);
			
			if (pref_backlongpress != 1 && Helpers.isEight())
				ControlsMods.execHook_BackLongpressEight(lpparam);
			
			if (pref.getBoolean("pref_key_prism_homemenu", false) && Helpers.isEight())
				ControlsMods.execHook_HomeLongpressEight(lpparam);
			
			if (!Helpers.isLP() && pref.getBoolean("pref_key_cb_texts", false))
				StatusbarMods.execHook_StatusBarTexts(lpparam);
			
			if (pref.getBoolean("pref_key_controls_extendedpanel", false))
				SysUIMods.execHook_SearchGlowPadLaunch(lpparam);
			
			if (pref.getBoolean("pref_key_other_nolowbattwarn", false))
				SysUIMods.execHook_NoLowBatteryWarning(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_tnsb", false))
				SysUIMods.execHook_TranslucentNotifications(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_theqs", false))
				SysUIMods.execHook_TranslucentHorizEQSCode(lpparam);
			
			int pref_footer = Integer.parseInt(pref.getString("pref_key_sysui_footeralpha", "1"));
			if (pref_footer != 1)
				SysUIMods.execHook_DrawerFooterDynamicAlpha(lpparam, pref_footer);
			
			if (pref.getBoolean("pref_key_other_screendelete", false))
				SysUIMods.execHook_ScreenshotDelete(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_recentappsclear", false) || pref.getBoolean("pref_key_sysui_recentslongtap", false))
				SysUIMods.execHook_RecentAppsInit(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_recentappsclear", false))
				SysUIMods.execHook_RecentAppsClearTouch(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_recentslongtap", false))
				SysUIMods.execHook_RecentsLongTap(lpparam);
			
			if (pref.getBoolean("pref_key_other_musicchannel", false))
				OtherMods.execHook_MusicChannelEQSTile(lpparam);
			
			if (pref.getBoolean("pref_key_statusbar_selectivealarmicon_enable", false))
				StatusbarMods.execHook_SmartAlarm(lpparam);
			
			if (pref.getBoolean("pref_key_other_screenopen", false))
				OtherMods.execHook_ScreenshotViewer(lpparam);
			
			if (Helpers.isLP() && pref.getBoolean("pref_key_other_movevol", false))
				OtherMods.execHook_MoveVolume(lpparam);
			
			if (Helpers.isLP() && pref.getBoolean("pref_key_other_volsound", false))
				OtherMods.execHook_VolSound(lpparam);
			
			if (pref.getBoolean("better_headsup_active", false))
				SysUIMods.execHook_BetterHeadsUpSysUI(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_compacteqs", false))
				SysUIMods.execHook_EQSTiles(lpparam);
			
			if (Helpers.isLP() && pref.getBoolean("pref_key_other_secureeqs", false))
				OtherMods.execHook_SecureEQS(lpparam);
			
			int pref_autoeqs = Integer.parseInt(pref.getString("pref_key_sysui_autoeqs", "1"));
			if (pref_autoeqs > 1)
				SysUIMods.execHook_AutoEQS(lpparam, pref_autoeqs == 3);
			
			if (pref.getBoolean("fleeting_glance_active", false))
				WakeGesturesMods.execHook_FleetingGlanceSysUI(lpparam);
			
			if (pref.getBoolean("pref_key_other_powersavenotif", false) && Helpers.isLP())
				OtherMods.execHook_PowerSaverNotifSysUI(lpparam);
			
			if (pref.getBoolean("pref_key_sysui_restoretiles", false))
				SysUIMods.execHook_RestoreEQSTiles(lpparam);
			
			StatusbarMods.execHook_HideIcons(lpparam);
		}
		
		if (pkg.equals("com.android.packageinstaller")) {
			if (pref.getBoolean("pref_key_persist_installer", false))
				OtherMods.execHook_EnhancedInstaller(lpparam);
		}
		
		if (pkg.equals("com.android.phone")) {
			int largePhoto = Integer.parseInt(pref.getString("pref_key_other_largephoto", "1"));
			if (largePhoto > 1)
				OtherMods.execHook_LargePhotoCode(lpparam, largePhoto);
			
			if (pref.getBoolean("pref_key_other_rejectedcall", false))
				OtherMods.execHook_RejectCallSilently(lpparam);
			
			if (pref.getBoolean("pref_key_other_nofliptomute", false))
				OtherMods.execHook_NoFlipToMute(lpparam);
			
			if (pref.getBoolean("pref_key_other_nameorder", false))
				OtherMods.execHook_ContactsNameOrderPhone(lpparam);
			
			if (pref.getBoolean("pref_key_other_callvibrateon", false))
				OtherMods.execHook_VibrateOnCallConnected(lpparam);
			
			if (pref.getBoolean("pref_key_other_callvibrateoff", false))
				OtherMods.execHook_VibrateOnCallDisconnected(lpparam);
			
			if (pref.getBoolean("pref_key_other_callvibratedur_enable", false))
				OtherMods.execHook_VibrateOnCallDuration(lpparam);
			
			if (pref.getBoolean("pref_key_other_nobacktomute", false))
				OtherMods.execHook_NoBackToMute(lpparam);
			
			//OtherMods.execHook_USSD(lpparam);
		}
		
		if (pkg.equals("com.htc.widget.weatherclock")) {
			if (pref.getBoolean("pref_key_prism_invisiwidget_enable", false)) {
				PrismMods.execHook_invisiWidgetFix(lpparam);
			}
		}
		
		if (pkg.equals("com.htc.htcdialer") || pkg.equals("com.htc.contacts")) {
			if (pref.getBoolean("pref_key_controls_smallsoftkeys", false))
				ControlsMods.execHook_FixDialer(lpparam);

			if (pref.getBoolean("pref_key_other_nameorder", false))
				OtherMods.execHook_ContactsNameOrder(lpparam);
		}
		
		if (pref.getBoolean("themes_active", false))
		if (pkg.startsWith("com.htc.") || pkg.equals("com.android.systemui"))
			SysUIMods.execHook_Sense6ColorControlCustom(lpparam, pkg);
		
		if (pkg.equals("com.htc.sense.ime")) {
			if (pref.getBoolean("pref_key_controls_keyboardhaptic_enable", false))
				ControlsMods.execHook_KeyboardHapticFeedback(lpparam);
			
			if (pref.getBoolean("pref_key_other_noautocorrect", false))
				OtherMods.execHook_KeyboardNoAutocorrect(lpparam);
			
			if (pref.getBoolean("pref_key_other_tracecolor", false))
				OtherMods.execHook_KeyboardTraceColor(lpparam);
			
			if (pref.getBoolean("pref_key_other_tracealpha_enable", false))
				OtherMods.execHook_KeyboardTraceAlpha(lpparam);
		}
		
		if (pkg.equals("com.htc.sense.easyaccessservice")) {
			if (pref.getBoolean("wake_gestures_active", false) && Helpers.isEight())
				WakeGesturesMods.execHook_EasyAccessService(lpparam);
		}
		
		if (pkg.equals("com.android.vending")) {
			if (pref.getBoolean("pref_key_other_psscrolltotop", false))
				OtherMods.execHook_PSScroll(lpparam);
		}
	}
}

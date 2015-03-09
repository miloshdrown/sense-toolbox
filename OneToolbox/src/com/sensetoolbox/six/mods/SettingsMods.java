package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import java.io.File;
import java.util.ArrayList;

import com.htc.preference.HtcPreference;
import com.htc.preference.HtcPreferenceFragment;
import com.htc.widget.HtcListItem;
import com.htc.widget.HtcListItem2LineStamp;
import com.htc.widget.HtcListItem2LineText;
import com.htc.widget.HtcListItemSeparator;
import com.htc.widget.HtcRimButton;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.XModuleResources;
import android.os.Bundle;
import android.os.Message;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SettingsMods {

	public static void execHook_ScreenOn(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.settings.framework.flag.feature.HtcDisplayFeatureFlags", lpparam.classLoader, "supportStayAwake", Context.class, new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
	}
	
	public static void execHook_UnhidePrefs(final LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.android.settings.framework.flag.feature.HtcAboutPhoneFeatureFlags", lpparam.classLoader, "supportROMVersion", new XC_MethodHook(){
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(true);
				}
			});
		} catch (Throwable t1) {
			try {
				findAndHookMethod("com.android.settings.framework.flag.feature.HtcAboutPhoneFeatureFlags", lpparam.classLoader, "supportRomVersion", new XC_MethodHook(){
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						param.setResult(true);
					}
				});
			} catch (Throwable t2) {
				XposedBridge.log(t2);
			}
		}
		
		try {
			findAndHookMethod("com.android.settings.framework.flag.feature.HtcAboutPhoneFeatureFlags", lpparam.classLoader, "supportDistributionTime", new XC_MethodHook(){
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(true);
				}
			});
		} catch (Throwable t) {}
		
		try {
			findAndHookMethod("com.android.settings.framework.flag.feature.HtcAboutPhoneFeatureFlags", lpparam.classLoader, "supportHardwareInformation", new XC_MethodHook(){
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(true);
				}
			});
		} catch (Throwable t1) {
			try {
				findAndHookMethod("com.android.settings.framework.activity.aboutphone.HtcAboutPhoneSettings", lpparam.classLoader, "doPlugin", Context.class, new XC_MethodHook(){
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Class<?> clsHBP = XposedHelpers.findClass("com.android.settings.framework.content.custom.property.HtcBooleanProperty", lpparam.classLoader);
						Object supportHardwareVersion = clsHBP.getConstructor(String.class, Boolean.class).newInstance("support_hardware_version", Boolean.valueOf(true));
						XposedHelpers.setStaticObjectField(findClass("com.android.settings.framework.flag.feature.HtcAboutPhoneFeatureFlags", lpparam.classLoader), "supportHardwareVersion", supportHardwareVersion);
						
						Class<?> clsHAHP = findClass("com.android.settings.framework.preference.aboutphone.HtcAboutPhoneHardwarePreference", lpparam.classLoader);
						HtcPreference hwPref = (HtcPreference)clsHAHP.getConstructor(Context.class).newInstance((Context)XposedHelpers.callMethod(param.thisObject, "getContext"));
						HtcPreferenceFragment prefFrag = (HtcPreferenceFragment)param.thisObject;
						hwPref.setOrder(4);
						prefFrag.getPreferenceScreen().addPreference(hwPref);
						XposedHelpers.callMethod(param.thisObject, "addCallback", hwPref);
					}
				});
			} catch (Throwable t2) {
				XposedBridge.log(t2);
			}
		}
	}
	
	static HtcRimButton apk_launch_btn = null;
	static HtcListItem uninstall_start_item = null;
	static ApplicationInfo appInfo = null;
	static Context theContext = null;
	static Boolean showDisabledOnly = false;
	
	public static void execHook_AppFilter(LoadPackageParam lpparam) {
		final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
		
		XposedBridge.hookAllConstructors(findClass("com.android.settings.applications.ManageApplications", lpparam.classLoader), new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				showDisabledOnly = false;
			}
		});

		// Add and setup new menu item
		findAndHookMethod("com.android.settings.applications.ManageApplicationsFragment", lpparam.classLoader, "onCreateOptionsMenu", Menu.class, MenuInflater.class, new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Menu mOptionsMenu = (Menu)XposedHelpers.getObjectField(param.thisObject, "mOptionsMenu");
				if (mOptionsMenu != null) {
					int mCurView = XposedHelpers.getIntField(param.thisObject, "mCurView");
					if (mCurView != 2)
					if (showDisabledOnly)
						mOptionsMenu.add(0, 31337, 2, Helpers.xl10n(modRes, R.string.apps_all)).setShowAsAction(0);
					else
						mOptionsMenu.add(0, 31337, 2, Helpers.xl10n(modRes, R.string.apps_disabled)).setShowAsAction(0);
					XposedHelpers.callMethod(param.thisObject, "updateOptionsMenu");
				}
			}
		});
		
		findAndHookMethod("com.android.settings.applications.ManageApplicationsFragment", lpparam.classLoader, "onOptionsItemSelected", MenuItem.class, new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				int i = ((MenuItem)param.args[0]).getItemId();
				if (i == 31337) {
					showDisabledOnly = !showDisabledOnly;
					int mCurView = XposedHelpers.getIntField(param.thisObject, "mCurView");
					if (mCurView != 2) {
						Object mApplicationsAdapter = XposedHelpers.getObjectField(param.thisObject, "mApplicationsAdapter");
						XposedHelpers.callMethod(mApplicationsAdapter, "rebuild", true);
					}
					XposedHelpers.callMethod(param.thisObject, "updateOptionsMenu");
				}
			}
		});
		
		findAndHookMethod("com.android.settings.applications.ManageApplicationsFragment", lpparam.classLoader, "updateOptionsMenu", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				int mCurView = XposedHelpers.getIntField(param.thisObject, "mCurView");
				Menu mOptionsMenu = (Menu)XposedHelpers.getObjectField(param.thisObject, "mOptionsMenu");
				if (mOptionsMenu != null && mOptionsMenu.size() > 0)
				if (mCurView != 2) {
					MenuItem eleetMenuItem = mOptionsMenu.findItem(31337);
					if (eleetMenuItem != null) {
						eleetMenuItem.setVisible(true);
						if (showDisabledOnly)
							eleetMenuItem.setTitle(Helpers.xl10n(modRes, R.string.apps_all));
						else
							eleetMenuItem.setTitle(Helpers.xl10n(modRes, R.string.apps_disabled));
					}
				}
			}
		});

		// Apply custom filter after stock one
		findAndHookMethod("com.android.settings.applications.ManageApplicationsFragment$ApplicationsAdapter", lpparam.classLoader, "applyPrefixFilter", CharSequence.class, ArrayList.class, new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				@SuppressWarnings("unchecked")
				ArrayList<Object> arraylist = (ArrayList<Object>)param.getResult();
				ArrayList<Object> arraylist2 = new ArrayList<Object>();
				if (showDisabledOnly) {
					for (int i = 0; i < arraylist.size(); i++) {
						Object entry = arraylist.get(i);
						ApplicationInfo entryAppInfo = (ApplicationInfo)XposedHelpers.getObjectField(entry, "info");
						if (!entryAppInfo.enabled) arraylist2.add(entry);
					}
				} else arraylist2 = arraylist;
				param.setResult(arraylist2);
			}
		});
	}
	
	public static void execHook_Apps(LoadPackageParam lpparam) {
		final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
		
		findAndHookMethod("com.android.settings.applications.InstalledAppDetails", lpparam.classLoader, "onHandleUiMessage", Message.class, new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				// Change elements dynamically
				Message msg = (Message)param.args[0];
				if (msg == null) return;
				if (msg.what != 5 || apk_launch_btn == null) return;
					
				Bundle bundle = (Bundle)msg.obj;
				int i = Integer.valueOf(bundle.getInt("widget_id_field")).intValue();
				//String s = bundle.getString("widget_text_field");
				//Boolean boolean1 = Boolean.valueOf(bundle.getBoolean("widget_enabled_field"));
				
				if (i == 107)
				try {
					final Intent mainActivity = theContext.getPackageManager().getLaunchIntentForPackage(appInfo.packageName);
					if (mainActivity == null)
						apk_launch_btn.setEnabled(false);
					else {
						apk_launch_btn.setEnabled(true);
						apk_launch_btn.setOnClickListener(new HtcRimButton.OnClickListener() {
							@Override
							public void onClick(View v) {
								theContext.startActivity(mainActivity);
							}
						});
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
				
				if (i == 103)
				try {
					int j = bundle.getInt("widget_visibility_field");
					if (j == 8) {
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
						lp.setMargins(0, 0, 0, 0);
						apk_launch_btn.setLayoutParams(lp);
					}
					if (uninstall_start_item != null) {
						if (!apk_launch_btn.isEnabled() && j != 0)
						uninstall_start_item.setVisibility(8);
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
			
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// Add new options to App Details view
				Message msg = (Message)param.args[0];
				if (msg == null) return;
				if (msg.what == 4) try {
					ScrollView mRootView = (ScrollView)XposedHelpers.getObjectField(param.thisObject, "mRootView");
					
					Object mAppEntry = XposedHelpers.getObjectField(param.thisObject, "mAppEntry");
					if (mAppEntry == null) {
						XposedBridge.log("Cannot get mAppEntry");
						return;
					}
					final File apkFile = (File)XposedHelpers.getObjectField(mAppEntry, "apkFile");
					appInfo = (ApplicationInfo)XposedHelpers.getObjectField(mAppEntry, "info");
					LinearLayout all_details = (LinearLayout)mRootView.getChildAt(0);
					theContext = all_details.getContext();
					
					int uninstall_item_id = all_details.getResources().getIdentifier("uninstall_button_item", "id", "com.android.settings");
					HtcListItem uninstall_item = (HtcListItem)all_details.findViewById(uninstall_item_id);
					HtcRimButton uninstall_btn = (HtcRimButton)uninstall_item.getChildAt(0);
					
					HtcListItemSeparator toolbox_separator_apk = new HtcListItemSeparator(theContext);
					toolbox_separator_apk.setText(0, Helpers.xl10n(modRes, R.string.appdetails_package));
					
					HtcListItem toolbox_item_filename = new HtcListItem(theContext);
					HtcListItem2LineText toolbox_item_filename_text = new HtcListItem2LineText(theContext);
					toolbox_item_filename_text.setPrimaryText(Helpers.xl10n(modRes, R.string.appdetails_apk_file));
					toolbox_item_filename_text.setSecondaryText(apkFile.getName());
					HtcListItem2LineStamp toolbox_item_filename_stamp = new HtcListItem2LineStamp(theContext);
					toolbox_item_filename_stamp.setPrimaryText(Formatter.formatFileSize(theContext, apkFile.length()));
					toolbox_item_filename.addView(toolbox_item_filename_text);
					toolbox_item_filename.addView(toolbox_item_filename_stamp);
					
					HtcListItem toolbox_item_path = new HtcListItem(theContext);
					HtcListItem2LineText toolbox_item_path_text = new HtcListItem2LineText(theContext);
					toolbox_item_path_text.setPrimaryText(Helpers.xl10n(modRes, R.string.appdetails_apk_path));
					toolbox_item_path_text.setSecondaryText(apkFile.getParent() + "/");
					toolbox_item_path.addView(toolbox_item_path_text);
					
					HtcListItem toolbox_item_data = new HtcListItem(theContext);
					HtcListItem2LineText toolbox_item_data_text = new HtcListItem2LineText(theContext);
					toolbox_item_data_text.setPrimaryText(Helpers.xl10n(modRes, R.string.appdetails_data_path));
					toolbox_item_data_text.setSecondaryText(appInfo.dataDir);
					toolbox_item_data.addView(toolbox_item_data_text);
					
					HtcListItemSeparator toolbox_separator_dev = new HtcListItemSeparator(theContext);
					toolbox_separator_dev.setText(0, Helpers.xl10n(modRes, R.string.appdetails_dev));
					int permissions_id = all_details.getResources().getIdentifier("permissions_section", "id", "com.android.settings");
					LinearLayout permissions_section = (LinearLayout)all_details.findViewById(permissions_id);
					int perm_pos;
					for (perm_pos = 0; perm_pos < all_details.getChildCount(); perm_pos++)
					if (all_details.getChildAt(perm_pos).equals(permissions_section)) break;
					
					HtcListItem toolbox_item_process = new HtcListItem(theContext);
					HtcListItem2LineText toolbox_item_process_text = new HtcListItem2LineText(theContext);
					toolbox_item_process_text.setPrimaryText(Helpers.xl10n(modRes, R.string.appdetails_proc_name));
					toolbox_item_process_text.setSecondaryText(appInfo.processName);
					toolbox_item_process.addView(toolbox_item_process_text);
					
					HtcListItem toolbox_item_uid = new HtcListItem(theContext);
					HtcListItem2LineText toolbox_item_uid_text = new HtcListItem2LineText(theContext);
					toolbox_item_uid_text.setPrimaryText(Helpers.xl10n(modRes, R.string.appdetails_uid));
					toolbox_item_uid_text.setSecondaryText(String.valueOf(appInfo.uid));
					toolbox_item_uid.addView(toolbox_item_uid_text);
					
					HtcListItem toolbox_item_api = new HtcListItem(theContext);
					HtcListItem2LineText toolbox_item_api_text = new HtcListItem2LineText(theContext);
					toolbox_item_api_text.setPrimaryText(Helpers.xl10n(modRes, R.string.appdetails_sdk));
					toolbox_item_api_text.setSecondaryText(String.valueOf(appInfo.targetSdkVersion));
					toolbox_item_api.addView(toolbox_item_api_text);
					
					apk_launch_btn = new HtcRimButton(theContext);
					
					final Intent mainActivity = theContext.getPackageManager().getLaunchIntentForPackage(appInfo.packageName);
					apk_launch_btn.setText(Helpers.xl10n(modRes, R.string.appdetails_launch));
					if (mainActivity == null)
						apk_launch_btn.setEnabled(false);
					else
						apk_launch_btn.setOnClickListener(new HtcRimButton.OnClickListener() {
							@Override
							public void onClick(View v) {
								theContext.startActivity(mainActivity);
							}
						});
					
					LinearLayout.LayoutParams htclp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
					LinearLayout.LayoutParams htclp2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
					float density = theContext.getResources().getDisplayMetrics().density;
					
					htclp1.setMargins(0, 0, Math.round(5.0f * density), 0);
					htclp2.setMargins(Math.round(5.0f * density), 0, 0, 0);
					uninstall_btn.setLayoutParams(htclp1);
					apk_launch_btn.setLayoutParams(htclp2);
					
					uninstall_item.removeView(uninstall_btn);
					all_details.removeView(uninstall_item);
					
					uninstall_start_item = new HtcListItem(theContext);
					HtcListItem.LayoutParams htclp_item = new HtcListItem.LayoutParams(HtcListItem.LayoutParams.MATCH_PARENT, HtcListItem.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
					LinearLayout uninstall_start = new LinearLayout(theContext);
					uninstall_start.setLayoutParams(htclp_item);
					uninstall_start.setOrientation(LinearLayout.HORIZONTAL);
					uninstall_start.addView(uninstall_btn);
					uninstall_start.addView(apk_launch_btn);
					uninstall_start_item.addView(uninstall_start);
					
					all_details.addView(uninstall_start_item, 4);
					all_details.addView(toolbox_separator_apk, 5);
					all_details.addView(toolbox_item_filename, 6);
					all_details.addView(toolbox_item_path, 7);
					all_details.addView(toolbox_item_data, 8);
					all_details.addView(toolbox_separator_dev, 22);
					all_details.addView(toolbox_item_process, 23);
					all_details.addView(toolbox_item_uid, 24);
					all_details.addView(toolbox_item_api, 25);
					
					XposedHelpers.callMethod(param.thisObject, "initUninstallButton");
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
}

package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import java.io.File;
import java.util.ArrayList;

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
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
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
						Object hwPref = clsHAHP.getConstructor(Context.class).newInstance((Context)XposedHelpers.callMethod(param.thisObject, "getContext"));
						XposedHelpers.callMethod(hwPref, "setOrder", 4);
						Object prefScr = XposedHelpers.callMethod(param.thisObject, "getPreferenceScreen");
						XposedHelpers.callMethod(prefScr, "addPreference", hwPref);
						XposedHelpers.callMethod(param.thisObject, "addCallback", hwPref);
					}
				});
			} catch (Throwable t2) {
				XposedBridge.log(t2);
			}
		}
	}
	
	static Boolean showDisabledOnly = false;
	
	public static void execHook_AppFilter(LoadPackageParam lpparam) {
		final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
		try {
			
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
		
		} catch (Throwable t) {}
	}
	
	static HtcRimButton apk_launch_btn = null;
	static HtcListItem uninstall_start_item = null;
	static ApplicationInfo appInfo = null;
	static Context theContext = null;
	
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
						uninstall_start_item.setVisibility(View.GONE);
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
					FrameLayout uninstall_item = (FrameLayout)all_details.findViewById(uninstall_item_id);
					Button uninstall_btn = (Button)uninstall_item.getChildAt(0);
					
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
					toolbox_item_path_text.setSecondaryText(apkFile.getParent());
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
					if (Helpers.isLP())
						apk_launch_btn.setBackground(modRes.getDrawable(R.drawable.button_selector_light));
					else if (uninstall_btn != null && uninstall_btn.getBackground() != null)
						apk_launch_btn.setBackground(uninstall_btn.getBackground().mutate());
					
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
					
					int startCnt = 4;
					if (Helpers.isSense7()) startCnt = 3;
					all_details.addView(uninstall_start_item, startCnt);
					all_details.addView(toolbox_separator_apk, startCnt + 1);
					all_details.addView(toolbox_item_filename, startCnt + 2);
					all_details.addView(toolbox_item_path, startCnt + 3);
					all_details.addView(toolbox_item_data, startCnt + 4);
					all_details.addView(toolbox_separator_dev, startCnt + 18);
					all_details.addView(toolbox_item_process, startCnt + 19);
					all_details.addView(toolbox_item_uid, startCnt + 20);
					all_details.addView(toolbox_item_api, startCnt + 21);
					
					XposedHelpers.callMethod(param.thisObject, "initUninstallButton");
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
	
	static Button mapk_launch_btn = null;
	static RelativeLayout muninstall_start_item = null;
	
	private static RelativeLayout makeItem(String text1, String text2, String stamp) {
		float density = theContext.getResources().getDisplayMetrics().density;
		
		RelativeLayout list_item = new RelativeLayout(theContext);
		list_item.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		int padding = Math.round(15.33f * density);
		list_item.setPadding(padding, padding, padding, 0);
		list_item.setClipToPadding(false);
		
		TextView tv1 = new TextView(theContext);
		tv1.setId(10001);
		tv1.setText(text1);
		tv1.setTextColor(0xff4b4b4b);
		tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
		tv1.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
		RelativeLayout.LayoutParams tlp1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		tlp1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		tlp1.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		tv1.setLayoutParams(tlp1);
		list_item.addView(tv1);

		TextView tv2 = new TextView(theContext);
		tv2.setId(10002);
		tv2.setText(text2);
		tv2.setTextColor(0xff4b4b4b);
		tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv2.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
		RelativeLayout.LayoutParams tlp2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		tlp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		tlp2.addRule(RelativeLayout.BELOW, 10001);
		tv2.setLayoutParams(tlp2);
		list_item.addView(tv2);
		
		if (stamp != null) {
			TextView tv3 = new TextView(theContext);
			tv3.setText(stamp);
			tv3.setTextColor(0xff888888);
			tv3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
			tv3.setAllCaps(true);
			tv3.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
			RelativeLayout.LayoutParams tlp3 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			tlp3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			tlp3.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
			tlp3.topMargin = Math.round(4 * density);
			tv3.setLayoutParams(tlp3);
			list_item.addView(tv3);
		}
		
		FrameLayout divider = new FrameLayout(theContext);
		RelativeLayout.LayoutParams tlp4 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 2);
		tlp4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		tlp4.addRule(RelativeLayout.BELOW, 10002);
		tlp4.topMargin = padding;
		XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
		divider.setBackground(modRes.getDrawable(R.drawable.common_list_divider));
		divider.setLayoutParams(tlp4);
		list_item.addView(divider);
		
		return list_item;
	}
	
	public static void execHook_AppsM(LoadPackageParam lpparam) {
		final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
		
		findAndHookMethod("com.android.settings.applications.InstalledAppDetails", lpparam.classLoader, "onHandleUiMessage", Message.class, new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				// Change elements dynamically
				Message msg = (Message)param.args[0];
				if (msg == null) return;
				if (msg.what != 5 || mapk_launch_btn == null) return;
					
				Bundle bundle = (Bundle)msg.obj;
				int i = Integer.valueOf(bundle.getInt("widget_id_field")).intValue();
				//String s = bundle.getString("widget_text_field");
				//Boolean boolean1 = Boolean.valueOf(bundle.getBoolean("widget_enabled_field"));
				
				if (i == 107)
				try {
					final Intent mainActivity = theContext.getPackageManager().getLaunchIntentForPackage(appInfo.packageName);
					if (mainActivity == null) {
						mapk_launch_btn.setEnabled(false);
						mapk_launch_btn.setAlpha(0.5f);
					} else {
						mapk_launch_btn.setEnabled(true);
						mapk_launch_btn.setAlpha(1.0f);
						mapk_launch_btn.setOnClickListener(new Button.OnClickListener() {
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
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Math.round(34.66f * theContext.getResources().getDisplayMetrics().density), 1);
						lp.setMargins(0, 0, 0, 0);
						mapk_launch_btn.setLayoutParams(lp);
					}
					if (muninstall_start_item != null) {
						if (!mapk_launch_btn.isEnabled() && j != 0)
						muninstall_start_item.setVisibility(View.GONE);
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
					FrameLayout uninstall_item = (FrameLayout)all_details.findViewById(uninstall_item_id);
					Button uninstall_btn = (Button)uninstall_item.getChildAt(0);
					
					float density = theContext.getResources().getDisplayMetrics().density;
					
					TextView toolbox_separator_apk = new TextView(theContext);
					LinearLayout.LayoutParams lpsep = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Math.round(30 * density));
					toolbox_separator_apk.setLayoutParams(lpsep);
					toolbox_separator_apk.setBackground(modRes.getDrawable(R.drawable.category_header));
					toolbox_separator_apk.setTextColor(0xff939393);
					toolbox_separator_apk.setAllCaps(true);
					toolbox_separator_apk.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
					toolbox_separator_apk.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
					toolbox_separator_apk.setPadding(Math.round(15.33f * density), 0, Math.round(15.33f * density), 0);
					toolbox_separator_apk.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
					toolbox_separator_apk.setText(Helpers.xl10n(modRes, R.string.appdetails_package));
					
					RelativeLayout toolbox_item_filename = makeItem(Helpers.xl10n(modRes, R.string.appdetails_apk_file), apkFile.getName(), Formatter.formatFileSize(theContext, apkFile.length()));
					RelativeLayout toolbox_item_path = makeItem(Helpers.xl10n(modRes, R.string.appdetails_apk_path), apkFile.getParent(), null);
					RelativeLayout toolbox_item_data = makeItem(Helpers.xl10n(modRes, R.string.appdetails_data_path), appInfo.dataDir, null);
					
					TextView toolbox_separator_dev = new TextView(theContext);
					toolbox_separator_dev.setLayoutParams(lpsep);
					toolbox_separator_dev.setBackground(modRes.getDrawable(R.drawable.category_header));
					toolbox_separator_dev.setTextColor(0xff939393);
					toolbox_separator_dev.setAllCaps(true);
					toolbox_separator_dev.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
					toolbox_separator_dev.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
					toolbox_separator_dev.setPadding(Math.round(15.33f * density), 0, Math.round(15.33f * density), 0);
					toolbox_separator_dev.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
					toolbox_separator_dev.setText(Helpers.xl10n(modRes, R.string.appdetails_dev));
					int permissions_id = all_details.getResources().getIdentifier("permissions_section", "id", "com.android.settings");
					LinearLayout permissions_section = (LinearLayout)all_details.findViewById(permissions_id);
					int perm_pos;
					for (perm_pos = 0; perm_pos < all_details.getChildCount(); perm_pos++)
					if (all_details.getChildAt(perm_pos).equals(permissions_section)) break;
					
					RelativeLayout toolbox_item_process = makeItem(Helpers.xl10n(modRes, R.string.appdetails_proc_name), appInfo.processName, null);
					RelativeLayout toolbox_item_uid = makeItem(Helpers.xl10n(modRes, R.string.appdetails_uid), String.valueOf(appInfo.uid), null);
					RelativeLayout toolbox_item_api = makeItem(Helpers.xl10n(modRes, R.string.appdetails_sdk), String.valueOf(appInfo.targetSdkVersion), null);

					mapk_launch_btn = new Button(theContext);
					if (uninstall_btn.getBackground() != null) mapk_launch_btn.setBackground(uninstall_btn.getBackground().getConstantState().newDrawable().mutate());
					mapk_launch_btn.setPadding(uninstall_btn.getPaddingLeft(), uninstall_btn.getPaddingTop(), uninstall_btn.getPaddingRight(), uninstall_btn.getPaddingBottom());
					mapk_launch_btn.setTypeface(uninstall_btn.getTypeface());
					mapk_launch_btn.setTextColor(uninstall_btn.getCurrentTextColor());
					mapk_launch_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, uninstall_btn.getTextSize());
					mapk_launch_btn.setAllCaps(false);
					mapk_launch_btn.setIncludeFontPadding(uninstall_btn.getIncludeFontPadding());
					
					final Intent mainActivity = theContext.getPackageManager().getLaunchIntentForPackage(appInfo.packageName);
					mapk_launch_btn.setText(Helpers.xl10n(modRes, R.string.appdetails_launch));
					if (mainActivity == null) {
						mapk_launch_btn.setEnabled(false);
						mapk_launch_btn.setAlpha(0.5f);
					} else {
						mapk_launch_btn.setEnabled(true);
						mapk_launch_btn.setAlpha(1.0f);
						mapk_launch_btn.setOnClickListener(new Button.OnClickListener() {
							@Override
							public void onClick(View v) {
								theContext.startActivity(mainActivity);
							}
						});
					}

					int btnHeight = Math.round(34.66f * density);
					LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, btnHeight, 1);
					LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, btnHeight, 1);
					
					lp1.setMargins(0, 0, Math.round(5.0f * density), 0);
					lp2.setMargins(Math.round(5.0f * density), 0, 0, 0);
					uninstall_btn.setLayoutParams(lp1);
					mapk_launch_btn.setLayoutParams(lp2);
					
					uninstall_item.removeView(uninstall_btn);
					all_details.removeView(uninstall_item);
					
					muninstall_start_item = new RelativeLayout(theContext);
					muninstall_start_item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
					muninstall_start_item.setPadding(0, Math.round(15.33f * density), 0, Math.round(15.33f * density));
					LinearLayout uninstall_start = new LinearLayout(theContext);
					uninstall_start.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
					uninstall_start.setPadding(Math.round(15.33f * density), 0, Math.round(15.33f * density), 0);
					uninstall_start.setOrientation(LinearLayout.HORIZONTAL);
					uninstall_start.addView(uninstall_btn);
					uninstall_start.addView(mapk_launch_btn);
					muninstall_start_item.addView(uninstall_start);
					
					int startCnt = 4;
					if (Helpers.isSense7()) startCnt = 3;
					all_details.addView(muninstall_start_item, startCnt);
					all_details.addView(toolbox_separator_apk, startCnt + 1);
					all_details.addView(toolbox_item_filename, startCnt + 2);
					all_details.addView(toolbox_item_path, startCnt + 3);
					all_details.addView(toolbox_item_data, startCnt + 4);
					all_details.addView(toolbox_separator_dev, startCnt + 18);
					all_details.addView(toolbox_item_process, startCnt + 19);
					all_details.addView(toolbox_item_uid, startCnt + 20);
					all_details.addView(toolbox_item_api, startCnt + 21);
					
					XposedHelpers.callMethod(param.thisObject, "initUninstallButton");
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
}

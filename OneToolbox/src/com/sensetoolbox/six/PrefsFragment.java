package com.sensetoolbox.six;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.htc.app.HtcProgressDialog;
import com.htc.preference.HtcCheckBoxPreference;
import com.htc.preference.HtcListPreference;
import com.htc.preference.HtcPreference;
import com.htc.preference.HtcPreference.OnPreferenceChangeListener;
import com.htc.preference.HtcPreference.OnPreferenceClickListener;
import com.htc.preference.HtcPreferenceCategory;
import com.htc.preference.HtcPreferenceFragment;
import com.htc.preference.HtcPreferenceManager;
import com.htc.preference.HtcPreferenceScreen;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.HtcAlertDialog;
import com.sensetoolbox.six.utils.ApkInstaller;
import com.sensetoolbox.six.utils.ColorPreference;
import com.sensetoolbox.six.utils.DynamicPreference;
import com.sensetoolbox.six.utils.Helpers;
import com.sensetoolbox.six.utils.HtcListPreferencePlus;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class PrefsFragment extends HtcPreferenceFragment {
	
	static public List<ResolveInfo> pkgAppsList = null;
	static public List<Drawable> pkgAppsListIcons = new ArrayList<Drawable>();
	static public List<Boolean> pkgAppsListSystem = new ArrayList<Boolean>();
	static public SharedPreferences prefs = null;
	private boolean toolboxModuleActive = false;
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_mods, menu);
		menu.getItem(2).setIcon(Helpers.applySenseTheme(getActivity(), menu.getItem(2).getIcon()));
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		if(!Helpers.isXposedInstalled(getActivity()))
		{
			HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(getActivity());
			builder.setTitle(R.string.xposed_not_found);
			builder.setMessage(R.string.xposed_not_found_explain);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setNeutralButton(R.string.okay, null);
			HtcAlertDialog dlg = builder.create();
			dlg.show();
		} else checkForXposed();
		
		getPreferenceManager().setSharedPreferencesName("one_toolbox_prefs");
		getPreferenceManager().setSharedPreferencesMode(1);
		HtcPreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
		
		addPreferencesFromResource(R.xml.preferences);

		//Save current Sense version into the sharedprefs
		prefs = getPreferenceManager().getSharedPreferences();
		String senseVer = Helpers.getSenseVersion();
		prefs.edit().putString("pref_sense_version", senseVer).commit();
		
		if (prefs.getBoolean("pref_key_was_restore", false))
		{
			prefs.edit().putBoolean("pref_key_was_restore", false).commit();
			showRestoreInfoDialog();
		}
		
		if (findPreference("pref_key_other_oldtoasts") != null) ((HtcPreferenceScreen) findPreference("pref_key_other")).removePreference(findPreference("pref_key_other_oldtoasts"));
		
		if (!Build.DEVICE.equalsIgnoreCase("m7")) {
			if (findPreference("pref_key_other_keyslight") != null) ((HtcPreferenceScreen) findPreference("pref_key_other")).removePreference(findPreference("pref_key_other_keyslight"));
			if (findPreference("pref_key_other_keyslight_auto") != null) ((HtcPreferenceScreen) findPreference("pref_key_other")).removePreference(findPreference("pref_key_other_keyslight_auto"));
		}
		
		//Add version name to support title
		try {
			HtcPreferenceCategory supportCat = (HtcPreferenceCategory) findPreference("pref_key_support");
			supportCat.setTitle(getActivity().getString(R.string.support_version, getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName));
		} catch (NameNotFoundException e) {
			//Shouldn't happen...
			e.printStackTrace();
		}

		HtcPreference.OnPreferenceChangeListener camChangeListener = new HtcPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
				Toast.makeText(getActivity(), R.string.close_camera, Toast.LENGTH_LONG).show();
				return true;
			}
		};
		
		HtcPreference.OnPreferenceChangeListener chooseAction = new HtcPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
				HtcPreference launchApps = null;
				HtcPreference toggleSettings = null;
				
				if (preference.equals(findPreference("pref_key_prism_swipedownaction"))) {
					launchApps = findPreference("pref_key_prism_swipedown_app");
					toggleSettings = findPreference("pref_key_prism_swipedown_toggle");
				}
				
				if (preference.equals(findPreference("pref_key_prism_swipeupaction"))) {
					launchApps = findPreference("pref_key_prism_swipeup_app");
					toggleSettings = findPreference("pref_key_prism_swipeup_toggle");
				}
				
				if (preference.equals(findPreference("pref_key_prism_swiperightaction"))) {
					launchApps = findPreference("pref_key_prism_swiperight_app");
					toggleSettings = findPreference("pref_key_prism_swiperight_toggle");
				}
				
				if (preference.equals(findPreference("pref_key_prism_swipeleftaction"))) {
					launchApps = findPreference("pref_key_prism_swipeleft_app");
					toggleSettings = findPreference("pref_key_prism_swipeleft_toggle");
				}

				if (preference.equals(findPreference("pref_key_controls_backlongpressaction"))) {
					launchApps = findPreference("pref_key_controls_backlongpress_app");
					toggleSettings = findPreference("pref_key_controls_backlongpress_toggle");
				}

				if (preference.equals(findPreference("pref_key_controls_homeassistaction"))) {
					launchApps = findPreference("pref_key_controls_homeassist_app");
					toggleSettings = findPreference("pref_key_controls_homeassist_toggle");
				}
				
				if (preference.equals(findPreference("pref_key_prism_shakeaction"))) {
					launchApps = findPreference("pref_key_prism_shake_app");
					toggleSettings = findPreference("pref_key_prism_shake_toggle");
				}
				
				if (launchApps != null)
				if (newValue.equals("7")) {
					launchApps.setEnabled(true);
					if (launchApps instanceof DynamicPreference)
						((DynamicPreference)launchApps).show();
					else
						launchApps.getOnPreferenceClickListener().onPreferenceClick(launchApps);
				} else launchApps.setEnabled(false);
				
				if (toggleSettings != null)
				if (newValue.equals("8")) {
					toggleSettings.setEnabled(true);
					((HtcListPreferencePlus)toggleSettings).show();
				} else toggleSettings.setEnabled(false);
				
				return true;
			}
		};
		
		HtcListPreference.OnPreferenceChangeListener applyButtonsLight = new HtcListPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
				if (!(new File("/sys/class/leds/button-backlight/currents")).isFile()) {
					Toast.makeText(getActivity(), R.string.no_currents, Toast.LENGTH_LONG).show();
					return false;
				} else
					return setButtonBacklightTo(getActivity(), Integer.parseInt((String)newValue));
			}
		};
		
		final HtcPreference.OnPreferenceChangeListener fillSummary = new HtcPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
				preference.setSummary(getAppName((String)newValue));				
				return true;
			}
		};

		HtcPreference.OnPreferenceClickListener clickPref = new HtcPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(HtcPreference preference) {
				int prefCategory = 0;
				Context mContext = null;
				HtcPreferenceCategory senseGesturesCat = (HtcPreferenceCategory) findPreference("pref_key_sense_gestures");
				HtcPreferenceCategory senseControlsBackCat = (HtcPreferenceCategory) findPreference("pref_key_controls_back");
				HtcPreferenceCategory senseControlsHomeCat = (HtcPreferenceCategory) findPreference("pref_key_controls_home");
				
				if (senseGesturesCat.findPreference(preference.getKey()) != null) {
					prefCategory = 1;
					mContext = senseGesturesCat.getContext();
				} else if (senseControlsBackCat.findPreference(preference.getKey()) != null) {
					prefCategory = 2;
					mContext = senseControlsBackCat.getContext();
				} else if (senseControlsHomeCat.findPreference(preference.getKey()) != null) {
					prefCategory = 3;
					mContext = senseControlsHomeCat.getContext();
				} else return true;
				
				final DynamicPreference dp = new DynamicPreference(mContext);
				dp.setTitle(preference.getTitle());
				dp.setIcon(preference.getIcon());
				dp.setDialogTitle(preference.getTitle());
				dp.setSummary(preference.getSummary());
				dp.setOrder(preference.getOrder());
				dp.setKey(preference.getKey());
				dp.setOnPreferenceChangeListener(fillSummary);
				
				if (prefCategory == 1) {
					senseGesturesCat.removePreference(preference);
					senseGesturesCat.addPreference(dp);
				} else if (prefCategory == 2) {
					senseControlsBackCat.removePreference(preference);
					senseControlsBackCat.addPreference(dp);
				} else if (prefCategory == 3) {
					senseControlsHomeCat.removePreference(preference);
					senseControlsHomeCat.addPreference(dp);
				}
				
				if (PrefsFragment.pkgAppsList == null) {
					final HtcProgressDialog dialog = new HtcProgressDialog(getActivity());
					dialog.setMessage(getString(R.string.loading_app_data));
					dialog.setCancelable(false);
					dialog.show();
					
					new Thread() {
						@Override
						public void run() {
							try {
								getApps(getActivity());
								getActivity().runOnUiThread(new Runnable(){
									@Override
									public void run(){
										dialog.dismiss();
										dp.show();										
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}.start();
				} else dp.show();
				
				return true;
			}
		};
		
		//HtcPreferenceScreen scrPref = (HtcPreferenceScreen) findPreference("pref_key_sysui");
		//Fade fadeTrans = new Fade();
		//fadeTrans.addTarget(scrPref.getLayoutResource());
		
		ColorPreference colorChanger = (ColorPreference) findPreference("pref_key_colorfilter");
		HtcListPreference voldownPreference = (HtcListPreference) findPreference("pref_key_controls_camdownaction");
		HtcListPreference volupPreference = (HtcListPreference) findPreference("pref_key_controls_camupaction");
		HtcListPreference swipeDownActionPreference = (HtcListPreference) findPreference("pref_key_prism_swipedownaction");
		HtcListPreference swipeUpActionPreference = (HtcListPreference) findPreference("pref_key_prism_swipeupaction");
		HtcListPreference swipeRightActionPreference = (HtcListPreference) findPreference("pref_key_prism_swiperightaction");
		HtcListPreference swipeLeftActionPreference = (HtcListPreference) findPreference("pref_key_prism_swipeleftaction");
		HtcListPreference backLongPressActionPreference = (HtcListPreference) findPreference("pref_key_controls_backlongpressaction");
		HtcListPreference homeAssistActionPreference = (HtcListPreference) findPreference("pref_key_controls_homeassistaction");
		HtcListPreference shakeActionPreference = (HtcListPreference) findPreference("pref_key_prism_shakeaction");
		HtcListPreference keysLightPreference = (HtcListPreference) findPreference("pref_key_other_keyslight");
		HtcPreference senseThemesPreference = (HtcPreference) findPreference("pref_key_sense_themes");
		
		// Insert new option to controls listprefs
		CharSequence[] entries = backLongPressActionPreference.getEntries();
		entries = addToArray(entries, 5, getResources().getString(R.string.kill_foreground));
		entries = addToArray(entries, 6, getResources().getString(R.string.open_menu));
		entries = addToArray(entries, 7, getResources().getString(R.string.open_recents));
		CharSequence[] entryVals = backLongPressActionPreference.getEntryValues();
		entryVals = addToArray(entryVals, 5, "9");
		entryVals = addToArray(entryVals, 6, "10");
		entryVals = addToArray(entryVals, 7, "11");
		backLongPressActionPreference.setEntries(entries);
		backLongPressActionPreference.setEntryValues(entryVals);
		homeAssistActionPreference.setEntries(entries);
		homeAssistActionPreference.setEntryValues(entryVals);
		
		colorChanger.applyThemes();
		voldownPreference.setOnPreferenceChangeListener(camChangeListener);
		volupPreference.setOnPreferenceChangeListener(camChangeListener);
		swipeDownActionPreference.setOnPreferenceChangeListener(chooseAction);
		swipeUpActionPreference.setOnPreferenceChangeListener(chooseAction);
		swipeRightActionPreference.setOnPreferenceChangeListener(chooseAction);
		swipeLeftActionPreference.setOnPreferenceChangeListener(chooseAction);
		backLongPressActionPreference.setOnPreferenceChangeListener(chooseAction);
		homeAssistActionPreference.setOnPreferenceChangeListener(chooseAction);
		shakeActionPreference.setOnPreferenceChangeListener(chooseAction);
		if (keysLightPreference != null) keysLightPreference.setOnPreferenceChangeListener(applyButtonsLight);
		
		HtcPreference launchAppsSwipeDown = findPreference("pref_key_prism_swipedown_app");
		HtcPreference launchAppsSwipeUp = findPreference("pref_key_prism_swipeup_app");
		HtcPreference launchAppsSwipeRight = findPreference("pref_key_prism_swiperight_app");
		HtcPreference launchAppsSwipeLeft = findPreference("pref_key_prism_swipeleft_app");
		HtcPreference launchAppsBackLongPress = findPreference("pref_key_controls_backlongpress_app");
		HtcPreference launchAppsHomeAssist = findPreference("pref_key_controls_homeassist_app");
		HtcPreference launchAppsShake = findPreference("pref_key_prism_shake_app");
		
		HtcListPreferencePlus toggleSwipeDown = (HtcListPreferencePlus) findPreference("pref_key_prism_swipedown_toggle");
		HtcListPreferencePlus toggleSwipeUp = (HtcListPreferencePlus) findPreference("pref_key_prism_swipeup_toggle");
		HtcListPreferencePlus toggleSwipeRight = (HtcListPreferencePlus) findPreference("pref_key_prism_swiperight_toggle");
		HtcListPreferencePlus toggleSwipeLeft = (HtcListPreferencePlus) findPreference("pref_key_prism_swipeleft_toggle");
		HtcListPreferencePlus toggleBackLongPress = (HtcListPreferencePlus) findPreference("pref_key_controls_backlongpress_toggle");
		HtcListPreferencePlus toggleHomeAssist = (HtcListPreferencePlus) findPreference("pref_key_controls_homeassist_toggle");
		HtcListPreferencePlus toggleShake = (HtcListPreferencePlus) findPreference("pref_key_prism_shake_toggle");
		
		OnPreferenceChangeListener setEntryAsSummary = new OnPreferenceChangeListener() {
	        @Override
	        public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
	        	((HtcListPreferencePlus)preference).setValue((String)newValue);
	        	preference.setSummary(((HtcListPreferencePlus)preference).getEntry());
	            return false;
	        }
	    };
		
		String not_selected = getResources().getString(R.string.notselected);
		
		launchAppsSwipeDown.setSummary(getAppName(prefs.getString("pref_key_prism_swipedown_app", not_selected)));
		launchAppsSwipeDown.setOnPreferenceClickListener(clickPref);
		toggleSwipeDown.setSummary(toggleSwipeDown.getEntry() == null ? not_selected: toggleSwipeDown.getEntry());
		toggleSwipeDown.setOnPreferenceChangeListener(setEntryAsSummary);
		
		launchAppsSwipeUp.setSummary(getAppName(prefs.getString("pref_key_prism_swipeup_app", not_selected)));
		launchAppsSwipeUp.setOnPreferenceClickListener(clickPref);
		toggleSwipeUp.setSummary(toggleSwipeUp.getEntry() == null ? not_selected: toggleSwipeUp.getEntry());
		toggleSwipeUp.setOnPreferenceChangeListener(setEntryAsSummary);
		
		launchAppsSwipeRight.setSummary(getAppName(prefs.getString("pref_key_prism_swiperight_app", not_selected)));
		launchAppsSwipeRight.setOnPreferenceClickListener(clickPref);
		toggleSwipeRight.setSummary(toggleSwipeRight.getEntry() == null ? not_selected: toggleSwipeRight.getEntry());
		toggleSwipeRight.setOnPreferenceChangeListener(setEntryAsSummary);
		
		launchAppsSwipeLeft.setSummary(getAppName(prefs.getString("pref_key_prism_swipeleft_app", not_selected)));
		launchAppsSwipeLeft.setOnPreferenceClickListener(clickPref);
		toggleSwipeLeft.setSummary(toggleSwipeLeft.getEntry() == null ? not_selected: toggleSwipeLeft.getEntry());
		toggleSwipeLeft.setOnPreferenceChangeListener(setEntryAsSummary);
		
		launchAppsBackLongPress.setSummary(getAppName(prefs.getString("pref_key_controls_backlongpress_app", not_selected)));
		launchAppsBackLongPress.setOnPreferenceClickListener(clickPref);
		toggleBackLongPress.setSummary(toggleBackLongPress.getEntry() == null ? not_selected: toggleBackLongPress.getEntry());
		toggleBackLongPress.setOnPreferenceChangeListener(setEntryAsSummary);
		
		launchAppsHomeAssist.setSummary(getAppName(prefs.getString("pref_key_controls_homeassist_app", not_selected)));
		launchAppsHomeAssist.setOnPreferenceClickListener(clickPref);
		toggleHomeAssist.setSummary(toggleHomeAssist.getEntry() == null ? not_selected: toggleHomeAssist.getEntry());
		toggleHomeAssist.setOnPreferenceChangeListener(setEntryAsSummary);
		
		launchAppsShake.setSummary(getAppName(prefs.getString("pref_key_prism_shake_app", not_selected)));
		launchAppsShake.setOnPreferenceClickListener(clickPref);
		toggleShake.setSummary(toggleShake.getEntry() == null ? not_selected: toggleShake.getEntry());
		toggleShake.setOnPreferenceChangeListener(setEntryAsSummary);

		if (swipeDownActionPreference.getValue().equals("7"))		launchAppsSwipeDown.setEnabled(true);		else launchAppsSwipeDown.setEnabled(false);
		if (swipeDownActionPreference.getValue().equals("8"))		toggleSwipeDown.setEnabled(true);			else toggleSwipeDown.setEnabled(false);
		if (swipeUpActionPreference.getValue().equals("7"))			launchAppsSwipeUp.setEnabled(true);			else launchAppsSwipeUp.setEnabled(false);
		if (swipeUpActionPreference.getValue().equals("8"))			toggleSwipeUp.setEnabled(true);				else toggleSwipeUp.setEnabled(false);
		if (swipeRightActionPreference.getValue().equals("7"))		launchAppsSwipeRight.setEnabled(true);		else launchAppsSwipeRight.setEnabled(false);
		if (swipeRightActionPreference.getValue().equals("8"))		toggleSwipeRight.setEnabled(true);			else toggleSwipeRight.setEnabled(false);
		if (swipeLeftActionPreference.getValue().equals("7"))		launchAppsSwipeLeft.setEnabled(true);		else launchAppsSwipeLeft.setEnabled(false);
		if (swipeLeftActionPreference.getValue().equals("8"))		toggleSwipeLeft.setEnabled(true);			else toggleSwipeLeft.setEnabled(false);
		if (backLongPressActionPreference.getValue().equals("7"))	launchAppsBackLongPress.setEnabled(true);	else launchAppsBackLongPress.setEnabled(false);
		if (backLongPressActionPreference.getValue().equals("8"))	toggleBackLongPress.setEnabled(true);		else toggleBackLongPress.setEnabled(false);
		if (homeAssistActionPreference.getValue().equals("7"))		launchAppsHomeAssist.setEnabled(true);		else launchAppsHomeAssist.setEnabled(false);
		if (homeAssistActionPreference.getValue().equals("8"))		toggleHomeAssist.setEnabled(true);			else toggleHomeAssist.setEnabled(false);
		if (shakeActionPreference.getValue().equals("7"))			launchAppsShake.setEnabled(true);			else launchAppsShake.setEnabled(false);
		if (shakeActionPreference.getValue().equals("8"))			toggleShake.setEnabled(true);				else toggleShake.setEnabled(false);
		
        HtcPreference sunbeamInstallPref = findPreference("pref_key_cb_sunbeam");
        sunbeamInstallPref.setOnPreferenceClickListener(new HtcPreference.OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(HtcPreference preference) {
				ApkInstaller.installSunbeam(getActivity());
				return true;
			}
        });
        
        HtcPreference vol2wakePref = findPreference("pref_key_controls_vol2wake");
        vol2wakePref.setOnPreferenceClickListener(new HtcPreference.OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(HtcPreference preference) {
				initScriptHandler(((HtcCheckBoxPreference) preference).isChecked());
				return true;
			}
        });
        
        senseThemesPreference.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(HtcPreference arg0) {
				getActivity().startActivity(new Intent(getActivity(), SenseThemes.class));
				return true;
			}
        });
	}
	
	static boolean isWaitingForCmd = false;
	static boolean setButtonBacklightTo(Context ctx, final int pref_keyslight) {
		if (isWaitingForCmd) return false; else try {
			isWaitingForCmd = true;
			final String currents = "/sys/class/leds/button-backlight/currents";
			CommandCapture command = new CommandCapture(0, "cat " + currents) {
				int lineCnt = 0;
				
				@Override
				@SuppressWarnings("deprecation")
				public void output(int id, String line) {
					if (lineCnt > 0) return;
					
					String level = "20";
					if (pref_keyslight == 2) level = "7";
					else if (pref_keyslight == 3) level = "3";
					else if (pref_keyslight == 4) level = "0";
					
					if (!line.trim().equals(level)) {
						/*
						final String[] cmdsDefault = {
							"chown 1000 " + currents,
							"chmod 644 " + currents,
							"echo 20 > " + currents
						};
						*/
						final String[] cmdsPerm = {
							"chown " + String.valueOf(android.os.Process.myUid()) + " " + currents,
							"chmod 644 " + currents,
							"echo " + level + " > " + currents,
							"chmod 444 " + currents
						};
						final String[] cmds = {
							"chmod 644 " + currents,
							"echo " + level + " > " + currents,
							"chmod 444 " + currents
						};
						
						try {
							CommandCapture commandOwner = new CommandCapture(0, "stat -c '%u' " + currents) {
								int lineCnt2 = 0;
								
								@Override
								public void output(int id, String line) {
									if (lineCnt2 == 0) try {
										if (line.trim().equals("0") || line.trim().equals("1000")) {
											RootTools.sendShell(cmdsPerm, 0, null, 3000);
										} else
											RootTools.sendShell(cmds, 0, null, false, 3000);

										// 500ms interval between backlight updates
										new Thread() {
											@Override
											public void run() {
												try {
													sleep(500);
													isWaitingForCmd = false;
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}.start();
									} catch (Exception e) {
										e.printStackTrace();
									}
									lineCnt2++;
								}
							};
							RootTools.getShell(false).add(commandOwner);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else isWaitingForCmd = false;
					lineCnt++;
				}
			};
			RootTools.getShell(false).add(command);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			isWaitingForCmd = false;
			return false;
		}
	}
	
	static class SetButtonBacklight extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null) {
				int thepref = Integer.parseInt(context.getSharedPreferences("one_toolbox_prefs", 1).getString("pref_key_other_keyslight", "1"));
				if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
					if (thepref > 1) setButtonBacklightTo(context, thepref);
				} else if (intent.getAction().equals("com.sensetoolbox.six.UPDATEBACKLIGHT")) {
					boolean forceDisableBacklight = false;
					PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
					if (!pm.isScreenOn())
						forceDisableBacklight = true;
					else
						forceDisableBacklight = intent.getBooleanExtra("forceDisableBacklight", false);
					
					if (forceDisableBacklight)
						setButtonBacklightTo(context, 4);
					else
						setButtonBacklightTo(context, thepref);
				}
			}
		}
	}
	
	/**
	 * Enables or diables the init script for vol2wake
	 * @param newState true to enable, false to disable
	 */
	private static void initScriptHandler(Boolean newState)
	{
		if(newState)
		{
			CommandCapture command = new CommandCapture(0,
					"mount -o rw,remount /system",
					"echo \"#!/system/bin/sh\n\necho 1 > /sys/keyboard/vol_wakeup\nchmod 444 /sys/keyboard/vol_wakeup\" > /etc/init.d/89s5tvol2wake",
					"chmod 755 /system/etc/init.d/89s5tvol2wake",
					"sed -i 's/\\(key [0-9]\\+\\s\\+VOLUME_\\(DOWN\\|UP\\)$\\)/\\1   WAKE_DROPPED/gw /system/usr/keylayout/Generic.kl' /system/usr/keylayout/Generic.kl",
					"mount -o ro,remount /system");
			try {
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
		{
			CommandCapture command = new CommandCapture(0,
					"mount -o rw,remount /system",
					"rm -f /etc/init.d/89s5tvol2wake",
					"sed -i 's/\\(key [0-9]\\+\\s\\+VOLUME_\\(DOWN\\|UP\\)\\)\\s\\+WAKE_DROPPED/\\1/gw /system/usr/keylayout/Generic.kl' /system/usr/keylayout/Generic.kl",
					"mount -o ro,remount /system");
		    try {
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private CharSequence[] addToArray(CharSequence[] cs, int position, String toAdd) {
		List<CharSequence> entries = new ArrayList<CharSequence>(Arrays.asList(cs));
		entries.add(position, toAdd);
		CharSequence[] entriesNew = entries.toArray(new CharSequence[entries.size()]);
		return entriesNew;
	}
	
	public static void getApps(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		PrefsFragment.pkgAppsList = pm.queryIntentActivities(mainIntent, 0);
		Collections.sort(PrefsFragment.pkgAppsList, new ResolveInfo.DisplayNameComparator(pm));
		for (ResolveInfo inf: PrefsFragment.pkgAppsList) {
			PrefsFragment.pkgAppsListIcons.add(inf.loadIcon(pm));
			if ((inf.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)
				PrefsFragment.pkgAppsListSystem.add(true);
			else
				PrefsFragment.pkgAppsListSystem.add(false);
		}
	}
	
	public CharSequence getAppName(String pkgActName) {
		PackageManager pm = getActivity().getPackageManager();
		String not_selected = getResources().getString(R.string.notselected);
		String[] pkgActArray = pkgActName.split("\\|");
		ApplicationInfo ai = null;

		if (pkgActArray.length >= 1)
		if (!pkgActArray[0].trim().equals(""))
		if (pkgActName.equals(not_selected))
			ai = null;
		else try {
		    ai = pm.getApplicationInfo(pkgActArray[0], 0);
		} catch (Exception e) {
			e.printStackTrace();
		    ai = null;
		}
		return (ai != null ? pm.getApplicationLabel(ai) : not_selected);
	}
	
	public boolean checkStorageReadable() {
		Context ctx = getActivity();
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) || state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(ctx);
			alert.setTitle(R.string.warning);
			alert.setView(Helpers.createCenteredText(ctx, R.string.storage_unavailable));
			alert.setNeutralButton(ctx.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return false;
		}
	}
	
	public boolean preparePathForBackup(String path) {
		String state = Environment.getExternalStorageState();
		Context ctx = getActivity();
		if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(ctx);
			alert.setTitle(R.string.warning);
			alert.setView(Helpers.createCenteredText(ctx, R.string.storage_read_only));
			alert.setNeutralButton(ctx.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return false;
		} else if (state.equals(Environment.MEDIA_MOUNTED)) {
			File file = new File(path);
			if (!file.exists() && !file.mkdirs()) {
	        	HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(ctx);
				alert.setTitle(R.string.warning);
				alert.setView(Helpers.createCenteredText(ctx, R.string.storage_cannot_mkdir));
				alert.setNeutralButton(ctx.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
				});
				alert.show();
				return false;
		    }
			return true;
		} else {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(ctx);
			alert.setTitle(R.string.warning);
			alert.setView(Helpers.createCenteredText(ctx, R.string.storage_unavailable));
			alert.setNeutralButton(ctx.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return false;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.softreboot) {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(getActivity());
			alert.setTitle(R.string.soft_reboot);
			alert.setView(Helpers.createCenteredText(getActivity(), R.string.hotreboot_explain_prefs));
			alert.setPositiveButton(getText(R.string.yes) + "!", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						CommandCapture command = new CommandCapture(0, "setprop ctl.restart zygote");
						RootTools.getShell(true).add(command).waitForFinish();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			alert.setNegativeButton(getText(R.string.no) + "!", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return true;
		} else if (item.getItemId() == R.id.backuprestore) {
			final String backupPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SenseToolbox/";
			final String backupFile = "settings_backup";
			
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(getActivity());
			alert.setTitle(R.string.backup_restore);
			alert.setView(Helpers.createCenteredText(getActivity(), R.string.backup_restore_choose));
			alert.setPositiveButton(getText(R.string.do_restore), new DialogInterface.OnClickListener() {
				@SuppressWarnings("unchecked")
				public void onClick(DialogInterface dialog, int whichButton) {
					if (!checkStorageReadable()) return;
					ObjectInputStream input = null;
					try {
						input = new ObjectInputStream(new FileInputStream(backupPath + backupFile));
						Map<String, ?> entries = (Map<String, ?>)input.readObject();
						if (entries == null || entries.isEmpty()) throw new Exception("Cannot read entries");
							
						Editor prefEdit = prefs.edit();
						prefEdit.clear();
						for (Entry<String, ?> entry: entries.entrySet()) {
							Object val = entry.getValue();
							String key = entry.getKey();

							if (val instanceof Boolean)
								prefEdit.putBoolean(key, ((Boolean)val).booleanValue());
							else if (val instanceof Float)
								prefEdit.putFloat(key, ((Float)val).floatValue());
							else if (val instanceof Integer)
								prefEdit.putInt(key, ((Integer)val).intValue());
							else if (val instanceof Long)
								prefEdit.putLong(key, ((Long)val).longValue());
							else if (val instanceof String)
								prefEdit.putString(key, ((String)val));
							else if (val instanceof Set<?>)
								prefEdit.putStringSet(key, ((Set<String>)val));
						}
						prefEdit.commit();
						
						HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(getActivity());
						alert.setTitle(R.string.do_restore);
						alert.setView(Helpers.createCenteredText(getActivity(), R.string.restore_ok));
						alert.setCancelable(false);
						alert.setNeutralButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								getActivity().finish();
								startActivity(getActivity().getIntent());
							}
						});
						alert.show();
					} catch (Exception e) {
						HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(getActivity());
						alert.setTitle(R.string.warning);
						alert.setView(Helpers.createCenteredText(getActivity(), R.string.storage_cannot_restore));
						alert.setNeutralButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {}
						});
						alert.show();
					} finally {
						try {
							if (input != null) input.close();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			});
			alert.setNegativeButton(getText(R.string.do_backup), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if (!preparePathForBackup(backupPath)) return;
					ObjectOutputStream output = null;
					try {
						output = new ObjectOutputStream(new FileOutputStream(backupPath + backupFile));
						output.writeObject(prefs.getAll());
						
						HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(getActivity());
						alert.setTitle(R.string.do_backup);
						alert.setView(Helpers.createCenteredText(getActivity(), R.string.backup_ok));
						alert.setNeutralButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {}
						});
						alert.show();
					} catch (Exception e) {
						HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(getActivity());
						alert.setTitle(R.string.warning);
						alert.setView(Helpers.createCenteredText(getActivity(), R.string.storage_cannot_backup));
						alert.setNeutralButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {}
						});
						alert.show();
						
						e.printStackTrace();
					} finally {
						try {
							if (output != null) {
								output.flush();
								output.close();
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			});
			alert.setNeutralButton(getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return true;
		} else if (item.getItemId() == R.id.about) {
			Intent intent = new Intent(getActivity(), AboutScreen.class);
			startActivity(intent);
		}
		return true;
	}

	// Fix for the sub HtcPreferenceScreens HomeBackUp
	@Override
	public boolean onPreferenceTreeClick(HtcPreferenceScreen parentPreferenceScreen, HtcPreference preference) {
		super.onPreferenceTreeClick(parentPreferenceScreen, preference);
		if (preference instanceof HtcPreferenceScreen) {
			HtcPreferenceScreen preferenceScreen = (HtcPreferenceScreen) preference;
			final Dialog dialog = preferenceScreen.getDialog();

			if (dialog != null) {
				ActionBar ab = dialog.getActionBar();
				ActionBarExt actionBarExt = new ActionBarExt(this.getActivity(), ab);
				ActionBarContainer actionBarContainer = actionBarExt.getCustomContainer();
				actionBarContainer.setBackUpEnabled(true);
				
				View homeBtn = actionBarContainer.getChildAt(0);
				if (homeBtn != null) {
					OnClickListener dismissDialogClickListener = new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					};

					ViewParent homeBtnContainer = homeBtn.getParent();

					if (homeBtnContainer instanceof FrameLayout) {
						ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();

						if (containerParent instanceof LinearLayout) {
							((LinearLayout) containerParent).setOnClickListener(dismissDialogClickListener);
						} else {
							((FrameLayout) homeBtnContainer).setOnClickListener(dismissDialogClickListener);
						}
					} else {
						homeBtn.setOnClickListener(dismissDialogClickListener);
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isXposedInstalled = false;
	private int lineCount = 0;
	
	public void checkForXposed() {		
		CommandCapture command = new CommandCapture(0, "/system/bin/app_process --xposedversion 2>/dev/null") {
			@Override
			public void output(int id, String line)
			{
				if (lineCount > 0) return;
				Pattern pattern = Pattern.compile("Xposed version: (\\d+)");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String xposed_ver = matcher.group(1);
					try {
						Integer.parseInt(xposed_ver);
						isXposedInstalled = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (!isXposedInstalled) {					
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							showXposedDialog();
						}
					});
				} else if (!toolboxModuleActive) {
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							showXposedDialog2();
						}
					});
				}
				lineCount++;
			}
		};
		try {
			RootTools.getShell(false).add(command).waitForFinish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showXposedDialog()
	{
		HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(getActivity());
		builder.setTitle(R.string.warning);
		builder.setMessage(R.string.xposed_not_installed);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setCancelable(true);
		builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton){}
		});
		HtcAlertDialog dlg = builder.create();
		dlg.show();
	}
	
	public void showXposedDialog2()
	{
		HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(getActivity());
		builder.setTitle(R.string.warning);
		builder.setMessage(R.string.module_not_active);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setCancelable(true);
		builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton){}
		});
		HtcAlertDialog dlg = builder.create();
		dlg.show();
	}
	
	private void showRestoreInfoDialog()
	{
		HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(getActivity());
		builder.setTitle(R.string.warning);
		builder.setMessage(R.string.backup_restore_info);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setCancelable(true);
		builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton){}
		});
		HtcAlertDialog dlg = builder.create();
		dlg.show();
	}
}

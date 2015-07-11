package com.sensetoolbox.six.material;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.sensetoolbox.six.BlinkFeed;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.material.utils.ApkInstaller;
import com.sensetoolbox.six.material.utils.AppShortcutAddDialog;
import com.sensetoolbox.six.material.utils.DynamicPreference;
import com.sensetoolbox.six.material.utils.ListPreferenceEx;
import com.sensetoolbox.six.material.utils.MultiSelectListPreferenceEx;
import com.sensetoolbox.six.utils.AppData;
import com.sensetoolbox.six.utils.GlobalActions;
import com.sensetoolbox.six.utils.Helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.util.TypedValue;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MSubFragment extends MPreferenceFragmentExt {
private int xmlResId = 0;
	
	public MSubFragment() {
		super();
		this.setRetainInstance(true);
		xmlResId = 0;
	}
	
	public MSubFragment(int resId) {
		super();
		this.setRetainInstance(true);
		xmlResId = resId;
	}
	
	OnPreferenceChangeListener setEntryAsSummary = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			((ListPreferenceEx)preference).setValue((String)newValue);
			preference.setSummary(((ListPreferenceEx)preference).getEntry());
			return false;
		}
	};
	
	@Override
	@SuppressWarnings("unchecked")
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState, xmlResId);
		if (xmlResId == 0) {
			getActivity().finish();
			return;
		}
		addPreferencesFromResource(xmlResId);
		
		int mThemeBackground = Integer.parseInt(Helpers.prefs.getString("pref_key_toolbox_material_background", "1"));
		
		prefListView = (ListView)getActivity().findViewById(android.R.id.list);
		prefListView.setPadding(0, 0, 0, 0);
		final Typeface face = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
		final Typeface faceTitle = Typeface.create("sans-serif-condensed", Typeface.BOLD);
		final Typeface faceSecondary = Typeface.create("sans-serif-light", Typeface.NORMAL);
		prefListView.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
			@Override
			public void onChildViewAdded(View parent, View child) {
				if (child != null) {
					ArrayList<View> nViews = Helpers.getChildViewsRecursive(child);
					for (View nView: nViews) if (nView != null)
					if (nView instanceof TextView) try {
						TextView tView = (TextView)nView;
						if (tView.getId() == android.R.id.title && tView.getParent() instanceof ListView) {
							tView.setTypeface(faceTitle);
							tView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
						} else if (tView.getId() == android.R.id.summary) {
							tView.setTypeface(faceSecondary);
							tView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
						} else {
							tView.setTypeface(face);
							tView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
						}
					} catch (Exception e) {}
					else if (nView instanceof LinearLayout && nView.getId() == nView.getResources().getIdentifier("icon_frame", "id", "android")) try {
						((LinearLayout)nView).setGravity(Gravity.CENTER);
					} catch (Exception e) {}
				}
			}

			@Override
			public void onChildViewRemoved(View parent, View child) {}
		});
		
		if (xmlResId == R.xml.mprefs_systemui) {
			final ListPreference clockActionPreference = (ListPreference)findPreference("pref_key_controls_clockaction");
			final Preference launchAppsClock = findPreference("pref_key_controls_clock_app");
			if (Integer.parseInt(Helpers.prefs.getString("pref_key_sysui_headerclick", "1")) != 1) clockActionPreference.setEnabled(true);
			
			findPreference("pref_key_sysui_headerclick").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if (Integer.parseInt((String)newValue) != 1) {
						clockActionPreference.setEnabled(true);
						if (clockActionPreference.getValue().equals("2")) launchAppsClock.setEnabled(true); else launchAppsClock.setEnabled(false);
					} else {
						clockActionPreference.setEnabled(false);
						launchAppsClock.setEnabled(false);
					}
					return true;
				}
			});
			
			launchAppsClock.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_controls_clock_app", Helpers.l10n(getActivity(), R.string.notselected))));
			launchAppsClock.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					PreferenceCategory sysUiStatusBar = (PreferenceCategory)findPreference("pref_systemui_statusbar");
					makeDynamicPref(sysUiStatusBar, preference);
					return true;
				}
			});
			
			if (clockActionPreference.isEnabled() && clockActionPreference.getValue().equals("2")) launchAppsClock.setEnabled(true); else launchAppsClock.setEnabled(false);
			clockActionPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Preference launchApps = findPreference("pref_key_controls_clock_app");
					if (launchApps != null)
					if (preference.isEnabled() && newValue.equals("2")) {
						launchApps.setEnabled(true);
						if (launchApps instanceof DynamicPreference)
							((DynamicPreference)launchApps).show();
						else
							launchApps.getOnPreferenceClickListener().onPreferenceClick(launchApps);
					} else launchApps.setEnabled(false);
						
					if (newValue.equals("3")) {
						Helpers.shortcutDlgStock = new AppShortcutAddDialog(getActivity(), preference.getKey() + "_shortcut");
						Helpers.shortcutDlgStock.setTitle(preference.getTitle());
						Helpers.shortcutDlgStock.setIcon(preference.getIcon());
						Helpers.shortcutDlgStock.show();
					}
					
					return true;
				}
			});
		} else if (xmlResId == R.xml.mprefs_statusbar) {
			Preference sunbeamInstallPref = findPreference("pref_key_cb_sunbeam");
			sunbeamInstallPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					ApkInstaller.installSunbeam(getActivity());
					return true;
				}
			});
			
			if (!Helpers.hasRoot || !Helpers.hasRootAccess)
			Helpers.disablePref(this, "pref_key_cb_sunbeam", Helpers.l10n(getActivity(), R.string.no_root_summ));
			
			applyIconThemes();
		} else if (xmlResId == R.xml.mprefs_prism) {
			this.rebootType = 1;
			
			Preference.OnPreferenceChangeListener chooseAction = new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Preference launchApps = null;
					Preference toggleSettings = null;
					
					if (preference.equals(findPreference("pref_key_prism_appslongpressaction"))) {
						launchApps = findPreference("pref_key_prism_appslongpress_app");
						toggleSettings = findPreference("pref_key_prism_appslongpress_toggle");
					}
					
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
						((ListPreferenceEx)toggleSettings).show();
					} else toggleSettings.setEnabled(false);
					
					if (newValue.equals("12")) {
						Helpers.shortcutDlgStock = new AppShortcutAddDialog(getActivity(), preference.getKey() + "_shortcut");
						Helpers.shortcutDlgStock.setTitle(preference.getTitle());
						Helpers.shortcutDlgStock.setIcon(preference.getIcon());
						Helpers.shortcutDlgStock.show();
					}
					
					return true;
				}
			};
			
			Preference.OnPreferenceClickListener clickPref = new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					PreferenceCategory senseGesturesCat = (PreferenceCategory) findPreference("pref_key_sense_gestures");
					makeDynamicPref(senseGesturesCat, preference);
					return true;
				}
			};
			
			CheckBoxPreference.OnPreferenceChangeListener toggleBF = new CheckBoxPreference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					PackageManager pm = getActivity().getPackageManager();
					if ((Boolean)newValue)
						pm.setComponentEnabledSetting(new ComponentName(getActivity(), BlinkFeed.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
					else
						pm.setComponentEnabledSetting(new ComponentName(getActivity(), BlinkFeed.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					return true;
				}
			};
			
			ListPreference appsLongPressActionPreference = (ListPreference) findPreference("pref_key_prism_appslongpressaction");
			ListPreference swipeDownActionPreference = (ListPreference) findPreference("pref_key_prism_swipedownaction");
			ListPreference swipeUpActionPreference = (ListPreference) findPreference("pref_key_prism_swipeupaction");
			ListPreference swipeRightActionPreference = (ListPreference) findPreference("pref_key_prism_swiperightaction");
			ListPreference swipeLeftActionPreference = (ListPreference) findPreference("pref_key_prism_swipeleftaction");
			ListPreference shakeActionPreference = (ListPreference) findPreference("pref_key_prism_shakeaction");
			CheckBoxPreference blinkFeedIconPreference = (CheckBoxPreference) findPreference("pref_key_prism_blinkfeedicon");
			
			Preference launchAppsLongPress = findPreference("pref_key_prism_appslongpress_app");
			Preference launchAppsSwipeDown = findPreference("pref_key_prism_swipedown_app");
			Preference launchAppsSwipeUp = findPreference("pref_key_prism_swipeup_app");
			Preference launchAppsSwipeRight = findPreference("pref_key_prism_swiperight_app");
			Preference launchAppsSwipeLeft = findPreference("pref_key_prism_swipeleft_app");
			Preference launchAppsShake = findPreference("pref_key_prism_shake_app");
			
			ListPreferenceEx toggleAppsLongPress = (ListPreferenceEx) findPreference("pref_key_prism_appslongpress_toggle");
			ListPreferenceEx toggleSwipeDown = (ListPreferenceEx) findPreference("pref_key_prism_swipedown_toggle");
			ListPreferenceEx toggleSwipeUp = (ListPreferenceEx) findPreference("pref_key_prism_swipeup_toggle");
			ListPreferenceEx toggleSwipeRight = (ListPreferenceEx) findPreference("pref_key_prism_swiperight_toggle");
			ListPreferenceEx toggleSwipeLeft = (ListPreferenceEx) findPreference("pref_key_prism_swipeleft_toggle");
			ListPreferenceEx toggleShake = (ListPreferenceEx) findPreference("pref_key_prism_shake_toggle");
			
			String not_selected = Helpers.l10n(getActivity(), R.string.notselected);
			
			launchAppsLongPress.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_prism_appslongpress_app", not_selected)));
			launchAppsLongPress.setOnPreferenceClickListener(clickPref);
			toggleAppsLongPress.setSummary(toggleAppsLongPress.getEntry() == null ? not_selected: toggleAppsLongPress.getEntry());
			toggleAppsLongPress.setOnPreferenceChangeListener(setEntryAsSummary);
			
			launchAppsSwipeDown.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_prism_swipedown_app", not_selected)));
			launchAppsSwipeDown.setOnPreferenceClickListener(clickPref);
			toggleSwipeDown.setSummary(toggleSwipeDown.getEntry() == null ? not_selected: toggleSwipeDown.getEntry());
			toggleSwipeDown.setOnPreferenceChangeListener(setEntryAsSummary);
			
			launchAppsSwipeUp.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_prism_swipeup_app", not_selected)));
			launchAppsSwipeUp.setOnPreferenceClickListener(clickPref);
			toggleSwipeUp.setSummary(toggleSwipeUp.getEntry() == null ? not_selected: toggleSwipeUp.getEntry());
			toggleSwipeUp.setOnPreferenceChangeListener(setEntryAsSummary);
			
			launchAppsSwipeRight.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_prism_swiperight_app", not_selected)));
			launchAppsSwipeRight.setOnPreferenceClickListener(clickPref);
			toggleSwipeRight.setSummary(toggleSwipeRight.getEntry() == null ? not_selected: toggleSwipeRight.getEntry());
			toggleSwipeRight.setOnPreferenceChangeListener(setEntryAsSummary);
			
			launchAppsSwipeLeft.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_prism_swipeleft_app", not_selected)));
			launchAppsSwipeLeft.setOnPreferenceClickListener(clickPref);
			toggleSwipeLeft.setSummary(toggleSwipeLeft.getEntry() == null ? not_selected: toggleSwipeLeft.getEntry());
			toggleSwipeLeft.setOnPreferenceChangeListener(setEntryAsSummary);

			launchAppsShake.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_prism_shake_app", not_selected)));
			launchAppsShake.setOnPreferenceClickListener(clickPref);
			toggleShake.setSummary(toggleShake.getEntry() == null ? not_selected: toggleShake.getEntry());
			toggleShake.setOnPreferenceChangeListener(setEntryAsSummary);
			
			if (appsLongPressActionPreference.getValue().equals("7"))	launchAppsLongPress.setEnabled(true);		else launchAppsLongPress.setEnabled(false);
			if (appsLongPressActionPreference.getValue().equals("8"))	toggleAppsLongPress.setEnabled(true);		else toggleAppsLongPress.setEnabled(false);
			if (swipeDownActionPreference.getValue().equals("7"))		launchAppsSwipeDown.setEnabled(true);		else launchAppsSwipeDown.setEnabled(false);
			if (swipeDownActionPreference.getValue().equals("8"))		toggleSwipeDown.setEnabled(true);			else toggleSwipeDown.setEnabled(false);
			if (swipeUpActionPreference.getValue().equals("7"))			launchAppsSwipeUp.setEnabled(true);			else launchAppsSwipeUp.setEnabled(false);
			if (swipeUpActionPreference.getValue().equals("8"))			toggleSwipeUp.setEnabled(true);				else toggleSwipeUp.setEnabled(false);
			if (swipeRightActionPreference.getValue().equals("7"))		launchAppsSwipeRight.setEnabled(true);		else launchAppsSwipeRight.setEnabled(false);
			if (swipeRightActionPreference.getValue().equals("8"))		toggleSwipeRight.setEnabled(true);			else toggleSwipeRight.setEnabled(false);
			if (swipeLeftActionPreference.getValue().equals("7"))		launchAppsSwipeLeft.setEnabled(true);		else launchAppsSwipeLeft.setEnabled(false);
			if (swipeLeftActionPreference.getValue().equals("8"))		toggleSwipeLeft.setEnabled(true);			else toggleSwipeLeft.setEnabled(false);
			if (shakeActionPreference.getValue().equals("7"))			launchAppsShake.setEnabled(true);			else launchAppsShake.setEnabled(false);
			if (shakeActionPreference.getValue().equals("8"))			toggleShake.setEnabled(true);				else toggleShake.setEnabled(false);
			
			appsLongPressActionPreference.setOnPreferenceChangeListener(chooseAction);
			
			List<CharSequence> entriesCS = new ArrayList<CharSequence>(Arrays.asList(appsLongPressActionPreference.getEntries()));
			entriesCS.remove(5);
			CharSequence[] entries = entriesCS.toArray(new CharSequence[entriesCS.size()]);

			List<CharSequence> entryValsCS = new ArrayList<CharSequence>(Arrays.asList(appsLongPressActionPreference.getEntryValues()));
			entryValsCS.remove(5);
			CharSequence[] entryVals = entryValsCS.toArray(new CharSequence[entryValsCS.size()]);
			
			appsLongPressActionPreference.setEntries(entries);
			appsLongPressActionPreference.setEntryValues(entryVals);
			
			swipeDownActionPreference.setOnPreferenceChangeListener(chooseAction);
			swipeUpActionPreference.setOnPreferenceChangeListener(chooseAction);
			swipeRightActionPreference.setOnPreferenceChangeListener(chooseAction);
			swipeLeftActionPreference.setOnPreferenceChangeListener(chooseAction);
			shakeActionPreference.setOnPreferenceChangeListener(chooseAction);
			blinkFeedIconPreference.setOnPreferenceChangeListener(toggleBF);
		} else if (xmlResId == R.xml.mprefs_message) {
			this.rebootType = 2;
		} else if (xmlResId == R.xml.mprefs_controls) {
			Preference.OnPreferenceClickListener clickPref = new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					PreferenceCategory senseControlsBackCat = (PreferenceCategory) findPreference("pref_key_controls_back");
					PreferenceCategory senseControlsHomeCat = (PreferenceCategory) findPreference("pref_key_controls_home");
					PreferenceCategory wiredHeadsetCat = (PreferenceCategory) findPreference("pref_key_controls_wiredheadset");
					PreferenceCategory btHeadsetCat = (PreferenceCategory) findPreference("pref_key_controls_btheadset");
					
					if (senseControlsBackCat.findPreference(preference.getKey()) != null)
						makeDynamicPref(senseControlsBackCat, preference);
					else if (senseControlsHomeCat.findPreference(preference.getKey()) != null)
						makeDynamicPref(senseControlsHomeCat, preference);
					else if (wiredHeadsetCat.findPreference(preference.getKey()) != null)
						makeDynamicPref(wiredHeadsetCat, preference);
					else if (btHeadsetCat.findPreference(preference.getKey()) != null)
						makeDynamicPref(btHeadsetCat, preference);
					
					return true;
				}
			};
			
			Preference.OnPreferenceChangeListener chooseAction = new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Preference launchApps = null;
					Preference toggleSettings = null;
					
					if (preference.equals(findPreference("pref_key_controls_backlongpressaction"))) {
						launchApps = findPreference("pref_key_controls_backlongpress_app");
						toggleSettings = findPreference("pref_key_controls_backlongpress_toggle");
					}

					if (preference.equals(findPreference("pref_key_controls_homeassistaction"))) {
						launchApps = findPreference("pref_key_controls_homeassist_app");
						toggleSettings = findPreference("pref_key_controls_homeassist_toggle");
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
						((ListPreferenceEx)toggleSettings).show();
					} else toggleSettings.setEnabled(false);
					
					if (newValue.equals("12")) {
						Helpers.shortcutDlgStock = new AppShortcutAddDialog(getActivity(), preference.getKey() + "_shortcut");
						Helpers.shortcutDlgStock.setTitle(preference.getTitle());
						Helpers.shortcutDlgStock.setIcon(preference.getIcon());
						Helpers.shortcutDlgStock.show();
					}
					
					return true;
				}
			};
			
			Preference vol2wakePref = findPreference("pref_key_controls_vol2wake");
			//ListPreference voldownPreference = (ListPreference) findPreference("pref_key_controls_camdownaction");
			//ListPreference volupPreference = (ListPreference) findPreference("pref_key_controls_camupaction");
			ListPreference backLongPressActionPreference = (ListPreference) findPreference("pref_key_controls_backlongpressaction");
			ListPreference homeAssistActionPreference = (ListPreference) findPreference("pref_key_controls_homeassistaction");
			final ListPreference wiredHeadsetConnectedActionPreference = (ListPreference) findPreference("pref_key_controls_wiredheadsetonaction");
			final ListPreference wiredHeadsetDisconnectedActionPreference = (ListPreference) findPreference("pref_key_controls_wiredheadsetoffaction");
			final ListPreference btHeadsetConnectedActionPreference = (ListPreference) findPreference("pref_key_controls_btheadsetonaction");
			final ListPreference btHeadsetDisconnectedActionPreference = (ListPreference) findPreference("pref_key_controls_btheadsetoffaction");
			Preference launchAppsBackLongPress = findPreference("pref_key_controls_backlongpress_app");
			Preference launchAppsHomeAssist = findPreference("pref_key_controls_homeassist_app");
			final Preference launchAppsWiredHeadsetConnected = findPreference("pref_key_controls_wiredheadseton_app");
			final Preference launchAppsWiredHeadsetDisconnected = findPreference("pref_key_controls_wiredheadsetoff_app");
			final Preference launchAppsBtHeadsetConnected = findPreference("pref_key_controls_btheadseton_app");
			final Preference launchAppsBtHeadsetDisconnected = findPreference("pref_key_controls_btheadsetoff_app");
			ListPreferenceEx toggleBackLongPress = (ListPreferenceEx) findPreference("pref_key_controls_backlongpress_toggle");
			ListPreferenceEx toggleHomeAssist = (ListPreferenceEx) findPreference("pref_key_controls_homeassist_toggle");
			
			// Insert new option to controls listprefs
			List<CharSequence> entriesCS = new ArrayList<CharSequence>(Arrays.asList(backLongPressActionPreference.getEntries()));
			entriesCS.add(5, Helpers.l10n(getActivity(), R.string.kill_foreground));
			entriesCS.add(6, Helpers.l10n(getActivity(), R.string.open_menu));
			entriesCS.add(7, Helpers.l10n(getActivity(), R.string.open_recents));
			entriesCS.add(8, Helpers.l10n(getActivity(), R.string.switch_to_previous));
			CharSequence[] entries = entriesCS.toArray(new CharSequence[entriesCS.size()]);

			List<CharSequence> entryValsCS = new ArrayList<CharSequence>(Arrays.asList(backLongPressActionPreference.getEntryValues()));
			entryValsCS.add(5, "9");
			entryValsCS.add(6, "10");
			entryValsCS.add(7, "11");
			entryValsCS.add(8, "13");
			CharSequence[] entryVals = entryValsCS.toArray(new CharSequence[entryValsCS.size()]);
			
			backLongPressActionPreference.setEntries(entries);
			backLongPressActionPreference.setEntryValues(entryVals);
			
			entriesCS.add(entriesCS.size(), Helpers.l10n(getActivity(), R.string.quick_recents));
			entryValsCS.add(entryValsCS.size(), "15");
			homeAssistActionPreference.setEntries(entriesCS.toArray(new CharSequence[entriesCS.size()]));
			homeAssistActionPreference.setEntryValues(entryValsCS.toArray(new CharSequence[entryValsCS.size()]));

			String not_selected = Helpers.l10n(getActivity(), R.string.notselected);
			
			launchAppsBackLongPress.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_controls_backlongpress_app", not_selected)));
			launchAppsBackLongPress.setOnPreferenceClickListener(clickPref);
			toggleBackLongPress.setSummary(toggleBackLongPress.getEntry() == null ? not_selected: toggleBackLongPress.getEntry());
			toggleBackLongPress.setOnPreferenceChangeListener(setEntryAsSummary);
			
			launchAppsHomeAssist.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_controls_homeassist_app", not_selected)));
			launchAppsHomeAssist.setOnPreferenceClickListener(clickPref);
			toggleHomeAssist.setSummary(toggleHomeAssist.getEntry() == null ? not_selected: toggleHomeAssist.getEntry());
			toggleHomeAssist.setOnPreferenceChangeListener(setEntryAsSummary);
			
			launchAppsWiredHeadsetConnected.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_controls_wiredheadseton_app", not_selected)));
			launchAppsWiredHeadsetConnected.setOnPreferenceClickListener(clickPref);
			launchAppsWiredHeadsetDisconnected.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_controls_wiredheadsetoff_app", not_selected)));
			launchAppsWiredHeadsetDisconnected.setOnPreferenceClickListener(clickPref);
			
			launchAppsBtHeadsetConnected.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_controls_btheadseton_app", not_selected)));
			launchAppsBtHeadsetConnected.setOnPreferenceClickListener(clickPref);
			launchAppsBtHeadsetDisconnected.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_controls_btheadsetoff_app", not_selected)));
			launchAppsBtHeadsetDisconnected.setOnPreferenceClickListener(clickPref);
			
			launchAppsBackLongPress.setShouldDisableView(true);
			toggleBackLongPress.setShouldDisableView(true);
			launchAppsHomeAssist.setShouldDisableView(true);
			toggleHomeAssist.setShouldDisableView(true);
			
			if (backLongPressActionPreference.getValue().equals("7"))	launchAppsBackLongPress.setEnabled(true);	else launchAppsBackLongPress.setEnabled(false);
			if (backLongPressActionPreference.getValue().equals("8"))	toggleBackLongPress.setEnabled(true);		else toggleBackLongPress.setEnabled(false);
			if (homeAssistActionPreference.getValue().equals("7"))		launchAppsHomeAssist.setEnabled(true);		else launchAppsHomeAssist.setEnabled(false);
			if (homeAssistActionPreference.getValue().equals("8"))		toggleHomeAssist.setEnabled(true);			else toggleHomeAssist.setEnabled(false);
			if (wiredHeadsetConnectedActionPreference.getValue().equals("2"))		launchAppsWiredHeadsetConnected.setEnabled(true);		else launchAppsWiredHeadsetConnected.setEnabled(false);
			if (wiredHeadsetDisconnectedActionPreference.getValue().equals("2"))	launchAppsWiredHeadsetDisconnected.setEnabled(true);	else launchAppsWiredHeadsetDisconnected.setEnabled(false);
			if (btHeadsetConnectedActionPreference.getValue().equals("2"))			launchAppsBtHeadsetConnected.setEnabled(true);			else launchAppsBtHeadsetConnected.setEnabled(false);
			if (btHeadsetDisconnectedActionPreference.getValue().equals("2"))		launchAppsBtHeadsetDisconnected.setEnabled(true);		else launchAppsBtHeadsetDisconnected.setEnabled(false);

			//voldownPreference.setOnPreferenceChangeListener(camChangeListener);
			//volupPreference.setOnPreferenceChangeListener(camChangeListener);
			backLongPressActionPreference.setOnPreferenceChangeListener(chooseAction);
			homeAssistActionPreference.setOnPreferenceChangeListener(chooseAction);
			
			Preference.OnPreferenceChangeListener chooseHeadsetAction = new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Preference launchApps = null;
					
					if (preference.equals(findPreference("pref_key_controls_wiredheadsetonaction")))
						launchApps = findPreference("pref_key_controls_wiredheadseton_app");
					else if (preference.equals(findPreference("pref_key_controls_wiredheadsetoffaction")))
						launchApps = findPreference("pref_key_controls_wiredheadsetoff_app");
					if (preference.equals(findPreference("pref_key_controls_btheadsetonaction")))
						launchApps = findPreference("pref_key_controls_btheadseton_app");
					else if (preference.equals(findPreference("pref_key_controls_btheadsetoffaction")))
						launchApps = findPreference("pref_key_controls_btheadsetoff_app");
					
					if (launchApps != null)
					if (newValue.equals("2")) {
						launchApps.setEnabled(true);
						if (launchApps instanceof DynamicPreference)
							((DynamicPreference)launchApps).show();
						else
							launchApps.getOnPreferenceClickListener().onPreferenceClick(launchApps);
					} else launchApps.setEnabled(false);
					
					if (newValue.equals("3")) {
						Helpers.shortcutDlgStock = new AppShortcutAddDialog(getActivity(), preference.getKey() + "_shortcut");
						Helpers.shortcutDlgStock.setTitle(preference.getTitle());
						Helpers.shortcutDlgStock.setIcon(preference.getIcon());
						Helpers.shortcutDlgStock.show();
					}
					
					return true;
				}
			};
			
			wiredHeadsetConnectedActionPreference.setOnPreferenceChangeListener(chooseHeadsetAction);
			wiredHeadsetDisconnectedActionPreference.setOnPreferenceChangeListener(chooseHeadsetAction);
			btHeadsetConnectedActionPreference.setOnPreferenceChangeListener(chooseHeadsetAction);
			btHeadsetDisconnectedActionPreference.setOnPreferenceChangeListener(chooseHeadsetAction);
			
			vol2wakePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Helpers.initScriptHandler(((CheckBoxPreference) preference).isChecked());
					return true;
				}
			});
			
			if (!Helpers.hasRoot || !Helpers.hasRootAccess)
				Helpers.disablePref(this, "pref_key_controls_vol2wake", Helpers.l10n(getActivity(), R.string.no_root_summ));
			else if (!Helpers.hasBusyBox)
				Helpers.disablePref(this, "pref_key_controls_vol2wake", Helpers.l10n(getActivity(), R.string.no_busybox_summ));
			
			if (Helpers.isEight()) {
				PreferenceCategory assist_cat = (PreferenceCategory) findPreference("pref_key_controls_home");
				assist_cat.setTitle(Helpers.l10n(getActivity(), R.string.controls_mods_recentslongpress));
				ListPreference assist = (ListPreference) findPreference("pref_key_controls_homeassistaction");
				assist.setSummary(Helpers.l10n(getActivity(), R.string.controls_recentslongpressaction_summ));
			} else if (!Helpers.isDesire816())
				Helpers.removePref(this, "pref_key_controls_smallsoftkeys", "pref_key_controls");
		} else if (xmlResId == R.xml.mprefs_other) {
			Helpers.disablePref(this, "pref_key_other_fleetingglance", Helpers.l10n(getActivity(), R.string.coming_soon) + " :)");
			
			if (!Helpers.isM7()) {
				Helpers.removePref(this, "pref_key_other_keyslight", "pref_key_other");
				Helpers.removePref(this, "pref_key_other_keyslight_auto", "pref_key_other");
			} else if (!Helpers.hasRoot || !Helpers.hasRootAccess) {
				Helpers.disablePref(this, "pref_key_other_apm", Helpers.l10n(getActivity(), R.string.no_root_summ));
				Helpers.disablePref(this, "pref_key_other_keyslight", Helpers.l10n(getActivity(), R.string.no_root_summ));
				Helpers.disablePref(this, "pref_key_other_keyslight_auto", Helpers.l10n(getActivity(), R.string.no_root_summ));
			} else if (!Helpers.hasBusyBox) {
				Helpers.disablePref(this, "pref_key_other_keyslight", Helpers.l10n(getActivity(), R.string.no_busybox_summ));
				Helpers.disablePref(this, "pref_key_other_keyslight_auto", Helpers.l10n(getActivity(), R.string.no_busybox_summ));
			}
			
			if (Helpers.isSense7()) {
				Helpers.removePref(this, "pref_key_other_musicchannel", "pref_key_other");
				Helpers.removePref(this, "pref_key_other_nochargerwarn", "pref_various_mods_notifications");
			} else {
				Helpers.removePref(this, "pref_key_other_beatsnotif", "pref_various_mods_notifications");
			}
			
			ListPreference.OnPreferenceChangeListener applyButtonsLight = new ListPreference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if (!(new File("/sys/class/leds/button-backlight/currents")).isFile()) {
						Toast.makeText(getActivity(), Helpers.l10n(getActivity(), R.string.no_currents), Toast.LENGTH_LONG).show();
						return false;
					} else
						return Helpers.setButtonBacklightTo(getActivity(), Integer.parseInt((String)newValue), true);
				}
			};
			
			ListPreference keysLightPreference = (ListPreference) findPreference("pref_key_other_keyslight");
			if (keysLightPreference != null) keysLightPreference.setOnPreferenceChangeListener(applyButtonsLight);
			
			Preference extremePowerSaverPreference = (Preference) findPreference("pref_key_other_extremepower");
			extremePowerSaverPreference.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent subActIntent = new Intent(getActivity(), MSubActivity.class);
					subActIntent.putExtra("pref_section_name", (String)preference.getTitle());
					subActIntent.putExtra("pref_section_xml", R.xml.mdummy_eps);
					getActivity().startActivity(subActIntent);
					return true;
				}
			});
			
			Preference fleetingGlancePreference = (Preference) findPreference("pref_key_other_fleetingglance");
			fleetingGlancePreference.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent subActIntent = new Intent(getActivity(), MSubActivity.class);
					subActIntent.putExtra("pref_section_name", (String)preference.getTitle());
					subActIntent.putExtra("pref_section_xml", R.xml.mprefs_fleetingglance);
					getActivity().startActivity(subActIntent);
					return true;
				}
			});
			
			CheckBoxPreference textMagnifierPreference = (CheckBoxPreference) findPreference("pref_key_other_textmagnifier");
			if (textMagnifierPreference != null) {
				if (Settings.System.getInt(getActivity().getContentResolver(), "htc_magnifier_setting", 0) == 1)
					textMagnifierPreference.setChecked(true);
				else
					textMagnifierPreference.setChecked(false);
				
				textMagnifierPreference.setOnPreferenceChangeListener(new CheckBoxPreference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						try {
							int val = 0;
							if ((Boolean)newValue) val = 1;
							Settings.System.putInt(getActivity().getContentResolver(), "htc_magnifier_setting", val);
							return true;
						} catch (Exception e) {
							e.printStackTrace();
							return false;
						}
					}
				});
			}
		} else if (xmlResId == R.xml.mprefs_wakegest) {
			this.menuType = 1;
			
			final ListPreference swipeRightActionPreference = (ListPreference) findPreference("pref_key_wakegest_swiperight");
			final ListPreference swipeleftActionPreference = (ListPreference) findPreference("pref_key_wakegest_swipeleft");
			final ListPreference swipeUpActionPreference = (ListPreference) findPreference("pref_key_wakegest_swipeup");
			final ListPreference swipeDownActionPreference = (ListPreference) findPreference("pref_key_wakegest_swipedown");
			final ListPreference doubleTapActionPreference = (ListPreference) findPreference("pref_key_wakegest_dt2w");
			final ListPreference logoPressActionPreference = (ListPreference) findPreference("pref_key_wakegest_logo2wake");
			
			if (Helpers.isEight()) {
				logoPressActionPreference.setTitle(Helpers.l10n(getActivity(), R.string.wakegestures_volume_title));
				logoPressActionPreference.setSummary(Helpers.l10n(getActivity(), R.string.wakegestures_volume_summ));
				if (!Helpers.isWakeGestures()) {
					List<String> newEntries = new ArrayList<String>();
					TypedArray ids = getResources().obtainTypedArray(R.array.wakegest_m8stock_actions);
					for (int i = 0; i < ids.length(); i++) {
						int id = ids.getResourceId(i, 0);
						if (id != 0)
							newEntries.add(Helpers.l10n(getActivity(), id));
						else
							newEntries.add("???");
					}
					ids.recycle();
					CharSequence[] wakegest_m8stock_actions_l10n = newEntries.toArray(new CharSequence[newEntries.size()]);
					
					swipeRightActionPreference.setEntries(wakegest_m8stock_actions_l10n);
					swipeRightActionPreference.setEntryValues(R.array.wakegest_m8stock_actions_val);
					swipeleftActionPreference.setEntries(wakegest_m8stock_actions_l10n);
					swipeleftActionPreference.setEntryValues(R.array.wakegest_m8stock_actions_val);
					swipeUpActionPreference.setEntries(wakegest_m8stock_actions_l10n);
					swipeUpActionPreference.setEntryValues(R.array.wakegest_m8stock_actions_val);
					swipeDownActionPreference.setEntries(wakegest_m8stock_actions_l10n);
					swipeDownActionPreference.setEntryValues(R.array.wakegest_m8stock_actions_val);
					doubleTapActionPreference.setEntries(wakegest_m8stock_actions_l10n);
					doubleTapActionPreference.setEntryValues(R.array.wakegest_m8stock_actions_val);
					logoPressActionPreference.setEntries(wakegest_m8stock_actions_l10n);
					logoPressActionPreference.setEntryValues(R.array.wakegest_m8stock_actions_val);
				}
			}
			
			Preference launchAppsSwipeRight = findPreference("pref_key_wakegest_swiperight_app");
			Preference launchAppsSwipeLeft = findPreference("pref_key_wakegest_swipeleft_app");
			Preference launchAppsSwipeUp = findPreference("pref_key_wakegest_swipeup_app");
			Preference launchAppsSwipeDown = findPreference("pref_key_wakegest_swipedown_app");
			Preference launchAppsDoubleTap = findPreference("pref_key_wakegest_dt2w_app");
			Preference launchAppsLogoPress = findPreference("pref_key_wakegest_logo2wake_app");
			
			final Activity prefAct = getActivity();
			Preference.OnPreferenceChangeListener chooseAction = new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Preference launchApps = null;
					
					if (preference.equals(swipeRightActionPreference)) launchApps = findPreference("pref_key_wakegest_swiperight_app");
					if (preference.equals(swipeleftActionPreference)) launchApps = findPreference("pref_key_wakegest_swipeleft_app");
					if (preference.equals(swipeUpActionPreference)) launchApps = findPreference("pref_key_wakegest_swipeup_app");
					if (preference.equals(swipeDownActionPreference)) launchApps = findPreference("pref_key_wakegest_swipedown_app");
					if (preference.equals(doubleTapActionPreference)) launchApps = findPreference("pref_key_wakegest_dt2w_app");
					if (preference.equals(logoPressActionPreference)) launchApps = findPreference("pref_key_wakegest_logo2wake_app");
					
					if (launchApps != null)
					if (newValue.equals("10")) {
						launchApps.setEnabled(true);
						if (launchApps instanceof DynamicPreference)
							((DynamicPreference)launchApps).show();
						else
							launchApps.getOnPreferenceClickListener().onPreferenceClick(launchApps);
					} else launchApps.setEnabled(false);
					
					if (newValue.equals("14")) {
						Helpers.shortcutDlgStock = new AppShortcutAddDialog(prefAct, preference.getKey() + "_shortcut");
						Helpers.shortcutDlgStock.setTitle(preference.getTitle());
						Helpers.shortcutDlgStock.setIcon(preference.getIcon());
						Helpers.shortcutDlgStock.show();
					}
					
					return true;
				}
			};
			
			if (swipeRightActionPreference.getValue().equals("10"))	launchAppsSwipeRight.setEnabled(true);	else launchAppsSwipeRight.setEnabled(false);
			if (swipeleftActionPreference.getValue().equals("10"))	launchAppsSwipeLeft.setEnabled(true);	else launchAppsSwipeLeft.setEnabled(false);
			if (swipeUpActionPreference.getValue().equals("10"))	launchAppsSwipeUp.setEnabled(true);		else launchAppsSwipeUp.setEnabled(false);
			if (swipeDownActionPreference.getValue().equals("10"))	launchAppsSwipeDown.setEnabled(true);	else launchAppsSwipeDown.setEnabled(false);
			if (doubleTapActionPreference.getValue().equals("10"))	launchAppsDoubleTap.setEnabled(true);	else launchAppsDoubleTap.setEnabled(false);
			if (logoPressActionPreference.getValue().equals("10"))	launchAppsLogoPress.setEnabled(true);	else launchAppsLogoPress.setEnabled(false);
			
			swipeRightActionPreference.setOnPreferenceChangeListener(chooseAction);
			swipeleftActionPreference.setOnPreferenceChangeListener(chooseAction);
			swipeUpActionPreference.setOnPreferenceChangeListener(chooseAction);
			swipeDownActionPreference.setOnPreferenceChangeListener(chooseAction);
			doubleTapActionPreference.setOnPreferenceChangeListener(chooseAction);
			logoPressActionPreference.setOnPreferenceChangeListener(chooseAction);
			
			String not_selected = Helpers.l10n(getActivity(), R.string.notselected);
			Preference.OnPreferenceClickListener clickPref = new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					PreferenceScreen gesturesCat = (PreferenceScreen) findPreference("pref_key_wakegest");
					makeDynamicPref(gesturesCat, preference);
					return true;
				}
			};
			
			launchAppsSwipeRight.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_wakegest_swiperight_app", not_selected)));
			launchAppsSwipeRight.setOnPreferenceClickListener(clickPref);
			launchAppsSwipeLeft.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_wakegest_swipeleft_app", not_selected)));
			launchAppsSwipeLeft.setOnPreferenceClickListener(clickPref);
			launchAppsSwipeUp.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_wakegest_swipeup_app", not_selected)));
			launchAppsSwipeUp.setOnPreferenceClickListener(clickPref);
			launchAppsSwipeDown.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_wakegest_swipedown_app", not_selected)));
			launchAppsSwipeDown.setOnPreferenceClickListener(clickPref);
			launchAppsDoubleTap.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_wakegest_dt2w_app", not_selected)));
			launchAppsDoubleTap.setOnPreferenceClickListener(clickPref);
			launchAppsLogoPress.setSummary(Helpers.getAppName(getActivity(), Helpers.prefs.getString("pref_key_wakegest_logo2wake_app", not_selected)));
			launchAppsLogoPress.setOnPreferenceClickListener(clickPref);
			
			if (mThemeBackground == 2)
				prefListView.setDivider(getResources().getDrawable(R.drawable.inset_list_divider_dark));
			else
				prefListView.setDivider(getResources().getDrawable(R.drawable.inset_list_divider));
			prefListView.setDividerHeight(2);
			prefListView.setFooterDividersEnabled(false);
			
			themeHint = (TextView)getActivity().findViewById(R.id.themehint);
			themeHint.setText(Helpers.l10n(getActivity(), R.string.wakegestures_hint));
			
			if (Helpers.isButterflyS()) {
				if (logoPressActionPreference != null) ((PreferenceScreen)findPreference("pref_key_wakegest")).removePreference(logoPressActionPreference);
				if (launchAppsLogoPress != null) ((PreferenceScreen)findPreference("pref_key_wakegest")).removePreference(launchAppsLogoPress);
			}
		} else if (xmlResId == R.xml.mprefs_betterheadsup) {
			this.menuType = 4;
			
			TextView experimental = (TextView)getActivity().findViewById(R.id.experimental);
			experimental.setText(Helpers.l10n(getActivity(), R.string.popupnotify_experimental));
			experimental.setTextColor(getResources().getColor(android.R.color.background_light));
			
			if (mThemeBackground == 2)
				prefListView.setDivider(getResources().getDrawable(R.drawable.inset_list_divider_dark));
			else
				prefListView.setDivider(getResources().getDrawable(R.drawable.inset_list_divider));
			prefListView.setDividerHeight(2);
			prefListView.setFooterDividersEnabled(false);
			
			themeHint = (TextView)getActivity().findViewById(R.id.themehint);
			themeHint.setText(Helpers.l10n(getActivity(), R.string.betterheadsup_hint));
			
			final SwitchPreference bwlist = (SwitchPreference)findPreference("pref_key_betterheadsup_bwlist");
			bwlist.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object state) {
					updateListTypeHeadsUp((boolean)state);
					return true;
				}
			});
			updateListTypeHeadsUp(bwlist.isChecked());
			
			class DialogAdapter extends BaseAdapter {
				String[] items;
				
				DialogAdapter() {
					items = Helpers.l10n_array(getActivity(), R.array.headsup_theme_presets);
				}

				@Override
				public int getCount() {
					return items.length;
				}

				@Override
				public String getItem(int position) {
					return items[position];
				}

				@Override
				public long getItemId(int position) {
					return position;
				}

				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					TextView itemView;
					if (convertView != null)
						itemView = (TextView)convertView;
					else
						itemView = (TextView)LayoutInflater.from(getActivity()).inflate(R.layout.list_item, parent, false);
					
					itemView.setText(getItem(position));
					return itemView;
				}
			}
			
			Preference presetPref = (Preference)findPreference("pref_key_betterheadsup_theme_preset");
			presetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					DialogAdapter adapter = new DialogAdapter();
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(preference.getTitle());
					builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int theme) {
							Helpers.setThemeForElement(Helpers.prefs, "pref_key_betterheadsup_theme_background", theme + 1);
							Helpers.setThemeForElement(Helpers.prefs, "pref_key_betterheadsup_theme_primary", theme + 1);
							Helpers.setThemeForElement(Helpers.prefs, "pref_key_betterheadsup_theme_secondary", theme + 1);
							Helpers.setThemeForElement(Helpers.prefs, "pref_key_betterheadsup_theme_dismiss", theme + 1);
							Helpers.setThemeForElement(Helpers.prefs, "pref_key_betterheadsup_theme_dividers", theme + 1);
						}
					});
					builder.create().show();
					return true;
				}
			});
			
			final MultiSelectListPreferenceEx bwlistApps = (MultiSelectListPreferenceEx)findPreference("pref_key_betterheadsup_bwlist_apps");
			bwlistApps.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference paramPreference) {
					if (bwlistApps.getEntries().length == 0 || bwlistApps.getEntryValues().length == 0) {
						if (bwlistApps.getDialog() != null) bwlistApps.getDialog().dismiss();
						final ProgressDialog dialog = new ProgressDialog(getActivity());
						if (Helpers.installedAppsList == null) {
							dialog.setMessage(Helpers.l10n(getActivity(), R.string.loading_app_data));
							dialog.setCancelable(false);
							dialog.show();
						}
						
						new Thread() {
							@Override
							public void run() {
								try {
									if (Helpers.installedAppsList == null) Helpers.getInstalledApps(getActivity());
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											HashSet<String> appsList = (HashSet<String>)Helpers.prefs.getStringSet("pref_key_betterheadsup_bwlist_apps", new HashSet<String>());
											ArrayList<ArrayList<CharSequence>> entries = new ArrayList<ArrayList<CharSequence>>();
											for (AppData ad: Helpers.installedAppsList) {
												ArrayList<CharSequence> entry = new ArrayList<CharSequence>();
												entry.add(ad.label);
												entry.add(ad.pkgName);
												if (appsList.contains(ad.pkgName))
													entry.add("1");
												else
													entry.add("0");
												entries.add(entry);
											}
											
											Collections.sort(entries, new Comparator<ArrayList<CharSequence>>() {
												@Override
												public int compare(ArrayList<CharSequence> entry1, ArrayList<CharSequence> entry2) {
													return ((String)entry2.get(2)).compareTo((String)entry1.get(2));
												}
											});
											
											ArrayList<CharSequence> entryLabels = new ArrayList<CharSequence>();
											ArrayList<CharSequence> entryVals = new ArrayList<CharSequence>();
											for (ArrayList<CharSequence> entry: entries) {
												entryLabels.add(entry.get(0));
												entryVals.add(entry.get(1));
											}
											
											bwlistApps.setEntries(entryLabels.toArray(new CharSequence[entries.size()]));
											bwlistApps.setEntryValues(entryVals.toArray(new CharSequence[entryVals.size()]));
											
											if (getActivity() != null && !getActivity().isFinishing()) {
												if (dialog != null && dialog.isShowing()) dialog.dismiss();
												if (bwlistApps.getDialog() != null && bwlistApps.getDialog().isShowing()) bwlistApps.getDialog().dismiss();
											}
											bwlistApps.show();
										}
									});
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}.start();
					}
					return false;
				}
			});
			
			final MultiSelectListPreferenceEx ignoreListApps = (MultiSelectListPreferenceEx)findPreference("pref_key_betterheadsup_ignorelist_apps");
			ignoreListApps.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference paramPreference) {
					if (ignoreListApps.getEntries().length == 0 || ignoreListApps.getEntryValues().length == 0) {
						if (ignoreListApps.getDialog() != null) ignoreListApps.getDialog().dismiss();
						final ProgressDialog dialog = new ProgressDialog(getActivity());
						if (Helpers.installedAppsList == null) {
							dialog.setMessage(Helpers.l10n(getActivity(), R.string.loading_app_data));
							dialog.setCancelable(false);
							dialog.show();
						}
						
						new Thread() {
							@Override
							public void run() {
								try {
									if (Helpers.installedAppsList == null) Helpers.getInstalledApps(getActivity());
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											HashSet<String> ignoreList = (HashSet<String>)Helpers.prefs.getStringSet("pref_key_betterheadsup_ignorelist_apps", new HashSet<String>());
											ArrayList<ArrayList<CharSequence>> entries = new ArrayList<ArrayList<CharSequence>>();
											for (AppData ad: Helpers.installedAppsList) {
												ArrayList<CharSequence> entry = new ArrayList<CharSequence>();
												entry.add(ad.label);
												entry.add(ad.pkgName);
												if (ignoreList.contains(ad.pkgName))
													entry.add("1");
												else
													entry.add("0");
												entries.add(entry);
											}
											
											Collections.sort(entries, new Comparator<ArrayList<CharSequence>>() {
												@Override
												public int compare(ArrayList<CharSequence> entry1, ArrayList<CharSequence> entry2) {
													return ((String)entry2.get(2)).compareTo((String)entry1.get(2));
												}
											});
											
											ArrayList<CharSequence> entryLabels = new ArrayList<CharSequence>();
											ArrayList<CharSequence> entryVals = new ArrayList<CharSequence>();
											for (ArrayList<CharSequence> entry: entries) {
												entryLabels.add(entry.get(0));
												entryVals.add(entry.get(1));
											}
											
											ignoreListApps.setEntries(entryLabels.toArray(new CharSequence[entries.size()]));
											ignoreListApps.setEntryValues(entryVals.toArray(new CharSequence[entryVals.size()]));
											
											if (getActivity() != null && !getActivity().isFinishing()) {
												if (dialog != null && dialog.isShowing()) dialog.dismiss();
												if (ignoreListApps.getDialog() != null && ignoreListApps.getDialog().isShowing()) ignoreListApps.getDialog().dismiss();
											}
											ignoreListApps.show();
										}
									});
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}.start();
					}
					return false;
				}
			});
		} else if (xmlResId == R.xml.mprefs_fleetingglance) {
			this.menuType = 5;
			
			TextView experimental = (TextView)getActivity().findViewById(R.id.experimental);
			experimental.setText(Helpers.l10n(getActivity(), R.string.popupnotify_experimental));
			experimental.setTextColor(getResources().getColor(android.R.color.background_light));
			
			FrameLayout experimentalFrame = (FrameLayout)getActivity().findViewById(R.id.experimentalFrame);
			experimentalFrame.setVisibility(View.VISIBLE);
			
			if (mThemeBackground == 2)
				prefListView.setDivider(getResources().getDrawable(R.drawable.inset_list_divider_dark));
			else
				prefListView.setDivider(getResources().getDrawable(R.drawable.inset_list_divider));
			prefListView.setDividerHeight(2);
			prefListView.setFooterDividersEnabled(false);
			
			themeHint = (TextView)getActivity().findViewById(R.id.themehint);
			themeHint.setText(Helpers.l10n(getActivity(), R.string.fleetingglance_hint));
			
			SensorManager mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
			Sensor mSensorSigMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
			if (mSensorSigMotion == null) {
				CheckBoxPreference fleetingGlanceSigMotionPreference = (CheckBoxPreference) findPreference("pref_key_fleetingglance_sigmotion");
				fleetingGlanceSigMotionPreference.setEnabled(false);
				fleetingGlanceSigMotionPreference.setSummary(Helpers.l10n(getActivity(), R.string.fleetingglance_no_sensor));
			}

			ArrayList<Sensor> sensors = new ArrayList<Sensor>();
			try {
				Class<?> ssm = Class.forName("android.hardware.SystemSensorManager");
				Method m = ssm.getDeclaredMethod("getFullSensorList");
				m.setAccessible(true);
				sensors = (ArrayList<Sensor>)m.invoke(mSensorManager);
			} catch (Exception e) {
				e.printStackTrace();
			}
			boolean hasPickUpSensor = false;
			for (Sensor sensor: sensors) if (sensor.getType() == 25) {
				hasPickUpSensor = true;
				break;
			}
			if (!hasPickUpSensor) {
				CheckBoxPreference fleetingGlancePickUpPreference = (CheckBoxPreference) findPreference("pref_key_fleetingglance_pickup");
				fleetingGlancePickUpPreference.setEnabled(false);
				fleetingGlancePickUpPreference.setSummary(Helpers.l10n(getActivity(), R.string.fleetingglance_no_sensor));
			}
		} else if (xmlResId == R.xml.mdummy_eps) {
			this.menuType = 2;
			
			TextView hint = (TextView)getActivity().findViewById(R.id.hint);
			hint.setText(Helpers.l10n(getActivity(), R.string.various_extremepower_hint));
			
			contentsView = (LinearLayout)getActivity().findViewById(R.id.contents);
			
			themeHint = (TextView)getActivity().findViewById(R.id.themehint);
			themeHint.setText(Helpers.l10n(getActivity(), R.string.various_eps_hint));
		}
	}
	
	public void makeDynamicPref(final PreferenceGroup group, Preference clickedPreference) {
		final DynamicPreference dp = new DynamicPreference(group.getContext());
		dp.setTitle(clickedPreference.getTitle());
		dp.setIcon(clickedPreference.getIcon());
		dp.setDialogTitle(clickedPreference.getTitle());
		dp.setSummary(clickedPreference.getSummary());
		dp.setOrder(clickedPreference.getOrder());
		dp.setKey(clickedPreference.getKey());
		dp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(Helpers.getAppName(getActivity(), (String)newValue));
				return true;
			}
		});
		
		group.removePreference(clickedPreference);
		group.addPreference(dp);
		
		if (Helpers.launchableAppsList == null) {
			final ProgressDialog dialog = new ProgressDialog(getActivity());
			dialog.setMessage(Helpers.l10n(getActivity(), R.string.loading_app_data));
			dialog.setCancelable(false);
			dialog.show();
			
			new Thread() {
				@Override
				public void run() {
					try {
						Helpers.getLaunchableApps(getActivity());
						getActivity().runOnUiThread(new Runnable(){
							@Override
							public void run() {
								dp.show();
							}
						});
						// Nasty hack! Wait for icons to load.
						Thread.sleep(1000);
						getActivity().runOnUiThread(new Runnable(){
							@Override
							public void run() {
								dialog.dismiss();
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		} else dp.show();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (xmlResId == R.xml.mprefs_wakegest)
			return inflater.inflate(R.layout.mfragment_wake_gestures, container, false);
		else if (xmlResId == R.xml.mprefs_betterheadsup || xmlResId == R.xml.mprefs_fleetingglance)
			return inflater.inflate(R.layout.mfragment_with_listview, container, false);
		else if (xmlResId == R.xml.mdummy_eps)
			return inflater.inflate(R.layout.mfragment_eps_remap, container, false);
		else
			return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	private void updateListTypeHeadsUp(boolean state) {
		MultiSelectListPreference bwlistApps = (MultiSelectListPreference)findPreference("pref_key_betterheadsup_bwlist_apps");
		Activity act = getActivity();
		if (state) {
			bwlistApps.setTitle(Helpers.l10n(act, R.string.various_betterheadsup_bwlist_white_title));
			bwlistApps.setDialogTitle(Helpers.l10n(act, R.string.various_betterheadsup_bwlist_white_title));
			bwlistApps.setSummary(Helpers.l10n(act, R.string.various_betterheadsup_bwlist_white_summ));
		} else {
			bwlistApps.setTitle(Helpers.l10n(act, R.string.various_betterheadsup_bwlist_black_title));
			bwlistApps.setDialogTitle(Helpers.l10n(act, R.string.various_betterheadsup_bwlist_black_title));
			bwlistApps.setSummary(Helpers.l10n(act, R.string.various_betterheadsup_bwlist_black_summ));
		}
	}
	
	private void applyIconTheme(Drawable icon) {
		ColorFilter cf = GlobalActions.createColorFilter(false);
		icon.clearColorFilter();
		if (cf != null) icon.setColorFilter(cf);
	}
	
	private void applyIconThemes() {
		Preference beats = findPreference("pref_key_cb_beats");
		Activity act = getActivity();
		
		Drawable beats_icon = act.getResources().getDrawable(R.drawable.stat_sys_beats);
		beats.setIcon(Helpers.dropIconShadow(act, beats_icon));
		
		Preference wifi = findPreference("pref_key_cb_wifi");
		Drawable wifi_icon = act.getResources().getDrawable(R.drawable.b_stat_sys_wifi_signal_4);
		applyIconTheme(wifi_icon);
		wifi.setIcon(Helpers.dropIconShadow(act, wifi_icon));
		
		if (Helpers.isEight()) {
			beats.setTitle(beats.getTitle().toString().replace("Beats", "Boomsound"));
			beats.setSummary(beats.getSummary().toString().replace("Beats", "Boomsound"));
			Drawable boomsound_icon = act.getResources().getDrawable(R.drawable.stat_sys_boomsound);
			applyIconTheme(boomsound_icon);
			beats.setIcon(Helpers.dropIconShadow(act, boomsound_icon));
		}
		
		Preference signal = findPreference("pref_key_cb_signal");
		Drawable signal_icon = getResources().getDrawable(R.drawable.cb_signal_preview);
		applyIconTheme(signal_icon);
		signal.setIcon(Helpers.dropIconShadow(act, signal_icon));
		
		Preference data = findPreference("pref_key_cb_data");
		Drawable data_icon = getResources().getDrawable(R.drawable.cb_data_preview);
		applyIconTheme(data_icon);
		data.setIcon(Helpers.dropIconShadow(act, data_icon));
		
		Preference headphone = findPreference("pref_key_cb_headphone");
		Drawable headphone_icon = getResources().getDrawable(R.drawable.stat_sys_headphones);
		applyIconTheme(headphone_icon);
		headphone.setIcon(Helpers.dropIconShadow(act, headphone_icon));
		
		Preference profile = findPreference("pref_key_cb_profile");
		Drawable profile_icon = act.getResources().getDrawable(R.drawable.stat_sys_ringer_silent);
		applyIconTheme(profile_icon);
		profile.setIcon(Helpers.dropIconShadow(act, profile_icon));
		
		Preference alarm = findPreference("pref_key_cb_alarm");
		Drawable alarm_icon = act.getResources().getDrawable(R.drawable.stat_notify_alarm);
		applyIconTheme(alarm_icon);
		alarm.setIcon(Helpers.dropIconShadow(act, alarm_icon));
		
		Preference sync = findPreference("pref_key_cb_sync");
		Drawable sync_icon = act.getResources().getDrawable(R.drawable.stat_sys_sync);
		applyIconTheme(sync_icon);
		sync.setIcon(Helpers.dropIconShadow(act, sync_icon));
		
		Preference gps = findPreference("pref_key_cb_gps");
		Drawable gps_icon = act.getResources().getDrawable(R.drawable.stat_sys_gps_acquiring);
		applyIconTheme(gps_icon);
		gps.setIcon(Helpers.dropIconShadow(act, gps_icon));
		
		Preference bt = findPreference("pref_key_cb_bt");
		Drawable bt_icon = act.getResources().getDrawable(R.drawable.stat_sys_data_bluetooth_connected);
		applyIconTheme(bt_icon);
		bt.setIcon(Helpers.dropIconShadow(act, bt_icon));
		
		Preference screenshot = findPreference("pref_key_cb_screenshot");
		Drawable screenshot_icon = act.getResources().getDrawable(R.drawable.stat_notify_image);
		applyIconTheme(screenshot_icon);
		screenshot.setIcon(Helpers.dropIconShadow(act, screenshot_icon));
		
		Preference usb = findPreference("pref_key_cb_usb");
		Drawable usb_icon_orig = act.getResources().getDrawable(R.drawable.stat_sys_data_usb);
		ScaleDrawable usb_icon = new ScaleDrawable(usb_icon_orig, Gravity.CENTER, 1.0f, 1.0f);
		usb_icon.setLevel(8000);
		applyIconTheme(usb_icon);
		usb.setIcon(Helpers.dropIconShadow(act, usb_icon));
		
		Preference powersave = findPreference("pref_key_cb_powersave");
		Drawable powersave_icon = act.getResources().getDrawable(R.drawable.stat_notify_power_saver);
		applyIconTheme(powersave_icon);
		powersave.setIcon(Helpers.dropIconShadow(act, powersave_icon));
		
		Preference nfc = findPreference("pref_key_cb_nfc");
		Drawable nfc_icon = act.getResources().getDrawable(R.drawable.stat_sys_nfc_vzw);
		applyIconTheme(nfc_icon);
		nfc.setIcon(Helpers.dropIconShadow(act, nfc_icon));
		
		Preference dnd = findPreference("pref_key_cb_dnd");
		Drawable dnd_icon = act.getResources().getDrawable(R.drawable.stat_notify_dnd);
		applyIconTheme(dnd_icon);
		dnd.setIcon(Helpers.dropIconShadow(act, dnd_icon));
		
		Preference phone = findPreference("pref_key_cb_phone");
		Drawable phone_icon = act.getResources().getDrawable(R.drawable.stat_sys_phone_call);
		applyIconTheme(phone_icon);
		phone.setIcon(Helpers.dropIconShadow(act, phone_icon));
		
		Preference tv = findPreference("pref_key_cb_tv");
		Drawable tv_icon = act.getResources().getDrawable(R.drawable.stat_notify_tv);
		applyIconTheme(tv_icon);
		tv.setIcon(Helpers.dropIconShadow(act, tv_icon));
	}
}

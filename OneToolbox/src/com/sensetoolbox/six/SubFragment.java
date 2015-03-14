package com.sensetoolbox.six;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.htc.app.HtcProgressDialog;
import com.htc.preference.HtcCheckBoxPreference;
import com.htc.preference.HtcListPreference;
import com.htc.preference.HtcPreference;
import com.htc.preference.HtcPreferenceCategory;
import com.htc.preference.HtcPreferenceScreen;
import com.htc.preference.HtcPreference.OnPreferenceChangeListener;
import com.htc.preference.HtcPreference.OnPreferenceClickListener;
import com.htc.widget.HtcListView;
import com.sensetoolbox.six.utils.ApkInstaller;
import com.sensetoolbox.six.utils.AppShortcutAddDialog;
import com.sensetoolbox.six.utils.ColorPreference;
import com.sensetoolbox.six.utils.DynamicPreference;
import com.sensetoolbox.six.utils.Helpers;
import com.sensetoolbox.six.utils.HtcListPreferencePlus;
import com.sensetoolbox.six.utils.HtcPreferenceFragmentExt;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SubFragment extends HtcPreferenceFragmentExt {
	private int xmlResId = 0;
	
	public SubFragment() {
		super();
		this.setRetainInstance(true);
		xmlResId = 0;
	}
	
	public SubFragment(int resId) {
		super();
		this.setRetainInstance(true);
		xmlResId = resId;
	}
	
	OnPreferenceChangeListener setEntryAsSummary = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
			((HtcListPreferencePlus)preference).setValue((String)newValue);
			preference.setSummary(((HtcListPreferencePlus)preference).getEntry());
			return false;
		}
	};
	
	private static void removePref(HtcPreferenceFragmentExt frag, String prefName, String catName) {
		if (frag.findPreference(prefName) != null) {
			HtcPreference cat = frag.findPreference(catName);
			if (cat instanceof HtcPreferenceScreen) ((HtcPreferenceScreen)cat).removePreference(frag.findPreference(prefName));
			else if (cat instanceof HtcPreferenceCategory) ((HtcPreferenceCategory)cat).removePreference(frag.findPreference(prefName));
		}
	}
	
	private static void disablePref(HtcPreferenceFragmentExt frag, String prefName, String reasonText) {
		HtcPreference pref = frag.findPreference(prefName);
		if (pref != null) {
			pref.setEnabled(false);
			pref.setSummary(reasonText);
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (xmlResId == 0) {
			getActivity().finish();
			return;
		}
		super.onActivityCreated(savedInstanceState, xmlResId);
		addPreferencesFromResource(xmlResId);
		int backResId = getResources().getIdentifier("common_app_bkg", "drawable", "com.htc");
		
		if (xmlResId == R.xml.prefs_systemui) {
			HtcPreference senseThemesPreference = (HtcPreference) findPreference("pref_key_sense_themes");
			senseThemesPreference.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(HtcPreference arg0) {
					getActivity().startActivity(new Intent(getActivity(), SenseThemes.class));
					return true;
				}
			});
			if (Helpers.isLP()) {
				findPreference("pref_key_sysui_recentappsclear").setDependency(null);
				findPreference("pref_key_sysui_recentram").setDependency(null);
				findPreference("pref_key_sysui_hqthumbs").setDependency(null);
				findPreference("pref_key_sysui_recentslongtap").setDependency(null);
				removePref(this, "pref_key_sysui_aosprecent", "pref_systemui_recent");
				removePref(this, "pref_key_sysui_footeralpha", "pref_systemui_statusbar");
				removePref(this, "pref_key_sysui_tnsb", "pref_systemui_statusbar");
				removePref(this, "pref_key_sysui_theqs", "pref_systemui_eqs");
				removePref(this, "pref_key_sysui_minorqs_notext", "pref_systemui_eqs");
				removePref(this, "pref_key_sysui_minorqs", "pref_systemui_eqs");
				removePref(this, "pref_key_sysui_noeqs", "pref_systemui_eqs");
			}
		} else if (xmlResId == R.xml.prefs_statusbar) {
			HtcPreference sunbeamInstallPref = findPreference("pref_key_cb_sunbeam");
			sunbeamInstallPref.setOnPreferenceClickListener(new HtcPreference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(HtcPreference preference) {
					ApkInstaller.installSunbeam(getActivity());
					return true;
				}
			});
			
			ColorPreference colorChanger = (ColorPreference) findPreference("pref_key_colorfilter");
			colorChanger.applyThemes();
			
			if (Helpers.isEight()) {
				HtcCheckBoxPreference beats = (HtcCheckBoxPreference) findPreference("pref_key_cb_beats");
				beats.setTitle(beats.getTitle().toString().replace("Beats", "Boomsound"));
				beats.setSummary(beats.getSummary().toString().replace("Beats", "Boomsound"));
				beats.setIcon(R.drawable.stat_sys_boomsound);
			}
		} else if (xmlResId == R.xml.prefs_prism) {
			this.rebootType = 1;
			
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
					
					if (newValue.equals("12")) {
						Helpers.shortcutDlg = new AppShortcutAddDialog(getActivity(), preference.getKey() + "_shortcut");
						Helpers.shortcutDlg.setTitle(preference.getTitle());
						Helpers.shortcutDlg.setIcon(preference.getIcon());
						Helpers.shortcutDlg.show();
					}
					
					return true;
				}
			};
			
			HtcPreference.OnPreferenceClickListener clickPref = new HtcPreference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(HtcPreference preference) {
					HtcPreferenceCategory senseGesturesCat = (HtcPreferenceCategory) findPreference("pref_key_sense_gestures");
					Context mContext = senseGesturesCat.getContext();
					
					final DynamicPreference dp = new DynamicPreference(mContext);
					dp.setTitle(preference.getTitle());
					dp.setIcon(preference.getIcon());
					dp.setDialogTitle(preference.getTitle());
					dp.setSummary(preference.getSummary());
					dp.setOrder(preference.getOrder());
					dp.setKey(preference.getKey());
					dp.setOnPreferenceChangeListener(new HtcPreference.OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
							preference.setSummary(Helpers.getAppName(getActivity(), (String)newValue));
							return true;
						}
					});
					
					senseGesturesCat.removePreference(preference);
					senseGesturesCat.addPreference(dp);
					
					if (Helpers.launchableAppsList == null) {
						final HtcProgressDialog dialog = new HtcProgressDialog(getActivity());
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
										public void run(){
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
					
					return true;
				}
			};
			
			HtcCheckBoxPreference.OnPreferenceChangeListener toggleBF = new HtcCheckBoxPreference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
					PackageManager pm = getActivity().getPackageManager();
					if ((Boolean)newValue)
						pm.setComponentEnabledSetting(new ComponentName(getActivity(), BlinkFeed.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
					else
						pm.setComponentEnabledSetting(new ComponentName(getActivity(), BlinkFeed.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					return true;
				}
			};
			
			HtcListPreference swipeDownActionPreference = (HtcListPreference) findPreference("pref_key_prism_swipedownaction");
			HtcListPreference swipeUpActionPreference = (HtcListPreference) findPreference("pref_key_prism_swipeupaction");
			HtcListPreference swipeRightActionPreference = (HtcListPreference) findPreference("pref_key_prism_swiperightaction");
			HtcListPreference swipeLeftActionPreference = (HtcListPreference) findPreference("pref_key_prism_swipeleftaction");
			HtcListPreference shakeActionPreference = (HtcListPreference) findPreference("pref_key_prism_shakeaction");
			HtcCheckBoxPreference blinkFeedIconPreference = (HtcCheckBoxPreference) findPreference("pref_key_prism_blinkfeedicon");
			
			HtcPreference launchAppsSwipeDown = findPreference("pref_key_prism_swipedown_app");
			HtcPreference launchAppsSwipeUp = findPreference("pref_key_prism_swipeup_app");
			HtcPreference launchAppsSwipeRight = findPreference("pref_key_prism_swiperight_app");
			HtcPreference launchAppsSwipeLeft = findPreference("pref_key_prism_swipeleft_app");
			HtcPreference launchAppsShake = findPreference("pref_key_prism_shake_app");
			
			HtcListPreferencePlus toggleSwipeDown = (HtcListPreferencePlus) findPreference("pref_key_prism_swipedown_toggle");
			HtcListPreferencePlus toggleSwipeUp = (HtcListPreferencePlus) findPreference("pref_key_prism_swipeup_toggle");
			HtcListPreferencePlus toggleSwipeRight = (HtcListPreferencePlus) findPreference("pref_key_prism_swiperight_toggle");
			HtcListPreferencePlus toggleSwipeLeft = (HtcListPreferencePlus) findPreference("pref_key_prism_swipeleft_toggle");
			HtcListPreferencePlus toggleShake = (HtcListPreferencePlus) findPreference("pref_key_prism_shake_toggle");
			
			String not_selected = Helpers.l10n(getActivity(), R.string.notselected);
			
			launchAppsSwipeDown.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_prism_swipedown_app", not_selected)));
			launchAppsSwipeDown.setOnPreferenceClickListener(clickPref);
			toggleSwipeDown.setSummary(toggleSwipeDown.getEntry() == null ? not_selected: toggleSwipeDown.getEntry());
			toggleSwipeDown.setOnPreferenceChangeListener(setEntryAsSummary);
			
			launchAppsSwipeUp.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_prism_swipeup_app", not_selected)));
			launchAppsSwipeUp.setOnPreferenceClickListener(clickPref);
			toggleSwipeUp.setSummary(toggleSwipeUp.getEntry() == null ? not_selected: toggleSwipeUp.getEntry());
			toggleSwipeUp.setOnPreferenceChangeListener(setEntryAsSummary);
			
			launchAppsSwipeRight.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_prism_swiperight_app", not_selected)));
			launchAppsSwipeRight.setOnPreferenceClickListener(clickPref);
			toggleSwipeRight.setSummary(toggleSwipeRight.getEntry() == null ? not_selected: toggleSwipeRight.getEntry());
			toggleSwipeRight.setOnPreferenceChangeListener(setEntryAsSummary);
			
			launchAppsSwipeLeft.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_prism_swipeleft_app", not_selected)));
			launchAppsSwipeLeft.setOnPreferenceClickListener(clickPref);
			toggleSwipeLeft.setSummary(toggleSwipeLeft.getEntry() == null ? not_selected: toggleSwipeLeft.getEntry());
			toggleSwipeLeft.setOnPreferenceChangeListener(setEntryAsSummary);

			launchAppsShake.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_prism_shake_app", not_selected)));
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
			if (shakeActionPreference.getValue().equals("7"))			launchAppsShake.setEnabled(true);			else launchAppsShake.setEnabled(false);
			if (shakeActionPreference.getValue().equals("8"))			toggleShake.setEnabled(true);				else toggleShake.setEnabled(false);
			
			swipeDownActionPreference.setOnPreferenceChangeListener(chooseAction);
			swipeUpActionPreference.setOnPreferenceChangeListener(chooseAction);
			swipeRightActionPreference.setOnPreferenceChangeListener(chooseAction);
			swipeLeftActionPreference.setOnPreferenceChangeListener(chooseAction);
			shakeActionPreference.setOnPreferenceChangeListener(chooseAction);
			blinkFeedIconPreference.setOnPreferenceChangeListener(toggleBF);
		} else if (xmlResId == R.xml.prefs_message) {
			this.rebootType = 2;
		} else if (xmlResId == R.xml.prefs_controls) {
			HtcPreference.OnPreferenceClickListener clickPref = new HtcPreference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(HtcPreference preference) {
					int prefCategory = 0;
					Context mContext = null;
					HtcPreferenceCategory senseControlsBackCat = (HtcPreferenceCategory) findPreference("pref_key_controls_back");
					HtcPreferenceCategory senseControlsHomeCat = (HtcPreferenceCategory) findPreference("pref_key_controls_home");
					
					if (senseControlsBackCat.findPreference(preference.getKey()) != null) {
						prefCategory = 1;
						mContext = senseControlsBackCat.getContext();
					} else if (senseControlsHomeCat.findPreference(preference.getKey()) != null) {
						prefCategory = 2;
						mContext = senseControlsHomeCat.getContext();
					} else return true;
					
					final DynamicPreference dp = new DynamicPreference(mContext);
					dp.setTitle(preference.getTitle());
					dp.setIcon(preference.getIcon());
					dp.setDialogTitle(preference.getTitle());
					dp.setSummary(preference.getSummary());
					dp.setOrder(preference.getOrder());
					dp.setKey(preference.getKey());
					dp.setOnPreferenceChangeListener(new HtcPreference.OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
							preference.setSummary(Helpers.getAppName(getActivity(), (String)newValue));
							return true;
						}
					});
					
					if (prefCategory == 1) {
						senseControlsBackCat.removePreference(preference);
						senseControlsBackCat.addPreference(dp);
					} else if (prefCategory == 2) {
						senseControlsHomeCat.removePreference(preference);
						senseControlsHomeCat.addPreference(dp);
					}
					
					if (Helpers.launchableAppsList == null) {
						final HtcProgressDialog dialog = new HtcProgressDialog(getActivity());
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
										public void run(){
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
					
					return true;
				}
			};
			
			HtcPreference.OnPreferenceChangeListener chooseAction = new HtcPreference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
					HtcPreference launchApps = null;
					HtcPreference toggleSettings = null;
					
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
						((HtcListPreferencePlus)toggleSettings).show();
					} else toggleSettings.setEnabled(false);
					
					if (newValue.equals("12")) {
						Helpers.shortcutDlg = new AppShortcutAddDialog(getActivity(), preference.getKey() + "_shortcut");
						Helpers.shortcutDlg.setTitle(preference.getTitle());
						Helpers.shortcutDlg.setIcon(preference.getIcon());
						Helpers.shortcutDlg.show();
					}
					
					return true;
				}
			};
			
			HtcPreference vol2wakePref = findPreference("pref_key_controls_vol2wake");
			//HtcListPreference voldownPreference = (HtcListPreference) findPreference("pref_key_controls_camdownaction");
			//HtcListPreference volupPreference = (HtcListPreference) findPreference("pref_key_controls_camupaction");
			HtcListPreference backLongPressActionPreference = (HtcListPreference) findPreference("pref_key_controls_backlongpressaction");
			HtcListPreference homeAssistActionPreference = (HtcListPreference) findPreference("pref_key_controls_homeassistaction");
			HtcPreference launchAppsBackLongPress = findPreference("pref_key_controls_backlongpress_app");
			HtcPreference launchAppsHomeAssist = findPreference("pref_key_controls_homeassist_app");
			HtcListPreferencePlus toggleBackLongPress = (HtcListPreferencePlus) findPreference("pref_key_controls_backlongpress_toggle");
			HtcListPreferencePlus toggleHomeAssist = (HtcListPreferencePlus) findPreference("pref_key_controls_homeassist_toggle");
			
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
			homeAssistActionPreference.setEntries(entries);
			homeAssistActionPreference.setEntryValues(entryVals);

			String not_selected = Helpers.l10n(getActivity(), R.string.notselected);
			
			launchAppsBackLongPress.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_controls_backlongpress_app", not_selected)));
			launchAppsBackLongPress.setOnPreferenceClickListener(clickPref);
			toggleBackLongPress.setSummary(toggleBackLongPress.getEntry() == null ? not_selected: toggleBackLongPress.getEntry());
			toggleBackLongPress.setOnPreferenceChangeListener(setEntryAsSummary);
			
			launchAppsHomeAssist.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_controls_homeassist_app", not_selected)));
			launchAppsHomeAssist.setOnPreferenceClickListener(clickPref);
			toggleHomeAssist.setSummary(toggleHomeAssist.getEntry() == null ? not_selected: toggleHomeAssist.getEntry());
			toggleHomeAssist.setOnPreferenceChangeListener(setEntryAsSummary);
			
			if (backLongPressActionPreference.getValue().equals("7"))	launchAppsBackLongPress.setEnabled(true);	else launchAppsBackLongPress.setEnabled(false);
			if (backLongPressActionPreference.getValue().equals("8"))	toggleBackLongPress.setEnabled(true);		else toggleBackLongPress.setEnabled(false);
			if (homeAssistActionPreference.getValue().equals("7"))		launchAppsHomeAssist.setEnabled(true);		else launchAppsHomeAssist.setEnabled(false);
			if (homeAssistActionPreference.getValue().equals("8"))		toggleHomeAssist.setEnabled(true);			else toggleHomeAssist.setEnabled(false);

			//voldownPreference.setOnPreferenceChangeListener(camChangeListener);
			//volupPreference.setOnPreferenceChangeListener(camChangeListener);
			backLongPressActionPreference.setOnPreferenceChangeListener(chooseAction);
			homeAssistActionPreference.setOnPreferenceChangeListener(chooseAction);
			vol2wakePref.setOnPreferenceClickListener(new HtcPreference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(HtcPreference preference) {
					Helpers.initScriptHandler(((HtcCheckBoxPreference) preference).isChecked());
					return true;
				}
			});
			
			if (!Helpers.hasRoot)
				disablePref(this, "pref_key_controls_vol2wake", Helpers.l10n(getActivity(), R.string.no_root_summ));
			else if (!Helpers.hasBusyBox)
				disablePref(this, "pref_key_controls_vol2wake", Helpers.l10n(getActivity(), R.string.no_busybox_summ));
			
			if (Helpers.isEight()) {
				HtcPreferenceCategory assist_cat = (HtcPreferenceCategory) findPreference("pref_key_controls_home");
				assist_cat.setTitle(Helpers.l10n(getActivity(), R.string.controls_mods_recentslongpress));
				HtcListPreference assist = (HtcListPreference) findPreference("pref_key_controls_homeassistaction");
				assist.setSummary(Helpers.l10n(getActivity(), R.string.controls_recentslongpressaction_summ));
			} else if (!Helpers.isDesire816())
				removePref(this, "pref_key_controls_smallsoftkeys", "pref_key_controls");
			
			if (Helpers.isLP()) {
				removePref(this, "pref_key_controls_extendedpanel_left", "pref_key_controls");
				removePref(this, "pref_key_controls_extendedpanel_right", "pref_key_controls");
				removePref(this, "pref_key_controls_extendedpanel", "pref_key_controls");
			}
		} else if (xmlResId == R.xml.prefs_other) {
			if (Helpers.isNotM7()) {
				removePref(this, "pref_key_other_keyslight", "pref_key_other");
				removePref(this, "pref_key_other_keyslight_auto", "pref_key_other");
			} else if (!Helpers.hasRoot){
				disablePref(this, "pref_key_other_keyslight", Helpers.l10n(getActivity(), R.string.no_root_summ));
				disablePref(this, "pref_key_other_keyslight_auto", Helpers.l10n(getActivity(), R.string.no_root_summ));
			} else if (!Helpers.hasBusyBox){
				disablePref(this, "pref_key_other_keyslight", Helpers.l10n(getActivity(), R.string.no_busybox_summ));
				disablePref(this, "pref_key_other_keyslight_auto", Helpers.l10n(getActivity(), R.string.no_busybox_summ));
			}
			
			if (Helpers.isLP()) {
				HtcListPreference scrOffPref = (HtcListPreference)findPreference("pref_key_other_screenoff");
				scrOffPref.setEntries(Helpers.l10n_array(getActivity(), R.array.various_screenoff_lp));
				
				removePref(this, "pref_key_other_psscrolltotop", "pref_key_other");
				removePref(this, "pref_key_other_vzwnotif", "pref_various_mods_notifications");
				removePref(this, "pref_key_other_ledtimeout", "pref_various_mods_notifications");
			}
			
			HtcListPreference.OnPreferenceChangeListener applyButtonsLight = new HtcListPreference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
					if (!(new File("/sys/class/leds/button-backlight/currents")).isFile()) {
						Toast.makeText(getActivity(), Helpers.l10n(getActivity(), R.string.no_currents), Toast.LENGTH_LONG).show();
						return false;
					} else
						return Helpers.setButtonBacklightTo(Integer.parseInt((String)newValue), true);
				}
			};
			
			HtcListPreference keysLightPreference = (HtcListPreference) findPreference("pref_key_other_keyslight");
			if (keysLightPreference != null) keysLightPreference.setOnPreferenceChangeListener(applyButtonsLight);
			
			HtcPreference extremePowerSaverPreference = (HtcPreference) findPreference("pref_key_other_extremepower");
			extremePowerSaverPreference.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(HtcPreference preference) {
					Intent subActIntent = new Intent(getActivity(), SubActivity.class);
					subActIntent.putExtra("pref_section_name", (String)preference.getTitle());
					subActIntent.putExtra("pref_section_xml", R.xml.dummy);
					getActivity().startActivity(subActIntent);
					return true;
				}
			});
		} else if (xmlResId == R.xml.prefs_wakegest) {
			this.menuType = 1;
			
			final HtcListPreference swipeRightActionPreference = (HtcListPreference) findPreference("pref_key_wakegest_swiperight");
			final HtcListPreference swipeleftActionPreference = (HtcListPreference) findPreference("pref_key_wakegest_swipeleft");
			final HtcListPreference swipeUpActionPreference = (HtcListPreference) findPreference("pref_key_wakegest_swipeup");
			final HtcListPreference swipeDownActionPreference = (HtcListPreference) findPreference("pref_key_wakegest_swipedown");
			final HtcListPreference doubleTapActionPreference = (HtcListPreference) findPreference("pref_key_wakegest_dt2w");
			final HtcListPreference logoPressActionPreference = (HtcListPreference) findPreference("pref_key_wakegest_logo2wake");
			
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
			
			HtcPreference launchAppsSwipeRight = findPreference("pref_key_wakegest_swiperight_app");
			HtcPreference launchAppsSwipeLeft = findPreference("pref_key_wakegest_swipeleft_app");
			HtcPreference launchAppsSwipeUp = findPreference("pref_key_wakegest_swipeup_app");
			HtcPreference launchAppsSwipeDown = findPreference("pref_key_wakegest_swipedown_app");
			HtcPreference launchAppsDoubleTap = findPreference("pref_key_wakegest_dt2w_app");
			HtcPreference launchAppsLogoPress = findPreference("pref_key_wakegest_logo2wake_app");
			
			final Activity prefAct = getActivity();
			HtcPreference.OnPreferenceChangeListener chooseAction = new HtcPreference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
					HtcPreference launchApps = null;
					
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
						Helpers.shortcutDlg = new AppShortcutAddDialog(prefAct, preference.getKey() + "_shortcut");
						Helpers.shortcutDlg.setTitle(preference.getTitle());
						Helpers.shortcutDlg.setIcon(preference.getIcon());
						Helpers.shortcutDlg.show();
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
			
			final Activity act = getActivity();
			
			String not_selected = Helpers.l10n(act, R.string.notselected);
			HtcPreference.OnPreferenceClickListener clickPref = new HtcPreference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(HtcPreference preference) {
					HtcPreferenceScreen gesturesCat = (HtcPreferenceScreen) findPreference("pref_key_wakegest");
					final Context mContext = gesturesCat.getContext();
					final DynamicPreference dp = new DynamicPreference(mContext);
					dp.setTitle(preference.getTitle());
					dp.setIcon(preference.getIcon());
					dp.setDialogTitle(preference.getTitle());
					dp.setSummary(preference.getSummary());
					dp.setOrder(preference.getOrder());
					dp.setKey(preference.getKey());
					dp.setOnPreferenceChangeListener(new HtcPreference.OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
							preference.setSummary(Helpers.getAppName(act, (String)newValue));
							return true;
						}
					});
					
					gesturesCat.removePreference(preference);
					gesturesCat.addPreference(dp);
					
					if (Helpers.launchableAppsList == null) {
						final HtcProgressDialog dialog = new HtcProgressDialog(act);
						dialog.setMessage(Helpers.l10n(act, R.string.loading_app_data));
						dialog.setCancelable(false);
						dialog.show();
						
						new Thread() {
							@Override
							public void run() {
								try {
									Helpers.getLaunchableApps(act);
									act.runOnUiThread(new Runnable(){
										@Override
										public void run() {
											dp.show();
										}
									});
									// Nasty hack! Wait for icons to load.
									Thread.sleep(1000);
									act.runOnUiThread(new Runnable(){
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
					
					return true;
				}
			};
			
			launchAppsSwipeRight.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_wakegest_swiperight_app", not_selected)));
			launchAppsSwipeRight.setOnPreferenceClickListener(clickPref);
			launchAppsSwipeLeft.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_wakegest_swipeleft_app", not_selected)));
			launchAppsSwipeLeft.setOnPreferenceClickListener(clickPref);
			launchAppsSwipeUp.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_wakegest_swipeup_app", not_selected)));
			launchAppsSwipeUp.setOnPreferenceClickListener(clickPref);
			launchAppsSwipeDown.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_wakegest_swipedown_app", not_selected)));
			launchAppsSwipeDown.setOnPreferenceClickListener(clickPref);
			launchAppsDoubleTap.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_wakegest_dt2w_app", not_selected)));
			launchAppsDoubleTap.setOnPreferenceClickListener(clickPref);
			launchAppsLogoPress.setSummary(Helpers.getAppName(getActivity(), prefs.getString("pref_key_wakegest_logo2wake_app", not_selected)));
			launchAppsLogoPress.setOnPreferenceClickListener(clickPref);
			
			prefListView = (HtcListView)getActivity().findViewById(android.R.id.list);
			prefListView.setBackgroundResource(backResId);
			prefListView.setDivider(getResources().getDrawable(getResources().getIdentifier("inset_list_divider", "drawable", "com.htc")));
			prefListView.setDividerHeight(1);
			prefListView.setFooterDividersEnabled(false);
			
			themeHint = (TextView)getActivity().findViewById(R.id.themehint);
			themeHint.setBackgroundResource(backResId);
			themeHint.setText(Helpers.l10n(getActivity(), R.string.wakegestures_hint));
			
			if (Helpers.isButterflyS()) {
				if (logoPressActionPreference != null) ((HtcPreferenceScreen)findPreference("pref_key_wakegest")).removePreference(logoPressActionPreference);
				if (launchAppsLogoPress != null) ((HtcPreferenceScreen)findPreference("pref_key_wakegest")).removePreference(launchAppsLogoPress);
			}
		} else if (xmlResId == R.xml.prefs_persist) {
			if (Helpers.isLP())
			removePref(this, "pref_key_persist_appfilter", "pref_key_persist");
		} else if (xmlResId == R.xml.dummy) {
			this.menuType = 2;
			
			TextView hint = (TextView)getActivity().findViewById(R.id.hint);
			hint.setText(Helpers.l10n(getActivity(), R.string.various_extremepower_hint));
			getActivity().findViewById(R.id.backLayer).setBackgroundResource(backResId);
			
			contentsView = (LinearLayout)getActivity().findViewById(R.id.contents);
			
			themeHint = (TextView)getActivity().findViewById(R.id.themehint);
			themeHint.setBackgroundResource(backResId);
			themeHint.setText(Helpers.l10n(getActivity(), R.string.various_eps_hint));
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (xmlResId == R.xml.prefs_wakegest)
			return inflater.inflate(R.layout.fragment_wake_gestures, container, false);
		else if (xmlResId == R.xml.dummy) {
			return inflater.inflate(R.layout.fragment_eps_remap, container, false);
		} else
			return super.onCreateView(inflater, container, savedInstanceState);
	}
}

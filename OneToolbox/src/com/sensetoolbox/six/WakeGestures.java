package com.sensetoolbox.six;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import com.htc.app.HtcProgressDialog;
import com.htc.preference.HtcListPreference;
import com.htc.preference.HtcPreference;
import com.htc.preference.HtcPreferenceActivity;
import com.htc.preference.HtcPreferenceManager;
import com.htc.preference.HtcPreferenceScreen;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarText;
import com.htc.widget.HtcListView;
import com.htc.widget.HtcToggleButtonLight;
import com.htc.widget.HtcToggleButtonLight.OnCheckedChangeListener;
import com.sensetoolbox.six.utils.AppShortcutAddDialog;
import com.sensetoolbox.six.utils.DynamicPreference;
import com.sensetoolbox.six.utils.Helpers;

public class WakeGestures extends HtcPreferenceActivity {
	SharedPreferences prefs;
	HtcToggleButtonLight OnOffSwitch;
	HtcListView prefListView;
	int mThemeId = 0;
	
	public static CharSequence getAppName(Context ctx, String pkgActName) {
		PackageManager pm = ctx.getPackageManager();
		String not_selected = Helpers.l10n(ctx, R.string.notselected);
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Apply Settings theme
		mThemeId = Helpers.getCurrentTheme(this);
		setTheme(mThemeId);
		Helpers.setTranslucentStatusBar(this);
		
		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
		ActionBarContainer actionBarContainer = actionBarExt.getCustomContainer();
		ActionBarText actionBarText = new ActionBarText(this);
		actionBarText.setPrimaryText(Helpers.l10n(this, R.string.wakegestures_mod));
		actionBarContainer.addCenterView(actionBarText);
		actionBarContainer.setBackUpEnabled(true);
		
		View homeBtn = actionBarContainer.getChildAt(0);
		if (homeBtn != null) {
			OnClickListener goBack = new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			};
			homeBtn.setOnClickListener(goBack);
		}
		
		getPreferenceManager().setSharedPreferencesName("one_toolbox_prefs");
		getPreferenceManager().setSharedPreferencesMode(1);
		HtcPreferenceManager.setDefaultValues(this, R.xml.prefs_wakegest, false);
		prefs = getPreferenceManager().getSharedPreferences();

		addPreferencesFromResource(R.xml.prefs_wakegest);
		setContentView(R.layout.activity_wake_gestures);
		
		int backResId = getResources().getIdentifier("common_app_bkg", "drawable", "com.htc");
		findViewById(R.id.backLayer).setBackgroundResource(backResId);
		
		OnOffSwitch = new HtcToggleButtonLight(this);
		OnOffSwitch.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		OnOffSwitch.setEnabled(true);
		OnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(HtcToggleButtonLight toggle, boolean state) {
				prefs.edit().putBoolean("wake_gestures_active", state).commit();
				applyThemeState(state);
			}
		});
		actionBarContainer.addRightView(OnOffSwitch);
		
		prefListView = (HtcListView)this.findViewById(android.R.id.list);
		prefListView.setBackgroundResource(backResId);
		prefListView.setDivider(getResources().getDrawable(getResources().getIdentifier("inset_list_divider", "drawable", "com.htc")));
		prefListView.setDividerHeight(1);
		prefListView.setFooterDividersEnabled(false);
		applyThemeState(prefs.getBoolean("wake_gestures_active", false));
		
		final HtcListPreference swipeRightActionPreference = (HtcListPreference) findPreference("pref_key_wakegest_swiperight");
		final HtcListPreference swipeleftActionPreference = (HtcListPreference) findPreference("pref_key_wakegest_swipeleft");
		final HtcListPreference swipeUpActionPreference = (HtcListPreference) findPreference("pref_key_wakegest_swipeup");
		final HtcListPreference swipeDownActionPreference = (HtcListPreference) findPreference("pref_key_wakegest_swipedown");
		final HtcListPreference doubleTapActionPreference = (HtcListPreference) findPreference("pref_key_wakegest_dt2w");
		final HtcListPreference logoPressActionPreference = (HtcListPreference) findPreference("pref_key_wakegest_logo2wake");
		
		if (Helpers.isM8()) {
			logoPressActionPreference.setTitle(Helpers.l10n(this, R.string.wakegestures_volume_title));
			logoPressActionPreference.setSummary(Helpers.l10n(this, R.string.wakegestures_volume_summ));
			if (!Helpers.isWakeGestures()) {
				List<String> newEntries = new ArrayList<String>();
				TypedArray ids = getResources().obtainTypedArray(R.array.wakegest_m8stock_actions);
				for (int i = 0; i < ids.length(); i++) {
					int id = ids.getResourceId(i, 0);
					if (id != 0)
						newEntries.add(Helpers.l10n(this, id));
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
		
		final HtcPreferenceActivity prefAct = this;
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
					PrefsFragment.shortcutDlg = new AppShortcutAddDialog(prefAct, preference.getKey() + "_shortcut");
					PrefsFragment.shortcutDlg.setTitle(preference.getTitle());
					PrefsFragment.shortcutDlg.setIcon(preference.getIcon());
					PrefsFragment.shortcutDlg.show();
				}
				
				return true;
			}
		};
		
		if (swipeRightActionPreference.getValue().equals("10"))	launchAppsSwipeRight.setEnabled(true);	else launchAppsSwipeRight.setEnabled(false);
		if (swipeleftActionPreference.getValue().equals("10"))	launchAppsSwipeLeft.setEnabled(true);	else launchAppsSwipeLeft.setEnabled(false);
		if (swipeUpActionPreference.getValue().equals("10"))		launchAppsSwipeUp.setEnabled(true);		else launchAppsSwipeUp.setEnabled(false);
		if (swipeDownActionPreference.getValue().equals("10"))	launchAppsSwipeDown.setEnabled(true);	else launchAppsSwipeDown.setEnabled(false);
		if (doubleTapActionPreference.getValue().equals("10"))	launchAppsDoubleTap.setEnabled(true);	else launchAppsDoubleTap.setEnabled(false);
		if (logoPressActionPreference.getValue().equals("10"))	launchAppsLogoPress.setEnabled(true);	else launchAppsLogoPress.setEnabled(false);
		
		swipeRightActionPreference.setOnPreferenceChangeListener(chooseAction);
		swipeleftActionPreference.setOnPreferenceChangeListener(chooseAction);
		swipeUpActionPreference.setOnPreferenceChangeListener(chooseAction);
		swipeDownActionPreference.setOnPreferenceChangeListener(chooseAction);
		doubleTapActionPreference.setOnPreferenceChangeListener(chooseAction);
		logoPressActionPreference.setOnPreferenceChangeListener(chooseAction);
		
		final HtcPreferenceActivity act = this;
		
		String not_selected = Helpers.l10n(this, R.string.notselected);
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
						preference.setSummary(getAppName(act, (String)newValue));
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
		
		launchAppsSwipeRight.setSummary(getAppName(this, prefs.getString("pref_key_wakegest_swiperight_app", not_selected)));
		launchAppsSwipeRight.setOnPreferenceClickListener(clickPref);
		launchAppsSwipeLeft.setSummary(getAppName(this, prefs.getString("pref_key_wakegest_swipeleft_app", not_selected)));
		launchAppsSwipeLeft.setOnPreferenceClickListener(clickPref);
		launchAppsSwipeUp.setSummary(getAppName(this, prefs.getString("pref_key_wakegest_swipeup_app", not_selected)));
		launchAppsSwipeUp.setOnPreferenceClickListener(clickPref);
		launchAppsSwipeDown.setSummary(getAppName(this, prefs.getString("pref_key_wakegest_swipedown_app", not_selected)));
		launchAppsSwipeDown.setOnPreferenceClickListener(clickPref);
		launchAppsDoubleTap.setSummary(getAppName(this, prefs.getString("pref_key_wakegest_dt2w_app", not_selected)));
		launchAppsDoubleTap.setOnPreferenceClickListener(clickPref);
		launchAppsLogoPress.setSummary(getAppName(this, prefs.getString("pref_key_wakegest_logo2wake_app", not_selected)));
		launchAppsLogoPress.setOnPreferenceClickListener(clickPref);
		
		if (Helpers.isButterflyS()) {
			if (logoPressActionPreference != null) ((HtcPreferenceScreen)findPreference("pref_key_wakegest")).removePreference(logoPressActionPreference);
			if (launchAppsLogoPress != null) ((HtcPreferenceScreen)findPreference("pref_key_wakegest")).removePreference(launchAppsLogoPress);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Helpers.processResult(this, requestCode, resultCode, data);
	}
	
	@Override
	public void addPreferencesFromResource(int resId) {
		super.addPreferencesFromResource(resId);
		Helpers.applyLang(this, null);
	}
	
	private void applyThemeState(Boolean state) {
		OnOffSwitch.setChecked(state);
		prefListView.setEnabled(state);
		if (state) prefListView.setAlpha(1.0f); else prefListView.setAlpha(0.5f);
	}
}

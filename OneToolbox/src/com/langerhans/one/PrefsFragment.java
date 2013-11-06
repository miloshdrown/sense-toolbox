package com.langerhans.one;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.htc.preference.HtcPreferenceCategory;
import com.htc.preference.HtcPreferenceFragment;
import com.htc.preference.HtcPreferenceManager;
import com.htc.preference.HtcPreferenceScreen;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.HtcAlertDialog;
import com.langerhans.one.mods.ControlsMods;
import com.langerhans.one.utils.ApkInstaller;
import com.langerhans.one.utils.DynamicPreference;
import com.langerhans.one.utils.Helpers;
import com.langerhans.one.utils.Version;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;
import com.langerhans.one.utils.HtcListPreferencePlus;

public class PrefsFragment extends HtcPreferenceFragment {
	
	static public List<ResolveInfo> pkgAppsList = null;
	static public List<Drawable> pkgAppsListIcons = new ArrayList<Drawable>();
	static public List<Boolean> pkgAppsListSystem = new ArrayList<Boolean>();

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
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		String senseVer = Helpers.getSenseVersion();
		prefs.edit().putString("pref_sense_version", senseVer).commit();
		
		if (findPreference("pref_key_eqs") != null && (MainActivity.isRootAccessGiven == false))
		findPreference("pref_key_eqs").setEnabled(false);
		
		if (android.os.Build.VERSION.SDK_INT <= 17) {
			if (findPreference("pref_key_prism_sevenscreens") != null) ((HtcPreferenceCategory) findPreference("pref_key_sense_homescreen")).removePreference(findPreference("pref_key_prism_sevenscreens"));
		}
		
		if ((new Version(senseVer)).compareTo(new Version("5.5")) >= 0) {
			if (findPreference("pref_key_eqs") != null) getPreferenceScreen().removePreference(findPreference("pref_key_eqs"));
			if (findPreference("pref_key_prism_bfremove") != null) ((HtcPreferenceCategory) findPreference("pref_key_sense_homescreen")).removePreference(findPreference("pref_key_prism_bfremove"));
			if (findPreference("pref_key_prism_infiniscroll") != null) ((HtcPreferenceCategory) findPreference("pref_key_sense_homescreen")).removePreference(findPreference("pref_key_prism_infiniscroll"));
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
								getApps();
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
		
		HtcListPreference voldownPreference = (HtcListPreference) findPreference("pref_key_cam_voldown");
		HtcListPreference volupPreference = (HtcListPreference) findPreference("pref_key_cam_volup");
		HtcListPreference swipeDownActionPreference = (HtcListPreference) findPreference("pref_key_prism_swipedownaction");
		HtcListPreference swipeUpActionPreference = (HtcListPreference) findPreference("pref_key_prism_swipeupaction");
		HtcListPreference swipeRightActionPreference = (HtcListPreference) findPreference("pref_key_prism_swiperightaction");
		HtcListPreference swipeLeftActionPreference = (HtcListPreference) findPreference("pref_key_prism_swipeleftaction");
		HtcListPreference backLongPressActionPreference = (HtcListPreference) findPreference("pref_key_controls_backlongpressaction");
		HtcListPreference homeAssistActionPreference = (HtcListPreference) findPreference("pref_key_controls_homeassistaction");
		
		// Insert new option to controls listprefs
		backLongPressActionPreference.setEntries(addToArray(backLongPressActionPreference.getEntries(), 5, getResources().getString(R.string.kill_foreground)));
		backLongPressActionPreference.setEntryValues(addToArray(backLongPressActionPreference.getEntryValues(), 5, "9"));
		homeAssistActionPreference.setEntries(addToArray(homeAssistActionPreference.getEntries(), 5, getResources().getString(R.string.kill_foreground)));
		homeAssistActionPreference.setEntryValues(addToArray(homeAssistActionPreference.getEntryValues(), 5, "9"));
		
		voldownPreference.setOnPreferenceChangeListener(camChangeListener);
		volupPreference.setOnPreferenceChangeListener(camChangeListener);
		swipeDownActionPreference.setOnPreferenceChangeListener(chooseAction);
		swipeUpActionPreference.setOnPreferenceChangeListener(chooseAction);
		swipeRightActionPreference.setOnPreferenceChangeListener(chooseAction);
		swipeLeftActionPreference.setOnPreferenceChangeListener(chooseAction);
		backLongPressActionPreference.setOnPreferenceChangeListener(chooseAction);
		homeAssistActionPreference.setOnPreferenceChangeListener(chooseAction);
		
		HtcPreference launchAppsSwipeDown = findPreference("pref_key_prism_swipedown_app");
		HtcPreference launchAppsSwipeUp = findPreference("pref_key_prism_swipeup_app");
		HtcPreference launchAppsSwipeRight = findPreference("pref_key_prism_swiperight_app");
		HtcPreference launchAppsSwipeLeft = findPreference("pref_key_prism_swipeleft_app");
		HtcPreference launchAppsBackLongPress = findPreference("pref_key_controls_backlongpress_app");
		HtcPreference launchAppsHomeAssist = findPreference("pref_key_controls_homeassist_app");
		
		HtcListPreferencePlus toggleSwipeDown = (HtcListPreferencePlus) findPreference("pref_key_prism_swipedown_toggle");
		HtcListPreferencePlus toggleSwipeUp = (HtcListPreferencePlus) findPreference("pref_key_prism_swipeup_toggle");
		HtcListPreferencePlus toggleSwipeRight = (HtcListPreferencePlus) findPreference("pref_key_prism_swiperight_toggle");
		HtcListPreferencePlus toggleSwipeLeft = (HtcListPreferencePlus) findPreference("pref_key_prism_swipeleft_toggle");
		HtcListPreferencePlus toggleBackLongPress = (HtcListPreferencePlus) findPreference("pref_key_controls_backlongpress_toggle");
		HtcListPreferencePlus toggleHomeAssist = (HtcListPreferencePlus) findPreference("pref_key_controls_homeassist_toggle");
		
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
				ControlsMods.initScriptHandler(((HtcCheckBoxPreference) preference).isChecked());
				return true;
			}
        });
	}
	
	private CharSequence[] addToArray(CharSequence[] cs, int position, String toAdd) {
		List<CharSequence> entries = new ArrayList<CharSequence>(Arrays.asList(cs));
		entries.add(position, toAdd);
		CharSequence[] entriesNew = entries.toArray(new CharSequence[entries.size()]);
		return entriesNew;
	}
	
	public void getApps() {
		PackageManager pm = getActivity().getPackageManager();
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		PrefsFragment.pkgAppsList = pm.queryIntentActivities(mainIntent, 0);
		Collections.sort(PrefsFragment.pkgAppsList, new ResolveInfo.DisplayNameComparator(pm));
		for (ResolveInfo inf: PrefsFragment.pkgAppsList) {
			PrefsFragment.pkgAppsListIcons.add(inf.loadIcon(getActivity().getPackageManager()));
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
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.manu_mods, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.softreboot)
		{
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(getActivity());
			alert.setTitle(R.string.soft_reboot);
			alert.setMessage(R.string.hotreboot_explain_prefs);
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
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
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
		CommandCapture command = new CommandCapture(0, "/system/bin/app_process --xposedversion") {
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
				if (!isXposedInstalled)					
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						showXposedDialog();
					}
				});
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
}

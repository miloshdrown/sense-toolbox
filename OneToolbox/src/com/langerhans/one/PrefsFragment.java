package com.langerhans.one;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Dialog;
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
import com.htc.preference.HtcListPreference;
import com.htc.preference.HtcPreference;
import com.htc.preference.HtcPreferenceCategory;
import com.htc.preference.HtcPreferenceFragment;
import com.htc.preference.HtcPreferenceManager;
import com.htc.preference.HtcPreferenceScreen;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.HtcAlertDialog;
import com.langerhans.one.utils.ApkInstaller;
import com.langerhans.one.utils.DynamicPreference;
import com.langerhans.one.utils.Helpers;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

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
		
		if (findPreference("pref_key_eqs") != null && MainActivity.isRootAccessGiven == false)
		findPreference("pref_key_eqs").setEnabled(false);
		
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
		
		HtcPreference.OnPreferenceChangeListener chooseAppToLaunch = new HtcPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
				HtcPreference launchApps = null;
				
				if (preference.equals(findPreference("pref_key_prism_swipedownaction")))
				launchApps = findPreference("pref_key_prism_swipedown_app");
				
				if (preference.equals(findPreference("pref_key_prism_swipeupaction")))
				launchApps = findPreference("pref_key_prism_swipeup_app");

				if (preference.equals(findPreference("pref_key_controls_backlongpressaction")))
				launchApps = findPreference("pref_key_controls_backlongpress_app");
				
				if (launchApps != null)
				if (newValue.equals("6")) {
					launchApps.setEnabled(true);
					if (launchApps instanceof DynamicPreference)
						((DynamicPreference)launchApps).show();
					else
						launchApps.getOnPreferenceClickListener().onPreferenceClick(launchApps);
				} else {
					launchApps.setEnabled(false);
				}
				
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
				HtcPreferenceCategory senseHomeCat = (HtcPreferenceCategory) findPreference("sense_homescreen");
				HtcPreferenceScreen senseControlsCat = (HtcPreferenceScreen) findPreference("pref_key_controls");
				
				final DynamicPreference dp = new DynamicPreference(senseHomeCat.getContext() == null ? senseControlsCat.getContext() : senseHomeCat.getContext());
				dp.setTitle(preference.getTitle());
				dp.setDialogTitle(preference.getTitle());
				dp.setSummary(preference.getSummary());
				dp.setOrder(preference.getOrder());
				dp.setKey(preference.getKey());
				dp.setOnPreferenceChangeListener(fillSummary);
				
				if (senseHomeCat.findPreference(preference.getKey()) != null) {
					senseHomeCat.removePreference(preference);
					senseHomeCat.addPreference(dp);
				} else if (senseControlsCat.findPreference(preference.getKey()) != null) {
					senseControlsCat.removePreference(preference);
					senseControlsCat.addPreference(dp);
				}
				
				if (PrefsFragment.pkgAppsList == null) {
					final HtcProgressDialog dialog = new HtcProgressDialog(getActivity());
					dialog.setMessage("Loading apps data...");
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
        HtcListPreference backLongPressActionPreference = (HtcListPreference) findPreference("pref_key_controls_backlongpressaction");
        voldownPreference.setOnPreferenceChangeListener(camChangeListener);
        volupPreference.setOnPreferenceChangeListener(camChangeListener);
        swipeDownActionPreference.setOnPreferenceChangeListener(chooseAppToLaunch);
        swipeUpActionPreference.setOnPreferenceChangeListener(chooseAppToLaunch);
        backLongPressActionPreference.setOnPreferenceChangeListener(chooseAppToLaunch);
        
		HtcPreference launchAppsSwipeDown = findPreference("pref_key_prism_swipedown_app");
		HtcPreference launchAppsSwipeUp = findPreference("pref_key_prism_swipeup_app");
		HtcPreference launchAppsBackLongPress = findPreference("pref_key_controls_backlongpress_app");
		
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		String not_selected = getResources().getString(R.string.notselected);
		launchAppsSwipeDown.setSummary(getAppName(prefs.getString("pref_key_prism_swipedown_app", not_selected)));
		launchAppsSwipeDown.setOnPreferenceClickListener(clickPref);
		launchAppsSwipeUp.setSummary(getAppName(prefs.getString("pref_key_prism_swipeup_app", not_selected)));
		launchAppsSwipeUp.setOnPreferenceClickListener(clickPref);
		launchAppsBackLongPress.setSummary(getAppName(prefs.getString("pref_key_controls_backlongpress_app", not_selected)));
		launchAppsBackLongPress.setOnPreferenceClickListener(clickPref);
		
		if (swipeDownActionPreference.getValue().equals("6"))
			launchAppsSwipeDown.setEnabled(true);
		else
			launchAppsSwipeDown.setEnabled(false);
		
		if (swipeUpActionPreference.getValue().equals("6"))
			launchAppsSwipeUp.setEnabled(true);
		else
			launchAppsSwipeUp.setEnabled(false);
		
		if (backLongPressActionPreference.getValue().equals("6"))
			launchAppsBackLongPress.setEnabled(true);
		else
			launchAppsBackLongPress.setEnabled(false);
		
        HtcPreference sunbeamInstallPref = findPreference("pref_key_cb_sunbeam");
        sunbeamInstallPref.setOnPreferenceClickListener(new HtcPreference.OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(HtcPreference preference) {
				ApkInstaller.installSunbeam(getActivity());
				return true;
			}
        });
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
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == 13370 && resultCode == -1)
    	((HtcListPreference)findPreference("pref_key_prism_swipedownaction")).setValue("4");

    	if (requestCode == 13371 && resultCode == -1)
    	((HtcListPreference)findPreference("pref_key_prism_swipeupaction")).setValue("4");
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
			getPreferenceManager().setSharedPreferencesMode(1);
			try {
				CommandCapture command = new CommandCapture(0, "setprop ctl.restart zygote");
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (Exception e) {
				e.printStackTrace();
			}
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
	
	public void checkForXposed() {		
		CommandCapture command = new CommandCapture(0, "/system/bin/app_process --xposedversion") {
			@Override
			public void output(int id, String line)
			{
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

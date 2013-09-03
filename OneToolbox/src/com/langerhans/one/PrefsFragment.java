package com.langerhans.one;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
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

import com.langerhans.one.utils.ApkInstaller;
import com.langerhans.one.utils.Helpers;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class PrefsFragment extends PreferenceFragment {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        if(!Helpers.isXposedInstalled(getActivity()))
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        	builder.setTitle(R.string.xposed_not_found);
        	builder.setMessage(R.string.xposed_not_found_explain);
        	builder.setNeutralButton(R.string.okay, null);
        	AlertDialog dlg = builder.create();
        	dlg.show();
        }
        
        getPreferenceManager().setSharedPreferencesName("one_toolbox_prefs");
        getPreferenceManager().setSharedPreferencesMode(1);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        addPreferencesFromResource(R.xml.preferences);
        
        //Add version name to support title
        try {
            PreferenceCategory supportCat = (PreferenceCategory) findPreference("pref_key_support");
			supportCat.setTitle(getActivity().getString(R.string.support_version, getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName));
		} catch (NameNotFoundException e) {
			//Shouldn't happen...
			e.printStackTrace();
		}
        
        Preference.OnPreferenceChangeListener camChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(getActivity(), R.string.close_camera, Toast.LENGTH_LONG).show();
                return true;
            }
        };

        ListPreference voldownPreference = (ListPreference) findPreference("pref_key_cam_voldown");
        ListPreference volupPreference = (ListPreference) findPreference("pref_key_cam_volup");
        voldownPreference.setOnPreferenceChangeListener(camChangeListener);
        volupPreference.setOnPreferenceChangeListener(camChangeListener);
        
        Preference sunbeamInstallPref = findPreference("pref_key_cb_sunbeam");
        sunbeamInstallPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ApkInstaller.installSunbeam(getActivity());
				return true;
			}
        });
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
		}
		return true;
	}
	
	// Fix for the sub PreferenceScreens HomeAsUp bug 
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen parentPreferenceScreen, Preference preference) {
		super.onPreferenceTreeClick(parentPreferenceScreen, preference);

		if (preference instanceof PreferenceScreen) {
			PreferenceScreen preferenceScreen = (PreferenceScreen) preference;
			final Dialog dialog = preferenceScreen.getDialog();

			if (dialog != null) {
				dialog.getActionBar().setDisplayHomeAsUpEnabled(true);
				View homeBtn = dialog.findViewById(android.R.id.home);

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
}

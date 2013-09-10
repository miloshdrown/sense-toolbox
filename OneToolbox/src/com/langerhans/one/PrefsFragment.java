package com.langerhans.one;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.pm.PackageManager.NameNotFoundException;
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
import com.langerhans.one.utils.Helpers;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class PrefsFragment extends HtcPreferenceFragment {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
		
        if(!Helpers.isXposedInstalled(getActivity()))
        {
        	HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(getActivity());
        	builder.setTitle(R.string.xposed_not_found);
        	builder.setMessage(R.string.xposed_not_found_explain);
        	builder.setNeutralButton(R.string.okay, null);
        	HtcAlertDialog dlg = builder.create();
        	dlg.show();
        }
        
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

        HtcListPreference voldownPreference = (HtcListPreference) findPreference("pref_key_cam_voldown");
        HtcListPreference volupPreference = (HtcListPreference) findPreference("pref_key_cam_volup");
        voldownPreference.setOnPreferenceChangeListener(camChangeListener);
        volupPreference.setOnPreferenceChangeListener(camChangeListener);
        
        HtcPreference sunbeamInstallPref = findPreference("pref_key_cb_sunbeam");
        sunbeamInstallPref.setOnPreferenceClickListener(new HtcPreference.OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(HtcPreference preference) {
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
}

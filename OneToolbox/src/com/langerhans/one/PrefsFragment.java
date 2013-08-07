package com.langerhans.one;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

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
        	builder.setTitle("Xposed Installer not found");
        	builder.setMessage("It looks like you don't have Xposed Installed.\n\n"
        			+ "Please note that the mods on this page will only work with Xposed!\n\n"
        			+ "Xposed is available in the ARHD installer.");
        	builder.setNeutralButton("Okay", null);
        	AlertDialog dlg = builder.create();
        	dlg.show();
        }
        
        getPreferenceManager().setSharedPreferencesName("one_toolbox_prefs");
        getPreferenceManager().setSharedPreferencesMode(1);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        addPreferencesFromResource(R.xml.preferences);
        Toast.makeText(getActivity(), "Make sure to enable the module in Xposed!", Toast.LENGTH_LONG).show();
        Preference.OnPreferenceChangeListener camChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(getActivity(), "Make sure to close camera from recent apps!", Toast.LENGTH_LONG).show();
                return true;
            }
        };

        ListPreference voldownPreference = (ListPreference) findPreference("pref_key_cam_voldown");
        ListPreference volupPreference = (ListPreference) findPreference("pref_key_cam_volup");
        voldownPreference.setOnPreferenceChangeListener(camChangeListener);
        volupPreference.setOnPreferenceChangeListener(camChangeListener);
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
}

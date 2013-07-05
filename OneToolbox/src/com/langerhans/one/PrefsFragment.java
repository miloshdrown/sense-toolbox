package com.langerhans.one;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class PrefsFragment extends PreferenceFragment {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //PreferenceManager prefMgr = getPreferenceManager();
        getPreferenceManager().setSharedPreferencesName("one_toolbox_prefs");
        getPreferenceManager().setSharedPreferencesMode(1);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        
        //SharedPreferences prefs = getActivity().getSharedPreferences("one_toolbox_prefs", 1);
		//if (prefs.getBoolean("firstrun_mods", true)) {
		//	try { //since setSharedPreferencesMode(1) is deprecated, this is a dirty workaround for the module until xposed 2.2 is available...
		//		RootTools.sendShell("touch /data/data/com.langerhans.one/shared_prefs/com.langerhans.one_preferences.xml", -1);
		//		RootTools.sendShell("chmod 664 /data/data/com.langerhans.one/shared_prefs/com.langerhans.one_preferences.xml", -1);
		//		prefs.edit().putBoolean("firstrun_mods", false).commit();
		//	} catch (Exception e) {
		//		e.printStackTrace();
		//	}
		//}
        
        addPreferencesFromResource(R.xml.preferences);
        Toast.makeText(getActivity(), "Make sure to enable the module in xposed!", Toast.LENGTH_LONG).show();
        Preference.OnPreferenceChangeListener camChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(getActivity(), "Make sure to close camera from recent apps!", Toast.LENGTH_LONG).show();
                return true;
            }
        };

        ListPreference voldownPreference = (ListPreference) findPreference("pref_key_cam_voldown");
        ListPreference volupPreference = (ListPreference) findPreference("pref_key_cam_volup");
        CheckBoxPreference powerWPreference = (CheckBoxPreference) findPreference("pref_key_cam_powerW");
        voldownPreference.setOnPreferenceChangeListener(camChangeListener);
        volupPreference.setOnPreferenceChangeListener(camChangeListener);
        powerWPreference.setOnPreferenceChangeListener(camChangeListener);
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

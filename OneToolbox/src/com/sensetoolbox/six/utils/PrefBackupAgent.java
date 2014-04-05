package com.sensetoolbox.six.utils;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.SharedPreferences;
import android.os.ParcelFileDescriptor;

public class PrefBackupAgent extends BackupAgentHelper {

	@Override
    public void onCreate() {
        super.onCreate();
        // A Helper for our Preferences, this name is the same name we use when saving SharedPreferences
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, "one_toolbox_prefs");
        addHelper("prefs_helper", helper);
    }
	
	@Override
	public void onRestore (BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException
	{
		super.onRestore(data, appVersionCode, newState);
		SharedPreferences prefs = getSharedPreferences("one_toolbox_prefs", 1);
		prefs.edit().putBoolean("pref_key_was_restore", true).commit();
	}
	
}

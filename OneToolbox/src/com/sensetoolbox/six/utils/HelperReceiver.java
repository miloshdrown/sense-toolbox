package com.sensetoolbox.six.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

public class HelperReceiver extends BroadcastReceiver {
	@Override
	@SuppressWarnings("deprecation")
	public void onReceive(final Context ctx, Intent intent) {
		if (intent.getAction() == null) return;
		/*
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) && !Helpers.isEight() && Helpers.isLP()) {
			SharedPreferences prefs = ctx.getSharedPreferences("one_toolbox_prefs", 1);
			if (prefs.getBoolean("wake_gestures_active", false) && Helpers.isWakeGesturesAvailable()) {
				Log.i("[ST]", "Wake gestures activated");
				Helpers.setWakeGestures(true);
			}
		}
		*/
		if (intent.getAction().equals("com.sensetoolbox.six.BLOCKHEADSUP")) {
			String pkgName = intent.getStringExtra("pkgName");
			if (pkgName == null) return;
			SharedPreferences prefs = ctx.getSharedPreferences("one_toolbox_prefs", 1);
			HashSet<String> appsList = new HashSet<String>(prefs.getStringSet("pref_key_betterheadsup_bwlist_apps", new HashSet<String>()));
			appsList.add(pkgName);
			prefs.edit().putStringSet("pref_key_betterheadsup_bwlist_apps", new HashSet<String>(appsList)).commit();
		} else if (intent.getAction().equals("com.sensetoolbox.six.SAVEEXCEPTION")) {
			try {
				Throwable thw = (Throwable)intent.getSerializableExtra("throwable");
				StringWriter stackTrace = new StringWriter();
				thw.printStackTrace(new PrintWriter(stackTrace));
				
				String exceptionsPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SenseToolbox/";
				if (!Helpers.preparePathSilently(exceptionsPath)) return;
				
				File f = new File(exceptionsPath + "uncaught_exceptions");
				if (!f.exists()) f.createNewFile();
				
				try (FileOutputStream fOut = new FileOutputStream(f, true)) {
					try (OutputStreamWriter output = new OutputStreamWriter(fOut)) {
						output.write(stackTrace.toString() + "\n\n");
					}
				}
			} catch (Throwable t) {}
		} else if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
			Helpers.l10n = null;
			Helpers.cLang = "";
		} else {
			if (!Helpers.isM7()) return;
			final int thepref = Integer.parseInt(ctx.getSharedPreferences("one_toolbox_prefs", 1).getString("pref_key_other_keyslight", "1"));
			if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
				if (thepref > 1) Helpers.setButtonBacklightTo(ctx, thepref, false);
				Command command = new Command(0, false, "getenforce 2>/dev/null") {
					int lineCnt = 0;
					
					@Override
					public void commandOutput(int id, String line) {
						super.commandOutput(id, line);
						if (lineCnt > 0) return;
						boolean isSELinuxEnforcing = line.trim().equalsIgnoreCase("enforcing");
						Settings.System.putString(ctx.getContentResolver(), "isSELinuxEnforcing", String.valueOf(isSELinuxEnforcing));
						if (isSELinuxEnforcing && thepref > 1) Helpers.setButtonBacklightTo(ctx, thepref, false);
						lineCnt++;
					}
				};
				try {
					RootTools.getShell(false).add(command);
				} catch (Exception e) {
					// handle exception
				}
			} else if (intent.getAction().equals("com.sensetoolbox.six.UPDATEBACKLIGHT")) {
				boolean forceDisableBacklight = false;
				PowerManager pm = (PowerManager)ctx.getSystemService(Context.POWER_SERVICE);
				if (!pm.isScreenOn())
					forceDisableBacklight = true;
				else
					forceDisableBacklight = intent.getBooleanExtra("forceDisableBacklight", false);
				
				if (forceDisableBacklight)
					Helpers.setButtonBacklightTo(ctx, 5, false);
				else
					Helpers.setButtonBacklightTo(ctx, thepref, false);
			}
		}
	}
}

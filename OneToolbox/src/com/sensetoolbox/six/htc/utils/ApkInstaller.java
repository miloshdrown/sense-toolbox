package com.sensetoolbox.six.htc.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.util.Log;

import com.htc.widget.HtcAlertDialog;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.htc.HActivityEx;
import com.sensetoolbox.six.utils.Helpers;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

public class ApkInstaller {
	
	public static void installSunbeam(final Activity act) {
		AssetManager assetManager = act.getAssets();

		InputStream in = null;
		OutputStream out = null;

		try {
			in = assetManager.open("SunBeam.apk");
			File cache = act.getCacheDir();
			out = new FileOutputStream(cache.getAbsolutePath() + "/SunBeam.apk");

			byte[] buffer = new byte[1024];

			int read;
			while((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}

			in.close();
			in = null;

			out.flush();
			out.close();
			out = null;

			String mkdirCmd = "echo";
			String apkPath = "/system/app/SunBeam.apk";
			if (Helpers.isLP()) {
				mkdirCmd = "mkdir -p /system/app/SunBeam; chmod 755 /system/app/SunBeam";
				apkPath = "/system/app/SunBeam/SunBeam.apk";
			}
			
			Command command = new Command(0, false,
					"mount -o rw,remount /system",
					mkdirCmd,
					"cp -f " + cache.getAbsolutePath() + "/SunBeam.apk " + apkPath,
					"rm -f " + cache.getAbsolutePath() + "/SunBeam.apk",
					"chmod 644 " + apkPath,
					"mount -o ro,remount /system") {
				@Override
				public void commandCompleted(int id, int exitcode) {
					if (exitcode == 0 && act != null && !act.isFinishing() && ((HActivityEx)act).isActive)
					act.runOnUiThread(new Runnable() {
						public void run() {
							new HtcAlertDialog.Builder(act).setMessage(Helpers.l10n(act, R.string.sunbeam_installed)).setTitle(Helpers.l10n(act, R.string.success)).setCancelable(true)
							.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) { }
							}).show();
						}
					});
				}
			};
			RootTools.getShell(true).add(command);
		} catch(Exception e) {
			Log.e("APK", e.toString());
		}
	}
}

package com.sensetoolbox.six.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.util.Log;

import com.htc.widget.HtcAlertDialog;
import com.sensetoolbox.six.R;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class ApkInstaller {
	
	public static void installSunbeam(final Context ctx) {
		AssetManager assetManager = ctx.getAssets();

		InputStream in = null;
		OutputStream out = null;

		try {
		    in = assetManager.open("SunBeam.apk");
		    File cache = ctx.getCacheDir();
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

		    CommandCapture command = new CommandCapture(0, 
		    		"mount -o rw,remount /system",
		    		"cp -f " + cache.getAbsolutePath() + "/SunBeam.apk /system/app/SunBeam.apk",
		    		"rm -f " + cache.getAbsolutePath() + "/SunBeam.apk",
		    		"chmod 644 /system/app/SunBeam.apk",
		    		"mount -o ro,remount /system") {
		    	@Override
	    		public void commandCompleted(int id, int exitcode) {
	    			if (exitcode == 0) new HtcAlertDialog.Builder(ctx).setMessage(Helpers.l10n(ctx, R.string.sunbeam_installed)).setTitle(Helpers.l10n(ctx, R.string.success)).setCancelable(true)
	    			.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int whichButton) { }
	    			}).show();
	    		}
		    };
		    RootTools.getShell(true).add(command);
		} catch(Exception e) {
			Log.e("APK", e.toString());
		}
	}
}

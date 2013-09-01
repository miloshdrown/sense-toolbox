package com.langerhans.one.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class ApkInstaller {
	
	public static void installSunbeam(Context ctx) {
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
		    		"mount -o ro,remount /system");
		    RootTools.getShell(true).add(command).waitForFinish();
		    		    
		    new AlertDialog.Builder(ctx)
		    	.setMessage("SunBeam has been installed. Make sure you soft reboot to apply the changes. Remember that you need to reinstall SunBeam after every ROM flash.")
		    	.setTitle("Success")
		    	.setCancelable(true)
		    	.setNeutralButton(android.R.string.ok,
		        new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton){}
		    	})
		    	.show();

		} catch(Exception e) {
			Log.e("APK", e.toString());
		}
	}
}

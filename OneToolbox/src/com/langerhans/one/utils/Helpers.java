package com.langerhans.one.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.htc.widget.HtcAlertDialog;
import com.langerhans.one.R;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.view.Gravity;
import android.widget.TextView;

public class Helpers {
	
	static DocumentBuilderFactory dbf;
	static DocumentBuilder db;
	static Document doc;
	static Element eQS;

	public Helpers() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Check if the Xposed Installer is installed. 
	 * It could still be that the user hasn't clikced 'install' there yet. 
	 * But I don't know of a way to check that...
	 * @param ctx The app context
	 * @return true if Xposed Installer is installed
	 */
	public static boolean isXposedInstalled(Context ctx)
	{
		PackageManager pm = ctx.getPackageManager();
	    boolean installed = false;
	    try {
	       pm.getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_ACTIVITIES);
	       installed = true;
	    } catch (PackageManager.NameNotFoundException e) {
	       installed = false;
	    }
	    return installed;
	}
	
	/**
	 * Gets the current Sense version as String
	 * @return Current Sense version
	 */
	public static String getSenseVersion()
	{
		return String.valueOf(com.htc.util.phone.ProjectUtils.getSenseVersion());
	}
	
	public static TextView createCenteredText(Context ctx, int resId) {
		TextView centerMsg = new TextView(ctx);
		centerMsg.setText(resId);
		centerMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		centerMsg.setPadding(0, 60, 0, 60);
		centerMsg.setTextSize(18.0f);
		centerMsg.setTextColor(Color.LTGRAY);
		return centerMsg; 
	}
	
	public static boolean checkStorageReadable(Context ctx) {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) || state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(ctx);
			alert.setTitle(R.string.warning);
			alert.setView(Helpers.createCenteredText(ctx, R.string.storage_unavailable));
			alert.setNeutralButton(ctx.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return false;
		}
	}
}

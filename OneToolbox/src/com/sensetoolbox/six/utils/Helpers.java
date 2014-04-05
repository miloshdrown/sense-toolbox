package com.sensetoolbox.six.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
	
	public static boolean isXposedInstalled(Context ctx) {
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
	
	public static String getSenseVersion() {
		return String.valueOf(com.htc.util.phone.ProjectUtils.getSenseVersion());
	}
	
	public static TextView createCenteredText(Context ctx, int resId) {
		TextView centerMsg = new TextView(ctx);
		centerMsg.setText(resId);
		centerMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		centerMsg.setPadding(0, 60, 0, 60);
		centerMsg.setTextSize(18.0f);
		centerMsg.setTextColor(Color.DKGRAY);
		return centerMsg; 
	}
}

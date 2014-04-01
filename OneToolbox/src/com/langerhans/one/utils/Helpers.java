package com.langerhans.one.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import android.content.Context;
import android.content.pm.PackageManager;

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
}

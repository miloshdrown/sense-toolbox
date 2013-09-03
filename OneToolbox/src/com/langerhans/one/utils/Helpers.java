package com.langerhans.one.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.widget.ArrayAdapter;

import com.langerhans.one.R;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class Helpers {
	
	static DocumentBuilderFactory dbf;
	static DocumentBuilder db;
	static Document doc;
	static Element eQS;

	public Helpers() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Gets the CID of the device.
	 * @return The CID
	 */
	public static String getCID() {
		String cidXML;
        InputStream inputstream = null;
        try {
            inputstream = Runtime.getRuntime().exec("/system/bin/getprop ro.cid").getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String propval = "";
        try {
            propval = new Scanner(inputstream).useDelimiter("\\A").nextLine();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        try {
        	List<String> availACC = new ArrayList<String>();
			availACC = RootTools.sendShell("ls /system/customize/ACC/", -1);
			for (int i = 0; i < availACC.size(); i++) {
				String tc = propval + ".xml";
				if (availACC.get(i).equals(tc))
				{
					cidXML = propval;
					System.out.println("Found file for "+tc); 
					return cidXML;
				}
			}
			cidXML = "default";
			return cidXML;
		} catch (Exception e) {
			e.printStackTrace();
		}
        return "ERR_NO_CID";
    }	
	
	/**
	 * Parse the ACC XML file for the given CID
	 * @param cidXML The CID of the device
	 * @return A String Array that holds all currently used tiles
	 */
	public static String[] parseACC(String cidXML, Context ctx)
	{
		dbf = DocumentBuilderFactory.newInstance();
		try {
			db = dbf.newDocumentBuilder();

//			File sdcard = Environment.getExternalStorageDirectory();
			File sdcard = ctx.getCacheDir();
			
			CommandCapture command = new CommandCapture(0, 
					"cp /system/customize/ACC/" + cidXML + ".xml " + sdcard.getAbsolutePath() +"/tmp.xml", 
					"chmod 777 " + sdcard.getAbsolutePath() + "/tmp.xml");
		    RootTools.getShell(true).add(command).waitForFinish();

			File file = new File(sdcard,"tmp.xml");
			
			try {
				InputSource is = new InputSource(new FileReader(file));
				doc = db.parse(is);
	        	doc.getDocumentElement().normalize();
	        	NodeList allItems = doc.getElementsByTagName("item");
	        	for (int i = 0; i < allItems.getLength(); i++) {
	        		Node node = allItems.item(i);
	        		if(node.getAttributes().getNamedItem("name").getNodeValue().equals("quick_setting_items"))
	        		{
	        			eQS = (Element) node;
	        			NodeList qsOrder = eQS.getElementsByTagName("int");
	        			String[] qsList = new String[qsOrder.getLength()];
	        			for (int j = 0; j < qsOrder.getLength(); j++)
	        			{
	        				qsList[j] = qsOrder.item(j).getChildNodes().item(0).getNodeValue();
	        			}
	    				//Remove tmp.xml
	    				RootTools.sendShell("rm " + sdcard.getAbsolutePath() +"/tmp.xml", 0);
	        			return qsList;
	        		}
	        	}
			} catch (Exception e) {
				System.out.println("Inner XML Parsing Excpetion = " + e);
			}
			
		} catch (Exception e) {
			System.out.println("Outer XML Parsing Excpetion = " + e);
		}
		return null;
	}
	
	/**
	 * Writes the new ACC XML file.
	 * @param cidXML The CID for naming the file. Should be the same as we read from.
	 * @param adapter The ArrayAdapter that holds the tile order to be saved.
	 */
	public static void writeACC(String cidXML, ArrayAdapter<String> adapter, Context ctx)
	{		
		try 
		{
			removeAllChildren(eQS);
			for (int i = 0; i<adapter.getCount(); i++)
			{
				Element e = doc.createElement("int");
				e.appendChild(doc.createTextNode(adapter.getItem(i)));
				eQS.appendChild(e);
			}

//			String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
			String dir = ctx.getCacheDir().getAbsolutePath() + File.separator;

//			File file = new File(Environment.getExternalStorageDirectory() + File.separator + "new.xml");
			File file = new File(ctx.getCacheDir() + File.separator + "new.xml");
			file.createNewFile();
			if(file.exists())
			{
			     OutputStream fo = new FileOutputStream(file);              
			     fo.write(getStringFromDoc(doc).getBytes());
			     fo.close();
			     CommandCapture command = new CommandCapture(0, 
			    		 "mount -o rw,remount /system", 
			    		 "cat "+dir+"new.xml > /system/customize/ACC/" + cidXML + ".xml",
			    		 "rm "+dir+"tmp.xml",
			    		 "rm "+dir+"new.xml",
			    		 "mount -o ro,remount /system"
			    		 );
			     RootTools.getShell(true).add(command).waitForFinish();
			} 
			
		} catch (Exception e) {
			System.out.println("XML Parsing Excpetion = " + e);
		}
		
	}
	
	/**
	 * Removes all children from the given XML node
	 * @param node The node from which the children should be removed
	 */
	public static void removeAllChildren(Node node)
	{
	  for (Node child; (child = node.getFirstChild()) != null; node.removeChild(child));
	}
	
	/**
	 * Gets the String representation from an XML document
	 * @param doc The XML document
	 * @return The String representation
	 */
	public static String getStringFromDoc(org.w3c.dom.Document doc)    {
		try {
			  Transformer transformer = TransformerFactory.newInstance().newTransformer();
			  StreamResult result = new StreamResult(new StringWriter());
			  DOMSource source = new DOMSource(doc);
			  transformer.transform(source, result);
			  return result.getWriter().toString();
			} catch(TransformerException ex) {
			  ex.printStackTrace();
			  return null;
			} 
	}
	
	/**
	 * Maps a given EQS name to it's ID
	 * @param name EQS tile name
	 * @return EQS tile ID
	 */
	public static String mapStringToID(Context context, String name)
	{
		Resources res = context.getResources();
		if (name.equals(res.getText(R.string.eqs_user_card))) return "0";
		else if (name.equals(res.getText(R.string.eqs_brightness))) return "1";
		else if (name.equals(res.getText(R.string.eqs_settings))) return "2";
		else if (name.equals(res.getText(R.string.eqs_wifi))) return "3";
		else if (name.equals(res.getText(R.string.eqs_bluetooth))) return "4";
		else if (name.equals(res.getText(R.string.eqs_airplane))) return "5";
		else if (name.equals(res.getText(R.string.eqs_power_save))) return "6";
		else if (name.equals(res.getText(R.string.eqs_rotation))) return "7";
		else if (name.equals(res.getText(R.string.eqs_mobile_data))) return "8";
		else if (name.equals(res.getText(R.string.eqs_sound_profile))) return "9";
		else if (name.equals(res.getText(R.string.eqs_wifi_hotspot))) return "10";
		else if (name.equals(res.getText(R.string.eqs_screenshot))) return "11";
		else if (name.equals(res.getText(R.string.eqs_gps))) return "12";
		else if (name.equals(res.getText(R.string.eqs_roaming))) return "13";
		else if (name.equals(res.getText(R.string.eqs_media_output))) return "14";
		else if (name.equals(res.getText(R.string.eqs_auto_sync))) return "15";
		else if (name.equals(res.getText(R.string.eqs_roaming_setting))) return "16";
		else if (name.equals(res.getText(R.string.eqs_music_channel))) return "17";
		else if (name.equals(res.getText(R.string.eqs_ringtone))) return "18";
		else if (name.equals(res.getText(R.string.eqs_timeout))) return "19";
		else if (name.equals(res.getText(R.string.eqs_syn_all_fake))) return "20";
		else if (name.equals(res.getText(R.string.eqs_apn))) return "21";
		return "0";
	}
	
	/**
	 * Maps an EQS tile ID to it's name
	 * @param id EQS tile ID
	 * @return EQS tile name
	 */
	public static int mapIDToString(int id)
	{
		switch(id){
		case 0:
			return R.string.eqs_user_card;
		case 1:
			return R.string.eqs_brightness;
		case 2:
			return R.string.eqs_settings;
		case 3:
			return R.string.eqs_wifi;
		case 4:
			return R.string.eqs_bluetooth;
		case 5:
			return R.string.eqs_airplane;
		case 6:
			return R.string.eqs_power_save;
		case 7:
			return R.string.eqs_rotation;
		case 8:
			return R.string.eqs_mobile_data;
		case 9:
			return R.string.eqs_sound_profile;
		case 10:
			return R.string.eqs_wifi_hotspot;
		case 11:
			return R.string.eqs_screenshot;
		case 12:
			return R.string.eqs_gps;
		case 13:
			return R.string.eqs_roaming;
		case 14:
			return R.string.eqs_media_output;
		case 15:
			return R.string.eqs_auto_sync;
		case 16:
			return R.string.eqs_roaming_setting;
		case 17:
			return R.string.eqs_music_channel;
		case 18:
			return R.string.eqs_ringtone;
		case 19:
			return R.string.eqs_timeout;
		case 20:
			return R.string.eqs_syn_all_fake;
		case 21:
			return R.string.eqs_apn;
		}
		return R.string.dummy;
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
}

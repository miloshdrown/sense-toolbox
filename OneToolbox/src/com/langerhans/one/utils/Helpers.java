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

import android.os.Environment;
import android.widget.ArrayAdapter;

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
	
	public static String[] parseACC(String cidXML)
	{
		dbf = DocumentBuilderFactory.newInstance();
		try {
			db = dbf.newDocumentBuilder();

			CommandCapture command = new CommandCapture(0, 
					"cp /system/customize/ACC/" + cidXML + ".xml /data/media/0/tmp.xml", 
					"chmod 777 /data/media/0/tmp.xml");
		    RootTools.getShell(true).add(command).waitForFinish();
			
			File sdcard = Environment.getExternalStorageDirectory();

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
	        			return qsList;
	        		}
	        	}
			} catch (Exception e) {
				System.out.println("XML Parsing Excpetion = " + e);
			}
			
		} catch (Exception e) {
			System.out.println("XML Parsing Excpetion = " + e);
		}
		return null;
	}
	
	public static void writeACC(String cidXML, ArrayAdapter<String> adapter)
	{		
		try 
		{
			removeAllChildren(eQS);
			for (int i = 0; i<adapter.getCount(); i++)
			{
				Element e = doc.createElement("int");
				e.appendChild(doc.createTextNode(mapStringToID(adapter.getItem(i))));
				eQS.appendChild(e);
			}
			File file = new File(Environment.getExternalStorageDirectory() + File.separator + "new.xml");
			file.createNewFile();
			if(file.exists())
			{
			     OutputStream fo = new FileOutputStream(file);              
			     fo.write(getStringFromDoc(doc).getBytes());
			     fo.close();
			     CommandCapture command = new CommandCapture(0, 
			    		 "mount -o rw,remount /system", 
			    		 "cat /data/media/0/new.xml > /system/customize/ACC/" + cidXML + ".xml",
			    		 "rm /data/media/0/tmp.xml",
			    		 "rm /data/media/0/new.xml",
			    		 "mount -o ro,remount /system"
			    		 );
			     RootTools.getShell(true).add(command).waitForFinish();
			} 
			
		} catch (Exception e) {
			System.out.println("XML Parsing Excpetion = " + e);
		}
		
	}
	
	public static void removeAllChildren(Node node)
	{
	  for (Node child; (child = node.getFirstChild()) != null; node.removeChild(child));
	}
	
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
	
	public static String mapStringToID(String name)
	{
		if (name.equals("User Card")) return "0";
		else if (name.equals("Brightness")) return "1";
		else if (name.equals("Settings")) return "2";
		else if (name.equals("WiFi")) return "3";
		else if (name.equals("Bluetooth")) return "4";
		else if (name.equals("Airplane")) return "5";
		else if (name.equals("Power Save")) return "6";
		else if (name.equals("Rotation")) return "7";
		else if (name.equals("Mobile Data")) return "8";
		else if (name.equals("Sound Profile")) return "9";
		else if (name.equals("WiFi Hotspot")) return "10";
		else if (name.equals("Screenshot")) return "11";
		else if (name.equals("GPS")) return "12";
		else if (name.equals("Roaming")) return "13";
		else if (name.equals("Media Output")) return "14";
		else if (name.equals("Auto Sync")) return "15";
		else if (name.equals("Roaming Setting")) return "16";
		else if (name.equals("Music Channel")) return "17";
		else if (name.equals("Ringtone")) return "18";
		else if (name.equals("Timeout")) return "19";
		else if (name.equals("Syn_All_Fake(?)")) return "20";
		else if (name.equals("APN")) return "21";
		return "0";
	}
	
	public static String mapIDToString(int id)
	{
		switch(id){
		case 0:
			return "User Card";
		case 1:
			return "Brightness";
		case 2:
			return "Settings";
		case 3:
			return "WiFi";
		case 4:
			return "Bluetooth";
		case 5:
			return "Airplane";
		case 6:
			return "Power Save";
		case 7:
			return "Rotation";
		case 8:
			return "Mobile Data";
		case 9:
			return "Sound Profile";
		case 10:
			return "WiFi Hotspot";
		case 11:
			return "Screenshot";
		case 12:
			return "GPS";
		case 13:
			return "Roaming";
		case 14:
			return "Media Output";
		case 15:
			return "Auto Sync";
		case 16:
			return "Roaming Setting";
		case 17:
			return "Music Channel";
		case 18:
			return "Ringtone";
		case 19:
			return "Timeout";
		case 20:
			return "Syn_All_Fake(?)";
		case 21:
			return "APN";
		}
		return "";
	}
}

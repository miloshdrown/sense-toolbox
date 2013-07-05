package com.langerhans.one;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.langerhans.one.R;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class ReorderFragment extends Fragment {

	DragSortListView listView;
	ArrayAdapter<String> adapter;
	DocumentBuilderFactory dbf;
	DocumentBuilder db;
	Document doc;
	Element eQS;
	String cidXML;
	ArrayList<String> qsAvail;
	Context ctx;

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener()
	{
	    @Override
	    public void drop(int from, int to)
	    {
	        if (from != to)
	        {
	            String item = adapter.getItem(from);
	            adapter.remove(item);
	            adapter.insert(item, to);
	        }
	    }
	};

	private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener()
	{
	    @Override
	    public void remove(int which)
	    {
	    	if(adapter.getCount()<2)
	    	{
	    		alertbox("Warning", "You can't remove the last item!");
	    		adapter.notifyDataSetChanged();
	    		return;
	    	}else
	    	{
			    qsAvail.add(mapStringToID(adapter.getItem(which)));
		        adapter.remove(adapter.getItem(which));
	    	}
	    }
	};
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.reorder_fragment, container, false);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.main, menu);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.remove("android:support:fragments");
	}
	
	@Override
	public void onStart() {
		super.onStart();
		ctx = getActivity();
		SharedPreferences prefs = getActivity().getSharedPreferences("one_toolbox_prefs", 1);
		if (prefs.getBoolean("firstrun_reorder", true)) {
			showOverLay();
            prefs.edit().putBoolean("firstrun_reorder", false).commit();
		}
		getCID();
		
		listView = (DragSortListView) getActivity().findViewById(R.id.listview);
	    String[] qsAvailH = getResources().getStringArray(R.array.availTiles);
	    qsAvail = new ArrayList<String>();
	    Collections.addAll(qsAvail, qsAvailH);
	    ArrayList<String> list = new ArrayList<String>(Arrays.asList(parseACC()));
	    qsAvail.removeAll(list);
	    for (int i = 0; i<list.size(); i++)
	    {
	    	list.set(i, mapIDToString(Integer.parseInt(list.get(i))));
	    }
	    
	    adapter = new CustomArrayAdapter(getActivity(), R.layout.row_layout, R.id.text, list);
	    listView.setAdapter(adapter);
	    listView.setDropListener(onDrop);
	    listView.setRemoveListener(onRemove);

	    DragSortController controller = new DragSortController(listView);
	    controller.setDragHandleId(R.id.handler);
	    controller.setRemoveEnabled(true);
	    controller.setSortEnabled(true);
	    controller.setDragInitMode(1);

	    listView.setFloatViewManager(controller);
	    listView.setOnTouchListener(controller);
	    listView.setDragEnabled(true);
		    
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_save)
		{
			writeACC();
			return true;
		}
		if (item.getItemId() == R.id.menu_add)
		{
			openAddDialog();
			return true;
		}
		if (item.getItemId() == R.id.menu_restore)
		{
			restorePrev();
			return true;
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public void getCID() {
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
					return;
				}
			}
			cidXML = "default";
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
        return;
    }	
	
	protected String[] parseACC()
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
	
	protected void writeACC()
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
			     savePrev();
			     askForReboot();
			} 
			
		} catch (Exception e) {
			System.out.println("XML Parsing Excpetion = " + e);
		}
		
	}
	
	public static void removeAllChildren(Node node)
	{
	  for (Node child; (child = node.getFirstChild()) != null; node.removeChild(child));
	}
	
	public String getStringFromDoc(org.w3c.dom.Document doc)    {
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
	
	protected void alertbox(String title, String mymessage)
	{
		new AlertDialog.Builder(getActivity())
	    	.setMessage(mymessage)
	    	.setTitle(title)
	    	.setCancelable(true)
	    	.setNeutralButton(android.R.string.cancel,
	        new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton){}
	    	})
	    	.show();
	   }		
	
	protected void askForReboot()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle("Hot Reboot");
		alert.setMessage("Your order has been saved! A hot reboot is required for this to take effect. Would you like to do this now?");

		alert.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		     try {
				CommandCapture command = new CommandCapture(0, "setprop ctl.restart zygote");
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		  }
		});

		alert.setNegativeButton("No!", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
	}

	protected void openAddDialog()
	{
		final String[] items = qsAvail.toArray(new String[qsAvail.size()]);
		for (int i = 0; i<items.length;i++)
		{
			items[i] = mapIDToString(Integer.parseInt(items[i]));
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select tile to add");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		         adapter.insert(items[item], 0);
		         qsAvail.remove(mapStringToID(items[item]));
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	protected String mapStringToID(String name)
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
	
	protected String mapIDToString(int id)
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
	
	protected void restorePrev()
	{
		File file = new File(getActivity().getExternalFilesDir(null), "qsOrder");
		if (file != null) {
			String qss;
			try {
				qss = new Scanner(file).useDelimiter("\n").nextLine();
				if (!qss.isEmpty())
				{
					adapter.clear();
					adapter.addAll(qss.split(";;"));
					alertbox("Success", "Backup successfully restored! Tap on \"Save\" to apply the order.");
				}
			} catch (FileNotFoundException e) {
				alertbox("No backup found", "Sorry, no backup was found. Did you wipe your storage?");
			}
		}
	}
	
	protected void savePrev()
	{
		File file = new File(getActivity().getExternalFilesDir(null), "qsOrder");
		if (file != null) {
			StringBuilder sb = new StringBuilder(adapter.getItem(0));
			for (int i = 1; i < adapter.getCount();i++)
			{
				sb.append(";;" + adapter.getItem(i));
			}
			try {
				PrintStream ps = new PrintStream(new FileOutputStream(file));
				ps.print(sb.toString());
				if (ps != null) ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
		
	}

	private void showOverLay(){
		final Dialog dialog = new Dialog(ctx, android.R.style.Theme_Translucent_NoTitleBar);
		dialog.setContentView(R.layout.overlay_view);
		LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.overlayLayout);
		layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
}

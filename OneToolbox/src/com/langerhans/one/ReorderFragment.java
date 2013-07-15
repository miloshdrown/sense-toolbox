package com.langerhans.one;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.langerhans.one.utils.CustomArrayAdapter;
import com.langerhans.one.utils.Helpers;
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
			    qsAvail.add(Helpers.mapStringToID(adapter.getItem(which)));
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

		cidXML = Helpers.getCID();
		
		listView = (DragSortListView) getActivity().findViewById(R.id.listview);
	    String[] qsAvailH = getResources().getStringArray(R.array.availTiles);
	    qsAvail = new ArrayList<String>();
	    Collections.addAll(qsAvail, qsAvailH);
	    ArrayList<String> list = new ArrayList<String>(Arrays.asList(Helpers.parseACC(cidXML)));
	    qsAvail.removeAll(list);
	    for (int i = 0; i<list.size(); i++)
	    {
	    	list.set(i, Helpers.mapIDToString(Integer.parseInt(list.get(i))));
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
			Helpers.writeACC(cidXML, adapter);
		    savePrev();
		    askForReboot();
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
			items[i] = Helpers.mapIDToString(Integer.parseInt(items[i]));
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select tile to add");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		         adapter.insert(items[item], 0);
		         qsAvail.remove(Helpers.mapStringToID(items[item]));
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
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

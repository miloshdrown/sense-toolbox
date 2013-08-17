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
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.langerhans.one.dgv.DraggableGridView;
import com.langerhans.one.dgv.OnRearrangeListener;
import com.langerhans.one.utils.Helpers;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class ReorderFragment extends Fragment {

	DraggableGridView dgv;
	ArrayAdapter<String> adapter;
	DocumentBuilderFactory dbf;
	DocumentBuilder db;
	Document doc;
	Element eQS;
	String cidXML;
	ArrayList<String> qsAvail;
	Context ctx;
    String packagename;
	Resources res;
		
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
		SharedPreferences prefs = getActivity().getSharedPreferences("one_toolbox_prefs", 1); //1 = deprecated MODE_WORLD_READABLE
		
		//Add version string to bottom title
		try {
			TextView bottomTitle = (TextView) getActivity().findViewById(R.id.bottom_title);
			bottomTitle.setText(ctx.getString(R.string.app_name_version, ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName));
		} catch (NameNotFoundException e) {
			// Shouldn't happen...
			e.printStackTrace();
		}
		
		//First run handling. May be removed later.
		if (prefs.getBoolean("firstrun_reorder", true)) {
			showHelp();
            prefs.edit().putBoolean("firstrun_reorder", false).commit();
		}

		//Getting the CID for the right ACC file
		cidXML = Helpers.getCID();
		
		//Get the available tiles from the Resources
	    String[] qsAvailH = getResources().getStringArray(R.array.availTiles);
	    qsAvail = new ArrayList<String>();
	    Collections.addAll(qsAvail, qsAvailH);
	    
	    //Parse the ACC file and add the currently used tiles into a new list, then create an ArrayAdapter from it
	    ArrayList<String> used = new ArrayList<String>(Arrays.asList(Helpers.parseACC(cidXML)));
	    qsAvail.removeAll(used);
	    adapter = new ArrayAdapter<String>(ctx, R.layout.dummy_layout, R.id.invisible_text, used);
	    
	    //Some setup for later
	    dgv = ((DraggableGridView)getActivity().findViewById(R.id.dgv));
	    packagename = ctx.getPackageName();
		res = ctx.getResources();
		
		//Build the initial grid
		renewGrid();
	    
	    dgv.setOnRearrangeListener(new OnRearrangeListener(){
			@Override
			public void onRearrange(int from, int to) {
				if (from != to)
		        {
		            String item = adapter.getItem(from);
		            adapter.remove(item);
		            adapter.insert(item, to);
		        }
			}
	    });
	    
	    dgv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				if(adapter.getCount()<2)
		    	{
					Toast.makeText(ctx, "You can't remove the last item!", Toast.LENGTH_SHORT).show();
		    		return;
		    	}else
		    	{
					askForDelete(adapter.getItem(position), view);
		    	}
			}
	    	
	    });
    }
	
	/**
	 * Ask the user if he wants to delete the clicked EQS tile.
	 * @param item The tile ID to be removed
	 * @param view The grid child view to be removed
	 */
	private void askForDelete(final String item, final View view)
	{
		String tile = Helpers.mapIDToString(Integer.parseInt(item));
		AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
		adb.setTitle("Remove " + tile + "?");
		adb.setMessage("Do you want to remove the " + tile + " tile?\nYou can add it back via the menu on top.");
		adb.setCancelable(true);
		adb.setPositiveButton("Yes", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				adapter.remove(item);
				dgv.removeView(view);
			}
		});
		adb.setNegativeButton("No", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Do nothing
			}
		});
		AlertDialog removeDialog = adb.create();
		removeDialog.show();
	}
	
	/**
	 * Rebuild the grid view, e.g. after the adapter has been filled or a backup is restored
	 */
	private void renewGrid()
	{
		dgv.removeAllViews();
		for(int i = 0; i < adapter.getCount(); i++)
		{
			ImageView icon = new ImageView(ctx);
			icon.setImageDrawable(res.getDrawable(res.getIdentifier("qstile_" + adapter.getItem(i), "drawable", packagename)));
			dgv.addView(icon);
		}
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
	
	/**
	 * Simple helper to display an AlertDialog with an Ok button
	 * @param title Title of the dialog
	 * @param mymessage Message of the dialog
	 */
	protected void alertbox(String title, String mymessage)
	{
		new AlertDialog.Builder(getActivity())
	    	.setMessage(mymessage)
	    	.setTitle(title)
	    	.setCancelable(true)
	    	.setNeutralButton(android.R.string.ok,
	        new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton){}
	    	})
	    	.show();
	   }		
	
	/**
	 * Ask the user if he wants to do a hot reboot.
	 */
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

	/**
	 * Opens a dialog with a list of currently available tiles to add to the grid.
	 */
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
		    	 String id = Helpers.mapStringToID(items[item]);
		         adapter.add(id);
		         qsAvail.remove(id);
		         ImageView icon = new ImageView(ctx);
		         icon.setImageDrawable(res.getDrawable(res.getIdentifier("qstile_" + id, "drawable", packagename)));
		         dgv.addView(icon);
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	/**
	 * Restore the previous made backup, or inform the user that no backup exists
	 */
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
					for(String item : qss.split(";;")){
						adapter.add(item);
					}
					renewGrid();
					alertbox("Success", "Backup successfully restored! Tap on \"Save\" to apply the order.");
				}
			} catch (FileNotFoundException e) {
				alertbox("No backup found", "Sorry, no backup was found. Did you wipe your storage?");
			}
		}
	}
	
	/**
	 * Save the backup
	 */
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

	/**
	 * Shows a help dialog. Used at first start
	 */
	private void showHelp(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("First start");
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialoglayout = inflater.inflate(R.layout.first_start, (ViewGroup) getActivity().getCurrentFocus());
		builder.setView(dialoglayout);
		builder.setNeutralButton("Close dialog forever!", null);
		AlertDialog alert = builder.create();
		alert.show();
	}
}

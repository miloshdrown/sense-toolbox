package com.langerhans.one;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.langerhans.one.R;
import com.langerhans.one.utils.MyTabListener;
import com.stericson.RootTools.RootTools;

public class MainActivity extends Activity {

	public MainActivity() {
		// TODO Auto-generated constructor stub
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);  
		ActionBar ab = getActionBar();
		
		if (RootTools.isAccessGiven()) {
			ActionBar.Tab eqsTab = ab.newTab().setText("EQS Reorder");
			eqsTab.setTabListener(new MyTabListener<ReorderFragment>(this, "reorder", ReorderFragment.class));
			ab.addTab(eqsTab);
		}else
		{
			final SharedPreferences prefs = getSharedPreferences("one_toolbox_prefs", 1);
			if(prefs.getBoolean("show_root_note", true))
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setTitle("No root access");
	        	builder.setMessage("Either your device is not rooted, or you didn't allow the app to access SuperUser.\n"
	        			+ "Without root you can't reorder your EQS tiles, therefore the function is not shown.\n"
	        			+ "You can dismiss this message once, or forever. A check for root will be done on every start, "
	        			+ "to make the function available if you decide to allow root access later.");
	        	builder.setPositiveButton("Dismiss once", null);
	        	builder.setNegativeButton("Dismiss forever", new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						prefs.edit().putBoolean("show_root_note", false).commit();
					}     		
	        	});
	        	AlertDialog dlg = builder.create();
	        	dlg.show();
			}
		}
		
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
        ActionBar.Tab modsTab = ab.newTab().setText("Mods");
        modsTab.setTabListener(new MyTabListener<PrefsFragment>(this, "prefs", PrefsFragment.class));
	    ab.addTab(modsTab);
	    
    } 
	
	public void showEasterEgg(View view)
	{
		Toast.makeText(view.getContext(), "You can kill a man, but a man you will lose - Matt", Toast.LENGTH_SHORT).show();
	}
}

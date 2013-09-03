package com.langerhans.one;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
			ActionBar.Tab eqsTab = ab.newTab().setText(R.string.eqs_reorder);
			eqsTab.setTabListener(new MyTabListener<ReorderFragment>(this, "reorder", ReorderFragment.class));
			ab.addTab(eqsTab);
		}else
		{
			final SharedPreferences prefs = getSharedPreferences("one_toolbox_prefs", 1);
			if(prefs.getBoolean("show_root_note", true))
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setTitle(R.string.no_root_access);
	        	builder.setMessage(R.string.no_root_explain);
	        	builder.setPositiveButton(R.string.dismiss_once, null);
	        	builder.setNegativeButton(R.string.dismiss_forever, new OnClickListener(){
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
		
        ActionBar.Tab modsTab = ab.newTab().setText(R.string.mods);
        modsTab.setTabListener(new MyTabListener<PrefsFragment>(this, "prefs", PrefsFragment.class));
	    ab.addTab(modsTab);
    }
	
	public void showEasterEgg(View view)
	{
		Toast.makeText(view.getContext(), "You can kill a man, but a man you will lose - Matt", Toast.LENGTH_SHORT).show();
	}
}

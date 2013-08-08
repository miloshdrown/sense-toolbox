package com.langerhans.one;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.langerhans.one.R;
import com.langerhans.one.utils.MyTabListener;

public class MainActivity extends Activity {

	public MainActivity() {
		// TODO Auto-generated constructor stub
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);  
		ActionBar ab = getActionBar();
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ActionBar.Tab eqsTab = ab.newTab().setText("EQS Reorder");
        ActionBar.Tab modsTab = ab.newTab().setText("Mods");
                
        eqsTab.setTabListener(new MyTabListener<ReorderFragment>(this, "reorder", ReorderFragment.class));
        modsTab.setTabListener(new MyTabListener<PrefsFragment>(this, "prefs", PrefsFragment.class));
        
	    ab.addTab(eqsTab);
	    ab.addTab(modsTab);
	    
    } 
	
	public void showEasterEgg(View view)
	{
		Toast.makeText(view.getContext(), "You can kill a man, but a man you will lose - Matt", Toast.LENGTH_SHORT).show();
	}
}

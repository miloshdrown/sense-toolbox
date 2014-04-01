package com.langerhans.one;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarText;

public class AboutScreen extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_screen);
		
		Typeface face = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		 
		TextView iv2 = (TextView)findViewById(R.id.textView2);
		iv2.setPaintFlags(iv2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		iv2.setTypeface(face);
		TextView iv3 = (TextView)findViewById(R.id.textView3);
		iv3.setTypeface(face);
		
		TextView iv02 = (TextView)findViewById(R.id.TextView02);
		iv02.setPaintFlags(iv02.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		iv02.setTypeface(face);
		TextView iv03 = (TextView)findViewById(R.id.TextView03);
		iv03.setTypeface(face);
		
		TextView iv04 = (TextView)findViewById(R.id.TextView04);
		iv04.setPaintFlags(iv02.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		iv04.setTypeface(face);
		TextView iv4 = (TextView)findViewById(R.id.TextView4);
		iv4.setTypeface(face);
		TextView iv5 = (TextView)findViewById(R.id.TextView5);
		iv5.setTypeface(face);
		
		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
        ActionBarContainer actionBarContainer = actionBarExt.getCustomContainer();
        ActionBarText actionBarText = new ActionBarText(this);    		        
	    actionBarText.setPrimaryText(R.string.app_about);
	    actionBarContainer.addCenterView(actionBarText);

		actionBarContainer.setBackUpEnabled(true);
		
		actionBarContainer.setBackgroundColor(Color.BLACK);
		
        View homeBtn = actionBarContainer.getChildAt(0);
		if (homeBtn != null) {
			View.OnClickListener goBackFromEQS = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			};
			homeBtn.setOnClickListener(goBackFromEQS);
		}
		
		//Add version name
        try {
        	TextView versionTv = (TextView)findViewById(R.id.textViewVersion);
			versionTv.setText(getString(R.string.about_version, getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
			versionTv.setTypeface(face);
			versionTv.setPaintFlags(iv02.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			OnLongClickListener olcl = new OnLongClickListener(){
			    public boolean onLongClick(View v) {
			    	Intent intent = new Intent();
			        intent.setAction("com.langerhans.one.mods.action.StartEasterEgg");
			        sendBroadcast(intent);
			    	return true;
			    }
			};
			versionTv.setLongClickable(true);
			versionTv.setOnLongClickListener(olcl);
			ImageView logo = (ImageView)findViewById(R.id.imageView1);
			logo.setLongClickable(true);
			logo.setOnLongClickListener(olcl);
			TextView title1 = (TextView)findViewById(R.id.textView1);
			title1.setLongClickable(true);
			title1.setOnLongClickListener(olcl);
			TextView title2 = (TextView)findViewById(R.id.TextView01);
			title2.setLongClickable(true);
			title2.setOnLongClickListener(olcl);
		} catch (NameNotFoundException e) {
			//Shouldn't happen...
			e.printStackTrace();
		}
	}
}

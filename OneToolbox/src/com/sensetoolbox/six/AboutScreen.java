package com.sensetoolbox.six;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
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
import com.sensetoolbox.six.utils.Helpers;

public class AboutScreen extends Activity {
	int mThemeId = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Apply Settings theme
		mThemeId = Helpers.getCurrentTheme(this);
		setTheme(mThemeId);
		Helpers.setTranslucentStatusBar(this);
		
		setContentView(R.layout.about_screen);
		
		int bkgResId = getResources().getIdentifier("common_app_bkg", "drawable", "com.htc");
		findViewById(R.id.backLayer).setBackgroundResource(bkgResId);
		findViewById(R.id.scrollView1).setBackgroundResource(bkgResId);
		
		Typeface face = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
		
		TextView iv2 = (TextView)findViewById(R.id.textView2);
		iv2.setPaintFlags(iv2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		iv2.setTypeface(face);
		iv2.setText(Helpers.l10n(this, R.string.about_devs));
		TextView iv3 = (TextView)findViewById(R.id.textView3);
		iv3.setTypeface(face);
		iv3.setText(Helpers.l10n(this, R.string.about_devs_names));
		
		TextView iv02 = (TextView)findViewById(R.id.TextView02);
		iv02.setPaintFlags(iv02.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		iv02.setTypeface(face);
		iv02.setText(Helpers.l10n(this, R.string.about_thanks));
		TextView iv03 = (TextView)findViewById(R.id.TextView03);
		iv03.setTypeface(face);
		iv03.setText(Helpers.l10n(this, R.string.about_thanks_data));
		
		TextView iv04 = (TextView)findViewById(R.id.TextView04);
		iv04.setPaintFlags(iv02.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		iv04.setTypeface(face);
		iv04.setText(Helpers.l10n(this, R.string.about_l10n));
		TextView iv4 = (TextView)findViewById(R.id.TextView4);
		iv4.setTypeface(face);
		iv4.setText(Helpers.l10n(this, R.string.about_l10n_data_left));
		TextView iv5 = (TextView)findViewById(R.id.TextView5);
		iv5.setTypeface(face);
		iv5.setText(Helpers.l10n(this, R.string.about_l10n_data_right));
		
		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
		ActionBarContainer actionBarContainer = actionBarExt.getCustomContainer();
		ActionBarText actionBarText = new ActionBarText(this);
		actionBarText.setPrimaryText(Helpers.l10n(this, R.string.app_about));
		actionBarContainer.addCenterView(actionBarText);
		actionBarContainer.setBackUpEnabled(true);
		
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
			versionTv.setText(String.format(Helpers.l10n(this, R.string.about_version), getPackageManager().getPackageInfo(getPackageName(), 0).versionName, Helpers.buildVersion));
			versionTv.setTypeface(face);
			versionTv.setPaintFlags(iv02.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			OnLongClickListener olcl = new OnLongClickListener(){
				public boolean onLongClick(View v) {
					sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.StartEasterEgg"));
					return true;
				}
			};
			versionTv.setLongClickable(true);
			versionTv.setOnLongClickListener(olcl);
			ImageView logo = (ImageView)findViewById(R.id.imageView1);
			logo.setLongClickable(true);
			logo.setOnLongClickListener(olcl);
			logo.setImageDrawable(Helpers.applySenseTheme(this, logo.getDrawable()));
			logo.setContentDescription(Helpers.l10n(this, R.string.app_about));
		} catch (NameNotFoundException e) {
			//Shouldn't happen...
			e.printStackTrace();
		}
	}
	
	protected void onResume() {
		super.onResume();
		int newThemeId = Helpers.getCurrentTheme(this);
		if (newThemeId != mThemeId) recreate();
	}
}

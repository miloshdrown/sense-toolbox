package com.sensetoolbox.six.htc;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarText;
import com.sensetoolbox.six.BaseAboutScreen;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

public class HAboutScreen extends BaseAboutScreen {
	int mThemeId = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Apply Settings theme
		mThemeId = Helpers.getCurrentTheme(this);
		setTheme(mThemeId);
		Helpers.setTranslucentStatusBar(this);
		
		alphaTitle = 0.9f;
		alphaText = 0.8f;
		
		createViews();
		
		ImageView logo = (ImageView)findViewById(R.id.imageView1);
		logo.setImageDrawable(Helpers.applySenseTheme(this, logo.getDrawable()));
		
		int bkgResId = getResources().getIdentifier("common_app_bkg", "drawable", "com.htc");
		findViewById(R.id.backLayer).setBackgroundResource(bkgResId);
		findViewById(R.id.scrollView1).setBackgroundResource(bkgResId);
		
		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
		ActionBarContainer actionBarContainer = actionBarExt.getCustomContainer();
		ActionBarText actionBarText = new ActionBarText(this);
		actionBarText.setPrimaryText(Helpers.l10n(this, R.string.app_about));
		actionBarContainer.addCenterView(actionBarText);
		actionBarContainer.setBackUpEnabled(true);
		actionBarContainer.setBackUpOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	protected void onResume() {
		super.onResume();
		int newThemeId = Helpers.getCurrentTheme(this);
		if (newThemeId != mThemeId) recreate();
	}
}

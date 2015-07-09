package com.sensetoolbox.six.material;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import com.sensetoolbox.six.BaseAboutScreen;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

public class MAboutScreen extends BaseAboutScreen {
	public String mThemeHeaderName = null;
	public String mThemeAccentName = null;
	public int mThemeBackground = 1;
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mThemeHeaderName = Helpers.prefs.getString("pref_key_toolbox_material_header", null);
		if (mThemeHeaderName != null) getTheme().applyStyle(getResources().getIdentifier(mThemeHeaderName, "style", getPackageName()), true);
		mThemeAccentName = Helpers.prefs.getString("pref_key_toolbox_material_accent", null);
		if (mThemeAccentName != null) getTheme().applyStyle(getResources().getIdentifier(mThemeAccentName, "style", getPackageName()), true);
		mThemeBackground = Integer.parseInt(Helpers.prefs.getString("pref_key_toolbox_material_background", "1"));
		if (mThemeBackground == 2) getTheme().applyStyle(R.style.MaterialThemeDark, true);
		
		alphaTitle = 0.9f;
		alphaText = 0.8f;
		
		createViews();
		
		ImageView logo = (ImageView)findViewById(R.id.imageView1);
		if (mThemeBackground == 2)
			logo.setImageDrawable(Helpers.applyMaterialTheme(this, getResources().getDrawable(R.drawable.ic_logo_dark)));
		else
			logo.setImageDrawable(Helpers.applyMaterialTheme(this, logo.getDrawable()));
		
		getActionBar().setTitle(Helpers.l10n(this, R.string.app_about));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setElevation(0);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		String newThemeHeaderName = Helpers.prefs.getString("pref_key_toolbox_material_header", null);
		if (newThemeHeaderName != null && !newThemeHeaderName.equals(mThemeHeaderName)) recreate();
		String newThemeAccentName = Helpers.prefs.getString("pref_key_toolbox_material_accent", null);
		if (newThemeAccentName != null && !newThemeAccentName.equals(mThemeAccentName)) recreate();
		int newThemeBackground = Integer.parseInt(Helpers.prefs.getString("pref_key_toolbox_material_background", "1"));
		if (newThemeBackground != mThemeBackground) recreate();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

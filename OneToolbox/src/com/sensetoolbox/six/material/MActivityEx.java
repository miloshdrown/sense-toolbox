package com.sensetoolbox.six.material;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;
import com.sensetoolbox.six.utils.Version;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

public class MActivityEx extends Activity {
	public String mThemeHeaderName = null;
	public String mThemeAccentName = null;
	public int mThemeBackground = 1;
	public boolean launch = true;
	public boolean isActive = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		onCreate(savedInstanceState, R.layout.activity_template);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	protected void onCreate(Bundle savedInstanceState, int actLayout) {
		super.onCreate(savedInstanceState);
		
		Helpers.prefs = getSharedPreferences("one_toolbox_prefs", 1);
		
		mThemeHeaderName = Helpers.prefs.getString("pref_key_toolbox_material_header", null);
		if (mThemeHeaderName != null) getTheme().applyStyle(getResources().getIdentifier(mThemeHeaderName, "style", getPackageName()), true);
		mThemeAccentName = Helpers.prefs.getString("pref_key_toolbox_material_accent", null);
		if (mThemeAccentName != null) getTheme().applyStyle(getResources().getIdentifier(mThemeAccentName, "style", getPackageName()), true);
		mThemeBackground = Integer.parseInt(Helpers.prefs.getString("pref_key_toolbox_material_background", "1"));
		if (mThemeBackground == 2) getTheme().applyStyle(R.style.MaterialThemeDark, true);
		
		if (new Version(Helpers.getSenseVersion()).compareTo(new Version("6.0")) < 0) {
			launch = false;
			getActionBar().hide();
			
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(Helpers.l10n(this, R.string.warning));
			alert.setView(Helpers.createCenteredText(this, R.string.wrong_sense_version));
			alert.setCancelable(false);
			alert.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
				}
			});
			alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				}
			});
			alert.show();
			return;
		} else if (Helpers.isMalwareInstalled(this)) {
			Helpers.openURL(this, "http://sensetoolbox.com/copyright");
			
			launch = false;
			getActionBar().hide();
			finish();
			return;
		}
		
		getActionBar().setElevation(0);
		setContentView(actLayout);
	}

	public void updateTheme(int newBkg) {
		String newThemeHeaderName = Helpers.prefs.getString("pref_key_toolbox_material_header", null);
		if (newThemeHeaderName != null && !newThemeHeaderName.equals(mThemeHeaderName)) recreate();
		String newThemeAccentName = Helpers.prefs.getString("pref_key_toolbox_material_accent", null);
		if (newThemeAccentName != null && !newThemeAccentName.equals(mThemeAccentName)) recreate();
		int newThemeBackground = 1;
		if (newBkg == 0)
			newThemeBackground = Integer.parseInt(Helpers.prefs.getString("pref_key_toolbox_material_background", "1"));
		else
			newThemeBackground = newBkg;
		if (newThemeBackground != mThemeBackground) recreate();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;
		if (launch) updateTheme(0);
	}
	
	@Override
	protected void onPause() {
		super.onResume();
		isActive = false;
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

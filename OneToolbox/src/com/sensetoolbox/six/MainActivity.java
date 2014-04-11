package com.sensetoolbox.six;

import android.os.Bundle;

import com.htc.configuration.HtcWrapConfiguration;
import com.htc.preference.HtcPreferenceActivity;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarText;

public class MainActivity extends HtcPreferenceActivity {

	//public static boolean isRootAccessGiven = false;
	private int mThemeId = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Apply HTC's default theme
		mThemeId = HtcWrapConfiguration.getHtcThemeId(this, 0);
		setTheme(mThemeId);
		
		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
		ActionBarContainer actionBarContainer = actionBarExt.getCustomContainer();
		ActionBarText actionBarText = new ActionBarText(this);
		actionBarText.setPrimaryText(R.string.app_name);
		actionBarContainer.addCenterView(actionBarText);
		actionBarContainer.setBackUpEnabled(false);
		
		/*
		if (RootTools.isAccessGiven()) {
			isRootAccessGiven = true;
		} else {
			final SharedPreferences prefs = getSharedPreferences("one_toolbox_prefs", 1);
			if(prefs.getBoolean("show_root_note", true))
			{
				HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(this);
				builder.setTitle(R.string.no_root_access);
				builder.setMessage(R.string.no_root_explain);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setPositiveButton(R.string.dismiss_once, null);
				builder.setNegativeButton(R.string.dismiss_forever, new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						prefs.edit().putBoolean("show_root_note", false).commit();
					}
				});
				HtcAlertDialog dlg = builder.create();
				dlg.show();
			}
		}
		*/
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }
	
	protected void onResume() {
		super.onResume();
		int newThemeId = HtcWrapConfiguration.getHtcThemeId(this, 0);
		if (newThemeId != mThemeId) setTheme(newThemeId);
	}
}

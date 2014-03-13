package com.langerhans.one;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.htc.preference.HtcPreferenceActivity;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarText;
import com.htc.widget.HtcAlertDialog;
import com.langerhans.one.utils.Helpers;
import com.langerhans.one.utils.Version;
import com.stericson.RootTools.RootTools;

public class MainActivity extends HtcPreferenceActivity {

	public static boolean isRootAccessGiven = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
		ActionBarContainer actionBarContainer = actionBarExt.getCustomContainer();
		ActionBarText actionBarText = new ActionBarText(this);    		        
		actionBarText.setPrimaryText(R.string.app_name);
		actionBarContainer.addCenterView(actionBarText);

		actionBarExt.enableHTCLandscape(false);
		actionBarExt.setShowHideAnimationEnabled(true);
		actionBarContainer.setRightDividerEnabled(true);
		actionBarContainer.setBackUpEnabled(false);

		if ((new Version(Helpers.getSenseVersion())).compareTo(new Version("5.5")) < 0)
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
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }
}

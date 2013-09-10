package com.langerhans.one;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.htc.preference.HtcPreferenceActivity;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarText;
import com.htc.widget.HtcAlertDialog;
import com.stericson.RootTools.RootTools;

public class MainActivity extends HtcPreferenceActivity {

	public static boolean isRootAccessGiven = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
		actionBarExt.setShowHideAnimationEnabled(true);
        actionBarExt.enableHTCLandscape(false);
        ActionBarContainer actionBarContainer = actionBarExt.getCustomContainer();
        ActionBarText actionBarText = new ActionBarText(this);    		        
	    actionBarText.setPrimaryText(R.string.app_name);
	    //ImageView logo = new ImageView(actionBarContainer.getContext());
	    //logo.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
	    //actionBarText.addView(logo);
	    actionBarText.setLogoDrawable(getResources().getDrawable(R.drawable.ic_launcher));
	    actionBarContainer.addCenterView(actionBarText);
		actionBarContainer.setRightDividerEnabled(true);
		actionBarContainer.setBackUpEnabled(false);

		if (RootTools.isAccessGiven()) {
			isRootAccessGiven = true;
		} else {
			final SharedPreferences prefs = getSharedPreferences("one_toolbox_prefs", 1);
			if(prefs.getBoolean("show_root_note", true))
			{
				HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(this);
				builder.setTitle(R.string.no_root_access);
				builder.setMessage(R.string.no_root_explain);
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

	public void showEasterEgg(View view)
	{
		Toast.makeText(view.getContext(), "You can kill a man, but a man you will lose - Matt", Toast.LENGTH_SHORT).show();
	}
}

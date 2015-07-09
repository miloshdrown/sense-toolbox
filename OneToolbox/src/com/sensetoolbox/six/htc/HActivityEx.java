package com.sensetoolbox.six.htc;

import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarItemView;
import com.htc.widget.ActionBarText;
import com.htc.widget.HtcAlertDialog;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;
import com.sensetoolbox.six.utils.Version;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class HActivityEx extends Activity {
	private int mThemeId = 0;
	public boolean launch = true;
	public ActionBarContainer actionBarContainer;
	ActionBarText actionBarTextMain;
	ActionBarItemView actionBarBackBtn;
	public boolean isActive = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		onCreate(savedInstanceState, R.layout.activity_template);
	}
	
	protected void onCreate(Bundle savedInstanceState, int actLayout) {
		super.onCreate(savedInstanceState);
		Helpers.prefs = getSharedPreferences("one_toolbox_prefs", 1);
		
		if (new Version(Helpers.getSenseVersion()).compareTo(new Version("6.0")) < 0) {
			launch = false;
			getActionBar().hide();
			
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(this);
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
		} else if (isMalwareInstalled()) {
			Helpers.openURL(this, "http://sensetoolbox.com/copyright");
			
			launch = false;
			getActionBar().hide();
			finish();
			return;
		}
		
		// Apply Settings theme
		mThemeId = Helpers.getCurrentTheme(this);
		setTheme(mThemeId);
		Helpers.setTranslucentStatusBar(this);
		
		// Setup HTC action bar
		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
		actionBarContainer = actionBarExt.getCustomContainer();
		actionBarTextMain = new ActionBarText(this);
		
		RelativeLayout.LayoutParams mainlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		actionBarTextMain.setLayoutParams(mainlp);
		actionBarContainer.addCenterView(actionBarTextMain);
		actionBarContainer.setBackUpEnabled(false);
		
		setContentView(actLayout);
	}
	
	private boolean isMalwareInstalled() {
		PackageManager pm = getPackageManager();
		try {
			pm.getPackageInfo("richmondouk.xtended.settings", PackageManager.GET_ACTIVITIES);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;
		if (launch) {
			int newThemeId = Helpers.getCurrentTheme(this);
			if (newThemeId != mThemeId) recreate();
		}
	}
	
	@Override
	protected void onPause() {
		super.onResume();
		isActive = false;
		if (launch) {
			int newThemeId = Helpers.getCurrentTheme(this);
			if (newThemeId != mThemeId) recreate();
		}
	}
}

package com.sensetoolbox.six;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.htc.preference.HtcPreferenceActivity;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarItemView;
import com.htc.widget.ActionBarText;
import com.sensetoolbox.six.utils.Helpers;

public class MainActivity extends HtcPreferenceActivity {

	//public static boolean isRootAccessGiven = false;
	private int mThemeId = 0;
	public ActionBarContainer actionBarContainer;
	ActionBarText actionBarTextMain;
	ActionBarText actionBarTextSub;
	ActionBarItemView actionBarBackBtn;
	
	public void setActionBarText(String txt) {
		// Nice title crossfade
		Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		Animation fadeOutMain = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		Animation fadeOutSub = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		
		fadeOutMain.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				actionBarTextMain.setVisibility(View.INVISIBLE);
				actionBarBackBtn.setVisibility(View.VISIBLE);
			}
			public void onAnimationStart(Animation animation) {}
			public void onAnimationRepeat(Animation animation) {}
		});
		
		fadeOutSub.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				actionBarTextSub.setVisibility(View.INVISIBLE);
				actionBarBackBtn.setVisibility(View.INVISIBLE);
			}
			public void onAnimationStart(Animation animation) {}
			public void onAnimationRepeat(Animation animation) {}
		});
		
		if (txt == null) {
			fadeOutSub.setStartOffset(100);
			fadeOut.setStartOffset(100);
			fadeIn.setStartOffset(100);
			actionBarTextSub.startAnimation(fadeOutSub);
			actionBarBackBtn.startAnimation(fadeOut);
			actionBarTextMain.setVisibility(View.VISIBLE);
			actionBarTextMain.startAnimation(fadeIn);
		} else {
			fadeOutMain.setStartOffset(180);
			fadeIn.setStartOffset(180);
			actionBarTextSub.setPrimaryText(txt);
			actionBarTextMain.startAnimation(fadeOutMain);
			actionBarTextSub.setVisibility(View.VISIBLE);
			actionBarTextSub.startAnimation(fadeIn);
			actionBarBackBtn.startAnimation(fadeIn);
		}
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Apply Settings theme
		mThemeId = Helpers.getCurrentTheme(this);
		setTheme(mThemeId);
		Helpers.setTranslucentStatusBar(this);
		
		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
		actionBarContainer = actionBarExt.getCustomContainer();
		
		actionBarTextMain = new ActionBarText(this);
		actionBarTextMain.setPrimaryText(Helpers.l10n(this, R.string.app_name));
		actionBarTextSub = new ActionBarText(this);
		actionBarTextSub.setVisibility(View.INVISIBLE);
		//ImageView actionBarBackImg = new ImageView(this);
		//actionBarBackImg.setImageResource(getResources().getIdentifier("icon_btn_previous_dark", "drawable", "com.htc"));
		actionBarBackBtn = new ActionBarItemView(this);
		actionBarBackBtn.setIcon(getResources().getIdentifier("icon_btn_previous_dark", "drawable", "com.htc"));
		actionBarBackBtn.setVisibility(View.INVISIBLE);
		
		float density = getResources().getDisplayMetrics().density;
		RelativeLayout.LayoutParams mainlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		mainlp.setMargins(Math.round(density * 15) + 1, 0, 0, 0);
		actionBarTextMain.setLayoutParams(mainlp);
		RelativeLayout.LayoutParams sublp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		sublp.setMargins(Math.round(density * 45), 0, 0, 0);
		actionBarTextSub.setLayoutParams(sublp);
		RelativeLayout.LayoutParams backupbtnlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		actionBarBackBtn.setLayoutParams(backupbtnlp);
		actionBarBackBtn.setPadding(Math.round(density * 10), 0, Math.round(density * 10), 0);
		
		RelativeLayout titles = new RelativeLayout(this);
		titles.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		titles.addView(actionBarTextMain);
		titles.addView(actionBarBackBtn);
		titles.addView(actionBarTextSub);
		actionBarContainer.addLeftView(titles);
		actionBarContainer.setBackUpEnabled(false);
		/*
		if (RootTools.isAccessGiven()) {
			isRootAccessGiven = true;
		} else {
			final SharedPreferences prefs = getSharedPreferences("one_toolbox_prefs", 1);
			if(prefs.getBoolean("show_root_note", true))
			{
				HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(this);
				builder.setTitle(Helpers.l10n(this, R.string.no_root_access));
				builder.setMessage(Helpers.l10n(this, R.string.no_root_explain));
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setPositiveButton(Helpers.l10n(this, R.string.dismiss_once), null);
				builder.setNegativeButton(Helpers.l10n(this, R.string.dismiss_forever), new OnClickListener(){
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
		setContentView(R.layout.activity_main);
		getFragmentManager().beginTransaction().replace(R.id.fragment_container, new PrefsFragment()).commit();
		((FrameLayout)findViewById(R.id.fragment_container)).getChildAt(0).setBackgroundResource(android.R.color.background_light);
	}
	
	protected void onResume() {
		super.onResume();
		int newThemeId = Helpers.getCurrentTheme(this);
		if (newThemeId != mThemeId) recreate();
	}
}

package com.sensetoolbox.six;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
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
		ObjectAnimator fadeIn = (ObjectAnimator)AnimatorInflater.loadAnimator(this, R.animator.fade_in);
		ObjectAnimator fadeInBtn = (ObjectAnimator)AnimatorInflater.loadAnimator(this, R.animator.fade_in);
		ObjectAnimator fadeOutBtn = (ObjectAnimator)AnimatorInflater.loadAnimator(this, R.animator.fade_out);
		ObjectAnimator fadeOutMain = (ObjectAnimator)AnimatorInflater.loadAnimator(this, R.animator.fade_out);
		ObjectAnimator fadeOutSub = (ObjectAnimator)AnimatorInflater.loadAnimator(this, R.animator.fade_out);
		
		fadeOutMain.addListener(new AnimatorListener(){
			public void onAnimationStart(Animator animation) {
				actionBarBackBtn.setVisibility(View.VISIBLE);
			}
			public void onAnimationEnd(Animator animation) {
				actionBarTextMain.setVisibility(View.INVISIBLE);
			}
			public void onAnimationCancel(Animator animation) {}
			public void onAnimationRepeat(Animator animation) {}
		});
		
		fadeOutSub.addListener(new AnimatorListener() {
			public void onAnimationEnd(Animator animation) {
				actionBarTextSub.setVisibility(View.INVISIBLE);
				actionBarBackBtn.setVisibility(View.INVISIBLE);
			}
			public void onAnimationStart(Animator animation) {}
			public void onAnimationRepeat(Animator animation) {}
			public void onAnimationCancel(Animator animation) {}
		});
		
		if (txt == null) {
			fadeOutSub.setTarget(actionBarTextSub);
			fadeOutBtn.setTarget(actionBarBackBtn);
			fadeIn.setTarget(actionBarTextMain);
			
			actionBarTextMain.setVisibility(View.VISIBLE);
			fadeOutSub.start();
			fadeOutBtn.start();
			fadeIn.start();
		} else {
			fadeOutMain.setTarget(actionBarTextMain);
			fadeOutMain.setStartDelay(100);
			fadeIn.setTarget(actionBarTextSub);
			fadeIn.setStartDelay(100);
			fadeInBtn.setTarget(actionBarBackBtn);
			fadeInBtn.setStartDelay(100);
			
			actionBarTextSub.setPrimaryText(txt);
			actionBarTextSub.setAlpha(0);
			actionBarTextSub.setVisibility(View.VISIBLE);
			fadeOutMain.start();
			fadeIn.start();
			fadeInBtn.start();
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
		setContentView(R.layout.activity_main);
		
		getFragmentManager().beginTransaction().replace(R.id.fragment_container, new PrefsFragment()).commit();
		((FrameLayout)findViewById(R.id.fragment_container)).getChildAt(0).setBackgroundResource(getResources().getIdentifier("common_app_bkg", "drawable", "com.htc"));
	}
	
	protected void onResume() {
		super.onResume();
		int newThemeId = Helpers.getCurrentTheme(this);
		if (newThemeId != mThemeId) recreate();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Helpers.processResult(this, requestCode, resultCode, data);
	}
}

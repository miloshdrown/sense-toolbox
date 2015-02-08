package com.sensetoolbox.six;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarItemView;
import com.htc.widget.ActionBarText;
import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcToggleButtonLight;
import com.htc.widget.HtcToggleButtonLight.OnCheckedChangeListener;
import com.sensetoolbox.six.utils.Helpers;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TouchLock extends Activity {
	int mThemeId = 0;
	ActionBarItemView menuDefine;
	ActionBarItemView menuReboot;
	SharedPreferences prefs;
	HtcToggleButtonLight OnOffSwitch;
	LinearLayout gridBkg;
	TextView modHint;
	TextView hintSeq;
	List<String> sequence = new ArrayList<String>();
	boolean isRecording = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Apply Settings theme
		mThemeId = Helpers.getCurrentTheme(this);
		setTheme(mThemeId);
		Helpers.setTranslucentStatusBar(this);
				
		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
		ActionBarContainer actionBarContainer = actionBarExt.getCustomContainer();
		ActionBarText actionBarText = new ActionBarText(this);
		actionBarText.setPrimaryText(Helpers.l10n(this, R.string.various_touchlock_title));
		actionBarContainer.addCenterView(actionBarText);
		actionBarContainer.setBackUpEnabled(true);
		
		View homeBtn = actionBarContainer.getChildAt(0);
		if (homeBtn != null) {
			OnClickListener goBack = new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			};
			homeBtn.setOnClickListener(goBack);
		}
		
		OnOffSwitch = new HtcToggleButtonLight(this);
		OnOffSwitch.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		OnOffSwitch.setEnabled(true);
		actionBarContainer.addRightView(OnOffSwitch);
		
		menuDefine = new ActionBarItemView(this);
		menuDefine.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		menuDefine.setIcon(R.drawable.ic_menu_define_pattern);
		menuDefine.setLongClickable(true);
		menuDefine.setTitle(Helpers.l10n(this, R.string.define_lock_seq));
		menuDefine.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LinearLayout row1 = (LinearLayout)findViewById(R.id.row1);
				LinearLayout row2 = (LinearLayout)findViewById(R.id.row2);
				if (!isRecording) {
					isRecording = true;
					menuDefine.setIcon(R.drawable.ic_menu_check);
					row1.setAlpha(1.0f);
					row2.setAlpha(1.0f);
					sequence.clear();
				} else {
					isRecording = false;
					menuDefine.setIcon(R.drawable.ic_menu_define_pattern);
					row1.setAlpha(0.5f);
					row2.setAlpha(0.5f);
					prefs.edit().putString("touch_lock_sequence", TextUtils.join(",", sequence)).commit();
				}
				updateSequenceText();
			}
		});
		actionBarContainer.addRightView(menuDefine);
		
		menuReboot = new ActionBarItemView(this);
		menuReboot.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		menuReboot.setIcon(R.drawable.ic_menu_reboot_small);
		menuReboot.setLongClickable(true);
		menuReboot.setTitle(Helpers.l10n(this, R.string.soft_reboot));
		menuReboot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(TouchLock.this);
				alert.setTitle(Helpers.l10n(TouchLock.this, R.string.soft_reboot));
				alert.setView(Helpers.createCenteredText(TouchLock.this, R.string.hotreboot_explain_prefs));
				alert.setPositiveButton(Helpers.l10n(TouchLock.this, R.string.yes) + "!", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						try {
							CommandCapture command = new CommandCapture(0, "setprop ctl.restart zygote");
							RootTools.getShell(true).add(command);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				alert.setNegativeButton(Helpers.l10n(TouchLock.this, R.string.no) + "!", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
				});
				alert.show();
			}
		});
		actionBarContainer.addRightView(menuReboot);
		
		setContentView(R.layout.activity_touch_lock);
		prefs = getSharedPreferences("one_toolbox_prefs", 1);
		
		LinearLayout row1 = (LinearLayout)findViewById(R.id.row1);
		LinearLayout row2 = (LinearLayout)findViewById(R.id.row2);
		row1.setAlpha(0.5f);
		row2.setAlpha(0.5f);
		
		TextView hint = (TextView)findViewById(R.id.hint);
		hint.setText(Helpers.l10n(this, R.string.touchlock_hint));
		
		sequence = new ArrayList<String>(Arrays.asList(prefs.getString("touch_lock_sequence", "").split(",")));
		sequence.removeAll(Collections.singleton(null));
		sequence.removeAll(Collections.singleton(""));

		hintSeq = (TextView)findViewById(R.id.hintSeq);
		updateSequenceText();
		
		LinearLayout cell1 = (LinearLayout)findViewById(R.id.cell1);
		cell1.setOnTouchListener(otl);
		cell1.setTag("1");
		LinearLayout cell2 = (LinearLayout)findViewById(R.id.cell2);
		cell2.setOnTouchListener(otl);
		cell2.setTag("2");
		LinearLayout cell3 = (LinearLayout)findViewById(R.id.cell3);
		cell3.setOnTouchListener(otl);
		cell3.setTag("3");
		LinearLayout cell4 = (LinearLayout)findViewById(R.id.cell4);
		cell4.setOnTouchListener(otl);
		cell4.setTag("4");
				
		TextView cell1text = (TextView)findViewById(R.id.cell1txt);
		cell1text.setText("1");
		TextView cell2text = (TextView)findViewById(R.id.cell2txt);
		cell2text.setText("2");
		TextView cell3text = (TextView)findViewById(R.id.cell3txt);
		cell3text.setText("3");
		TextView cell4text = (TextView)findViewById(R.id.cell4txt);
		cell4text.setText("4");
		
		int backResId = getResources().getIdentifier("common_app_bkg", "drawable", "com.htc");
		
		modHint = (TextView)findViewById(R.id.modhint);
		modHint.setBackgroundResource(backResId);
		modHint.setText(Helpers.l10n(this, R.string.touchlock_hint_mod));
		
		TextView experimental = (TextView)findViewById(R.id.experimental);
		experimental.setText(Helpers.l10n(this, R.string.popupnotify_experimental));
		experimental.setTextColor(getResources().getColor(android.R.color.background_light));
		
		gridBkg = (LinearLayout)findViewById(R.id.gridBkg);
		
		boolean state = prefs.getBoolean("touch_lock_active", false);
		applyState(state);
		OnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(HtcToggleButtonLight toggle, boolean state) {
				prefs.edit().putBoolean("touch_lock_active", state).commit();
				applyState(state);
			}
		});
	}
	
	private void updateSequenceText() {
		if (sequence.isEmpty())
			hintSeq.setText(Helpers.l10n(this, R.string.touchlock_seq) + ": " + Helpers.l10n(this, R.string.touchlock_not_defined));
		else
			hintSeq.setText(Helpers.l10n(this, R.string.touchlock_seq) + ": " + TextUtils.join(" ", sequence));
	}
	
	private void applyState(boolean state) {
		gridBkg.setEnabled(state);
		OnOffSwitch.setChecked(state);
		menuDefine.setEnabled(state);
		if (state) {
			gridBkg.setVisibility(View.VISIBLE);
			modHint.setVisibility(View.GONE);
		} else {
			gridBkg.setVisibility(View.GONE);
			modHint.setVisibility(View.VISIBLE);
		}
	}
	
	View.OnTouchListener otl = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (isRecording && OnOffSwitch.isChecked())
			switch (event.getAction()) {
				case 0:
					v.setBackgroundColor(0xff434343);
					if (v.getTag() != null) {
						sequence.add((String)v.getTag());
						updateSequenceText();
						if (sequence.size() >= 12) {
							v.setBackgroundColor(0xff141414);
							menuDefine.callOnClick();
						}
					}
					break;
				case 1:
					v.setBackgroundColor(0xff141414);
					break;
			}
			v.performClick();
			return true;
		}
	};
	
	protected void onResume() {
		super.onResume();
		int newThemeId = Helpers.getCurrentTheme(this);
		if (newThemeId != mThemeId) recreate();
	}
}

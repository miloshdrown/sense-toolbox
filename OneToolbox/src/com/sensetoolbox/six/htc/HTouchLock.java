package com.sensetoolbox.six.htc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.htc.widget.ActionBarItemView;
import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcToggleButtonLight;
import com.htc.widget.HtcToggleButtonLight.OnCheckedChangeListener;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HTouchLock extends HActivityEx {
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
		super.onCreate(savedInstanceState, R.layout.activity_touch_lock);
		
		actionBarTextMain.setPrimaryText(Helpers.l10n(this, R.string.various_touchlock_title));
		actionBarContainer.setBackUpEnabled(true);
		actionBarContainer.setBackUpOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
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
				HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(HTouchLock.this);
				alert.setTitle(Helpers.l10n(HTouchLock.this, R.string.soft_reboot));
				alert.setView(Helpers.createCenteredText(HTouchLock.this, R.string.hotreboot_explain_prefs));
				alert.setPositiveButton(Helpers.l10n(HTouchLock.this, R.string.yes) + "!", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						try {
							Command command = new Command(0, false, "setprop ctl.restart zygote");
							RootTools.getShell(true).add(command);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				alert.setNegativeButton(Helpers.l10n(HTouchLock.this, R.string.no) + "!", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
				});
				alert.show();
			}
		});
		actionBarContainer.addRightView(menuReboot);
		
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
		experimental.setText(Helpers.l10n(this, R.string.touchlock_root_remind));
		experimental.setTextColor(getResources().getColor(android.R.color.background_light));
		
		FrameLayout experimentalFrame = (FrameLayout)findViewById(R.id.experimentalFrame);
		experimentalFrame.setVisibility(View.VISIBLE);
		
		gridBkg = (LinearLayout)findViewById(R.id.gridBkg);
		gridBkg.setBackgroundResource(backResId);
		
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
					v.setBackgroundColor(0xff888888);
					if (v.getTag() != null) {
						sequence.add((String)v.getTag());
						updateSequenceText();
						if (sequence.size() >= 12) {
							v.setBackgroundColor(0xff666666);
							menuDefine.callOnClick();
						}
					}
					break;
				case 1:
					v.setBackgroundColor(0xff666666);
					break;
			}
			v.performClick();
			return true;
		}
	};
}

package com.sensetoolbox.six.htc.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.htc.preference.HtcDialogPreference;
import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcCheckBox;
import com.htc.widget.HtcCompoundButton;
import com.htc.widget.HtcNumberPicker;
import com.htc.widget.HtcCompoundButton.OnCheckedChangeListener;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

public class TimeoutPicker extends HtcDialogPreference implements HtcNumberPicker.OnScrollIdleStateListener {
	private Context mContext;
	private float density = 3;
	private SharedPreferences prefs = null;
	private HtcNumberPicker timeoutPick1;
	private HtcNumberPicker timeoutPick2;
	private HtcNumberPicker timeoutPick3;
	private LinearLayout checkBoxContainer;
	private HtcCheckBox prefSwitch;
	private TextView prefSwitchText;
	private LinearLayout layout;
	private TextView desc;
	private int curVal1 = 7;
	private int curVal2 = 6;
	private int curVal3 = 4;
	String[] timeouts = new String[]{ "1 hr", "30 min", "10 min", "2 min", "1 min", "45 sec", "30 sec",  "15 sec" };
	
	public TimeoutPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		density = mContext.getResources().getDisplayMetrics().density;
		this.setDialogTitle(Helpers.l10n(context, this.getTitleRes()));
	}
	
	private int densify(int dimension) {
		return Math.round(density * dimension);
	}
	
	@Override
	protected View onCreateDialogView() {
		prefs = getPreferenceManager().getSharedPreferences();
		
		LinearLayout layout_main = new LinearLayout(mContext);
		layout_main.setOrientation(LinearLayout.VERTICAL);
		
		layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setPadding(densify(5), densify(5), 0, densify(10));

		checkBoxContainer = new LinearLayout(mContext);
		checkBoxContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		checkBoxContainer.setGravity(Gravity.CENTER);
		checkBoxContainer.setPadding(0, densify(4), 0, densify(4));
		
		prefSwitch = new HtcCheckBox(mContext);
		prefSwitch.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
		boolean isEnabled = getSharedPreferences().getBoolean("pref_key_sysui_timeoutqs", false);
		prefSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(HtcCompoundButton btn, boolean state) {
				if (layout != null && desc != null)
				if (state) {
					desc.setVisibility(View.VISIBLE);
					layout.setVisibility(View.VISIBLE);
				} else {
					desc.setVisibility(View.GONE);
					layout.setVisibility(View.GONE);
				}
				reapplyStates();
			}
		});
		
		prefSwitchText = new TextView(mContext);
		prefSwitchText.setText(Helpers.l10n(mContext, R.string.timeoutpicker_switch));
		prefSwitchText.setPadding(densify(5), 0, 0, densify(2));
		prefSwitchText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		prefSwitchText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				prefSwitch.toggle();
			}
		});
		
		checkBoxContainer.addView(prefSwitch);
		checkBoxContainer.addView(prefSwitchText);
		layout_main.addView(checkBoxContainer);
		
		desc = new TextView(mContext);
		desc.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		desc.setText(Helpers.l10n(mContext, R.string.timeoutpicker_desc));
		desc.setGravity(Gravity.CENTER);
		desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		desc.setPadding(0, 0, 0, densify(10));
		layout_main.addView(desc);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		lp.weight = 1.0f;
		lp.rightMargin = densify(5);
		
		curVal1 = prefs.getInt("pref_key_sysui_timeoutqs_value1", 7);
		curVal2 = prefs.getInt("pref_key_sysui_timeoutqs_value2", 6);
		curVal3 = prefs.getInt("pref_key_sysui_timeoutqs_value3", 4);
		
		timeoutPick1 = new HtcNumberPicker(mContext);
		timeoutPick1.setLayoutParams(lp);
		timeoutPick1.setTextStyle(R.style.numberPickerText);
		timeoutPick1.setRange(0, 7, timeouts);
		timeoutPick1.setCenterView(curVal1);
		timeoutPick1.setOnScrollIdleStateListener(this);
		layout.addView(timeoutPick1);
		
		timeoutPick2 = new HtcNumberPicker(mContext);
		timeoutPick2.setLayoutParams(lp);
		timeoutPick2.setTextStyle(R.style.numberPickerText);
		timeoutPick2.setRange(curVal1, 7, timeouts);
		timeoutPick2.setCenterView(curVal2);
		timeoutPick2.setOnScrollIdleStateListener(this);
		layout.addView(timeoutPick2);
		
		timeoutPick3 = new HtcNumberPicker(mContext);
		timeoutPick3.setLayoutParams(lp);
		timeoutPick3.setTextStyle(R.style.numberPickerText);
		timeoutPick3.setRange(curVal2, 7, timeouts);
		timeoutPick3.setCenterView(curVal3);
		timeoutPick3.setOnScrollIdleStateListener(this);
		layout.addView(timeoutPick3);
		
		layout_main.addView(layout);
		prefSwitch.setChecked(isEnabled);
		
		return layout_main;
	}

	private void getStates() {
		if (timeoutPick1 != null) curVal1 = timeoutPick1.getCenterView();
		if (timeoutPick2 != null) curVal2 = timeoutPick2.getCenterView();
		if (timeoutPick3 != null) curVal3 = timeoutPick3.getCenterView();
	}
	
	private void reapplyStates() {
		if (timeoutPick1 != null) timeoutPick1.setCenterView(curVal1);
		if (timeoutPick2 != null) timeoutPick2.setCenterView(curVal2);
		if (timeoutPick3 != null) timeoutPick3.setCenterView(curVal3);
	}
	
	private void changeLimits(HtcNumberPicker numberPicker, int newVal) {
		HtcNumberPicker nextPicker = null;
		if (numberPicker.equals(timeoutPick1)) nextPicker = timeoutPick2;
		else if (numberPicker.equals(timeoutPick2)) nextPicker = timeoutPick3;
		
		if (nextPicker != null) {
			int curVal = nextPicker.getCenterView();
			nextPicker.setRange(newVal, 7, timeouts);
			if (curVal < newVal) {
				nextPicker.setCenterView(newVal);
				changeLimits(nextPicker, newVal);
			} else {
				nextPicker.setCenterView(curVal);
				changeLimits(nextPicker, curVal);
			}
		}
	}
	
	@Override
	public void onDataSet(HtcNumberPicker numberPicker, int newVal) {
		changeLimits(numberPicker, newVal);
	}
	
	@Override
	protected void showDialog(Bundle bundle) {
		super.showDialog(bundle);
		final HtcAlertDialog dlg = (HtcAlertDialog)this.getDialog();
		Button pos = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
		pos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getStates();
				if (curVal1 == curVal2 || curVal2 == curVal3) {
					Toast.makeText(mContext, Helpers.l10n(mContext, R.string.timeoutpicker_warn_text), Toast.LENGTH_LONG).show();
				} else {
					prefs.edit().putBoolean("pref_key_sysui_timeoutqs", prefSwitch.isChecked()).commit();
					prefs.edit().putInt("pref_key_sysui_timeoutqs_value1", curVal1).commit();
					prefs.edit().putInt("pref_key_sysui_timeoutqs_value2", curVal2).commit();
					prefs.edit().putInt("pref_key_sysui_timeoutqs_value3", curVal3).commit();
					dlg.dismiss();
				}
			}
		});
		
		if (prefSwitch.isChecked()) {
			desc.setVisibility(View.VISIBLE);
			layout.setVisibility(View.VISIBLE);
		} else {
			desc.setVisibility(View.GONE);
			layout.setVisibility(View.GONE);
		}
		
		reapplyStates();
	}
}

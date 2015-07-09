package com.sensetoolbox.six.material.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

public class BrightnessPicker extends DialogPreference implements NumberPicker.OnValueChangeListener {
	private Context mContext;
	private float density = 3;
	private SharedPreferences prefs = null;
	private NumberPicker brightPick1;
	private NumberPicker brightPick2;
	private NumberPicker brightPick3;
	private NumberPicker brightPick4;
	private LinearLayout checkBoxContainer;
	private CheckBox prefSwitch;
	private TextView prefSwitchText;
	private LinearLayout layout;
	private TextView desc;
	private int curVal1 = 10;
	private int curVal2 = 40;
	private int curVal3 = 60;
	private int curVal4 = 100;
	
	public BrightnessPicker(Context context, AttributeSet attrs) {
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
		
		prefSwitch = new CheckBox(mContext);
		prefSwitch.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
		boolean isEnabled = getSharedPreferences().getBoolean("pref_key_sysui_brightqs", false);
		prefSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton btn, boolean state) {
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
		prefSwitchText.setText(Helpers.l10n(mContext, R.string.brightpicker_switch));
		prefSwitchText.setPadding(densify(5), 0, 0, densify(2));
		prefSwitchText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
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
		desc.setText(Helpers.l10n(mContext, R.string.brightpicker_desc));
		desc.setGravity(Gravity.CENTER);
		desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
		desc.setPadding(0, 0, 0, densify(10));
		layout_main.addView(desc);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		lp.weight = 1.0f;
		lp.rightMargin = densify(5);
		
		curVal1 = prefs.getInt("pref_key_sysui_brightqs_value1", 10);
		curVal2 = prefs.getInt("pref_key_sysui_brightqs_value2", 40);
		curVal3 = prefs.getInt("pref_key_sysui_brightqs_value3", 60);
		curVal4 = prefs.getInt("pref_key_sysui_brightqs_value4", 100);

		brightPick1 = new NumberPicker(mContext);
		brightPick1.setLayoutParams(lp);
		brightPick1.setMinValue(10);
		brightPick1.setMaxValue(100);
		brightPick1.setValue(curVal1);
		brightPick1.setOnValueChangedListener(this);
		layout.addView(brightPick1);
		Helpers.themeNumberPicker(brightPick1);
		
		brightPick2 = new NumberPicker(mContext);
		brightPick2.setLayoutParams(lp);
		brightPick2.setMinValue(curVal1);
		brightPick2.setMaxValue(100);
		brightPick2.setValue(curVal2);
		brightPick2.setOnValueChangedListener(this);
		layout.addView(brightPick2);
		Helpers.themeNumberPicker(brightPick2);
		
		brightPick3 = new NumberPicker(mContext);
		brightPick3.setLayoutParams(lp);
		brightPick3.setMinValue(curVal2);
		brightPick3.setMaxValue(100);
		brightPick3.setValue(curVal3);
		brightPick3.setOnValueChangedListener(this);
		layout.addView(brightPick3);
		Helpers.themeNumberPicker(brightPick3);
		
		brightPick4 = new NumberPicker(mContext);
		brightPick4.setLayoutParams(lp);
		brightPick4.setMinValue(curVal3);
		brightPick4.setMaxValue(100);
		brightPick4.setValue(curVal4);
		brightPick4.setOnValueChangedListener(this);
		layout.addView(brightPick4);
		Helpers.themeNumberPicker(brightPick4);
		
		layout_main.addView(layout);
		prefSwitch.setChecked(isEnabled);
		
		return layout_main;
	}

	private void getStates() {
		if (brightPick1 != null) curVal1 = brightPick1.getValue();
		if (brightPick2 != null) curVal2 = brightPick2.getValue();
		if (brightPick3 != null) curVal3 = brightPick3.getValue();
		if (brightPick4 != null) curVal4 = brightPick4.getValue();
	}
	
	private void reapplyStates() {
		if (brightPick1 != null) brightPick1.setValue(curVal1);
		if (brightPick2 != null) brightPick2.setValue(curVal2);
		if (brightPick3 != null) brightPick3.setValue(curVal3);
		if (brightPick4 != null) brightPick4.setValue(curVal4);
	}
	
	private void changeLimits(NumberPicker numberPicker, int newVal) {
		NumberPicker nextPicker = null;
		if (numberPicker.equals(brightPick1)) nextPicker = brightPick2;
		else if (numberPicker.equals(brightPick2)) nextPicker = brightPick3;
		else if (numberPicker.equals(brightPick3)) nextPicker = brightPick4;
		
		if (nextPicker != null) {
			int curVal = nextPicker.getValue();
			nextPicker.setMinValue(newVal);
			nextPicker.setMaxValue(100);
			if (curVal < newVal) {
				nextPicker.setValue(newVal);
				changeLimits(nextPicker, newVal);
			} else {
				nextPicker.setValue(curVal);
				changeLimits(nextPicker, curVal);
			}
		}
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		changeLimits(picker, newVal);
	}
	
	@Override
	protected void showDialog(Bundle bundle) {
		super.showDialog(bundle);
		final AlertDialog dlg = (AlertDialog)this.getDialog();
		Button pos = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
		pos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getStates();
				if (curVal1 == curVal2 || curVal2 == curVal3 || curVal3 == curVal4) {
					Toast.makeText(mContext, Helpers.l10n(mContext, R.string.brightpicker_warn_text), Toast.LENGTH_LONG).show();
				} else {
					prefs.edit().putBoolean("pref_key_sysui_brightqs", prefSwitch.isChecked()).commit();
					prefs.edit().putInt("pref_key_sysui_brightqs_value1", curVal1).commit();
					prefs.edit().putInt("pref_key_sysui_brightqs_value2", curVal2).commit();
					prefs.edit().putInt("pref_key_sysui_brightqs_value3", curVal3).commit();
					prefs.edit().putInt("pref_key_sysui_brightqs_value4", curVal4).commit();
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

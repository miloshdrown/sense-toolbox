package com.sensetoolbox.six.utils;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.htc.preference.HtcDialogPreference;
import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcCheckBox;
import com.htc.widget.HtcCompoundButton;
import com.htc.widget.HtcNumberPicker;
import com.htc.widget.HtcCompoundButton.OnCheckedChangeListener;
import com.sensetoolbox.six.R;

public class LEDTimePicker extends HtcDialogPreference {
	private Context mContext;
	private float density = 3;
	private SharedPreferences prefs = null;
	private HtcNumberPicker timeoutPick;
	private LinearLayout checkBoxContainer;
	private HtcCheckBox prefSwitch;
	private TextView prefSwitchText;
	private LinearLayout layout;
	ArrayList<String> timeouts = new ArrayList<String>();
	
	public LEDTimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		density = mContext.getResources().getDisplayMetrics().density;
		this.setDialogTitle(Helpers.l10n(context, this.getTitleRes()));
		for (int i = 60; i >= 1; i--) timeouts.add(String.valueOf(i));
		timeouts.add("\u221E");
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
		checkBoxContainer.setGravity(Gravity.CENTER_HORIZONTAL);
		checkBoxContainer.setPadding(0, densify(4), 0, densify(4));
		
		prefSwitch = new HtcCheckBox(mContext);
		prefSwitch.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
		boolean isEnabled = getSharedPreferences().getBoolean("pref_key_other_ledtimeout", false);
		prefSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(HtcCompoundButton btn, boolean state) {
				if (layout != null && state)
					layout.setVisibility(View.VISIBLE);
				else
					layout.setVisibility(View.GONE);
				
				timeoutPick.setCenterView(prefs.getInt("pref_key_other_ledtimeout_value", 5));
			}
		});
		
		prefSwitchText = new TextView(mContext);
		prefSwitchText.setText(Helpers.l10n(mContext, R.string.ledtimeoutpicker_switch));
		prefSwitchText.setPadding(densify(5), 0, 0, densify(2));
		prefSwitchText.setTextSize(20f);
		prefSwitchText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				prefSwitch.toggle();
			}
		});
		
		checkBoxContainer.addView(prefSwitch);
		checkBoxContainer.addView(prefSwitchText);
		layout_main.addView(checkBoxContainer);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		lp.weight = 1.0f;
		lp.rightMargin = densify(5);
		
		timeoutPick = new HtcNumberPicker(mContext);
		timeoutPick.setRange(0, 60, timeouts.toArray(new String[timeouts.size()]));
		timeoutPick.setCenterView(prefs.getInt("pref_key_other_ledtimeout_value", 5));
		layout.addView(timeoutPick);
		
		layout_main.addView(layout);
		prefSwitch.setChecked(isEnabled);
		
		return layout_main;
	}

	@Override
	protected void showDialog(Bundle bundle) {
		super.showDialog(bundle);
		final HtcAlertDialog dlg = (HtcAlertDialog)this.getDialog();
		Button pos = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
		pos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				prefs.edit().putBoolean("pref_key_other_ledtimeout", prefSwitch.isChecked()).commit();
				Log.e(null, String.valueOf(timeoutPick.getCenterView()));
				prefs.edit().putInt("pref_key_other_ledtimeout_value", timeoutPick.getCenterView()).commit();
				dlg.dismiss();
			}
		});
		
		if (prefSwitch.isChecked())
			layout.setVisibility(View.VISIBLE);
		else
			layout.setVisibility(View.GONE);
	}
}

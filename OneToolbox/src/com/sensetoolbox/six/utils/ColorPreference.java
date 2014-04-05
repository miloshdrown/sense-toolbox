package com.sensetoolbox.six.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.htc.preference.HtcDialogPreference;
import com.htc.preference.HtcPreferenceManager;
import com.htc.widget.HtcRimButton;
import com.htc.widget.HtcSeekBar;
import com.sensetoolbox.six.R;

public class ColorPreference extends HtcDialogPreference implements SeekBar.OnSeekBarChangeListener {
	
	private static final String androidns="http://schemas.android.com/apk/res/android";

	private HtcSeekBar hueSlider, satSlider, brightSlider;
	private TextView hueTitleVal, satTitleVal, brightTitleVal;
	private ImageView icon1, icon2, icon3, icon4, icon5, icon6, icon7, icon8, icon9, icon10; 
	private Context mContext;
	private String mKey;
	private int hueValue = 180;
	private int satValue = 100;
	private int brightValue = 100;
	private float density = 3;
	private SharedPreferences prefs = null;
	
	private void applyTheme(Drawable icon) {
		ColorFilter cf = GlobalActions.createColorFilter(false);
		icon.clearColorFilter();
		if (cf != null) icon.setColorFilter(cf);
	}

	public ColorPreference(Context context, AttributeSet attrs) { 
		super(context, attrs); 
		mContext = context;
		mKey = attrs.getAttributeValue(androidns, "key");
		density = mContext.getResources().getDisplayMetrics().density;
	}
	
	private int densify(int dimension) {
		return Math.round(density * dimension);	
	}
	
	@Override 
	protected View onCreateDialogView() {
		prefs = getPreferenceManager().getSharedPreferences();
		
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(densify(2), densify(6), densify(2), densify(12));
		
		RelativeLayout.LayoutParams paramsLeft = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		RelativeLayout.LayoutParams paramsRight = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		
		LinearLayout.LayoutParams paramsMatchWrap = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		// Preview icons
		LinearLayout iconsContainer = new LinearLayout(mContext);
		iconsContainer.setPadding(densify(16), densify(6), densify(16), densify(4));
		iconsContainer.setGravity(Gravity.CENTER);
		icon1 = new ImageView(mContext);
		icon1.setImageResource(R.drawable.cb_signal_preview);
		icon1.setPadding(0, 0, densify(7), 0);
		icon2 = new ImageView(mContext);
		icon2.setImageResource(R.drawable.stat_sys_ringer_silent);
		icon2.setPadding(0, 0, densify(7), 0);
		icon3 = new ImageView(mContext);
		icon3.setImageResource(R.drawable.stat_notify_alarm);
		icon3.setPadding(0, 0, densify(7), 0);
		icon4 = new ImageView(mContext);
		icon4.setImageResource(R.drawable.stat_sys_sync);
		icon4.setPadding(0, 0, densify(7), 0);
		icon5 = new ImageView(mContext);
		icon5.setImageResource(R.drawable.stat_sys_data_usb);
		icon5.setPadding(0, 0, densify(7), 0);
		icon6 = new ImageView(mContext);
		icon6.setImageResource(R.drawable.stat_sys_phone_call);
		icon6.setPadding(0, 0, densify(7), 0);
		icon7 = new ImageView(mContext);
		icon7.setImageResource(R.drawable.stat_notify_tv);
		icon7.setPadding(0, 0, densify(7), 0);
		icon8 = new ImageView(mContext);
		icon8.setImageResource(R.drawable.stat_sys_gps_acquiring);
		icon8.setPadding(0, 0, densify(7), 0);
		icon9 = new ImageView(mContext);
		icon9.setImageResource(R.drawable.stat_sys_headphones);
		icon9.setPadding(0, 0, densify(7), 0);
		icon10 = new ImageView(mContext);
		icon10.setImageResource(R.drawable.b_stat_sys_wifi_signal_4);
		icon10.setPadding(0, 0, densify(7), 0);
		
		iconsContainer.addView(icon1);
		iconsContainer.addView(icon2);
		iconsContainer.addView(icon3);
		iconsContainer.addView(icon4);
		iconsContainer.addView(icon5);
		iconsContainer.addView(icon6);
		iconsContainer.addView(icon7);
		iconsContainer.addView(icon8);
		iconsContainer.addView(icon9);
		iconsContainer.addView(icon10);
		
		layout.addView(iconsContainer);
		
		View divider = new View(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 2);
		params.setMargins(densify(16), densify(7), densify(16), densify(7));
		divider.setLayoutParams(params);
		divider.setPadding(0, 0, 0, 0);
		divider.setBackgroundColor(Color.rgb(200, 200, 200));
		layout.addView(divider);
		
		// Hue
		RelativeLayout hueContainer = new RelativeLayout(mContext);
		hueContainer.setLayoutParams(paramsMatchWrap);
		hueContainer.setPadding(0, densify(4), 0, densify(4));

		TextView hueTitle = new TextView(mContext);
		hueTitle.setText(R.string.cleanbeam_colortheme_hue);
		hueTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		hueTitle.setPadding(densify(16), densify(4), densify(4), densify(4));
		hueTitle.setLayoutParams(paramsLeft);
		
		hueTitleVal = new TextView(mContext);
		hueTitleVal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		hueTitleVal.setText("-180");
		hueTitleVal.setPadding(0, densify(4), densify(16), densify(4));
		RelativeLayout.LayoutParams paramsRightHue = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsRightHue.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		hueTitleVal.setLayoutParams(paramsRightHue);
		
		LinearLayout hueSliderContainer = new LinearLayout(mContext);
		hueSlider = new HtcSeekBar(mContext);
		hueSlider.setMax(360);
		hueSlider.setOnSeekBarChangeListener(this);
		hueValue = prefs.getInt(mKey + "_hueValue", 180);
		hueSlider.setProgress(hueValue);
		hueSlider.setLayoutParams(paramsMatchWrap);

		hueContainer.addView(hueTitle);
		hueContainer.addView(hueTitleVal);
		hueSliderContainer.addView(hueSlider);
		layout.addView(hueContainer);
		layout.addView(hueSliderContainer);
		
		// Saturation
		RelativeLayout satContainer = new RelativeLayout(mContext);
		satContainer.setLayoutParams(paramsMatchWrap);
		satContainer.setPadding(0, densify(4), 0, densify(4));

		TextView satTitle = new TextView(mContext);
		satTitle.setText(R.string.cleanbeam_colortheme_sat);
		satTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		satTitle.setPadding(densify(16), densify(4), densify(4), densify(4));
		satTitle.setLayoutParams(paramsLeft);
		satTitle.invalidate();
		
		satTitleVal = new TextView(mContext);
		satTitleVal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		satTitleVal.setText("-100");
		satTitleVal.setPadding(0, densify(4), densify(16), densify(4));
		RelativeLayout.LayoutParams paramsRightSat = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsRightSat.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		satTitleVal.setLayoutParams(paramsRightSat);
		
		LinearLayout satSliderContainer = new LinearLayout(mContext);
		satSlider = new HtcSeekBar(mContext);
		satSlider.setMax(200);
		satSlider.setOnSeekBarChangeListener(this);
		satValue = prefs.getInt(mKey + "_satValue", 100);
		satSlider.setProgress(satValue);
		satSlider.setLayoutParams(paramsMatchWrap);
		
		satContainer.addView(satTitle);
		satContainer.addView(satTitleVal);
		satSliderContainer.addView(satSlider);
		layout.addView(satContainer);
		layout.addView(satSliderContainer);
		
		// Brightness
		RelativeLayout brightContainer = new RelativeLayout(mContext);
		brightContainer.setLayoutParams(paramsMatchWrap);
		brightContainer.setPadding(0, densify(4), 0, densify(4));

		TextView brightTitle = new TextView(mContext);
		brightTitle.setText(R.string.cleanbeam_colortheme_bright);
		brightTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		brightTitle.setPadding(densify(16), densify(4), densify(4), densify(4));
		brightTitle.setLayoutParams(paramsLeft);
		
		brightTitleVal = new TextView(mContext);
		brightTitleVal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		brightTitleVal.setText("-100");
		brightTitleVal.setPadding(0, densify(4), densify(16), densify(4));
		RelativeLayout.LayoutParams paramsRightBright = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsRightBright.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		brightTitleVal.setLayoutParams(paramsRightBright);
		
		LinearLayout brightSliderContainer = new LinearLayout(mContext);
		brightSlider = new HtcSeekBar(mContext);
		brightSlider.setMax(200);
		brightSlider.setOnSeekBarChangeListener(this);
		brightValue = prefs.getInt(mKey + "_brightValue", 100);
		brightSlider.setProgress(brightValue);
		brightSlider.setLayoutParams(paramsMatchWrap);
		
		brightContainer.addView(brightTitle);
		brightContainer.addView(brightTitleVal);
		brightSliderContainer.addView(brightSlider);
		layout.addView(brightContainer);
		layout.addView(brightSliderContainer);
		
		View divider2 = new View(mContext);
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 2);
		params2.setMargins(densify(16), densify(7), densify(16), densify(7));
		divider2.setLayoutParams(params2);
		divider2.setPadding(0, 0, 0, 0);
		divider2.setBackgroundColor(Color.rgb(200, 200, 200));
		layout.addView(divider2);
		
		TextView presets = new TextView(mContext);
		presets.setText(R.string.cleanbeam_colortheme_preset);
		presets.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		presets.setPadding(densify(16), densify(4), densify(16), densify(4));
		layout.addView(presets);
		
		String[] presetsArray = mContext.getResources().getStringArray(R.array.theme_colors);
		LinearLayout presetsContainer = null;
		int col = 0;
		int cnt = 1;
		for (String colorName: presetsArray) {
			HtcRimButton btn = new HtcRimButton(mContext);
			btn.setText(colorName);
			LinearLayout.LayoutParams llparams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			llparams.rightMargin = densify(10);
			llparams.bottomMargin = densify(3);
			btn.setLayoutParams(llparams);
			btn.setTag(cnt);
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					switch ((Integer)v.getTag()) {
						case 1: brightSlider.setProgress(100); satSlider.setProgress(100); hueSlider.setProgress(180); break;
						case 2: brightSlider.setProgress(100); satSlider.setProgress(115); hueSlider.setProgress(90); break;
						case 3: brightSlider.setProgress(90); satSlider.setProgress(140); hueSlider.setProgress(340); break;
						case 4: brightSlider.setProgress(100); satSlider.setProgress(125); hueSlider.setProgress(5); break;
						case 5: brightSlider.setProgress(200); satSlider.setProgress(0); hueSlider.setProgress(180); break;
					}
				}
			});

			if (presetsContainer == null || col >= 3) {
				presetsContainer = new LinearLayout(mContext);
				presetsContainer.setPadding(densify(16), densify(3), densify(16), densify(3));
				presetsContainer.setLayoutParams(paramsMatchWrap);
				presetsContainer.addView(btn);
				layout.addView(presetsContainer);
				col = 0;
			} else {
				presetsContainer.addView(btn);				
			}
			col++;
			cnt++;
		}
		
		applyColors();
		return layout;
	}

	@Override
	public void onClick(DialogInterface paramDialogInterface, int paramInt) {
		super.onClick(paramDialogInterface, paramInt);
		if (paramInt == DialogInterface.BUTTON_POSITIVE) {
			prefs.edit().putInt(mKey + "_hueValue", hueValue).commit();
			prefs.edit().putInt(mKey + "_satValue", satValue).commit();
			prefs.edit().putInt(mKey + "_brightValue", brightValue).commit();
			applyThemes();
		}
	}
	
	public void applyThemes() {
		HtcPreferenceManager pm = getPreferenceManager();
		applyTheme(pm.findPreference("pref_key_cb_signal").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_data").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_headphone").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_profile").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_alarm").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_sync").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_gps").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_bt").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_screenshot").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_usb").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_powersave").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_nfc").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_mtp").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_dnd").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_phone").getIcon());
		applyTheme(pm.findPreference("pref_key_cb_tv").getIcon());
	}
	
	private void applyColors() {
		ColorFilter cf;
		if (brightValue == 200 && satValue == 0)
			cf = ColorFilterGenerator.adjustColor(brightValue - 100, 100, satValue - 100, -180);
		else
			cf = ColorFilterGenerator.adjustColor(brightValue - 100, 0, satValue - 100, hueValue - 180);
		
		if (icon1 != null) icon1.setColorFilter(cf);
		if (icon2 != null) icon2.setColorFilter(cf);
		if (icon3 != null) icon3.setColorFilter(cf);
		if (icon4 != null) icon4.setColorFilter(cf);
		if (icon5 != null) icon5.setColorFilter(cf);
		if (icon6 != null) icon6.setColorFilter(cf);
		if (icon7 != null) icon7.setColorFilter(cf);
		if (icon8 != null) icon8.setColorFilter(cf);
		if (icon9 != null) icon9.setColorFilter(cf);
		if (icon10 != null) icon10.setColorFilter(cf);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
    	hueValue = 180;
    	satValue = 100;
    	brightValue = 100;
	}

	@Override
	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		if (seek.equals(hueSlider)) {
			hueTitleVal.setText(String.valueOf(value - 180));
			hueValue = value;
		} else if (seek.equals(satSlider)) {
			satTitleVal.setText(String.valueOf(value - 100));
			satValue = value;
		} else if (seek.equals(brightSlider)) {
			brightTitleVal.setText(String.valueOf(value - 100));
			brightValue = value;
		}
		applyColors();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seek) {}
	
	@Override
	public void onStopTrackingTouch(SeekBar seek) {}
}

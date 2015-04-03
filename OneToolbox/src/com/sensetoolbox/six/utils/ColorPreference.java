package com.sensetoolbox.six.utils;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
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
import com.htc.preference.HtcPreference;
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
	private ArrayList<ImageView> icons;
	private ArrayList<Integer> icons_res;
	
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
		this.setDialogTitle(Helpers.l10n(context, this.getTitleRes()));
	}
	
	private int densify(int dimension) {
		return Math.round(density * dimension);
	}
	
	@Override
	protected View onCreateDialogView() {
		prefs = getPreferenceManager().getSharedPreferences();
		icons = new ArrayList<ImageView>();
		icons_res = new ArrayList<Integer>();
		
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
		icon2 = new ImageView(mContext);
		icon3 = new ImageView(mContext);
		icon4 = new ImageView(mContext);
		icon5 = new ImageView(mContext);
		icon6 = new ImageView(mContext);
		icon7 = new ImageView(mContext);
		icon8 = new ImageView(mContext);
		icon9 = new ImageView(mContext);
		icon10 = new ImageView(mContext);
		
		icons.add(icon1);
		icons.add(icon2);
		icons.add(icon3);
		icons.add(icon4);
		icons.add(icon5);
		icons.add(icon6);
		icons.add(icon7);
		icons.add(icon8);
		icons.add(icon9);
		icons.add(icon10);
		
		icons_res.add(R.drawable.cb_signal_preview);
		icons_res.add(R.drawable.stat_sys_ringer_silent);
		icons_res.add(R.drawable.stat_notify_alarm);
		icons_res.add(R.drawable.stat_sys_sync);
		icons_res.add(R.drawable.stat_sys_data_usb);
		icons_res.add(R.drawable.stat_sys_phone_call);
		icons_res.add(R.drawable.stat_notify_tv);
		icons_res.add(R.drawable.stat_sys_gps_acquiring);
		icons_res.add(R.drawable.stat_sys_headphones);
		icons_res.add(R.drawable.b_stat_sys_wifi_signal_4);
		
		for (ImageView icon: icons) iconsContainer.addView(icon);
		
		layout.addView(iconsContainer);
		
		View divider = new View(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 2);
		params.setMargins(densify(1), densify(7), densify(1), densify(7));
		divider.setLayoutParams(params);
		divider.setPadding(0, 0, 0, 0);
		divider.setBackgroundResource(mContext.getResources().getIdentifier("inset_list_divider", "drawable", "com.htc"));
		layout.addView(divider);
		
		// Hue
		RelativeLayout hueContainer = new RelativeLayout(mContext);
		hueContainer.setLayoutParams(paramsMatchWrap);
		hueContainer.setPadding(0, densify(4), 0, densify(4));

		TextView hueTitle = new TextView(mContext);
		hueTitle.setText(Helpers.l10n(mContext, R.string.cleanbeam_colortheme_hue));
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
		satTitle.setText(Helpers.l10n(mContext, R.string.cleanbeam_colortheme_sat));
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
		brightTitle.setText(Helpers.l10n(mContext, R.string.cleanbeam_colortheme_bright));
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
		params2.setMargins(densify(1), densify(16), densify(1), densify(7));
		divider2.setLayoutParams(params2);
		divider2.setPadding(0, 0, 0, 0);
		divider2.setBackgroundResource(mContext.getResources().getIdentifier("inset_list_divider", "drawable", "com.htc"));
		layout.addView(divider2);
		
		TextView presets = new TextView(mContext);
		presets.setText(Helpers.l10n(mContext, R.string.cleanbeam_colortheme_preset));
		presets.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		presets.setPadding(densify(16), densify(4), densify(16), densify(4));
		layout.addView(presets);
		
		TypedArray presetsArrayIds = mContext.getResources().obtainTypedArray(R.array.theme_colors);
		LinearLayout presetsContainer = null;
		int col = 0;
		int cnt = 1;
		for (int colorNameId = 0; colorNameId < presetsArrayIds.length(); colorNameId++) {
			HtcRimButton btn = new HtcRimButton(mContext);
			btn.setText(Helpers.l10n(mContext, presetsArrayIds.getResourceId(colorNameId, 0)));
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
		presetsArrayIds.recycle();
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
		
		if (Helpers.isLP()) {
			HtcPreference beats = pm.findPreference("pref_key_cb_beats");
			Drawable beats_icon = mContext.getResources().getDrawable(R.drawable.stat_sys_beats);
			beats.setIcon(Helpers.dropIconShadow(mContext, beats_icon));
			
			HtcPreference wifi = pm.findPreference("pref_key_cb_wifi");
			Drawable wifi_icon = mContext.getResources().getDrawable(R.drawable.b_stat_sys_wifi_signal_4);
			applyTheme(wifi_icon);
			wifi.setIcon(Helpers.dropIconShadow(mContext, wifi_icon));
		} else {
			HtcPreference mtp = pm.findPreference("pref_key_cb_mtp");
			Drawable mtp_icon = mContext.getResources().getDrawable(R.drawable.stat_notify_running_services);
			applyTheme(mtp_icon);
			mtp.setIcon(mtp_icon);
		}
		
		HtcPreference signal = pm.findPreference("pref_key_cb_signal");
		Drawable signal_icon = mContext.getResources().getDrawable(R.drawable.cb_signal_preview);
		applyTheme(signal_icon);
		signal.setIcon(Helpers.dropIconShadow(mContext, signal_icon));
		
		HtcPreference data = pm.findPreference("pref_key_cb_data");
		Drawable data_icon = mContext.getResources().getDrawable(R.drawable.cb_data_preview);
		applyTheme(data_icon);
		data.setIcon(Helpers.dropIconShadow(mContext, data_icon));
		
		HtcPreference headphone = pm.findPreference("pref_key_cb_headphone");
		Drawable headphone_icon = mContext.getResources().getDrawable(R.drawable.stat_sys_headphones);
		applyTheme(headphone_icon);
		headphone.setIcon(Helpers.dropIconShadow(mContext, headphone_icon));
		
		HtcPreference profile = pm.findPreference("pref_key_cb_profile");
		Drawable profile_icon = mContext.getResources().getDrawable(R.drawable.stat_sys_ringer_silent);
		applyTheme(profile_icon);
		profile.setIcon(Helpers.dropIconShadow(mContext, profile_icon));
		
		HtcPreference alarm = pm.findPreference("pref_key_cb_alarm");
		Drawable alarm_icon = mContext.getResources().getDrawable(R.drawable.stat_notify_alarm);
		applyTheme(alarm_icon);
		alarm.setIcon(Helpers.dropIconShadow(mContext, alarm_icon));
		
		HtcPreference sync = pm.findPreference("pref_key_cb_sync");
		Drawable sync_icon = mContext.getResources().getDrawable(R.drawable.stat_sys_sync);
		applyTheme(sync_icon);
		sync.setIcon(Helpers.dropIconShadow(mContext, sync_icon));
		
		HtcPreference gps = pm.findPreference("pref_key_cb_gps");
		Drawable gps_icon = mContext.getResources().getDrawable(R.drawable.stat_sys_gps_acquiring);
		applyTheme(gps_icon);
		gps.setIcon(Helpers.dropIconShadow(mContext, gps_icon));
		
		HtcPreference bt = pm.findPreference("pref_key_cb_bt");
		Drawable bt_icon = mContext.getResources().getDrawable(R.drawable.stat_sys_data_bluetooth_connected);
		applyTheme(bt_icon);
		bt.setIcon(Helpers.dropIconShadow(mContext, bt_icon));
		
		HtcPreference screenshot = pm.findPreference("pref_key_cb_screenshot");
		Drawable screenshot_icon = mContext.getResources().getDrawable(R.drawable.stat_notify_image);
		applyTheme(screenshot_icon);
		screenshot.setIcon(Helpers.dropIconShadow(mContext, screenshot_icon));
		
		HtcPreference usb = pm.findPreference("pref_key_cb_usb");
		Drawable usb_icon = mContext.getResources().getDrawable(R.drawable.stat_sys_data_usb);
		applyTheme(usb_icon);
		usb.setIcon(Helpers.dropIconShadow(mContext, usb_icon));
		
		HtcPreference powersave = pm.findPreference("pref_key_cb_powersave");
		Drawable powersave_icon = mContext.getResources().getDrawable(R.drawable.stat_notify_power_saver);
		applyTheme(powersave_icon);
		powersave.setIcon(Helpers.dropIconShadow(mContext, powersave_icon));
		
		HtcPreference nfc = pm.findPreference("pref_key_cb_nfc");
		Drawable nfc_icon = mContext.getResources().getDrawable(R.drawable.stat_sys_nfc_vzw);
		applyTheme(nfc_icon);
		nfc.setIcon(Helpers.dropIconShadow(mContext, nfc_icon));
		
		HtcPreference dnd = pm.findPreference("pref_key_cb_dnd");
		Drawable dnd_icon = mContext.getResources().getDrawable(R.drawable.stat_notify_dnd);
		applyTheme(dnd_icon);
		dnd.setIcon(Helpers.dropIconShadow(mContext, dnd_icon));
		
		HtcPreference phone = pm.findPreference("pref_key_cb_phone");
		Drawable phone_icon = mContext.getResources().getDrawable(R.drawable.stat_sys_phone_call);
		applyTheme(phone_icon);
		phone.setIcon(Helpers.dropIconShadow(mContext, phone_icon));
		
		HtcPreference tv = pm.findPreference("pref_key_cb_tv");
		Drawable tv_icon = mContext.getResources().getDrawable(R.drawable.stat_notify_tv);
		applyTheme(tv_icon);
		tv.setIcon(Helpers.dropIconShadow(mContext, tv_icon));
	}
	
	private void applyColors() {
		ColorFilter cf;
		if (brightValue == 200 && satValue == 0)
			cf = ColorFilterGenerator.adjustColor(brightValue - 100, 100, satValue - 100, -180);
		else
			cf = ColorFilterGenerator.adjustColor(brightValue - 100, 0, satValue - 100, hueValue - 180);
		
		int i = 0;
		for (ImageView icon: icons)
		if (icon != null) { icon.setImageResource(icons_res.get(i)); i++; icon.setColorFilter(cf); }
		
		if (brightValue == 200 && satValue == 0) {
			for (ImageView icon: icons) if (icon != null) {
				icon.setImageDrawable(Helpers.dropIconShadow(mContext, icon.getDrawable(), true));
				icon.setColorFilter(null);
				icon.setPadding(densify(1), 0, densify(1), 0);
			}
		} else {
			for (ImageView icon: icons) if (icon != null) icon.setPadding(densify(4), densify(3), densify(4), densify(3));
		}
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

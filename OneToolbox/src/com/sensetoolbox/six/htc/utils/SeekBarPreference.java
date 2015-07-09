package com.sensetoolbox.six.htc.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.htc.preference.HtcDialogPreference;
import com.htc.widget.HtcCheckBox;
import com.htc.widget.HtcCompoundButton;
import com.htc.widget.HtcCompoundButton.OnCheckedChangeListener;
import com.htc.widget.HtcRimButton;
import com.htc.widget.HtcSeekBar;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

public class SeekBarPreference extends HtcDialogPreference implements SeekBar.OnSeekBarChangeListener {
	private static final String androidns = "http://schemas.android.com/apk/res/android";
	private static final String toolboxns = "http://schemas.android.com/apk/res/com.sensetoolbox.six";

	private SeekBar mSeekBar;
	private HtcRimButton testBtn;
	private TextView mSplashText, mValueText;
	private Context mContext;
	private float density = 3;
	
	private String mDialogMessage, mDialogEnableMessage, mSuffix;
	private int mDefault, mAdd, mMax, mValue = 0;

	private LinearLayout mCheckBoxContainer;
	private TextView mEnableText;
	private HtcCheckBox mPrefSwitch;
	private String mKey;
	private String mEnableKey;
	private boolean mIsEnabled;
	private boolean mEnableValue;
	private boolean mHapticPref;
	private boolean mAnimPref;
	private int mSeekBarValue;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context,attrs);
		mContext = context;

		mDialogEnableMessage = (String) Helpers.l10n(context, attrs.getAttributeResourceValue(toolboxns, "dialogEnableMessage", R.string.transparency_enable));
		mDialogMessage = (String) Helpers.l10n(context, attrs.getAttributeResourceValue(androidns, "dialogMessage", R.string.transparency_msg));
		this.setDialogTitle(Helpers.l10n(context, this.getTitleRes()));
		mSuffix = attrs.getAttributeValue(androidns, "text");
		mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
		mMax = attrs.getAttributeIntValue(androidns, "max", 100);
		mAdd = attrs.getAttributeIntValue(toolboxns, "add", 0);
		mKey = attrs.getAttributeValue(androidns, "key");
		mEnableKey = attrs.getAttributeValue(toolboxns, "enableKey");
		mHapticPref = attrs.getAttributeBooleanValue(toolboxns, "hapticPref", false);
		mAnimPref = attrs.getAttributeBooleanValue(toolboxns, "animPref", false);
		density = mContext.getResources().getDisplayMetrics().density;
	}
	
	private int densify(int dimension) {
		return Math.round(density * dimension);
	}

	@Override
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(densify(2), densify(2), densify(2), densify(2));
		
		final LinearLayout innerLayout = new LinearLayout(mContext);
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		innerLayout.setPadding(densify(2), densify(2), densify(2), densify(6));
		
		mCheckBoxContainer = new LinearLayout(mContext);
		mCheckBoxContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		mCheckBoxContainer.setGravity(Gravity.CENTER_HORIZONTAL);
		mCheckBoxContainer.setPadding(0, densify(4), 0, densify(4));
		
		mPrefSwitch = new HtcCheckBox(getContext());
		mPrefSwitch.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
		
		mIsEnabled = getSharedPreferences().getBoolean(mEnableKey, false);
		
		mPrefSwitch.setChecked(!mIsEnabled); //To trigger the listener on next pref read
		mPrefSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(HtcCompoundButton arg0, boolean arg1) {
				if (arg1)
					innerLayout.setVisibility(View.VISIBLE);
				else
					innerLayout.setVisibility(View.GONE);
				mEnableValue = arg1;
			}
		});
		mPrefSwitch.setChecked(mIsEnabled);
		
		mEnableText = new TextView(mContext);
		mEnableText.setText(mDialogEnableMessage);
		mEnableText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		mEnableText.setPadding(densify(5), 0, 0, densify(2));
		mEnableText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPrefSwitch.toggle();
			}
		});
		
		mCheckBoxContainer.addView(mPrefSwitch);
		mCheckBoxContainer.addView(mEnableText);
		layout.addView(mCheckBoxContainer);
		
		mSplashText = new TextView(mContext);
		if (mDialogMessage != null) {
			mSplashText.setText(mDialogMessage);
			mSplashText.setGravity(Gravity.CENTER_HORIZONTAL);
			mSplashText.setPadding(densify(2), densify(2), densify(6), densify(3));
		}
		innerLayout.addView(mSplashText);
		
		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
		params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		innerLayout.addView(mValueText, params);
		
		mSeekBar = new HtcSeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		innerLayout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		if (shouldPersist()) mValue = getPersistedInt(mDefault);
		
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
		mValueText.setText(mSuffix == null ? String.valueOf(mValue + mAdd) : String.valueOf(mValue + mAdd).concat(mSuffix));
		
		if (mHapticPref) {
			FrameLayout btnFrame = new FrameLayout(mContext);
			btnFrame.setPadding(densify(16), densify(12), densify(16), densify(3));
			
			testBtn = new HtcRimButton(mContext);
			testBtn.setText(Helpers.l10n(mContext, R.string.controls_keyshaptic_testbtn));
			testBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Vibrator vibe = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
					if (Helpers.isLP())
						vibe.vibrate(mSeekBarValue);
					else if (mKey.equals("pref_key_controls_keyshaptic"))
						vibe.vibrate(new long[] { 0, mSeekBarValue, 0, 0 }, -1);
					else if (mKey.equals("pref_key_controls_longpresshaptic"))
						vibe.vibrate(new long[] { 0, 1, 20, mSeekBarValue }, -1);
					else if (mKey.equals("pref_key_controls_keyboardhaptic"))
						vibe.vibrate(mSeekBarValue);
				}
			});
			if (mValue == 0)
				testBtn.setVisibility(View.GONE);
			else
				testBtn.setVisibility(View.VISIBLE);
			btnFrame.addView(testBtn, new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			
			innerLayout.addView(btnFrame, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		}
		
		layout.addView(innerLayout);
		return layout;
	}

	@Override
	public void onClick(DialogInterface paramDialogInterface, int paramInt) {
		super.onClick(paramDialogInterface, paramInt);
		if (paramInt == DialogInterface.BUTTON_POSITIVE && shouldPersist()) {
			persistInt(mSeekBarValue);
			getEditor().putBoolean(mEnableKey, mEnableValue).commit();
			callChangeListener(Integer.valueOf(mSeekBarValue));
			if (mAnimPref) mContext.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.UpdateAnimDuration"));
		}
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
	}
	
	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
		else
			mValue = (Integer)defaultValue;
	}

	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		int newVal = value;
		if (mHapticPref) {
			newVal = newVal / 5;
			newVal = newVal * 5;
			if (testBtn != null)
			if (newVal == 0)
				testBtn.setVisibility(View.GONE);
			else
				testBtn.setVisibility(View.VISIBLE);
		} else if (mAnimPref) {
			newVal = newVal / 50;
			newVal = newVal * 50;
		}
		String t = String.valueOf(newVal + mAdd);
		mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
		mSeekBarValue = newVal;
	}
	public void onStartTrackingTouch(SeekBar seek) {}
	public void onStopTrackingTouch(SeekBar seek) {}

	public void setMax(int max) { mMax = max; }
	public int getMax() { return mMax; }

	public void setProgress(int progress) {
		mValue = progress;
		if (mSeekBar != null) mSeekBar.setProgress(progress);
	}
	public int getProgress() { return mValue; }
}
package com.sensetoolbox.six.utils;

/* The following code was written by Matthew Wiggins 
 * and is released under the APACHE 2.0 license 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.htc.preference.HtcDialogPreference;
import com.htc.widget.HtcCheckBox;
import com.htc.widget.HtcCompoundButton;
import com.htc.widget.HtcCompoundButton.OnCheckedChangeListener;
import com.htc.widget.HtcSeekBar;
import com.sensetoolbox.six.R;


public class SeekBarPreference extends HtcDialogPreference implements SeekBar.OnSeekBarChangeListener
{
  private static final String androidns="http://schemas.android.com/apk/res/android";
  private static final String toolboxns="http://schemas.android.com/apk/res/com.sensetoolbox.six";

  private SeekBar mSeekBar;
  private TextView mSplashText,mValueText;
  private Context mContext;

  private String mDialogMessage, mSuffix;
  private int mDefault, mMax, mValue = 0;

private LinearLayout mCheckBoxContainer;

private TextView mEnableText;

private HtcCheckBox mPrefSwitch;
private String mEnableKey;
private boolean mIsEnabled;
private int mSeekBarValue;
private boolean mEnableValue;

  public SeekBarPreference(Context context, AttributeSet attrs) { 
    super(context,attrs); 
    mContext = context;

    mDialogMessage = (String) context.getResources().getText(attrs.getAttributeResourceValue(androidns,"dialogMessage",R.string.transparency_msg));
    mSuffix = attrs.getAttributeValue(androidns,"text");
    mDefault = attrs.getAttributeIntValue(androidns,"defaultValue", 0);
    mMax = attrs.getAttributeIntValue(androidns,"max", 100);
    mEnableKey = attrs.getAttributeValue(toolboxns, "enableKey");
  }
   
  @Override 
  protected View onCreateDialogView() {
    LinearLayout.LayoutParams params;
    LinearLayout layout = new LinearLayout(mContext);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(6,6,6,6);
    
    final LinearLayout innerLayout = new LinearLayout(mContext);
    innerLayout.setOrientation(LinearLayout.VERTICAL);
    innerLayout.setPadding(6,6,6,6);

    mCheckBoxContainer = new LinearLayout(mContext);
    mCheckBoxContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    mCheckBoxContainer.setGravity(Gravity.CENTER_HORIZONTAL);
    mCheckBoxContainer.setPadding(0, 12, 0, 12);
     
    mPrefSwitch = new HtcCheckBox(getContext());
    mPrefSwitch.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    
    mIsEnabled = getSharedPreferences().getBoolean(mEnableKey, false);
    
    mPrefSwitch.setChecked(!mIsEnabled); //To trigger the listener on next pref read
    mPrefSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(HtcCompoundButton arg0, boolean arg1) {
			if(arg1)
			{
				innerLayout.setVisibility(View.VISIBLE);
			}
			else
			{
				innerLayout.setVisibility(View.GONE);
			}
			mEnableValue = arg1;
		}
	});
    mPrefSwitch.setChecked(mIsEnabled);
    
    mEnableText = new TextView(mContext);
    mEnableText.setText(mContext.getResources().getText(R.string.transparency_enable));
    mEnableText.setTextSize(20);
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
    if (mDialogMessage != null)
    {
        mSplashText.setText(mDialogMessage);
        mSplashText.setGravity(Gravity.CENTER_HORIZONTAL);
        mSplashText.setPadding(5, 5, 25, 10);
    }
    innerLayout.addView(mSplashText);
    
    mValueText = new TextView(mContext);
    mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
    mValueText.setTextSize(32);
    params = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, 
        LinearLayout.LayoutParams.WRAP_CONTENT);
    innerLayout.addView(mValueText, params);

    mSeekBar = new HtcSeekBar(mContext);
    mSeekBar.setOnSeekBarChangeListener(this);
    innerLayout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

    if (shouldPersist())
      mValue = getPersistedInt(mDefault);

    mSeekBar.setMax(mMax);
    mSeekBar.setProgress(mValue);
    
    layout.addView(innerLayout);
    return layout;
  }
  
  @Override
  public void onClick(DialogInterface paramDialogInterface, int paramInt)
  {
    super.onClick(paramDialogInterface, paramInt);
    if(paramInt == DialogInterface.BUTTON_POSITIVE && shouldPersist())
    {
        persistInt(mSeekBarValue);
        getEditor().putBoolean(mEnableKey, mEnableValue).commit();
        callChangeListener(Integer.valueOf(mSeekBarValue));
    }
  }
  
  @Override 
  protected void onBindDialogView(View v) {
    super.onBindDialogView(v);
    mSeekBar.setMax(mMax);
    mSeekBar.setProgress(mValue);
  }
  @Override
  protected void onSetInitialValue(boolean restore, Object defaultValue)  
  {
    super.onSetInitialValue(restore, defaultValue);
    if (restore) 
      mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
    else 
      mValue = (Integer)defaultValue;
  }

  public void onProgressChanged(SeekBar seek, int value, boolean fromTouch)
  {
    String t = String.valueOf(value);
    mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
    mSeekBarValue = value;
  }
  public void onStartTrackingTouch(SeekBar seek) {}
  public void onStopTrackingTouch(SeekBar seek) {}

  public void setMax(int max) { mMax = max; }
  public int getMax() { return mMax; }

  public void setProgress(int progress) { 
    mValue = progress;
    if (mSeekBar != null)
      mSeekBar.setProgress(progress); 
  }
  public int getProgress() { return mValue; }
}
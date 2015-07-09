package com.sensetoolbox.six.htc.utils;

import android.content.Context;
import android.util.AttributeSet;

import com.htc.preference.HtcListPreference;

public class HtcListPreferenceEx extends HtcListPreference {

	public HtcListPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HtcListPreferenceEx(Context context) {
		super(context);
	}

	public void show() {
		showDialog(null);
	}
}
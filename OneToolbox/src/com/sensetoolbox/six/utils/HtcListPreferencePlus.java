package com.sensetoolbox.six.utils;

import android.content.Context;
import android.util.AttributeSet;

import com.htc.preference.HtcListPreference;

public class HtcListPreferencePlus extends HtcListPreference {

	public HtcListPreferencePlus(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HtcListPreferencePlus(Context context) {
		super(context);
	}

	public void show() {
		showDialog(null);
	}
}
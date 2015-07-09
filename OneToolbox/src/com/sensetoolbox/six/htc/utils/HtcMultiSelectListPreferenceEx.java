package com.sensetoolbox.six.htc.utils;

import android.content.Context;
import android.util.AttributeSet;

import com.htc.preference.HtcMultiSelectListPreference;

public class HtcMultiSelectListPreferenceEx extends HtcMultiSelectListPreference {
	
	public HtcMultiSelectListPreferenceEx(Context context) {
		super(context);
	}
	
	public HtcMultiSelectListPreferenceEx(Context context, AttributeSet attrSet) {
		super(context, attrSet);
	}

	public void show() {
		this.showDialog(null);
	}
}

package com.sensetoolbox.six.material.utils;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

public class MultiSelectListPreferenceEx extends MultiSelectListPreference {
	
	public MultiSelectListPreferenceEx(Context context) {
		super(context);
	}
	
	public MultiSelectListPreferenceEx(Context context, AttributeSet attrSet) {
		super(context, attrSet);
	}

	public void show() {
		this.showDialog(null);
	}
}

package com.sensetoolbox.six.material.utils;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class ListPreferenceEx extends ListPreference {

	public ListPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListPreferenceEx(Context context) {
		super(context);
	}

	public void show() {
		showDialog(null);
	}
}
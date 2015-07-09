package com.sensetoolbox.six;

import com.sensetoolbox.six.htc.HMainActivity;
import com.sensetoolbox.six.material.MMainActivity;
import com.sensetoolbox.six.utils.Helpers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class UISwitcher extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Helpers.isNewSense()) {
			startActivity(new Intent(this, MMainActivity.class));
		} else {
			startActivity(new Intent(this, HMainActivity.class));
		}
		finish();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super()
	}
}
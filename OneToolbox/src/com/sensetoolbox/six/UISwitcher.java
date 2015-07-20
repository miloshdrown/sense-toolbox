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
		//SharedPreferences prefs = getSharedPreferences("one_toolbox_prefs", 1);
		//boolean forceMaterial = prefs.getBoolean("pref_key_toolbox_force_material", false);
		boolean forceMaterial = false;
		if (Helpers.isNewSense() || (forceMaterial && Helpers.isLP())) {
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
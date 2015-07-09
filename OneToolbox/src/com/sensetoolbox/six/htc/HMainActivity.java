package com.sensetoolbox.six.htc;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

import android.content.Intent;
import android.os.Bundle;

public class HMainActivity extends HActivityEx {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!launch) return;
		actionBarTextMain.setPrimaryText(Helpers.l10n(this, R.string.app_name));
		getFragmentManager().beginTransaction().replace(R.id.fragment_container, new HMainFragment()).commit();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Helpers.processResult(this, requestCode, resultCode, data);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super()
	}
}

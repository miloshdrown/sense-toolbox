package com.sensetoolbox.six.material;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;
import android.content.Intent;
import android.os.Bundle;

public class MMainActivity extends MActivityEx {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!launch) return;
		getActionBar().setTitle(Helpers.l10n(this, R.string.app_name));
		getFragmentManager().beginTransaction().replace(R.id.fragment_container, new MMainFragment()).commit();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Helpers.processResult(this, requestCode, resultCode, data);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super()
	}
}

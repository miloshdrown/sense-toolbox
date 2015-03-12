package com.sensetoolbox.six;

import com.sensetoolbox.six.utils.Helpers;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends ActivityEx {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!launch) return;
		actionBarTextMain.setPrimaryText(Helpers.l10n(this, R.string.app_name));
		getFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment()).commit();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Helpers.processResult(this, requestCode, resultCode, data);
	}
}

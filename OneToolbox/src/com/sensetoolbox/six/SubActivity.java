package com.sensetoolbox.six;

import com.sensetoolbox.six.utils.Helpers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class SubActivity extends ActivityEx {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String pref_section_name = this.getIntent().getStringExtra("pref_section_name");
		if (pref_section_name == null) pref_section_name = Helpers.l10n(this, R.string.app_name);
		actionBarTextMain.setPrimaryText(pref_section_name);
		actionBarContainer.setBackUpEnabled(true);
		actionBarContainer.setBackUpOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		getFragmentManager().beginTransaction().replace(R.id.fragment_container, new SubFragment(this.getIntent().getIntExtra("pref_section_xml", 0))).commit();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Helpers.processResult(this, requestCode, resultCode, data);
	}
}

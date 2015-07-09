package com.sensetoolbox.six.material;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;
import android.content.Intent;
import android.os.Bundle;

public class MSubActivity extends MActivityEx {
	String pref_section_name;
	int pref_section_xml;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!launch) return;
		pref_section_name = getIntent().getStringExtra("pref_section_name");
		pref_section_xml = getIntent().getIntExtra("pref_section_xml", 0);
		if (pref_section_name == null) pref_section_name = Helpers.l10n(this, R.string.app_name);
		getActionBar().setTitle(pref_section_name);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		getFragmentManager().beginTransaction().replace(R.id.fragment_container, new MSubFragment(pref_section_xml)).commit();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Helpers.processResult(this, requestCode, resultCode, data);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super()
	}
}

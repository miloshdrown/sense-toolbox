package com.sensetoolbox.six;

import com.sensetoolbox.six.utils.Helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class SubActivity extends ActivityEx {
	String pref_section_name;
	int pref_section_xml;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!launch) return;
		pref_section_name = getIntent().getStringExtra("pref_section_name");
		pref_section_xml = getIntent().getIntExtra("pref_section_xml", 0);
		if (pref_section_name == null) pref_section_name = Helpers.l10n(this, R.string.app_name);
		actionBarTextMain.setPrimaryText(pref_section_name);
		actionBarContainer.setBackUpEnabled(true);
		actionBarContainer.setBackUpOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		startListen();
		
		getFragmentManager().beginTransaction().replace(R.id.fragment_container, new SubFragment(pref_section_xml)).commit();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Helpers.processResult(this, requestCode, resultCode, data);
	}
	
	String recreateIntent = "com.sensetoolbox.six.PREFSUPDATED";
	IntentFilter filter = new IntentFilter(recreateIntent);
	public BroadcastReceiver recreateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null && intent.getAction().equals(recreateIntent)) recreate();
		}
	};
	
	private void startListen() {
		if (pref_section_xml == R.xml.prefs_popupnotify) try {
			registerReceiver(recreateReceiver, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void stopListen() {
		if (pref_section_xml == R.xml.prefs_popupnotify && recreateReceiver != null) try {
			unregisterReceiver(recreateReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onRestart() {
		startListen();
		super.onRestart();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		stopListen();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super()
	}
}

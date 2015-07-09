package org.acra;

import com.sensetoolbox.six.utils.Helpers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class CrashReportDialog extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent proxy = getIntent();
		
		if (Helpers.isNewSense())
			proxy.setClass(this, MCrashReportDialog.class);
		else
			proxy.setClass(this, HCrashReportDialog.class);
		
		startActivity(proxy);
		finish();
	}
}

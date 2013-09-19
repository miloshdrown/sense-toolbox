package com.langerhans.one;

import android.app.Activity;
import android.os.Bundle;

public class ApmActivity extends Activity {

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		
		ApmDialog rebD = new ApmDialog(this);
		rebD.show();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		finish();
	}
}

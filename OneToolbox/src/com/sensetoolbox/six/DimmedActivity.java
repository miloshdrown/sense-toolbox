package com.sensetoolbox.six;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DimmedActivity extends Activity {

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		
		final Intent intent = getIntent();
		int dialogType = intent.getIntExtra("dialogType", 1);
		
		if (dialogType == 1) {
			ApmDialog rebD = new ApmDialog(this);
			rebD.show();
		} 
	}

	@Override
	public void onPause()
	{
		super.onPause();
		finish();
	}
}

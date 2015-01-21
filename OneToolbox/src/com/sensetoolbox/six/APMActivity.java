package com.sensetoolbox.six;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

public class APMActivity extends Activity {
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		APMDialog rebD = new APMDialog(this);
		rebD.show();
	}
}

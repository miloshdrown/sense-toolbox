package com.sensetoolbox.six;

import com.sensetoolbox.six.htc.HAPMDialog;
import com.sensetoolbox.six.material.MAPMDialog;
import com.sensetoolbox.six.utils.Helpers;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

public class APMActivity extends Activity {
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		if (Helpers.isNewSense()) {
			MAPMDialog rebD = new MAPMDialog(this);
			rebD.show();
		} else {
			HAPMDialog rebD = new HAPMDialog(this);
			rebD.show();
		}
	}
}

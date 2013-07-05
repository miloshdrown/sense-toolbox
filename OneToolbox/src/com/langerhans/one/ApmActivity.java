package com.langerhans.one;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

public class ApmActivity extends Activity implements ApmDialog.AlertNegativeListener, ApmDialog.AlertPositiveListener{

	int selected;
	
	public ApmActivity() {
		selected = 0;
	}
	
	@Override
	public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        FragmentManager fm = getFragmentManager();
        ApmDialog rebD = new ApmDialog();
        Bundle bndl = new Bundle();
        bndl.putInt("selected", selected);
        rebD.setArguments(bndl);
        rebD.show(fm, "alert_dialog_radio");
    }

	@Override
	public void onPositiveClick(int i) {
		selected = i;
	}

	@Override
	public void onNegativeClick() {
		finish();
	}
	
	@Override
	public void onPause()
    {
        super.onPause();
        finish();
    }

}

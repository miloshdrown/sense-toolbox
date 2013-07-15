package com.langerhans.one;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

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
        rebD.show(fm, "apm_dialog");
    }

	@Override
	public void onPositiveClick(int i) {
		selected = i;
		switch (i){
			case 0: 
				Intent rebIntent = new Intent("ONETB_REBOOT");
				sendBroadcast(rebIntent);
				return;
			case 1:
				try {
					CommandCapture command = new CommandCapture(0, "setprop ctl.restart zygote");
					RootTools.getShell(true).add(command).waitForFinish();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			case 2:
				Intent recIntent = new Intent("ONETB_RECOVERY");
				sendBroadcast(recIntent);
				return;
			case 3:
				Intent blIntent = new Intent("ONETB_BOOTLOADER");
				sendBroadcast(blIntent);
				return;
		}
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

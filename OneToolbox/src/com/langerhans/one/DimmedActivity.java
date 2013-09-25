package com.langerhans.one;

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
		} /* else if (dialogType == 2) {
			HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(this);
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        	case DialogInterface.BUTTON_POSITIVE:
			        		Intent intent_kill = new Intent();
			        		intent_kill.setAction("com.langerhans.one.mods.action.killForegroundAppShedule");
			        		sendBroadcast(intent_kill);
			        		break;

			        	case DialogInterface.BUTTON_NEGATIVE:
			        		break;
			        }
			        finish();
			    }
			};
			builder.setMessage(R.string.kill_foreground_warn).setCancelable(false).setPositiveButton(R.string.kill_foreground_yes, dialogClickListener).setNegativeButton(R.string.kill_foreground_no, dialogClickListener).show();
		} */ 
	}

	@Override
	public void onPause()
	{
		super.onPause();
		finish();
	}
}

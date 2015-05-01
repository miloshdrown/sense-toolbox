package com.sensetoolbox.six;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class BlinkFeed extends Activity {
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setClassName("com.htc.launcher", "com.htc.launcher.Launcher");
		i.addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_TASK_ON_HOME).putExtra("action", 0);
		this.startActivity(i);
		finish();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		finish();
	}
}

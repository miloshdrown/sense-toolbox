package com.sensetoolbox.six.utils;

import android.content.DialogInterface;
import android.view.MenuItem;

import com.htc.preference.HtcPreferenceActivity;
import com.htc.widget.HtcAlertDialog;
import com.sensetoolbox.six.R;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class HtcPreferenceActivityEx extends HtcPreferenceActivity {
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.softreboot) {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(this);
			alert.setTitle(Helpers.l10n(this, R.string.soft_reboot));
			alert.setView(Helpers.createCenteredText(this, R.string.hotreboot_explain_prefs));
			alert.setPositiveButton(Helpers.l10n(this, R.string.yes) + "!", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						CommandCapture command = new CommandCapture(0, "setprop ctl.restart zygote");
						RootTools.getShell(true).add(command);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			alert.setNegativeButton(Helpers.l10n(this, R.string.no) + "!", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
		}
		return super.onOptionsItemSelected(item);
	}
}

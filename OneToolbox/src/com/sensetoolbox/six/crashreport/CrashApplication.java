package com.sensetoolbox.six.crashreport;

import org.acra.*;
import org.acra.sender.*;
import org.acra.annotation.*;

import com.sensetoolbox.six.R;

import android.app.Application;

@ReportsCrashes(
	formKey="",
	formUri="http://sensetoolbox.com/crashreports/reporter.php",
	reportType = HttpSender.Type.JSON,
	mode = ReportingInteractionMode.DIALOG,
	resDialogText = R.string.dummy,
	logcatArguments = { "-t", "200", "-v", "time" },
	sharedPreferencesName = "one_toolbox_prefs",
	sharedPreferencesMode = 1
)
public class CrashApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
		CrashReport crash = new CrashReport();
		ACRA.getErrorReporter().setReportSender(crash);
	}
}
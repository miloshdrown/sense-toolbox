package org.acra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

import org.acra.collector.CrashReportData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;

public class MCrashReportDialog extends Activity {
	private String mReportFileName;
	private String xposedLog;
	private EditText desc;
	
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private String getXposedLog() {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/data/data/de.robv.android.xposed.installer/log/error.log"))))) {
			String line = null;
			while ((line = reader.readLine()) != null) sb.append(line).append("\n");
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}
	
	void showFinishDialog(boolean isOk, String details) {
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setTitle(Helpers.l10n(this, R.string.crash_result));
		dlg.setCancelable(true);
		if (isOk)
			dlg.setView(Helpers.createCenteredText(this, R.string.crash_ok));
		else {
			TextView errorTxt = Helpers.createCenteredText(this, R.string.crash_error);
			if (details != null) errorTxt.setText(errorTxt.getText() + ": " + details);
			dlg.setView(errorTxt);
		}
		dlg.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		dlg.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
			}
		});
		dlg.show();
	}
	
	@SuppressLint("SdCardPath")
	private void sendCrash(final String xposedLogStr) {
		String exceptionsPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SenseToolbox/";
		try {
			CrashReportPersister persister = new CrashReportPersister(getApplicationContext());
			CrashReportData crashData = persister.load(mReportFileName);
			
			String ROM = "";
			Process ifc = null;
			try {
				ifc = Runtime.getRuntime().exec("getprop ro.product.version");
				BufferedReader bis = new BufferedReader(new InputStreamReader(ifc.getInputStream()), 2048);
				ROM = bis.readLine();
			} catch (Exception e) {} finally {
				if (ifc != null) ifc.destroy();
			}
			
			String kernel = System.getProperty("os.version");
			if (kernel == null) kernel = "";
			
			SharedPreferences prefs = getSharedPreferences("one_toolbox_prefs", 1);
			TreeMap<String, Object> keys = new TreeMap<String, Object>();
			keys.putAll(prefs.getAll());
			String keysAsString = "";
			for (Map.Entry<String, Object> entry: keys.entrySet())
			keysAsString += entry.getKey() + "=" + entry.getValue().toString() + "\n";
			
			StringBuilder sb = new StringBuilder();
			try (FileInputStream in = new FileInputStream(new File(exceptionsPath + "uncaught_exceptions"))) {
				try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in))) {
					String line;
					while ((line = bufferedReader.readLine()) != null) sb.append(line).append("\n");
				} catch (Exception e) {}
			} catch (Exception e) {}
			
			String buildData = crashData.getProperty(ReportField.BUILD);
			buildData += "ROM.VERSION=" + ROM + "\n";
			buildData += "KERNEL.VERSION=" + kernel + "\n";
			buildData += "SHARED.PREFS=" + Base64.encodeToString(keysAsString.getBytes(), Base64.NO_WRAP) + "\n";
			if (!sb.toString().isEmpty())
			buildData += "UNCAUGHT.EXCEPTIONS=" + Base64.encodeToString(sb.toString().getBytes(), Base64.NO_WRAP) + "\n";
			
			crashData.put(ReportField.BUILD, buildData);
			crashData.put(ReportField.USER_COMMENT, desc.getText().toString());
			if (xposedLogStr == null || xposedLogStr.trim() == "")
				crashData.put(ReportField.CUSTOM_DATA, "Xposed log is empty...");
			else
				crashData.put(ReportField.CUSTOM_DATA, xposedLogStr);
			persister.store(crashData, mReportFileName);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			ACRA.getErrorReporter().putCustomData("XPOSED_LOG", "Retrieval failed. Stack trace:\n" + sw.toString());
		}
		SendWorker worker = ACRA.getErrorReporter().startSendingReports(false, true);
		try {
			worker.join(60000);
		} catch (InterruptedException e) {
			showFinishDialog(false, "server timeout");
			return;
		}
		Helpers.emptyFile(exceptionsPath + "uncaught_exceptions", true);
		showFinishDialog(true, null);
	}
	
	private void cancelReports() {
		ACRA.getErrorReporter().deletePendingNonApprovedReports(false);
		finish();
	}
	
	int densify(int size) {
		return Math.round(getResources().getDisplayMetrics().density * size);
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			String mThemeAccentName = getSharedPreferences("one_toolbox_prefs", 1).getString("pref_key_toolbox_material_accent", null);
			if (mThemeAccentName != null) getTheme().applyStyle(getResources().getIdentifier(mThemeAccentName, "style", getPackageName()), true);
			String mThemeHeaderName = getSharedPreferences("one_toolbox_prefs", 1).getString("pref_key_toolbox_material_header", null);
			if (mThemeHeaderName != null) getTheme().applyStyle(getResources().getIdentifier(mThemeHeaderName, "style", getPackageName()), true);
			int mThemeBackground = Integer.parseInt(Helpers.prefs.getString("pref_key_toolbox_material_background", "1"));
			if (mThemeBackground == 2) getTheme().applyStyle(R.style.MaterialThemeDark, true);
			getTheme().applyStyle(R.style.ApplyInvisible, true);
		} catch (Exception e) {}

		if (getIntent().getBooleanExtra("FORCE_CANCEL", false)) {
			cancelReports();
			return;
		}

		mReportFileName = getIntent().getStringExtra("REPORT_FILE_NAME");
		if (mReportFileName == null) finish();
		
		String title = Helpers.l10n(this, R.string.warning);
		String neutralText = Helpers.l10n(this, R.string.crash_ignore);
		TextView text = Helpers.createCenteredText(this, R.string.crash_dialog);
		LinearLayout dialogView = new LinearLayout(this);
		dialogView.setOrientation(LinearLayout.VERTICAL);
		
		int tries = 5;
		xposedLog = null;
		while (xposedLog == null && tries > 0) try {
			tries--;
			xposedLog = getXposedLog();
			if (xposedLog == null) Thread.sleep(500);
		} catch (Exception e) {}
		
		try {
			CrashReportPersister persister = new CrashReportPersister(getApplicationContext());
			CrashReportData crashData = persister.load(mReportFileName);
			ObjectMapper mapper = new ObjectMapper();
			String payload = mapper.writeValueAsString(Helpers.getParamsAsStringString(crashData));
			int payloadSize = payload.getBytes("UTF-8").length;
			boolean isManualReport = crashData.getProperty(ReportField.STACK_TRACE).contains("Report requested by developer");
			Log.e("ACRA", crashData.getProperty(ReportField.STACK_TRACE));
			
			TextView descText = new TextView(this);
			descText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			descText.setGravity(Gravity.START);
			descText.setPadding(densify(10), 0, densify(10), 0);
			descText.setTextColor(text.getCurrentTextColor());
			descText.setTextSize(TypedValue.COMPLEX_UNIT_PX, text.getTextSize() - 10);
			
			desc = new EditText(this);
			desc.setGravity(Gravity.TOP | Gravity.START);
			desc.setInputType(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
			desc.setSingleLine(false);
			desc.setPadding(densify(5), densify(5), densify(5), densify(5));
			desc.setTextColor(text.getCurrentTextColor());
			
			if (isManualReport) {
				title = Helpers.l10n(this, R.string.popupnotify_blconfirm);
				text = Helpers.createCenteredText(this, R.string.crash_dialog_manual);
				neutralText = Helpers.l10n(this, R.string.sense_themes_cancel);
				descText.setText(Helpers.l10n(this, R.string.crash_dialog_manual_desc));
				
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, densify(100));
				lp.setMargins(densify(10), densify(5), densify(10), densify(10));
				desc.setLayoutParams(lp);
			} else {
				descText.setText(Helpers.l10n(this, R.string.crash_dialog_manual_desc2));
				
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, densify(50));
				lp.setMargins(densify(10), densify(5), densify(10), densify(10));
				desc.setLayoutParams(lp);
				desc.setFocusable(false);
				desc.setFocusableInTouchMode(false);
				desc.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						v.setFocusable(true);
						v.setFocusableInTouchMode(true);
						v.performClick();
						return false;
					}
				});
			}
			
			TextView feedbackNote = new TextView(this);
			feedbackNote.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			feedbackNote.setGravity(Gravity.START);
			feedbackNote.setPadding(densify(10), 0, densify(10), densify(10));
			feedbackNote.setTextColor(text.getCurrentTextColor());
			feedbackNote.setTextSize(TypedValue.COMPLEX_UNIT_PX, text.getTextSize() - 10);
			feedbackNote.setText(Helpers.l10n(this, R.string.crash_dialog_note));
			
			dialogView.addView(text);
			dialogView.addView(descText);
			dialogView.addView(desc);
			
			if (getSharedPreferences("one_toolbox_prefs", 1).getString("acra.user.email", "").isEmpty())
			dialogView.addView(feedbackNote);
			
			text.setText(text.getText() + "\n" + Helpers.l10n(this, R.string.crash_dialog_manual_size) + ": " + String.valueOf(Math.round(payloadSize / 1024)) + " KB");
		} catch (Exception e) {}
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(title);
		alert.setView(dialogView);
		alert.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				cancelReports();
			}
		});
		alert.setNeutralButton(neutralText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				cancelReports();
			}
		});
		alert.setPositiveButton(Helpers.l10n(this, R.string.crash_send), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});
		final AlertDialog alertDlg = alert.show();
		alertDlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (desc != null && desc.getText().toString().trim().equals("")) {
					Toast.makeText(MCrashReportDialog.this, Helpers.l10n(MCrashReportDialog.this, R.string.crash_needs_desc), Toast.LENGTH_LONG).show();
				} else if (!isNetworkAvailable()) {
					Toast.makeText(MCrashReportDialog.this, Helpers.l10n(MCrashReportDialog.this, R.string.crash_needs_inet), Toast.LENGTH_LONG).show();
				} else {
					alertDlg.dismiss();
					sendCrash(xposedLog);
				}
			}
		});
	}
}

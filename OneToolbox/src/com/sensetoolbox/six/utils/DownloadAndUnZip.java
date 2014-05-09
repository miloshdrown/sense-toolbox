package com.sensetoolbox.six.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.htc.app.HtcProgressDialog;
import com.htc.widget.HtcAlertDialog;
import com.sensetoolbox.six.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

public class DownloadAndUnZip extends AsyncTask<String, Integer, String> {
	private Activity act;
	HtcProgressDialog mProgressDialog;

	public DownloadAndUnZip(Activity act) {
		this.act = act;
		final DownloadAndUnZip task = this;
		mProgressDialog = new HtcProgressDialog(act);
		mProgressDialog.setTitle(Helpers.l10n(act, R.string.download_title));
		mProgressDialog.setMessage(Helpers.l10n(act, R.string.download_desc));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
		    @Override
		    public void onCancel(DialogInterface dialog) {
		        task.cancel(true);
		    }
		});
	}

	@Override
	protected String doInBackground(String... sUrl) {
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		
		try {
			URL url = new URL(sUrl[0]);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED) {
				return null; // "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
			}
			int fileLength = connection.getContentLength();

			input = connection.getInputStream();
			File tmp = new File(Helpers.dataPath);
			tmp.mkdirs();
			output = new FileOutputStream(Helpers.dataPath + "strings.zip", false);

			byte data[] = new byte[4096];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1) {
				if (isCancelled()) {
					input.close();
					break;
				}
				total += count;
				if (fileLength > 0) publishProgress((int) (total * 100 / fileLength));
				output.write(data, 0, count);
			}
		} catch (Exception e) {
			act.runOnUiThread(new Runnable() {
				@Override
			    public void run() {
					HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(act);
					alert.setTitle(Helpers.l10n(act, R.string.warning));
					alert.setView(Helpers.createCenteredText(act, R.string.download_failed));
					alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {}
					});
					alert.show();
				}
			});
			e.printStackTrace();
			return null;
		}
		
		try {
			if (output != null) output.close();
			if (input != null) input.close();
		} catch (Exception ignored) {}
		if (connection != null) connection.disconnect();
		return "OK";
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog.show();
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgress(progress[0]);
	}

	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(act);
			
			String buildIdBefore = "";
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Helpers.dataPath + "version")));
				buildIdBefore = br.readLine();
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (unpackZip(Helpers.dataPath, "strings.zip")) {
				String buildIdAfter = "";
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Helpers.dataPath + "version")));
					buildIdAfter = br.readLine();
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (buildIdAfter == "") {
					alert.setTitle(Helpers.l10n(act, R.string.warning));
					alert.setView(Helpers.createCenteredText(act, R.string.download_version_problem));
					alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {}
					});
				} else if (!buildIdBefore.equals(buildIdAfter)) {
					alert.setTitle(Helpers.l10n(act, R.string.success));
					alert.setView(Helpers.createCenteredText(act, R.string.download_succeeded));
					alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							Helpers.l10n = null;
							Helpers.cLang = "";
							act.recreate();
						}
					});
				} else {
					alert.setTitle(Helpers.l10n(act, R.string.warning));
					alert.setView(Helpers.createCenteredText(act, R.string.download_same_version));
					alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {}
					});
				}
			} else {
				alert.setTitle(Helpers.l10n(act, R.string.warning));
				alert.setView(Helpers.createCenteredText(act, R.string.download_unzip_failed));
				alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
				});
			}
			alert.show();
		}
		mProgressDialog.dismiss();
	}
	
	private boolean unpackZip(String path, String zipname) {       
		InputStream is;
		ZipInputStream zis;
		try {
			String filename;
			is = new FileInputStream(path + zipname);
			zis = new ZipInputStream(new BufferedInputStream(is));          
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;

			while ((ze = zis.getNextEntry()) != null) {
				filename = ze.getName();
				File fmd = new File(path + filename);
				if (ze.isDirectory()) {
					fmd.mkdirs();
					continue;
				}
				FileOutputStream fout = new FileOutputStream(fmd, false);
				while ((count = zis.read(buffer)) != -1) fout.write(buffer, 0, count);             
				fout.close();               
				zis.closeEntry();
				fmd.setReadable(true, false);
			}
			zis.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	    return true;
	}
}
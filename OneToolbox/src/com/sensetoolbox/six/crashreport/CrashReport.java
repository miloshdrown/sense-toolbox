package com.sensetoolbox.six.crashreport;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.acra.ACRA;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensetoolbox.six.utils.Helpers;

public class CrashReport implements ReportSender {
	public CrashReport(){}
	
	@Override
	public void send(CrashReportData report) throws ReportSenderException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(Helpers.getParamsAsStringString(report));
			
			//final String basicAuth = "Basic " + Base64.encodeToString("Sense6Toolbox:NotASecret".getBytes(), Base64.NO_WRAP);
			URL url = new URL(ACRA.getConfig().formUri());
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setReadTimeout(5000);
			conn.setConnectTimeout(5000);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			try (OutputStream os = conn.getOutputStream()) {
				try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
					writer.write(json);
					writer.flush();
				}
			}
			conn.connect();

			//Log.e(null, "Report server response code: " + String.valueOf(conn.getResponseCode()));
			//Log.e(null, "Report server response: " + conn.getResponseMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package com.sensetoolbox.six.crashreport;

import java.util.Map;

import org.acra.ACRA;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import com.htc.gson.GsonBuilder;
import com.sensetoolbox.six.utils.Helpers;

public class CrashReport implements ReportSender {
	public CrashReport(){}
	
	@Override
	public void send(CrashReportData report) throws ReportSenderException {
		try {
			String json = new GsonBuilder().create().toJson(Helpers.getParamsAsStringString(report), Map.class);

			DefaultHttpClient http = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(ACRA.getConfig().formUri());
			//final String basicAuth = "Basic " + Base64.encodeToString("Sense6Toolbox:NotASecret".getBytes(), Base64.NO_WRAP);
			
			httpPost.setEntity(new StringEntity(json, HTTP.UTF_8));
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			//httpPost.setHeader("Authorization", basicAuth);
			
			http.execute(httpPost);
			//HttpResponse resp =
			//String respText = EntityUtils.toString(resp.getEntity());
			//Log.d(null, "Report server response: " + respText);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

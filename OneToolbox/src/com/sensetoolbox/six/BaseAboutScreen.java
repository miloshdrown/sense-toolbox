package com.sensetoolbox.six;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import com.sensetoolbox.six.utils.Helpers;

public class BaseAboutScreen extends Activity {
	
	public float alphaTitle = 1.0f;
	public float alphaText = 1.0f;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public void createViews() {
		setContentView(R.layout.about_screen);
		
		Typeface face = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
		TextView senseAndroidVer = (TextView)findViewById(R.id.senseAndroidVer);
		senseAndroidVer.setTypeface(face);
		senseAndroidVer.setAlpha(alphaText);
		String versions;
		if (Helpers.isSense7())
			versions = Helpers.l10n(this, R.string.about_rom_base) + ": Sense 7";
		else
			versions = Helpers.l10n(this, R.string.about_rom_base) + ": Sense 6";
		versions += "\n" + Helpers.l10n(this, R.string.about_android_ver) + ": " + Build.VERSION.RELEASE;
		senseAndroidVer.setText(versions);
		
		TextView iv2 = (TextView)findViewById(R.id.textView2);
		iv2.setAlpha(alphaTitle);
		iv2.setPaintFlags(iv2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		iv2.setTypeface(face);
		iv2.setText(Helpers.l10n(this, R.string.about_devs));
		TextView iv3 = (TextView)findViewById(R.id.textView3);
		iv3.setAlpha(alphaText);
		iv3.setTypeface(face);
		iv3.setText(Helpers.l10n(this, R.string.about_devs_names));
		iv3.setText(iv3.getText() + "\n\u00a9 2013-2015");
		
		TextView iv02 = (TextView)findViewById(R.id.TextView02);
		iv02.setAlpha(alphaTitle);
		iv02.setPaintFlags(iv02.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		iv02.setTypeface(face);
		iv02.setText(Helpers.l10n(this, R.string.about_thanks));
		TextView iv03 = (TextView)findViewById(R.id.TextView03);
		iv03.setAlpha(alphaText);
		iv03.setTypeface(face);
		iv03.setText(Helpers.l10n(this, R.string.about_thanks_data));
		
		TextView iv04 = (TextView)findViewById(R.id.TextView04);
		iv04.setAlpha(alphaTitle);
		iv04.setPaintFlags(iv02.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		iv04.setTypeface(face);
		iv04.setText(Helpers.l10n(this, R.string.about_l10n));
		
		TextView iv4 = (TextView)findViewById(R.id.TextView4);
		iv4.setAlpha(alphaText);
		iv4.setTypeface(face);
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Helpers.dataPath + "translators")))) {
			StringBuilder builder = new StringBuilder();
			String tmp = "";

			while ((tmp = br.readLine()) != null) builder.append(tmp);
			String htmlTrans = builder.toString();
			String[] langs = { "de", "es", "fr", "hr", "it", "nl", "pl", "ro", "ru", "tr", "vi", "zh", "zh_TW", "cs", "pt_BR", "hi", "ja", "bg", "sv" };
			for (String lang: langs)
			htmlTrans = htmlTrans.replace("[" + lang + "]", Helpers.l10n(this, getResources().getIdentifier("about_l10n_" + lang, "string", this.getPackageName())));
			
			iv4.setText(Html.fromHtml(htmlTrans));
		} catch (Exception e) {
			iv4.setText(Helpers.l10n(this, R.string.about_l10n_notfound) + "\n");
			if (!(e instanceof FileNotFoundException)) e.printStackTrace();
		}
		
		//Add version name
		try {
			TextView versionTv = (TextView)findViewById(R.id.textViewVersion);
			versionTv.setAlpha(alphaTitle);
			versionTv.setText(String.format(Helpers.l10n(this, R.string.about_version), getPackageManager().getPackageInfo(getPackageName(), 0).versionName, Helpers.buildVersion));
			versionTv.setTypeface(face);
			versionTv.setPaintFlags(iv02.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			OnLongClickListener olcl = new OnLongClickListener(){
				public boolean onLongClick(View v) {
					sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.StartEasterEgg"));
					return true;
				}
			};
			versionTv.setLongClickable(true);
			versionTv.setOnLongClickListener(olcl);
			ImageView logo = (ImageView)findViewById(R.id.imageView1);
			logo.setLongClickable(true);
			logo.setOnLongClickListener(olcl);
			logo.setContentDescription(Helpers.l10n(this, R.string.app_about));
		} catch (NameNotFoundException e) {
			//Shouldn't happen...
			e.printStackTrace();
		}
	}
}

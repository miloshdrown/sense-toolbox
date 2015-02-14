package com.sensetoolbox.six;

import java.util.ArrayList;
import java.util.List;

import com.htc.app.HtcProgressDialog;
import com.htc.preference.HtcPreference;
import com.htc.preference.HtcPreference.OnPreferenceChangeListener;
import com.htc.preference.HtcPreferenceScreen;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarText;
import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcToggleButtonLight;
import com.htc.widget.HtcToggleButtonLight.OnCheckedChangeListener;
import com.sensetoolbox.six.utils.DynamicPreference;
import com.sensetoolbox.six.utils.Helpers;
import com.sensetoolbox.six.utils.HtcPreferenceActivityEx;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EPSRemap extends HtcPreferenceActivityEx {
	int mThemeId = 0;
	SharedPreferences prefs;
	HtcToggleButtonLight OnOffSwitch;
	LinearLayout gridBkg;
	int[][] cellArray = {
		{ 0, 0, 0 },
		{ R.id.cell1, R.id.cell1img, R.id.cell1txt },
		{ R.id.cell2, R.id.cell2img, R.id.cell2txt },
		{ R.id.cell3, R.id.cell3img, R.id.cell3txt },
		{ R.id.cell4, R.id.cell4img, R.id.cell4txt },
		{ R.id.cell5, R.id.cell5img, R.id.cell5txt },
		{ R.id.cell6, R.id.cell6img, R.id.cell6txt }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Apply Settings theme
		mThemeId = Helpers.getCurrentTheme(this);
		setTheme(mThemeId);
		Helpers.setTranslucentStatusBar(this);
				
		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
		ActionBarContainer actionBarContainer = actionBarExt.getCustomContainer();
		ActionBarText actionBarText = new ActionBarText(this);
		actionBarText.setPrimaryText(Helpers.l10n(this, R.string.various_extremepower_title));
		actionBarContainer.addCenterView(actionBarText);
		actionBarContainer.setBackUpEnabled(true);
		
		View homeBtn = actionBarContainer.getChildAt(0);
		if (homeBtn != null) {
			OnClickListener goBack = new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			};
			homeBtn.setOnClickListener(goBack);
		}
		
		setContentView(R.layout.activity_eps_remap);
		// Hidden prefscreen to assign DynamicPreference
		addPreferencesFromResource(R.xml.dummy);
		getPreferenceManager().setSharedPreferencesName("one_toolbox_prefs");
		getPreferenceManager().setSharedPreferencesMode(1);
		prefs = getPreferenceManager().getSharedPreferences();
		
		int backResId = getResources().getIdentifier("common_app_bkg", "drawable", "com.htc");
		gridBkg = (LinearLayout)findViewById(R.id.gridBkg);
		gridBkg.setBackgroundResource(backResId);
		
		TextView hint = (TextView)findViewById(R.id.hint);
		hint.setText(Helpers.l10n(this, R.string.various_extremepower_hint));
		
		for (int i = 1; i <= 6; i++) initCell(i);
	}
	
	private void applyState(boolean state) {
		gridBkg.setEnabled(state);
		OnOffSwitch.setChecked(state);
		LinearLayout row1 = (LinearLayout)findViewById(R.id.row1);
		LinearLayout row2 = (LinearLayout)findViewById(R.id.row2);
		LinearLayout row3 = (LinearLayout)findViewById(R.id.row3);
		if (state) {
			row1.setAlpha(1.0f);
			row2.setAlpha(1.0f);
			row3.setAlpha(1.0f);
		} else {
			row1.setAlpha(0.5f);
			row2.setAlpha(0.5f);
			row3.setAlpha(0.5f);
		}
	}
	
	View.OnTouchListener otl = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (OnOffSwitch.isChecked())
			switch (event.getAction()) {
				case 0:
					v.setBackgroundColor(0x7f888888);
					break;
				case 1:
					v.setBackgroundColor(0x7f666666);
					editApp(v, (int)v.getTag());
					break;
			}
			v.performClick();
			return true;
		}
	};
	
	private void initCell(int cellnum) {
		String pkgActName = prefs.getString("eps_remap_cell" + String.valueOf(cellnum), null);
		updateCell(cellnum, pkgActName);
		
		int cellid = cellArray[cellnum][0];
		final LinearLayout cell = (LinearLayout)findViewById(cellid);
		cell.setTag(cellnum);
		cell.setOnTouchListener(otl);
	}
	
	private void updateCell(int cellnum, String pkgActName) {
		int cellimgid = cellArray[cellnum][1];
		int celltxtid = cellArray[cellnum][2];
		try {
			ImageView cellimg = (ImageView)findViewById(cellimgid);
			TextView celltxt = (TextView)findViewById(celltxtid);
			if (pkgActName != null) {
				final PackageManager pm = getApplicationContext().getPackageManager();
				String[] pkgActArray = pkgActName.split("\\|");
				cellimg.setImageDrawable(pm.getActivityIcon(new ComponentName(pkgActArray[0], pkgActArray[1])));
				celltxt.setText(Helpers.getAppName(this, pkgActName));
			} else {
				cellimg.setImageResource(R.drawable.question_icon);
				celltxt.setText(Helpers.l10n(this, R.string.array_default));
			}
		} catch (Exception e) {}
	}
	
	private void editApp(View cell, final int id) {
		HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(this);
		final String title = Helpers.l10n(this, R.string.various_extremepower_cell) + " " + String.valueOf(id);
		builder.setTitle(title);
		
		TypedArray ids = getResources().obtainTypedArray(R.array.EPSRemaps);
		List<String> newEntries = new ArrayList<String>();
		for (int i = 0; i < ids.length(); i++) {
			int itemid = ids.getResourceId(i, 0);
			if (itemid != 0)
				newEntries.add(Helpers.l10n(this, itemid));
			else
				newEntries.add("???");
		}
		ids.recycle();
		
		builder.setItems(newEntries.toArray(new CharSequence[newEntries.size()]), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0:
						prefs.edit().putString("eps_remap_cell" + String.valueOf(id), null).commit();
						prefs.edit().putString("eps_remap_cell" + String.valueOf(id) + "_intent", null).commit();
						initCell(id);
						break;
					case 1:
						final DynamicPreference dp = new DynamicPreference(EPSRemap.this);
						dp.setTitle(title);
						dp.setDialogTitle(title);
						dp.setKey("eps_remap_cell" + String.valueOf(id));
						dp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
							@Override
							public boolean onPreferenceChange(HtcPreference pref, Object newValue) {
								updateCell(id, (String)newValue);
								return true;
							}
						});
						HtcPreferenceScreen cat = (HtcPreferenceScreen)findPreference("dummy");
						cat.removeAll();
						cat.addPreference(dp);
						
						if (Helpers.launchableAppsList == null) {
							final HtcProgressDialog dialogLoad = new HtcProgressDialog(EPSRemap.this);
							dialogLoad.setMessage(Helpers.l10n(EPSRemap.this, R.string.loading_app_data));
							dialogLoad.setCancelable(false);
							dialogLoad.show();
							
							new Thread() {
								@Override
								public void run() {
									try {
										Helpers.getLaunchableApps(EPSRemap.this);
										runOnUiThread(new Runnable(){
											@Override
											public void run(){
												dp.show();
											}
										});
										// Nasty hack! Wait for icons to load.
										Thread.sleep(1000);
										runOnUiThread(new Runnable(){
											@Override
											public void run() {
												dialogLoad.dismiss();
											}
										});
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}.start();
						} else dp.show();
						break;
				}
			}
		});
		builder.setNeutralButton(R.string.sense_themes_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}
	
	protected void onResume() {
		super.onResume();
		int newThemeId = Helpers.getCurrentTheme(this);
		if (newThemeId != mThemeId) recreate();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_sub, menu);
		
		OnOffSwitch = (HtcToggleButtonLight)menu.getItem(1).getActionView().findViewById(R.id.onoffSwitch);
		OnOffSwitch.setEnabled(true);
		boolean state = prefs.getBoolean("eps_remap_active", false);
		applyState(state);
		OnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(HtcToggleButtonLight toggle, boolean state) {
				prefs.edit().putBoolean("eps_remap_active", state).commit();
				applyState(state);
			}
		});
		return true;
	}
}

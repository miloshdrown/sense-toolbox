package com.sensetoolbox.six;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.htc.app.HtcProgressDialog;
import com.htc.preference.HtcMultiSelectListPreference;
import com.htc.preference.HtcPreference;
import com.htc.preference.HtcPreference.OnPreferenceChangeListener;
import com.htc.preference.HtcPreference.OnPreferenceClickListener;
import com.htc.preference.HtcPreferenceActivity;
import com.htc.preference.HtcPreferenceManager;
import com.htc.preference.HtcSwitchPreference;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarItemView;
import com.htc.widget.ActionBarText;
import com.htc.widget.HtcListView;
import com.htc.widget.HtcToggleButtonLight;
import com.htc.widget.HtcToggleButtonLight.OnCheckedChangeListener;
import com.sensetoolbox.six.utils.Helpers;
import com.sensetoolbox.six.utils.HtcMultiSelectListPreferenceEx;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class PopupNotify extends HtcPreferenceActivity {
	public static List<ApplicationInfo> pkgAppsList = null;
	SharedPreferences prefs;
	String recreateIntent = "com.sensetoolbox.six.PREFSUPDATED";
	IntentFilter filter = new IntentFilter(recreateIntent);
	SharedPreferences.OnSharedPreferenceChangeListener prefListener;
	HtcToggleButtonLight OnOffSwitch;
	ActionBarItemView menuTest;
	HtcListView prefListView;
	TextView themeHint;
	int mThemeId = 0;
	
	public BroadcastReceiver recreateReceiver = new BroadcastReceiver() {    
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null && intent.getAction().equals(recreateIntent)) recreate();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startListen();
		
		// Apply Settings theme
		mThemeId = Helpers.getCurrentTheme(this);
		setTheme(mThemeId);
		Helpers.setTranslucentStatusBar(this);
		
		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
		ActionBarContainer actionBarContainer = actionBarExt.getCustomContainer();
		ActionBarText actionBarText = new ActionBarText(this);
		actionBarText.setPrimaryText(Helpers.l10n(this, R.string.various_popupnotify_title));
		actionBarContainer.addCenterView(actionBarText);
		actionBarContainer.setBackUpEnabled(true);
		
		OnOffSwitch = new HtcToggleButtonLight(this);
		OnOffSwitch.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		OnOffSwitch.setEnabled(true);
		actionBarContainer.addRightView(OnOffSwitch);
		
		View homeBtn = actionBarContainer.getChildAt(0);
		if (homeBtn != null) {
			OnClickListener goBack = new OnClickListener() {
				@Override
				public void onClick(View v) {
					stopListen();
					finish();
				}
			};
			homeBtn.setOnClickListener(goBack);
		}
		
		final PopupNotify pn = this;
		
		menuTest = new ActionBarItemView(this);
		menuTest.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		menuTest.setIcon(getResources().getIdentifier("icon_btn_view_dark", "drawable", "com.htc"));
		menuTest.setLongClickable(false);
		menuTest.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PendingIntent pendingintent = PendingIntent.getActivity(pn, 0, new Intent(pn, MainActivity.class), 0x10000000);
	            PendingIntent pendingintent1 = PendingIntent.getActivity(pn, 0, new Intent(), 0x10000000);
	            Notification notification1 = (new Notification.Builder(pn))
	            		.setContentTitle("Inbox Style notification")
	            		.setContentText("This is a test notification")
	            		.setAutoCancel(true)
	            		.setContentIntent(pendingintent)
	            		.setSmallIcon(R.drawable.apm_bootloader)
	            		.setLargeIcon(BitmapFactory.decodeResource(pn.getResources(), R.drawable.apm_bootloader))
	            		.addAction(android.R.drawable.ic_menu_call, "Do nothing", pendingintent1)
	            		.addAction(android.R.drawable.ic_menu_delete, "...do it again", pendingintent1)
	            		.setStyle(new Notification.InboxStyle()
	            			.addLine("This is a test!")
	            			.addLine("Multiple lines")
	            			.setSummaryText("Summary example"))
	            		.build();
	            
	            final Notification notification2 = (new Notification.Builder(pn))
	            		.setContentTitle("Big Picture Style")
	            		.setContentText("This is a test!")
	            		.setAutoCancel(true)
	            		.setContentIntent(pendingintent)
	            		.setSmallIcon(R.drawable.apm_hotreboot)
	            		.setLargeIcon(BitmapFactory.decodeResource(pn.getResources(), R.drawable.apm_hotreboot))
	            		.addAction(android.R.drawable.ic_menu_call, "Do nothing", pendingintent1)
	            		.addAction(android.R.drawable.ic_menu_delete, "...do it again", pendingintent1)
	            		.setStyle(new Notification.BigPictureStyle()
	            			.bigPicture(BitmapFactory.decodeResource(pn.getResources(), getResources().getIdentifier("htc_logo", "drawable", "com.htc")))
	            			.bigLargeIcon(BitmapFactory.decodeResource(pn.getResources(), R.drawable.apm_hotreboot))
	            			.setSummaryText("Summary example"))
	            		.build();
	            
	            final Notification notification3 = (new Notification.Builder(pn))
	            		.setContentTitle("Big Text Style notification")
	            		.setContentText("This is a test notification")
	            		.setAutoCancel(true)
	            		.setContentIntent(pendingintent)
	            		.setSmallIcon(R.drawable.apm_recovery)
	            		.setLargeIcon(BitmapFactory.decodeResource(pn.getResources(), R.drawable.apm_recovery))
	            		.addAction(android.R.drawable.ic_menu_call, "Do nothing", pendingintent1)
	            		.addAction(android.R.drawable.ic_menu_delete, "...do it again", pendingintent1)
	            		.setStyle(new Notification.BigTextStyle().setSummaryText("Summary example").bigText("This is a test!"))
	            		.build();
	            final NotificationManager nm = (NotificationManager)pn.getSystemService("notification");
	            nm.notify(6661, notification1);
	            
	            (new Handler()).postDelayed(new Runnable() {
	                public void run() {
	                	nm.notify(6662, notification2);
	                }
	            }, 3000L);
	            
	            (new Handler()).postDelayed(new Runnable() {
	                public void run() {
	                	nm.notify(6663, notification3);
	                }
	            }, 5000L);
			}
		});
		actionBarContainer.addRightView(menuTest);
		
		getPreferenceManager().setSharedPreferencesName("one_toolbox_prefs");
		getPreferenceManager().setSharedPreferencesMode(1);
		HtcPreferenceManager.setDefaultValues(this, R.xml.prefs_popupnotify, false);
		prefs = getPreferenceManager().getSharedPreferences();
		addPreferencesFromResource(R.xml.prefs_popupnotify);
		setContentView(R.layout.activity_popup_notify);
		
		int backResId = getResources().getIdentifier("common_app_bkg", "drawable", "com.htc");
		findViewById(R.id.backLayer).setBackgroundResource(backResId);
		
		TextView experimental = (TextView)findViewById(R.id.experimental);
		experimental.setText(Helpers.l10n(this, R.string.popupnotify_experimental));
		experimental.setTextColor(getResources().getColor(android.R.color.background_light));
		
		prefListView = (HtcListView)this.findViewById(android.R.id.list);
		prefListView.setBackgroundResource(backResId);
		prefListView.setDivider(getResources().getDrawable(getResources().getIdentifier("inset_list_divider", "drawable", "com.htc")));
		prefListView.setDividerHeight(1);
		prefListView.setFooterDividersEnabled(false);
		
		themeHint = (TextView)findViewById(R.id.themehint);
		themeHint.setBackgroundResource(backResId);
		themeHint.setText(Helpers.l10n(this, R.string.popupnotify_hint));
		
		applyThemeState(prefs.getBoolean("popup_notify_active", false));
		
		OnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(HtcToggleButtonLight toggle, boolean state) {
				prefs.edit().putBoolean("popup_notify_active", state).commit();
				applyThemeState(state);
			}
		});
		
		final HtcSwitchPreference bwlist = (HtcSwitchPreference)findPreference("pref_key_other_popupnotify_bwlist");
		bwlist.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(HtcPreference preference, Object state) {
				updateListType((boolean)state);
				return true;
			}
		});
		updateListType(bwlist.isChecked());
		
		final HtcMultiSelectListPreferenceEx bwlistApps = (HtcMultiSelectListPreferenceEx)findPreference("pref_key_other_popupnotify_bwlist_apps");
		bwlistApps.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(HtcPreference paramHtcPreference) {
				if (bwlistApps.getEntries().length == 0 || bwlistApps.getEntryValues().length == 0) {
					if (bwlistApps.getDialog() != null) bwlistApps.getDialog().dismiss();
					final HtcProgressDialog dialog = new HtcProgressDialog(pn);
					if (pkgAppsList == null) {
						dialog.setMessage(Helpers.l10n(pn, R.string.loading_app_data));
						dialog.setCancelable(false);
						dialog.show();
					}
					
					new Thread() {
						@Override
						public void run() {
							try {
								if (pkgAppsList == null) getApps(pn);
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										HashSet<String> appsList = (HashSet<String>)prefs.getStringSet("pref_key_other_popupnotify_bwlist_apps", new HashSet<String>());
										ArrayList<ArrayList<CharSequence>> entries = new ArrayList<ArrayList<CharSequence>>();
										for (ApplicationInfo appInfo: pkgAppsList) {
											ArrayList<CharSequence> entry = new ArrayList<CharSequence>();
											entry.add(appInfo.loadLabel(pn.getPackageManager()));
											entry.add(appInfo.packageName);
											if (appsList.contains(appInfo.packageName))
												entry.add("1");
											else
												entry.add("0");
											entries.add(entry);
										}
										
									    Collections.sort(entries, new Comparator<ArrayList<CharSequence>>() {
									        @Override
									        public int compare(ArrayList<CharSequence> entry1, ArrayList<CharSequence> entry2) {
									        	return ((String)entry2.get(2)).compareTo((String)entry1.get(2));
									        }
									    });
									    
									    ArrayList<CharSequence> entryLabels = new ArrayList<CharSequence>();
										ArrayList<CharSequence> entryVals = new ArrayList<CharSequence>();
										for (ArrayList<CharSequence> entry: entries) {
											entryLabels.add(entry.get(0));
											entryVals.add(entry.get(1));
										}
										
										bwlistApps.setEntries(entryLabels.toArray(new CharSequence[entries.size()]));
										bwlistApps.setEntryValues(entryVals.toArray(new CharSequence[entryVals.size()]));
										
										dialog.dismiss();
										if (bwlistApps.getDialog() != null) bwlistApps.getDialog().dismiss();
										bwlistApps.show();								
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}.start();
				}
				return false;
			}
		});
	}
	
	private void updateListType(boolean state) {
		HtcMultiSelectListPreference bwlistApps = (HtcMultiSelectListPreference)findPreference("pref_key_other_popupnotify_bwlist_apps");
		if (state) {
			bwlistApps.setTitle(R.string.various_popupnotify_bwlist_white_title);
			bwlistApps.setDialogTitle(R.string.various_popupnotify_bwlist_white_title);
			bwlistApps.setSummary(R.string.various_popupnotify_bwlist_white_summ);
		} else {
			bwlistApps.setTitle(R.string.various_popupnotify_bwlist_black_title);
			bwlistApps.setDialogTitle(R.string.various_popupnotify_bwlist_black_title);
			bwlistApps.setSummary(R.string.various_popupnotify_bwlist_black_summ);
		}
	}
	
	public static void getApps(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		pkgAppsList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		Collections.sort(pkgAppsList, new ApplicationInfo.DisplayNameComparator(pm));
	}

	@Override
	public void addPreferencesFromResource(int resId) {
		super.addPreferencesFromResource(resId);
		Helpers.applyLang(this, null);
	}
	
	private void applyThemeState(Boolean state) {
		OnOffSwitch.setChecked(state);
		prefListView.setEnabled(state);
		menuTest.setEnabled(state);
		if (state) {
			prefListView.setVisibility(View.VISIBLE);
			themeHint.setVisibility(View.GONE);
		} else {
			prefListView.setVisibility(View.GONE);
			themeHint.setVisibility(View.VISIBLE);
		}
	}
	
	private void startListen() {
		try {
			this.registerReceiver(recreateReceiver, filter);
		} catch (Exception e) {}
	}
	
	private void stopListen() {
		if (recreateReceiver != null) try {
			this.unregisterReceiver(recreateReceiver);
		} catch (Exception e) {}
	}
	
	protected void onResume() {
		super.onResume();
		int newThemeId = Helpers.getCurrentTheme(this);
		if (newThemeId != mThemeId) recreate();
	}
	
	protected void onRestart() {
		startListen();
		super.onRestart();
	}
	
	protected void onStop() {
		super.onStop();
		stopListen();
	}
}

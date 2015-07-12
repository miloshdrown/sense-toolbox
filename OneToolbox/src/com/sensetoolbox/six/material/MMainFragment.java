package com.sensetoolbox.six.material;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.acra.ACRA;

import com.sensetoolbox.six.GateWay;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.material.utils.MaterialColorPicker;
import com.sensetoolbox.six.utils.Helpers;
import com.stericson.RootTools.RootTools;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.FrameLayout;
import android.widget.GridLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

public class MMainFragment extends MPreferenceFragmentExt {
	
	public boolean toolboxModuleActive = false;
	
	public MMainFragment() {
		super();
		this.setRetainInstance(true);
	}

	private Runnable showUpdateNotification = new Runnable() {
		@Override
		public void run() {
			if (isFragmentReady(getActivity())) try {
				TextView update = (TextView)getActivity().findViewById(R.id.update);
				update.setText(Helpers.l10n(getActivity(), R.string.update_available));
				update.setTextColor(getResources().getColor(android.R.color.background_light));
				
				FrameLayout updateFrame = (FrameLayout)getActivity().findViewById(R.id.updateFrame);
				updateFrame.setLayoutTransition(new LayoutTransition());
				updateFrame.setVisibility(View.VISIBLE);
				updateFrame.setBackgroundColor(0xff252525);
				updateFrame.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							Intent detailsIntent = new Intent();
							detailsIntent.setComponent(new ComponentName("de.robv.android.xposed.installer", "de.robv.android.xposed.installer.DownloadDetailsActivity"));
							detailsIntent.setData(Uri.fromParts("package", "com.sensetoolbox.six", null));
							startActivity(detailsIntent);
						} catch (Exception e) {
							Helpers.openURL(getActivity(), "http://sensetoolbox.com/6/download");
						}
					}
				});
			} catch (Exception e) {}
		}
	};
	
	private Runnable hideUpdateNotification = new Runnable() {
		@Override
		public void run() {
			if (isFragmentReady(getActivity())) try {
				FrameLayout updateFrame = (FrameLayout)getActivity().findViewById(R.id.updateFrame);
				updateFrame.setVisibility(View.GONE);
			} catch (Exception e) {}
		}
	};
	
	private boolean isFragmentReady(Activity act) {
		return act != null && !act.isFinishing() && ((MActivityEx)act).isActive && MMainFragment.this.isAdded();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState, R.xml.mpreferences);
		final Activity act = getActivity();
		final ProgressDialog checkingDlg = new ProgressDialog(act);
		final Handler handler = new Handler();
		
		addPreferencesFromResource(R.xml.mpreferences);
		
		ListView prefList = (ListView)getActivity().findViewById(android.R.id.list);
		prefList.setPadding(0, 0, 0, 0);
		final Typeface face = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
		final Typeface faceTitle = Typeface.create("sans-serif-condensed", Typeface.BOLD);
		final Typeface faceSecondary = Typeface.create("sans-serif-light", Typeface.NORMAL);
		prefList.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
			@Override
			public void onChildViewAdded(View parent, View child) {
				if (child != null) {
					ArrayList<View> nViews = Helpers.getChildViewsRecursive(child);
					for (View nView: nViews)
					if (nView != null && nView instanceof TextView) try {
						TextView tView = (TextView)nView;
						if (tView.getId() == android.R.id.title && tView.getParent() instanceof ListView) {
							tView.setTypeface(faceTitle);
							tView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
						} else if (tView.getId() == android.R.id.summary) {
							tView.setTypeface(faceSecondary);
							tView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
						} else {
							tView.setTypeface(face);
							tView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
						}
					} catch (Exception e) {}
				}
			}

			@Override
			public void onChildViewRemoved(View parent, View child) {}
		});
		/*
		if (Helpers.isSense7()) {
			TextView experimental = (TextView)act.findViewById(R.id.experimental);
			experimental.setText(Helpers.l10n(act, R.string.preview_release));
			experimental.setTextColor(getResources().getColor(android.R.color.background_light));
			FrameLayout experimentalFrame = (FrameLayout)act.findViewById(R.id.experimentalFrame);
			experimentalFrame.setVisibility(View.VISIBLE);
		}
		*/
		// Preventing launch delay
		new Thread(new Runnable() {
			public void run() {
				Runnable showCheck = new Runnable() {
					@Override
					public void run() {
						if (isFragmentReady(act)) try {
							checkingDlg.setMessage(Helpers.l10n(act, R.string.checking_root));
							checkingDlg.setCancelable(false);
							checkingDlg.show();
						} catch (Exception e) {}
					}
				};
				handler.postDelayed(showCheck, 1000);
				
				Helpers.hasRoot = RootTools.isRootAvailable();
				Helpers.hasRootAccess = RootTools.isAccessGiven();
				Helpers.hasBusyBox = RootTools.isBusyboxAvailable();
				
				if (!Helpers.hasRoot || !Helpers.hasRootAccess) {
					act.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (isFragmentReady(act)) try {
								Helpers.disablePref(MMainFragment.this, "pref_key_wakegest", Helpers.l10n(act, R.string.no_root_summ));
								Helpers.disablePref(MMainFragment.this, "pref_key_touchlock", Helpers.l10n(act, R.string.no_root_summ));
							} catch (Exception e) {}
						}
					});
				}
				
				if (!Helpers.isXposedInstallerInstalled(act))
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						AlertDialog.Builder builder = new AlertDialog.Builder(act);
						builder.setTitle(Helpers.l10n(act, R.string.xposed_not_found));
						builder.setMessage(Helpers.l10n(act, R.string.xposed_not_found_explain));
						builder.setNeutralButton(Helpers.l10n(act, R.string.okay), null);
						AlertDialog dlg = builder.create();
						if (isFragmentReady(act)) dlg.show();
					}
				}); else Helpers.checkForXposedFramework(new Runnable() {
					@Override
					public void run() {
						final Activity act = getActivity();
						if (isFragmentReady(act))
						if (!Helpers.isXposedFrameworkInstalled) {
							act.runOnUiThread(new Runnable() {
								public void run() {
									showXposedDialog(act);
								}
							});
						} else if (!toolboxModuleActive) {
							act.runOnUiThread(new Runnable() {
								public void run() {
									showXposedDialog2(act);
								}
							});
						}
					}
				});
				
				String toolboxPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SenseToolbox/";
				HttpURLConnection connection = null;
				
				try {
					URL url = new URL("http://sensetoolbox.com/last_build");
					connection = (HttpURLConnection)url.openConnection();
					connection.setDefaultUseCaches(false);
					connection.setUseCaches(false);
					connection.setRequestProperty("Pragma", "no-cache");
					connection.setRequestProperty("Cache-Control", "no-cache");
					connection.setRequestProperty("Expires", "-1");
					connection.connect();

					if (connection.getResponseCode() == HttpURLConnection.HTTP_OK || connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
						String last_build = "";
						
						try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
							last_build = reader.readLine().trim();
						} catch (Exception e) { e.printStackTrace(); }
						
						File tmp = new File(toolboxPath);
						if (!tmp.exists()) tmp.mkdirs();
						try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(toolboxPath + "last_build", false))) {
							writer.write(last_build);
						} catch (Exception e) { e.printStackTrace(); }
					}
				} catch (Exception e) {}
				
				try {
					if (connection != null) connection.disconnect();
				} catch (Exception e) {}
				
				try (InputStream inputFile = new FileInputStream(toolboxPath + "last_build")) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile));
					int last_build = 0;
					try {
						last_build = Integer.parseInt(reader.readLine().trim());
					} catch (Exception e) {}
					
					if (last_build != 0 && Helpers.buildVersion < last_build)
						handler.post(showUpdateNotification);
					else
						handler.post(hideUpdateNotification);
				} catch (Exception e) {}
				
				Runnable hideCheck = new Runnable() {
					@Override
					public void run() {
						try {
							if (checkingDlg != null && checkingDlg.isShowing()) checkingDlg.dismiss();
						} catch (Exception e) {}
					}
				};
				handler.removeCallbacks(showCheck);
				handler.post(hideCheck);
			}
		}).start();

		if (Helpers.prefs.getBoolean("pref_key_was_restore", false)) {
			Helpers.prefs.edit().putBoolean("pref_key_was_restore", false).commit();
			showRestoreInfoDialog();
		}

		//Add version name to support title
		try {
			PreferenceCategory supportCat = (PreferenceCategory) findPreference("pref_key_support");
			supportCat.setTitle(String.format(Helpers.l10n(act, R.string.support_version), act.getPackageManager().getPackageInfo(act.getPackageName(), 0).versionName));
		} catch (NameNotFoundException e) {
			//Shouldn't happen...
			e.printStackTrace();
		}

		CheckBoxPreference.OnPreferenceChangeListener toggleIcon = new CheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				PackageManager pm = act.getPackageManager();
				if ((Boolean)newValue)
					pm.setComponentEnabledSetting(new ComponentName(act, GateWay.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				else
					pm.setComponentEnabledSetting(new ComponentName(act, GateWay.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				return true;
			}
		};
		
		OnPreferenceClickListener changeHeaderColor = new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final MaterialColorPicker maColorPicker = new MaterialColorPicker(getActivity(), preference.getKey(), "MaterialThemeHeader");
				maColorPicker.setTitle(preference.getTitle());
				maColorPicker.show();
				int height = LayoutParams.WRAP_CONTENT;
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
				height = Math.round(getResources().getDisplayMetrics().heightPixels * 0.78f);
				maColorPicker.getWindow().setLayout(Math.round(getResources().getDisplayMetrics().density * 340), height);
				return true;
			}
		};
		
		OnPreferenceClickListener changeAccentColor = new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final MaterialColorPicker maColorPicker = new MaterialColorPicker(getActivity(), preference.getKey(), "MaterialThemeAccent");
				maColorPicker.setTitle(preference.getTitle());
				maColorPicker.show();
				int height = LayoutParams.WRAP_CONTENT;
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
				height = Math.round(getResources().getDisplayMetrics().heightPixels * 0.78f);
				maColorPicker.getWindow().setLayout(Math.round(getResources().getDisplayMetrics().density * 340), height);
				return true;
			}
		};
		
		ListPreference.OnPreferenceChangeListener changeBackgroundColor = new ListPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (act != null & !act.isFinishing()) ((MActivityEx)act).updateTheme(Integer.parseInt((String)newValue));
				return true;
			}
		};
		
		CheckBoxPreference.OnPreferenceClickListener openLang = new CheckBoxPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Helpers.openLangDialogM(getActivity());
				return true;
			}
		};
		
		CheckBoxPreference.OnPreferenceClickListener sendCrashReport = new CheckBoxPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ACRA.getErrorReporter().handleException(null);
				return true;
			}
		};
		
		CheckBoxPreference toolboxSettingsPreference = (CheckBoxPreference) findPreference("pref_key_toolbox_icon");
		if (toolboxSettingsPreference != null)
		toolboxSettingsPreference.setOnPreferenceChangeListener(toggleIcon);
		Preference toolboxAccentColorPreference = findPreference("pref_key_toolbox_material_accent");
		if (toolboxAccentColorPreference != null)
		toolboxAccentColorPreference.setOnPreferenceClickListener(changeAccentColor);
		Preference toolboxHeaderColorPreference = findPreference("pref_key_toolbox_material_header");
		if (toolboxHeaderColorPreference != null)
		toolboxHeaderColorPreference.setOnPreferenceClickListener(changeHeaderColor);
		Preference toolboxBackgroundColorPreference = findPreference("pref_key_toolbox_material_background");
		if (toolboxBackgroundColorPreference != null)
		toolboxBackgroundColorPreference.setOnPreferenceChangeListener(changeBackgroundColor);
		Preference toolboxLanguagePreference = (Preference) findPreference("pref_key_toolbox_lang");
		if (toolboxLanguagePreference != null)
		toolboxLanguagePreference.setOnPreferenceClickListener(openLang);
		Preference toolboxCrashReportPreference = (Preference) findPreference("pref_key_toolbox_sendreport");
		if (toolboxCrashReportPreference != null)
		toolboxCrashReportPreference.setOnPreferenceClickListener(sendCrashReport);
		
		Preference issueTrackerPreference = findPreference("pref_key_issuetracker");
		issueTrackerPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "https://bitbucket.org/langerhans/sense-toolbox/issues/");
				return true;
			}
		});
		Preference toolboxSitePreference = findPreference("pref_key_toolboxsite");
		toolboxSitePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "http://sensetoolbox.com/");
				return true;
			}
		});
		Preference donatePagePreference = findPreference("pref_key_donatepage");
		donatePagePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "http://sensetoolbox.com/donate");
				return true;
			}
		});
		Preference ARHDPreference = findPreference("pref_key_ARHD");
		ARHDPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "http://android-revolution-hd.blogspot.de");
				return true;
			}
		});
		Preference ARTMODPreference = findPreference("pref_key_ARTMOD");
		ARTMODPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "http://forum.xda-developers.com/htc-one/development/rom-artmod-sense-7-lollipop-1-32-401-8-t3064090");
				return true;
			}
		});
		
		if (Helpers.isSense7()) {
			Helpers.removePref(this, "pref_key_sms", "prefs_cat");
			Helpers.prefs.edit().putBoolean("pref_key_sms_smsmmsconv", false).commit();
			Helpers.prefs.edit().putBoolean("pref_key_sms_toastnotification", false).commit();
			Helpers.prefs.edit().putBoolean("pref_key_sms_mmssize", false).commit();
			Helpers.prefs.edit().putBoolean("pref_key_sms_accents", false).commit();
			Helpers.prefs.edit().putBoolean("pref_key_other_smscreenon", false).commit();
			Helpers.prefs.edit().putBoolean("pref_key_other_musicchannel", false).commit();
			Helpers.prefs.edit().putBoolean("pref_key_other_nochargerwarn", false).commit();
		}
		
		if (Helpers.isNewSense()) {
			Helpers.prefs.edit().putBoolean("themes_active", false).commit();
		}
	}
	
	// PreferenceScreens management
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen parentPreferenceScreen, Preference preference) {
		if (preference != null && preference instanceof PreferenceScreen) {
			Intent subActIntent = new Intent(getActivity(), MSubActivity.class);
			subActIntent.putExtra("pref_section_name", (String)preference.getTitle());
			int xmlResId = 0;
			
			switch (preference.getKey()) {
				case "pref_key_sysui":
					xmlResId = R.xml.mprefs_systemui;
					break;
				case "pref_key_cb":
					xmlResId = R.xml.mprefs_statusbar;
					break;
				case "pref_key_prism":
					xmlResId = R.xml.mprefs_prism;
					break;
				case "pref_key_sms":
					xmlResId = R.xml.mprefs_message;
					break;
				case "pref_key_controls":
					xmlResId = R.xml.mprefs_controls;
					break;
				case "pref_key_other":
					xmlResId = R.xml.mprefs_other;
					break;
				case "pref_key_betterheadsup":
					xmlResId = R.xml.mprefs_betterheadsup;
					break;
				case "pref_key_wakegest":
					if (Helpers.isWakeGestures() || Helpers.isEight()) {
						xmlResId = R.xml.mprefs_wakegest;
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle(Helpers.l10n(getActivity(), R.string.warning));
						builder.setMessage(Helpers.l10n(getActivity(), R.string.wakegestures_not_supported));
						builder.setNeutralButton(Helpers.l10n(getActivity(), R.string.okay), null);
						AlertDialog dlg = builder.create();
						dlg.show();
					}
					break;
				case "pref_key_touchlock":
					if (Helpers.isWakeGestures()) {
						getActivity().startActivity(new Intent(getActivity(), MTouchLock.class));
						return true;
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle(Helpers.l10n(getActivity(), R.string.warning));
						builder.setMessage(Helpers.l10n(getActivity(), R.string.touchlock_not_supported));
						builder.setNeutralButton(Helpers.l10n(getActivity(), R.string.okay), null);
						AlertDialog dlg = builder.create();
						dlg.show();
					}
					break;
			}
			
			if (xmlResId != 0) {
				subActIntent.putExtra("pref_section_xml", xmlResId);
				getActivity().startActivity(subActIntent);
				return true;
			}
		}
		return super.onPreferenceTreeClick(parentPreferenceScreen, preference);
	}
	
	public void showXposedDialog(Activity act) {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			builder.setTitle(Helpers.l10n(act, R.string.warning));
			builder.setMessage(Helpers.l10n(act, R.string.xposed_not_installed));
			builder.setCancelable(true);
			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton){}
			});
			AlertDialog dlg = builder.create();
			dlg.show();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void showXposedDialog2(Activity act) {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			builder.setTitle(Helpers.l10n(act, R.string.warning));
			builder.setMessage(Helpers.l10n(act, R.string.module_not_active));
			builder.setCancelable(true);
			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton){}
			});
			AlertDialog dlg = builder.create();
			dlg.show();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	private void showRestoreInfoDialog() {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(Helpers.l10n(getActivity(), R.string.warning));
			builder.setMessage(Helpers.l10n(getActivity(), R.string.backup_restore_info));
			builder.setCancelable(true);
			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton){}
			});
			AlertDialog dlg = builder.create();
			dlg.show();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}

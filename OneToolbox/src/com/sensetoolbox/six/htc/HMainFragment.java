package com.sensetoolbox.six.htc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.acra.ACRA;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.PopupWindow.OnDismissListener;

import com.htc.app.HtcProgressDialog;
import com.htc.preference.HtcCheckBoxPreference;
import com.htc.preference.HtcPreference;
import com.htc.preference.HtcPreference.OnPreferenceClickListener;
import com.htc.preference.HtcPreferenceCategory;
import com.htc.preference.HtcPreferenceScreen;
import com.htc.widget.HtcAlertDialog;
import com.htc.widget.quicktips.PopupBubbleWindow.OnUserDismissListener;
import com.htc.widget.quicktips.QuickTipPopup;
import com.sensetoolbox.six.GateWay;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.UISwitcher;
import com.sensetoolbox.six.utils.Helpers;
import com.stericson.RootTools.RootTools;

public class HMainFragment extends HPreferenceFragmentExt {
	
	public boolean toolboxModuleActive = false;
	
	public HMainFragment() {
		super();
		this.setRetainInstance(true);
	}
	
	private Runnable showUpdateNotification = new Runnable() {
		@Override
		public void run() {
			if (isFragmentReady(getActivity())) try {
				TypedValue typedValue = new TypedValue();
				getActivity().getTheme().resolveAttribute(getResources().getIdentifier("multiply_color", "attr", "com.htc"), typedValue, true);
				int multiply_color = typedValue.data;
				
				TextView update = (TextView)getActivity().findViewById(R.id.update);
				update.setText(Helpers.l10n(getActivity(), R.string.update_available));
				update.setTextColor(getResources().getColor(android.R.color.background_light));
				
				FrameLayout updateFrame = (FrameLayout)getActivity().findViewById(R.id.updateFrame);
				updateFrame.setLayoutTransition(new LayoutTransition());
				updateFrame.setVisibility(View.VISIBLE);
				updateFrame.setBackgroundColor(multiply_color);
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
		return act != null && !act.isFinishing() && ((HActivityEx)act).isActive && HMainFragment.this.isAdded();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState, R.xml.preferences);
		final Activity act = getActivity();
		final HtcProgressDialog checkingDlg = new HtcProgressDialog(act);
		final Handler handler = new Handler();
		
		addPreferencesFromResource(R.xml.preferences);
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
								Helpers.disablePref(HMainFragment.this, "pref_key_wakegest", Helpers.l10n(act, R.string.no_root_summ));
								Helpers.disablePref(HMainFragment.this, "pref_key_touchlock", Helpers.l10n(act, R.string.no_root_summ));
							} catch (Exception e) {}
						}
					});
				}
				
				if (!Helpers.isXposedInstallerInstalled(act))
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(act);
						builder.setTitle(Helpers.l10n(act, R.string.xposed_not_found));
						builder.setMessage(Helpers.l10n(act, R.string.xposed_not_found_explain));
						builder.setNeutralButton(Helpers.l10n(act, R.string.okay), null);
						HtcAlertDialog dlg = builder.create();
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
			HtcPreferenceCategory supportCat = (HtcPreferenceCategory) findPreference("pref_key_support");
			supportCat.setTitle(String.format(Helpers.l10n(act, R.string.support_version), act.getPackageManager().getPackageInfo(act.getPackageName(), 0).versionName));
		} catch (NameNotFoundException e) {
			//Shouldn't happen...
			e.printStackTrace();
		}

		HtcCheckBoxPreference.OnPreferenceChangeListener toggleIcon = new HtcCheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
				PackageManager pm = act.getPackageManager();
				if ((Boolean)newValue)
					pm.setComponentEnabledSetting(new ComponentName(act, GateWay.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				else
					pm.setComponentEnabledSetting(new ComponentName(act, GateWay.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				return true;
			}
		};
		
		HtcCheckBoxPreference.OnPreferenceClickListener openLang = new HtcCheckBoxPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(HtcPreference preference) {
				Helpers.openLangDialogH(getActivity());
				return true;
			}
		};
		
		HtcCheckBoxPreference.OnPreferenceClickListener sendCrashReport = new HtcCheckBoxPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(HtcPreference preference) {
				ACRA.getErrorReporter().handleException(null);
				return true;
			}
		};
		
		HtcCheckBoxPreference toolboxSettingsPreference = (HtcCheckBoxPreference) findPreference("pref_key_toolbox_icon");
		if (toolboxSettingsPreference != null)
		toolboxSettingsPreference.setOnPreferenceChangeListener(toggleIcon);
		HtcPreference toolboxLanguagePreference = (HtcPreference) findPreference("pref_key_toolbox_lang");
		if (toolboxLanguagePreference != null)
		toolboxLanguagePreference.setOnPreferenceClickListener(openLang);
		HtcPreference toolboxCrashReportPreference = (HtcPreference) findPreference("pref_key_toolbox_sendreport");
		if (toolboxCrashReportPreference != null)
		toolboxCrashReportPreference.setOnPreferenceClickListener(sendCrashReport);
		
		HtcPreference issueTrackerPreference = findPreference("pref_key_issuetracker");
		issueTrackerPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(HtcPreference pref) {
				Helpers.openURL(act, "https://bitbucket.org/langerhans/sense-toolbox/issues/");
				return true;
			}
		});
		HtcPreference toolboxSitePreference = findPreference("pref_key_toolboxsite");
		toolboxSitePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(HtcPreference pref) {
				Helpers.openURL(act, "http://sensetoolbox.com/");
				return true;
			}
		});
		HtcPreference donatePagePreference = findPreference("pref_key_donatepage");
		donatePagePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(HtcPreference pref) {
				Helpers.openURL(act, "http://sensetoolbox.com/donate");
				return true;
			}
		});
		HtcPreference ARHDPreference = findPreference("pref_key_ARHD");
		ARHDPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(HtcPreference pref) {
				Helpers.openURL(act, "http://android-revolution-hd.blogspot.de");
				return true;
			}
		});
		HtcPreference ARTMODPreference = findPreference("pref_key_ARTMOD");
		ARTMODPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(HtcPreference pref) {
				Helpers.openURL(act, "http://forum.xda-developers.com/htc-one/development/rom-artmod-sense-7-lollipop-1-32-401-8-t3064090");
				return true;
			}
		});
		
		HtcCheckBoxPreference forceMaterialPreference = (HtcCheckBoxPreference)findPreference("pref_key_toolbox_force_material");
		forceMaterialPreference.setOnPreferenceChangeListener(new HtcCheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(HtcPreference preference, Object newValue) {
				Activity act = getActivity();
				act.startActivity(new Intent(act, UISwitcher.class));
				act.finish();
				return true;
			}
		});
		
		if (Helpers.isLP())
			Helpers.removePref(this, "pref_key_popupnotify", "prefs_cat");
		else
			Helpers.removePref(this, "pref_key_betterheadsup", "prefs_cat");
		
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
		
		Helpers.removePref(this, "pref_key_toolbox_force_material", "pref_key_toolbox");
	}

	private void showQuickTip(int step) {
		if (qtp == null) try {
			int width = getWidthWithPadding();
			qtp = new QuickTipPopup(getActivity());
			qtp.setCloseVisibility(true);
			qtp.setClipToScreenEnabled(true);
			qtp.setMaxWidth(width);
			qtp.setWidth(width);
		} catch (Exception e) {}
		
		if (qtp == null) return;
		
		View target = null;
		if (step == 0) {
			target = getActivity().findViewById(R.id.softreboot);
			if (target == null || !getQuickTipFlag("soft_reboot")) showQuickTip(1); else {
				qtp.setExpandDirection(QuickTipPopup.EXPAND_DOWN);
				qtp.setText(Helpers.l10n(getActivity(), R.string.soft_reboot_tip));
				qtp.setOnUserDismissListener(new OnUserDismissListener() {
					@Override
					public void onDismiss() {
						disableQuickTipFlag("soft_reboot");
						enableTouch();
						showQuickTip(1);
					}
				});
				disableTouch();
				qtp.showAsDropDown(target);
			}
		} else if (step == 1) {
			target = getActivity().findViewById(R.id.backuprestore);
			if (target == null || !getQuickTipFlag("backup_restore")) showQuickTip(2); else {
				qtp.setExpandDirection(QuickTipPopup.EXPAND_DOWN);
				qtp.setText(Helpers.l10n(getActivity(), R.string.backup_restore_tip));
				qtp.setOnUserDismissListener(new OnUserDismissListener() {
					@Override
					public void onDismiss() {
						disableQuickTipFlag("backup_restore");
						enableTouch();
						showQuickTip(2);
					}
				});
				disableTouch();
				qtp.showAsDropDown(target);
			}
		} else if (step == 2 && getQuickTipFlag("toolbox_acramail")) {
			this.getHtcListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			this.getHtcListView().smoothScrollToPosition(17);
			this.getHtcListView().postDelayed(new Runnable() {
				@Override
				public void run() {
					int pos = 17 - HMainFragment.this.getHtcListView().getFirstVisiblePosition();
					View feedback = null;
					if (pos < HMainFragment.this.getHtcListView().getChildCount())
					feedback = HMainFragment.this.getHtcListView().getChildAt(pos);
					
					if (feedback == null) {
						HMainFragment.this.getHtcListView().smoothScrollToPosition(0);
					} else {
						qtp.setExpandDirection(QuickTipPopup.EXPAND_UP);
						qtp.setText(Helpers.l10n(getActivity(), R.string.toolbox_acramail_tip));
						qtp.setOnUserDismissListener(new OnUserDismissListener() {
							@Override
							public void onDismiss() {
								disableQuickTipFlag("toolbox_acramail");
								HMainFragment.this.getHtcListView().smoothScrollToPosition(0);
								enableTouch();
							}
						});
						qtp.setOnDismissListener(new OnDismissListener() {
							@Override
							public void onDismiss() {
								if (!qtp.isShowing()) disableQuickTipFlag("toolbox_acramail");
							}
						});
						qtp.showAsDropDown(feedback);
					}
				}
			}, 700);
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (qtp != null) {
			int width = getWidthWithPadding();
			qtp.setMaxWidth(width);
			qtp.setWidth(width);
			qtp.update();
		}
	}
	
	public static class SysUIFragment extends HPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_systemui);
			addPreferencesFromResource(R.xml.prefs_systemui);
		}
	}
	
	public static class StatusBarFragment extends HPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_statusbar);
			addPreferencesFromResource(R.xml.prefs_statusbar);
		}
	}
	
	public static class PrismFragment extends HPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_prism);
			addPreferencesFromResource(R.xml.prefs_prism);
		}
	}
	
	public static class MessageFragment extends HPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_message);

		}
	}
	
	public static class ControlsFragment extends HPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_controls);
			addPreferencesFromResource(R.xml.prefs_controls);
		}
	}
	
	public static class WakeGesturesFragment extends HPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_wakegest);
			addPreferencesFromResource(R.xml.prefs_wakegest);
		}
	}
	
	public static class OtherFragment extends HPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_other);
			addPreferencesFromResource(R.xml.prefs_other);
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((FrameLayout)getActivity().findViewById(R.id.fragment_container)).setBackgroundResource(getResources().getIdentifier("common_app_bkg", "drawable", "com.htc"));
		
		view.post(new Runnable() {
			@Override
			public void run() {
				if (isFragmentReady(getActivity())) showQuickTip(0);
			}
		});
	}
	
	// HtcPreferenceScreens management
	@Override
	public boolean onPreferenceTreeClick(HtcPreferenceScreen parentPreferenceScreen, HtcPreference preference) {
		if (preference != null && preference instanceof HtcPreferenceScreen) {
			Intent subActIntent = new Intent(getActivity(), HSubActivity.class);
			subActIntent.putExtra("pref_section_name", (String)preference.getTitle());
			int xmlResId = 0;
			
			switch (preference.getKey()) {
				case "pref_key_sysui":
					xmlResId = R.xml.prefs_systemui;
					break;
				case "pref_key_cb":
					xmlResId = R.xml.prefs_statusbar;
					break;
				case "pref_key_prism":
					xmlResId = R.xml.prefs_prism;
					break;
				case "pref_key_sms":
					xmlResId = R.xml.prefs_message;
					break;
				case "pref_key_controls":
					xmlResId = R.xml.prefs_controls;
					break;
				case "pref_key_other":
					xmlResId = R.xml.prefs_other;
					break;
				case "pref_key_popupnotify":
					xmlResId = R.xml.prefs_popupnotify;
					break;
				case "pref_key_betterheadsup":
					xmlResId = R.xml.prefs_betterheadsup;
					break;
				case "pref_key_wakegest":
					if (Helpers.isWakeGestures() || Helpers.isEight()) {
						xmlResId = R.xml.prefs_wakegest;
					} else {
						HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(getActivity());
						builder.setTitle(Helpers.l10n(getActivity(), R.string.warning));
						builder.setMessage(Helpers.l10n(getActivity(), R.string.wakegestures_not_supported));
						builder.setNeutralButton(Helpers.l10n(getActivity(), R.string.okay), null);
						HtcAlertDialog dlg = builder.create();
						dlg.show();
					}
					break;
				case "pref_key_touchlock":
					if (Helpers.isWakeGestures()) {
						getActivity().startActivity(new Intent(getActivity(), HTouchLock.class));
						return true;
					} else {
						HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(getActivity());
						builder.setTitle(Helpers.l10n(getActivity(), R.string.warning));
						builder.setMessage(Helpers.l10n(getActivity(), R.string.touchlock_not_supported));
						builder.setNeutralButton(Helpers.l10n(getActivity(), R.string.okay), null);
						HtcAlertDialog dlg = builder.create();
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
			HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(act);
			builder.setTitle(Helpers.l10n(act, R.string.warning));
			builder.setMessage(Helpers.l10n(act, R.string.xposed_not_installed));
			builder.setCancelable(true);
			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton){}
			});
			HtcAlertDialog dlg = builder.create();
			dlg.show();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void showXposedDialog2(Activity act) {
		try {
			HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(act);
			builder.setTitle(Helpers.l10n(act, R.string.warning));
			builder.setMessage(Helpers.l10n(act, R.string.module_not_active));
			builder.setCancelable(true);
			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton){}
			});
			HtcAlertDialog dlg = builder.create();
			dlg.show();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	private void showRestoreInfoDialog() {
		try {
			HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(getActivity());
			builder.setTitle(Helpers.l10n(getActivity(), R.string.warning));
			builder.setMessage(Helpers.l10n(getActivity(), R.string.backup_restore_info));
			builder.setCancelable(true);
			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton){}
			});
			HtcAlertDialog dlg = builder.create();
			dlg.show();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
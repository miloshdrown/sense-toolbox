package com.sensetoolbox.six;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.acra.ACRA;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;

import com.htc.preference.HtcCheckBoxPreference;
import com.htc.preference.HtcPreference;
import com.htc.preference.HtcPreference.OnPreferenceClickListener;
import com.htc.preference.HtcPreferenceCategory;
import com.htc.preference.HtcPreferenceScreen;
import com.htc.widget.HtcAlertDialog;
import com.htc.widget.quicktips.PopupBubbleWindow.OnUserDismissListener;
import com.htc.widget.quicktips.QuickTipPopup;
import com.sensetoolbox.six.utils.Helpers;
import com.sensetoolbox.six.utils.HtcPreferenceFragmentExt;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class MainFragment extends HtcPreferenceFragmentExt {
	
	public static String lastShortcutKey = null;
	public static String lastShortcutKeyContents = null;
	private boolean toolboxModuleActive = false;
	
	public MainFragment() {
		super();
		this.setRetainInstance(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState, R.xml.preferences);
		final Activity act = getActivity();
		
		// Preventing launch delay
		new Thread(new Runnable() {
			public void run() {
				Helpers.hasRoot = RootTools.isRootAvailable();
				Helpers.hasBusyBox = RootTools.isBusyboxAvailable();
				
				if (!Helpers.isXposedInstalled(act))
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(act);
						builder.setTitle(Helpers.l10n(act, R.string.xposed_not_found));
						builder.setMessage(Helpers.l10n(act, R.string.xposed_not_found_explain));
						builder.setIcon(android.R.drawable.ic_dialog_alert);
						builder.setNeutralButton(Helpers.l10n(act, R.string.okay), null);
						HtcAlertDialog dlg = builder.create();
						dlg.show();
					}
				}); else checkForXposed();
			}
		}).start();
				
		addPreferencesFromResource(R.xml.preferences);
		
		//Save current Sense version into the sharedprefs
		String senseVer = Helpers.getSenseVersion();
		prefs.edit().putString("pref_sense_version", senseVer).commit();
		
		if (prefs.getBoolean("pref_key_was_restore", false)) {
			prefs.edit().putBoolean("pref_key_was_restore", false).commit();
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
				Helpers.openLangDialog(getActivity());
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
				openURL("https://bitbucket.org/langerhans/sense-toolbox/issues/");
				return true;
			}
		});
		HtcPreference toolboxSitePreference = findPreference("pref_key_toolboxsite");
		toolboxSitePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(HtcPreference pref) {
				openURL("http://sensetoolbox.com/");
				return true;
			}
		});
		HtcPreference donatePagePreference = findPreference("pref_key_donatepage");
		donatePagePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(HtcPreference pref) {
				openURL("http://sensetoolbox.com/donate");
				return true;
			}
		});
		HtcPreference ARHDPreference = findPreference("pref_key_ARHD");
		ARHDPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(HtcPreference pref) {
				openURL("http://android-revolution-hd.blogspot.de");
				return true;
			}
		});
		
		if (Helpers.isLP())
			Helpers.removePref(this, "pref_key_popupnotify", "prefs_cat");
		else
			Helpers.removePref(this, "pref_key_betterheadsup", "prefs_cat");
	}
	
	private void showQuickTip(int step) {
		if (qtp == null) {
			int width = getWidthWithPadding();
			qtp = new QuickTipPopup(getActivity());
			qtp.setCloseVisibility(true);
			qtp.setClipToScreenEnabled(true);
			qtp.setMaxWidth(width);
			qtp.setWidth(width);
		}
		
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
					int pos = 17 - MainFragment.this.getHtcListView().getFirstVisiblePosition();
					View feedback = null;
					if (pos < MainFragment.this.getHtcListView().getChildCount())
					feedback = MainFragment.this.getHtcListView().getChildAt(pos);
					
					if (feedback == null) {
						MainFragment.this.getHtcListView().smoothScrollToPosition(0);
					} else {
						qtp.setExpandDirection(QuickTipPopup.EXPAND_UP);
						qtp.setText(Helpers.l10n(getActivity(), R.string.toolbox_acramail_tip));
						qtp.setOnUserDismissListener(new OnUserDismissListener() {
							@Override
							public void onDismiss() {
								disableQuickTipFlag("toolbox_acramail");
								MainFragment.this.getHtcListView().smoothScrollToPosition(0);
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
	
	private void openURL(String url) {
		Intent uriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		if (uriIntent.resolveActivity(getActivity().getPackageManager()) != null) {
			startActivity(uriIntent);
		} else {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(getActivity());
			alert.setTitle(Helpers.l10n(getActivity(), R.string.warning));
			alert.setView(Helpers.createCenteredText(getActivity(), R.string.no_browser));
			alert.setCancelable(true);
			alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
		}
	}
	
	public static class SysUIFragment extends HtcPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_systemui);
			addPreferencesFromResource(R.xml.prefs_systemui);
		}
	}
	
	public static class StatusBarFragment extends HtcPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_statusbar);
			addPreferencesFromResource(R.xml.prefs_statusbar);
		}
	}
	
	public static class PrismFragment extends HtcPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_prism);
			addPreferencesFromResource(R.xml.prefs_prism);
		}
	}
	
	public static class MessageFragment extends HtcPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_message);

		}
	}
	
	public static class ControlsFragment extends HtcPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_controls);
			addPreferencesFromResource(R.xml.prefs_controls);
		}
	}
	
	public static class WakeGesturesFragment extends HtcPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_wakegest);
			addPreferencesFromResource(R.xml.prefs_wakegest);
		}
	}
	
	public static class OtherFragment extends HtcPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_other);
			addPreferencesFromResource(R.xml.prefs_other);
		}
	}
	
	public static class PersistFragment extends HtcPreferenceFragmentExt {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState, R.xml.prefs_persist);
			addPreferencesFromResource(R.xml.prefs_persist);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static class HelperReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(final Context context, Intent intent) {
			if (intent.getAction() != null)
			if (intent.getAction().equals("com.sensetoolbox.six.BLOCKHEADSUP")) {
				String pkgName = intent.getStringExtra("pkgName");
				if (pkgName == null) return;
				if (prefs == null) prefs = context.getSharedPreferences("one_toolbox_prefs", 1);
				HashSet<String> appsList = new HashSet<String>(prefs.getStringSet("pref_key_betterheadsup_bwlist_apps", new HashSet<String>()));
				appsList.add(pkgName);
				prefs.edit().putStringSet("pref_key_betterheadsup_bwlist_apps", new HashSet<String>(appsList)).commit();
			} else if (intent.getAction().equals("android.intent.action.LOCALE_CHANGED")) {
				Helpers.l10n = null;
				Helpers.cLang = "";
			} else {
				if (Helpers.isNotM7()) return;
				final int thepref = Integer.parseInt(context.getSharedPreferences("one_toolbox_prefs", 1).getString("pref_key_other_keyslight", "1"));
				if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
					if (thepref > 1) Helpers.setButtonBacklightTo(context, thepref, false);
					CommandCapture command = new CommandCapture(0, "getenforce 2>/dev/null") {
						int lineCnt = 0;
						
						@Override
						public void commandOutput(int id, String line) {
							if (lineCnt > 0) return;
							boolean isSELinuxEnforcing = line.trim().equalsIgnoreCase("enforcing");
							Settings.System.putString(context.getContentResolver(), "isSELinuxEnforcing", String.valueOf(isSELinuxEnforcing));
							if (isSELinuxEnforcing && thepref > 1) Helpers.setButtonBacklightTo(context, thepref, false);
							lineCnt++;
						}
					};
					try {
						RootTools.getShell(false).add(command);
					} catch (Exception e) {
						// handle exception
					}
				} else if (intent.getAction().equals("com.sensetoolbox.six.UPDATEBACKLIGHT")) {
					boolean forceDisableBacklight = false;
					PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
					if (!pm.isScreenOn())
						forceDisableBacklight = true;
					else
						forceDisableBacklight = intent.getBooleanExtra("forceDisableBacklight", false);
					
					if (forceDisableBacklight)
						Helpers.setButtonBacklightTo(context, 5, false);
					else
						Helpers.setButtonBacklightTo(context, thepref, false);
				}
			}
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((FrameLayout)getActivity().findViewById(R.id.fragment_container)).setBackgroundResource(getResources().getIdentifier("common_app_bkg", "drawable", "com.htc"));
		
		view.post(new Runnable() {
			@Override
			public void run() {
				if (MainFragment.this.isAdded()) showQuickTip(0);
			}
		});
	}
	
	// HtcPreferenceScreens management
	@Override
	public boolean onPreferenceTreeClick(HtcPreferenceScreen parentPreferenceScreen, HtcPreference preference) {
		if (preference != null && preference instanceof HtcPreferenceScreen) {
			Intent subActIntent = new Intent(getActivity(), SubActivity.class);
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
				case "pref_key_persist":
					xmlResId = R.xml.prefs_persist;
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
						builder.setIcon(android.R.drawable.ic_dialog_alert);
						builder.setNeutralButton(Helpers.l10n(getActivity(), R.string.okay), null);
						HtcAlertDialog dlg = builder.create();
						dlg.show();
					}
					break;
				case "pref_key_touchlock":
					if (Helpers.isWakeGestures()) {
						getActivity().startActivity(new Intent(getActivity(), TouchLock.class));
						return true;
					} else {
						HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(getActivity());
						builder.setTitle(Helpers.l10n(getActivity(), R.string.warning));
						builder.setMessage(Helpers.l10n(getActivity(), R.string.touchlock_not_supported));
						builder.setIcon(android.R.drawable.ic_dialog_alert);
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
	
	public static boolean isXposedInstalled = false;
	private int lineCount = 0;
	
	public void checkForXposed() {
		CommandCapture command = new CommandCapture(0, "/system/bin/app_process --xposedversion 2>/dev/null") {
			@Override
			public void commandOutput(int id, String line) {
				if (lineCount > 0) return;
				Pattern pattern = Pattern.compile("Xposed version: (\\d+)");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String xposed_ver = matcher.group(1);
					try {
						Integer.parseInt(xposed_ver);
						isXposedInstalled = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				final Activity act = getActivity();
				if (act != null)
				if (!isXposedInstalled) {
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
				lineCount++;
			}
		};
		try {
			RootTools.getShell(false).add(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showXposedDialog(Activity act) {
		try {
			HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(act);
			builder.setTitle(Helpers.l10n(act, R.string.warning));
			builder.setMessage(Helpers.l10n(act, R.string.xposed_not_installed));
			builder.setIcon(android.R.drawable.ic_dialog_alert);
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
			builder.setIcon(android.R.drawable.ic_dialog_alert);
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
			builder.setIcon(android.R.drawable.ic_dialog_alert);
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
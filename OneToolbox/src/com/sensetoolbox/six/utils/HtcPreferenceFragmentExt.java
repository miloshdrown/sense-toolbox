package com.sensetoolbox.six.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.htc.app.HtcProgressDialog;
import com.htc.preference.HtcPreference;
import com.htc.preference.HtcPreferenceFragment;
import com.htc.preference.HtcPreferenceManager;
import com.htc.preference.HtcPreferenceScreen;
import com.htc.preference.HtcPreference.OnPreferenceChangeListener;
import com.htc.widget.ActionBarItemView;
import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcListView;
import com.htc.widget.HtcToggleButtonLight;
import com.htc.widget.HtcToggleButtonLight.OnCheckedChangeListener;
import com.htc.widget.quicktips.QuickTipPopup;
import com.htc.wrap.android.provider.HtcWrapSettings;
import com.sensetoolbox.six.AboutScreen;
import com.sensetoolbox.six.ActivityEx;
import com.sensetoolbox.six.MainActivity;
import com.sensetoolbox.six.R;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HtcPreferenceFragmentExt extends HtcPreferenceFragment {
	public static SharedPreferences prefs = null;
	public HtcToggleButtonLight OnOffSwitch;
	public ActionBarItemView menuTest;
	public HtcListView prefListView;
	public LinearLayout contentsView;
	public TextView themeHint;
	public int rebootType = 0;
	public int menuType = 0;
	public QuickTipPopup qtp = null;
	
	protected int getWidthWithPadding() {
		float padding = 0.9f;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) padding = 0.8f;
		return Math.round(getResources().getDisplayMetrics().widthPixels * padding);
	}
	
	protected boolean getQuickTipFlag(String tipName) {
		return HtcWrapSettings.System.getQuickTipFlag(getActivity().getContentResolver(), "com.sensetoolbox.six.tip." + tipName);
	}
	
	protected void disableQuickTipFlag(String tipName) {
		HtcWrapSettings.System.disableQuickTipFlag(getActivity().getContentResolver(), "com.sensetoolbox.six.tip." + tipName);
	}
	
	protected void disableTouch() {
		getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}
	
	protected void enableTouch() {
		getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}
	
	private boolean handleOptionsItemSelected(final Activity act, MenuItem item) {
		if (item.getItemId() == R.id.softreboot) {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(act);
			if (rebootType == 1) {
				alert.setTitle(Helpers.l10n(act, R.string.restart_prism));
				alert.setView(Helpers.createCenteredText(act, R.string.restartprism_explain_prefs));
			} else if (rebootType == 2) {
				alert.setTitle(Helpers.l10n(act, R.string.restart_messages));
				alert.setView(Helpers.createCenteredText(act, R.string.restartmessages_explain_prefs));
			} else {
				alert.setTitle(Helpers.l10n(act, R.string.soft_reboot));
				alert.setView(Helpers.createCenteredText(act, R.string.hotreboot_explain_prefs));
			}
			alert.setPositiveButton(Helpers.l10n(act, R.string.yes) + "!", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						if (rebootType == 1) {
							getActivity().sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.RestartPrism"));
							Toast.makeText(getActivity(), Helpers.l10n(act, R.string.restarted_prism), Toast.LENGTH_SHORT).show();
						} else if (rebootType == 2) {
							getActivity().sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.RestartMessages"));
							Toast.makeText(getActivity(), Helpers.l10n(act, R.string.restarted_messages), Toast.LENGTH_SHORT).show();
						} else {
							Command command = new Command(0, false, "setprop ctl.restart zygote");
							RootTools.getShell(true).add(command);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			alert.setNegativeButton(Helpers.l10n(act, R.string.no) + "!", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return true;
		} else if (item.getItemId() == R.id.backuprestore) {
			final String backupPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SenseToolbox/";
			final String backupFile = "settings_backup";
			
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(act);
			alert.setTitle(Helpers.l10n(act, R.string.backup_restore));
			alert.setView(Helpers.createCenteredText(act, R.string.backup_restore_choose));
			alert.setPositiveButton(Helpers.l10n(act, R.string.do_restore), new DialogInterface.OnClickListener() {
				@SuppressWarnings("unchecked")
				public void onClick(DialogInterface dialog, int whichButton) {
					if (!Helpers.checkStorageReadable(act)) return;
					ObjectInputStream input = null;
					try {
						input = new ObjectInputStream(new FileInputStream(backupPath + backupFile));
						Map<String, ?> entries = (Map<String, ?>)input.readObject();
						if (entries == null || entries.isEmpty()) throw new Exception("Cannot read entries");
							
						Editor prefEdit = prefs.edit();
						prefEdit.clear();
						for (Entry<String, ?> entry: entries.entrySet()) {
							Object val = entry.getValue();
							String key = entry.getKey();

							if (val instanceof Boolean)
								prefEdit.putBoolean(key, ((Boolean)val).booleanValue());
							else if (val instanceof Float)
								prefEdit.putFloat(key, ((Float)val).floatValue());
							else if (val instanceof Integer)
								prefEdit.putInt(key, ((Integer)val).intValue());
							else if (val instanceof Long)
								prefEdit.putLong(key, ((Long)val).longValue());
							else if (val instanceof String)
								prefEdit.putString(key, ((String)val));
							else if (val instanceof Set<?>)
								prefEdit.putStringSet(key, ((Set<String>)val));
						}
						prefEdit.commit();
						
						HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(act);
						alert.setTitle(Helpers.l10n(act, R.string.do_restore));
						alert.setView(Helpers.createCenteredText(act, R.string.restore_ok));
						alert.setCancelable(false);
						alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								act.finish();
								act.startActivity(act.getIntent());
							}
						});
						alert.show();
					} catch (Exception e) {
						HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(act);
						alert.setTitle(Helpers.l10n(act, R.string.warning));
						alert.setView(Helpers.createCenteredText(act, R.string.storage_cannot_restore));
						alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {}
						});
						alert.show();
					} finally {
						try {
							if (input != null) input.close();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			});
			alert.setNegativeButton(Helpers.l10n(act, R.string.do_backup), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if (!Helpers.preparePathForBackup(act, backupPath)) return;
					ObjectOutputStream output = null;
					try {
						output = new ObjectOutputStream(new FileOutputStream(backupPath + backupFile));
						output.writeObject(prefs.getAll());
						
						HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(act);
						alert.setTitle(Helpers.l10n(act, R.string.do_backup));
						alert.setView(Helpers.createCenteredText(act, R.string.backup_ok));
						alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {}
						});
						alert.show();
					} catch (Exception e) {
						HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(act);
						alert.setTitle(Helpers.l10n(act, R.string.warning));
						alert.setView(Helpers.createCenteredText(act, R.string.storage_cannot_backup));
						alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {}
						});
						alert.show();
						
						e.printStackTrace();
					} finally {
						try {
							if (output != null) {
								output.flush();
								output.close();
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			});
			alert.setNeutralButton(R.string.sense_themes_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return true;
		} else if (item.getItemId() == R.id.about) {
			Intent intent = new Intent(act, AboutScreen.class);
			act.startActivity(intent);
		}
		return true;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		
		if (menuType == 0) {
			inflater.inflate(R.menu.menu_mods, menu);
			
			menu.getItem(2).setIcon(Helpers.applySenseTheme(getActivity(), menu.getItem(2).getIcon()));
			if (rebootType == 1) {
				menu.getItem(0).setIcon(R.drawable.ic_menu_restart_prism);
				menu.getItem(0).setTitle(Helpers.l10n(getActivity(), R.string.restart_prism));
			} else if (rebootType == 2) {
				menu.getItem(0).setIcon(R.drawable.ic_menu_restart_message);
				menu.getItem(0).setTitle(Helpers.l10n(getActivity(), R.string.restart_messages));
			}
		} else if (menuType == 1) {
			inflater.inflate(R.menu.menu_sub, menu);
			
			OnOffSwitch = (HtcToggleButtonLight)menu.getItem(1).getActionView().findViewById(R.id.onoffSwitch);
			OnOffSwitch.setEnabled(true);
			OnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(HtcToggleButtonLight toggle, boolean state) {
					prefs.edit().putBoolean("wake_gestures_active", state).commit();
					if (!Helpers.isEight()) Helpers.setWakeGestures(state);
					applyWGState(state);
				}
			});
			
			applyWGState(prefs.getBoolean("wake_gestures_active", false));
		} else if (menuType == 2) {
			inflater.inflate(R.menu.menu_sub, menu);
			
			OnOffSwitch = (HtcToggleButtonLight)menu.getItem(1).getActionView().findViewById(R.id.onoffSwitch);
			OnOffSwitch.setEnabled(true);
			OnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(HtcToggleButtonLight toggle, boolean state) {
					prefs.edit().putBoolean("eps_remap_active", state).commit();
					applyEPSState(state);
				}
			});
			
			applyEPSState(prefs.getBoolean("eps_remap_active", false));
			for (int i = 1; i <= 6; i++) initCell(i);
		} else if (menuType == 3) {
			OnOffSwitch = new HtcToggleButtonLight(getActivity());
			OnOffSwitch.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			OnOffSwitch.setEnabled(true);
			OnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(HtcToggleButtonLight toggle, boolean state) {
					prefs.edit().putBoolean("popup_notify_active", state).commit();
					applyPopupState(state);
				}
			});
			((ActivityEx)getActivity()).actionBarContainer.addRightView(OnOffSwitch);
			
			menuTest = new ActionBarItemView(getActivity());
			menuTest.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
			menuTest.setIcon(getResources().getIdentifier("icon_btn_view_dark", "drawable", "com.htc"));
			menuTest.setLongClickable(true);
			menuTest.setTitle(Helpers.l10n(getActivity(), R.string.popupnotify_test));
			menuTest.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final Activity act = getActivity();
					PendingIntent pendingintent = PendingIntent.getActivity(act, 0, new Intent(act, MainActivity.class), 0x10000000);
					PendingIntent pendingintent1 = PendingIntent.getActivity(act, 0, new Intent(), 0x10000000);
					Notification notification1 = (new Notification.Builder(act))
							.setContentTitle("Inbox Style notification")
							.setContentText("This is a test notification")
							.setAutoCancel(true)
							.setContentIntent(pendingintent)
							.setSmallIcon(R.drawable.apm_bootloader)
							.setLargeIcon(BitmapFactory.decodeResource(act.getResources(), R.drawable.apm_bootloader))
							.addAction(android.R.drawable.ic_menu_call, "Do nothing", pendingintent1)
							.addAction(android.R.drawable.ic_menu_delete, "...do it again", pendingintent1)
							.setStyle(new Notification.InboxStyle()
								.addLine("This is a test!")
								.addLine("Multiple lines")
								.setSummaryText("Summary example"))
							.build();
					
					final Notification notification2 = (new Notification.Builder(act))
							.setContentTitle("Big Picture Style")
							.setContentText("This is a test!")
							.setAutoCancel(true)
							.setContentIntent(pendingintent)
							.setSmallIcon(R.drawable.apm_hotreboot)
							.setLargeIcon(BitmapFactory.decodeResource(act.getResources(), R.drawable.apm_hotreboot))
							.addAction(android.R.drawable.ic_menu_call, "Do nothing", pendingintent1)
							.addAction(android.R.drawable.ic_menu_delete, "...do it again", pendingintent1)
							.setStyle(new Notification.BigPictureStyle()
								.bigPicture(BitmapFactory.decodeResource(act.getResources(), getResources().getIdentifier("htc_logo", "drawable", "com.htc")))
								.bigLargeIcon(BitmapFactory.decodeResource(act.getResources(), R.drawable.apm_hotreboot))
								.setSummaryText("Summary example"))
							.build();
					
					final Notification notification3 = (new Notification.Builder(act))
							.setContentTitle("Big Text Style notification")
							.setContentText("This is a test notification")
							.setAutoCancel(true)
							.setContentIntent(pendingintent)
							.setSmallIcon(R.drawable.apm_recovery)
							.setLargeIcon(BitmapFactory.decodeResource(act.getResources(), R.drawable.apm_recovery))
							.addAction(android.R.drawable.ic_menu_call, "Do nothing", pendingintent1)
							.addAction(android.R.drawable.ic_menu_delete, "...do it again", pendingintent1)
							.setStyle(new Notification.BigTextStyle().setSummaryText("Summary example").bigText("This is a test!"))
							.build();
					final NotificationManager nm = (NotificationManager)act.getSystemService("notification");
					nm.notify(6661, notification1);
					
					(new Handler()).postDelayed(new Runnable() {
						public void run() {
							nm.notify(6662, notification2);
						}
					}, 2000L);
					
					(new Handler()).postDelayed(new Runnable() {
						public void run() {
							nm.notify(6663, notification3);
						}
					}, 3000L);
				}
			});
			((ActivityEx)getActivity()).actionBarContainer.addRightView(menuTest);
			
			applyPopupState(prefs.getBoolean("popup_notify_active", false));
		} else if (menuType == 4) {
			OnOffSwitch = new HtcToggleButtonLight(getActivity());
			OnOffSwitch.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			OnOffSwitch.setEnabled(true);
			OnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(HtcToggleButtonLight toggle, boolean state) {
					prefs.edit().putBoolean("better_headsup_active", state).commit();
					applyPopupState(state);
				}
			});
			((ActivityEx)getActivity()).actionBarContainer.addRightView(OnOffSwitch);
			
			menuTest = new ActionBarItemView(getActivity());
			menuTest.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
			menuTest.setIcon(getResources().getIdentifier("icon_btn_view_dark", "drawable", "com.htc"));
			menuTest.setLongClickable(true);
			menuTest.setTitle(Helpers.l10n(getActivity(), R.string.betterheadsup_test));
			menuTest.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final Activity act = getActivity();
					final PendingIntent pendingintent = PendingIntent.getActivity(act, 0, new Intent(act, MainActivity.class), 0x10000000);
					final PendingIntent pendingintent1 = PendingIntent.getActivity(act, 0, new Intent(), 0x10000000);
					Notification notification1 = (new Notification.Builder(act))
							.setContentTitle("Inbox Style notification")
							.setContentText("This is a test notification")
							.setAutoCancel(true)
							.setContentIntent(pendingintent)
							.setSmallIcon(R.drawable.ic_for_settings)
							.setLargeIcon(BitmapFactory.decodeResource(act.getResources(), android.R.drawable.ic_menu_call))
							.addAction(android.R.drawable.ic_menu_save, "SMS", pendingintent1)
							.addAction(android.R.drawable.ic_menu_delete, "Call back", pendingintent1)
							.setStyle(new Notification.InboxStyle()
								.addLine("This is a test!")
								.addLine("Multiple lines")
								.setSummaryText("Summary example"))
							.build();
					final Notification notification2 = (new Notification.Builder(act))
							.setContentTitle("Photo")
							.setContentText("This is a test")
							.setAutoCancel(true)
							.setContentIntent(pendingintent)
							.setSmallIcon(R.drawable.ic_for_settings)
							.setLargeIcon(BitmapFactory.decodeResource(act.getResources(), android.R.drawable.ic_menu_camera))
							.addAction(android.R.drawable.ic_menu_view, "Agree!", pendingintent1)
							.addAction(android.R.drawable.ic_menu_report_image, "Totally!", pendingintent1)
							.setStyle(new Notification.BigPictureStyle()
								.bigPicture(BitmapFactory.decodeResource(act.getResources(), R.drawable.willa))
								.bigLargeIcon(BitmapFactory.decodeResource(act.getResources(), R.drawable.apm_hotreboot))
								.setSummaryText("of gorgeous Willa Holland :)"))
							.build();
					
					final Notification notification3 = (new Notification.Builder(act))
							.setContentTitle("Big Text Style notification")
							.setContentText("This is a test notification")
							.setAutoCancel(true)
							.setContentIntent(pendingintent)
							.setSmallIcon(R.drawable.ic_for_settings)
							.setLargeIcon(BitmapFactory.decodeResource(act.getResources(), android.R.drawable.ic_menu_help))
							.addAction(android.R.drawable.ic_menu_save, "OK", pendingintent1)
							.addAction(android.R.drawable.ic_menu_delete, "Not OK", pendingintent1)
							.setStyle(new Notification.BigTextStyle().setSummaryText("Summary example").bigText("This is a test!"))
							.build();
					final NotificationManager nm = (NotificationManager)act.getSystemService("notification");
					nm.notify(6661, notification1);
					
					(new Handler()).postDelayed(new Runnable() {
						public void run() {
							nm.notify(6662, notification2);
						}
					}, 2000L);
					
					(new Handler()).postDelayed(new Runnable() {
						public void run() {
							nm.notify(6663, notification3);
						}
					}, 3000L);
				}
			});
			((ActivityEx)getActivity()).actionBarContainer.addRightView(menuTest);
			
			applyPopupState(prefs.getBoolean("better_headsup_active", false));
		} else if (menuType == 5) {
			inflater.inflate(R.menu.menu_sub, menu);
			
			OnOffSwitch = (HtcToggleButtonLight)menu.getItem(1).getActionView().findViewById(R.id.onoffSwitch);
			OnOffSwitch.setEnabled(true);
			OnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(HtcToggleButtonLight toggle, boolean state) {
					prefs.edit().putBoolean("fleeting_glance_active", state).commit();
					applyWGState(state);
				}
			});
			
			applyWGState(prefs.getBoolean("fleeting_glance_active", false));
		}
	}
	
	// Wake gestures
	public void applyWGState(Boolean state) {
		OnOffSwitch.setChecked(state);
		if (state) {
			prefListView.setVisibility(View.VISIBLE);
			themeHint.setVisibility(View.GONE);
		} else {
			prefListView.setVisibility(View.GONE);
			themeHint.setVisibility(View.VISIBLE);
		}
	}
	
	// EPS Remap
	public void applyEPSState(boolean state) {
		OnOffSwitch.setChecked(state);
		if (state) {
			contentsView.setVisibility(View.VISIBLE);
			themeHint.setVisibility(View.GONE);
		} else {
			contentsView.setVisibility(View.GONE);
			themeHint.setVisibility(View.VISIBLE);
		}
	}
	
	// Popup Notify
	public void applyPopupState(Boolean state) {
		OnOffSwitch.setChecked(state);
		menuTest.setEnabled(state);
		if (state) {
			prefListView.setVisibility(View.VISIBLE);
			themeHint.setVisibility(View.GONE);
		} else {
			prefListView.setVisibility(View.GONE);
			themeHint.setVisibility(View.VISIBLE);
		}
	}
	
	int[][] cellArray = {
		{ 0, 0, 0 },
		{ R.id.cell1, R.id.cell1img, R.id.cell1txt },
		{ R.id.cell2, R.id.cell2img, R.id.cell2txt },
		{ R.id.cell3, R.id.cell3img, R.id.cell3txt },
		{ R.id.cell4, R.id.cell4img, R.id.cell4txt },
		{ R.id.cell5, R.id.cell5img, R.id.cell5txt },
		{ R.id.cell6, R.id.cell6img, R.id.cell6txt }
	};
		
	View.OnTouchListener otl = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (OnOffSwitch.isChecked())
			switch (event.getAction()) {
				case 0:
					v.setBackgroundColor(0xff888888);
					break;
				case 1:
					v.setBackgroundColor(0xff666666);
					editApp(v, (int)v.getTag());
					break;
			}
			v.performClick();
			return true;
		}
	};
		
	public void initCell(int cellnum) {
		String pkgActName = prefs.getString("eps_remap_cell" + String.valueOf(cellnum), null);
		updateCell(cellnum, pkgActName);
		
		int cellid = cellArray[cellnum][0];
		LinearLayout cell = (LinearLayout)getActivity().findViewById(cellid);
		cell.setTag(cellnum);
		cell.setOnTouchListener(otl);
		alignCell(cellnum);
	}
		
	public void updateCell(int cellnum, String pkgActName) {
		alignCell(cellnum);
		int cellimgid = cellArray[cellnum][1];
		int celltxtid = cellArray[cellnum][2];
		try {
			ImageView cellimg = (ImageView)getActivity().findViewById(cellimgid);
			TextView celltxt = (TextView)getActivity().findViewById(celltxtid);
			if (pkgActName != null) {
				final PackageManager pm = getActivity().getApplicationContext().getPackageManager();
				String[] pkgActArray = pkgActName.split("\\|");
				cellimg.setImageDrawable(pm.getActivityIcon(new ComponentName(pkgActArray[0], pkgActArray[1])));
				celltxt.setText(Helpers.getAppName(getActivity(), pkgActName));
			} else {
				cellimg.setImageResource(R.drawable.question_icon);
				celltxt.setText(Helpers.l10n(getActivity(), R.string.array_default));
			}
		} catch (Exception e) {}
	}
		
	public void alignCell(int cellnum) {
		LinearLayout cell = (LinearLayout)getActivity().findViewById(cellArray[cellnum][0]);
		ImageView cellimg = (ImageView)getActivity().findViewById(cellArray[cellnum][1]);
		float density = getResources().getDisplayMetrics().density;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)cellimg.getLayoutParams();
		
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			cell.setOrientation(LinearLayout.HORIZONTAL);
			lp.setMargins(0, 0, Math.round(20 * density), 0);
		} else {
			cell.setOrientation(LinearLayout.VERTICAL);
			lp.setMargins(0, 0, 0, Math.round(10 * density));
		}
		
		cellimg.setLayoutParams(lp);
	}
	
	private void editApp(View cell, final int id) {
		HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(getActivity());
		final String title = Helpers.l10n(getActivity(), R.string.various_extremepower_cell) + " " + String.valueOf(id);
		builder.setTitle(title);
		
		TypedArray ids = getResources().obtainTypedArray(R.array.EPSRemaps);
		List<String> newEntries = new ArrayList<String>();
		for (int i = 0; i < ids.length(); i++) {
			int itemid = ids.getResourceId(i, 0);
			if (itemid != 0)
				newEntries.add(Helpers.l10n(getActivity(), itemid));
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
						final DynamicPreference dp = new DynamicPreference(getActivity());
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
							final HtcProgressDialog dialogLoad = new HtcProgressDialog(getActivity());
							dialogLoad.setMessage(Helpers.l10n(getActivity(), R.string.loading_app_data));
							dialogLoad.setCancelable(false);
							dialogLoad.show();
							
							new Thread() {
								@Override
								public void run() {
									try {
										Helpers.getLaunchableApps(getActivity());
										getActivity().runOnUiThread(new Runnable(){
											@Override
											public void run(){
												dp.show();
											}
										});
										// Nasty hack! Wait for icons to load.
										Thread.sleep(1000);
										getActivity().runOnUiThread(new Runnable(){
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (menuType == 2) for (int i = 1; i <= 6; i++) alignCell(i);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return handleOptionsItemSelected(getActivity(), item);
	}
	
	public void onActivityCreated(Bundle savedInstanceState, int pref_defaults) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		getPreferenceManager().setSharedPreferencesName("one_toolbox_prefs");
		getPreferenceManager().setSharedPreferencesMode(1);
		HtcPreferenceManager.setDefaultValues(getActivity(), pref_defaults, false);
		prefs = getPreferenceManager().getSharedPreferences();
	}
	
	@Override
	public void addPreferencesFromResource(int resId) {
		super.addPreferencesFromResource(resId);
		Helpers.applyLang(this.getActivity(), this);
	}
}

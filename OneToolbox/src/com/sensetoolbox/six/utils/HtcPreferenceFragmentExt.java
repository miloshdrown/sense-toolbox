package com.sensetoolbox.six.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.htc.preference.HtcPreferenceActivity;
import com.htc.preference.HtcPreferenceFragment;
import com.htc.preference.HtcPreferenceManager;
import com.htc.widget.HtcAlertDialog;
import com.sensetoolbox.six.AboutScreen;
import com.sensetoolbox.six.R;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class HtcPreferenceFragmentExt extends HtcPreferenceFragment {
	private SharedPreferences prefs = null;
	public int rebootType = 0;
	
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
							CommandCapture command = new CommandCapture(0, "setprop ctl.restart zygote");
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
		inflater.inflate(R.menu.menu_mods, menu);
		menu.getItem(2).setIcon(Helpers.applySenseTheme(getActivity(), menu.getItem(2).getIcon()));
		if (rebootType == 1) {
			menu.getItem(0).setIcon(R.drawable.ic_menu_restart_prism);
			menu.getItem(0).setTitle(Helpers.l10n(getActivity(), R.string.restart_prism));
		} else if (rebootType == 2) {
			menu.getItem(0).setIcon(R.drawable.ic_menu_restart_message);
			menu.getItem(0).setTitle(Helpers.l10n(getActivity(), R.string.restart_messages));
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return handleOptionsItemSelected(getActivity(), item);
	}
	
	public void onCreate(Bundle savedInstanceState, int pref_defaults) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getPreferenceManager().setSharedPreferencesName("one_toolbox_prefs");
		getPreferenceManager().setSharedPreferencesMode(1);
		HtcPreferenceManager.setDefaultValues(getActivity(), pref_defaults, false);
		prefs = getPreferenceManager().getSharedPreferences();
	}
	
	@Override
	public void addPreferencesFromResource(int resId) {
		super.addPreferencesFromResource(resId);
		Helpers.applyLang((HtcPreferenceActivity)this.getActivity(), this);
	}
}

package com.langerhans.one;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.htc.preference.HtcPreferenceActivity;
import com.htc.preference.HtcPreferenceManager;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarItemView;
import com.htc.widget.ActionBarText;
import com.htc.widget.HtcAlertDialog;
import com.langerhans.one.utils.Helpers;
import com.langerhans.one.utils.Version;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class MainActivity extends HtcPreferenceActivity {

	public static boolean isRootAccessGiven = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBarExt actionBarExt = new ActionBarExt(this, getActionBar());
		ActionBarContainer actionBarContainer = actionBarExt.getCustomContainer();
		ActionBarText actionBarText = new ActionBarText(this);    		        
		actionBarText.setPrimaryText(R.string.app_name);
		actionBarContainer.addCenterView(actionBarText);

		actionBarContainer.setBackUpEnabled(false);
		
		actionBarContainer.setBackgroundColor(Color.BLACK);
		
		ActionBarItemView menuAbout = new ActionBarItemView(this);
		menuAbout.setIcon(R.drawable.ic_menu_about);
		menuAbout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, AboutScreen.class);
				startActivity(intent);
			}
		});
		
		ActionBarItemView menuReboot = new ActionBarItemView(this);
		menuReboot.setIcon(R.drawable.ic_menu_reboot);
		menuReboot.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(MainActivity.this);
				alert.setTitle(R.string.soft_reboot);
				alert.setView(Helpers.createCenteredText(MainActivity.this, R.string.hotreboot_explain_prefs));
				alert.setPositiveButton(getText(R.string.yes) + "!", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						try {
							CommandCapture command = new CommandCapture(0, "setprop ctl.restart zygote");
							RootTools.getShell(true).add(command).waitForFinish();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				alert.setNegativeButton(getText(R.string.no) + "!", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						//Canceled
					}
				});
				alert.show();
			}
		});
		
		ActionBarItemView menuBackup = new ActionBarItemView(this);
		menuBackup.setIcon(R.drawable.ic_menu_backup);
		menuBackup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final String backupPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SenseToolbox/";
				final String backupFile = "settings_backup";
				
				getPreferenceManager().setSharedPreferencesName("one_toolbox_prefs");
				getPreferenceManager().setSharedPreferencesMode(1);
				HtcPreferenceManager.setDefaultValues(MainActivity.this, R.xml.preferences, false);
				final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
				
				HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(MainActivity.this);
				alert.setTitle(R.string.backup_restore);
				alert.setView(Helpers.createCenteredText(MainActivity.this, R.string.backup_restore_choose));
				alert.setPositiveButton(getText(R.string.do_restore), new DialogInterface.OnClickListener() {
					@SuppressWarnings("unchecked")
					public void onClick(DialogInterface dialog, int whichButton) {
						if (!Helpers.checkStorageReadable(MainActivity.this)) return;
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
							
							HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(MainActivity.this);
							alert.setTitle(R.string.do_restore);
							alert.setView(Helpers.createCenteredText(MainActivity.this, R.string.restore_ok));
							alert.setCancelable(false);
							alert.setNeutralButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									MainActivity.this.finish();
									startActivity(MainActivity.this.getIntent());
								}
							});
							alert.show();
						} catch (Exception e) {
							HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(MainActivity.this);
							alert.setTitle(R.string.warning);
							alert.setView(Helpers.createCenteredText(MainActivity.this, R.string.storage_cannot_restore));
							alert.setNeutralButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
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
				alert.setNegativeButton(getText(R.string.do_backup), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (!preparePathForBackup(backupPath)) return;
						ObjectOutputStream output = null;
						try {
							output = new ObjectOutputStream(new FileOutputStream(backupPath + backupFile));
							output.writeObject(prefs.getAll());
							
							HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(MainActivity.this);
							alert.setTitle(R.string.do_backup);
							alert.setView(Helpers.createCenteredText(MainActivity.this, R.string.backup_ok));
							alert.setNeutralButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {}
							});
							alert.show();
						} catch (Exception e) {
							HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(MainActivity.this);
							alert.setTitle(R.string.warning);
							alert.setView(Helpers.createCenteredText(MainActivity.this, R.string.storage_cannot_backup));
							alert.setNeutralButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
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
				alert.setNeutralButton(getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
				});
				alert.show();
			}
		});
		
		actionBarContainer.addRightView(menuAbout);
		actionBarContainer.addRightView(menuReboot);
		actionBarContainer.addRightView(menuBackup);
		
		if ((new Version(Helpers.getSenseVersion())).compareTo(new Version("5.5")) < 0)
		if (RootTools.isAccessGiven()) {
			isRootAccessGiven = true;
		} else {
			final SharedPreferences prefs = getSharedPreferences("one_toolbox_prefs", 1);
			if(prefs.getBoolean("show_root_note", true))
			{
				HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(this);
				builder.setTitle(R.string.no_root_access);
				builder.setMessage(R.string.no_root_explain);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setPositiveButton(R.string.dismiss_once, null);
				builder.setNegativeButton(R.string.dismiss_forever, new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						prefs.edit().putBoolean("show_root_note", false).commit();
					}
				});
				HtcAlertDialog dlg = builder.create();
				dlg.show();
			}
		}
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }
	
	private boolean preparePathForBackup(String path) {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(this);
			alert.setTitle(R.string.warning);
			alert.setView(Helpers.createCenteredText(this, R.string.storage_read_only));
			alert.setNeutralButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return false;
		} else if (state.equals(Environment.MEDIA_MOUNTED)) {
			File file = new File(path);
			if (!file.exists() && !file.mkdirs()) {
	        	HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(this);
				alert.setTitle(R.string.warning);
				alert.setView(Helpers.createCenteredText(this, R.string.storage_cannot_mkdir));
				alert.setNeutralButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
				});
				alert.show();
				return false;
		    }
			return true;
		} else {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(this);
			alert.setTitle(R.string.warning);
			alert.setView(Helpers.createCenteredText(this, R.string.storage_unavailable));
			alert.setNeutralButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return false;
		}
	}
}

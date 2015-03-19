package com.sensetoolbox.six;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htc.app.HtcProgressDialog;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarItemView;
import com.htc.widget.ActionBarText;
import com.htc.widget.HtcListItem;
import com.htc.widget.HtcListItem2LineText;
import com.htc.widget.HtcListView;
import com.htc.widget.HtcToggleButtonLight;
import com.htc.widget.HtcToggleButtonLight.OnCheckedChangeListener;
import com.sensetoolbox.six.utils.AppAddDialog;
import com.sensetoolbox.six.utils.AppDetailDialog;
import com.sensetoolbox.six.utils.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class SenseThemes extends Activity {
	public static List<PackageTheme> pkgthm = new ArrayList<PackageTheme>();
	public static SharedPreferences prefs;
	public AppAddDialog appAddDialog;
	HtcToggleButtonLight OnOffSwitch;
	ActionBarItemView menuAdd;
	ActionBarItemView menuAll;
	HtcListView appsList;
	TextView themeHint;
	int mThemeId = 0;
	ObjectMapper mapper = new ObjectMapper();
	
	public static PackageTheme arrayHasPkg(String pkgName) {
		if (pkgName == null) return null;
		PackageTheme ptOut = null;
		for (PackageTheme pt: pkgthm) if (pt.getPkg() != null && pt.getPkg().equals(pkgName)) {
			ptOut = pt;
			break;
		}
		return ptOut;
	}
	
	public static class PackageTheme {
		private String pkg;
		private int theme;
		
		public PackageTheme() {
			super();
		}
		
		public PackageTheme(String packg, int thm) {
			super();
			this.pkg = packg;
			this.theme = thm;
		}
		
		public void setPkg(String packg) {
			this.pkg = packg;
		}
		
		public void setTheme(int thm) {
			this.theme = thm;
		}
		
		public String getPkg() {
			return this.pkg;
		}
		
		public int getTheme() {
			return (this.theme >= 0 ? this.theme : 0);
		}
	}
	
	public static class PackageLabel {
		public String pkg;
		public String label;
		
		public PackageLabel() {
			super();
		}
		
		public PackageLabel(String packg, String lbl) {
			super();
			this.pkg = packg;
			this.label = lbl;
		}
	}
	
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
		actionBarText.setPrimaryText(Helpers.l10n(this, R.string.sense_themes_title));
		actionBarContainer.addCenterView(actionBarText);
		actionBarContainer.setBackUpEnabled(true);
		actionBarContainer.setBackUpOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		OnOffSwitch = new HtcToggleButtonLight(this);
		OnOffSwitch.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		OnOffSwitch.setEnabled(true);
		actionBarContainer.addRightView(OnOffSwitch);
		
		menuAll = new ActionBarItemView(this);
		menuAll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		menuAll.setIcon(getResources().getIdentifier("icon_btn_edit_dark", "drawable", "com.htc"));
		menuAll.setTitle(Helpers.l10n(this, R.string.sense_theme_replace_all_title));
		menuAll.setLongClickable(true);
		menuAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AppDetailDialog appDetailDialog = new AppDetailDialog(SenseThemes.this, "replace_all");
				appDetailDialog.setTitle(Helpers.l10n(SenseThemes.this, R.string.sense_theme_replace_all));
				appDetailDialog.show();
			}
		});
		actionBarContainer.addRightView(menuAll);
		
		menuAdd = new ActionBarItemView(this);
		menuAdd.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		menuAdd.setIcon(getResources().getIdentifier("icon_btn_add_dark", "drawable", "com.htc"));
		menuAdd.setTitle(Helpers.l10n(this, R.string.select_app));
		menuAdd.setLongClickable(true);
		menuAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				appAddDialog = new AppAddDialog(SenseThemes.this);
				appAddDialog.setTitle(Helpers.l10n(SenseThemes.this, R.string.select_app));
				
				if (Helpers.installedAppsList == null) {
					final HtcProgressDialog dialog = new HtcProgressDialog(SenseThemes.this);
					dialog.setMessage(Helpers.l10n(SenseThemes.this, R.string.loading_app_data));
					dialog.setCancelable(false);
					dialog.show();
					appAddDialog.setOnShowListener(new OnShowListener() {
						@Override
						public void onShow(DialogInterface dlg) {
							if (SenseThemes.this != null && !SenseThemes.this.isFinishing() && dialog != null && dialog.isShowing()) try { dialog.dismiss(); } catch (Throwable t) {}
						}
					});
					
					new Thread() {
						@Override
						public void run() {
							try {
								Helpers.getInstalledApps(SenseThemes.this);
								runOnUiThread(new Runnable(){
									@Override
									public void run() {
										appAddDialog.show();
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}.start();
				} else appAddDialog.show();
			}
		});
		actionBarContainer.addRightView(menuAdd);
		
		prefs = getSharedPreferences("one_toolbox_prefs", 1);
		loadPkgs();
		
		setContentView(R.layout.activity_sense_themes);
		int backResId = getResources().getIdentifier("common_app_bkg", "drawable", "com.htc");
		
		appsList = (HtcListView)findViewById(R.id.appslist);
		appsList.setBackgroundResource(backResId);
		appsList.setDivider(getResources().getDrawable(getResources().getIdentifier("inset_list_divider", "drawable", "com.htc")));
		appsList.setDividerHeight(1);
		appsList.setFooterDividersEnabled(false);
		AppsAdapter appsAdapter = new AppsAdapter(this);
		appsList.setAdapter(appsAdapter);
		appsList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AppDetailDialog appDetailDialog = new AppDetailDialog(SenseThemes.this, (String)view.getTag());
				HtcListItem2LineText title = (HtcListItem2LineText)view.findViewById(R.id.app_name);
				appDetailDialog.setTitle(title.getPrimaryText());
				appDetailDialog.show();
			}
		});
		
		themeHint = (TextView)findViewById(R.id.themehint);
		themeHint.setBackgroundResource(backResId);
		themeHint.setText(Helpers.l10n(this, R.string.sense_theme_hint));
		applyThemeState(prefs.getBoolean("themes_active", false));
		OnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(HtcToggleButtonLight toggle, boolean state) {
				prefs.edit().putBoolean("themes_active", state).commit();
				applyThemeState(state);
			}
		});
	}
	
	private void applyThemeState(Boolean state) {
		OnOffSwitch.setChecked(state);
		appsList.setEnabled(state);
		menuAdd.setEnabled(state);
		menuAll.setEnabled(state);
		if (state) {
			appsList.setVisibility(View.VISIBLE);
			themeHint.setVisibility(View.GONE);
		} else {
			appsList.setVisibility(View.GONE);
			themeHint.setVisibility(View.VISIBLE);
		}
	}
	
	protected void onResume() {
		super.onResume();
		int newThemeId = Helpers.getCurrentTheme(this);
		if (newThemeId != mThemeId) recreate();
	}
	
	private class AppsAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		Context mContext = null;
		
		public AppsAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(context);
		}
		
		public int getCount() {
			return pkgthm.size();
		}
		
		public String getItem(int position) {
			return pkgthm.get(position).getPkg();
		}
		
		public long getItemId(int position) {
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			HtcListItem listItem;
			if (convertView != null)
				listItem = (HtcListItem)convertView;
			else
				listItem = (HtcListItem)mInflater.inflate(R.layout.htc_list_item, parent, false);
			
			HtcListItem2LineText title = (HtcListItem2LineText)listItem.findViewById(R.id.app_name);
			String pkgName = getItem(position);
			
			PackageManager pm = mContext.getPackageManager();
			ApplicationInfo ai;
			try {
				ai = pm.getApplicationInfo(pkgName, 0);
			} catch (Exception e) {
				ai = null;
			}
			
			String pkgTitle = (String) (ai != null ? pm.getApplicationLabel(ai) : pkgName + " [Uninstalled]");
			title.setPrimaryText(pkgTitle);
			title.setSecondaryTextVisibility(View.GONE);
			listItem.setTag(pkgName);
			
			return listItem;
		}
	}
	
	public static Map<String, String> labels = new HashMap<String, String>();
	public class AscComparator implements Comparator<PackageTheme> {
		@Override
		public int compare(PackageTheme first, PackageTheme second) {
			return labels.get(first.pkg).compareToIgnoreCase(labels.get(second.pkg));
		}
	}
	
	public void sortListArray() {
		PackageManager pm = getPackageManager();
		ApplicationInfo ai = null;
		for (PackageTheme pkgth: pkgthm)
		if (!labels.containsKey(pkgth.pkg)) {
			try {
				ai = pm.getApplicationInfo(pkgth.pkg, 0);
			} catch (Exception e) {
				ai = null;
			}
			String pkgTitle = (String)(ai != null ? pm.getApplicationLabel(ai) : pkgth.pkg + " [Uninstalled]");
			labels.put(pkgth.pkg, pkgTitle);
		}
		Collections.sort(pkgthm, new AscComparator());
	}
	
	public void savePkgs() {
		try {
			prefs.edit().putString("pkgthm", mapper.writeValueAsString(pkgthm)).commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadPkgs() {
		String json = prefs.getString("pkgthm", null);
		if (json == null)
			pkgthm = new ArrayList<PackageTheme>();
		else try {
			pkgthm = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, PackageTheme.class));
		} catch (Exception e) {
			pkgthm = new ArrayList<PackageTheme>();
			e.printStackTrace();
		}
		sortListArray();
	}
	
	public void updateListArray() {
		loadPkgs();
		ListView appsListView = (ListView)findViewById(R.id.appslist);
		AppsAdapter aa = (AppsAdapter)appsListView.getAdapter();
		aa.notifyDataSetChanged();
	}
	
	public void notifyThemeChanged(String forPkg) {
		Intent intent = new Intent("com.htc.intent.action.CONFIGURATION_CHANGED");
		intent.addCategory("com.htc.intent.category.THEMEID");
		sendBroadcast(intent);
		int newThemeId = Helpers.getCurrentTheme(this);
		if (newThemeId != mThemeId) recreate();
		// Restart launcher to refresh themes for widgets
		if (forPkg.equals("replace_all") || (forPkg.startsWith("com.htc.") && forPkg.toLowerCase(Locale.getDefault()).contains("widget")))
		sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.RestartPrism"));
	}
}

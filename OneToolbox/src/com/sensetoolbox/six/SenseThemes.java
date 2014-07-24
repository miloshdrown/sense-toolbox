package com.sensetoolbox.six;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.htc.app.HtcProgressDialog;
import com.htc.gson.Gson;
import com.htc.gson.reflect.TypeToken;
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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class SenseThemes extends Activity {
	public static SparseArray<Object[]> colors = null;
	public static List<PackageTheme> pkgthm = new ArrayList<PackageTheme>();
	public static SharedPreferences prefs;
	public AppAddDialog appAddDialog;
	HtcToggleButtonLight OnOffSwitch;
	ActionBarItemView menuAdd;
	ActionBarItemView menuAll;
	HtcListView appsList;
	TextView themeHint;
	int mThemeId = 0;
	
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
		
		OnOffSwitch = new HtcToggleButtonLight(this);
		OnOffSwitch.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		OnOffSwitch.setEnabled(true);
		actionBarContainer.addRightView(OnOffSwitch);
		
		final SenseThemes st = this;
		
		menuAll = new ActionBarItemView(this);
		menuAll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		menuAll.setIcon(getResources().getIdentifier("icon_btn_edit_dark", "drawable", "com.htc"));
		menuAll.setLongClickable(false);
		menuAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AppDetailDialog appDetailDialog = new AppDetailDialog(st, "replace_all");
				appDetailDialog.setTitle(Helpers.l10n(st, R.string.sense_theme_replace_all));
				appDetailDialog.show();
			}
		});
		actionBarContainer.addRightView(menuAll);
		
		menuAdd = new ActionBarItemView(this);
		menuAdd.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		menuAdd.setIcon(getResources().getIdentifier("icon_btn_add_dark", "drawable", "com.htc"));
		menuAdd.setLongClickable(false);
		menuAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				appAddDialog = new AppAddDialog(st);
				appAddDialog.setTitle(Helpers.l10n(st, R.string.select_app));
				
				if (Helpers.installedAppsList == null) {
					final HtcProgressDialog dialog = new HtcProgressDialog(st);
					dialog.setMessage(Helpers.l10n(st, R.string.loading_app_data));
					dialog.setCancelable(false);
					dialog.show();
					appAddDialog.setOnShowListener(new OnShowListener() {
						@Override
						public void onShow(DialogInterface dlg) {
							dialog.dismiss();
						}
					});
					
					new Thread() {
						@Override
						public void run() {
							try {
								Helpers.getInstalledApps(st);
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
		
		prefs = getSharedPreferences("one_toolbox_prefs", 1);
		loadPkgs();
		
		setContentView(R.layout.activity_sense_themes);
		int backResId = getResources().getIdentifier("common_app_bkg", "drawable", "com.htc");
		
		appsList = (HtcListView)findViewById(R.id.appslist);
		appsList.setBackgroundResource(backResId);
		appsList.setDivider(getResources().getDrawable(getResources().getIdentifier("inset_list_divider", "drawable", "com.htc")));
		appsList.setDividerHeight(1);
		appsList.setFooterDividersEnabled(false);
		AppsAdapter appsAdapter = new AppsAdapter(this, pkgthm);
		appsList.setAdapter(appsAdapter);
		appsList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AppDetailDialog appDetailDialog = new AppDetailDialog(st, (String)view.getTag());
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
		List<PackageTheme> pkgthm;
		private LayoutInflater mInflater;
		Context mContext = null;
		
		public AppsAdapter(Context context, List<PackageTheme> objects) {
			mContext = context;
			mInflater = LayoutInflater.from(context);
			assignLocal(objects);
		}
		
		public void updateWith(List<PackageTheme> objects) {
			assignLocal(objects);
			notifyDataSetChanged();
		}
		
		public void assignLocal(List<PackageTheme> objects) {
			pkgthm = new ArrayList<PackageTheme>(objects);
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
	
	public class AscComparator implements Comparator<PackageTheme> {
		@Override
		public int compare(PackageTheme first, PackageTheme second) {
			PackageManager pm = getPackageManager();
			ApplicationInfo ai1; ApplicationInfo ai2;
			try {
				ai1 = pm.getApplicationInfo(first.getPkg(), 0);
				ai2 = pm.getApplicationInfo(second.getPkg(), 0);
			} catch (Exception e) {
				ai1 = null;
				ai2 = null;
			}
			String pkgTitle1 = (String) (ai1 != null ? pm.getApplicationLabel(ai1) : first + " [Uninstalled]");
			String pkgTitle2 = (String) (ai2 != null ? pm.getApplicationLabel(ai2) : second + " [Uninstalled]");
			return pkgTitle1.compareToIgnoreCase(pkgTitle2);
		}
	}
	
	public void sortListArray() {
		Collections.sort(pkgthm, new AscComparator());
	}
	
	public void savePkgs() {
		String json = new Gson().toJson(pkgthm);
		prefs.edit().putString("pkgthm", json).commit();
	}
	
	public void loadPkgs() {
		String tmp = prefs.getString("pkgthm", null);
		if (tmp != null)
			pkgthm = new Gson().fromJson(tmp, new TypeToken<ArrayList<PackageTheme>>(){}.getType());
		else
			pkgthm = new ArrayList<PackageTheme>();
		sortListArray();
	}
	
	public void updateListArray() {
		loadPkgs();
		ListView appsList = (ListView)findViewById(R.id.appslist);
		AppsAdapter aa = (AppsAdapter)appsList.getAdapter();
		aa.updateWith(pkgthm);
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
	
	public synchronized static SparseArray<Object[]> getColors() {
		if (colors == null) {
			colors = new SparseArray<Object[]>();
			
			// Theme 1
			//colors.append(Helpers.allStyles.get(0), new Object[]{ 0xff252525, 0xff4ea770, "HtcDeviceDefault", 0xff141414 });
			colors.append(Helpers.allStyles.get(1), new Object[]{ 0xff0086cb, 0xff0086cb, "HtcDeviceDefault.CategoryOne", 0xff4b4b4b });
			colors.append(Helpers.allStyles.get(2), new Object[]{ 0xff4ea770, 0xff4ea770, "HtcDeviceDefault.CategoryTwo", 0xff4b4b4b });
			colors.append(Helpers.allStyles.get(3), new Object[]{ 0xffff5d3d, 0xffff5d3d, "HtcDeviceDefault.CategoryThree", 0xff787878 });
			colors.append(Helpers.allStyles.get(4), new Object[]{ 0xff252525, 0xff4ea770, "HtcDeviceDefault.CategoryFour", 0xff4ea770 });
			
			// Theme 2
			//colors.append(Helpers.allStyles.get(5), new Object[]{ 0xff252525, 0xffff813d, "ThemeOne", 0xff141414 });
			colors.append(Helpers.allStyles.get(6), new Object[]{ 0xffffa63d, 0xffffa63d, "ThemeOne.CategoryOne", 0xff4b4b4b });
			colors.append(Helpers.allStyles.get(7), new Object[]{ 0xffe74457, 0xffe74457, "ThemeOne.CategoryTwo", 0xff4b4b4b });
			colors.append(Helpers.allStyles.get(8), new Object[]{ 0xfff64541, 0xfff64541, "ThemeOne.CategoryThree", 0xff787878 });
			colors.append(Helpers.allStyles.get(9), new Object[]{ 0xff252525, 0xffff813d, "ThemeOne.CategoryFour", 0xffff813d });
			
			// Theme 3
			//colors.append(Helpers.allStyles.get(10), new Object[]{ 0xff252525, 0xff6658cf, "ThemeTwo", 0xff141414 });
			colors.append(Helpers.allStyles.get(11), new Object[]{ 0xff0761B9, 0xff0761b9, "ThemeTwo.CategoryOne", 0xff4b4b4b });
			colors.append(Helpers.allStyles.get(12), new Object[]{ 0xff07B7B9, 0xff07b7b9, "ThemeTwo.CategoryTwo", 0xff4b4b4b });
			colors.append(Helpers.allStyles.get(13), new Object[]{ 0xffA325A3, 0xffa325a3, "ThemeTwo.CategoryThree", 0xff787878 });
			colors.append(Helpers.allStyles.get(14), new Object[]{ 0xff252525, 0xff6658cf, "ThemeTwo.CategoryFour", 0xff6658cf });
			
			// Theme 4
			//colors.append(Helpers.allStyles.get(15), new Object[]{ 0xff252525, 0xff4ea770, "ThemeThree", 0xff141414 });
			//colors.append(Helpers.allStyles.get(16), new Object[]{ 0xff252525, 0xff4ea770, "ThemeThree.CategoryOne", 0xff4b4b4b });
			//colors.append(Helpers.allStyles.get(17), new Object[]{ 0xff252525, 0xff4ea770, "ThemeThree.CategoryTwo", 0xff4b4b4b });
			//colors.append(Helpers.allStyles.get(18), new Object[]{ 0xff252525, 0xff4ea770, "ThemeThree.CategoryThree", 0xff787878 });
			//colors.append(Helpers.allStyles.get(19), new Object[]{ 0xff252525, 0xff4ea770, "ThemeThree.CategoryFour", 0xff252525 });
		}
		return colors;
	}
}

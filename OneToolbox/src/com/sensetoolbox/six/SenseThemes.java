package com.sensetoolbox.six;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.htc.app.HtcProgressDialog;
import com.htc.gson.Gson;
import com.htc.gson.reflect.TypeToken;
import com.htc.widget.ActionBarContainer;
import com.htc.widget.ActionBarExt;
import com.htc.widget.ActionBarItemView;
import com.htc.widget.ActionBarText;
import com.htc.widget.HtcAlertDialog;
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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
	static public List<ApplicationInfo> pkgAppsList = null;
	static public List<Drawable> pkgAppsListIcons = new ArrayList<Drawable>();
	static public SparseArray<int[]> colors = null;
	public static List<PackageTheme> pkgthm = new ArrayList<PackageTheme>();
	public static SharedPreferences prefs;
	public HtcAlertDialog appAddDialog;
	HtcToggleButtonLight OnOffSwitch;
	ActionBarItemView menuAdd;
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
			return this.theme;
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
		actionBarText.setPrimaryText(R.string.sense_themes_title);
		actionBarContainer.addCenterView(actionBarText);
		actionBarContainer.setBackUpEnabled(true);
		
		OnOffSwitch = new HtcToggleButtonLight(this);
		OnOffSwitch.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		OnOffSwitch.setEnabled(true);
		actionBarContainer.addRightView(OnOffSwitch);
		
		menuAdd = new ActionBarItemView(this);
		menuAdd.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		menuAdd.setIcon(getResources().getIdentifier("icon_btn_add_dark", "drawable", "com.htc"));
		menuAdd.setLongClickable(false);
		final SenseThemes st = this;
		menuAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				appAddDialog = new AppAddDialog(st);
				appAddDialog.setTitle(R.string.select_app);
				
				if (pkgAppsList == null) {
					final HtcProgressDialog dialog = new HtcProgressDialog(st);
					dialog.setMessage(getString(R.string.loading_app_data));
					dialog.setCancelable(false);
					dialog.show();
					
					new Thread() {
						@Override
						public void run() {
							try {
								getApps(st);
								runOnUiThread(new Runnable(){
									@Override
									public void run(){
										dialog.dismiss();
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
		final SenseThemes sense = this;
		
		appsList = (HtcListView)findViewById(R.id.appslist);
		appsList.setDivider(getResources().getDrawable(getResources().getIdentifier("inset_list_divider", "drawable", "com.htc")));
		appsList.setDividerHeight(2);
		appsList.setFooterDividersEnabled(false);
		AppsAdapter appsAdapter = new AppsAdapter(this, pkgthm);
		appsList.setAdapter(appsAdapter);
		appsList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AppDetailDialog appDetailDialog = new AppDetailDialog(sense, (String)view.getTag());
				HtcListItem2LineText title = (HtcListItem2LineText)view.findViewById(R.id.app_name);
				appDetailDialog.setTitle(title.getPrimaryText());
				appDetailDialog.show();
			}
		});
		
		themeHint = (TextView)findViewById(R.id.themehint);
		
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
	
	public static void getApps(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		pkgAppsList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		Collections.sort(pkgAppsList, new ApplicationInfo.DisplayNameComparator(pm));
		ApplicationInfo toolbox = null;
		for (ApplicationInfo inf: pkgAppsList) {
			if (inf.packageName.equals("com.sensetoolbox.six"))
				toolbox = inf;
			else
				pkgAppsListIcons.add(inf.loadIcon(pm));
		}
		if (toolbox != null) pkgAppsList.remove(toolbox);
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
	
	public void notifyThemeChanged() {
		Intent intent = new Intent("com.htc.intent.action.CONFIGURATION_CHANGED");
        intent.addCategory("com.htc.intent.category.THEMEID");
        sendBroadcast(intent);
        android.provider.Settings.Global.putString(this.getContentResolver(), "restart_launcher_on_resume", "true");
        int newThemeId = Helpers.getCurrentTheme(this);
		if (newThemeId != mThemeId) recreate();
	}

	public static SparseArray<int[]> getColors() {
		if (colors == null) {
			colors = new SparseArray<int[]>();

			// Theme 1
			//colors.append(0x02030069, new int[]{ 0xff252525, 0xff4ea770 });
			colors.append(0x0203012d, new int[]{ 0xff0086cb, 0xff0086cb });
			colors.append(0x0203012e, new int[]{ 0xff4ea770, 0xff4ea770 });
			colors.append(0x0203012f, new int[]{ 0xffff5d3d, 0xffff5d3d });
			colors.append(0x02030130, new int[]{ 0xff252525, 0xff4ea770 });
			
			// Theme 2
			//colors.append(0x020301c3, new int[]{ 0xff252525, 0xffff813d });
			colors.append(0x020301c7, new int[]{ 0xffffa63d, 0xffffa63d });
			colors.append(0x020301cb, new int[]{ 0xffe74457, 0xffe74457 });
			colors.append(0x020301cf, new int[]{ 0xfff64541, 0xfff64541 });
			colors.append(0x020301d3, new int[]{ 0xff252525, 0xffff813d });
			
			// Theme 3
			//colors.append(0x020301d7, new int[]{ 0xff252525, 0xff6658cf });
			colors.append(0x020301db, new int[]{ 0xff0761B9, 0xff0761b9 });
			colors.append(0x020301df, new int[]{ 0xff07B7B9, 0xff07b7b9 });
			colors.append(0x020301e3, new int[]{ 0xffA325A3, 0xffa325a3 });
			colors.append(0x020301e7, new int[]{ 0xff252525, 0xff6658cf });
			
			// Theme 4
			colors.append(0x020301eb, new int[]{ 0xff252525, 0xff4ea770 });
			//colors.append(0x020301ef, new int[]{ 0xff252525, 0xff4ea770 });
			//colors.append(0x020301f3, new int[]{ 0xff252525, 0xff4ea770 });
			//colors.append(0x020301f7, new int[]{ 0xff252525, 0xff4ea770 });
			//colors.append(0x020301fb, new int[]{ 0xff252525, 0xff4ea770 });
		}
		return colors;
	}
}

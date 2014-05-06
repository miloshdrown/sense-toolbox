package com.sensetoolbox.six.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcListItem2LineText;
import com.htc.widget.HtcListItemColorIcon;
import com.htc.widget.HtcListView;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.SenseThemes;
import com.sensetoolbox.six.SenseThemes.PackageTheme;

public class AppAddDialog extends HtcAlertDialog {
	SenseThemes stContext = null;
	
	public AppAddDialog(SenseThemes st) {
		super(st);
		stContext = st;
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		final HtcListView listView = new HtcListView(this.getContext());
        listView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        listView.setId(android.R.id.list);
        listView.setAdapter(new ImageArrayAdapter(this.getContext()));
        listView.setDivider(stContext.getResources().getDrawable(stContext.getResources().getIdentifier("inset_list_divider", "drawable", "com.htc")));
        listView.setDividerHeight(2);
        listView.setFooterDividersEnabled(false);
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (view.isEnabled()) {
					String pkgName = SenseThemes.pkgAppsList.get(position).packageName;
					PackageTheme pt = SenseThemes.arrayHasPkg(pkgName);
	        		if (pt == null) {
	        			SenseThemes.pkgthm.add(new PackageTheme(pkgName, 0));
	        			stContext.savePkgs();
	        			stContext.updateListArray();
	        			stContext.notifyThemeChanged();
	        			view.setEnabled(false);
	        			((ImageArrayAdapter)listView.getAdapter()).notifyDataSetChanged();
	        		} else {
	        			Toast.makeText(parent.getContext(), Helpers.l10n(parent.getContext(), R.string.sense_theme_package_has_profile), Toast.LENGTH_SHORT).show();
	        		}
				}
			}
		});
        this.setButton(DialogInterface.BUTTON_NEUTRAL, Helpers.l10n(this.getContext(), R.string.array_recents_menu_close), new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {}
        });
        this.setCancelable(true);
        this.setInverseBackgroundForced(true);
        this.setView(listView);
        super.onCreate(savedInstanceState);
	}
	
	private class ImageArrayAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public ImageArrayAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}
		
		public int getCount() {
			return SenseThemes.pkgAppsList.size();
		}
		 
		public CharSequence getItem(int position) {
			return SenseThemes.pkgAppsList.get(position).loadLabel(stContext.getPackageManager());
		}
		 
		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView != null)
				row = convertView;
			else
				row = mInflater.inflate(R.layout.htc_list_item_with_image, parent, false);			
			
			HtcListItem2LineText itemTitle = (HtcListItem2LineText)row.findViewById(R.id.list_item);
			itemTitle.setPrimaryText(getItem(position));
			itemTitle.setSecondaryTextVisibility(View.GONE);
			
			HtcListItemColorIcon itemIcon = (HtcListItemColorIcon)row.findViewById(R.id.list_item_img);
			itemIcon.setColorIconImageDrawable(SenseThemes.pkgAppsListIcons.get(position));
			float density = stContext.getResources().getDisplayMetrics().density;
			itemIcon.setPadding(Math.round(density * 10), Math.round(density * 6), Math.round(density * 15), Math.round(density * 3));
			
			if (SenseThemes.arrayHasPkg(SenseThemes.pkgAppsList.get(position).packageName) == null)
				row.setEnabled(true);
			else
				row.setEnabled(false);
			
			return row;
		}
	}
}
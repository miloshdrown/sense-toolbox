package com.sensetoolbox.six.utils;

import java.util.ArrayList;
import java.util.List;

import com.htc.preference.HtcListPreference;
import com.sensetoolbox.six.PrefsFragment;
import com.sensetoolbox.six.R;

import com.htc.widget.HtcAlertDialog.Builder;
import com.htc.widget.HtcListItem2LineText;
import com.htc.widget.HtcListItemRadioButton;
import com.htc.widget.HtcListItemTileImage;
import com.htc.widget.HtcListView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

public class DynamicPreference extends HtcListPreference {

	public DynamicPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DynamicPreference(Context context) {
		super(context);
	}

	public void show() {
		showDialog(null);
	}

	@Override
	public void onBindDialogView(View view) {
		List<CharSequence> entries = new ArrayList<CharSequence>();
		List<CharSequence> entryValues = new ArrayList<CharSequence>();
		for (int i = 0; i < PrefsFragment.pkgAppsList.size(); i++) {
			entries.add(PrefsFragment.pkgAppsList.get(i).loadLabel(getContext().getPackageManager()));
			entryValues.add(PrefsFragment.pkgAppsList.get(i).activityInfo.applicationInfo.packageName + "|" + PrefsFragment.pkgAppsList.get(i).activityInfo.name);
		}
		setEntries(entries.toArray(new CharSequence[entries.size()]));
		setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
	}

	@Override
	protected View onCreateDialogView() {
		HtcListView view = new HtcListView(getContext());
		setEntries(new CharSequence[0]);
		setEntryValues(new CharSequence[0]);
		return view;
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		int index = findIndexOfValue(getSharedPreferences().getString(getKey(), "1"));

		ListAdapter listAdapter = new ImageArrayAdapter(getContext(), getEntries(), index);

		builder.setAdapter(listAdapter, this);
		super.onPrepareDialogBuilder(builder);
	}
	
	private class ImageArrayAdapter extends BaseAdapter {
		
		final CharSequence[] items;
		private LayoutInflater mInflater;
		private int index = 0;
		Context mContext = null;

		public ImageArrayAdapter(Context context, CharSequence[] objects, int i) {
			mContext = context;
			items = objects;
			index = i;
			mInflater = LayoutInflater.from(context);
		}
		
		public int getCount() {
			return items.length;
		}
		 
		public CharSequence getItem(int position) {
			return items[position];
		}
		 
		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View row = mInflater.inflate(R.layout.select_dialog_with_images, parent, false);

			HtcListItem2LineText itemTitle = (HtcListItem2LineText) row.findViewById(R.id.list_item);
			HtcListItemTileImage itemIcon = (HtcListItemTileImage) row.findViewById(R.id.list_item_img);
			HtcListItemRadioButton itemRadio = (HtcListItemRadioButton) row.findViewById(R.id.list_item_radio);
			
			itemTitle.setPrimaryText(getItem(position));
			itemTitle.setSecondaryTextVisibility(8);
			itemIcon.setTileImageDrawable(PrefsFragment.pkgAppsListIcons.get(position));
			itemIcon.setScaleX(0.65f);
			itemIcon.setScaleY(0.65f);
			itemIcon.setTranslationX(mContext.getResources().getDisplayMetrics().density * 5.0f);

			//if (PrefsFragment.pkgAppsListSystem.get(position)) 
			if (position == index) itemRadio.setChecked(true);

			return row;
		}
	}
}
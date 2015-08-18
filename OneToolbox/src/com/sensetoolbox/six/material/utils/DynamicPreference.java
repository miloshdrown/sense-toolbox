package com.sensetoolbox.six.material.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.AppData;
import com.sensetoolbox.six.utils.Helpers;
import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class DynamicPreference extends ListPreference {
	
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
		for (int i = 0; i < Helpers.launchableAppsList.size(); i++) {
			entries.add(Helpers.launchableAppsList.get(i).label);
			entryValues.add(Helpers.launchableAppsList.get(i).pkgName + "|" + Helpers.launchableAppsList.get(i).actName);
		}
		setEntries(entries.toArray(new CharSequence[entries.size()]));
		setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
	}

	@Override
	protected View onCreateDialogView() {
		ListView view = new ListView(getContext());
		view.setDividerHeight(0);
		view.setFooterDividersEnabled(false);
		setEntries(new CharSequence[0]);
		setEntryValues(new CharSequence[0]);
		return view;
	}
	
	@Override
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	protected void onPrepareDialogBuilder(Builder builder) {
		int index = findIndexOfValue(getSharedPreferences().getString(getKey(), "1"));

		ListAdapter listAdapter = new ImageArrayAdapter(getContext(), getEntries(), index);

		builder.setAdapter(listAdapter, this);
		builder.setView(0);
		super.onPrepareDialogBuilder(builder);
	}
	
	private class ImageArrayAdapter extends BaseAdapter {
		
		final CharSequence[] items;
		private LayoutInflater mInflater;
		private int index = 0;
		Context mContext = null;
		private ThreadPoolExecutor pool;
		private int cpuCount = Runtime.getRuntime().availableProcessors();

		public ImageArrayAdapter(Context context, CharSequence[] objects, int i) {
			mContext = context;
			items = objects;
			index = i;
			mInflater = LayoutInflater.from(context);
			pool = new ThreadPoolExecutor(cpuCount + 1, cpuCount * 2 + 1, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
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
			View row;
			if (convertView != null)
				row = convertView;
			else
				row = mInflater.inflate(R.layout.mselect_dialog_with_images, parent, false);
			
			TextView itemTitle = (TextView) row.findViewById(android.R.id.text1);
			ImageView itemIcon = (ImageView) row.findViewById(android.R.id.icon);
			itemIcon.setTag(position);
			RadioButton itemRadio = (RadioButton) row.findViewById(android.R.id.checkbox);
			
			itemTitle.setText(getItem(position));
			
			AppData ad = Helpers.launchableAppsList.get(position);
			String cacheKey = ad.pkgName;
			if (ad.actName != null) cacheKey += "|" + ad.actName;
			Bitmap icon = Helpers.memoryCache.get(cacheKey);
			if (icon == null) {
				itemIcon.setAlpha(0.0f);
				(new BitmapCachedLoader(itemIcon, ad, mContext)).executeOnExecutor(pool);
			} else {
				itemIcon.setAlpha(1.0f);
				itemIcon.setImageBitmap(icon);
			}
			
			itemIcon.setScaleX(0.8f);
			itemIcon.setScaleY(0.8f);
			//itemIcon.setTranslationX(mContext.getResources().getDisplayMetrics().density * 6.0f);

			if (position == index)
				itemRadio.setChecked(true);
			else
				itemRadio.setChecked(false);

			itemRadio.setBackgroundColor(Color.TRANSPARENT);
			return row;
		}
	}
}
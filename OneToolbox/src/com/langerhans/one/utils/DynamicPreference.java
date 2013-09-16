package com.langerhans.one.utils;

import java.util.ArrayList;
import java.util.List;

import com.htc.preference.HtcListPreference;
import com.langerhans.one.PrefsFragment;
import com.langerhans.one.R;

import com.htc.widget.HtcAlertDialog.Builder;
import com.htc.widget.HtcListView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

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

		ListAdapter listAdapter = new ImageArrayAdapter(getContext(), R.layout.select_dialog_with_images, getEntries(), index);

		builder.setAdapter(listAdapter, this);
		super.onPrepareDialogBuilder(builder);
	}
	
	private class ImageArrayAdapter extends ArrayAdapter<CharSequence> {
		private int index = 0;

		public ImageArrayAdapter(Context context, int textViewResourceId, CharSequence[] objects, int i) {
			super(context, textViewResourceId, objects);
			index = i;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
			View row = inflater.inflate(R.layout.select_dialog_with_images, parent, false);

			ImageView imageView = (ImageView)row.findViewById(android.R.id.icon1);
			imageView.setImageDrawable(PrefsFragment.pkgAppsListIcons.get(position));
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(row.getLayoutParams().height, row.getLayoutParams().height);
			imageView.setLayoutParams(lp);
			
			CheckedTextView checkedTextView = (CheckedTextView)row.findViewById(android.R.id.text1);
			checkedTextView.setText(getItem(position));
			if (PrefsFragment.pkgAppsListSystem.get(position)) checkedTextView.setTypeface(null, Typeface.BOLD); 

			if (position == index) {
				checkedTextView.setChecked(true);
			}

			return row;
		}
	}
}
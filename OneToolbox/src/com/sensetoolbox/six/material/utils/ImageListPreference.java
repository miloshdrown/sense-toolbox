package com.sensetoolbox.six.material.utils;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.GlobalActions;
import com.sensetoolbox.six.utils.Helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class ImageListPreference extends ListPreference {

	private int[] resourceIds = null;
	private int[] imageThemes = null;
	private String namespace = "http://schemas.android.com/apk/res/com.sensetoolbox.six";

	public ImageListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		int namesResId = attrs.getAttributeResourceValue(namespace, "entryImages", -1);
		String[] imageNames = null;
		if (namesResId > 0) imageNames = context.getResources().getStringArray(namesResId);
		if (imageNames != null) {
			resourceIds = new int[imageNames.length];
			for (int i = 0; i < imageNames.length; i++) {
				if (imageNames[i].equals(""))
					resourceIds[i] = 0;
				else {
					String imageName = imageNames[i].substring(imageNames[i].lastIndexOf('/') + 1, imageNames[i].lastIndexOf('.'));
					resourceIds[i] = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
				}
			}
		}
		
		int themesResId = attrs.getAttributeResourceValue(namespace, "entryThemes", -1);
		if (themesResId > 0) imageThemes = context.getResources().getIntArray(themesResId);
	}
	
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		int index = findIndexOfValue(getSharedPreferences().getString(getKey(), "0"));
		ListAdapter listAdapter = new ImageArrayAdapter(getContext(), R.layout.select_dialog_with_images, getEntries(), resourceIds, imageThemes, index);
		builder.setAdapter(listAdapter, this);
		super.onPrepareDialogBuilder(builder);
	}
	
	public class ImageArrayAdapter extends ArrayAdapter<CharSequence> {
		private int index = 0;
		private int[] resourceIds = null;
		private int[] imageThemes = null;
		private LayoutInflater mInflater;
		
		public ImageArrayAdapter(Context context, int textViewResourceId, CharSequence[] objects, int[] ids, int[] themes, int i) {
			super(context, textViewResourceId, objects);
			index = i;
			resourceIds = ids;
			imageThemes = themes;
			mInflater = LayoutInflater.from(context);
		}
		
		public View getView(int position, View view, ViewGroup parent) {
			View row;
			if (view != null)
				row = view;
			else
				row = mInflater.inflate(R.layout.mselect_dialog_with_images, parent, false);
			
			TextView title = (TextView)row.findViewById(android.R.id.text1);
			ImageView img = (ImageView)row.findViewById(android.R.id.icon);
			title.setText(getItem(position));
			if (resourceIds != null && (resourceIds[position]) != 0) {
				Drawable icon = getContext().getResources().getDrawable(resourceIds[position]);
				img.setImageDrawable(icon);
				
				if (imageThemes != null) {
					if (imageThemes[position] == 1)
						img.getDrawable().setColorFilter(GlobalActions.createColorFilter(false));
					else
						img.getDrawable().clearColorFilter();
				}

				if (resourceIds[position] == R.drawable.stat_sys_wifi_signal_preview || resourceIds[position] == R.drawable.stat_sys_battery_preview || resourceIds[position] == R.drawable.stat_sys_battery_preview2)
					img.setImageDrawable(Helpers.dropIconShadow(getContext(), icon, true));
				else if (resourceIds[position] == R.drawable.b_stat_sys_wifi_signal_4)
					img.setImageDrawable(Helpers.dropIconShadow(getContext(), icon, false));
				
				img.setScaleX(0.55f);
				img.setScaleY(0.55f);
			} else img.setImageDrawable(null);
			RadioButton radio = (RadioButton)row.findViewById(android.R.id.checkbox);
			
			if (position == index)
				radio.setChecked(true);
			else
				radio.setChecked(false);
			
			radio.setBackgroundColor(Color.TRANSPARENT);
			return row;
		}
	}
}

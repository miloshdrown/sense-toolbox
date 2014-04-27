package com.sensetoolbox.six.utils;

import com.htc.preference.HtcListPreference;
import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcListItem2LineText;
import com.htc.widget.HtcListItemTileImage;
import com.htc.widget.HtcRadioButton;
import com.sensetoolbox.six.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class ImageListPreference extends HtcListPreference {

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
	
	protected void onPrepareDialogBuilder(HtcAlertDialog.Builder builder) {
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
			View row = mInflater.inflate(R.layout.select_dialog_with_images, parent, false);
			
			HtcListItem2LineText title = (HtcListItem2LineText)row.findViewById(android.R.id.text1);
			HtcListItemTileImage img = (HtcListItemTileImage)row.findViewById(android.R.id.icon);
			title.setPrimaryText(getItem(position));
			title.setSecondaryTextVisibility(8);
			if (resourceIds != null && (resourceIds[position]) != 0) {
				Drawable icon = getContext().getResources().getDrawable(resourceIds[position]);
				img.setTileImageDrawable(icon);
				
				if (imageThemes != null) {
					if (imageThemes[position] == 1)
						img.getTileImageDrawable().setColorFilter(GlobalActions.createColorFilter(false));
					else
						img.getTileImageDrawable().clearColorFilter();
				}

				if (resourceIds[position] == R.drawable.stat_sys_wifi_signal_preview || resourceIds[position] == R.drawable.stat_sys_battery_preview)
					img.setTileImageDrawable(Helpers.dropIconShadow(getContext(), icon, true));
				else if (resourceIds[position] == R.drawable.b_stat_sys_wifi_signal_4)
					img.setTileImageDrawable(Helpers.dropIconShadow(getContext(), icon, false));
				
				img.setScaleX(0.55f);
				img.setScaleY(0.55f);
			}
			HtcRadioButton radio = (HtcRadioButton)row.findViewById(android.R.id.checkbox);
			
			if (position == index)
				radio.setChecked(true);
			else
				radio.setChecked(false);
			return row;
		}
	}
}

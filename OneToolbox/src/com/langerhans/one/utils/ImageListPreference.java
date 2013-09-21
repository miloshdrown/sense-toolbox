package com.langerhans.one.utils;

import com.htc.preference.HtcListPreference;
import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcListItem2LineText;
import com.htc.widget.HtcListItemRadioButton;
import com.htc.widget.HtcListItemTileImage;
import com.langerhans.one.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class ImageListPreference extends HtcListPreference {

	private int[] resourceIds = null;

	public ImageListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageListPreference);
		String[] imageNames = context.getResources().getStringArray(typedArray.getResourceId(typedArray.getIndexCount() - 1, -1));
		resourceIds = new int[imageNames.length];
		for (int i = 0; i < imageNames.length; i++) {
			if (imageNames[i].equals(""))
				resourceIds[i] = 0;
			else {
				String imageName = imageNames[i].substring(imageNames[i].lastIndexOf('/') + 1, imageNames[i].lastIndexOf('.'));
				resourceIds[i] = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
			}				
		}
		typedArray.recycle();
	}
	
	protected void onPrepareDialogBuilder(HtcAlertDialog.Builder builder) {
		int index = findIndexOfValue(getSharedPreferences().getString(getKey(), "0"));
		ListAdapter listAdapter = new ImageArrayAdapter(getContext(), R.layout.select_dialog_with_images, getEntries(), resourceIds, index);
		builder.setAdapter(listAdapter, this);
		super.onPrepareDialogBuilder(builder);
	}
	
	public class ImageArrayAdapter extends ArrayAdapter<CharSequence> {
		private int index = 0;
		private int[] resourceIds = null;
		private LayoutInflater mInflater;
		
		public ImageArrayAdapter(Context context, int textViewResourceId, CharSequence[] objects, int[] ids, int i) {
			super(context, textViewResourceId, objects);
			index = i;
			resourceIds = ids;
			mInflater = LayoutInflater.from(context);
        }
		
		public View getView(int position, View view, ViewGroup parent) {
			View row = mInflater.inflate(R.layout.select_dialog_with_images, parent, false);
			
			HtcListItem2LineText title = (HtcListItem2LineText)row.findViewById(R.id.list_item);
			HtcListItemTileImage img = (HtcListItemTileImage)row.findViewById(R.id.list_item_img);
			title.setPrimaryText(getItem(position));
			title.setSecondaryTextVisibility(8);
			if ((resourceIds[position]) != 0) {
				img.setTileImageResource(resourceIds[position]);
				img.setScaleX(0.55f);
				img.setScaleY(0.55f);
			}			
			HtcListItemRadioButton radio = (HtcListItemRadioButton)row.findViewById(R.id.list_item_radio);
			
			if (position == index) radio.setChecked(true);
			return row;
		}
	}
}

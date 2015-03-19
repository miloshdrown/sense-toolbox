package com.sensetoolbox.six.utils;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.mods.XMain;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.XModuleResources;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PopupAdapter extends BaseAdapter {
	
	final String[] items;
	private LayoutInflater mInflater;
	boolean isRecents;

	public PopupAdapter(Context context, String[] objects, boolean recents) {
		items = objects;
		mInflater = LayoutInflater.from(context);
		isRecents = recents;
	}
	
	public int getCount() {
		return items.length;
	}
	
	public String getItem(int position) {
		return items[position];
	}
	
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("NewApi")
	public View getView(int position, View convertView, ViewGroup parent) {
		XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
		TextView itemTitle;
		if (convertView != null)
			itemTitle = (TextView)convertView;
		else
			itemTitle = (TextView)mInflater.inflate(modRes.getLayout(R.layout.simple_list_item), parent, false);

		itemTitle.setText(getItem(position));
		
		float density = parent.getResources().getDisplayMetrics().density;
		if (isRecents) {
			int theWidth = Math.round(parent.getResources().getDisplayMetrics().widthPixels / 3 + 20 * density);
			itemTitle.setSingleLine();
			itemTitle.setWidth(theWidth);
			itemTitle.setTextSize(17.0f);
			itemTitle.setPadding(Math.round(5 * density), Math.round(5 * density), Math.round(5 * density), Math.round(5 * density));
			itemTitle.setGravity(Gravity.START);
		} else {
			itemTitle.setPadding(Math.round(10 * density), Math.round(8 * density), Math.round(5 * density), Math.round(8 * density));
			itemTitle.setWidth(parent.getWidth());
			if (position == 5)
				if (Boolean.parseBoolean(Settings.System.getString(itemTitle.getContext().getContentResolver(), "lock_homescreen_dragging")))
					itemTitle.setText(Helpers.xl10n(modRes, R.string.array_home_menu_dragunlock));
				else
					itemTitle.setText(Helpers.xl10n(modRes, R.string.array_home_menu_draglock));
		}
		
		if (Helpers.isLP()) {
			itemTitle.setPadding(Math.round(20 * density), Math.round(12 * density), Math.round(20 * density), Math.round(12 * density));
			itemTitle.setBackgroundColor(0xff404040);
			itemTitle.setTextColor(0xffeeeeee);
			itemTitle.setIncludeFontPadding(false);
		}
		
		return itemTitle;
	}
}

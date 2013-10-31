package com.langerhans.one.utils;

import com.langerhans.one.R;
import com.langerhans.one.mods.XMain;

import android.content.Context;
import android.content.res.XModuleResources;
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

	public View getView(int position, View convertView, ViewGroup parent) {
		TextView itemTitle;
		if (convertView != null)
			itemTitle = (TextView)convertView;
		else {
			XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
			itemTitle = (TextView)mInflater.inflate(modRes.getLayout(R.layout.simple_list_item), parent, false);			
		}
		itemTitle.setText(getItem(position));
		
		float density = parent.getResources().getDisplayMetrics().density;
		if (isRecents) {
			int theWidth = Math.round(parent.getResources().getDisplayMetrics().widthPixels / 3 + 20 * density);
			itemTitle.setSingleLine();
			itemTitle.setWidth(theWidth);
			itemTitle.setTextSize(17.0f);
			itemTitle.setPadding(Math.round(5 * density), Math.round(5 * density), Math.round(5 * density), Math.round(5 * density));
			itemTitle.setGravity(Gravity.LEFT);
		} else {
			itemTitle.setPadding(Math.round(10 * density), Math.round(8 * density), Math.round(5 * density), Math.round(8 * density));
			itemTitle.setWidth(parent.getWidth());
		}
		return itemTitle;
	}
}

package com.langerhans.one.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PopupAdapter extends BaseAdapter {
	
	final String[] items;
	private LayoutInflater mInflater;
	Context mContext = null;

	public PopupAdapter(Context context, String[] objects) {
		mContext = context;
		items = objects;
		mInflater = LayoutInflater.from(context);
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
		View row = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
		TextView itemTitle = (TextView)row.findViewById(android.R.id.text1);
		itemTitle.setText(getItem(position));
		itemTitle.setWidth(parent.getWidth());
		return row;
	}
}

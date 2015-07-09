package com.sensetoolbox.six.material.utils;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

public class AppShortcutAddDialog extends AlertDialog {
	Activity act = null;
	String key = null;
	String keyContents = null;
	SharedPreferences prefs = null;
	List<ResolveInfo> shortcuts;
	
	public AppShortcutAddDialog(Activity prefAct, String thekey) {
		super(prefAct);
		act = prefAct;
		prefs = act.getSharedPreferences("one_toolbox_prefs", 1);
		key = thekey;
		keyContents = prefs.getString(key, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Intent shortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
		PackageManager pm = act.getPackageManager();
		shortcuts = pm.queryIntentActivities(shortcutIntent, 0);
		
		int selected = -1;
		int cnt = 0;
		if (keyContents != null) for (ResolveInfo shortcut: shortcuts)
		if (keyContents.equals(shortcut.activityInfo.packageName + "|" + shortcut.activityInfo.name)) {
			selected = cnt;
			break;
		} else cnt++;
		
		final ListView listView = new ListView(this.getContext());
		listView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		listView.setId(android.R.id.list);
		listView.setAdapter(new ImageArrayAdapter(act, selected));
		listView.setDividerHeight(0);
		listView.setFooterDividersEnabled(false);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
				ComponentName cn = new ComponentName(shortcuts.get(position).activityInfo.packageName, shortcuts.get(position).activityInfo.name);
				createShortcutIntent.setComponent(cn);
				Helpers.lastShortcutKey = key;
				Helpers.lastShortcutKeyContents = shortcuts.get(position).activityInfo.packageName + "|" + shortcuts.get(position).activityInfo.name;
				act.startActivityForResult(createShortcutIntent, 7350);
			}
		});
		listView.setSelection(selected);
		this.setButton(DialogInterface.BUTTON_POSITIVE, Helpers.l10n(this.getContext(), R.string.array_recents_menu_close), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});
		this.setCancelable(true);
		this.setInverseBackgroundForced(true);
		this.setView(listView);
		super.onCreate(savedInstanceState);
	}
	
	private class ImageArrayAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private int selected = -1;
		
		public ImageArrayAdapter(Context context, int i) {
			mInflater = LayoutInflater.from(context);
			selected = i;
		}

		public int getCount() {
			return shortcuts.size();
		}
		
		public CharSequence getItem(int position) {
			return shortcuts.get(position).loadLabel(act.getPackageManager());
		}
		
		public Drawable getIcon(int position) {
			return shortcuts.get(position).loadIcon(act.getPackageManager());
		}
		
		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView != null)
				row = convertView;
			else
				row = mInflater.inflate(R.layout.mselect_dialog_with_images_and_shortcuts, parent, false);
			
			TextView itemTitle = (TextView)row.findViewById(android.R.id.text1);
			itemTitle.setText(getItem(position));
			
			ImageView itemIcon = (ImageView)row.findViewById(android.R.id.icon1);
			itemIcon.setImageDrawable(getIcon(position));
			//itemIcon.setScaleX(0.85f);
			//itemIcon.setScaleY(0.85f);
			//itemIcon.setTranslationX(act.getResources().getDisplayMetrics().density * 5.0f);
			
			ImageView arrowIcon = (ImageView)row.findViewById(android.R.id.icon);
			arrowIcon.setImageResource(R.drawable.right_arrow);
			arrowIcon.setScaleX(0.833f);
			arrowIcon.setScaleY(0.833f);
			arrowIcon.setTranslationX(act.getResources().getDisplayMetrics().density * -3.0f);
			
			RelativeLayout hint = (RelativeLayout)row.findViewById(android.R.id.hint);
			RadioButton radioBtn = (RadioButton)row.findViewById(android.R.id.checkbox);
			
			if (position == selected) try {
				radioBtn.setChecked(true);
				hint.setVisibility(View.VISIBLE);

				String shortcutName = prefs.getString(key + "_name", null);
				String shortcutIcon = prefs.getString(key + "_icon", null);
				
				if (shortcutName != null) {
					TextView itemTitle2 = (TextView)row.findViewById(android.R.id.text2);
					itemTitle2.setText(shortcutName);
				}
				
				if (shortcutIcon != null) {
					ImageView itemIcon2 = (ImageView)row.findViewById(android.R.id.icon2);
					if (itemIcon2.getDrawable() == null) {
						itemIcon2.setImageDrawable(Drawable.createFromPath(shortcutIcon));
						//itemIcon2.setScaleX(0.85f);
						//itemIcon2.setScaleY(0.85f);
						//itemIcon2.setTranslationX(act.getResources().getDisplayMetrics().density * 5.0f);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} else {
				radioBtn.setChecked(false);
				hint.setVisibility(View.GONE);
			}
			
			radioBtn.setBackgroundColor(Color.TRANSPARENT);
			return row;
		}
	}
}
package com.sensetoolbox.six.htc;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcListItem2LineText;
import com.htc.widget.HtcListItemTileImage;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

public class HAPMDialog extends HtcAlertDialog.Builder {
	
	public HAPMDialog(final Context context) {
		super(context);
		
		this.setTitle(Helpers.l10n(context, R.string.apm_title));
		this.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				((Activity)context).finish();
			}
		});
		
		class MenuItem {
			private String mTitle;
			private String mSummary;
			private int mIcon;
			
			public MenuItem(String title, String summary, int icon) {
				mTitle = title;
				mIcon = icon;
				mSummary = summary;
			}
			
			public String getTitle() {
				return mTitle;
			}
			public String getSummary() {
				return mSummary;
			}
			public int getIcon() {
				return mIcon;
			}
		}
		
		final ArrayList<MenuItem> items = new ArrayList<MenuItem>();
		items.add(new MenuItem(Helpers.l10n(context, R.string.apm_normal_title), Helpers.l10n(context, R.string.apm_normal_summ), R.drawable.apm_reboot));
		items.add(new MenuItem(Helpers.l10n(context, R.string.soft_reboot), Helpers.l10n(context, R.string.apm_hotreboot_summ), R.drawable.apm_hotreboot));
		items.add(new MenuItem(Helpers.l10n(context, R.string.apm_recovery_title), Helpers.l10n(context, R.string.apm_recovery_summ), R.drawable.apm_recovery));
		items.add(new MenuItem(Helpers.l10n(context, R.string.apm_bootloader_title), Helpers.l10n(context, R.string.apm_bootloader_summ), R.drawable.apm_bootloader));
		
		class HtcAlertDialogAdapter extends BaseAdapter {
			
			final ArrayList<MenuItem> items;
			private LayoutInflater mInflater;
			
			public HtcAlertDialogAdapter(Context context, ArrayList<MenuItem> itms) {
				items = itms;
				mInflater = LayoutInflater.from(context);
			}
			
			public int getCount() {
				return items.size();
			}
			
			public Object getItem(int position) {
				return items.get(position);
			}
			
			public long getItemId(int position) {
				return position;
			}
			
			@SuppressLint("InflateParams")
			public View getView(int position, View convertView, ViewGroup parent) {
				View row;
				if (convertView == null)
					row = mInflater.inflate(R.layout.select_dialog_apm, null);
				else
					row = convertView;
				
				HtcListItem2LineText itemTitle = (HtcListItem2LineText) row.findViewById(R.id.list_item);
				HtcListItemTileImage itemIcon = (HtcListItemTileImage) row.findViewById(R.id.list_item_img);
				
				itemTitle.setPrimaryText(items.get(position).getTitle());
				itemTitle.setSecondaryTextSingleLine(true);
				itemTitle.setSecondaryText(items.get(position).getSummary());
				itemIcon.setTileImageResource(items.get(position).getIcon());
				itemIcon.setScaleX(0.65f);
				itemIcon.setScaleY(0.65f);
				itemIcon.setTranslationX(context.getResources().getDisplayMetrics().density * 5.0f);
				
				return row;
			}
		}
		
		this.setAdapter(new HtcAlertDialogAdapter(context, items), new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					Intent rebIntent = new Intent("com.sensetoolbox.six.mods.action.APMReboot");
					((Activity)context).sendBroadcast(rebIntent);
					break;
				case 1:
					try {
						RootTools.getShell(true).add(new Command(0, false, "setprop ctl.restart zygote"));
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case 2:
					Intent recIntent = new Intent("com.sensetoolbox.six.mods.action.APMRebootRecovery");
					((Activity)context).sendBroadcast(recIntent);
					break;
				case 3:
					Intent blIntent = new Intent("com.sensetoolbox.six.mods.action.APMRebootBootloader");
					((Activity)context).sendBroadcast(blIntent);
					break;
				}
				((Activity)context).finish();
			}
		});
	}
}
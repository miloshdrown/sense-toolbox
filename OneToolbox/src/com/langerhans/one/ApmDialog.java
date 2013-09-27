package com.langerhans.one;

import java.util.ArrayList;

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
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class ApmDialog extends HtcAlertDialog.Builder {

	public ApmDialog(final Context context) {
		super(context);

		this.setTitle(R.string.apm_title);

		this.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				((Activity)context).finish();
			}
		});
		
		class MenuItem {
			private int mTitle;
			private int mSummary;
			private int mIcon;
		
			public MenuItem(int title, int summary, int icon) {
				mTitle = title;
				mIcon = icon;
				mSummary = summary;
			}
		
			public int getTitle() {
				return mTitle;
			}
			public int getIcon() {
				return mIcon;
			}
			public int getSummary() {
				return mSummary;
			}
		}
		
		final ArrayList<MenuItem> items = new ArrayList<MenuItem>();
		items.add(new MenuItem(R.string.apm_normal_title, R.string.apm_normal_summ, R.drawable.apm_reboot));
		items.add(new MenuItem(R.string.apm_hotreboot_title, R.string.apm_hotreboot_summ, R.drawable.apm_hotreboot));
		items.add(new MenuItem(R.string.apm_recovery_title, R.string.apm_recovery_summ, R.drawable.apm_recovery));
		items.add(new MenuItem(R.string.apm_bootloader_title, R.string.apm_bootloader_summ, R.drawable.apm_bootloader));

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
			 
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null)
				convertView = mInflater.inflate(R.layout.select_dialog_apm, null);

				HtcListItem2LineText itemTitle = (HtcListItem2LineText) convertView.findViewById(R.id.list_item);
				HtcListItemTileImage itemIcon = (HtcListItemTileImage) convertView.findViewById(R.id.list_item_img);
				
				itemTitle.setPrimaryText(items.get(position).getTitle());
				itemTitle.setSecondaryTextSingleLine(true);
				itemTitle.setSecondaryText(items.get(position).getSummary());
				itemIcon.setTileImageResource(items.get(position).getIcon());
				itemIcon.setScaleX(0.65f);
				itemIcon.setScaleY(0.65f);
				itemIcon.setTranslationX(context.getResources().getDisplayMetrics().density * 5.0f);
				
				return convertView;
			}
		}
		
		
		this.setAdapter(new HtcAlertDialogAdapter(context, items), new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0: 
					Intent rebIntent = new Intent("com.langerhans.one.mods.action.APMReboot");
					((Activity)context).sendBroadcast(rebIntent);
					break;
				case 1:
					try {
						CommandCapture command = new CommandCapture(0, "setprop ctl.restart zygote");
						RootTools.getShell(true).add(command).waitForFinish();
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case 2:
					Intent recIntent = new Intent("com.langerhans.one.mods.action.APMRebootRecovery");
					((Activity)context).sendBroadcast(recIntent);
					break;
				case 3:
					Intent blIntent = new Intent("com.langerhans.one.mods.action.APMRebootBootloader");
					((Activity)context).sendBroadcast(blIntent);
					break;
				}
				((Activity)context).finish();
			}
		});
	}
}
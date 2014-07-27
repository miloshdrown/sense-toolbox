package com.sensetoolbox.six.utils;

import java.util.HashSet;

import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcRimButton;
import com.sensetoolbox.six.DimmedActivity;
import com.sensetoolbox.six.R;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class NotificationTab extends Fragment {
	SharedPreferences prefs;
	DimmedActivity act;
	StatusBarNotification sbn;
	boolean isInBigView = true;
	boolean doubleSwipeFailed = false;
	//int touchPositionX, touchCurrentPositionX;
	int touchPositionY, touchCurrentPositionY;
	
	public NotificationTab() {
		super();
	}
	
	private int densify(int dimens) {
		return Math.round(getActivity().getResources().getDisplayMetrics().density * dimens);
	}
	
	private void cancelNotification() {
		Intent cancelIntent = new Intent("com.sensetoolbox.six.CLEARNOTIFICATION");
		cancelIntent.putExtra("pkgName", sbn.getPackageName());
		cancelIntent.putExtra("tag", sbn.getTag());
		cancelIntent.putExtra("id", sbn.getId());
		cancelIntent.putExtra("userId", sbn.getUserId());
		act.sendBroadcast(cancelIntent);
	}
	
	private void updateView(final LinearLayout tab) {
		try {
			final int id = getArguments().getInt("id");
			final String pkgName = getArguments().getString("pkgName");
			final String appName = getArguments().getString("appName");
			final String tag = String.valueOf(getArguments().getString("tag"));
			final String uniqueTag = pkgName + "_" + String.valueOf(id) + "_" + String.valueOf(tag);

			act = ((DimmedActivity)getActivity());
			sbn = act.findInLatest(pkgName, id, tag);
			if (sbn != null) {
				RemoteViews content = null;
				if (prefs == null || prefs.getBoolean("pref_key_other_popupnotify_expand", true))
				content = sbn.getNotification().bigContentView;
				if (content == null) {
					content = sbn.getNotification().contentView;
					isInBigView = false;
				}
				if (content != null) {
					View localContent = content.apply(act, tab);
					final RelativeLayout notifyRemote = (RelativeLayout)tab.findViewById(R.id.notifyRemote);
					notifyRemote.addView(localContent);
					notifyRemote.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (sbn.getNotification().contentIntent != null) {
								new Thread(new Runnable() {
									public void run() {
										try {
											Thread.sleep(500);
											Intent sendContentIntent = new Intent("com.sensetoolbox.six.SENDCONTENTINTENT");
											sendContentIntent.putExtra("contentIntent", sbn.getNotification().contentIntent);
											act.sendBroadcast(sendContentIntent);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}).start();
								act.finish();
								cancelNotification();
							}
						}
					});
					
					FrameLayout sblec = (FrameLayout)notifyRemote.findViewById(getResources().getIdentifier("status_bar_latest_event_content", "id", "android"));
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)sblec.getLayoutParams();
					lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
					sblec.setLayoutParams(lp);

					notifyRemote.setOnTouchListener(new OnTouchListener() {
						@Override
						public boolean onTouch(View v, MotionEvent event) {
							int pointerCount = event.getPointerCount();
							int action = event.getActionMasked();
							if (pointerCount == 2) {
								switch (action) {
									case MotionEvent.ACTION_POINTER_DOWN:
										touchPositionY = (int)event.getY(1);
										touchCurrentPositionY = touchPositionY;
										doubleSwipeFailed = false;
										break;
									case MotionEvent.ACTION_MOVE:
										if (doubleSwipeFailed) return true;
										touchCurrentPositionY = (int)event.getY(1);
										int diff = touchCurrentPositionY - touchPositionY;
										if (diff > 100 && !isInBigView) {
											RemoteViews contentNew = sbn.getNotification().bigContentView;
											if (contentNew != null) {
												View localContent = contentNew.apply(act, tab);
												notifyRemote.removeAllViews();
												notifyRemote.addView(localContent);
												isInBigView = true;
												updateHeight(tab, uniqueTag);
												updateIcon(act, pkgName, notifyRemote, sbn);
											} else {
												Toast.makeText(getActivity(), Helpers.l10n(getActivity(), R.string.popupnotify_nobigview), Toast.LENGTH_SHORT).show();
												doubleSwipeFailed = true;
											}
										} else if (diff < -100 && isInBigView) {
											RemoteViews contentNew = sbn.getNotification().contentView;
											if (contentNew != null) {
												View localContent = contentNew.apply(act, tab);
												notifyRemote.removeAllViews();
												notifyRemote.addView(localContent);
												isInBigView = false;
												updateHeight(tab, uniqueTag);
												updateIcon(act, pkgName, notifyRemote, sbn);
											} else {
												Toast.makeText(getActivity(), Helpers.l10n(getActivity(), R.string.popupnotify_noview), Toast.LENGTH_SHORT).show();
												doubleSwipeFailed = true;
											}
										}
										break;
									case MotionEvent.ACTION_POINTER_UP:
										touchPositionY = 0;
										break;
								}
							} else {
								switch (action) {
									case MotionEvent.ACTION_DOWN:
										touchPositionY = 0;
										//touchPositionX = (int)event.getX();
										touchCurrentPositionY = 0;
										//touchCurrentPositionX = touchPositionX;
										//if (sblec != null) sblec.setBackgroundColor(Color.argb(20, 255, 255, 255));
										break;
									case MotionEvent.ACTION_MOVE:
										//touchCurrentPositionX = (int)event.getX();
										//if (Math.abs(touchCurrentPositionX - touchPositionX) > 10)
										//if (sblec != null) sblec.setBackgroundColor(Color.TRANSPARENT);
										break;
									case MotionEvent.ACTION_UP:
										//if (sblec != null) sblec.setBackgroundColor(Color.TRANSPARENT);
										if (touchCurrentPositionY == 0) v.performClick();
										//touchPositionX = 0;
										break;
								}
							}
							return true;
						}
					});
					
					updateIcon(act, pkgName, notifyRemote, sbn);
				}
			}
			
			LinearLayout notifyDismiss = (LinearLayout)tab.findViewById(R.id.notifyDismiss);
			notifyDismiss.setGravity(Gravity.CENTER);
			
			OnLongClickListener blacklist = new OnLongClickListener() {
				@Override
				public boolean onLongClick(final View v) {
					if (prefs.getBoolean("pref_key_other_popupnotify_bwlist", false)) return true;
					
					HtcAlertDialog.Builder blconfirm = new HtcAlertDialog.Builder(act);
					blconfirm.setTitle(Helpers.l10n(act, R.string.popupnotify_blconfirm));
					TextView centerMsg = new TextView(act);
					centerMsg.setText(String.format(Helpers.l10n(act, R.string.popupnotify_blacklist), appName));
					centerMsg.setGravity(Gravity.CENTER_HORIZONTAL);
					centerMsg.setPadding(20, 60, 20, 60);
					centerMsg.setTextSize(18.0f);
					centerMsg.setTextColor(act.getResources().getColor(android.R.color.primary_text_light));
					blconfirm.setView(centerMsg);
					blconfirm.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							HashSet<String> appsList = new HashSet<String>(prefs.getStringSet("pref_key_other_popupnotify_bwlist_apps", new HashSet<String>()));
							appsList.add(pkgName);
							prefs.edit().putStringSet("pref_key_other_popupnotify_bwlist_apps", new HashSet<String>(appsList)).commit();
							if (act != null) act.sendBroadcast(new Intent("com.sensetoolbox.six.PREFSUPDATED"));
						}
					});
					blconfirm.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {}
					});
					blconfirm.show();
					return true;
				}
			};
			
			HtcRimButton rimBtn = new HtcRimButton(notifyDismiss.getContext());
			rimBtn.setText(Helpers.l10n(getActivity(), R.string.popupnotify_dismiss));
			rimBtn.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			rimBtn.setBackgroundResource(R.color.popup_top_bottom_color);
			rimBtn.setPadding(densify(10), densify(8), densify(10), densify(8));
			rimBtn.setOnLongClickListener(blacklist);
			rimBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (act.sbns.size() > 1)
						act.notifications.getCarouselHost().removeTabByTag(uniqueTag);
					else if (!act.isFinishing())
						act.finish();
					cancelNotification();
				}
			});
			
			if (act.sbns.size() == 1 && act.sleepOnDismissLast) {
				HtcRimButton rimBtnSleep = new HtcRimButton(notifyDismiss.getContext());
				rimBtnSleep.setText(Helpers.l10n(getActivity(), R.string.popupnotify_dismisssleep));
				rimBtnSleep.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
				rimBtnSleep.setBackgroundResource(R.color.popup_top_bottom_color);
				rimBtnSleep.setPadding(densify(20), densify(8), densify(20), densify(8));
				rimBtnSleep.setOnLongClickListener(blacklist);
				rimBtnSleep.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						cancelNotification();
						GlobalActions.goToSleep(getActivity());
						if (!act.isFinishing()) act.finish();
					}
				});
				notifyDismiss.addView(rimBtnSleep);
				rimBtn.setText(Helpers.l10n(getActivity(), R.string.popupnotify_dismissonly));
				rimBtn.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
			}
			notifyDismiss.addView(rimBtn);
			updateHeight(tab, uniqueTag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateHeight(LinearLayout tab, String uniqueTag) {
		tab.measure(
			MeasureSpec.makeMeasureSpec(tab.getResources().getDisplayMetrics().widthPixels, MeasureSpec.AT_MOST),
			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
		);
		//Log.e(null, uniqueTag + " [onCreateView]: " + String.valueOf(tab.getMeasuredHeight()));
		((DimmedActivity)getActivity()).updateTabHeight(uniqueTag, tab.getMeasuredHeight());
	}
	
	private void updateIcon(DimmedActivity act, String pkgName, RelativeLayout notifyRemote, StatusBarNotification sbn) {
		try {
			PackageManager manager = act.getPackageManager();
			Resources sourceRes = manager.getResourcesForApplication(pkgName);
			int iconResid = sbn.getNotification().icon;
			if (iconResid != 0) {
				ImageView icon = (ImageView)notifyRemote.findViewById(android.R.id.icon);
				icon.setBackground(null);
				icon.setImageDrawable(null);
				if (sbn.getNotification().largeIcon != null) {
					Bitmap newBmp = Bitmap.createScaledBitmap(sbn.getNotification().largeIcon, densify(50), densify(50), false);
					icon.setImageBitmap(newBmp);
					icon.setPadding(icon.getPaddingLeft(), densify(10), icon.getPaddingRight(), densify(10));
				} else {
					icon.setImageDrawable(sourceRes.getDrawable(iconResid));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = ((DimmedActivity)getActivity()).getSharedPreferences("one_toolbox_prefs", 1);
		LinearLayout tab = (LinearLayout)inflater.inflate(com.sensetoolbox.six.R.layout.notification_tab, container, false);
		updateView(tab);
		return tab;
	}
}
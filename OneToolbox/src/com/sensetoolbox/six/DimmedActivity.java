package com.sensetoolbox.six;

import java.util.ArrayList;

import com.htc.fragment.widget.CarouselHost.OnTabChangeListener;
import com.htc.fragment.widget.CarouselUtil;
import com.sensetoolbox.six.utils.BlurBuilder;
import com.sensetoolbox.six.utils.Helpers;
import com.sensetoolbox.six.utils.Notifications;
import com.sensetoolbox.six.utils.Notifications.OnCarouselReadyListener;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DimmedActivity extends Activity {
	int dialogType;
	boolean isInLockscreen;
	WindowManager.LayoutParams params;
	String clearIntent = "com.sensetoolbox.six.UPDATENOTIFICATIONS";
	IntentFilter filter = new IntentFilter(clearIntent);
	public Notifications notifications = new Notifications();
	public boolean sleepOnDismissLast = false;
	public ArrayList<StatusBarNotification> sbns = null;
	
	public BroadcastReceiver helperReceiver = new BroadcastReceiver() {    
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null)
			if (intent.getAction().equals(clearIntent)) {
				initNotifications(intent, false);
			}
		}
	};
	
	private void startListen() {
		try {
			this.registerReceiver(helperReceiver, filter);
		} catch (Exception e) {}
	}
	
	private void stopListen() {
		if (helperReceiver != null) try {
			this.unregisterReceiver(helperReceiver);
		} catch (Exception e) {}
	}
	
	private void createDialog(int dType, Intent intent) {
		if (dType == 1) {
			ApmDialog rebD = new ApmDialog(this, dialogType);
			rebD.show();
		} else if (dType == 2) {
			getWindow().getDecorView().setPadding(-1, 0, -1, 0); 
			setContentView(R.layout.notifications);
			findViewById(android.R.id.content).setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					v.performClick();
					finish();
					return true;
				}
			});
			
			initNotifications(intent, true);
			if (isInLockscreen) {
				Bitmap lockBmp = BitmapFactory.decodeResource(getResources(), R.drawable.lockscreen_icon_locked);
				ImageView lockIcon = new ImageView(this);
				lockIcon.setImageBitmap(lockBmp);
				this.addContentView(lockIcon, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
				
				TextView lockScr = new TextView(this);
				lockScr.setGravity(Gravity.CENTER);
				lockScr.setText(Helpers.l10n(this, R.string.popupnotify_taptolockscreen));
				lockScr.setTextAppearance(this, R.style.lockscreen_text);
				lockScr.setPadding(0, 0, 10, 36);
				this.addContentView(lockScr, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
			}
		} else finish();
	}
	
	@Override
	public void onCreate(Bundle bundle) {
		KeyguardManager kgMgr = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
		isInLockscreen = false;
		if (kgMgr.isKeyguardLocked()) isInLockscreen = true;
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()) sleepOnDismissLast = true;
		
		SharedPreferences prefs = getSharedPreferences("one_toolbox_prefs", 1);
		int backStyle = Integer.parseInt(prefs.getString("pref_key_other_popupnotify_back", "1"));
		
		final Intent intent = getIntent();
		dialogType = intent.getIntExtra("dialogType", 1);
		if (dialogType == 2) {
			if (backStyle > 1 && pm.isScreenOn()) {
				Bitmap bmp = (Bitmap)intent.getParcelableExtra("bmp");
				if (bmp != null)
				getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), BlurBuilder.blur(this, bmp, backStyle == 3 ? true : false)));
			}
			if (!isInLockscreen) overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		}

		super.onCreate(bundle);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		startListen();
		createDialog(dialogType, intent);
	}
	
	@Override
	public void onNewIntent(Intent newIntent) {
		super.onNewIntent(newIntent);
		initNotifications(newIntent, false);
	}
	
	String curTabTag = null;
	void initNotifications(final Intent intent, final boolean selectLast) {
		int dialogTypeNew = intent.getIntExtra("dialogType", 1);
		if (dialogType != dialogTypeNew) {
			createDialog(dialogTypeNew, intent);
			return;
		}
		dialogType = dialogTypeNew;
		if (dialogType != 2) return;
		ArrayList<StatusBarNotification> sbnsNew = intent.getParcelableArrayListExtra("sbns");
		if (sbnsNew.equals(sbns))
			return;
		else
			sbns = sbnsNew;
		
		if (sbns.size() == 0) {
			stopListen();
			finish();
			return;
		}
		if (notifications.isLoaded) curTabTag = notifications.getCarouselHost().getCurrentTabTag();

		notifications = new Notifications();
		notifications.setOnCarouselReadyListener(new OnCarouselReadyListener() {
			@Override
			public void onReady() {
				notifications.getCarouselHost().showTabWidget(false);
				notifications.getCarouselWidget().setBackgroundResource(R.color.popup_top_bottom_color);
				int k = sbns.size();
				for (int l = 0; l < k; l++) {
					StatusBarNotification notifyRecord = sbns.get(l);
					if (notifyRecord != null) {
						notifications.addTab(notifyRecord);
						//Log.e(null, String.valueOf(l) + ": " + String.valueOf(notifyRecord.getNotification().priority));
						if (selectLast) curTabTag = notifyRecord.getPackageName() + "_" + String.valueOf(notifyRecord.getId()) + "_" + String.valueOf(notifyRecord.getTag()); 
					}
				}
				if (curTabTag != null)
				notifications.getCarouselHost().setCurrentTabByTag(curTabTag);
			}
		});
		
		notifications.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabTag) {
				curTabTag = tabTag;
				View currentTabView = notifications.getCarouselHost().getCurrentTabFragment().getView();
				if (currentTabView != null) {
					currentTabView.measure(
						MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, MeasureSpec.AT_MOST),
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
					);
					//Log.e(null, tabTag + " [onTabChanged]: " + String.valueOf(currentTabView.getMeasuredHeight()));
					updateTabHeight(tabTag, currentTabView.getMeasuredHeight());
				}
			}
		});
		
		String tagName = "carousel";
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		if (getFragmentManager().findFragmentByTag(tagName) != null) {
			ft.replace(R.id.carousel, notifications, tagName);
		} else {
			ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
			ft.add(R.id.carousel, notifications, tagName);
		}
		ft.commit();
		getFragmentManager().executePendingTransactions();
	}
	
	public StatusBarNotification findInLatest(String pkgName, int id, String tag) {
		int k = sbns.size();
		for (int l = 0; l < k; l++) {
			StatusBarNotification notifyRecord = sbns.get(l);
			if (notifyRecord != null &&
				notifyRecord.getPackageName().equals(pkgName) &&
				notifyRecord.getId() == id &&
				String.valueOf(notifyRecord.getTag()).equals(tag))
			return notifyRecord;
		}
		return null;
	}
	
	public void updateTabHeight(String uniqueTag, int height) {
		if (notifications.getCarouselHost().getCurrentTabTag().equals(uniqueTag)) {
			// Add carousel header height
			height = height + CarouselUtil.Dimen.getWidgetHeight(this, false);

			final LinearLayout carousel = (LinearLayout)this.findViewById(R.id.carousel);
			ValueAnimator anim = ValueAnimator.ofInt(carousel.getMeasuredHeight(), height);
			anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator valueAnimator) {
					int val = (Integer) valueAnimator.getAnimatedValue();
					LayoutParams layoutParams = carousel.getLayoutParams();
					layoutParams.height = val;
					carousel.setLayoutParams(layoutParams);
				}
			});
			anim.setDuration(150);
			anim.start();
		}
	}
	
	@Override
	public void onRestart() {
		startListen();
		super.onRestart();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		stopListen();
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		if (pm.isScreenOn() && !isInLockscreen) finish();
	}
	
	@Override
	public void finish() {
	    super.finish();
	    if (!isInLockscreen) overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
}

package com.sensetoolbox.six.htc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.htc.fragment.widget.CarouselHost.OnTabChangeListener;
import com.htc.fragment.widget.CarouselHost;
import com.htc.fragment.widget.CarouselUtil;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.htc.utils.NotificationTab;
import com.sensetoolbox.six.htc.utils.Notifications;
import com.sensetoolbox.six.htc.utils.Notifications.OnCarouselReadyListener;
import com.sensetoolbox.six.utils.BlurBuilder;
import com.sensetoolbox.six.utils.Helpers;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.KeyguardManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import de.robv.android.xposed.XposedBridge;

public class DimmedActivity extends Activity {
	float density;
	boolean isInLockscreen;
	SharedPreferences prefs;
	AppWidgetHost mAppWidgetHost = null;
	WindowManager.LayoutParams params;
	String clearIntent = "com.sensetoolbox.six.UPDATENOTIFICATIONS";
	IntentFilter filter = new IntentFilter(clearIntent);
	Intent lastIntent;
	boolean bkgPortrait = true;
	BitmapDrawable blurredBkg = null;
	public Notifications notifications = new Notifications();
	public boolean sleepOnDismissLast = false;
	public ArrayList<StatusBarNotification> sbns = null;
	
	public BroadcastReceiver helperReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null)
			if (intent.getAction().equals(clearIntent)) {
				lastIntent = (Intent)intent.clone();
				initNotifications(intent, false);
			}
		}
	};
	
	private void startListen() {
		try {
			this.registerReceiver(helperReceiver, filter);
			Settings.System.putInt(getContentResolver(), "popup_notifications_visible", 1);
			if (mAppWidgetHost != null) mAppWidgetHost.startListening();
		} catch (Exception e) {}
	}
	
	private void stopListen() {
		if (helperReceiver != null) try {
			this.unregisterReceiver(helperReceiver);
			Settings.System.putInt(getContentResolver(), "popup_notifications_visible", 0);
			if (mAppWidgetHost != null) mAppWidgetHost.stopListening();
		} catch (Exception e) {}
	}
	
	private void createDialog(Intent intent) {
		density = getResources().getDisplayMetrics().density;
		getWindow().getDecorView().setPadding(-1, 0, -1, 0);
		setContentView(R.layout.notifications);
		if (prefs.getBoolean("pref_key_other_popupnotify_backclick", true))
		findViewById(android.R.id.content).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Check touch coordinates on screen - workaround for a bug with touch listeners after orientation change
				int[] coords = {0, 0};
				if (notifications != null && notifications.isLoaded) {
					notifications.getCarouselHost().getLocationOnScreen(coords);
					Rect loc = new Rect(coords[0], coords[1], coords[0] + notifications.getCarouselHost().getWidth(), coords[1] + notifications.getCarouselHost().getHeight());
					if (!loc.contains((int)event.getRawX(), (int)event.getRawY())) {
						v.performClick();
						finish();
						return true;
					} else return false;
				} else return false;
			}
		});
			
		RelativeLayout time_date_widget = (RelativeLayout)findViewById(R.id.time_date_widget);
		if (!isInLockscreen) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)time_date_widget.getLayoutParams();
			lp.topMargin = Math.round(density * 15);
			time_date_widget.setLayoutParams(lp);
		}
			
		TextClock time = (TextClock)findViewById(R.id.time);
		TextClock date_dayofweek = (TextClock)findViewById(R.id.date_dayofweek);
		TextClock date_day = (TextClock)findViewById(R.id.date_day);
		TextClock date_month = (TextClock)findViewById(R.id.date_month);
			
		time.setTextSize(24.2f * density);
		date_dayofweek.setTextSize(5.2f * density);
		date_day.setTextSize(5.2f * density);
		date_month.setTextSize(5.2f * density);
		time_date_widget.setVisibility(View.VISIBLE);
			
		int headerStyle = Integer.parseInt(prefs.getString("pref_key_other_popupnotify_clock", "1"));
		if (headerStyle == 1 && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			time_date_widget.setVisibility(View.GONE);
		} else if (headerStyle == 3) {
			AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(this);
			mAppWidgetHost = new AppWidgetHost(this, 1);
			int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
			mAppWidgetHost.startListening();
				
			List<AppWidgetProviderInfo> widgets = mAppWidgetManager.getInstalledProviders();
			for (int i = 0; i < widgets.size(); i++) {
				AppWidgetProviderInfo appWidgetInfo = widgets.get(i);
				if (appWidgetInfo.provider.flattenToString().equals("net.nurik.roman.dashclock/com.google.android.apps.dashclock.WidgetProvider")) {
					AppWidgetHostView hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
					hostView.setAppWidget(appWidgetId, appWidgetInfo);
					hostView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

					time_date_widget.removeAllViews();
					time_date_widget.addView(hostView);
					if (!mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, appWidgetInfo.provider)) Log.e(null, "No permission to bind widgets!");
				break;
				}
			}
		}
			
		initNotifications(intent, true);
		if (isInLockscreen) {
			Bitmap lockBmp = BitmapFactory.decodeResource(getResources(), R.drawable.lockscreen_icon_locked);
			ImageView lockIcon = new ImageView(this);
			lockIcon.setImageBitmap(lockBmp);
			this.addContentView(lockIcon, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
				
			if (prefs.getBoolean("pref_key_other_popupnotify_backclick", true)) {
				TextView lockScr = new TextView(this);
				lockScr.setGravity(Gravity.CENTER);
				lockScr.setText(Helpers.l10n(this, R.string.popupnotify_taptolockscreen));
				lockScr.setTextAppearance(this, R.style.lockscreen_text);
				lockScr.setPadding(0, 0, 10, 36);
				this.addContentView(lockScr, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
			}
		}
	}
	
	float initPosX;
	float initPosY;
	boolean isDraggingVert = false;
	boolean isDraggingHoriz = false;
	LinearLayout carousel;
	float cancelShift;
	
	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle bundle) {
		KeyguardManager kgMgr = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
		isInLockscreen = false;
		if (kgMgr.isKeyguardLocked()) isInLockscreen = true;
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()) sleepOnDismissLast = true;
		
		prefs = getSharedPreferences("one_toolbox_prefs", 1);
		int backStyle = Integer.parseInt(prefs.getString("pref_key_other_popupnotify_back", "1"));
		int headerStyle = Integer.parseInt(prefs.getString("pref_key_other_popupnotify_clock", "1"));
		
		final Intent intent = getIntent();
		lastIntent = (Intent)intent.clone();
		
		if (backStyle > 1 && pm.isScreenOn()) {
			if (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() > 16 * 1024 * 1024) {
				Bitmap bmp = (Bitmap)intent.getParcelableExtra("bmp");
				if (bmp != null && !bmp.isRecycled()) {
					blurredBkg = new BitmapDrawable(getResources(), BlurBuilder.blur(this, bmp, backStyle == 3 ? true : false));
					getWindow().setBackgroundDrawable(blurredBkg);
				}
			}
			bkgPortrait = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? true : false);
		} else if (headerStyle > 1) {
			if (!isInLockscreen) getWindow().getDecorView().setBackgroundColor(Color.argb(170, 0, 0, 0));
		}
		if (!isInLockscreen) overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		
		super.onCreate(bundle);
		if (isInLockscreen && this.getClass() == DimmedActivityLS.class)
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		startListen();
		createDialog(intent);
	}
	
	@Override
	public void onNewIntent(Intent newIntent) {
		lastIntent = (Intent)newIntent.clone();
		super.onNewIntent(newIntent);
		initNotifications(newIntent, false);
	}
	
	String curTabTag = null;
	void initNotifications(final Intent intent, final boolean selectLast) {
		ArrayList<StatusBarNotification> sbnsNew = null;
		try {
			sbnsNew = intent.getParcelableArrayListExtra("sbns");
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		
		if (sbnsNew != null)
		if (sbnsNew.equals(sbns) && !intent.getBooleanExtra("doRotate", false))
			return;
		else
			sbns = sbnsNew;
		
		if (sbnsNew == null || sbns.size() == 0) {
			finish();
			return;
		}
		
		if (notifications.isLoaded) curTabTag = notifications.getCarouselHost().getCurrentTabTag();
		
		notifications = new Notifications();
		notifications.setOnCarouselReadyListener(new OnCarouselReadyListener() {
			@Override
			public void onReady() {
				if (!notifications.isLoaded) return;
				notifications.getCarouselHost().showTabWidget(false);
				notifications.getCarouselWidget().setBackgroundResource(R.color.popup_top_bottom_color);
				notifications.getCarouselHost().findViewById(android.R.id.tabcontent).setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent ev) {
						if (!isDraggingHoriz && isDraggingVert) return true;
						if (ev.getPointerCount() > 1) isDraggingHoriz = false;
						if (ev.getPointerCount() == 1)
						if (ev.getAction() == MotionEvent.ACTION_DOWN) {
							initPosX = ev.getRawX();
							isDraggingHoriz = false;
						} else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
							float curPos = ev.getRawX() - initPosX;
							if (curPos > density * 30f) isDraggingHoriz = true;
						} else if (ev.getAction() == MotionEvent.ACTION_UP) {
							if (isDraggingHoriz)
								isDraggingHoriz = false;
							else
								v.performClick();
						}
						return false;
					}
				});
				
				int k = sbns.size();
				int start = 0;
				if (k > 12) start = k - 12;
				for (int l = start; l < k; l++) {
					StatusBarNotification notifyRecord = sbns.get(l);
					if (notifyRecord != null) {
						notifications.addTab(notifyRecord);
						//Log.e(null, String.valueOf(l) + ": " + String.valueOf(notifyRecord.getNotification().priority));
						if (selectLast) curTabTag = notifyRecord.getPackageName() + "_" + String.valueOf(notifyRecord.getId()) + "_" + String.valueOf(notifyRecord.getTag());
					}
				}
				if (curTabTag != null) try {
					Method getTabSpec = CarouselHost.class.getDeclaredMethod("getTabSpec", String.class);
					getTabSpec.setAccessible(true);
					if (getTabSpec.invoke(notifications.getCarouselHost(), curTabTag) != null)
					notifications.getCarouselHost().setCurrentTabByTag(curTabTag);
				} catch (Throwable t) {}
			}
		});
		
		notifications.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabTag) {
				curTabTag = tabTag;
				if (!notifications.isLoaded) return;
				View currentTabView = notifications.getCarouselHost().getCurrentTabFragment().getView();
				if (currentTabView != null) {
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
					currentTabView.measure(
						MeasureSpec.makeMeasureSpec(Math.round(400 * getResources().getDisplayMetrics().density), MeasureSpec.AT_MOST),
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
					);
					else
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
		try {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			if (getFragmentManager().findFragmentByTag(tagName) != null) {
				ft.replace(R.id.carousel, notifications, tagName);
			} else {
				ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
				ft.add(R.id.carousel, notifications, tagName);
			}
			ft.commit();
			getFragmentManager().executePendingTransactions();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		if (notifications.isLoaded && notifications.getCarouselHost().getCurrentTabTag().equals(uniqueTag)) {
			// Add carousel header height
			int newHeight = height + CarouselUtil.Dimen.getWidgetHeight(this, false);
			
			final LinearLayout carouselLayout = (LinearLayout)this.findViewById(R.id.carousel);
			if (carouselLayout == null) return;
			ValueAnimator anim = ValueAnimator.ofInt(carouselLayout.getMeasuredHeight(), newHeight);
			anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator valueAnimator) {
					int val = (Integer) valueAnimator.getAnimatedValue();
					LayoutParams layoutParams = carouselLayout.getLayoutParams();
					layoutParams.height = val;
					carouselLayout.setLayoutParams(layoutParams);
				}
			});
			anim.setDuration(150);
			anim.start();
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!isDraggingVert && isDraggingHoriz) return super.dispatchTouchEvent(ev);
		if (ev.getPointerCount() > 1) initPosY = ev.getRawY();
		if (ev.getPointerCount() == 1)
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			initPosY = ev.getRawY();
			cancelShift = getResources().getDisplayMetrics().heightPixels / 5;
			isDraggingVert = false;
			carousel = (LinearLayout)this.findViewById(R.id.carousel);
		} else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			float curPos = ev.getRawY() - initPosY;
			if (curPos > density * 30f) {
				isDraggingVert = true;
				if (carousel != null) {
					carousel.setTranslationY(curPos - density * 30f);
					carousel.setAlpha(Math.max(0, cancelShift - curPos) / cancelShift);
					return true;
				}
			}
		} else if (ev.getAction() == MotionEvent.ACTION_UP) {
			if (isDraggingVert && carousel != null) {
				boolean isCanceled = false;
				if (ev.getRawY() - initPosY > cancelShift && notifications.isLoaded) {
					NotificationTab curTab = (NotificationTab)notifications.getCarouselHost().getCurrentTabFragment();
					if (curTab != null) {
						isCanceled = true;
						curTab.cancelNotification();
					}
				}
				if (isCanceled && notifications.isLoaded && notifications.getCarouselHost().getTabCount() == 1)
					carousel.animate()
					.setDuration(300)
					.setInterpolator(new AccelerateInterpolator())
					.alpha(0.0f)
					.start();
				else
					carousel.animate()
					.setDuration(500)
					.setInterpolator(new DecelerateInterpolator())
					.translationY(0)
					.alpha(1.0f)
					.start();
				isDraggingVert = false;
				return true;
			}
		}
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (lastIntent != null) {
			lastIntent.putExtra("doRotate", true);
			createDialog(lastIntent);
			if (blurredBkg != null &&
				(bkgPortrait && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ||
				!bkgPortrait && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE))
				getWindow().setBackgroundDrawable(blurredBkg);
			else
				getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		} else getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		if (getClass() == DimmedActivity.class) {
			KeyguardManager kgMgr = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
			if (kgMgr.isKeyguardLocked()) finish();
		}
		startListen();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		stopListen();
		//PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		//if (pm.isScreenOn() && !isInLockscreen) finish();
	}
	
	@Override
	public void finish() {
		stopListen();
		super.finish();
		if (!isInLockscreen) overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super()
	}
}

package com.sensetoolbox.six.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.mods.XMain;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.KeyguardManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.robv.android.xposed.XposedBridge;

@SuppressLint({ "NewApi", "RtlHardcoded" })
public class FloatingSelector extends FrameLayout {
	Context mContext;
	Resources mResources;
	PackageManager mPackageManager;
	ActivityManager mActivityManager;
	float density;
	LinearLayout items;
	String pkgNameSel = null;
	List<RecentTaskInfo> recentTasks = null;
	boolean isShowing = false;
	//int materialTextColor;
	//int btn_borderless_material;
	//int accentColor;
	
	private int densify(int f) {
		return Math.round(density * f);
	}
	
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}
	
	@Override
	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent ev) {
		ViewParent prnt = getParent();
		if (prnt != null) prnt.requestDisallowInterceptTouchEvent(true);
		//XposedBridge.log("onTouchEvent: " + String.valueOf(ev.getActionMasked()));
		
		if (ev.getActionMasked() == MotionEvent.ACTION_CANCEL) {
			hide();
			return true;
		} else if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
			if (pkgNameSel != null) {
				boolean isInRecents = false;
				if (recentTasks != null)
				for (RecentTaskInfo task: recentTasks)
				if (task.baseIntent != null && task.baseIntent.getComponent().getPackageName().equals(pkgNameSel)) {
					isInRecents = true;
					if (task.id == -1) try {
						mContext.startActivity(task.baseIntent);
					} catch (Throwable t) {
						isInRecents = false;
					} else {
						mActivityManager.moveTaskToFront(task.persistentId, 0);
					}
					break;
				}
				if (!isInRecents) {
					Intent launchIntent = mPackageManager.getLaunchIntentForPackage(pkgNameSel);
					launchIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					mContext.startActivity(launchIntent);
				}
			}
			hide();
		} else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
			Rect r1 = new Rect();
			int rPointX = Math.round(ev.getRawX());
			int rPointY = Math.round(ev.getRawY());
			
			ArrayList<View> btns = Helpers.getChildViewsRecursive(items);
			for (View appBtn: btns) {
				if (!Button.class.isInstance(appBtn) || appBtn.getTag() == null) continue;
				Object[] tagObj = (Object[])appBtn.getTag();
				int tag = (Integer)tagObj[0];
				String pkgName = (String)tagObj[1];
				appBtn.getGlobalVisibleRect(r1);
				if (r1.contains(rPointX, rPointY)) {
					if (tag == 0) {
						pkgNameSel = pkgName;
						appBtn.setTag(new Object[] { 1, pkgName });
						appBtn.animate().cancel();
						appBtn.animate().setDuration(150).setStartDelay(0).z(densify(20)).scaleX(1.0f).scaleY(1.0f).start();
					}
				} else {
					if (tag == 1) {
						pkgNameSel = null;
						appBtn.setTag(new Object[] { 0, pkgName });
						appBtn.animate().cancel();
						appBtn.animate().setDuration(150).setStartDelay(0).z(5).scaleX(0.95f).scaleY(0.95f).start();
					}
				}
			}
		}
		return true;
	}
	
	public WindowManager.LayoutParams getLayoutParams() {
		WindowManager.LayoutParams alertParams = new WindowManager.LayoutParams(
				Math.round(mResources.getDisplayMetrics().widthPixels),
				Math.round(mResources.getDisplayMetrics().heightPixels),
				0, 0, 2024, // TYPE_NAVIGATION_BAR_PANEL
				WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
				WindowManager.LayoutParams.FLAG_SPLIT_TOUCH |
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
				PixelFormat.RGBA_8888);
		alertParams.gravity = Gravity.LEFT | Gravity.TOP;
		alertParams.horizontalMargin = 0;
		alertParams.verticalMargin = 0;
		alertParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
		alertParams.packageName = mContext.getPackageName();
		alertParams.setTitle("SearchPanel");
		return alertParams;
	}
	
	public FloatingSelector(Context context) {
		super(context);
		mContext = context;
		mResources = mContext.getResources();
		mPackageManager = mContext.getPackageManager();
		mActivityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
		density = mResources.getDisplayMetrics().density;
		//materialTextColor = mResources.getColor((mResources.getIdentifier("primary_text_default_material_dark", "color", "android")));
		//btn_borderless_material = mResources.getIdentifier("btn_borderless_material", "drawable", "android");
		
		//TypedValue typedValue = new TypedValue();
		//mContext.getTheme().resolveAttribute(mResources.getIdentifier("colorAccent", "attr", "android"), typedValue, true);
		//accentColor = typedValue.data;
		
		items = new LinearLayout(mContext);
		items.setAlpha(0f);
		items.setClipToPadding(false);
		items.setClipToOutline(false);
		updateOrientation();
		
		addView(items);
		setVisibility(View.INVISIBLE);
		setAlpha(0f);
		
		WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
		wm.addView(this, getLayoutParams());
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public void show() {
		if (mContext == null || mResources == null || mPackageManager == null) return;
		
		KeyguardManager km = (KeyguardManager)mContext.getSystemService(Context.KEYGUARD_SERVICE);
		if (km.isKeyguardLocked()) return;
		
		updateOrientation();
		items.removeAllViews();
		pkgNameSel = null;
		
		List<ActivityManager.RunningTaskInfo> currentTask = mActivityManager.getRunningTasks(1);
		String currAppPkgName = currentTask.get(0).topActivity.getPackageName();
		
		UsageStatsManager mUsageStatsManager = (UsageStatsManager)mContext.getSystemService("usagestats");
		long time = System.currentTimeMillis();
		List<UsageStats> stats = new ArrayList<UsageStats>();
		List<UsageStats> statsBest = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - 2 * 24 * 60 * 60 * 1000, time);
		XposedBridge.log("[ST] Interval best: " + String.valueOf(statsBest.size()));
		if (statsBest != null) stats.addAll(statsBest);
		
		if (stats.size() <= 2) {
			List<UsageStats> statsWeek = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, time - 7 * 24 * 60 * 60 * 1000, time);
			XposedBridge.log("[ST] Interval weekly: " + String.valueOf(statsWeek.size()));
			if (statsWeek != null) stats.addAll(statsWeek);
		}
		
		TreeMap<Long, String> mySortedMap = new TreeMap<Long, String>(Collections.reverseOrder());
		for (UsageStats usageStats: stats) {
			String pkgName = usageStats.getPackageName();
			if (!pkgName.equals(currAppPkgName) && !mySortedMap.containsValue(pkgName))
			mySortedMap.put(usageStats.getLastTimeUsed(), pkgName);
		}
		
		recentTasks = mActivityManager.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		
		long addTime = recentTasks.size();
		if (recentTasks != null)
		for (RecentTaskInfo task: recentTasks) {
			String pkgName = task.baseIntent.getComponent().getPackageName();
			if (task.baseIntent != null && !mySortedMap.containsValue(pkgName) && !pkgName.equals(currAppPkgName)) {
				mySortedMap.put(addTime, pkgName);
				addTime--;
			}
		}
		
		XposedBridge.log("[ST] Recently used apps: " + String.valueOf(mySortedMap.size()));
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.BOTTOM;
		lp.setMargins(0, 0, 0, densify(5));
		
		GradientDrawable gbkg = new GradientDrawable();
		gbkg.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
		gbkg.setShape(GradientDrawable.RECTANGLE);
		gbkg.setCornerRadius(densify(3));
		gbkg.setColor(0xff2E2E38);
		//gbkg.setStroke(2, 0xee222229);
		
		Intent intent_launcher = new Intent(Intent.ACTION_MAIN, null);
		intent_launcher.addCategory(Intent.CATEGORY_LAUNCHER);
		
		List<ResolveInfo> appsList = mPackageManager.queryIntentActivities(intent_launcher, 0);
		Map<String, ActivityInfo> pkgs = new HashMap<String, ActivityInfo>();
		for (ResolveInfo app: appsList) pkgs.put(app.activityInfo.applicationInfo.packageName, app.activityInfo);
		
		Intent intent_home = new Intent(Intent.ACTION_MAIN);
		intent_home.addCategory(Intent.CATEGORY_HOME);
		intent_home.addCategory(Intent.CATEGORY_DEFAULT);
		List<ResolveInfo> launcherList = mPackageManager.queryIntentActivities(intent_home, 0);
		ArrayList<String> launchers = new ArrayList<String>();
		for (ResolveInfo launcher: launcherList) launchers.add(launcher.activityInfo.applicationInfo.packageName);

		int rot = getDeviceRotation();
		boolean isLandscape = (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_270);
		int cnt = 0;
		int cntMax = 9;
		LinearLayout column1 = new LinearLayout(mContext);
		LinearLayout column2 = new LinearLayout(mContext);
		if (isLandscape) {
			items.setOrientation(LinearLayout.HORIZONTAL);
			
			LinearLayout.LayoutParams lpcL = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.47f);
			lpcL.gravity = Gravity.LEFT | Gravity.BOTTOM;
			LinearLayout.LayoutParams lpcR = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.47f);
			lpcR.gravity = Gravity.RIGHT | Gravity.TOP;
			column1.setOrientation(LinearLayout.VERTICAL);
			column1.setClipToPadding(false);
			column1.setClipToOutline(false);
			column2.setOrientation(LinearLayout.VERTICAL);
			column2.setClipToPadding(false);
			column2.setClipToOutline(false);
			
			if (rot == Surface.ROTATION_90) {
				items.setGravity(Gravity.RIGHT | Gravity.TOP);
				items.setPadding(0, 0, 0, 0);
				column2.setPadding(densify(20), densify(20), densify(10), densify(20));
				column2.setLayoutParams(lpcR);
				column1.setPadding(densify(10), densify(20), densify(20), densify(20));
				column1.setLayoutParams(lpcR);
				items.addView(column2);
				items.addView(column1);
			} else if (rot == Surface.ROTATION_270) {
				items.setGravity(Gravity.LEFT | Gravity.BOTTOM);
				items.setPadding(0, 0, 0, 0);
				column1.setPadding(densify(20), densify(20), densify(10), densify(20));
				column1.setLayoutParams(lpcL);
				column2.setPadding(densify(10), densify(20), densify(20), densify(20));
				column2.setLayoutParams(lpcL);
				items.addView(column1);
				items.addView(column2);
			}

			items.setWeightSum(1.0f);
			cntMax = 12;
		} else {
			items.setOrientation(LinearLayout.VERTICAL);
			if (rot == Surface.ROTATION_0)
				items.setPadding(densify(20), densify(20), densify(20), densify(20));
			else if (rot == Surface.ROTATION_180)
				items.setPadding(densify(20), densify(20), densify(20), densify(40));
		}
		
		boolean isEmpty = true;
		if (mySortedMap != null && !mySortedMap.isEmpty())
		for (Map.Entry<Long, String> entry: mySortedMap.entrySet()) {
			String pkgName = entry.getValue();
			ActivityInfo actInfo = pkgs.get(pkgName);
			
			if (actInfo == null || launchers.contains(pkgName)) continue;
			if (cnt >= cntMax) break;
			cnt++;
			Button appBtn = new Button(mContext);
			appBtn.setTag(new Object[] { 0, pkgName });
			appBtn.setFocusable(true);
			appBtn.setClickable(true);
			appBtn.setTextColor(Color.argb(255, 245, 245, 245));
			appBtn.setStateListAnimator(null);
			appBtn.setClipToOutline(false);
			appBtn.setZ(densify(5));
			appBtn.setScaleX(0.95f);
			appBtn.setScaleY(0.95f);
			appBtn.setBackground(gbkg);
			appBtn.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			appBtn.setPadding(densify(20), 0, densify(20), 0);
			appBtn.setAllCaps(false);
			
			String ai = null;
			try {
				ai = (String)actInfo.loadLabel(mPackageManager);
			} catch (Exception e) {}
			appBtn.setText(ai != null ? ai : pkgName);
			
			try {
				Drawable icon = actInfo.loadIcon(mPackageManager);
				if (icon == null) icon = mPackageManager.getApplicationIcon(pkgName);
				if (BitmapDrawable.class.isInstance(icon)) {
					int newIconSize = densify(30);
					Bitmap bmp = ((BitmapDrawable)icon).getBitmap();
					Matrix matrix = new Matrix();
					matrix.postScale(((float)newIconSize) / bmp.getWidth(), ((float)newIconSize) / bmp.getHeight());
					bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
					appBtn.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(mResources, bmp), null, null, null);
					appBtn.setCompoundDrawablePadding(densify(15));
				}
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
			
			LinearLayout itemsHolder = items;
			if (isLandscape)
			if (cnt <= 6)
				itemsHolder = column1;
			else
				itemsHolder = column2;
				appBtn.setLayoutParams(lp);

			switch (rot) {
				case Surface.ROTATION_0: itemsHolder.addView(appBtn, 0); break;
				case Surface.ROTATION_180: itemsHolder.addView(appBtn); break;
				case Surface.ROTATION_90: itemsHolder.addView(appBtn); break;
				case Surface.ROTATION_270: itemsHolder.addView(appBtn, 0); break;
			}
			isEmpty = false;
		}
			
		if (isEmpty) {
			TextView noApps = new TextView(mContext);
			noApps.setLayoutParams(lp);
			noApps.setElevation(densify(10));
			noApps.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
			noApps.setTextColor(Color.argb(225, 245, 245, 245));
			noApps.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f);
			noApps.setText(Helpers.xl10n(XModuleResources.createInstance(XMain.MODULE_PATH, null), R.string.controls_no_recents));
			noApps.setGravity(Gravity.CENTER);
			noApps.setAllCaps(true);
			noApps.setPadding(0, 0, 0, densify(40));
			items.addView(noApps);
		}
		setVisibility(View.VISIBLE);
		animate().setDuration(200L).setStartDelay(0).alpha(1.0F).start();
		items.animate().setDuration(250L).setStartDelay(50L).alpha(1.0F).translationX(0).translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		isShowing = true;
	}
	
	public int getDeviceRotation() {
		return ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
	}
	
	public void updateOrientation() {
		if (isAttachedToWindow())
		((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(this, getLayoutParams());
		
		GradientDrawable bkg = new GradientDrawable();
		bkg.setShape(GradientDrawable.RECTANGLE);
		bkg.setColors(new int[] { 0xfc141319, 0xee212026 });
		
		FrameLayout.LayoutParams lp;
		switch (getDeviceRotation()) {
			case Surface.ROTATION_0:
				lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
				items.setLayoutParams(lp);
				items.setTranslationX(0);
				items.setTranslationY(densify(50));
				bkg.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
				break;
			case Surface.ROTATION_180:
				lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
				items.setLayoutParams(lp);
				items.setTranslationX(0);
				items.setTranslationY(densify(-50));
				bkg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
				break;
			case Surface.ROTATION_90:
				lp = new FrameLayout.LayoutParams(Math.round(mResources.getDisplayMetrics().widthPixels), LayoutParams.MATCH_PARENT);
				lp.gravity = Gravity.RIGHT | Gravity.TOP;
				items.setLayoutParams(lp);
				items.setTranslationX(densify(50));
				items.setTranslationY(0);
				bkg.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
				break;
			case Surface.ROTATION_270:
				lp = new FrameLayout.LayoutParams(Math.round(mResources.getDisplayMetrics().widthPixels), LayoutParams.MATCH_PARENT);
				lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
				items.setLayoutParams(lp);
				items.setTranslationX(densify(-50));
				items.setTranslationY(0);
				bkg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
				break;
		}
		setBackground(bkg);
		items.requestLayout();
	}
	
	public void hide() {
		isShowing = false;
		
		ViewPropertyAnimator vpa = items.animate().setDuration(150L).setStartDelay(0).alpha(0.0F).setInterpolator(new DecelerateInterpolator(2));
		switch (getDeviceRotation()) {
			case Surface.ROTATION_0: vpa.translationY(densify(10)); break;
			case Surface.ROTATION_180: vpa.translationY(densify(-10)); break;
			case Surface.ROTATION_90: vpa.translationX(densify(10)); break;
			case Surface.ROTATION_270: vpa.translationX(densify(-10)); break;
		}
		vpa.start();
		
		animate().setDuration(150L).setStartDelay(0).alpha(0.0F).withEndAction(new Runnable() {
			public void run() {
				setVisibility(View.INVISIBLE);
			}
		}).start();
	}
}
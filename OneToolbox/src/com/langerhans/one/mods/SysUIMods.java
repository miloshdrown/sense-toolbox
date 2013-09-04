package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.langerhans.one.R;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SysUIMods {

	public static void execHook_InvisiBar(final InitPackageResourcesParam resparam, final String MODULE_PATH, final int transparency) {
//		resparam.res.hookLayout("com.android.systemui", "layout", "super_status_bar", new XC_LayoutInflated() {
//			@Override
//			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
//				View bg = liparam.view.findViewById(resparam.res.getIdentifier("status_bar", "id", "com.android.systemui"));
//				bg.getBackground().setAlpha(transparency);
//			}
//		});
		
		resparam.res.setReplacement("com.android.systemui", "drawable", "status_bar_background", new XResources.DrawableLoader() {
			@Override
			public Drawable newDrawable(XResources res, int id)	throws Throwable {
				XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
				if (modRes.getIdentifier("status_bar_background", "drawable", "com.langerhans.one") != 0) {
					Drawable sb = modRes.getDrawable(R.drawable.status_bar_background);
					sb.setAlpha(transparency);
					return sb;
				} else return null;
			}
		});
	}

	public static void execHook_MinorEQS(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.StatusBarFlag", lpparam.classLoader, "loadMinorQuickSetting", new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
	}

	public static void execHook_InvisiNotify(final InitPackageResourcesParam resparam, String MODULE_PATH, final int transparency) {
		resparam.res.hookLayout("com.android.systemui", "layout", "super_status_bar", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				View bg = liparam.view.findViewById(resparam.res.getIdentifier("notification_panel", "id", "com.android.systemui"));
				bg.getBackground().setAlpha(transparency);
			}
		});
	}

	public static void execHook_AospRecent(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.StatusBarFlag", lpparam.classLoader, "isHtcStyleRecentApp", new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(false);
			}
		});
	}
	
	public static void execHook_CenterClockLayout(final InitPackageResourcesParam resparam, String MODULE_PATH) {
		resparam.res.hookLayout("com.android.systemui", "layout", "super_status_bar", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				FrameLayout status_bar = (FrameLayout) liparam.view.findViewById(resparam.res.getIdentifier("status_bar", "id", "com.android.systemui"));
				TextView clock = (TextView) liparam.view.findViewById(resparam.res.getIdentifier("clock", "id", "com.android.systemui"));
				ImageView lights_out = (ImageView) liparam.view.findViewById(resparam.res.getIdentifier("notification_lights_out", "id", "com.android.systemui"));
				LinearLayout system_icon_area = (LinearLayout) liparam.view.findViewById(resparam.res.getIdentifier("system_icon_area", "id", "com.android.systemui"));
				
				if(status_bar != null && clock != null && lights_out != null && system_icon_area != null)
				{	
					clock.setGravity(Gravity.CENTER);
					LinearLayout clock_container = new LinearLayout(clock.getContext());
					clock_container.setOrientation(LinearLayout.HORIZONTAL);
					clock_container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					clock_container.setGravity(Gravity.CENTER);
					clock_container.setTag("centerClock");
					
					system_icon_area.removeView(clock);
					
					clock_container.addView(clock);
					
					status_bar.addView(clock_container, status_bar.indexOfChild(lights_out) - 1);
				}else
				{
					XposedBridge.log("[S5T] Center Clock Error: One or more layouts or views not found");
				}
			}
		});
		
	}
	
	public static void execHook_CenterClockAnimation(LoadPackageParam lpparam) {
		//Helper class to hold needed variables for later methods (because nested methods and final and blah blah... Couldn't think of a better solution)
		class Stuff{
			Object statusbar;
			Context ctx;
			Resources res;
			int animOut;
			int animIn;
			int animFade;
			LinearLayout clock_container;
		}
		final Stuff stuff = new Stuff();
		
		//Get what we need
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param)
			{
				stuff.statusbar = param.thisObject;
				stuff.ctx = (Context)getObjectField(stuff.statusbar, "mContext");
				stuff.res = stuff.ctx.getResources();
				stuff.animFade = stuff.res.getIdentifier("fade_in", "anim", "android");
				stuff.animIn = stuff.res.getIdentifier("push_down_in", "anim", "android");
				stuff.animOut = stuff.res.getIdentifier("push_up_out", "anim", "android");
				stuff.clock_container = (LinearLayout) ((FrameLayout) getObjectField(stuff.statusbar, "mStatusBarView")).findViewWithTag("centerClock");
			}
		});
		
		//And now the 3 Ticker hooks
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar$MyTicker", lpparam.classLoader, "tickerStarting", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param)
			{
				stuff.clock_container.setVisibility(View.GONE);
				Animation ani = (Animation) callMethod(stuff.statusbar, "loadAnim", stuff.animOut, null);
				stuff.clock_container.startAnimation(ani);
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar$MyTicker", lpparam.classLoader, "tickerDone", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param)
			{
				stuff.clock_container.setVisibility(View.VISIBLE);
				Animation ani = (Animation) callMethod(stuff.statusbar, "loadAnim", stuff.animIn, null);
				stuff.clock_container.startAnimation(ani);
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar$MyTicker", lpparam.classLoader, "tickerHalting", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param)
			{
				stuff.clock_container.setVisibility(View.VISIBLE);
				Animation ani = (Animation) callMethod(stuff.statusbar, "loadAnim", stuff.animFade, null);
				stuff.clock_container.startAnimation(ani);
			}
		});
	}

	// Pinch to clear all recent apps
	public static void execHook_RecentAppsClear(final LoadPackageParam lpparam) {
    	Object[] callbackObj = new Object[2];
        callbackObj[0] = "android.os.Bundle";
        callbackObj[1] = new RecentsModifier();

        findAndHookMethod("com.android.systemui.recent.RecentAppFxActivity", lpparam.classLoader, "onCreate", callbackObj);
       	findAndHookMethod("com.android.systemui.recent.RecentsGridView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new TouchListenerOnTouch());
       	findAndHookMethod("com.android.systemui.recent.RecentsGridView", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new TouchListenerOnTouchIntercept());		
	}
	
	private static GridView gridViewSelf;
	private static Object gridViewObject;
	private static Context gridViewContext;
	
	private static ActivityManager am;
	private static Field mTaskDescr;
	private static Field pTaskId;
	private static Field mFinished;
	private static Field td;
	private static Method onResume;
	private static Method removeTask;
	private static Method setDelPositionsList;
    
	private static ScaleGestureDetector mScaleDetector;
	private static GestureDetector mDetector;
	static boolean killedEmAll = false;
	
	// Get RecentAppFxActivity elements
	private static class RecentsModifier extends XC_MethodHook {
		private RecentsModifier() {}

		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
			if (onResume == null) onResume = XposedHelpers.findMethodExact(param.thisObject.getClass(), "onResume", new Class[0]);
			if (mTaskDescr == null) mTaskDescr = XposedHelpers.findField(param.thisObject.getClass(), "mRecentTaskDescriptions");
			if (mFinished == null) mFinished = XposedHelpers.findField(param.thisObject.getClass(), "mFinished");
			final GridView recentGridView = (GridView)XposedHelpers.findField(param.thisObject.getClass(), "mRecentGridView").get(param.thisObject);
			
			killedEmAll = false;
			
			gridViewObject = param.thisObject;
			gridViewContext = recentGridView.getContext();
			gridViewSelf = recentGridView;
		}
	}
	
	// Exterminate! © Daleks
	private static void terminateAll(Object termObj, Context termContext, GridView termGridView, ArrayList<?> taskDescriptionsArray, int animType) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
		if ((taskDescriptionsArray == null) || (taskDescriptionsArray.size() == 0))	{
			// Recent array is empty, resuming last activity
			sendOnResume(termObj);
			return;
		}
		int i = termGridView.getChildCount();
		int j = taskDescriptionsArray.size();
		int cnt = 0;

		// Go through all GridView items and get taskIds
		while (cnt < i) {
			View gridViewItem;
			gridViewItem = termGridView.getChildAt(cnt);
			if (gridViewItem != null) {
				td = gridViewItem.getTag().getClass().getDeclaredField("td");
				if (td != null) {
					Object gridViewItemTag = td.get(gridViewItem.getTag());
					if (gridViewItemTag != null) {
						pTaskId = gridViewItemTag.getClass().getDeclaredField("persistentTaskId");
						if (pTaskId != null) {
							
							// Recreate RecentAppFxActivity.handleSwipe() using hooked methods
							int m = j - taskDescriptionsArray.indexOf(gridViewItemTag) - 1;
							taskDescriptionsArray.remove(gridViewItemTag);
							if (m != 0)
							{
								if (setDelPositionsList == null)
								{
									Class<?> termGridViewClass = termGridView.getClass();
									Class<?>[] paramsTypesArray = new Class[1];
									paramsTypesArray[0] = Integer.TYPE;
									setDelPositionsList = termGridViewClass.getDeclaredMethod("setDelPositionsList", paramsTypesArray);
								}
								if (setDelPositionsList != null)
								{
									Method delPositionsListMethod = setDelPositionsList;
									Object[] paramsArray = new Object[1];
									paramsArray[0] = Integer.valueOf(m);
									delPositionsListMethod.invoke(termGridView, paramsArray);
								}
							}
						
							if (am == null || removeTask == null)
							{
								am = ((ActivityManager)termContext.getSystemService("activity"));
								Class<?> amClass = am.getClass();
								Class<?>[] paramsAMTypesArray = new Class[2];
								paramsAMTypesArray[0] = Integer.TYPE;
								paramsAMTypesArray[1] = Integer.TYPE;
								removeTask = amClass.getDeclaredMethod("removeTask", paramsAMTypesArray);
							}
							if ((pTaskId != null) && (removeTask != null) && (am != null))
							{
								Object[] paramsArray2 = new Object[2];
								paramsArray2[0] = pTaskId.get(gridViewItemTag);
								paramsArray2[1] = Integer.valueOf(1);
								removeTask.invoke(am, paramsArray2);
								pTaskId = null;
							}
							gridViewItem.startAnimation(terminateAnimation(termContext, i, cnt, animType, termObj));
						}
					}
				}
			}
			cnt++;
		}
	}
	
	// Shrink to center and fade out animations
	private static Animation terminateAnimation(Context paramContext, int i, int cnt, int animType, final Object paramObject) {
		Animation fadeOut = AnimationUtils.loadAnimation(paramContext, android.R.anim.fade_out);
		AnimationSet localAnimationSet = new AnimationSet(true);
		if (animType == 0)
		{
			fadeOut.setDuration(300l);
			fadeOut.setStartOffset((i - cnt) * 70);
			fadeOut.setInterpolator(AnimationUtils.loadInterpolator(paramContext, android.R.anim.linear_interpolator));
			
			TranslateAnimation drop = new TranslateAnimation(0.0F, 0.0F, 0.0F, 300.0f);
			drop.setDuration(500l);
			drop.setStartOffset((i - cnt) * 70);
			drop.setInterpolator(AnimationUtils.loadInterpolator(paramContext, android.R.anim.linear_interpolator));
			localAnimationSet.addAnimation(drop);
		} else {			
			fadeOut.setDuration(500l);
			fadeOut.setStartOffset(cnt * 50);
			fadeOut.setInterpolator(AnimationUtils.loadInterpolator(paramContext, android.R.anim.accelerate_interpolator));
			
			ScaleAnimation shrink = new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 
			shrink.setDuration(500l);
			shrink.setStartOffset(cnt * 50);
			shrink.setInterpolator(AnimationUtils.loadInterpolator(paramContext, android.R.anim.accelerate_decelerate_interpolator));
			localAnimationSet.addAnimation(shrink);
		}

		localAnimationSet.addAnimation(fadeOut);
		localAnimationSet.setFillAfter(true);
		if (cnt == i - 1)
		localAnimationSet.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationEnd(Animation paramAnonymousAnimation) {
				new Thread(new Runnable() {
				    @Override
				    public void run() {
				        try {
				            Thread.sleep(150);
				        } catch (InterruptedException e) {
				            e.printStackTrace();
				        }
				        sendOnResume(paramObject);
				    }
				}).start();				
			}
			public void onAnimationRepeat(Animation paramAnonymousAnimation) {}
			public void onAnimationStart(Animation paramAnonymousAnimation) {}
		});
		return localAnimationSet;
	}
	
	// Listener for scale gestures
	private static class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	    @Override
	    public boolean onScale(ScaleGestureDetector detector) {
	    	killedEmAll = true;
			try {
				terminateAll(gridViewObject, gridViewContext, gridViewSelf, (ArrayList<?>)mTaskDescr.get(gridViewObject), 1);
			} catch (Exception recentException) {
				XposedBridge.log("[S5T] TerminateAll Error: " + recentException.getMessage());
			}
	        return true;
	    }
	}

	// Listener for swipe gestures
	private static class SwipeListener extends GestureDetector.SimpleOnGestureListener {
		private final int SWIPE_MIN_DISTANCE = 120;
		private final int SWIPE_MIN_OFF_PATH = 250;
		private final int SWIPE_THRESHOLD_VELOCITY = 200;	
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) < SWIPE_MIN_OFF_PATH) return false;
				if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
			    	killedEmAll = true;
					try {
						terminateAll(gridViewObject, gridViewContext, gridViewSelf, (ArrayList<?>)mTaskDescr.get(gridViewObject), 0);
					} catch (Exception recentException) {
						XposedBridge.log("[S5T] TerminateAll Error: " + recentException.getMessage());
					}
				} 
			} catch (Exception e) {}
			return false;
		}
	}
	
	private static void initDetectors(MethodHookParam param) throws Throwable {
    	final GridView recentGridView = (GridView)param.thisObject;
    	if (mScaleDetector == null) mScaleDetector = new ScaleGestureDetector(recentGridView.getContext(), new ScaleListener());
    	if (mDetector == null) mDetector = new GestureDetector(recentGridView.getContext(), new SwipeListener());
	}
	
	// Detect second finger and cancel action if some app thumbnail was pressed
	private static class TouchListenerOnTouchIntercept extends XC_MethodHook {
		MotionEvent ev = null;
		
	    @Override
	    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
	    	initDetectors(param);
	    	ev = (MotionEvent)param.args[0];
	    	if (ev == null) return;
		    mScaleDetector.onTouchEvent(ev);
		    mDetector.onTouchEvent(ev);
		    
		    final int action = ev.getAction();
		    switch (action & MotionEvent.ACTION_MASK) {
		    case MotionEvent.ACTION_DOWN: {
		    }
		    case MotionEvent.ACTION_POINTER_DOWN: {
		    	if (ev.getPointerCount() == 2)
		    	try {
		    		param.setResult(Boolean.valueOf(true));
				} catch (Throwable thw) {
					param.setThrowable(thw);
				}
		    }		    
		    }		    
	    }
	}
	
	// Detect scale/swipe gestures
	private static class TouchListenerOnTouch extends XC_MethodHook {
	    MotionEvent ev = null;
	    
	    @Override
		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
		}
	    
	    @Override
	    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
	    	if (killedEmAll == true) return;
	    	
	    	initDetectors(param);
	    	ev = (MotionEvent)param.args[0];
	    	if (ev == null) return;
		    mScaleDetector.onTouchEvent(ev);
		    mDetector.onTouchEvent(ev);
		}
	}
	
	// Invoke RecentAppFxActivity.onResume() to close activity
	private static void sendOnResume(Object termObj) {
		try {
			if ((mFinished != null) && (onResume != null)) {
				mFinished.set(termObj, Boolean.valueOf(true));
				onResume.invoke(termObj);
			}
			return;
		} catch (Exception localException) {
			XposedBridge.log("[S5T] Error on onResume: " + localException.getMessage());
		}
	}
}

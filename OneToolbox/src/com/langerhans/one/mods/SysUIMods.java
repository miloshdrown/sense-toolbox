package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.langerhans.one.R;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
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
				Drawable sb = modRes.getDrawable(R.drawable.status_bar_background);
				sb.setAlpha(transparency);
				return sb;
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
	
	public static void execHook_CenterClock(final InitPackageResourcesParam resparam, String MODULE_PATH) {
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
	private static void terminateAll(Object termObj, Context termContext, GridView termGridView, ArrayList<?> taskDescriptionsArray) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
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
							gridViewItem.startAnimation(terminateAnimation(termContext, i, cnt, termObj));
						}
					}
				}
			}
			cnt++;
		}
	}
	
	// Shrink to center and fade out animations
	private static Animation terminateAnimation(Context paramContext, int i, int cnt, final Object paramObject) {
		Animation fadeOut = AnimationUtils.loadAnimation(paramContext, android.R.anim.fade_out);
		ScaleAnimation shrink = new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 
		fadeOut.setDuration(500L);
		shrink.setDuration(500L);
		fadeOut.setStartOffset(cnt * 50);
		fadeOut.setInterpolator(AnimationUtils.loadInterpolator(paramContext, android.R.anim.accelerate_interpolator));
		shrink.setStartOffset(cnt * 50);
		shrink.setInterpolator(AnimationUtils.loadInterpolator(paramContext, android.R.anim.accelerate_decelerate_interpolator));
		AnimationSet localAnimationSet = new AnimationSet(true);
		localAnimationSet.addAnimation(shrink);
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
				terminateAll(gridViewObject, gridViewContext, gridViewSelf, (ArrayList<?>)mTaskDescr.get(gridViewObject));
			} catch (Exception recentException) {
				XposedBridge.log("[S5T] TerminateAll Error: " + recentException.getMessage());
			}
	        return true;
	    }
	}
	
	private static void initScaleGestureDetector(MethodHookParam param) throws Throwable {
    	final GridView recentGridView = (GridView)param.thisObject;
    	if (mScaleDetector == null) mScaleDetector = new ScaleGestureDetector(recentGridView.getContext(), new ScaleListener());	    	
	}
	
	// Detect second finger and cancel action if some app thumbnail was pressed
	private static class TouchListenerOnTouchIntercept extends XC_MethodHook {
		MotionEvent ev = null;
		
	    @Override
	    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
	    	initScaleGestureDetector(param);
	    	ev = (MotionEvent)param.args[0];
	    	if (ev == null) return;
		    mScaleDetector.onTouchEvent(ev);
		    
		    final int action = ev.getAction();
		    switch (action & MotionEvent.ACTION_MASK) {
		    case MotionEvent.ACTION_DOWN: {
		    }
		    case MotionEvent.ACTION_POINTER_DOWN: {
		    	if (ev.getPointerCount() == 2)
		    	try {
		    		Object result = replaceHookedMethod(param);
		    		param.setResult(result);
				} catch (Throwable thw) {
					param.setThrowable(thw);
				}
		    }		    
		    }		    
	    }
	    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
    		return true;
    	}	    
	}
	
	// Detect any scale gestures
	private static class TouchListenerOnTouch extends XC_MethodHook {
	    MotionEvent ev = null;
	    
	    @Override
		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
		}
	    
	    @Override
	    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
	    	if (killedEmAll == true) return;
	    	
	    	initScaleGestureDetector(param);
	    	ev = (MotionEvent)param.args[0];
	    	if (ev == null) return;
		    mScaleDetector.onTouchEvent(ev);
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

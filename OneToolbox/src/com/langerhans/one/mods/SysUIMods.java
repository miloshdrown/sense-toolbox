package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils.TruncateAt;
import android.text.method.SingleLineTransformationMethod;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.htc.widget.HtcCheckBox;
import com.htc.widget.HtcCompoundButton;
import com.htc.widget.HtcPopupWindow;
import com.htc.widget.HtcCompoundButton.OnCheckedChangeListener;
import com.htc.widget.HtcSeekBar;
import com.langerhans.one.R;
import com.langerhans.one.utils.PopupAdapter;
import com.langerhans.one.utils.Version;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
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

	public static void execHook_MinorEQS(final LoadPackageParam lpparam, final boolean removeText) {
		//Enable mEQS
		findAndHookMethod("com.android.systemui.statusbar.StatusBarFlag", lpparam.classLoader, "loadMinorQuickSetting", new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
		
		//Recreate method to allow more tiles to be added to mEQS
		findAndHookMethod("com.android.systemui.statusbar.phone.QuickSettings", lpparam.classLoader, "refreshQuickSettingConfig", int[].class, new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param)	throws Throwable {
				int[] QS_DEFAULT = (int[]) getStaticObjectField(param.thisObject.getClass(), "QS_DEFAULT");
				String[] QS_MAPPING = (String[]) getStaticObjectField(param.thisObject.getClass(), "QS_MAPPING");
				ArrayList<String> qsContent = new ArrayList<String>();
				ArrayList<String> qsContent2 = new ArrayList<String>();
				int[] paramArgs;
				Class<?> CustomizationUtil = findClass("com.android.systemui.CustomizationUtil", lpparam.classLoader);
				Object hcr = callStaticMethod(CustomizationUtil, "getReader");
				if(param.args[0] == null)
				{
					if(hcr == null)
						paramArgs = QS_DEFAULT;
					else
					{
						paramArgs = (int[]) callMethod(hcr, "readIntArray", "quick_setting_items", QS_DEFAULT);
					}
				}else
				{
					 paramArgs = (int[]) param.args[0];
				}
				
				if (paramArgs == null || paramArgs.length == 0)
					paramArgs = QS_DEFAULT;
				int i = QS_MAPPING.length;
		        int j = 0;
		        for (int k = paramArgs.length; j < k; j++)
		        {
		            int i1 = paramArgs[j];
		            if (i1 >= 0 && i1 < i)
		                qsContent.add(QS_MAPPING[i1]);
		        }
		        qsContent2 = new ArrayList<String>();
		        int l = 0;
		        do
		        {
		            if (l >= qsContent.size())
		                break;
		            if (!((String)qsContent.get(l)).equals("user_card"))
		                qsContent2.add(qsContent.get(l));
//		            if (qsContent2.size() == 5)
//		                break;
		            l++;
		        } while (true);
		        setObjectField(param.thisObject, "qsContent", qsContent);
		        setObjectField(param.thisObject, "qsContent2", qsContent2);		        
				return null;
			}
		});
		
		//Redraw the tile view because we have added or removed something... Sense 5.5 only.
		if(XMain.senseVersion.compareTo(new Version("5.5")) >= 0)
		{
			findAndHookMethod("com.android.systemui.statusbar.phone.QuickSettings", lpparam.classLoader, "repositionQuickSettingTile", ViewGroup.class, ArrayList.class, boolean.class, new XC_MethodHook() {
				@Override
	    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					ViewGroup qsContainer = (ViewGroup) getObjectField(param.thisObject, "mContainerView2");
					if(!param.args[0].equals(qsContainer))
						return;
					
		        	WindowManager wm = (WindowManager) qsContainer.getContext().getSystemService(Context.WINDOW_SERVICE);
					Display display = wm.getDefaultDisplay();
					Point displaySize = new Point();
					display.getSize(displaySize);
					int displayWidth = displaySize.x;
		        	for(int k = 0; k < qsContainer.getChildCount(); k++)
					{
						LinearLayout tmp = (LinearLayout) qsContainer.getChildAt(k);
						LinearLayout.LayoutParams tmpParams = (LinearLayout.LayoutParams) tmp.getLayoutParams();
						tmpParams.width = (int) Math.floor(displayWidth / 5);
						tmp.setLayoutParams(tmpParams);
						if(removeText)
						{
							tmp.findViewById(tmp.getResources().getIdentifier("quick_setting_text", "id", "com.android.systemui")).setVisibility(View.GONE);;
							ImageView qsImg = (ImageView) tmp.findViewById(tmp.getResources().getIdentifier("quick_setting_image", "id", "com.android.systemui"));
							qsImg.setPadding(0, 0, 0, 20);
						}
					}
		        	qsContainer.invalidate();
				}
			});
		}
		
		//Makes them scrolling. Showing 5 at once.
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				FrameLayout mStatusBarWindow = (FrameLayout) getObjectField(param.thisObject, "mStatusBarWindow");
				if (mStatusBarWindow != null)
				{
					LinearLayout qsContainer = (LinearLayout) mStatusBarWindow.findViewById(mStatusBarWindow.getResources().getIdentifier("quick_settings_minor_container", "id", "com.android.systemui"));
					LinearLayout notificationContainer = (LinearLayout) mStatusBarWindow.findViewById(mStatusBarWindow.getResources().getIdentifier("notification_container", "id", "com.android.systemui"));
					if (qsContainer != null && notificationContainer != null)
					{
						HorizontalScrollView qsScroll = new HorizontalScrollView(mStatusBarWindow.getContext());
						qsScroll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
						qsScroll.setFillViewport(true);
						qsScroll.setHorizontalFadingEdgeEnabled(true);
						qsScroll.setHorizontalScrollBarEnabled(false);
						
						WindowManager wm = (WindowManager) mStatusBarWindow.getContext().getSystemService(Context.WINDOW_SERVICE);
						Display display = wm.getDefaultDisplay();
						Point displaySize = new Point();
						display.getSize(displaySize);
						int displayWidth = displaySize.x;
						
						for(int i = 0; i < qsContainer.getChildCount(); i++)
						{
							LinearLayout tmp = (LinearLayout) qsContainer.getChildAt(i);
							LinearLayout.LayoutParams tmpParams = (LinearLayout.LayoutParams) tmp.getLayoutParams();
							tmpParams.width = (int) Math.floor(displayWidth / 5);
							tmp.setLayoutParams(tmpParams);
							if(removeText)
							{
								tmp.findViewById(tmp.getResources().getIdentifier("quick_setting_text", "id", "com.android.systemui")).setVisibility(View.GONE);;
								ImageView qsImg = (ImageView) tmp.findViewById(tmp.getResources().getIdentifier("quick_setting_image", "id", "com.android.systemui"));
								qsImg.setPadding(0, 0, 0, 20);
							}
						}

						notificationContainer.removeView(qsContainer);
						qsScroll.addView(qsContainer);
						notificationContainer.addView(qsScroll, 0);
					}
				}
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
		resparam.res.hookLayout("com.android.systemui", "layout", "super_status_bar", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				
			}
		});
	}

	public static void execHook_AospRecent(final LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.StatusBarFlag", lpparam.classLoader, "isHtcStyleRecentApp", new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(false);
			}
		});
		
		//Fix for FC on Sense 5.5
		if(XMain.senseVersion.compareTo(new Version("5.5")) >= 0)
		{
			findAndHookMethod("com.android.systemui.recent.RecentsActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
				@Override
	    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					BroadcastReceiver mIntentReceiver = (BroadcastReceiver) getObjectField(param.thisObject, "mIntentReceiver");
					IntentFilter mIntentFilter = (IntentFilter) getObjectField(param.thisObject, "mIntentFilter");
					if(mIntentReceiver != null && mIntentFilter != null) {
						Activity thisActivity = (Activity) param.thisObject;
						thisActivity.registerReceiver(mIntentReceiver, mIntentFilter);
					}
				}
			});
		}
	}
	
	public static void execHook_CenterClockLayout(final InitPackageResourcesParam resparam, String MODULE_PATH) {
		resparam.res.hookLayout("com.android.systemui", "layout", "super_status_bar", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				FrameLayout statusBar = (FrameLayout) liparam.view.findViewById(resparam.res.getIdentifier("status_bar", "id", "com.android.systemui"));
				TextView clock = (TextView) liparam.view.findViewById(resparam.res.getIdentifier("clock", "id", "com.android.systemui"));
				LinearLayout systemIconArea = (LinearLayout) liparam.view.findViewById(resparam.res.getIdentifier("system_icon_area", "id", "com.android.systemui"));
				LinearLayout statusBarContents = (LinearLayout) liparam.view.findViewById(resparam.res.getIdentifier("status_bar_contents", "id", "com.android.systemui"));
				
				if(statusBar != null && clock != null && systemIconArea != null && statusBarContents != null)
				{	
					clock.setGravity(Gravity.CENTER);
					clock.setPadding(0, 0, 0, 0);
					LinearLayout clockContainer = new LinearLayout(clock.getContext());
					clockContainer.setOrientation(LinearLayout.HORIZONTAL);
					clockContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					clockContainer.setGravity(Gravity.CENTER);
					clockContainer.setTag("centerClock");
					clockContainer.setPadding(0, 0, 0, 0);
					
					systemIconArea.removeView(clock);
					
					clockContainer.addView(clock);
					
					statusBar.addView(clockContainer);
					
					LinearLayout fillView = new LinearLayout(clock.getContext());
					fillView.setOrientation(LinearLayout.HORIZONTAL);
					fillView.setLayoutParams(new LayoutParams(500, LayoutParams.MATCH_PARENT));
					fillView.setId(0x999999);
					statusBarContents.addView(fillView, statusBarContents.indexOfChild(systemIconArea));
					
				}else
				{
					XposedBridge.log("[S5T] Center Clock Error: One or more layouts or views not found");
				}
			}
		});
	}
	
	/**
	 * Updates the fillView to make the notification icons move to the left
	 * @param viewToUpdate 0 = iconMerger; 1 = signalClusterView
	 * @param param params of the hooked method
	 */
	private static void updateFillView(int viewToUpdate, MethodHookParam param)
	{
		//iconMerger needs to be retrieved, where we can use the current object for signal cluster
		LinearLayout startView = (LinearLayout) ((viewToUpdate == 0) ? getObjectField(param.thisObject, "mNotificationIcons") : param.thisObject);
		if (startView != null)
		{
			//signal cluster is one step deeper in the view hierarchy...
			FrameLayout statusBar = (viewToUpdate == 0) ? ((FrameLayout)startView.getParent().getParent().getParent()) : ((FrameLayout)startView.getParent().getParent().getParent().getParent());
			if(statusBar != null)
			{
				LinearLayout systemIconArea = (LinearLayout) statusBar.findViewById(statusBar.getResources().getIdentifier("system_icon_area", "id", "com.android.systemui"));
				if(systemIconArea != null)
				{
					LinearLayout fillView = (LinearLayout) statusBar.findViewById(0x999999);
					if(fillView != null)
					{
						TextView clock = (TextView) statusBar.findViewById(statusBar.getResources().getIdentifier("clock", "id", "com.android.systemui"));
						if(clock != null)
						{
							int systemIconAreaLeft = systemIconArea.getLeft();
							int clockContainerLeft = clock.getLeft();
							LayoutParams fillViewParams = fillView.getLayoutParams();
							fillViewParams.width = systemIconAreaLeft - clockContainerLeft;
							fillView.setLayoutParams(fillViewParams);
							fillView.invalidate();
						}else XposedBridge.log("clockContainer = null");
					}else XposedBridge.log("fillView = null");
				}else XposedBridge.log("systemIconArea = null");
			}else XposedBridge.log("statusBar = null");
		}else XposedBridge.log("startView = null");
	}
	
	public static void execHook_CenterClockAnimation(LoadPackageParam lpparam) {
		//Listen for icon changes and update the width of the fill view
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "updateNotificationIcons", new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
			{
				updateFillView(0, param);
			}
		});
		findAndHookMethod("com.android.systemui.statusbar.HtcGenericSignalClusterView", lpparam.classLoader, "apply", new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
			{
				updateFillView(1, param);
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "updateResources", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param)
			{
				updateFillView(0, param);
			}
		});
		
		try {
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "refreshAllIconsForLayout", LinearLayout.class, new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param)
				{
					updateFillView(0, param);
				}
			});
		} catch (Throwable t) {
			// TODO: handle exception
			// ^YOU DON'T TELL ME WHAT I HAVE TO DO ECLIPSE!
		}
		
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
	
	public static void execHook_removeAMPM(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param)
			{
				String newTime = ((TextView)param.thisObject).getText().toString().replaceAll("(?i)am|pm", "").trim();
				((TextView)param.thisObject).setText(newTime);
			}
		});
	}
	
	public static void execHook_ClockRemove(LoadPackageParam lpparam) {
		//Make clock invisible
		findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param)
			{
				((TextView)param.thisObject).setVisibility(View.GONE);
			}
		});
		//Prevent clock to be shown after phone unlock
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "showClock", boolean.class, new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
			{
				if((Boolean) param.args[0])
					param.setResult(null);
			}
		});
	}
	
	public static void execHook_BrightnessSlider(LoadPackageParam lpparam, final String MODULE_PATH) {
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param)
			{
				final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);
				
				FrameLayout mStatusBarWindow = (FrameLayout) getObjectField(param.thisObject, "mStatusBarWindow"); 
				LinearLayout panel = (LinearLayout) mStatusBarWindow.findViewById(mStatusBarWindow.getResources().getIdentifier("panel", "id", "com.android.systemui"));

				//Inflate the slider layout
				LayoutInflater inflater = LayoutInflater.from(panel.getContext());
				LinearLayout sliderConatiner = new LinearLayout(panel.getContext());
				sliderConatiner = (LinearLayout) inflater.inflate(modRes.getLayout(R.layout.brightness_slider), panel, false);
				sliderConatiner.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						//Just capture the event so the status bar won't get it.
						return true;
					}
				});
				
				TextView autoText = (TextView) sliderConatiner.findViewById(R.id.autoText);
				final HtcCheckBox cb = (HtcCheckBox) sliderConatiner.findViewById(R.id.autoCheckBox);
				autoText.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						cb.toggle(); //Make it easier to toggle the checkbox. Way harder to hit it without that...
					}
				});
				autoText.setText(modRes.getText(R.string.systemui_brightslide_auto));
				
				panel.addView(sliderConatiner, 1);
				
				final HtcSeekBar seekBar = (HtcSeekBar) mStatusBarWindow.findViewById(R.id.sliderSeekBar);
				final HtcCheckBox checkBox = (HtcCheckBox) mStatusBarWindow.findViewById(R.id.autoCheckBox);
				final ContentResolver cr = mStatusBarWindow.getContext().getContentResolver();
				
				try {
					seekBar.setProgress(android.provider.Settings.System.getInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS));
					checkBox.setChecked(android.provider.Settings.System.getInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == 0 ? false : true);
					seekBar.setEnabled(android.provider.Settings.System.getInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == 0 ? true : false);
					seekBar.setDisplayMode(1); //Seekbar black BG
				} catch (SettingNotFoundException e) {
					//No brightness setting?
				}
				seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						//Don't care
					}
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						//Don't care
					}
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						android.provider.Settings.System.putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS, progress);
					}
				});
				
				checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(HtcCompoundButton arg0, boolean arg1) {
						try{
							if(arg1)
							{
								android.provider.Settings.System.putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
								seekBar.setProgress(android.provider.Settings.System.getInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS));
								seekBar.setEnabled(false);
							}else
							{
								android.provider.Settings.System.putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
								seekBar.setProgress(android.provider.Settings.System.getInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS));
								seekBar.setEnabled(true);
							}
						} catch (SettingNotFoundException e) {
							//No brightness setting?
						}
					}
				});
				
				SettingsObserver so = new SettingsObserver(new Handler());
				so.setup(checkBox, cr);
				cr.registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, so);
			}
		});
	}
	
	private static ConnectivityManager connectivityManager = null;
	private static TextView dataRateVal = null;
	private static TextView dataRateUnits = null;
	private static Handler mHandler = null;
	private static Runnable mRunnable = null;
	private static long bytesTotal = 0;
	
	@SuppressLint("DefaultLocale")
	private static ArrayList<String> humanReadableByteCount(long bytes) {
		ArrayList<String> out = new ArrayList<String>();
	    if (bytes < 1024) {
	    	out.add(String.valueOf(bytes));
		    out.add("B/s");
	    	return out;
	    }
	    int exp = (int) (Math.log(bytes) / Math.log(1024));
	    char pre = "KMGTPE".charAt(exp-1);
	    out.add(String.format("%.1f", bytes / Math.pow(1024, exp)));
	    out.add(String.format("%sB/s", pre));
	    return out;
	}
	
	public static void execHook_DataRateStatus(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param) {
				if (dataRateVal != null && dataRateUnits != null) return;
				
				Context mContext = (Context)getObjectField(param.thisObject, "mContext");
				connectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
				
				FrameLayout mStatusBarView = (FrameLayout)getObjectField(param.thisObject, "mStatusBarView");
				LinearLayout systemIconArea = (LinearLayout)mStatusBarView.findViewById(mStatusBarView.getResources().getIdentifier("system_icon_area", "id", "com.android.systemui"));
				
				LinearLayout textFrame = new LinearLayout(mContext);
				textFrame.setOrientation(LinearLayout.VERTICAL);
				textFrame.setGravity(Gravity.CENTER_HORIZONTAL);
				textFrame.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
				
				dataRateVal = new TextView(mContext);
				dataRateVal.setVisibility(8);
				dataRateVal.setTransformationMethod(SingleLineTransformationMethod.getInstance());
				dataRateVal.setEllipsize(null);
				dataRateVal.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
				dataRateVal.setTextColor(Color.WHITE);
				dataRateVal.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				dataRateVal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13.0f);
				dataRateVal.setIncludeFontPadding(false);
				dataRateVal.setPadding(0, 1, 5, 0);
				dataRateVal.setLineSpacing(0, 0.9f);
				//dataRateVal.setTypeface(null, Typeface.BOLD);
				
				dataRateUnits = new TextView(mContext);
				dataRateUnits.setVisibility(8);
				dataRateUnits.setTransformationMethod(SingleLineTransformationMethod.getInstance());
				dataRateUnits.setEllipsize(null);
				dataRateUnits.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
				dataRateUnits.setTextColor(Color.WHITE);
				dataRateUnits.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				dataRateUnits.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10.0f);
				dataRateUnits.setIncludeFontPadding(false);
				dataRateUnits.setPadding(0, 0, 5, 1);
				dataRateUnits.setScaleY(0.9f);
				
				textFrame.addView(dataRateVal, 0);
				textFrame.addView(dataRateUnits, 1);
				systemIconArea.addView(textFrame, 0);

				mHandler = new Handler();
				mRunnable = new Runnable() {
		            public void run() {
						try {
							boolean isConnected = false;
							if (connectivityManager != null && dataRateVal != null && dataRateUnits != null) {
								NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
								if (activeNetworkInfo != null)
								if (activeNetworkInfo.isConnected()) isConnected = true;
								
								if (isConnected) {
									long rxBytes = TrafficStats.getTotalRxBytes();
									long txBytes = TrafficStats.getTotalTxBytes();
									long newBytes = 0;
									if (rxBytes != -1L && txBytes != -1L) newBytes = rxBytes + txBytes;						
									long newBytesFixed = newBytes - bytesTotal;
									if (newBytesFixed < 0 || bytesTotal == 0) newBytesFixed = 0;
									long speed = Math.round(newBytesFixed/3);
									bytesTotal = newBytes;
									ArrayList<String> spd = humanReadableByteCount(speed);
									dataRateVal.setText(spd.get(0));
									dataRateUnits.setText(spd.get(1));
									if (speed == 0) {
										dataRateVal.setAlpha(0.3f);
										dataRateUnits.setAlpha(0.3f);
									} else {
										dataRateVal.setAlpha(1.0f);
										dataRateUnits.setAlpha(1.0f);
									}
									dataRateVal.setVisibility(0);
									dataRateUnits.setVisibility(0);
								} else {
									dataRateVal.setVisibility(8);
									dataRateUnits.setVisibility(8);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
							
						if (mHandler != null)
						mHandler.postDelayed(mRunnable, 2000L);
		            }
		        };
		        mHandler.post(mRunnable);
			}
		});
	}
	
	//Need this to listen for settings changes
	protected static class SettingsObserver extends ContentObserver {	
		private HtcCheckBox cb = null;
		private ContentResolver cr;
		public SettingsObserver(Handler handler) {
			super(handler);			
		}
		public void setup(HtcCheckBox cb, ContentResolver cr) {
			this.cb = cb;
			this.cr = cr;
		}
		@Override
		public void onChange(boolean selfChange) {
			this.onChange(selfChange, null);
		}	
		@Override
		public void onChange(boolean selfChange, Uri uri) {
			try {
				this.cb.setChecked(android.provider.Settings.System.getInt(this.cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == 0 ? false : true);
			} catch (SettingNotFoundException e) {
				//No brightness setting?
			}
		}		
	}
	/*
	public static void execHook_DisableEQS(final InitPackageResourcesParam resparam) {
		resparam.res.setReplacement("com.android.systemui", "bool", "config_hasSettingsPanel", false);
		resparam.res.setReplacement("com.android.systemui", "bool", "config_hasFlipSettingsPanel", false);
	}
	*/
	public static void execHook_DisableEQS(final LoadPackageParam lpparam) {
		XposedBridge.hookAllConstructors(findClass("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader), new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				View.OnClickListener newButtonOnClick = new View.OnClickListener() {
		            public void onClick(View view) {
		            	XposedHelpers.callMethod(param.thisObject, "startActivityDismissingKeyguard", new Intent("android.settings.SETTINGS"), true);
	                    return;
		            }
		        };
		        XposedHelpers.findField(param.thisObject.getClass(), "mSettingsButtonListener").set(param.thisObject, newButtonOnClick);
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				ImageView mSettingsButton = (ImageView)XposedHelpers.findField(param.thisObject.getClass(), "mSettingsButton").get(param.thisObject);
				XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
		        mSettingsButton.setImageDrawable(modRes.getDrawable(R.drawable.ic_sysbar_quicksettings));				
			}
		});
	}
	
	//hEQS LongClickListeners
	public static void execHook_hEQSLongClick(final LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.QuickSettingsTileView", lpparam.classLoader, "onAttachedToWindow", new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				final LinearLayout thisTile = (LinearLayout) param.thisObject;
				thisTile.setLongClickable(true);
				thisTile.setOnLongClickListener(new OnLongClickListener() {
					@Override
					@SuppressLint("DefaultLocale")
					public boolean onLongClick(View v) {
						String clickedTile = (String) getObjectField(thisTile, "tileLabel");
						if (!clickedTile.equals("") && !(clickedTile == null))
						{
							String intentPkg = "";
							String intentClass = "";
							Intent settingIntent = null;
							if (clickedTile.equals("apn")) {intentPkg = "com.android.settings"; intentClass = "com.android.settings.CdmaApnSettings";}
							if (clickedTile.equals("auto_sync")) {intentClass = "android.settings.SYNC_SETTINGS";}
							if (clickedTile.equals("bluetooth")) {intentClass = "android.settings.BLUETOOTH_SETTINGS";}
							if (clickedTile.equals("brightness")) {settingIntent = new Intent("android.settings.DISPLAY_SETTINGS"); settingIntent.putExtra(":android:show_preference", "brightness");}
							if (clickedTile.equals("do_not_disturb")) {intentClass = "com.htc.settings.DND_SETTINGS";}
							if (clickedTile.equals("gps")) {intentClass = "android.settings.LOCATION_SOURCE_SETTINGS";}
							if (clickedTile.equals("mobile_data")) {intentPkg = "com.android.phone"; intentClass = "com.android.phone.MobileNetworkSettings";}
							if (clickedTile.equals("power_saver")) {intentPkg = "com.htc.htcpowermanager"; intentClass = "com.htc.htcpowermanager.powersaver.PowerSaverActivity";}
							if (clickedTile.equals("screenshot")) {
								int mBucketId = -1;
								File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Screenshots");
								if (file != null)
								{
		                        	mBucketId = file.getAbsolutePath().toLowerCase().hashCode();
								}
								settingIntent = new Intent("com.htc.album.action.VIEW_FOLDER_IN_THUMBNAIL");
								settingIntent.putExtra("folder_type", (new StringBuilder()).append("collection_regular_bucket ").append(mBucketId).append(" Screenshots").toString());
								settingIntent.putExtra("entry_from", "Screenshots");
								settingIntent.setDataAndType(null, "image/*");
								settingIntent.setFlags(0x14000000);
							}
							if (clickedTile.equals("wifi")) {intentClass = "android.settings.WIFI_SETTINGS";}
							if (clickedTile.equals("wifi_hotspot")) {intentPkg = "com.htc.WifiRouter"; intentClass = "com.htc.WifiRouter.WifiRouter";}
							Object viewTag = thisTile.getTag();
							if (viewTag != null)
							{
								if (!intentPkg.equals(""))
									callMethod(viewTag, "startSettingsActivity", intentPkg, intentClass);
								else if (!(settingIntent == null))
									callMethod(viewTag, "startSettingsActivity", settingIntent);
								else 
									callMethod(viewTag, "startSettingsActivity", intentClass);
							}
						}
						return true;
					}
				});
			}
		});
	}
	
	// Pinch to clear all recent apps
	public static void execHook_RecentAppsInit(final LoadPackageParam lpparam) {
        findAndHookMethod("com.android.systemui.recent.RecentAppFxActivity", lpparam.classLoader, "onCreate", "android.os.Bundle", new XC_MethodHook(){
    		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
    			final GridView recentGridView = (GridView)XposedHelpers.findField(param.thisObject.getClass(), "mRecentGridView").get(param.thisObject);
    			
    			killedEmAll = false;
    			
    			gridViewObject = param.thisObject;
    			gridViewContext = recentGridView.getContext();
    			gridViewSelf = recentGridView;
    		}
    	});
	}
	
	public static void execHook_RecentAppsClearTouch(final LoadPackageParam lpparam) {
       	findAndHookMethod("com.android.systemui.recent.RecentsGridView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new TouchListenerOnTouch());
       	findAndHookMethod("com.android.systemui.recent.RecentsGridView", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new TouchListenerOnTouchIntercept());		
	}
	
	private static GridView gridViewSelf;
	private static Object gridViewObject;
	private static Context gridViewContext;
	private static ActivityManager am;
	
	private static ScaleGestureDetector mScaleDetector;
	private static GestureDetector mDetector;
	static boolean killedEmAll = false;
	
	// Exterminate! � Daleks
	private static void terminateAll(int animType) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
		terminateAll(animType, null);
	}
	
	private static void terminateAll(int animType, final ViewGroup currApp) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
		ArrayList<?> taskDescriptionsArray = (ArrayList<?>)XposedHelpers.getObjectField(gridViewObject, "mRecentTaskDescriptions");
		if ((taskDescriptionsArray == null) || (taskDescriptionsArray.size() == 0))	{
			// Recent array is empty, resuming last activity
			sendOnResume();
			return;
		}
		int i = gridViewSelf.getChildCount();
		int j = taskDescriptionsArray.size();
		int cnt = 0;

		// Go through all GridView items and get taskIds
		while (cnt < i) {
			View gridViewItem;
			gridViewItem = gridViewSelf.getChildAt(cnt);
			if (gridViewItem != null && !gridViewItem.equals(currApp)) {
				Object gridViewItemTag = XposedHelpers.getObjectField(gridViewItem.getTag(), "td");
				if (gridViewItemTag != null) {
					// Recreate RecentAppFxActivity.handleSwipe() using hooked methods
					int m = j - taskDescriptionsArray.indexOf(gridViewItemTag) - 1;
					taskDescriptionsArray.remove(gridViewItemTag);
					if (m != 0) try {
						XposedHelpers.callMethod(gridViewSelf, "setDelPositionsList", Integer.valueOf(m));
					} catch (Exception e) {}
				
					if (am == null)
					am = ((ActivityManager)gridViewContext.getSystemService("activity"));
					if (am != null) 
					XposedHelpers.callMethod(am, "removeTask", XposedHelpers.getIntField(gridViewItemTag, "persistentTaskId"), Integer.valueOf(1));
					
					gridViewItem.startAnimation(terminateAnimation(i, cnt, animType, currApp));
				}
			}
			cnt++;
		}
		if (currApp != null) {
			Handler handler = (Handler)XposedHelpers.getObjectField(gridViewObject, "handler");
			Runnable runnable = new Runnable() {
				public void run() {
					XposedHelpers.callMethod(gridViewObject, "handleOnClick", currApp);
				}
			};
			handler.postDelayed(runnable, 550l + (i - 1) * 50l);
		}
	}
	
	// Shrink to center and fade out animations
	private static Animation terminateAnimation(final int i, int cnt, int animType, final ViewGroup currApp) {
		if (gridViewContext == null) return null;
		Animation fadeOut = AnimationUtils.loadAnimation(gridViewContext, android.R.anim.fade_out);
		AnimationSet localAnimationSet = new AnimationSet(true);
		if (animType == 0)
		{
			fadeOut.setDuration(300l);
			fadeOut.setStartOffset((i - cnt) * 70);
			fadeOut.setInterpolator(AnimationUtils.loadInterpolator(gridViewContext, android.R.anim.linear_interpolator));
			
			TranslateAnimation drop = new TranslateAnimation(0.0F, 0.0F, 0.0F, 300.0f);
			drop.setDuration(500l);
			drop.setStartOffset((i - cnt) * 70);
			drop.setInterpolator(AnimationUtils.loadInterpolator(gridViewContext, android.R.anim.linear_interpolator));
			localAnimationSet.addAnimation(drop);
		} else {			
			fadeOut.setDuration(500l);
			fadeOut.setStartOffset(cnt * 50);
			fadeOut.setInterpolator(AnimationUtils.loadInterpolator(gridViewContext, android.R.anim.accelerate_interpolator));
			
			ScaleAnimation shrink = new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 
			shrink.setDuration(500l);
			shrink.setStartOffset(cnt * 50);
			shrink.setInterpolator(AnimationUtils.loadInterpolator(gridViewContext, android.R.anim.accelerate_decelerate_interpolator));
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
						try { if (i > 3) Thread.sleep((i + 1) * 15); } catch (Exception e) {}
						if (currApp == null) sendOnResume();
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
				terminateAll(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
	        return true;
	    }
	}

	// Listener for swipe gestures
	private static class SwipeListener extends GestureDetector.SimpleOnGestureListener {
		// For HTC One
		private int SWIPE_MIN_DISTANCE = 120;
		private int SWIPE_MAX_OFF_PATH = 250;
		private int SWIPE_THRESHOLD_VELOCITY = 200;
		
		final Context helperContext;
		
		public SwipeListener(Context context) {
			helperContext = context;
			float density = helperContext.getResources().getDisplayMetrics().density;
			SWIPE_MIN_DISTANCE = Math.round(40 * density);
			SWIPE_MAX_OFF_PATH = Math.round(85 * density);
			SWIPE_THRESHOLD_VELOCITY = Math.round(66 * density);
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH) return false;
				if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
			    	killedEmAll = true;
					terminateAll(0);
				} 
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}
	
	private static void initDetectors(MethodHookParam param) throws Throwable {
    	final GridView recentGridView = (GridView)param.thisObject;
    	if (mScaleDetector == null) mScaleDetector = new ScaleGestureDetector(recentGridView.getContext(), new ScaleListener());
    	if (mDetector == null) mDetector = new GestureDetector(recentGridView.getContext(), new SwipeListener((recentGridView.getContext())));
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
	private static void sendOnResume() {
		try {
			if (gridViewObject != null) {
				XposedHelpers.setBooleanField(gridViewObject, "mFinished", Boolean.valueOf(true));
				XposedHelpers.callMethod(gridViewObject, "onResume");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int[] toIntArray(List<Integer> list)  {
		int[] ret = new int[list.size()];
		int i = 0;
		for (Integer e : list) ret[i++] = e.intValue();
		return ret;
    }
	
	private static List<ActivityManager.RunningAppProcessInfo> procs = null;
	private static String ramTAG = "RAMView";

	private static class getRAMView extends AsyncTask<MethodHookParam, Void, Void> {
		ViewGroup theView = null;
		TextView ramView = null;
		String ramText = null;
		
        @Override
        protected Void doInBackground(final MethodHookParam... params) {
        	try {
        		final MethodHookParam param = params[0];
        		theView = (ViewGroup)param.getResult();
        		if (theView != null) {
        			int pos = (Integer)param.args[0];
        			Object viewholder = theView.getTag();
        			
        			ArrayList<?> mRecentTaskDescriptions = (ArrayList<?>)XposedHelpers.getObjectField(XposedHelpers.getSurroundingThis(param.thisObject), "mRecentTaskDescriptions");
        			if (mRecentTaskDescriptions == null) return null;
        			Object taskdescription = mRecentTaskDescriptions.get(mRecentTaskDescriptions.size() - (Integer)param.args[0] - 1);
        			if (taskdescription == null) return null;
        			ResolveInfo resolveInfo = (ResolveInfo)XposedHelpers.getObjectField(taskdescription, "resolveInfo");
        			
        			final ActivityManager am = (ActivityManager)theView.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        			if (pos == 0 || procs == null) procs = am.getRunningAppProcesses();
        			
        			final List<Integer> pids_mem = new ArrayList<Integer>();
        			for (ActivityManager.RunningAppProcessInfo process: procs)
        			if (process.processName.equals(resolveInfo.activityInfo.processName))
        			if (!pids_mem.contains(process.pid)) pids_mem.add(process.pid);
        			
        			MemoryInfo[] mi = am.getProcessMemoryInfo(toIntArray(pids_mem));
        			int memTotal = 0;
        			for (MemoryInfo memInfo: mi)
        			memTotal += memInfo.getTotalPss();
        			
        			XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
        			ramText = String.format("%.1f", (float)(memTotal / 1024.0f)) + modRes.getString(R.string.ram_mb);
        			if (theView.findViewWithTag(ramTAG) == null) {
        				ramView = new TextView(theView.getContext());
        				ramView.setTag(ramTAG);
        				ramView.setText(ramText);
            			final TextView text1 = (TextView)XposedHelpers.getObjectField(viewholder, "text1");		
            			ramView.setTextSize(TypedValue.COMPLEX_UNIT_PX, text1.getTextSize());
            			ramView.setEllipsize(TruncateAt.END);
            			ramView.setSingleLine();
            			ramView.setTypeface(text1.getTypeface());
            			ramView.setTextColor(Color.argb(127, Color.red(text1.getCurrentTextColor()), Color.green(text1.getCurrentTextColor()), Color.blue(text1.getCurrentTextColor())));
            			if (XMain.senseVersion.compareTo(new Version("5.5")) == -1) {
            				RelativeLayout.LayoutParams p0 = (RelativeLayout.LayoutParams)text1.getLayoutParams();
            				ImageView img = (ImageView)XposedHelpers.getObjectField(viewholder, "img");	
            				p0.setMargins(img.getPaddingLeft(), p0.topMargin, img.getPaddingRight(), 0);
            				ramView.setLayoutParams(p0);
            				ramView.setGravity(Gravity.CENTER);
            				ramView.setBackground(new ColorDrawable(Color.parseColor("#9F333333")));
                			ramView.setTranslationY(-32f * theView.getContext().getResources().getDisplayMetrics().density);
            			}else {
            				FrameLayout.LayoutParams p0 = (FrameLayout.LayoutParams)text1.getLayoutParams();
                			ramView.setLayoutParams(p0);
                			ramView.setGravity(Gravity.CENTER_VERTICAL);
                			ramView.setBackground(text1.getBackground());
                			ramView.setPadding(text1.getPaddingLeft(), text1.getPaddingTop(), text1.getPaddingRight(), text1.getPaddingBottom());
                			ramView.setTranslationY(-29.7f * theView.getContext().getResources().getDisplayMetrics().density);
            			}
        			}
        		}
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        	return null;        	
        }

        @Override
        protected void onPostExecute(Void result) {
        	if (theView != null)
            	if (theView.findViewWithTag(ramTAG) == null) {
            		if (ramView != null) theView.addView(ramView);
            	} else {
            		if (ramText != null)
            		((TextView)theView.findViewWithTag(ramTAG)).setText(ramText);
            	}
        }
    }
	
	public static void execHook_RAMInRecents(final LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.recent.RecentAppFxActivity.RecentGridViewAdapter", lpparam.classLoader, "getView", int.class, View.class, ViewGroup.class, new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				// Text before actual data is available
				ViewGroup theView = (ViewGroup)param.getResult();
        		if (theView != null && theView.findViewWithTag(ramTAG) != null)
        		((TextView)theView.findViewWithTag(ramTAG)).setText("...");
        		// Get RAM usage for the task of this view
				new getRAMView().execute(param);
			}
		});
	}
	
	static HtcPopupWindow popup = null;
	
	public static void execHook_RecentsLongTap(final LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.recent.RecentAppFxActivity.RecentGridViewAdapter", lpparam.classLoader, "getView", int.class, View.class, ViewGroup.class, new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				final ViewGroup theView = (ViewGroup)param.getResult();
        		if (theView != null) {
        			theView.setOnLongClickListener(new OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							try {
								Activity FxRecent = (Activity)XposedHelpers.getSurroundingThis(param.thisObject);
								
								popup = new HtcPopupWindow(FxRecent);
								float density = theView.getContext().getResources().getDisplayMetrics().density;
								int theWidth = Math.round(theView.getContext().getResources().getDisplayMetrics().widthPixels / 3 + 30 * density);
								popup.setWidth(theWidth);
								popup.setHeight(-2);
								popup.setTouchable(true);
								popup.setFocusable(true);
								popup.setOutsideTouchable(true);
								
								ListView options = new ListView(FxRecent);
								XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
								ListAdapter listAdapter = new PopupAdapter(options.getContext(), modRes.getStringArray(R.array.recents_menu), true);
								options.setAdapter(listAdapter);
								options.setFocusableInTouchMode(true);								
								options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
									@Override
									public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
										popup.dismiss();
										Object viewholder = theView.getTag();
										Object taskdescription = XposedHelpers.getObjectField(viewholder, "td");
										String packageName = (String)XposedHelpers.getObjectField(taskdescription, "packageName");
										if (position == 0) {
											Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null));
											intent.setComponent(intent.resolveActivity(view.getContext().getPackageManager()));
											intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
									        view.getContext().startActivity(intent);
										} else if (position == 1) {
											XposedHelpers.callMethod(XposedHelpers.getSurroundingThis(param.thisObject), "handleSwipe", theView);
										} else {
											try {
												terminateAll(1, theView);
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
								});
								popup.setContentView(options);
								
								Object mRecentGridView = XposedHelpers.getObjectField(XposedHelpers.getSurroundingThis(param.thisObject), "mRecentGridView");
								XposedHelpers.setBooleanField(mRecentGridView, "isDragging", true);
								popup.showAtLocation(theView, Gravity.TOP|Gravity.LEFT, Math.round(theView.getX() - theWidth/4), Math.round(theView.getY() - 20 * density));
								return true;
							} catch (Exception e) {
								return false;
							}
						}
        			});
        		}
			}
		});
		
		findAndHookMethod("com.android.systemui.recent.RecentAppFxActivity", lpparam.classLoader, "handleSwipe", View.class, new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				if (popup != null) popup.dismiss();
			}
		});
	}
	
	public static void execHook_NotifDrawerHeader(final InitPackageResourcesParam resparam) {
		resparam.res.hookLayout("com.android.systemui", "layout", "status_bar_expanded_header", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				View clock = liparam.view.findViewById(resparam.res.getIdentifier("clock", "id", "com.android.systemui"));
				View date = liparam.view.findViewById(resparam.res.getIdentifier("date", "id", "com.android.systemui"));
				final Intent clockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
				OnClickListener ocl = new OnClickListener() {
					@Override
					public void onClick(View v) {
						ComponentName cn = new ComponentName("com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl");
						clockIntent.setComponent(cn);
						clockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						v.getContext().startActivity(clockIntent);
						
						try {
							Object sbservice = v.getContext().getSystemService("statusbar");
							Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
							Method hidesb;
							if (Build.VERSION.SDK_INT >= 17) {
								hidesb = statusbarManager.getMethod("collapsePanels");
							} else {
								hidesb = statusbarManager.getMethod("collapse");
							}
							hidesb.setAccessible(true);
							hidesb.invoke(sbservice);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				clock.setOnClickListener(ocl);
				date.setOnClickListener(ocl);
			}
		});
	}
}

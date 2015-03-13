package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XResources;
import android.content.res.Resources.Theme;
import android.content.res.XModuleResources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
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
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.service.notification.StatusBarNotification;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils.TruncateAt;
import android.text.method.SingleLineTransformationMethod;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
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
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.htc.configuration.HtcWrapConfiguration;
import com.htc.widget.HtcCheckBox;
import com.htc.widget.HtcCompoundButton;
import com.htc.widget.HtcPopupWindow;
import com.htc.widget.HtcCompoundButton.OnCheckedChangeListener;
import com.htc.widget.HtcSeekBar;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.SenseThemes.PackageTheme;
import com.sensetoolbox.six.utils.Helpers;
import com.sensetoolbox.six.utils.PopupAdapter;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SysUIMods {

	private static int densify(Context ctx, int dimens) {
		return Math.round(ctx.getResources().getDisplayMetrics().density * dimens);
	}
	
	public static void execHook_NoStatusBarBackground(InitPackageResourcesParam resparam) {
		try {
			XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
			resparam.res.setReplacement("com.android.systemui", "drawable", "status_bar_background_transparent", modRes.fwd(R.drawable.status_bar_background_transparent));
			if (Helpers.isLP())
			resparam.res.setReplacement("com.android.systemui", "color", "system_bar_background_semi_transparent", Color.TRANSPARENT);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
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
				Object QS_MAPPING_OBJ = getStaticObjectField(param.thisObject.getClass(), "QS_MAPPING");
				String[] QS_MAPPING_ONE = null;
				String[][] QS_MAPPING_MULTI = null;
				if (QS_MAPPING_OBJ.getClass().getCanonicalName().equalsIgnoreCase("java.lang.String[][]"))
					QS_MAPPING_MULTI = (String[][])QS_MAPPING_OBJ;
				else
					QS_MAPPING_ONE = (String[])QS_MAPPING_OBJ;
				
				ArrayList<String> qsContent = new ArrayList<String>();
				ArrayList<String> qsContent2 = new ArrayList<String>();
				int[] paramArgs;
				Class<?> CustomizationUtil = findClass("com.android.systemui.CustomizationUtil", lpparam.classLoader);
				Object hcr = callStaticMethod(CustomizationUtil, "getReader");
				if (param.args[0] == null) {
					if (hcr == null)
						paramArgs = QS_DEFAULT;
					else
						paramArgs = (int[]) callMethod(hcr, "readIntArray", "quick_setting_items", QS_DEFAULT);
				} else {
					paramArgs = (int[]) param.args[0];
				}
				
				if (paramArgs == null || paramArgs.length == 0)
					paramArgs = QS_DEFAULT;
				int i = 0;
				if (QS_MAPPING_MULTI != null)
					i = QS_MAPPING_MULTI.length;
				else
					i = QS_MAPPING_ONE.length;
				
				int j = 0;
				for (int k = paramArgs.length; j < k; j++) {
					int i1 = paramArgs[j];
					if (i1 >= 0 && i1 < i)
					if (QS_MAPPING_MULTI != null)
						qsContent.add(QS_MAPPING_MULTI[i1][0]);
					else
						qsContent.add(QS_MAPPING_ONE[i1]);
				}
				qsContent2 = new ArrayList<String>();
				int l = 0;
				do {
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
		
		//Redraw the tile view because we have added or removed something...
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
				for(int k = 0; k < qsContainer.getChildCount(); k++) {
					LinearLayout tmp = (LinearLayout) qsContainer.getChildAt(k);
					LinearLayout.LayoutParams tmpParams = (LinearLayout.LayoutParams) tmp.getLayoutParams();
					tmpParams.width = (int) Math.floor(displayWidth / 5 - 3);
					tmp.setLayoutParams(tmpParams);
					if (removeText) {
						View quick_setting_text = tmp.findViewById(tmp.getResources().getIdentifier("quick_setting_text", "id", "com.android.systemui"));
						if (quick_setting_text != null) quick_setting_text.setVisibility(View.GONE);
						ImageView qsImg = (ImageView) tmp.findViewById(tmp.getResources().getIdentifier("quick_setting_image", "id", "com.android.systemui"));
						if (qsImg != null) qsImg.setPadding(0, 0, 0, 20);
					}
				}
				qsContainer.invalidate();
			}
		});
		
		//Makes them scrolling. Showing 5 at once.
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				FrameLayout mStatusBarWindow = (FrameLayout) getObjectField(param.thisObject, "mStatusBarWindow");
				if (mStatusBarWindow != null) {
					LinearLayout qsContainer = (LinearLayout) mStatusBarWindow.findViewById(mStatusBarWindow.getResources().getIdentifier("quick_settings_minor_container", "id", "com.android.systemui"));
					LinearLayout notificationContainer = (LinearLayout) mStatusBarWindow.findViewById(mStatusBarWindow.getResources().getIdentifier("notification_container", "id", "com.android.systemui"));
					if (qsContainer != null && notificationContainer != null) {
						HorizontalScrollView qsScroll = new HorizontalScrollView(mStatusBarWindow.getContext());
						qsScroll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
						qsScroll.setFillViewport(true);
						qsScroll.setHorizontalFadingEdgeEnabled(true);
						qsScroll.setHorizontalScrollBarEnabled(false);
						qsScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
						if (XMain.pref.getBoolean("pref_key_sysui_theqs", false))
							qsScroll.setBackgroundColor(Color.TRANSPARENT);
						else
							qsScroll.setBackgroundColor(Color.rgb(22, 22, 22));

						WindowManager wm = (WindowManager) mStatusBarWindow.getContext().getSystemService(Context.WINDOW_SERVICE);
						Display display = wm.getDefaultDisplay();
						Point displaySize = new Point();
						display.getSize(displaySize);
						int displayWidth = displaySize.x;
						
						for(int i = 0; i < qsContainer.getChildCount(); i++) {
							LinearLayout tmp = (LinearLayout) qsContainer.getChildAt(i);
							if (tmp == null) continue;
							LinearLayout.LayoutParams tmpParams = (LinearLayout.LayoutParams) tmp.getLayoutParams();
							tmpParams.width = (int) Math.floor(displayWidth / 5 - 3);
							tmp.setLayoutParams(tmpParams);
							if (removeText) {
								View quick_setting_text = tmp.findViewById(tmp.getResources().getIdentifier("quick_setting_text", "id", "com.android.systemui"));
								if (quick_setting_text != null) quick_setting_text.setVisibility(View.GONE);
								ImageView qsImg = (ImageView) tmp.findViewById(tmp.getResources().getIdentifier("quick_setting_image", "id", "com.android.systemui"));
								if (qsImg != null) qsImg.setPadding(0, 0, 0, 20);
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
	
	public static void execHook_InvisiNotify(final InitPackageResourcesParam resparam, final int transparency) {
		resparam.res.hookLayout("com.android.systemui", "layout", "super_status_bar", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				View notification_panel = liparam.view.findViewById(resparam.res.getIdentifier("notification_panel", "id", "com.android.systemui"));
				notification_panel.getBackground().setAlpha(transparency);
			}
		});
	}
	
	public static void execHook_InvisiNotifyCode(final LoadPackageParam lpparam, final int transparency) {
		if (Helpers.isLP()) {
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					FrameLayout mStatusBarWindow = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mStatusBarWindow");
					if (mStatusBarWindow != null) {
						View scrim_behind = mStatusBarWindow.findViewById(mStatusBarWindow.getResources().getIdentifier("scrim_behind", "id", "com.android.systemui"));
						if (scrim_behind != null) scrim_behind.setAlpha(transparency / 255f);
					}
				}
			});
		} else {
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.classLoader, "panelExpansionChanged", "com.android.systemui.statusbar.phone.PanelView", float.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					try {
						FrameLayout panelview = (FrameLayout)param.args[0];
						FrameLayout mFadingPanel = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mFadingPanel");
						int mScrimColor = (Integer)XposedHelpers.getObjectField(param.thisObject, "mScrimColor");
						boolean mShouldFade = (Boolean)XposedHelpers.getObjectField(param.thisObject, "mShouldFade");
						
						if (panelview == mFadingPanel && mScrimColor != 0 && mShouldFade) {
							Object mBar = XposedHelpers.getObjectField(param.thisObject, "mBar");
							if (mBar != null) {
								FrameLayout mStatusBarWindow = (FrameLayout)XposedHelpers.getObjectField(mBar, "mStatusBarWindow");
								if (mStatusBarWindow != null && mStatusBarWindow.getBackground() != null)
								mStatusBarWindow.getBackground().setAlpha(Math.round(mStatusBarWindow.getBackground().getAlpha() * (transparency + 191)/446));
							}
						}
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		}
	}
	
	public static void execHook_AospRecent(final LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.StatusBarFlag", lpparam.classLoader, "isHtcStyleRecentApp", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(false);
			}
		});
	}
	
	public static void execHook_CenterClockLayout(final InitPackageResourcesParam resparam) {
		resparam.res.hookLayout("com.android.systemui", "layout", "super_status_bar", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				FrameLayout statusBar = (FrameLayout)liparam.view.findViewById(resparam.res.getIdentifier("status_bar", "id", "com.android.systemui"));
				TextView clock = (TextView)liparam.view.findViewById(resparam.res.getIdentifier("clock", "id", "com.android.systemui"));
				LinearLayout systemIconArea = (LinearLayout)liparam.view.findViewById(resparam.res.getIdentifier("system_icon_area", "id", "com.android.systemui"));
				LinearLayout statusBarContents = (LinearLayout)liparam.view.findViewById(resparam.res.getIdentifier("status_bar_contents", "id", "com.android.systemui"));
				
				if (statusBar != null && clock != null && systemIconArea != null && statusBarContents != null) {
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
				} else {
					XposedBridge.log("[S6T] Center Clock Error: One or more layouts or views not found");
				}
			}
		});
	}
	
	private static void updateFillView(MethodHookParam param) {
		FrameLayout mStatusBarView = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mStatusBarView");
		if (mStatusBarView != null) {
			LinearLayout systemIconArea = (LinearLayout)mStatusBarView.findViewById(mStatusBarView.getResources().getIdentifier("system_icon_area", "id", "com.android.systemui"));
			if (systemIconArea != null) {
				LinearLayout fillView = (LinearLayout)mStatusBarView.findViewById(0x999999);
				if (fillView != null) {
					TextView clock = (TextView)mStatusBarView.findViewById(mStatusBarView.getResources().getIdentifier("clock", "id", "com.android.systemui"));
					if (clock != null) {
						int systemIconAreaLeft = systemIconArea.getLeft();
						int clockContainerLeft = clock.getLeft();
						LayoutParams fillViewParams = fillView.getLayoutParams();
						fillViewParams.width = systemIconAreaLeft - clockContainerLeft;
						fillView.setLayoutParams(fillViewParams);
						fillView.invalidate();
					}
				}
			}
		}
	}
	
	public static void execHook_CenterClockAnimation(LoadPackageParam lpparam) {
		//Listen for icon changes and update the width of the fill view
		try {
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "updateNotificationIcons", new XC_MethodHook(){
				@Override
				protected void beforeHookedMethod(MethodHookParam param) {
					updateFillView(param);
				}
			});
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) {
					updateFillView(param);
				}
			});
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "updateResources", new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) {
					updateFillView(param);
				}
			});
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "refreshAllIconsForLayout", LinearLayout.class, new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) {
					updateFillView(param);
				}
			});
			
			//Hide keyguard clocks on LP
			if (Helpers.isLP()) {
				findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "showClock", boolean.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) {
						ViewGroup mKeyguardStatusBar = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mKeyguardStatusBar");
						if (mKeyguardStatusBar != null) {
							View keyguard_clock = mKeyguardStatusBar.findViewById(mKeyguardStatusBar.getResources().getIdentifier("keyguard_clock", "id", "com.android.systemui"));
							if (keyguard_clock != null) keyguard_clock.setVisibility(View.GONE);
						}
					}
				});
			
				findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) {
						TextView clock = (TextView)param.thisObject;
						if (clock.getId() == clock.getResources().getIdentifier("keyguard_clock", "id", "com.android.systemui")) clock.setVisibility(View.GONE);
					}
				});
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
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
			protected void afterHookedMethod(MethodHookParam param) {
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
			protected void afterHookedMethod(MethodHookParam param) {
				stuff.clock_container.setVisibility(View.GONE);
				Animation ani = (Animation) callMethod(stuff.statusbar, "loadAnim", stuff.animOut, null);
				stuff.clock_container.startAnimation(ani);
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar$MyTicker", lpparam.classLoader, "tickerDone", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param) {
				stuff.clock_container.setVisibility(View.VISIBLE);
				Animation ani = (Animation) callMethod(stuff.statusbar, "loadAnim", stuff.animIn, null);
				stuff.clock_container.startAnimation(ani);
			}
		});
		
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar$MyTicker", lpparam.classLoader, "tickerHalting", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param) {
				stuff.clock_container.setVisibility(View.VISIBLE);
				Animation ani = (Animation) callMethod(stuff.statusbar, "loadAnim", stuff.animFade, null);
				stuff.clock_container.startAnimation(ani);
			}
		});
	}
	
	public static void execHook_removeAMPM(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param) {
				String newTime = ((TextView)param.thisObject).getText().toString().replaceAll("(?i)am|pm", "").trim();
				((TextView)param.thisObject).setText(newTime);
			}
		});
	}
	
	public static void execHook_ClockRemove(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "showClock", boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) {
				if ((Boolean)param.args[0]) param.setResult(null);
			}
		});
		
		if (Helpers.isLP()) {
			findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) {
					TextView clock = (TextView)param.thisObject;
					if (clock.getId() != clock.getResources().getIdentifier("header_clock", "id", "com.android.systemui")) clock.setVisibility(View.GONE);
				}
			});
		} else {
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "updateClockTime", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) {
					ArrayList<?> mClockSet = (ArrayList<?>)XposedHelpers.getObjectField(param.thisObject, "mClockSet");
					if (mClockSet != null && mClockSet.size() > 0) ((TextView)mClockSet.get(0)).setVisibility(View.GONE);
				}
			});
		}
	}
	
	private static SettingsObserver so = null;
	private static void addBrightnessSlider(ViewGroup mStatusBarWindow, ViewGroup mHeader) {
		try {
			final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
			ViewGroup panel;
			if (mHeader != null)
				panel = mHeader;
			else
				panel = (LinearLayout) mStatusBarWindow.findViewById(mStatusBarWindow.getResources().getIdentifier("panel", "id", "com.android.systemui"));
			LinearLayout header = (LinearLayout) mStatusBarWindow.findViewById(mStatusBarWindow.getResources().getIdentifier("header", "id", "com.android.systemui"));
			
			// Get rid of old slider
			if (so != null) {
				mStatusBarWindow.getContext().getContentResolver().unregisterContentObserver(so);
				so = null;
			}
			View oldSliderConatiner = panel.findViewWithTag("sliderConatiner");
			if (oldSliderConatiner != null) panel.removeView(oldSliderConatiner);
			
			// Inflate the slider layout using current theme
			ContextThemeWrapper ctw = new ContextThemeWrapper(panel.getContext(), HtcWrapConfiguration.getHtcThemeId(panel.getContext(), 0));
			PackageTheme pt = Helpers.getThemeForPackageFromXposed(ctw.getPackageName());
			if (pt != null) ctw.setTheme(Helpers.colors.keyAt(pt.getTheme()));
			
			LayoutInflater inflater = LayoutInflater.from(ctw);
			LinearLayout sliderConatiner = new LinearLayout(ctw);
			sliderConatiner = (LinearLayout)inflater.inflate(modRes.getLayout(R.layout.brightness_slider), panel, false);
			if (Helpers.isLP()) {
				sliderConatiner.setBackground(new ColorDrawable(Color.TRANSPARENT));
				//panel.getResources().getColor(panel.getResources().getIdentifier("notification_header_color", "color", "com.android.systemui")
			} else {
				sliderConatiner.setBackground(panel.getResources().getDrawable(panel.getResources().getIdentifier("common_app_bkg_top_src", "drawable", "com.htc")));
				if (header != null && header.getBackground() != null) sliderConatiner.setBackground(header.getBackground().mutate().getConstantState().newDrawable());
			}
			sliderConatiner.setTag("sliderConatiner");
			sliderConatiner.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					v.performClick();
					return true;
				}
			});
			
			TextView autoText = (TextView)sliderConatiner.findViewById(R.id.autoText);
			final HtcCheckBox cb = (HtcCheckBox)sliderConatiner.findViewById(R.id.autoCheckBox);
			autoText.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					cb.toggle(); //Make it easier to toggle the checkbox. Way harder to hit it without that...
				}
			});
			autoText.setText(Helpers.xl10n(modRes, R.string.systemui_brightslide_auto));
			
			if (Helpers.isLP())
				panel.addView(sliderConatiner);
			else
				panel.addView(sliderConatiner, 1);
			
			final HtcSeekBar seekBar = (HtcSeekBar) mStatusBarWindow.findViewWithTag("sliderSeekBar");
			final HtcCheckBox checkBox = (HtcCheckBox) mStatusBarWindow.findViewById(R.id.autoCheckBox);
			final ContentResolver cr = mStatusBarWindow.getContext().getContentResolver();
			
			seekBar.setProgress(android.provider.Settings.System.getInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS) - 30);
			checkBox.setChecked(android.provider.Settings.System.getInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == 0 ? false : true);
			seekBar.setEnabled(android.provider.Settings.System.getInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == 0 ? true : false);
			seekBar.setDisplayMode(1); //Seekbar black BG
			
			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					seekBar.setPressed(false);
					android.provider.Settings.System.putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS, seekBar.getProgress() + 30);
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					seekBar.setPressed(true);
				}
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if (fromUser) try {
						Object pwrmgr = seekBar.getContext().getSystemService(Context.POWER_SERVICE);
						XposedHelpers.callMethod(pwrmgr, "setBacklightBrightness", progress + 30);
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
			
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(HtcCompoundButton arg0, boolean arg1) {
					try {
						if (arg1) {
							android.provider.Settings.System.putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
							seekBar.setProgress(android.provider.Settings.System.getInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS) - 30);
							seekBar.setEnabled(false);
						} else {
							android.provider.Settings.System.putInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
							seekBar.setProgress(android.provider.Settings.System.getInt(cr, android.provider.Settings.System.SCREEN_BRIGHTNESS) - 30);
							seekBar.setEnabled(true);
						}
					} catch (SettingNotFoundException e) {
						//No brightness setting?
					}
				}
			});
			
			if (so == null) {
				so = new SettingsObserver(new Handler());
				so.setup(checkBox, seekBar, cr);
				cr.registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, so);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_BrightnessSlider(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param) {
				ViewGroup mStatusBarWindow = (ViewGroup)getObjectField(param.thisObject, "mStatusBarWindow");
				ViewGroup mHeader = null;
				if (Helpers.isLP()) mHeader = (ViewGroup)getObjectField(param.thisObject, "mHeader");
				addBrightnessSlider(mStatusBarWindow, mHeader);
			}
		});
		
		if (Helpers.isLP()) {
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "onThemeChanged", int.class, new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) {
					ViewGroup mStatusBarWindow = (ViewGroup)getObjectField(param.thisObject, "mStatusBarWindow");
					ViewGroup mHeader = (ViewGroup)getObjectField(param.thisObject, "mHeader");
					addBrightnessSlider(mStatusBarWindow, mHeader);
				}
			});
			
			findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarHeaderView", lpparam.classLoader, "setupContainerParams", new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) {
					LinearLayout sbheader = (LinearLayout)param.thisObject;
					float density = sbheader.getResources().getDisplayMetrics().density;
					if (sbheader.getLayoutParams() != null) sbheader.getLayoutParams().height += density * 28;
				}
			});
		} else {
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "onOverlayColorChanged", new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) {
					FrameLayout mStatusBarWindow = (FrameLayout)getObjectField(param.thisObject, "mStatusBarWindow");
					addBrightnessSlider(mStatusBarWindow, null);
				}
			});
		}
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
	
	private static BroadcastReceiver connectChanged = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			try {
				if (mRunnable == null || mHandler == null) return;
				String action = intent.getAction();
				if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
					connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
					mHandler.removeCallbacks(mRunnable);
					if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
						mHandler.post(mRunnable);
					} else if (dataRateVal != null && dataRateUnits != null) {
						dataRateVal.setVisibility(8);
						dataRateUnits.setVisibility(8);
					}
				}
			} catch(Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
	
	public static void execHook_DataRateStatus(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook(){
			@Override
			protected void afterHookedMethod(MethodHookParam param) {
				if (dataRateVal != null && dataRateUnits != null) return;
				try {
					Context mContext = (Context)getObjectField(param.thisObject, "mContext");
					connectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
					
					FrameLayout mStatusBarView = (FrameLayout)getObjectField(param.thisObject, "mStatusBarView");
					LinearLayout systemIconArea = (LinearLayout)mStatusBarView.findViewById(mStatusBarView.getResources().getIdentifier("system_icon_area", "id", "com.android.systemui"));
					
					RelativeLayout alignFrame = new RelativeLayout(mContext);
					alignFrame.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
					
					LinearLayout textFrame = new LinearLayout(mContext);
					textFrame.setOrientation(LinearLayout.VERTICAL);
					textFrame.setGravity(Gravity.CENTER_HORIZONTAL);
					RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					//rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
					textFrame.setLayoutParams(rlp);
					
					dataRateVal = new TextView(mContext);
					dataRateVal.setVisibility(8);
					dataRateVal.setTransformationMethod(SingleLineTransformationMethod.getInstance());
					dataRateVal.setEllipsize(null);
					dataRateVal.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
					dataRateVal.setTextColor(Color.WHITE);
					dataRateVal.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
					dataRateVal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13.0f);
					dataRateVal.setIncludeFontPadding(false);
					dataRateVal.setPadding(0, 2, 5, 0);
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
					dataRateUnits.setPadding(0, 0, 5, 0);
					dataRateUnits.setScaleY(0.9f);
					
					textFrame.addView(dataRateVal, 0);
					textFrame.addView(dataRateUnits, 1);
					alignFrame.addView(textFrame);
					systemIconArea.addView(alignFrame, 0);
					
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
										long speed = Math.round(newBytesFixed/2);
										bytesTotal = newBytes;
										ArrayList<String> spd = humanReadableByteCount(speed);
										dataRateVal.setText(spd.get(0));
										dataRateUnits.setText(spd.get(1));
										if (XMain.pref.getBoolean("pref_key_cb_texts", false)) {
											int themeColor = StatusbarMods.getThemeColor();
											dataRateVal.setTextColor(themeColor);
											dataRateUnits.setTextColor(themeColor);
										}
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
							} catch (Throwable t) {
								XposedBridge.log(t);
							}
							
							if (mHandler != null)
							mHandler.postDelayed(mRunnable, 2000L);
						}
					};
					
					mContext.registerReceiver(connectChanged, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
	
	//Need this to listen for settings changes
	protected static class SettingsObserver extends ContentObserver {
		private HtcCheckBox cb = null;
		private HtcSeekBar sb = null;
		private ContentResolver cr;
		public SettingsObserver(Handler handler) {
			super(handler);
		}
		public void setup(HtcCheckBox cbx, HtcSeekBar sbr, ContentResolver crr) {
			this.cb = cbx;
			this.sb = sbr;
			this.cr = crr;
		}
		@Override
		public void onChange(boolean selfChange) {
			this.onChange(selfChange, null);
		}
		@Override
		public void onChange(boolean selfChange, Uri uri) {
			if (!this.sb.isPressed()) try {
				this.cb.setChecked(android.provider.Settings.System.getInt(this.cr, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == 0 ? false : true);
				this.sb.setProgress(android.provider.Settings.System.getInt(this.cr, android.provider.Settings.System.SCREEN_BRIGHTNESS) - 30);
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
		findAndHookMethod("com.android.systemui.statusbar.phone.QuickSettingsTileView", lpparam.classLoader, "getOnLongClickListener", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				final LinearLayout thisTile = (LinearLayout) param.thisObject;
				param.setResult(new OnLongClickListener() {
					@Override
					@SuppressLint("DefaultLocale")
					public boolean onLongClick(View v) {
						String clickedTile = (String) getObjectField(thisTile, "tileLabel");
						if (!clickedTile.equals("") && !(clickedTile == null)) {
							String intentPkg = "";
							String intentClass = "";
							Intent settingIntent = null;
							if (clickedTile.equals("apn")) { intentPkg = "com.android.settings"; intentClass = "com.android.settings.CdmaApnSettings"; }
							if (clickedTile.equals("auto_sync")) intentClass = "android.settings.SYNC_SETTINGS";
							if (clickedTile.equals("bluetooth")) intentClass = "android.settings.BLUETOOTH_SETTINGS";
							if (clickedTile.equals("brightness")) { settingIntent = new Intent("android.settings.DISPLAY_SETTINGS"); settingIntent.putExtra(":android:show_preference", "brightness"); }
							if (clickedTile.equals("do_not_disturb")) intentClass = "com.htc.settings.DND_SETTINGS";
							if (clickedTile.equals("gps")) intentClass = "android.settings.LOCATION_SOURCE_SETTINGS";
							if (clickedTile.equals("mobile_data")) { intentPkg = "com.android.phone"; intentClass = "com.android.phone.MobileNetworkSettings"; }
							if (clickedTile.equals("power_saver")) { intentPkg = "com.htc.htcpowermanager"; intentClass = "com.htc.htcpowermanager.powersaver.PowerSaverActivity"; }
							if (clickedTile.equals("power_saver_ex")) intentClass = "com.htc.htcpowermanager.EXTREME_POWER_SAVER_CONFIRM";
							if (clickedTile.equals("screenshot")) {
								int mBucketId = -1;
								File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Screenshots");
								if (file != null) mBucketId = file.getAbsolutePath().toLowerCase().hashCode();
								settingIntent = new Intent("com.htc.album.action.VIEW_FOLDER_IN_THUMBNAIL");
								settingIntent.putExtra("folder_type", (new StringBuilder()).append("collection_regular_bucket ").append(mBucketId).append(" Screenshots").toString());
								settingIntent.putExtra("entry_from", "Screenshots");
								settingIntent.setDataAndType(null, "image/*");
								settingIntent.setFlags(0x14000000);
							}
							if (clickedTile.equals("wifi")) intentClass = "android.settings.WIFI_SETTINGS";
							if (clickedTile.equals("wifi_hotspot")) { intentPkg = "com.htc.WifiRouter"; intentClass = "com.htc.WifiRouter.WifiRouter"; }
							Object viewTag = thisTile.getTag();
							if (viewTag != null) {
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
		try {
			if (Helpers.isLP())
			findAndHookMethod("com.android.systemui.recent.htc.RecentAppActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook(){
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					final ViewGroup mPager = (ViewGroup)XposedHelpers.findField(param.thisObject.getClass(), "mPager").get(param.thisObject);
			
					killedEmAll = false;
			
					actObject = param.thisObject;
					actContext = mPager.getContext();
					pagerSelf = mPager;
				}
			});
			else
			findAndHookMethod("com.android.systemui.recent.RecentAppFxActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook(){
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					final GridView recentGridView = (GridView)XposedHelpers.findField(param.thisObject.getClass(), "mRecentGridView").get(param.thisObject);
				
					killedEmAll = false;
				
					actObject = param.thisObject;
					actContext = recentGridView.getContext();
					gridViewSelf = recentGridView;
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_RecentAppsClearTouch(final LoadPackageParam lpparam) {
		if (Helpers.isLP()) {
			findAndHookMethod("com.android.systemui.recent.htc.RecentAppActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook(){
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					try {
						initDetectors(((Activity)param.thisObject));
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
					ViewGroup mPager = (ViewGroup)XposedHelpers.findField(param.thisObject.getClass(), "mPager").get(param.thisObject);
					mPager.setOnTouchListener(new OnTouchListener() {
						@Override
						@SuppressLint("ClickableViewAccessibility")
						public boolean onTouch(View v, MotionEvent ev) {
							if (killedEmAll) return false;
							if (ev == null) return false;
							mScaleDetector.onTouchEvent(ev);
							mDetector.onTouchEvent(ev);
							
							return false;
						}
					});
				}
			});
			
			findAndHookMethod("com.android.systemui.recent.htc.RecentAppSwipeHelper", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new TouchListenerOnTouch());
			findAndHookMethod("com.android.systemui.recent.htc.RecentAppSwipeHelper", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new TouchListenerOnTouchIntercept());
		} else {
			findAndHookMethod("com.android.systemui.recent.RecentsGridView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new TouchListenerOnTouch());
			findAndHookMethod("com.android.systemui.recent.RecentsGridView", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new TouchListenerOnTouchIntercept());
		}
	}
	
	private static Object actObject;
	private static Context actContext;
	private static ViewGroup pagerSelf;
	private static GridView gridViewSelf;
	private static ActivityManager am;
	
	private static ScaleGestureDetector mScaleDetector;
	private static GestureDetector mDetector;
	static boolean killedEmAll = false;
	
	private static void animateViewAtPos(Context ctx, View theItem, final ViewGroup currApp, int animType, int cnt, final int i) {
		if (ctx != null) {
			AnimationSet localAnimationSet = new AnimationSet(true);
			Animation fadeOut = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_out);
			if (animType == 0) {
				fadeOut.setDuration(220L);
				fadeOut.setStartOffset(cnt * 30);
				fadeOut.setInterpolator(AnimationUtils.loadInterpolator(ctx, android.R.anim.linear_interpolator));
				
				TranslateAnimation drop = new TranslateAnimation(0.0F, 0.0F, 0.0F, 100.0f);
				drop.setDuration(220L);
				drop.setStartOffset(cnt * 30);
				drop.setInterpolator(AnimationUtils.loadInterpolator(ctx, android.R.anim.linear_interpolator));
				localAnimationSet.addAnimation(drop);
				localAnimationSet.addAnimation(fadeOut);
			} else {
				fadeOut.setDuration(220L);
				fadeOut.setStartOffset(cnt * 30);
				fadeOut.setInterpolator(AnimationUtils.loadInterpolator(ctx, android.R.anim.linear_interpolator));
				
				ScaleAnimation shrink = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				shrink.setDuration(220L);
				shrink.setStartOffset(cnt * 30);
				shrink.setInterpolator(AnimationUtils.loadInterpolator(ctx, android.R.anim.linear_interpolator));
				localAnimationSet.addAnimation(shrink);
				localAnimationSet.addAnimation(fadeOut);
			}
			
			localAnimationSet.setFillAfter(true);
			if (cnt == 0)
			localAnimationSet.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationEnd(Animation paramAnonymousAnimation) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							if (Helpers.isLP()) {
								XposedHelpers.setBooleanField(actObject, "mIsClicked", true);
								Object mTaskLoader = XposedHelpers.getObjectField(actObject, "mTaskLoader");
								XposedHelpers.callMethod(mTaskLoader, "clearAllTasks");
							} else {
								try { if (i > 3) Thread.sleep((i + 1) * 15); } catch (Exception e) {}
								if (currApp == null) closeRecents();
							}
						}
					}).start();
				}
				public void onAnimationRepeat(Animation paramAnonymousAnimation) {}
				public void onAnimationStart(Animation paramAnonymousAnimation) {}
			});
			theItem.startAnimation(localAnimationSet);
		}
	}
	
	// Exterminate! - Daleks
	private static void terminateAll(int animType) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
		terminateAll(animType, null);
	}
	
	private static void terminateAll(int animType, final ViewGroup currApp) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
		if (Helpers.isLP()) {
			int pageNum = 0;
			try {
				pageNum = (Integer)XposedHelpers.getIntField(actObject, "mCurrentPage");
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
					
			ViewGroup page = (ViewGroup)pagerSelf.getChildAt(pageNum);
			if (page == null) return;
			int i = page.getChildCount();
			int cnt = i - 1;
			if (cnt < 0) {
				closeRecents();
				return;
			}
			
			// Go through all GridView items and get taskIds
			while (cnt >= 0) {
				View pageItem = page.getChildAt(cnt);
				if (pageItem != null) animateViewAtPos(actContext, pageItem, currApp, animType, cnt, i);
				cnt--;
			}
		} else {
			ArrayList<?> taskDescriptionsArray = (ArrayList<?>)XposedHelpers.getObjectField(actObject, "mRecentTaskDescriptions");
			if ((taskDescriptionsArray == null) || (taskDescriptionsArray.size() == 0))	{
				// Recent array is empty, resuming last activity
				closeRecents();
				return;
			}
			final int i = gridViewSelf.getChildCount();
			int j = taskDescriptionsArray.size();
			int cnt = i - 1;
			
			// Go through all GridView items and get taskIds
			while (cnt >= 0) {
				View gridViewItem = gridViewSelf.getChildAt(cnt);
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
						am = ((ActivityManager)actContext.getSystemService("activity"));
						if (am != null)
						XposedHelpers.callMethod(am, "removeTask", XposedHelpers.getIntField(gridViewItemTag, "persistentTaskId"), Integer.valueOf(1));
						
						animateViewAtPos(actContext, gridViewItem, currApp, animType, cnt, i);
					}
				}
				cnt--;
			}
			if (currApp != null) {
				Handler handler = (Handler)XposedHelpers.getObjectField(actObject, "handler");
				Runnable runnable = new Runnable() {
					public void run() {
						XposedHelpers.callMethod(actObject, "handleOnClick", currApp);
					}
				};
				handler.postDelayed(runnable, 300L + (i - 1) * 30L);
			}
		}

	}
	
	// Listener for scale gestures
	private static class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			killedEmAll = true;
			try {
				terminateAll(1);
			} catch (Throwable t) {
				XposedBridge.log(t);
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
				if (e1 == null || e2 == null) return false;
				if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH) return false;
				if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
					killedEmAll = true;
					terminateAll(0);
				}
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
			return false;
		}
	}
	
	private static void initDetectors(Context ctx) throws Throwable {
		if (mScaleDetector == null) mScaleDetector = new ScaleGestureDetector(ctx, new ScaleListener());
		if (mDetector == null) mDetector = new GestureDetector(ctx, new SwipeListener(ctx));
	}
	
	// Detect second finger and cancel action if some app thumbnail was pressed
	private static class TouchListenerOnTouchIntercept extends XC_MethodHook {
		MotionEvent ev = null;
		
		@Override
		protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
			if (!Helpers.isLP()) {
				ViewGroup recentsContainer = (ViewGroup)param.thisObject;
				initDetectors(recentsContainer.getContext());
			}
			ev = (MotionEvent)param.args[0];
			if (ev == null) return;
			mScaleDetector.onTouchEvent(ev);
			mDetector.onTouchEvent(ev);
			
			final int action = ev.getAction();
			switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					if (ev.getPointerCount() == 2)
					try {
						param.setResult(Boolean.valueOf(true));
					} catch (Throwable thw) {
						param.setThrowable(thw);
					}
			}
		}
	}
	
	// Detect scale/swipe gestures
	private static class TouchListenerOnTouch extends XC_MethodHook {
		MotionEvent ev = null;
		
		@Override
		protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
			if (killedEmAll) return;

			if (!Helpers.isLP()) {
				ViewGroup recentsContainer = (ViewGroup)param.thisObject;
				initDetectors(recentsContainer.getContext());
			}
			ev = (MotionEvent)param.args[0];
			if (ev == null) return;
			mScaleDetector.onTouchEvent(ev);
			mDetector.onTouchEvent(ev);
		}
	}
	
	// Close activity
	private static void closeRecents() {
		try {
			if (actObject != null) ((Activity)actObject).finish();
		} catch (Throwable t) {
			XposedBridge.log(t);
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
		MethodHookParam param;
		
		@Override
		protected Void doInBackground(final MethodHookParam... params) {
			try {
				param = params[0];
				theView = (ViewGroup)param.getResult();
				if (theView != null) {
					final ActivityManager amgr = (ActivityManager)theView.getContext().getSystemService(Context.ACTIVITY_SERVICE);
					final List<Integer> pids_mem = new ArrayList<Integer>();
					Object viewholder = theView.getTag();
					
					if (Helpers.isLP()) {
						String packageName = (String)XposedHelpers.getObjectField(param.args[1], "packageName");
						
						if (procs == null) procs = amgr.getRunningAppProcesses();
						for (ActivityManager.RunningAppProcessInfo process: procs)
						if (Arrays.asList(process.pkgList).contains(packageName))
						if (!pids_mem.contains(process.pid)) pids_mem.add(process.pid);
					} else {
						int pos = (Integer)param.args[0];
						
						ArrayList<?> mRecentTaskDescriptions = (ArrayList<?>)XposedHelpers.getObjectField(XposedHelpers.getSurroundingThis(param.thisObject), "mRecentTaskDescriptions");
						if (mRecentTaskDescriptions == null) return null;
						int taskPos = mRecentTaskDescriptions.size() - pos - 1;
						if (taskPos < 0) return null;
						Object taskdescription = mRecentTaskDescriptions.get(taskPos);
						if (taskdescription == null) return null;
						ResolveInfo resolveInfo = (ResolveInfo)XposedHelpers.getObjectField(taskdescription, "resolveInfo");
						
						if (pos == 0 || procs == null) procs = amgr.getRunningAppProcesses();
						for (ActivityManager.RunningAppProcessInfo process: procs)
						if (process.processName.equals(resolveInfo.activityInfo.processName))
						if (!pids_mem.contains(process.pid)) pids_mem.add(process.pid);
					}
					
					MemoryInfo[] mi = amgr.getProcessMemoryInfo(toIntArray(pids_mem));
					int memTotal = 0;
					for (MemoryInfo memInfo: mi) memTotal += memInfo.getTotalPss();
					
					XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
					ramText = String.format("%.1f", (float)(memTotal / 1024.0f)) + Helpers.xl10n(modRes, R.string.ram_mb);
					if (theView.findViewWithTag(ramTAG) == null) {
						ramView = new TextView(theView.getContext());
						ramView.setTag(ramTAG);
						ramView.setText(ramText);
						TextView text1;
						if (Helpers.isLP())
							text1 = (TextView)XposedHelpers.getObjectField(viewholder, "text");
						else
							text1 = (TextView)XposedHelpers.getObjectField(viewholder, "text1");
						
						ramView.setTextSize(TypedValue.COMPLEX_UNIT_PX, text1.getTextSize());
						ramView.setEllipsize(TruncateAt.END);
						ramView.setSingleLine();
						ramView.setTypeface(text1.getTypeface());
						ramView.setTextColor(Color.argb(190, Color.red(text1.getCurrentTextColor()), Color.green(text1.getCurrentTextColor()), Color.blue(text1.getCurrentTextColor())));
						FrameLayout.LayoutParams p0 = (FrameLayout.LayoutParams)text1.getLayoutParams();
						ramView.setLayoutParams(p0);
						ramView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
						ramView.setBackground(new ColorDrawable(0xa0252525));
						ramView.setPadding(text1.getPaddingLeft(), text1.getPaddingTop() + 5, text1.getPaddingRight(), text1.getPaddingBottom());
					}
				}
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			Handler hndl = null;
			if (Helpers.isLP())
				hndl = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
			else
				hndl = (Handler)XposedHelpers.getObjectField(XposedHelpers.getSurroundingThis(param.thisObject), "handler");
			
			if (hndl != null)
			hndl.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (theView != null)
					if (theView.findViewWithTag(ramTAG) == null) {
						if (ramView != null) {
							theView.addView(ramView, 1);
							ObjectAnimator translationY = ObjectAnimator.ofFloat(ramView, "translationY", 0.0f, -29.7f * theView.getContext().getResources().getDisplayMetrics().density);
							ObjectAnimator alpha = ObjectAnimator.ofFloat(ramView, "alpha", 0.0f, 1.0f);
							translationY.setInterpolator(AnimationUtils.loadInterpolator(theView.getContext(), android.R.anim.decelerate_interpolator));
							translationY.setDuration(220L);
							alpha.setInterpolator(AnimationUtils.loadInterpolator(theView.getContext(), android.R.anim.linear_interpolator));
							alpha.setDuration(220L);
							
							translationY.start();
							alpha.start();
						}
					} else {
						if (ramText != null) ((TextView)theView.findViewWithTag(ramTAG)).setText(ramText);
					}
				}
			}, 300L);
		}
	}
	
	private static void execRAMView(MethodHookParam param) {
		ViewGroup theView = (ViewGroup)param.getResult();
		if (theView != null && theView.findViewWithTag(ramTAG) != null)
		((TextView)theView.findViewWithTag(ramTAG)).setText("...");
		new getRAMView().execute(param);
	}
	
	public static void execHook_RAMInRecents(final LoadPackageParam lpparam) {
		if (Helpers.isLP()) {
			findAndHookMethod("com.android.systemui.recent.htc.RecentAppActivity", lpparam.classLoader, "inflateItemView", ViewGroup.class, "com.android.systemui.recent.htc.RecentAppTask", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					execRAMView(param);
				}
			});
			
			findAndHookMethod("com.android.systemui.recent.htc.RecentAppActivity", lpparam.classLoader, "onResume", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					procs = null;
				}
			});
		} else {
			findAndHookMethod("com.android.systemui.recent.RecentAppFxActivity.RecentGridViewAdapter", lpparam.classLoader, "getView", int.class, View.class, ViewGroup.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					execRAMView(param);
				}
			});
			
			findAndHookMethod("com.android.systemui.recent.RecentAppFxActivity", lpparam.classLoader, "onResume", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					procs = null;
				}
			});
		}
	}
	
	static HtcPopupWindow popup = null;
	
	private static void bindPopup(final Activity act, final ViewGroup theView) {
		theView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				try {
					popup = new HtcPopupWindow(act);
					float density = theView.getContext().getResources().getDisplayMetrics().density;
					int theWidth = Math.round(theView.getContext().getResources().getDisplayMetrics().widthPixels / 3 + 30 * density);
					popup.setWidth(theWidth);
					popup.setHeight(-2);
					popup.setTouchable(true);
					popup.setFocusable(true);
					popup.setOutsideTouchable(true);
					
					ListView options = new ListView(act);
					XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
					String[] recents_menu = Helpers.xl10n_array(modRes, R.array.recents_menu);
					if (Helpers.isLP()) recents_menu = Arrays.copyOfRange(recents_menu, 0, recents_menu.length - 1);
					ListAdapter listAdapter = new PopupAdapter(options.getContext(), recents_menu, true);
					options.setAdapter(listAdapter);
					options.setFocusableInTouchMode(true);
					options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							popup.dismiss();
							Object viewholder = theView.getTag();
							Object taskdescription = null;
							if (Helpers.isLP())
								taskdescription = XposedHelpers.getObjectField(viewholder, "task");
							else
								taskdescription = XposedHelpers.getObjectField(viewholder, "td");
							
							if (taskdescription == null) return;
							String packageName = (String)XposedHelpers.getObjectField(taskdescription, "packageName");
							if (position == 0) {
								Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null));
								intent.setComponent(intent.resolveActivity(view.getContext().getPackageManager()));
								intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
								view.getContext().startActivity(intent);
							} else if (position == 1) {
								try {
									theView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
								} catch (android.content.ActivityNotFoundException anfe) {
									theView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
								}
							} else if (position == 2) {
								XposedHelpers.callMethod(act, "handleSwipe", theView);
							} else {
								try {
									terminateAll(1, theView);
								} catch (Throwable t) {
									XposedBridge.log(t);
								}
							}
						}
					});
					popup.setContentView(options);
					
					if (!Helpers.isLP()) {
						Object mRecentGridView = XposedHelpers.getObjectField(act, "mRecentGridView");
						XposedHelpers.setBooleanField(mRecentGridView, "isDragging", true);
					}
					popup.showAtLocation(theView, Gravity.TOP|Gravity.START, Math.round(theView.getX() - theWidth/4), Math.round(theView.getY() - 20 * density));
					return true;
				} catch (Exception e) {
					return false;
				}
			}
		});
	}
	
	public static void execHook_RecentsLongTap(final LoadPackageParam lpparam) {
		if (Helpers.isLP()) {
			findAndHookMethod("com.android.systemui.recent.htc.RecentAppActivity", lpparam.classLoader, "inflateItemView", ViewGroup.class, "com.android.systemui.recent.htc.RecentAppTask", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					final FrameLayout theView = (FrameLayout)param.getResult();
					if (theView != null) bindPopup((Activity)param.thisObject, theView);
				}
			});
			
			findAndHookMethod("com.android.systemui.recent.htc.RecentAppActivity", lpparam.classLoader, "handleSwipe", View.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					Activity FxRecent = (Activity)param.thisObject;
					if (FxRecent != null && !FxRecent.isFinishing() && popup != null && popup.isShowing()) try { popup.dismiss(); } catch (Throwable t) {}
				}
			});
		} else {
			findAndHookMethod("com.android.systemui.recent.RecentAppFxActivity.RecentGridViewAdapter", lpparam.classLoader, "getView", int.class, View.class, ViewGroup.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					final ViewGroup theView = (ViewGroup)param.getResult();
					if (theView != null) bindPopup((Activity)XposedHelpers.getSurroundingThis(param.thisObject), theView);
				}
			});
		
			findAndHookMethod("com.android.systemui.recent.RecentAppFxActivity", lpparam.classLoader, "handleSwipe", View.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					Activity FxRecent = (Activity)param.thisObject;
					if (FxRecent != null && !FxRecent.isFinishing() && popup != null && popup.isShowing()) try { popup.dismiss(); } catch (Throwable t) {}
				}
			});
		}
	}
	
	private static Thread cpuThread = null;
	private static boolean isThreadActive = false;
	//private static int USSDState = 0;
	private static long workLast, totalLast, workC, totalC = 0;
	private static int curFreq;
	private static String curTemp = "?";
	private static void readCPU() {
		BufferedReader readStream;
		String[] a;
		long work, total;
		
		try {
			readStream = new BufferedReader(new FileReader("/proc/stat"));
			String line = readStream.readLine();
			if (line != null) {
				a = line.split("[ ]+", 9);
				work = Long.parseLong(a[1]) + Long.parseLong(a[2]) + Long.parseLong(a[3]);
				total = work + Long.parseLong(a[4]) + Long.parseLong(a[5]) + Long.parseLong(a[6]) + Long.parseLong(a[7]);

				if (totalLast != 0) {
					workC = work - workLast;
					totalC = total - totalLast;
				}
				workLast = work;
				totalLast = total;
			}
			readStream.close();
			
			readStream = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"));
			curFreq = Math.round((Integer.valueOf(readStream.readLine()) / 1000));
			readStream.close();
			
			CommandCapture command = new CommandCapture(0, "cat /sys/class/thermal/thermal_zone0/temp") {
				int lineCount = 0;
				
				@Override
				public void commandOutput(int id, String line) {
					if (lineCount > 0) return;
					if (line != null) {
						curTemp = line.trim();
						int curTempInt = Integer.parseInt(curTemp);
						if (curTempInt >= 1000) curTemp = String.valueOf(Math.round(curTempInt / 1000));
					}
					lineCount++;
				}
			};
			RootTools.getShell(true).add(command);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	/*
	private static BroadcastReceiver mBRUSSD = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			try {
				String resp = intent.getStringExtra("response");
				if (resp == null || resp.equals(""))
					XposedBridge.log("Empty USSD response!");
				else if (dateView != null) {
					dateView.setText(resp);
					USSDState = 2;
				}
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
	*/
	private static TextView dateView;
	
	public static void execHook_NotifDrawerHeaderSysInfo(final LoadPackageParam lpparam) {
		String className = "com.android.systemui.statusbar.policy.DateView";
		if (Helpers.isLP()) className += "2";
		XposedBridge.hookAllConstructors(findClass(className, lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				dateView = (TextView)param.thisObject;
				OnClickListener ocl = new OnClickListener() {
					@Override
					public void onClick(View v) {
						//if (USSDState == 1) return;
						if (cpuThread != null && cpuThread.isAlive()) {
							Thread tmpThread = cpuThread;
							cpuThread = null;
							tmpThread.interrupt();
							isThreadActive = false;
							XposedHelpers.callMethod(param.thisObject, "updateClock");
						//} else if (USSDState == 2) {
						//	USSDState = 0;
						//	XposedHelpers.callMethod(param.thisObject, "updateClock");
						} else {
							cpuThread = new Thread(new Runnable() {
								public void run() {
									try {
										while (Thread.currentThread() == cpuThread) {
											readCPU();
											dateView.getHandler().post(new Runnable() {
												@Override
												public void run() {
													ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
													ActivityManager activityManager = (ActivityManager)dateView.getContext().getSystemService(Context.ACTIVITY_SERVICE);
													activityManager.getMemoryInfo(mi);
													long availableMegs = mi.availMem / 1048576L;
													long totalMegs = mi.totalMem / 1048576L;
													
													XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
													String MB = Helpers.xl10n(modRes, R.string.ram_mb);
													String MHz = Helpers.xl10n(modRes, R.string.cpu_mhz);
													dateView.setText("CPU " + String.valueOf(Math.round(workC * 100 / (float)totalC)) + "% " + String.valueOf(curFreq) + MHz + " " + curTemp + "\u00B0C" + "\n" + "RAM " + String.valueOf(availableMegs) + MB + " / " + String.valueOf(totalMegs) + MB);
												}
											});
											Thread.sleep(1000);
										}
									} catch (Throwable t) {}
								}
							});
							cpuThread.setName("s6t_thermal_cputemp");
							cpuThread.start();
							isThreadActive = true;
						}
					}
				};
				dateView.setOnClickListener(ocl);
				/*
				OnLongClickListener olcl = new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						if (cpuThread != null && cpuThread.isAlive()) {
							Thread tmpThread = cpuThread;
							cpuThread = null;
							tmpThread.interrupt();
							isThreadActive = false;
						}
						USSDState = 1;
						
						XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
						dateView.setText(Helpers.xl10n(modRes, R.string.header_longclick_ussd_processing));
						Intent ussdReqIntent = new Intent("com.sensetoolbox.six.USSD_REQ");
						ussdReqIntent.putExtra("number", "*102" + Uri.encode("#"));
						dateView.getContext().sendBroadcast(ussdReqIntent);
						return false;
					}
				};
				dateView.setOnLongClickListener(olcl);
				dateView.getContext().registerReceiver(mBRUSSD, new IntentFilter("com.sensetoolbox.six.USSD_RESP"));
				*/
			}
		});
		
		findAndHookMethod(className, lpparam.classLoader, "updateClock", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				if (isThreadActive/* || USSDState > 0*/) param.setResult(null);
			}
		});
	}
	/*
	public static void chooseClockApp(Context ctx) {
		Intent intentTimeSet = new Intent("android.intent.action.TIME_SET");
		Intent intentAlarmSet = new Intent("android.intent.action.SET_ALARM");
		Intent intentAllApps = new Intent("android.intent.action.MAIN");
		intentAllApps.addCategory("android.intent.category.LAUNCHER");
		
		PackageManager pm = ctx.getPackageManager();
		List<ResolveInfo> resInfoTime = pm.queryBroadcastReceivers(intentTimeSet, 0);
		List<ResolveInfo> resInfoAlarm = pm.queryIntentActivities(intentAlarmSet, 0);
		List<ResolveInfo> resInfoAllApps = pm.queryIntentActivities(intentAllApps, 0);
		
		List<String> pkgAlarm = new ArrayList<String>();
		for (ResolveInfo ri: resInfoAlarm) pkgAlarm.add(ri.activityInfo.packageName);
		
		List<ResolveInfo> resInfo = new ArrayList<ResolveInfo>();
		for (ResolveInfo ri: resInfoTime)
		if (pkgAlarm.contains(ri.activityInfo.packageName))	resInfo.add(ri);
		
		for (ResolveInfo ri: resInfoAllApps) {
			String pkgName = ri.activityInfo.applicationInfo.packageName;
			String label = ri.activityInfo.applicationInfo.loadLabel(pm).toString().toLowerCase(Locale.getDefault());
			if (pkgName.contains("clock") || pkgName.contains("alarm") || pkgName.contains("time") ||
				label.contains("clock") || label.contains("alarm") || label.contains("time")) resInfo.add(ri);
		}
		
		if (resInfo.size() == 0) {
			XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
			Toast.makeText(ctx, Helpers.xl10n(modRes, R.string.no_clock_apps), Toast.LENGTH_LONG).show();
			return;
		}
		
		HashMap<String,Intent> intentList = new HashMap<String,Intent>();
		String packageName0 = resInfo.get(0).activityInfo.applicationInfo.packageName;
		Intent intentClock0 = pm.getLaunchIntentForPackage(packageName0);
		
		for (int i = 1; i < resInfo.size(); i++) {
			ResolveInfo ri = resInfo.get(i);
			String packageName = ri.activityInfo.applicationInfo.packageName;
			Intent intentClock = pm.getLaunchIntentForPackage(packageName);
			if (intentClock != null && !packageName.equals(packageName0)) intentList.put(packageName, intentClock);
		}
		
		Intent openInChooser = Intent.createChooser(intentClock0, "Select app");
		Intent[] extraIntents = intentList.values().toArray(new Intent[intentList.size()]);
		openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
		ctx.startActivity(openInChooser);
	}
	*/
	public static void execHook_NotifDrawerHeaderClock(final InitPackageResourcesParam resparam, final int headerClock) {
		resparam.res.hookLayout("com.android.systemui", "layout", "status_bar_expanded_header", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				View clock;
				View date;
				if (Helpers.isLP()) {
					clock = liparam.view.findViewById(resparam.res.getIdentifier("header_clock", "id", "com.android.systemui"));
					date = liparam.view.findViewById(resparam.res.getIdentifier("header_date", "id", "com.android.systemui"));
					clock.setBackgroundResource(clock.getResources().getIdentifier("ripple_drawable", "drawable", "com.android.systemui"));
				} else {
					clock = liparam.view.findViewById(resparam.res.getIdentifier("clock", "id", "com.android.systemui"));
					date = liparam.view.findViewById(resparam.res.getIdentifier("date", "id", "com.android.systemui"));
				}
				
				final Intent clockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
				OnClickListener ocl = new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							ComponentName cn = new ComponentName("com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl");
							clockIntent.setComponent(cn);
							clockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							v.getContext().startActivity(clockIntent);
							
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
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				};
				clock.setOnClickListener(ocl);
				if (headerClock == 2) date.setOnClickListener(ocl);
			}
		});
	}
	
	// Listen for alarm changes and update label
	static class SystemSettingsObserver extends ContentObserver {
		Object thisObj = null;
		public SystemSettingsObserver(Handler h, Object paramThisObject) {
			super(h);
			thisObj = paramThisObject;
		}
		
		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}
		
		@Override
		@SuppressWarnings("deprecation")
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange);
			try {
				String uriPart = uri.getLastPathSegment();
				if (uriPart != null && uriPart.equals(Settings.System.NEXT_ALARM_FORMATTED))
				if (thisObj != null)
				if (Helpers.isLP())
					XposedHelpers.callMethod(thisObj, "update");
				else
					XposedHelpers.callMethod(thisObj, "triggerUpdate");
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	}
	
	private static void setLabel(TextView targetView, String text, int mod) {
		if (mod == 1) {
			targetView.setText(text);
		} else if (mod == 2 && !targetView.getText().toString().contains("dBm")) {
			targetView.setText(targetView.getText() + text);
		}
	}
	
	private static void updateLabel(Object paramThisObject) {
		try {
			XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
			Context mContext = (Context)XposedHelpers.getObjectField(paramThisObject, "mContext");
			if (mContext == null) return;
			String text = null;
			int mod = 0;
			
			String txt = Helpers.getNextAlarm(mContext);
			if (XMain.pref_alarmnotify && txt != null && !txt.equals("")) {
				text = Helpers.xl10n(modRes, R.string.next_alarm) + ": " + txt;
				mod = 1;
			} else if (XMain.pref_signalnotify) {
				text = getCurrentSignalLevel(mContext);
				mod = 2;
			}
			if (mod == 0) return;
			
			TextView mPlmn;
			TextView mSpn;
			if (Helpers.isLP()) {
				mPlmn = (TextView)XposedHelpers.getObjectField(paramThisObject, "mPlmnView");
				mSpn = (TextView)XposedHelpers.getObjectField(paramThisObject, "mSpnView");
				
				if (Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0 ||
				((TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE)).getSimState() != TelephonyManager.SIM_STATE_READY) mSpn.setText("");
			} else {
				mPlmn = (TextView)XposedHelpers.getObjectField(paramThisObject, "mPlmnLabel");
				mSpn = (TextView)XposedHelpers.getObjectField(paramThisObject, "mSpnLabel");
			}
			
			if (mSpn != null && mPlmn != null && !mSpn.getText().equals("") && !mPlmn.getText().equals("")) {
				mPlmn.setText("");
				setLabel(mSpn, text, mod);
			}
			else if (mSpn != null && !mSpn.getText().equals("")) setLabel(mSpn, text, mod);
			else if (mPlmn != null && !mPlmn.getText().equals("")) setLabel(mPlmn, text, mod);
			
			if (!Helpers.isLP()) {
				TextView mNetworkTextView = (TextView)XposedHelpers.getObjectField(paramThisObject, "mNetworkTextView");
				if (mNetworkTextView != null) setLabel(mNetworkTextView, text, mod);
				
				View vp = (View)((View)paramThisObject).getParent();
				if (vp != null) vp.invalidate();
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_LabelsUpdate(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.CarrierLabel", lpparam.classLoader, "updateNetworkName", boolean.class, String.class, boolean.class, String.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				updateLabel(param.thisObject);
			}
		});

		
		findAndHookMethod("com.android.systemui.statusbar.phone.CarrierLabel", lpparam.classLoader, "updateNetworkNameExt", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				updateLabel(param.thisObject);
			}
		});
		
		if (!Helpers.isLP()) {
			findAndHookMethod("com.android.systemui.statusbar.phone.CarrierLabel", lpparam.classLoader, "updateAirplaneMode", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					updateLabel(param.thisObject);
				}
			});
		}
	}
	
	public static void execHook_AlarmNotification(LoadPackageParam lpparam) {
		XposedBridge.hookAllConstructors(findClass("com.android.systemui.statusbar.phone.CarrierLabel", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					if (mContext != null)
					mContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, new SystemSettingsObserver(new Handler(), param.thisObject));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
	
	public static class SignalListener extends PhoneStateListener {
		Object thisObj = null;
		public SignalListener(Object paramThisObject) {
			super();
			thisObj = paramThisObject;
		}
		
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			if (thisObj != null) {
				lastSignalStrength = signalStrength;
				if (Helpers.isLP())
					XposedHelpers.callMethod(thisObj, "update");
				else
					XposedHelpers.callMethod(thisObj, "triggerUpdate");
			}
		}
	}
	
	private static SignalStrength lastSignalStrength = null;
	private static String getCurrentSignalLevel(Context ctx) {
		if (lastSignalStrength == null || Settings.Global.getInt(ctx.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {
			lastSignalStrength = null;
			return "";
		}
		TelephonyManager telMgr = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		if (telMgr.getSimState() != TelephonyManager.SIM_STATE_READY) {
			lastSignalStrength = null;
			return "";
		}
		
		int asu = (Integer)XposedHelpers.callMethod(lastSignalStrength, "getAsuLevel");
		int dBm = (Integer)XposedHelpers.callMethod(lastSignalStrength, "getDbm");
		return "  " + String.valueOf(dBm) + " dBm " + String.valueOf(asu) + " asu";
	}
	
	public static void execHook_SignalNotification(LoadPackageParam lpparam) {
		XposedBridge.hookAllConstructors(findClass("com.android.systemui.statusbar.phone.CarrierLabel", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					if (mContext != null) {
						SignalListener signalListener = new SignalListener(param.thisObject);
						TelephonyManager telephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
						telephonyManager.listen(signalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
	
	public static void execHookTSB442Fix(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarTransitions", lpparam.classLoader, "transitionTo", int.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.args[0] = 2;
				}
			});
		} catch (Throwable ignore) {}
	}
	
	public static void execHook_anotherTSB44Fix(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.android.internal.policy.impl.BarController", lpparam.classLoader, "applyTranslucentFlagLw", "android.view.WindowManagerPolicy.WindowState", int.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					Object win = param.args[0];
					if (win != null && win.toString().contains("Keyguard"))
						param.setResult(param.args[2]);
				}
			});
		} catch (Throwable ignore) {}
	}
	
	public static void execHook_OverrideAssist(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "showSearchPanel", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					ControlsMods.assistAndSearchPanelOverride(param);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void replaceTheme(MethodHookParam param, int style, Theme theme) {
		if (Helpers.allStyles.contains(style)) {
			PackageTheme pt = Helpers.getThemeForPackageFromXposed(((Context)param.thisObject).getPackageName());
			if (pt != null) {
				if (theme != null) {
					theme.applyStyle(Helpers.colors.keyAt(pt.getTheme()), true);
					param.setResult(null);
				} else param.args[0] = Helpers.colors.keyAt(pt.getTheme());
			}
		}
	}
	
	public static void execHook_Sense6ColorControl() {
		try {
			// Overlay replacement
			/*
			findAndHookMethod("com.htc.util.skin.HtcSkinUtil", null, "getSkinResId", Context.class, String.class, int.class, String.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Context ctx = (Context)param.args[0];
					String pkgName = ctx.getPackageName();
					String resName = (String)param.args[1];
					String resType = (String)param.args[3];
					
					PackageTheme pt = Helpers.getThemeForPackageFromXposed(pkgName);
					if (pt != null && resType.equals("color") && resName.equals("overlay_color")) {
						Helpers.colors.keyAt(pt.getTheme());
						param.setResult(null);
					}
				}
			});
			*/
			findAndHookMethod("android.app.ContextImpl", null, "setTheme", int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					replaceTheme(param, (Integer)param.args[0], null);
				}
			});
			
			findAndHookMethod("android.view.ContextThemeWrapper", null, "onApplyThemeResource", Theme.class, int.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					replaceTheme(param, (Integer)param.args[1], (Theme)param.args[0]);
				}
			});
			
			findAndHookMethod("com.htc.configuration.HtcWrapConfiguration", null, "getHtcThemeId", Context.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Context ctx = (Context)param.args[0];
					if (ctx != null)
					if (ctx.getPackageName().equals("android")) {
						PackageTheme pt = Helpers.getThemeForPackageFromXposed("android");
						if (pt != null) param.setResult(Helpers.colors.keyAt(pt.getTheme()));
					} else if (ctx.getPackageName().equals("com.htc.camera")) {
						PackageTheme pt = Helpers.getThemeForPackageFromXposed("com.htc.camera");
						if (pt != null) param.setResult(Helpers.colors.keyAt(pt.getTheme()));
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static void replaceCustom(MethodHookParam param, String pkgName) {
		try {
			PackageTheme pt = Helpers.getThemeForPackageFromXposed(pkgName);
			if (pt != null) {
				Context ctx = (Context)param.args[0];
				String htc_theme = (String)Helpers.colors.valueAt(pt.getTheme())[0];
				int htc_theme_id = ctx.getResources().getIdentifier(htc_theme, "style", pkgName);
				if (htc_theme_id != 0) param.setResult(htc_theme_id);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static void replaceCustomIME(MethodHookParam param, String pkgName) {
		try {
			Context ctx = (Context)param.args[0];
			if (ctx == null) return;
			final ActivityManager amgr = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
			@SuppressWarnings("deprecation")
			final List<ActivityManager.RunningTaskInfo> taskInfo = amgr.getRunningTasks(1);
			if (taskInfo.size() == 0 || taskInfo.get(0).topActivity == null) return;
			String appPkgName = taskInfo.get(0).topActivity.getPackageName();
			
			PackageTheme pt1 = Helpers.getThemeForPackageFromXposed(appPkgName);
			String htc_theme = "";
			if (pt1 != null) {
				htc_theme = (String)Helpers.colors.valueAt(pt1.getTheme())[0];
			} else {
				PackageTheme pt2 = Helpers.getThemeForPackageFromXposed(pkgName);
				if (pt2 != null) htc_theme = (String)Helpers.colors.valueAt(pt2.getTheme())[0];
			}
			if (htc_theme != "") {
				int htc_theme_id = ctx.getResources().getIdentifier(htc_theme, "style", pkgName);
				if (htc_theme_id != 0) param.setResult(htc_theme_id);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_Sense6ColorControlCustom(final LoadPackageParam lpparam, final String pkgName) {
		if (pkgName.equals("com.htc.sense.ime")) {
			findAndHookMethod("com.htc.sense.ime.HTCIMMData", lpparam.classLoader, "getThemeId", Context.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					replaceCustomIME(param, pkgName);
				}
			});
			// Proguarded piece of shit!
			findAndHookMethod("com.htc.lib1.cc.c.b", lpparam.classLoader, "a", Context.class, int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					replaceCustomIME(param, pkgName);
				}
			});
			
			findAndHookMethod("com.htc.sense.ime.HTCIMEService", lpparam.classLoader, "onShowInputRequested", int.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.callStaticMethod(findClass("com.htc.sense.ime.HTCIMMData", lpparam.classLoader), "setThemeContext", param.thisObject);
				}
			});
		} else {
			try {
				findAndHookMethod("com.htc.lib1.cc.util.HtcCommonUtil", lpparam.classLoader, "getHtcThemeId", Context.class, int.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						replaceCustom(param, pkgName);
					}
				});
			} catch (Throwable t1) {
				try {
					findAndHookMethod("com.htc.lib1.cc.c.c", lpparam.classLoader, "a", Context.class, int.class, new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							replaceCustom(param, pkgName);
						}
					});
				} catch (Throwable t2) {
					try {
						findAndHookMethod("com.htc.lib1.cc.d.c", lpparam.classLoader, "a", Context.class, int.class, new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								replaceCustom(param, pkgName);
							}
						});
					} catch (Throwable t3) {
						try {
							findAndHookMethod("com.htc.lib1.cc.c.b", lpparam.classLoader, "a", Context.class, int.class, new XC_MethodHook() {
								@Override
								protected void afterHookedMethod(MethodHookParam param) throws Throwable {
									replaceCustom(param, pkgName);
								}
							});
						} catch (Throwable t4) {}
					}
				}
			}
		}
	}
	
	public static void execHook_ChangeBrightnessQSTile(LoadPackageParam lpparam) {
		try {
			String className = "com.android.systemui.statusbar.quicksetting.QuickSettingBrightness";
			if (Helpers.isLP()) className = "com.android.systemui.qs.tiles.QuickSettingBrightness";
			final Class<?> QSB = findClass(className, lpparam.classLoader);
			XposedBridge.hookAllConstructors(QSB, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					int valnorm1 = Math.round(255 * XMain.pref.getInt("pref_key_sysui_brightqs_value1", 10) / 100 + 1); if (valnorm1 > 255) valnorm1 = 255;
					int valnorm2 = Math.round(255 * XMain.pref.getInt("pref_key_sysui_brightqs_value2", 40) / 100 + 1); if (valnorm2 > 255) valnorm2 = 255;
					int valnorm3 = Math.round(255 * XMain.pref.getInt("pref_key_sysui_brightqs_value3", 60) / 100 + 1); if (valnorm3 > 255) valnorm3 = 255;
					int valnorm4 = Math.round(255 * XMain.pref.getInt("pref_key_sysui_brightqs_value4", 100) / 100 + 1); if (valnorm4 > 255) valnorm4 = 255;
					XposedHelpers.setStaticObjectField(QSB, "BRIGHTNESS_LEVEL", new int[] { valnorm1, valnorm2, valnorm3, valnorm4 });
					XposedHelpers.setStaticObjectField(QSB, "BRIGHTNESS_VALUE", new int[] { valnorm1, valnorm2, valnorm3, valnorm4 });
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static int numberToTimeout(int num) {
		int timeout = 15000;
		switch (num) {
			case 0: timeout = 15000; break;
			case 1: timeout = 30000; break;
			case 2: timeout = 45000; break;
			case 3: timeout = 60000; break;
			case 4: timeout = 120000; break;
			case 5: timeout = 600000; break;
			case 6: timeout = 1800000; break;
			case 7: timeout = 3600000; break;
		}
		return timeout;
	}
	
	public static void execHook_ChangeTimeoutQSTile(LoadPackageParam lpparam) {
		try {
			String className = "com.android.systemui.statusbar.quicksetting.QuickSettingTimeout";
			if (Helpers.isLP()) className = "com.android.systemui.qs.tiles.QuickSettingTimeout";
			final Class<?> QST = findClass(className, lpparam.classLoader);
			XposedBridge.hookAllConstructors(QST, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.setStaticObjectField(QST, "timeoutList", new int[] {
						numberToTimeout(XMain.pref.getInt("pref_key_sysui_timeoutqs_value1", 7)),
						numberToTimeout(XMain.pref.getInt("pref_key_sysui_timeoutqs_value2", 6)),
						numberToTimeout(XMain.pref.getInt("pref_key_sysui_timeoutqs_value3", 4))
					});
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static int getRotation(MethodHookParam param) {
		Context ctx = ((View)param.thisObject).getContext();
		WindowManager wm = (WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay().getRotation();
	}
	
	private static int getShortcutRes(int shortcut, boolean isLeft) {
		int sRes = R.drawable.ic_action_apm;
		if (isLeft) sRes = R.drawable.ic_action_voicedial;
		switch (shortcut) {
			case 1:
				sRes = R.drawable.ic_action_apm;
				break;
			case 2:
				sRes = R.drawable.ic_action_voicedial;
				break;
			case 3:
				sRes = R.drawable.ic_action_blinkfeed;
				break;
			case 4:
				sRes = R.drawable.ic_action_sleep;
				break;
			case 5:
				sRes = R.drawable.ic_action_lock;
				break;
		}
		return sRes;
	}
	
	public static void execHook_SearchGlowPad() {
		try {
			XposedBridge.hookAllConstructors(findClass("com.android.internal.widget.multiwaveview.GlowPadView", null), new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.setFloatField(param.thisObject, "mFirstItemOffset", 0);
					XposedHelpers.setIntField(param.thisObject, "mFeedbackCount", 1);
					XposedHelpers.setBooleanField(param.thisObject, "mAllowScaling", false);
				}
			});
			
			findAndHookMethod("com.android.internal.widget.multiwaveview.GlowPadView", null, "getSliceAngle", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult((float)Math.toRadians(-45));
				}
			});
			
			findAndHookMethod("com.android.internal.widget.multiwaveview.GlowPadView", null, "loadDrawableArray", int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
					Class<?> TargetDrawable = findClass("com.android.internal.widget.multiwaveview.TargetDrawable", null);
					
					Object stock_assist = TargetDrawable.getConstructor(Resources.class, int.class).newInstance(modRes, R.drawable.ic_action_assist_generic);
					Object dummy = TargetDrawable.getConstructor(Resources.class, int.class).newInstance(modRes, 0);
					
					XMain.pref.reload();
					int leftShortcut = Integer.parseInt(XMain.pref.getString("pref_key_controls_extendedpanel_left", "2"));
					int rightShortcut = Integer.parseInt(XMain.pref.getString("pref_key_controls_extendedpanel_right", "1"));
					Object leftObject = TargetDrawable.getConstructor(Resources.class, int.class).newInstance(modRes, getShortcutRes(leftShortcut, true));
					Object rightObject = TargetDrawable.getConstructor(Resources.class, int.class).newInstance(modRes, getShortcutRes(rightShortcut, false));
					
					ArrayList<Object> arraylist = new ArrayList<Object>();
					int rot = getRotation(param);
					if (rot == 1) {
						arraylist.add(dummy);
						arraylist.add(dummy);
						arraylist.add(dummy);
						arraylist.add(rightObject);
						arraylist.add(stock_assist);
						arraylist.add(leftObject);
						arraylist.add(dummy);
						arraylist.add(dummy);
					} else if (rot == 3) {
						arraylist.add(stock_assist);
						arraylist.add(leftObject);
						arraylist.add(dummy);
						arraylist.add(dummy);
						arraylist.add(dummy);
						arraylist.add(dummy);
						arraylist.add(dummy);
						arraylist.add(rightObject);
					} else {
						arraylist.add(dummy);
						arraylist.add(rightObject);
						arraylist.add(stock_assist);
						arraylist.add(leftObject);
						arraylist.add(dummy);
						arraylist.add(dummy);
						arraylist.add(dummy);
						arraylist.add(dummy);
					}
					
					param.setResult(arraylist);
				}
			});
			
			findAndHookMethod("com.android.internal.widget.multiwaveview.GlowPadView", null, "loadDescriptions", int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					ArrayList<String> arraylist = new ArrayList<String>();
					arraylist.add("");
					arraylist.add("");
					arraylist.add("");
					arraylist.add("");
					arraylist.add("");
					arraylist.add("");
					arraylist.add("");
					arraylist.add("");
					param.setResult(arraylist);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_SearchGlowPadLaunch(final LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.android.systemui.SearchPanelView.GlowPadTriggerListener", lpparam.classLoader, "onTrigger", View.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					int item = (Integer)param.args[1];
					View mGlowPadView = (View)param.args[0];
					if (mGlowPadView == null) return;
					Context ctx = mGlowPadView.getContext();
					int resId = (Integer)XposedHelpers.callMethod(mGlowPadView, "getResourceIdForTarget", item);
					
					switch (resId) {
						case R.drawable.ic_action_apm:
							Class<?> ActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
							Object activityManagerNative = XposedHelpers.callStaticMethod(ActivityManagerNative, "getDefault");
							XposedHelpers.callMethod(activityManagerNative, "dismissKeyguardOnNextActivity");
							OtherMods.startAPM(ctx);
							break;
						case R.drawable.ic_action_voicedial:
							Intent intent = new Intent("com.htc.HTCSpeaker.HtcSpeakLauncher_QuickCall");
							intent.setFlags(0x50000000);
							intent.putExtra("LaunchBy", "LockScreen");
							KeyguardManager kgm = (KeyguardManager)ctx.getSystemService(Context.KEYGUARD_SERVICE);
							intent.putExtra("isKeyguardShow", kgm.inKeyguardRestrictedInputMode());
							intent.putExtra("isKeyguardSecure", kgm.isKeyguardSecure());
							intent.putExtra("isLockscreenDisable", !kgm.isKeyguardLocked());
							ctx.startActivity(intent);
							break;
						case R.drawable.ic_action_blinkfeed:
							Class<?> ActivityManagerNative2 = Class.forName("android.app.ActivityManagerNative");
							Object activityManagerNative2 = XposedHelpers.callStaticMethod(ActivityManagerNative2, "getDefault");
							XposedHelpers.callMethod(activityManagerNative2, "dismissKeyguardOnNextActivity");
							Intent i = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("action", 0);
							ctx.startActivity(i);
							break;
						case R.drawable.ic_action_lock:
							ctx.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.LockDevice"));
							break;
						case R.drawable.ic_action_sleep:
							ctx.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.GoToSleep"));
							break;
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_HDThumbnails(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.android.server.wm.WindowManagerService", lpparam.classLoader, "screenshotApplications", IBinder.class, int.class, int.class, int.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.args[4] = false;
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_NoLowBatteryWarning(LoadPackageParam lpparam) {
		try {
			if (Helpers.isLP()) {
				findAndHookMethod("com.android.systemui.power.PowerNotificationWarnings", lpparam.classLoader, "showWarningNotification", XC_MethodReplacement.DO_NOTHING);
				findAndHookMethod("com.android.systemui.power.PowerNotificationWarnings", lpparam.classLoader, "startLowBatteryTone", XC_MethodReplacement.DO_NOTHING);
			} else {
				findAndHookMethod("com.android.systemui.power.PowerUI", lpparam.classLoader, "showLowBatteryWarningWithLevel", int.class, XC_MethodReplacement.DO_NOTHING);
				findAndHookMethod("com.android.systemui.power.PowerUI", lpparam.classLoader, "startLowBatteryTone", XC_MethodReplacement.DO_NOTHING);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_TranslucentNotifications(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.BaseStatusBar", lpparam.classLoader, "createNotificationViews", IBinder.class, StatusBarNotification.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Object entry = param.getResult();
				StatusBarNotification sbn = (StatusBarNotification)param.args[1];
				if (entry == null) return;
				
				View content = (View)XposedHelpers.getObjectField(entry, "content");
				if (content != null) {
					content.setBackgroundColor(Color.TRANSPARENT);
					content.destroyDrawingCache();
					content.invalidate();
				}
				
				View row = (View)XposedHelpers.getObjectField(entry, "row");
				if (row != null) {
					ViewGroup adaptive = (ViewGroup)row.findViewById(row.getResources().getIdentifier("adaptive", "id", "com.android.systemui"));
					if (adaptive != null && adaptive.getChildCount() > 1) {
						View adaptiveChild = adaptive.getChildAt(1);
						if (adaptiveChild.getBackground() != null)
						if (adaptiveChild instanceof LinearLayout) {
							LinearLayout adaptiveLL = (LinearLayout)adaptiveChild;
							adaptiveLL.setBackgroundColor(Color.TRANSPARENT);
							adaptiveLL.destroyDrawingCache();
							adaptiveLL.invalidate();
						} else if (adaptiveChild instanceof FrameLayout) {
							FrameLayout adaptiveFL = (FrameLayout)adaptiveChild;
							adaptiveFL.setBackgroundColor(Color.TRANSPARENT);
							adaptiveFL.destroyDrawingCache();
							adaptiveFL.invalidate();
						}
					}
						
					ArrayList<View> nViews = Helpers.getChildViewsRecursive(row);
					for (View nView: nViews)
					if (nView != null && nView.getResources() != null && nView.getId() > 0 && nView.getId() != 0xffffffff) try {
						String name = nView.getResources().getResourceEntryName(nView.getId());
						if (nView.getBackground() != null && nView.getVisibility() == 0 && !name.contains("glow") && !name.contains("icon")) {
							nView.setBackgroundColor(Color.TRANSPARENT);
							nView.destroyDrawingCache();
							nView.invalidate();
						}
						
						if (sbn != null && name.contains("icon") && nView.getClass() == ImageView.class) {
							ImageView icon = (ImageView)nView;
							Context ctx = icon.getContext();
							icon.setBackground(null);
							icon.setImageDrawable(null);
							Bitmap largeIcon = sbn.getNotification().largeIcon;
							if (largeIcon != null) {
								int dimen = densify(ctx, 64);
								if (largeIcon.getWidth() > dimen || largeIcon.getHeight() > dimen) {
									Bitmap newBmp = Bitmap.createScaledBitmap(sbn.getNotification().largeIcon, dimen, dimen, false);
									icon.setImageBitmap(newBmp);
								} else {
									icon.setImageBitmap(largeIcon);
								}
							} else {
								icon.setImageResource(sbn.getNotification().icon);
							}
						}
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			}
		});
	}
	
	public static void execHook_TranslucentNotificationsDividers() {
		try {
			XResources.hookSystemWideLayout("android", "layout", "notification_action_list", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					LinearLayout nal = (LinearLayout)liparam.view;
					if (nal != null) nal.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_TranslucentNotificationsTV(InitPackageResourcesParam resparam) {
		try {
			resparam.res.setReplacement("com.htc.videohub.ui", "drawable", "notification_bg", Color.TRANSPARENT);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_TranslucentEQS(InitPackageResourcesParam resparam) {
		try {
			if (Helpers.isLP()) {
				resparam.res.setReplacement("com.android.systemui", "color", "notification_panel_color", Color.TRANSPARENT);
			} else {
				resparam.res.setReplacement("com.android.systemui", "drawable", "quick_settings_tile_background", Color.TRANSPARENT);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_TranslucentHorizEQS(InitPackageResourcesParam resparam) {
		try {
			resparam.res.setReplacement("com.android.systemui", "drawable", "quick_settings_minor_container_background", Color.TRANSPARENT);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_TranslucentHorizEQSCode(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				FrameLayout mStatusBarWindow = (FrameLayout) getObjectField(param.thisObject, "mStatusBarWindow");
				if (mStatusBarWindow != null) {
					LinearLayout qsMinorContainer = (LinearLayout)mStatusBarWindow.findViewById(mStatusBarWindow.getResources().getIdentifier("quick_settings_minor_container", "id", "com.android.systemui"));
					if (qsMinorContainer != null) qsMinorContainer.setShowDividers(0);
				}
			}
		});
	}
	
	private static ImageView createIcon(Context ctx, int baseSize) {
		float density = ctx.getResources().getDisplayMetrics().density;
		ImageView iv = new ImageView(ctx);
		try {
			iv.setImageDrawable(ctx.getPackageManager().getApplicationIcon(ctx.getPackageName()));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		iv.setScaleType(ScaleType.CENTER_INSIDE);
		int size = Math.round(baseSize * density);
		LinearLayout.LayoutParams lpi = new LinearLayout.LayoutParams(size, size);
		if (baseSize > 22)
			lpi.setMargins(0, Math.round(1 * density), Math.round(8 * density), 0);
		else
			lpi.setMargins(0, 0, Math.round(8 * density), 0);
		lpi.gravity = Gravity.CENTER;
		iv.setLayoutParams(lpi);
			
		return iv;
	}
	
	private static TextView createLabel(Context ctx, TextView toastText) {
		TextView tv = new TextView(ctx);
		tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		tv.setText(ctx.getApplicationInfo().loadLabel(ctx.getPackageManager()) + ":");
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, toastText.getTextSize());
		tv.setTypeface(toastText.getTypeface());
		tv.setSingleLine(true);
		tv.setAlpha(0.6f);
		return tv;
	}
	
	public static void execHook_IconLabelToasts() {
		XResources.hookSystemWideLayout("android", "layout", "transient_notification", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				XMain.pref.reload();
				int option = Integer.parseInt(XMain.pref.getString("pref_key_other_iconlabletoasts", "1"));
				if (option == 1) return;
				Context ctx = liparam.view.getContext();
				float density = ctx.getResources().getDisplayMetrics().density;
				
				TextView toastText = (TextView)liparam.view.findViewById(android.R.id.message);
				LinearLayout.LayoutParams lpt = (LinearLayout.LayoutParams)toastText.getLayoutParams();
				lpt.gravity = Gravity.START;
				
				LinearLayout toast = ((LinearLayout)liparam.view);
				toast.setGravity(Gravity.START);
				toast.setPadding(toast.getPaddingLeft() - Math.round(5 * density), toast.getPaddingTop(), toast.getPaddingRight(), toast.getPaddingBottom());
				
				switch (option) {
					case 2:
						LinearLayout textOnly = new LinearLayout(ctx);
						textOnly.setOrientation(LinearLayout.VERTICAL);
						textOnly.setGravity(Gravity.START);
						ImageView iv = createIcon(ctx, 22);
						
						((LinearLayout)toastText.getParent()).removeAllViews();
						textOnly.addView(toastText);
						toast.setOrientation(LinearLayout.HORIZONTAL);
						toast.addView(iv);
						toast.addView(textOnly);
						break;
					case 3:
						TextView tv = createLabel(ctx, toastText);
						toast.setOrientation(LinearLayout.VERTICAL);
						toast.addView(tv, 0);
						break;
					case 4:
						LinearLayout textLabel = new LinearLayout(ctx);
						textLabel.setOrientation(LinearLayout.VERTICAL);
						textLabel.setGravity(Gravity.START);
						ImageView iv2 = createIcon(ctx, 45);
						TextView tv2 = createLabel(ctx, toastText);
						
						((LinearLayout)toastText.getParent()).removeAllViews();
						textLabel.addView(tv2);
						textLabel.addView(toastText);
						toast.setOrientation(LinearLayout.HORIZONTAL);
						toast.addView(iv2);
						toast.addView(textLabel);
						break;
				}
			}
		});
	}
	
	public static void execHook_DrawerFooterDynamicAlpha(LoadPackageParam lpparam, final int pref_footer) {
		findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "draw", Canvas.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				FrameLayout panelView = (FrameLayout)param.thisObject;
				if (panelView == null) return;
				View mHandleView = (View)XposedHelpers.getObjectField(panelView, "mHandleView");
				if (mHandleView != null && mHandleView.getBackground() != null) {
					if (pref_footer == 3) {
						mHandleView.getBackground().setAlpha(0);
					} else if (pref_footer == 2) {
						float drawerHeight = (float)panelView.getMeasuredHeight();
						float headerHeight = 85 * panelView.getResources().getDisplayMetrics().density;
						
						if (drawerHeight <= headerHeight)
							mHandleView.getBackground().setAlpha(255);
						else
							mHandleView.getBackground().setAlpha(Math.round(255f * (1.0f - (drawerHeight - headerHeight) / ((float)panelView.getResources().getDisplayMetrics().heightPixels - headerHeight))));
					}
				}
			}
		});
	}
	
	public static BroadcastReceiver mBRScrDelete = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			try {
				Uri uri = intent.getParcelableExtra("screenshot_file");
				if (uri != null) context.getContentResolver().delete(uri, null, null);
				((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(789);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};

	public static void execHook_ScreenshotDelete(final LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.screenshot.SaveImageInBackgroundTask", lpparam.classLoader, "doInBackground", "com.android.systemui.screenshot.SaveImageInBackgroundData[]", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Notification.Builder mNotificationBuilder = (Notification.Builder)XposedHelpers.getObjectField(param.thisObject, "mNotificationBuilder");
				if (mNotificationBuilder != null) try {
					Object[] saveImageData = (Object[])param.args[0];
					Context ctx = (Context)XposedHelpers.getObjectField(saveImageData[0], "context");
					Uri uri = (Uri)XposedHelpers.getObjectField(saveImageData[0], "imageUri");
					if (ctx != null && uri != null) {
						XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
						Intent intent = new Intent("com.sensetoolbox.six.DELETE_SCREENSHOT");
						intent.putExtra("screenshot_file", uri);
						mNotificationBuilder.addAction(ctx.getResources().getIdentifier("icon_btn_delete_light", "drawable", "com.htc"), Helpers.xl10n(modRes, R.string.delete), PendingIntent.getBroadcast(ctx, 0, intent, 0x10000000));
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
		
		findAndHookMethod("com.android.systemui.screenshot.ScreenshotService", lpparam.classLoader, "start", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				mContext.registerReceiver(mBRScrDelete, new IntentFilter("com.sensetoolbox.six.DELETE_SCREENSHOT"));
			}
		});
	}
	
	public static void execHook_EQSTiles(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.qs.QSTileView", lpparam.classLoader, "onLayout", boolean.class, int.class, int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				LinearLayout qstileview = (LinearLayout)param.thisObject;
				float density = qstileview.getResources().getDisplayMetrics().density;
				
				qstileview.setPadding(qstileview.getPaddingLeft(), qstileview.getPaddingTop(), qstileview.getPaddingRight(), Math.round(10 * density));
				
				View quick_setting_footer = qstileview.findViewById(qstileview.getResources().getIdentifier("quick_setting_footer", "id", "com.android.systemui"));
				if (quick_setting_footer != null) quick_setting_footer.setVisibility(View.GONE);
				View quick_setting_text = qstileview.findViewById(qstileview.getResources().getIdentifier("quick_setting_text", "id", "com.android.systemui"));
				if (quick_setting_text != null) quick_setting_text.setVisibility(View.GONE);
				
				View quick_setting_image = qstileview.findViewById(qstileview.getResources().getIdentifier("quick_setting_image", "id", "com.android.systemui"));
				if (quick_setting_image != null) quick_setting_image.setPadding(quick_setting_image.getPaddingLeft(), Math.round(7 * density), quick_setting_image.getPaddingRight(), Math.round(10 * density));
				
				View quick_setting_indicator = qstileview.findViewById(qstileview.getResources().getIdentifier("quick_setting_indicator", "id", "com.android.systemui"));
				if (quick_setting_indicator != null) quick_setting_indicator.setPadding(0, 0, 0, 0);
			}
		});
	}
	
	public static void execHook_EQSGrid(InitPackageResourcesParam resparam) {
		XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "integer", "quick_settings_num_columns", modRes.fwd(R.integer.quick_settings_num_columns));
		resparam.res.setReplacement("com.android.systemui", "integer", "quick_settings_max_rows", modRes.fwd(R.integer.quick_settings_max_rows));
		resparam.res.setReplacement("com.android.systemui", "integer", "quick_settings_max_rows_keyguard", modRes.fwd(R.integer.quick_settings_max_rows_keyguard));
		resparam.res.setReplacement("com.android.systemui", "dimen", "quick_settings_cell_height", modRes.fwd(R.dimen.quick_settings_cell_height));
	}
}

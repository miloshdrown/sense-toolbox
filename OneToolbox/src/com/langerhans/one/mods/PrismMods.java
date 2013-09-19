package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setIntField;
import static de.robv.android.xposed.XposedHelpers.setStaticIntField;

import java.lang.reflect.Method;
import java.util.Arrays;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.langerhans.one.R;
import com.langerhans.one.utils.GlobalActions;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodHook.Unhook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class PrismMods {
	
	public static Object mPWM = null;
	
	static Unhook onclickOption = null;
	public static int gridSizeVal = 0;
	private static GestureDetector mDetector;
	private static GestureDetector mDetectorDock;
	//private static GestureDetector mDetectorWP;
	
	public static void execHook_InvisiNav(final InitPackageResourcesParam resparam, final int transparency, String MODULE_PATH) {
		
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.htc.launcher", "drawable", "home_nav_bg", new XResources.DrawableLoader() {
			@Override
			public Drawable newDrawable(XResources res, int id) throws Throwable {
				Drawable bg = modRes.getDrawable(R.drawable.home_nav_bg);
				bg.setAlpha(transparency);
				return bg;
			}
		});
	}
	
	public static void execHook_InvisiWidget(final InitPackageResourcesParam resparam, final int transparency, String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.htc.widget.weatherclock", "drawable", "clock_weather_panel", new XResources.DrawableLoader() {
			@Override
			public Drawable newDrawable(XResources res, int id) throws Throwable {
				Drawable bg = modRes.getDrawable(R.drawable.clock_weather_panel);
				bg.setAlpha(transparency);
				return bg;
			}
		});
	}

	public static void execHook_PreserveWallpaper(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "updateWallpaperVisibility", boolean.class, XC_MethodReplacement.DO_NOTHING);
	}
		
	public static void execHook_20Folder_code(final LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.folder.Folder", lpparam.classLoader, "isFull", new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(false);
			}
		});
		
		findAndHookMethod("com.htc.launcher.folder.Folder", lpparam.classLoader, "isFull", findClass("com.htc.launcher.folder.FolderInfo", lpparam.classLoader), new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(false);
			}
		});
		
		XposedBridge.hookAllConstructors(findClass("com.htc.launcher.folder.Folder", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				setBooleanField(param.thisObject, "m_bMultiplePage", true);		
				setStaticIntField(param.thisObject.getClass(), "FOLDER_MAX_COUNT", 9999);
			}
		});
		
		findAndHookMethod("com.htc.launcher.folder.Folder", lpparam.classLoader, "setMultiplePage", boolean.class, new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
		
		findAndHookMethod("com.htc.launcher.pageview.CheckedAppsDataManager", lpparam.classLoader, "setMaxCheckedAmount", int.class, new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				setIntField(param.thisObject, "m_MaxCheckedAmount", 9999);
			}
		});
	}

	public static void execHookTSBFix(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.bar.BarController", lpparam.classLoader, "setStatusBarTransparent", boolean.class, new XC_MethodHook() {
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(null);
			}
		});
	}
	
	public static void execHook_InvisiFolder(final InitPackageResourcesParam resparam, final int transparency) {
		resparam.res.hookLayout("com.htc.launcher", "layout", "user_folder", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				RelativeLayout bg = (RelativeLayout)liparam.view;
				bg.getBackground().setAlpha(transparency);
				LinearLayout nameframe = (LinearLayout)liparam.view.findViewById(resparam.res.getIdentifier("folder_name_frame", "id", "com.htc.launcher"));
				if (nameframe != null) {
					RelativeLayout.LayoutParams lp =  (RelativeLayout.LayoutParams)nameframe.getLayoutParams();
					lp.rightMargin = 3;
					nameframe.setLayoutParams(lp);
					nameframe.setBackgroundColor(Color.argb(255, 20, 20, 20));
				}
			}
		});
	}
	
	public static void execHook_InvisiFolderBkg(final InitPackageResourcesParam resparam, final int transparency,  String MODULE_PATH) {
		final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.htc.launcher", "drawable", "home_folder_base", new XResources.DrawableLoader() {
			@Override
			public Drawable newDrawable(XResources res, int id) throws Throwable {
				try {
					Drawable bg = modRes.getDrawable(R.drawable.home_folder_base);
					bg.setAlpha(transparency);
					return bg;
				} catch(Exception e){
					//XposedBridge.log("[S5T] Resource loading bug... Need full restart");
					return null;
				}
			}
		});
	}
	
	public static void execHook_InvisiDrawerCode(LoadPackageParam lpparam, final int transparency) {
		execHook_PreserveWallpaper(lpparam);
		
		findAndHookMethod("com.htc.launcher.pageview.AllAppsPagedViewHost", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				ViewGroup m_PagedView = (ViewGroup)XposedHelpers.findField(param.thisObject.getClass(), "m_PagedView").get(param.thisObject);
				if (m_PagedView.getParent() != null)
				((RelativeLayout)m_PagedView.getParent()).getBackground().setAlpha(transparency);
			}
		});
		
		try {
			// This will fail on 4.2.2, best version check ever!
			XposedHelpers.findMethodExact("com.htc.launcher.masthead.Masthead", lpparam.classLoader, "updateActionbarPosition");
		} catch (NoSuchMethodError e){
			findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "showAllApps", boolean.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					ViewGroup m_workspace = (ViewGroup)XposedHelpers.findField(param.thisObject.getClass(), "m_workspace").get(param.thisObject);
					m_workspace.setVisibility(4);
				}
			});
		}						
	}
	
	// Move Action Bar
	private static void moveAB(MethodHookParam param) throws Throwable {
		FrameLayout m_headerActionBar = (FrameLayout)XposedHelpers.findField(param.thisObject.getClass(), "m_headerActionBar").get(param.thisObject);
		if (m_headerActionBar != null) {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)m_headerActionBar.getLayoutParams();
			
			Object container = ((FrameLayout)param.thisObject).getParent();
			if (container.getClass().getCanonicalName().equalsIgnoreCase("com.htc.launcher.feeds.view.FeedScrollView")) {
				Resources res = m_headerActionBar.getContext().getResources();
				lp.topMargin = res.getDimensionPixelSize(res.getIdentifier("header_height", "dimen", "com.htc.launcher"));
			} else {			
				lp.topMargin = 0;
			}
			m_headerActionBar.setLayoutParams(lp);
		}
	}

	public static void execHook_AppDrawerNoClock(final LoadPackageParam lpparam) {
		// Remove header clocks
		final Class<?> Masthead = XposedHelpers.findClass("com.htc.launcher.masthead.Masthead", lpparam.classLoader);
		findAndHookMethod("com.htc.launcher.pageview.AllAppsController", lpparam.classLoader, "attachMasthead", Masthead, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				int m_nControllerState = (Integer)XposedHelpers.findField(param.thisObject.getClass(), "m_nControllerState").get(param.thisObject);
				Object m_AllAppsPagedView = XposedHelpers.findField(param.thisObject.getClass(), "m_AllAppsPagedView").get(param.thisObject);
				View m_headerContent = (View)XposedHelpers.findField(param.args[0].getClass(), "m_headerContent").get(param.args[0]);
				m_headerContent.setVisibility(8);				
				if (m_nControllerState == 1) {
					XposedHelpers.callMethod(param.args[0], "attachTo", m_AllAppsPagedView);
					XposedHelpers.callMethod(param.thisObject, "addActionBarListenerToMasthead", param.args[0]);
					Object m_masthead = XposedHelpers.findField(param.thisObject.getClass(), "m_masthead").get(param.thisObject);
					if (m_masthead == null && param.args[0] != null)
					try {
						XposedHelpers.callMethod(param.thisObject, "updateSortType", XposedHelpers.callMethod(param.args[0], "getActionBar"));
					} catch (NoSuchMethodError e){
						Object m_AllAppsDataManager = XposedHelpers.findField(param.thisObject.getClass(), "m_AllAppsDataManager").get(param.thisObject);
						XposedHelpers.callMethod(param.thisObject, "updateSortType", XposedHelpers.callMethod(param.args[0], "getActionBar"), XposedHelpers.callMethod(m_AllAppsDataManager, "getAppSort"));
					}						
				}
				XposedHelpers.findField(param.thisObject.getClass(), "m_masthead").set(param.thisObject, param.args[0]);
				param.setResult(null);
			}
		});
		
		// Restore clocks on BlinkFeed page
		try {
			findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "showWorkspace", boolean.class, Runnable.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Object m_masthead = XposedHelpers.findField(param.thisObject.getClass(), "m_masthead").get(param.thisObject);
					View m_headerContent = (View)XposedHelpers.findField(m_masthead.getClass(), "m_headerContent").get(m_masthead);
					m_headerContent.setVisibility(0);
				}
			});
		} catch (NoSuchMethodError e) {
		}
		
		// Move first row up
		findAndHookMethod("com.htc.launcher.pageview.AllAppsDataManager", lpparam.classLoader, "getRowOffsets", XC_MethodReplacement.returnConstant(0));
	
		// Move ActionBar up
		try {
			findAndHookMethod("com.htc.launcher.masthead.Masthead", lpparam.classLoader, "updateActionbarPosition", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					moveAB(param);
					param.setResult(null);
				}
			});
		} catch (NoSuchMethodError e) {
			findAndHookMethod("com.htc.launcher.masthead.Masthead", lpparam.classLoader, "setActionBar", int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					moveAB(param);
				}
			});
		}

		// AppDrawer top padding fine tune
		findAndHookMethod("com.htc.launcher.pageview.AllAppsDataManager", lpparam.classLoader, "setupPaddings", Context.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				int m_nPageLayoutPaddingTop = (Integer)XposedHelpers.findField(param.thisObject.getClass(), "m_nPageLayoutPaddingTop").get(param.thisObject);
				XposedHelpers.setIntField(param.thisObject, "m_nPageLayoutPaddingTop", (int)Math.round((float)m_nPageLayoutPaddingTop/1.5));
			}
		});			
	}

	public static void execHook_AppDrawerGridSizes(LoadPackageParam lpparam) {
		// Override grid size with current value
		findAndHookMethod("com.htc.launcher.pageview.AllAppsDataManager", lpparam.classLoader, "setupGrid", Context.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				int cellX = 3;
				int cellY = 4;
				
				if (gridSizeVal == 0)
				{
					cellX = 3;
					cellY = 4;
				} else if (gridSizeVal == 1) {
					cellX = 4;
					cellY = 5;
				} else if (gridSizeVal == 2) {
					cellX = 5;
					cellY = 5;
				} else if (gridSizeVal == 3) {
					cellX = 4;
					cellY = 6;
				} else if (gridSizeVal == 4) {
					cellX = 5;
					cellY = 6;
				}
				
				XposedHelpers.setIntField(param.thisObject, "m_nCellCountX", cellX);
				XposedHelpers.setIntField(param.thisObject, "m_nCellCountY", cellY);

				// Calculate item width/height
				if (gridSizeVal > 0) {
					Context ctx = (Context)param.args[0];
					WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
					Display display = wm.getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);
					XposedHelpers.setIntField(param.thisObject, "m_nItemViewWidth", Math.round(size.x / (cellX + 0.5f)));
					XposedHelpers.setIntField(param.thisObject, "m_nItemViewHeight", Math.round(size.y / (cellY + 1.5f)));
 				}
			}
		});

		// Change grid size on dialog click
		final Class<?> OnOptionClickListener = XposedHelpers.findClass("com.htc.launcher.pageview.AllAppsDialogFragment.OnOptionClickListener", lpparam.classLoader);
		findAndHookMethod("com.htc.launcher.pageview.AllAppsDialogFragment", lpparam.classLoader, "setOnClickSortListener", OnOptionClickListener, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if (onclickOption != null) onclickOption.unhook();
				onclickOption = XposedHelpers.findAndHookMethod(param.args[0].getClass(), "onclickOption", int.class, new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						gridSizeVal = (Integer)param.args[0];
					}
				});
			}
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			}
		});
		
		// Save grid size to Sense launcher preferences
		try {
			findAndHookMethod("com.htc.launcher.pageview.AllAppsOptionsManager", lpparam.classLoader, "saveGridSize", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Context m_Context = (Context)XposedHelpers.findField(param.thisObject.getClass(), "m_Context").get(param.thisObject);
					SharedPreferences.Editor editor = m_Context.getSharedPreferences("launcher.preferences", 0).edit();
					editor.putInt("grid_size_override", gridSizeVal).commit();
				}
			});
		} catch (ClassNotFoundError e){
			findAndHookMethod("com.htc.launcher.pageview.AllAppsDataManager", lpparam.classLoader, "saveGridOption", Context.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Context context = (Context)param.args[0];
					SharedPreferences.Editor editor = context.getSharedPreferences("launcher.preferences", 0).edit();
					editor.putInt("grid_size_override", gridSizeVal).commit();
				}
			});
		}

		// Load grid size from Sense launcher preferences
		try {
			findAndHookMethod("com.htc.launcher.pageview.AllAppsOptionsManager", lpparam.classLoader, "loadGridSize", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Context m_Context = (Context)XposedHelpers.findField(param.thisObject.getClass(), "m_Context").get(param.thisObject);
					SharedPreferences prefs = m_Context.getSharedPreferences("launcher.preferences", 0);
					if (prefs.contains("grid_size_override")) gridSizeVal = prefs.getInt("grid_size_override", 0);
				}
			});
		} catch (ClassNotFoundError e){
			findAndHookMethod("com.htc.launcher.pageview.AllAppsDataManager", lpparam.classLoader, "loadGridOption", Context.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Context context = (Context)param.args[0];
					SharedPreferences prefs = context.getSharedPreferences("launcher.preferences", 0);
					if (prefs.contains("grid_size_override")) gridSizeVal = prefs.getInt("grid_size_override", 0);
				}
			});
		}
		
		// Select current grid size in dialog
		findAndHookMethod("com.htc.launcher.pageview.AllAppsDialogFragment", lpparam.classLoader, "newInstance", int.class, int.class, int.class, boolean.class, boolean.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.args[2] = gridSizeVal;
			}
		});
		
		// Layout scale and frame border for editor layout
		final Class<?> EditLayoutHelper = XposedHelpers.findClass("com.htc.launcher.pageview.AllAppsPagedView.EditLayoutHelper", lpparam.classLoader);
		XposedBridge.hookAllConstructors(EditLayoutHelper, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.args[2] = 0.95f;
				param.args[3] = false;
				//param.args[1] = Color.argb(153, 0, 0, 0);
				//m_nEditLayoutPageSpacing
				//param.args[4] = 300;
			}
		});		
	}

	// Add 5x5, 4x6 and 5x6 grid options to dialog
	public static void execHook_AppDrawerGridSizesLayout(final InitPackageResourcesParam resparam, String MODULE_PATH) {
		int apps_grid_option = resparam.res.getIdentifier("apps_grid_option", "array", "com.htc.launcher");
		String[] gridSizes = resparam.res.getStringArray(apps_grid_option);
		
	    final int n = gridSizes.length;
	    gridSizes = Arrays.copyOf(gridSizes, n + 3);
	    gridSizes[n] = "5 × 5";
	    gridSizes[n + 1] = "4 × 6";
	    gridSizes[n + 2] = "5 × 6";
	    
		resparam.res.setReplacement(apps_grid_option, gridSizes);
	}
	
	public static void execHook_AppDrawerGridTinyText(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.pageview.AllAppsDataManager", lpparam.classLoader, "bindView", View.class, Context.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Object item = param.args[0];
				TextView itemlabel = null;
			
				try {
					if (item instanceof TextView) {
						itemlabel = (TextView) item;
					} else if (item instanceof LinearLayout) {
						itemlabel = (TextView) ((LinearLayout) item).getChildAt(1);
						itemlabel.setPadding(itemlabel.getPaddingLeft(), itemlabel.getPaddingTop(), itemlabel.getPaddingRight(), 0);
					}
	        
					if (itemlabel != null) {
						if (gridSizeVal == 3 || gridSizeVal == 4)
							itemlabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, 0.73f * itemlabel.getTextSize());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void execHook_SwipeActions(final LoadPackageParam lpparam) {
		// Detect vertical swipes
		XposedHelpers.findAndHookMethod("com.htc.launcher.Workspace", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Context helperContext = ((ViewGroup)param.thisObject).getContext();

				if (helperContext == null) return;
				if (mDetector == null) mDetector = new GestureDetector(helperContext, new SwipeListener(helperContext));

				MotionEvent ev = (MotionEvent)param.args[0];
				if (ev == null) return;
				mDetector.onTouchEvent(ev);
			}
		});
/*		
		try {
			XposedHelpers.findAndHookMethod("com.htc.launcher.scroller.PagedViewScroller", lpparam.classLoader, "handleOnTouchEvent", MotionEvent.class, new TouchListenerOnTouchWP(param.thisObject));
		} catch (NoSuchMethodError e) {
			//XposedBridge.log("No handleOnTouchEvent!");
			//XposedHelpers.findAndHookMethod("com.htc.launcher.scroller.PagedViewScroller", lpparam.classLoader, "handleInterceptTouchEvet", MotionEvent.class, new TouchListenerOnTouch(param.thisObject));
		}
*/
	}

	// Detect horizontal swipes (blank for now)
	/*
	private static class TouchListenerOnTouchWP extends XC_MethodHook {
		MotionEvent ev = null;
		Context helperContext = null;
		Object workspace = null;
		
		public TouchListenerOnTouchWP(Object wspace) {
			helperContext = ((ViewGroup)wspace).getContext();
			workspace = wspace;
		}

		@Override
		protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
			if (workspace == null || helperContext == null) return;
			if (mDetectorWP == null) mDetectorWP = new GestureDetector(helperContext, new SwipeListenerWP(workspace));

			ev = (MotionEvent)param.args[0];
			if (ev == null) return;
			mDetectorWP.onTouchEvent(ev);
		}
	} */
	
	// Listener for vertical swipe gestures
	private static class SwipeListener extends GestureDetector.SimpleOnGestureListener {
		private final int SWIPE_MIN_DISTANCE = 300;
		private final int SWIPE_MAX_OFF_PATH = 250;
		private final int SWIPE_THRESHOLD_VELOCITY = 200;
		
		final Context helperContext;

		public SwipeListener(Context context) {
			helperContext = context;
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			return true;			
		} 
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1 == null || e2 == null) return false;
			if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH) return false;
			
			if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				switch (XMain.pref_swipedown) {
					case 2: return GlobalActions.expandNotifications(helperContext);
					case 3: return GlobalActions.expandEQS(helperContext);
					case 4: return GlobalActions.lockDevice(helperContext);
					case 5: return GlobalActions.goToSleep(helperContext);
					case 6: return GlobalActions.launchApp(helperContext, 1);
					default: return false;					
				}
			}
			
			if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				switch (XMain.pref_swipeup) {
					case 2: return GlobalActions.expandNotifications(helperContext);
					case 3: return GlobalActions.expandEQS(helperContext);
					case 4: return GlobalActions.lockDevice(helperContext);
					case 5: return GlobalActions.goToSleep(helperContext);
					case 6: return GlobalActions.launchApp(helperContext, 2);
					default: return false;
				}
			}

			return false;
		}
	}
	
	// Listener for horizontal swipe gestures
	/*
	private static class SwipeListenerWP extends GestureDetector.SimpleOnGestureListener {
		//private final int SWIPE_MIN_DISTANCE = 300;
		//private final int SWIPE_MAX_OFF_PATH = 250;
		//private final int SWIPE_THRESHOLD_VELOCITY = 200;
		
		final Object workspace;

		public SwipeListenerWP(Object wspace) {
			workspace = wspace;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;			
		} 
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			XposedBridge.log("Fling! " + workspace.getClass());
			//ViewGroup ws = (ViewGroup)workspace;
			return false;
		}
	}*/

	private static BroadcastReceiver mBR = new BroadcastReceiver() {      
        public void onReceive(Context context, Intent intent)
        {
        	String action = intent.getAction();
        	if (action.equals("com.langerhans.one.mods.PrismMods.GoToSleep")) {
        		((PowerManager)context.getSystemService(Context.POWER_SERVICE)).goToSleep(SystemClock.uptimeMillis());
        	}
        	if (action.equals("com.langerhans.one.mods.PrismMods.LockDevice")) {
        		try {
        			final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);
        			Method lockNow = XposedHelpers.findMethodExact(clsPWM, "lockNow", Bundle.class);
        			Object[] params = new Object[1];
        			params[0] = null;
					lockNow.invoke(mPWM, params);
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        }
	};
	
	public static void setupPWM() {
		try {
			final Class<?> clsPWM = findClass("com.android.internal.policy.impl.PhoneWindowManager", null);

			findAndHookMethod(clsPWM, "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					mPWM = param.thisObject;
					Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
		            IntentFilter intentfilter = new IntentFilter();
		            intentfilter.addAction("com.langerhans.one.mods.PrismMods.GoToSleep");
		            intentfilter.addAction("com.langerhans.one.mods.PrismMods.LockDevice");
					mPWMContext.registerReceiver(mBR, intentfilter);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void execHook_BfRemove(LoadPackageParam lpparam) {
		try{
			findAndHookMethod("com.htc.launcher.util.Protection", lpparam.classLoader, "isFeedEnabled", new XC_MethodHook() {
				@Override
	    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(false);
				}
			});
		}
		catch(Exception e)
		{
			//Probably on 4.2.2...
		}
	}

	public static void execHook_InfiniScroll(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.SmoothPagedView", lpparam.classLoader, "snapToDestination", new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param)	throws Throwable {
				int i = (Integer) callMethod(param.thisObject, "getPageCount");
				int j = (Integer) callMethod(param.thisObject, "getCurrentPage");
				if(j == 0)
					callMethod(param.thisObject, "snapToPage", i - 1, 550);
				else
					if(j == i - 1)
					{
						callMethod(param.thisObject, "snapToPage", 0, 550);
						return null;
					}
				return null;
			}
		});
	}

	static FrameLayout hotSeat = null;
	static int x_start = 0;
	static int y_start = 0;
	
	public static void execHook_DockScroll(final LoadPackageParam lpparam) {
		XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.htc.launcher.hotseat.Hotseat", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				//XposedHelpers.setObjectField(param.thisObject, "m_nCellCountY", 3);
			}
		});

		findAndHookMethod("com.htc.launcher.hotseat.Hotseat", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				//ViewGroup m_content = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "m_content");
			}
		});

		XposedHelpers.findAndHookMethod("com.htc.launcher.CellLayout", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Context helperContext = ((ViewGroup)param.thisObject).getContext();
				if (helperContext == null) return;
				if (mDetectorDock == null) mDetectorDock = new GestureDetector(helperContext, new SwipeListenerDock(helperContext));
				
				boolean m_bIsHotseat = (Boolean)XposedHelpers.getObjectField(param.thisObject, "m_bIsHotseat");
				if (m_bIsHotseat) {
					if (hotSeat == null) {
						Object hotSeatObj = ((ViewGroup)param.thisObject).getParent();
						if (hotSeatObj != null) hotSeat = (FrameLayout)hotSeatObj;
					}
					
					MotionEvent ev = (MotionEvent)param.args[0];
					if (ev == null) return;
					mDetectorDock.onTouchEvent(ev);
				}
			}
		});
	}
	
	// Listener for swipes on dock
	private static class SwipeListenerDock extends GestureDetector.SimpleOnGestureListener {
		private final int SWIPE_MIN_DISTANCE_HORIZ = 130;
		private final int SWIPE_MIN_DISTANCE_VERT = 50;
		private final int SWIPE_THRESHOLD_VELOCITY = 100;
		
		//final Context helperContext;

		public SwipeListenerDock(Context context) {
			//helperContext = context;
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			return true;			
		} 
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1 == null || e2 == null) return false;
			
			if (Math.abs(e2.getX() - e1.getX()) > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				//XposedBridge.log("Horizontal Swipe!");
				return true;

			}
			if (Math.abs(e2.getY() - e1.getY()) > SWIPE_MIN_DISTANCE_VERT && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				//XposedBridge.log("Vertical Swipe!");
				return true;
			}

			return false;
		}
	}
}

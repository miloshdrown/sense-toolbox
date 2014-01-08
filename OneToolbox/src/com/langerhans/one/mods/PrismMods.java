package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findConstructorExact;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setIntField;
import static de.robv.android.xposed.XposedHelpers.setStaticIntField;

import java.util.Arrays;
import java.util.EnumSet;

import android.app.Activity;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.htc.preference.HtcPreferenceFragment;
import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcPopupWindow;
import com.langerhans.one.R;
import com.langerhans.one.utils.GlobalActions;
import com.langerhans.one.utils.PopupAdapter;
import com.langerhans.one.utils.Version;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodHook.Unhook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LayoutInflated.LayoutInflatedParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class PrismMods {
	
	static Unhook onclickOption = null;
	public static int gridSizeVal = 0;
	private static GestureDetector mDetector;
	private static GestureDetector mDetectorDock;
	
	public static void execHook_InvisiDock(LoadPackageParam lpparam, final int transparency) {
		try {
			findAndHookMethod("com.htc.launcher.hotseat.Hotseat", lpparam.classLoader, "show", boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					boolean isAllAppsOpen = false;
					Object m_launcher = XposedHelpers.getObjectField(param.thisObject, "m_launcher");
					if (m_launcher != null)
						isAllAppsOpen = (Boolean)XposedHelpers.callMethod(m_launcher, "isAllAppsShown");
				
					ImageView m_BackgroundImg = (ImageView)XposedHelpers.getObjectField(param.thisObject, "m_BackgroundImg");
					float alphaDrawer = XMain.pref.getInt("pref_key_prism_invisidrawer", 100) / 100.0f;
					if (isAllAppsOpen && alphaDrawer > transparency/255.0f) {
						if (XMain.pref.getBoolean("pref_key_prism_invisidrawer_enable", false)) {
							m_BackgroundImg.animate().alpha(alphaDrawer);
						} else
							m_BackgroundImg.animate().alpha(1.0f);
					} else m_BackgroundImg.animate().alpha(transparency/255.0f);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_InvisiDockRes(InitPackageResourcesParam resparam, final int transparency) {
		try {
			final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, resparam.res);
			resparam.res.setReplacement("com.htc.launcher", "drawable", "home_nav_bg", new XResources.DrawableLoader() {
				@Override
				public Drawable newDrawable(XResources res, int id) throws Throwable {
					Drawable bg = modRes.getDrawable(R.drawable.home_nav_bg);
					bg.setAlpha(transparency);
					return bg;
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_InvisiWidget(final InitPackageResourcesParam resparam, final int transparency, String MODULE_PATH) {
		if (XMain.senseVersion.compareTo(new Version("5.5")) == -1) {
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
	}
	
	public static void execHook_InvisiWidgetSense55(LoadPackageParam lpparam, final int transparency) {
		if (XMain.senseVersion.compareTo(new Version("5.5")) >= 0) {
			findAndHookMethod("com.htc.launcher.LauncherAppWidgetHostView", lpparam.classLoader, "onViewAdded", View.class, new XC_MethodHook() {
				@Override
				public void afterHookedMethod(MethodHookParam param) throws Throwable {
					ViewGroup widgetView = (ViewGroup) param.args[0];
					Resources viewRes = widgetView.getResources();
					int bgId = viewRes.getIdentifier("background_panel", "id", "com.htc.widget.weatherclock");
					if(bgId != 0)
					{
						ImageView bg = (ImageView) widgetView.findViewById(bgId);
						bg.getBackground().setAlpha(transparency);
					}
				}
			});
		}
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
		if (XMain.senseVersion.compareTo(new Version("5.5")) >= 0) {
			findAndHookMethod("com.htc.launcher.bar.BarController", lpparam.classLoader, "setStatusBarTransparent", Context.class, boolean.class, new XC_MethodHook() {
				@Override
	    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(null);
				}
			});
		}else {
			findAndHookMethod("com.htc.launcher.bar.BarController", lpparam.classLoader, "setStatusBarTransparent", boolean.class, new XC_MethodHook() {
				@Override
	    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(null);
				}
			});
		}
	}
	
	public static void execHook_InvisiFolder(final InitPackageResourcesParam resparam, final int transparency) {
		if (XMain.senseVersion.compareTo(new Version("5.5")) >= 0) {
			try {
				resparam.res.hookLayout("com.htc.launcher", "layout", "specific_user_folder", new XC_LayoutInflated() {
					@Override
					public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
						InvisiFolder_Snippet(liparam, resparam, transparency);
					}
				});
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		} else {
			try {
				resparam.res.hookLayout("com.htc.launcher", "layout", "user_folder", new XC_LayoutInflated() {
					@Override
					public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
						InvisiFolder_Snippet(liparam, resparam, transparency);
					}
				});
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	}
	
	private static void InvisiFolder_Snippet(LayoutInflatedParam liparam, final InitPackageResourcesParam resparam, final int transparency) {
		RelativeLayout bg = (RelativeLayout)liparam.view;
		bg.getBackground().setAlpha(transparency);
		LinearLayout nameframe = (LinearLayout)liparam.view.findViewById(resparam.res.getIdentifier("folder_name_frame", "id", "com.htc.launcher"));
		if (nameframe != null) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)nameframe.getLayoutParams();
			lp.rightMargin = 3;
			nameframe.setLayoutParams(lp);
			nameframe.setBackgroundColor(Color.argb(255, 20, 20, 20));
		}
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
	
	public static void execHook_InvisiDrawerRes(InitPackageResourcesParam resparam) {
		try {
			resparam.res.setReplacement("com.htc.launcher", "integer", "config_workspaceUnshrinkTime", 300);
			resparam.res.setReplacement("com.htc.launcher", "integer", "config_appsCustomizeWorkspaceShrinkTime", 100);
		} catch (Exception e) {}
	}
	
	public static void execHook_InvisiDrawerCode(LoadPackageParam lpparam, final int transparency) {
		execHook_PreserveWallpaper(lpparam);
		
		if (XMain.senseVersion.compareTo(new Version("5.5")) >= 0) {
			XposedBridge.hookAllConstructors(findClass("com.htc.launcher.pageview.AllAppsPagedViewHost", lpparam.classLoader), new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					((FrameLayout)param.thisObject).getBackground().setAlpha(transparency);
				}
			});
			
			findAndHookMethod("com.htc.launcher.DragLayer", lpparam.classLoader, "setBackgroundAlpha", float.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					boolean isAllAppsOpen = false;
					Object m_launcher = XposedHelpers.getObjectField(param.thisObject, "m_launcher");
					if (m_launcher != null)
					isAllAppsOpen = (Boolean)XposedHelpers.callMethod(m_launcher, "isAllAppsShown");	
					
					if (isAllAppsOpen)
						param.args[0] = 0;
					else if ((Float)param.args[0] > transparency/255.0f)
						param.args[0] = transparency/255.0f;
				}
			});
			
			// Animate Workspace alpha during transition between Workspace and AllApps   
			final Class<?> Properties = XposedHelpers.findClass("com.htc.launcher.LauncherViewPropertyAnimator.Properties", lpparam.classLoader);
			findAndHookMethod("com.htc.launcher.LauncherViewPropertyAnimator", lpparam.classLoader, "start", new XC_MethodHook() {
				@Override
				@SuppressWarnings({ "rawtypes", "unchecked" })
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					EnumSet m_propertiesToSet = (EnumSet)XposedHelpers.getObjectField(param.thisObject, "m_propertiesToSet");
					Enum SCALE_X = (Enum) XposedHelpers.getStaticObjectField(Properties, "SCALE_X");
					Enum SCALE_Y = (Enum) XposedHelpers.getStaticObjectField(Properties, "SCALE_Y");
					if (m_propertiesToSet.contains(SCALE_X) && m_propertiesToSet.contains(SCALE_Y)) {
						float m_fScaleX = XposedHelpers.getFloatField(param.thisObject, "m_fScaleX");
						float m_fScaleY = XposedHelpers.getFloatField(param.thisObject, "m_fScaleY");
						
						Enum ALPHA = (Enum)XposedHelpers.getStaticObjectField(Properties, "ALPHA");
						if (m_fScaleX == 0.9f && m_fScaleY == 0.9f) {
							m_propertiesToSet.add(ALPHA);
							XposedHelpers.setFloatField(param.thisObject, "m_fAlpha", 0.0f);
						} else if (m_fScaleX == 1.0f && m_fScaleY == 1.0f) {
							m_propertiesToSet.add(ALPHA);
							XposedHelpers.setFloatField(param.thisObject, "m_fAlpha", 1.0f);
						}
					}
				}
			});
			
			/*
			findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "setBackgroundAlpha", float.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				}
			});
			
			findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "showAppsCustomizeHelper", "com.htc.launcher.Launcher.State", boolean.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				}
			});
			*/
		} else { 
			findAndHookMethod("com.htc.launcher.pageview.AllAppsPagedViewHost", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					ViewGroup m_PagedView = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "m_PagedView");
					ViewParent vp = m_PagedView.getParent();
					if (vp != null) {
						if (vp instanceof RelativeLayout) {
							((RelativeLayout)vp).getBackground().setAlpha(transparency);
						} else if (vp instanceof FrameLayout) {
							((FrameLayout)vp).getBackground().setAlpha(transparency);
						}					
					}
				}
			});
		}
		
		try {
			// This will fail on 4.2.2, best version check ever!
			XposedHelpers.findMethodExact("com.htc.launcher.masthead.Masthead", lpparam.classLoader, "updateActionbarPosition");
		} catch (NoSuchMethodError e){
			findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "showAllApps", boolean.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					ViewGroup m_workspace = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "m_workspace");
					m_workspace.setVisibility(4);
				}
			});
		}						
	}
	
	// Move Action Bar
	private static void moveAB(MethodHookParam param) throws Throwable {
		FrameLayout m_headerActionBar = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "m_headerActionBar");
		if (m_headerActionBar != null) {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)m_headerActionBar.getLayoutParams();
			
			Object container = ((FrameLayout)param.thisObject).getParent();
			String feedClass = "com.htc.launcher.feeds.view.FeedScrollView";
			if (XMain.senseVersion.compareTo(new Version("5.5")) >= 0)
			feedClass = "com.htc.launcher.feeds.view.FeedScrollPageView";
			
			if (container.getClass().getCanonicalName().equalsIgnoreCase(feedClass)) {
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
				int m_nControllerState = XposedHelpers.getIntField(param.thisObject, "m_nControllerState");
				Object m_AllAppsPagedView = XposedHelpers.getObjectField(param.thisObject, "m_AllAppsPagedView");
				View m_headerContent = (View)XposedHelpers.getObjectField(param.args[0], "m_headerContent");
				m_headerContent.setVisibility(8);				
				if (m_nControllerState == 1) {
					XposedHelpers.callMethod(param.args[0], "attachTo", m_AllAppsPagedView);
					XposedHelpers.callMethod(param.thisObject, "addActionBarListenerToMasthead", param.args[0]);
					Object m_masthead = XposedHelpers.getObjectField(param.thisObject, "m_masthead");
					if (m_masthead == null && param.args[0] != null)
					try {
						XposedHelpers.callMethod(param.thisObject, "updateSortType", XposedHelpers.callMethod(param.args[0], "getActionBar"));
					} catch (NoSuchMethodError e){
						Object m_AllAppsDataManager = XposedHelpers.getObjectField(param.thisObject, "m_AllAppsDataManager");
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
					Object m_masthead = XposedHelpers.getObjectField(param.thisObject, "m_masthead");
					View m_headerContent = (View)XposedHelpers.getObjectField(m_masthead, "m_headerContent");
					m_headerContent.setVisibility(0);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
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
				int m_nPageLayoutPaddingTop = XposedHelpers.getIntField(param.thisObject, "m_nPageLayoutPaddingTop");
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
		});
		
		// Save grid size to Sense launcher preferences
		try {
			findAndHookMethod("com.htc.launcher.pageview.AllAppsOptionsManager", lpparam.classLoader, "saveGridSize", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Context m_Context = (Context)XposedHelpers.getObjectField(param.thisObject, "m_Context");
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
					Context m_Context = (Context)XposedHelpers.getObjectField(param.thisObject, "m_Context");
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
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void execHook_HomeScreenGridSize(final InitPackageResourcesParam resparam, String MODULE_PATH) {
		try {
			XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
			
			int cell_count_y = resparam.res.getIdentifier("cell_count_y", "integer", "com.htc.launcher");
			resparam.res.setReplacement(cell_count_y, 5);
			
			int workspace_top_padding_port = resparam.res.getIdentifier("workspace_top_padding_port", "dimen", "com.htc.launcher");
			resparam.res.setReplacement(workspace_top_padding_port, modRes.fwd(R.dimen.workspace_top_padding_port));
			
			int celllayout_top_padding_port = resparam.res.getIdentifier("celllayout_top_padding_port", "dimen", "com.htc.launcher");
			resparam.res.setReplacement(celllayout_top_padding_port, modRes.fwd(R.dimen.celllayout_top_padding_port));
			
			int celllayout_bottom_padding_port = resparam.res.getIdentifier("celllayout_bottom_padding_port", "dimen", "com.htc.launcher");
			resparam.res.setReplacement(celllayout_bottom_padding_port, modRes.fwd(R.dimen.celllayout_bottom_padding_port));
			
			int workspace_height_gap_port = resparam.res.getIdentifier("workspace_height_gap_port", "dimen", "com.htc.launcher");
			resparam.res.setReplacement(workspace_height_gap_port, modRes.fwd(R.dimen.workspace_height_gap_port));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	// Make all widgets resizable along both axis
	public static void execHook_HomeScreenResizableWidgets(final LoadPackageParam lpparam) {
		XposedHelpers.findAndHookMethod("com.htc.launcher.Workspace", lpparam.classLoader, "isResizable", AppWidgetProviderInfo.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
		
		XposedBridge.hookAllConstructors(findClass("com.htc.launcher.AppWidgetResizeFrame", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				XposedHelpers.setIntField(param.thisObject, "mResizeMode", 3);
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
	}
	
	// Listener for vertical swipe gestures
	private static class SwipeListener extends GestureDetector.SimpleOnGestureListener {
		// For HTC One
		private int SWIPE_MIN_DISTANCE = 300;
		private int SWIPE_MAX_OFF_PATH = 250;
		private int SWIPE_THRESHOLD_VELOCITY = 200;
		
		final Context helperContext;

		public SwipeListener(Context context) {
			helperContext = context;
			float density = helperContext.getResources().getDisplayMetrics().density;
			SWIPE_MIN_DISTANCE = Math.round(100 * density);
			SWIPE_MAX_OFF_PATH = Math.round(85 * density);
			SWIPE_THRESHOLD_VELOCITY = Math.round(65 * density);			
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
				switch (Integer.parseInt(XMain.pref.getString("pref_key_prism_swipedownaction", "1"))) {
					case 2: return GlobalActions.expandNotifications(helperContext);
					case 3: return GlobalActions.expandEQS(helperContext);
					case 4: return GlobalActions.lockDevice(helperContext);
					case 5: return GlobalActions.goToSleep(helperContext);
					case 6: return GlobalActions.takeScreenshot(helperContext);
					case 7: return GlobalActions.launchApp(helperContext, 1);
					case 8: return GlobalActions.toggleThis(helperContext, Integer.parseInt(XMain.pref.getString("pref_key_prism_swipedown_toggle", "0")));
					default: return false;					
				}
			}
			
			if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				switch (Integer.parseInt(XMain.pref.getString("pref_key_prism_swipeupaction", "1"))) {
					case 2: return GlobalActions.expandNotifications(helperContext);
					case 3: return GlobalActions.expandEQS(helperContext);
					case 4: return GlobalActions.lockDevice(helperContext);
					case 5: return GlobalActions.goToSleep(helperContext);
					case 6: return GlobalActions.takeScreenshot(helperContext);
					case 7: return GlobalActions.launchApp(helperContext, 2);
					case 8: return GlobalActions.toggleThis(helperContext, Integer.parseInt(XMain.pref.getString("pref_key_prism_swipeup_toggle", "0")));
					default: return false;
				}
			}

			return false;
		}
	}
	
	public static void execHook_BfRemove(LoadPackageParam lpparam) {
		//Still throws error in Xposed, so better check version to keep other mods running.
		//People might have it enabled and can't disable it since we removed the preference.
		if (XMain.senseVersion.compareTo(new Version("5.5")) == -1) {
			try {
				findAndHookMethod("com.htc.launcher.util.Protection", lpparam.classLoader, "isFeedEnabled", new XC_MethodHook() {
					@Override
		    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						param.setResult(false);
					}
				});
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	}

	public static void execHook_InfiniScroll(LoadPackageParam lpparam) {
		if (XMain.senseVersion.compareTo(new Version("5.5")) == -1) {
			findAndHookMethod("com.htc.launcher.SmoothPagedView", lpparam.classLoader, "snapToDestination", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					int i = (Integer) callMethod(param.thisObject, "getPageCount");
					int j = (Integer) callMethod(param.thisObject, "getCurrentPage");
					if(j == 0)
					{
						callMethod(param.thisObject, "snapToPage", i - 1, 550);
						param.setResult(null);
					}
					if(j == i - 1)
					{
						callMethod(param.thisObject, "snapToPage", 0, 550);
						param.setResult(null);
					}
				}
			});
		}
	}

	static FrameLayout hotSeat = null;
	static int x_start = 0;
	static int y_start = 0;
	
	public static void execHook_DockSwipe(final LoadPackageParam lpparam) {
		XposedHelpers.findAndHookMethod("com.htc.launcher.CellLayout", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Context helperContext = ((ViewGroup)param.thisObject).getContext();
				if (helperContext == null) return;
				if (mDetectorDock == null) mDetectorDock = new GestureDetector(helperContext, new SwipeListenerDock(param.thisObject));
				
				boolean m_bIsHotseat = XposedHelpers.getBooleanField(param.thisObject, "m_bIsHotseat");
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
		// For HTC One
		private int SWIPE_MIN_DISTANCE_HORIZ = 230;
		private int SWIPE_MIN_DISTANCE_VERT = 50;
		private int SWIPE_THRESHOLD_VELOCITY = 100;
		
		final Context helperContext;
		final Object launcher;

		public SwipeListenerDock(Object cellLayout) {
			launcher = XposedHelpers.getObjectField(cellLayout, "m_launcher");
			helperContext = ((ViewGroup)cellLayout).getContext();
			float density = helperContext.getResources().getDisplayMetrics().density;
			SWIPE_MIN_DISTANCE_HORIZ = Math.round(75 * density);
			SWIPE_MIN_DISTANCE_VERT = Math.round(17 * density);
			SWIPE_THRESHOLD_VELOCITY = Math.round(33 * density);
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			return true;			
		} 
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1 == null || e2 == null) return false;
			
			if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				switch (Integer.parseInt(XMain.pref.getString("pref_key_prism_swiperightaction", "1"))) {
					case 2: return GlobalActions.expandNotifications(helperContext);
					case 3: return GlobalActions.expandEQS(helperContext);
					case 4: return GlobalActions.lockDevice(helperContext);
					case 5: return GlobalActions.goToSleep(helperContext);
					case 6: return GlobalActions.takeScreenshot(helperContext);
					case 7: return GlobalActions.launchApp(helperContext, 5);
					case 8: return GlobalActions.toggleThis(helperContext, Integer.parseInt(XMain.pref.getString("pref_key_prism_swiperight_toggle", "0")));
					default: return false;					
				}
			}
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				switch (Integer.parseInt(XMain.pref.getString("pref_key_prism_swipeleftaction", "1"))) {
					case 2: return GlobalActions.expandNotifications(helperContext);
					case 3: return GlobalActions.expandEQS(helperContext);
					case 4: return GlobalActions.lockDevice(helperContext);
					case 5: return GlobalActions.goToSleep(helperContext);
					case 6: return GlobalActions.takeScreenshot(helperContext);
					case 7: return GlobalActions.launchApp(helperContext, 6);
					case 8: return GlobalActions.toggleThis(helperContext, Integer.parseInt(XMain.pref.getString("pref_key_prism_swipeleft_toggle", "0")));
					default: return false;					
				}
			}

			if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE_VERT && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				if (XMain.pref.getBoolean("pref_key_prism_homemenu", false)) {
					createAndShowPopup((ViewGroup)XposedHelpers.getObjectField(launcher, "m_workspace"), (Activity)launcher);
					return true;
				} else return false;
			}

			return false;
		}
	}
	
	private static HtcPopupWindow popup = null;
	
	public static void execHook_HomeMenu(final LoadPackageParam lpparam) {
		XposedHelpers.findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "onKeyDown", int.class, KeyEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				if ((Integer)param.args[0] == KeyEvent.KEYCODE_MENU)
				createAndShowPopup((ViewGroup)XposedHelpers.getObjectField(param.thisObject, "m_workspace"), (Activity)param.thisObject);
			}
		});
	}
	
	public static void createAndShowPopup(ViewGroup m_workspace, final Activity launcher) {
		if (popup == null) {
			popup = new HtcPopupWindow(m_workspace.getContext());
			popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
			popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
			popup.setTouchable(true);
			popup.setFocusable(true);
			popup.setOutsideTouchable(true);
		}
		
		// Update items and listeners. I doubt launcher contexts will change over time but just in case :) 
		ListView options = new ListView(m_workspace.getContext());
		XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
		ListAdapter listAdapter = new PopupAdapter(options.getContext(), modRes.getStringArray(R.array.home_menu), false);
		options.setFocusableInTouchMode(true);
		options.setAdapter(listAdapter);
		options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				popup.dismiss();
				if (position == 0) {
					launcher.startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
				} else if (position == 1) {
					launcher.startActivity((new Intent("android.intent.action.MAIN")).setAction("com.htc.personalize.ACTION_HOMEPERSONALIZE"));
				} else if (position == 2) {
					launcher.startActivity(new Intent(Settings.ACTION_SETTINGS));
				} else if (position == 3) {
					launcher.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				} else if (position == 4) {
					OtherMods.startAPM(launcher);
				} else if (position == 5) {
					Settings.System.putString(view.getContext().getContentResolver(), "lock_homescreen_dragging", String.valueOf(!Boolean.parseBoolean(Settings.System.getString(view.getContext().getContentResolver(), "lock_homescreen_dragging"))));
				}
			}						
		});
		options.setOnKeyListener(new View.OnKeyListener() {        
		    @Override
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        if (keyCode ==  KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
		        	popup.dismiss();
		            return true;
		        }                
		        return false;
		    }
		});
		popup.setContentView(options);
		popup.showAtLocation(m_workspace, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
	}
	
	public static void execHook_SevenScreens(final LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.model.PagesManager", lpparam.classLoader, "getMaxPageCount", XC_MethodReplacement.returnConstant(7));
		
		XposedBridge.hookAllConstructors(findClass("com.htc.launcher.model.PagesManager", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				setIntField(param.thisObject, "m_nPageCount", 6);
			}
		});
		
		findAndHookMethod("com.htc.launcher.model.WorkspaceConfiguration", lpparam.classLoader, "getMaxPageCount", XC_MethodReplacement.returnConstant(7));
		
		XposedBridge.hookAllConstructors(findClass("com.htc.launcher.model.WorkspaceConfiguration", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				setIntField(param.thisObject, "m_nPageCount", 6);
				setStaticIntField(param.thisObject.getClass(), "DEFAULT_PAGE_COUNT_WIDGETHOME", 6);
			}
		});
	}
	
	private static void applyLockScreenState(Context ctx) {
		try {
			Class<?> classLPU = findClass("com.android.internal.widget.LockPatternUtils", null);
			Object LPU = findConstructorExact(classLPU, Context.class).newInstance(ctx);
			if (android.provider.Settings.Secure.getInt(ctx.getContentResolver(), "lockscreen.htc.types.bypasslockscreen", 0) == 1) {
				XposedHelpers.callMethod(LPU, "setLockScreenDisabled", true);
			} else {
				XposedHelpers.callMethod(LPU, "setLockScreenDisabled", false);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_BypassLockScreen(final LoadPackageParam lpparam) {
		if (XMain.senseVersion.compareTo(new Version("5.5")) >= 0) try {
			// Set lock screen according to bypass option when None is selected in Screen lock
			findAndHookMethod("com.android.settings.ChooseLockGeneric.ChooseLockGenericFragment", lpparam.classLoader, "updateUnlockMethodAndFinish", int.class, boolean.class, new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					if ((Integer)param.args[0] == 0 && (Boolean)param.args[1] == false) {
						Context ctx = ((HtcPreferenceFragment)param.thisObject).getActivity();
						applyLockScreenState(ctx);
					}
				}
			});
			// Set lock screen according to bypass option on that option change
			findAndHookMethod("com.android.settings.framework.preference.security.HtcBypassLockScreenOnWakePreference", lpparam.classLoader, "onSetValueInBackground", Context.class, boolean.class, new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					applyLockScreenState((Context)param.args[0]);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_invisiLabels(final LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.CellLayout", lpparam.classLoader, "addViewToCellLayout", View.class, int.class, int.class, "com.htc.launcher.CellLayout$LayoutParams", boolean.class, new XC_MethodHook() {
			@Override
			public void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					if (XMain.senseVersion.compareTo(new Version("5.5")) >= 0)
						callMethod(param.args[0], "hideText", true);
					else
						callMethod(param.args[0], "setTextColor", Color.TRANSPARENT);
				} catch (Throwable t) {
					//Not an app icon
				}
				
			}
		});
		
		XposedBridge.hookAllConstructors(findClass("com.htc.launcher.folder.WorkspaceFolderIcon", lpparam.classLoader), new XC_MethodHook() {
			@Override
			public void afterHookedMethod(MethodHookParam param) throws Throwable {
				setAdditionalInstanceField(param.thisObject, "workspaceFolder", true);
			}
		});
		
		findAndHookMethod("com.htc.launcher.folder.FolderIcon", lpparam.classLoader, "setTextVisible", boolean.class, new XC_MethodHook() {
			@Override
			public void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if ((Boolean) getAdditionalInstanceField(param.thisObject, "workspaceFolder"))
					param.args[0] = false;
			}
		});
	}

	// Long press on hotseat toggle button, no idea how to use it for now :D
	public static void execHook_hotseatToggleBtn(final LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.hotseat.Hotseat", lpparam.classLoader, "resetLayout", new XC_MethodHook() {
			@Override
			public void afterHookedMethod(final MethodHookParam param) throws Throwable {
				TextView m_toggleButton = (TextView)XposedHelpers.getObjectField(param.thisObject, "m_toggleButton");
				final Object m_launcher = XposedHelpers.getObjectField(param.thisObject, "m_launcher");
				m_toggleButton.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						if ((Boolean)XposedHelpers.callMethod(m_launcher, "isAllAppsShown"))
							XposedHelpers.callMethod(m_launcher, "showWorkspace", false);
						else
							XposedHelpers.callMethod(m_launcher, "showAllApps", false);
						
						return true;
					}
				});
			}
		});
	}
	
	public static void fixInvisibarKitKat(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "transparentStatusBarForKK", XC_MethodReplacement.DO_NOTHING);
	}
	
	private static boolean isLauncherLocked(Context context) {
		return Boolean.parseBoolean(Settings.System.getString(context.getContentResolver(), "lock_homescreen_dragging"));
	}
	
	private static void showLockedWarning(final Activity act) {
		XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
		HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(act);
		builder.setTitle(modRes.getString(R.string.warning));
		builder.setMessage(modRes.getString(R.string.locked_warning));
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setCancelable(false);
		builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton){
				XposedHelpers.callMethod(act, "onBackPressed");
			}
		});
		HtcAlertDialog dlg = builder.create();
		dlg.show();
	}
	
	public static void execHook_LauncherLock(final LoadPackageParam lpparam) {
		// Disable dragging inside folders
		XposedHelpers.findAndHookMethod("com.htc.launcher.folder.Folder.FolderDataManager", lpparam.classLoader, "allowedDrag", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				RelativeLayout folder = (RelativeLayout)XposedHelpers.getSurroundingThis(param.thisObject);
				if (folder != null && isLauncherLocked(folder.getContext())) param.setResult(false);
			}
		});
		
		// Disable appdrawer dragging
		XposedHelpers.findAndHookMethod("com.htc.launcher.pageview.AllAppsPagedView", lpparam.classLoader, "beginDragging", View.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				ViewGroup allApps = (ViewGroup)param.thisObject;
				if (allApps != null && isLauncherLocked(allApps.getContext())) param.setResult(false);
			}
		});

		// Disable other dragging
		XposedHelpers.findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "isDraggingEnabled", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Activity launcher = (Activity)param.thisObject;
				if (launcher != null && isLauncherLocked(launcher)) param.setResult(false);
			}
		});
		
		// Disable homescreen customization
		XposedHelpers.findAndHookMethod("com.htc.launcher.pageview.activity.AddToHomeActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Activity addToHome = (Activity)param.thisObject;
				if (addToHome != null && isLauncherLocked(addToHome)) showLockedWarning(addToHome);
			}			
		});
		
		XposedHelpers.findAndHookMethod("com.htc.launcher.pageview.activity.AddToHomeActivity", lpparam.classLoader, "onNewIntent", Intent.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Activity addToHome = (Activity)param.thisObject;
				if (addToHome != null && isLauncherLocked(addToHome)) showLockedWarning(addToHome);
			}
		});
	}
}

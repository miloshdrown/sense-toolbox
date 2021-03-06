package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setIntField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static de.robv.android.xposed.XposedHelpers.setStaticIntField;
import java.util.Arrays;
import java.util.EnumSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.htc.widget.HtcAlertDialog;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.GlobalActions;
import com.sensetoolbox.six.utils.Helpers;
import com.sensetoolbox.six.utils.PopupAdapter;
import com.sensetoolbox.six.utils.ShakeManager;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodHook.Unhook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LayoutInflated.LayoutInflatedParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class PrismMods {
	
	static Unhook onclickOption = null;
	public static int gridSizeVal = 0;
	private static GestureDetector mDetector;
	private static GestureDetector mDetectorHorizontal;
	private static GestureDetector mDetectorVertical;
	static HtcAlertDialog dlg = null;
	static AlertDialog dlgStock = null;
	static Object launcher;
	
	public static void execHook_InvisiWidget(LoadPackageParam lpparam, final int transparency) {
		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			public void afterHookedMethod(MethodHookParam param) throws Throwable {
				View widgetView = param.args.length == 2 ? (View) param.args[1] : (View) param.args[0];
				Resources viewRes = widgetView.getResources();
				int bgId = viewRes.getIdentifier("background_panel", "id", "com.htc.widget.weatherclock");
				int wlId = viewRes.getIdentifier("widget_layout", "id", "com.htc.widget.weatherclock");
				int dcId = viewRes.getIdentifier("digital_clock", "id", "com.htc.widget.weatherclock");
				if (bgId != 0 && wlId != 0 && dcId != 0) {
					RelativeLayout wl = (RelativeLayout)widgetView.findViewById(wlId);
					RelativeLayout dc = (RelativeLayout)widgetView.findViewById(dcId);
					if (wl != null && dc != null) {
						ImageView bg = (ImageView)widgetView.findViewById(bgId);
						if (bg != null) {
							bg.setAlpha(1.0f);
							bg.setBackgroundResource(viewRes.getIdentifier("common_panel_dark", "drawable", "com.htc.widget.weatherclock"));
							if (bg.getBackground() != null) bg.getBackground().setAlpha(transparency);
						}
					}
				}
			}
		};
		findAndHookMethod("com.htc.launcher.LauncherAppWidgetHostView", lpparam.classLoader, "onHierarchyViewAdded", View.class, View.class, hook);
		findAndHookMethod("com.htc.launcher.LauncherAppWidgetHostView", lpparam.classLoader, "onHierarchyViewUpdated", View.class, hook);
	}
	
	public static void execHook_invisiWidgetFix(LoadPackageParam lpparam) {
		String WeatherClock4x1ViewClass = "com.htc.widget.weatherclock.view.WeatherClock4x1View";
		String getControlsMethod = "getControls";
		String WidgetDataClass = "com.htc.widget.weatherclock.util.WidgetData";
		String getGraphicTypeMethod = "getGraphicType";
		if (Helpers.is443plus() && !Helpers.isLP()) {
			// Stupid HTC using ProGuard on widgets...
			WeatherClock4x1ViewClass = "com.htc.widget.weatherclock.a.d";
			getControlsMethod = "a";
			WidgetDataClass = "com.htc.widget.weatherclock.util.t";
			getGraphicTypeMethod = "a";
		}
				
		findAndHookMethod(WeatherClock4x1ViewClass, lpparam.classLoader, getControlsMethod, Context.class, WidgetDataClass, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Bundle result = (Bundle) param.getResult();
				result.putInt("digital_background", result.getInt("background_night"));
				result.putInt("background_day", result.getInt("background_night"));
				result.putInt("point_day", result.getInt("point_night"));
				result.putInt("divider_day", result.getInt("divider_night"));
				result.putInt("text_day", result.getInt("text_night"));
				result.putInt("error_day", result.getInt("error_night"));
				result.putIntArray("number_day", result.getIntArray("number_night"));
				param.setResult(result);
			}
		});
		
		if (Helpers.isSense7()) {
			findAndHookMethod(WeatherClock4x1ViewClass, lpparam.classLoader, "getAllViews", WidgetDataClass, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					Object widgetData = param.args[0];
					if (widgetData != null) {
						Object mWidgetInfo = XposedHelpers.getObjectField(widgetData, "mWidgetInfo");
						if (mWidgetInfo != null) {
							int widgetType = XposedHelpers.getIntField(mWidgetInfo, "widgetType");
							if (widgetType == 7) XposedHelpers.setObjectField(widgetData, "mDayNight", null);
						}
					}
				}
			});
		} else {
			findAndHookMethod(WeatherClock4x1ViewClass, lpparam.classLoader, getGraphicTypeMethod, boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					param.args[0] = false;
				}
			});
		}
		/*
		findAndHookMethod(WeatherClock4x1ViewClass, lpparam.classLoader, "d", WidgetDataClass, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Object widgetData = param.args[0];
				Object mDayNight = XposedHelpers.getObjectField(widgetData, "i");
				XposedBridge.log(String.valueOf(XposedHelpers.getBooleanField(mDayNight, "a")));
				XposedHelpers.setBooleanField(mDayNight, "a", true);
			}
		});
		*/
	}
	
	public static void execHook_googleSearchWidget(final InitPackageResourcesParam resparam, final int option) {
		resparam.res.hookLayout("com.google.android.googlequicksearchbox", "layout", "search_widget", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				ViewGroup searchView = (ViewGroup)liparam.view;
				if (searchView != null) {
					XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, resparam.res);
					Resources res = searchView.getResources();
					
					if (option == 2) {
						Drawable bkg = searchView.getBackground();
						if (bkg != null) bkg.setAlpha(0);
					} else if (option == 3) {
						searchView.setBackground(modRes.getDrawable(R.drawable.searchbox_bg));
					}
					
					ImageView logo = (ImageView)searchView.findViewById(res.getIdentifier("search_widget_logo_image", "id", "com.google.android.googlequicksearchbox"));
					if (logo != null) {
						logo.setImageDrawable(modRes.getDrawable(R.drawable.searchbox_google));
						logo.setId(View.NO_ID);
					}
					
					ImageButton imgbtn = (ImageButton)searchView.findViewById(res.getIdentifier("search_widget_voice_btn", "id", "com.google.android.googlequicksearchbox"));
					if (imgbtn != null) imgbtn.setColorFilter(Color.argb(255, 230, 230, 230));
				}
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
		
		try {
			findAndHookMethod("com.htc.launcher.folder.Folder", lpparam.classLoader, "isFull", findClass("com.htc.launcher.folder.FolderInfo", lpparam.classLoader), Context.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(false);
				}
			});
		} catch (Throwable t) {
			findAndHookMethod("com.htc.launcher.folder.Folder", lpparam.classLoader, "isFull", findClass("com.htc.launcher.folder.FolderInfo", lpparam.classLoader), new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(false);
				}
			});
		}
		
		XposedBridge.hookAllConstructors(findClass("com.htc.launcher.folder.Folder", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					setBooleanField(param.thisObject, "m_bMultiplePage", true);
					setStaticIntField(param.thisObject.getClass(), "FOLDER_MAX_COUNT", 9999);
				} catch (Throwable t) {}
				
				try {
					setBooleanField(param.thisObject, "SUPPORT_MULTIPLE_PAGE", true);
				} catch (Throwable t) {}
			}
		});
		
		try {
			findAndHookMethod("com.htc.launcher.folder.Folder", lpparam.classLoader, "setMultiplePage", boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(null);
				}
			});
		} catch (Throwable t) {}
		
		findAndHookMethod("com.htc.launcher.pageview.CheckedAppsDataManager", lpparam.classLoader, "setMaxCheckedAmount", int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				setIntField(param.thisObject, "m_MaxCheckedAmount", 9999);
			}
		});
		
		try {
			findAndHookMethod("com.htc.launcher.folder.Folder", lpparam.classLoader, "getFolderMaxPages", findClass("com.htc.launcher.folder.FolderInfo", lpparam.classLoader), Context.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(-1);
				}
			});
			
			findAndHookMethod("com.htc.launcher.folder.Folder", lpparam.classLoader, "onDragStart", "com.htc.launcher.DragSource", Object.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Object m_FolderDataManager = XposedHelpers.getObjectField(param.thisObject, "m_FolderDataManager");
					Object overrideDragSoruce = XposedHelpers.callMethod(m_FolderDataManager, "overrideDragSoruce");
					if (overrideDragSoruce == param.args[0]) XposedHelpers.callMethod(param.thisObject, "enterEditMode");
				}
			});
		} catch (Throwable t) {}
	}
	
	public static void execHook_InvisiFolder(final InitPackageResourcesParam resparam, final int transparency) {
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
	}
	
	private static void InvisiFolder_Snippet(LayoutInflatedParam liparam, final InitPackageResourcesParam resparam, final int transparency) {
		RelativeLayout bg = (RelativeLayout)liparam.view;
		bg.getBackground().setAlpha(transparency);
	}
	
	public static void execHook_InvisiFolderBkg(final InitPackageResourcesParam resparam, final int transparency) {
		final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.htc.launcher", "drawable", "home_folder_base", new XResources.DrawableLoader() {
			@Override
			public Drawable newDrawable(XResources res, int id) throws Throwable {
				try {
					Drawable bg = modRes.getDrawable(R.drawable.home_folder_base);
					bg.setAlpha(transparency);
					return bg;
				} catch (Throwable t){
					return null;
				}
			}
		});
	}
	
	public static void execHook_InvisiDrawerRes(InitPackageResourcesParam resparam) {
		try {
			resparam.res.setReplacement("com.htc.launcher", "integer", "config_workspaceUnshrinkTime", 200);
			resparam.res.setReplacement("com.htc.launcher", "integer", "config_appsCustomizeWorkspaceShrinkTime", 70);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void execHook_InvisiDrawerCode(LoadPackageParam lpparam, final int transparency) {
		execHook_PreserveWallpaper(lpparam);
		
		XposedBridge.hookAllConstructors(findClass("com.htc.launcher.pageview.AllAppsPagedViewHost", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				((FrameLayout)param.thisObject).getBackground().setAlpha(transparency);
			}
		});
		
		try {
			findAndHookMethod("com.htc.launcher.pageview.AllAppsPagedViewHost", lpparam.classLoader, "init", Context.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					((FrameLayout)param.thisObject).getBackground().setAlpha(transparency);
				}
			});
			
			findAndHookMethod("com.htc.launcher.util.Utilities", lpparam.classLoader, "overlayAllAppsImageBackground", Context.class, Drawable.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					BitmapDrawable wall = (BitmapDrawable)param.getResult();
					wall.setAlpha(transparency);
					param.setResult(wall);
				}
			});
			
			findAndHookMethod("com.htc.launcher.util.Utilities", lpparam.classLoader, "getAllAppsColorBackground", Context.class, Drawable.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					ColorDrawable wall = (ColorDrawable)param.getResult();
					wall.setAlpha(transparency);
					param.setResult(wall);
				}
			});
		} catch (Throwable t) {}
		/*
		findAndHookMethod("com.htc.launcher.DragLayer", lpparam.classLoader, "setBackgroundAlpha", float.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				boolean isAllAppsOpen = false;
				Object m_launcher = XposedHelpers.getObjectField(param.thisObject, "m_launcher");
				if (m_launcher != null)
				isAllAppsOpen = (Boolean)XposedHelpers.callMethod(m_launcher, "isAllAppsShown");
				XposedBridge.log("setBackgroundAlpha: " + String.valueOf((Float)param.args[0]));
				if (isAllAppsOpen)
					param.args[0] = 0;
				else if ((Float)param.args[0] > transparency/255.0f)
					param.args[0] = transparency/255.0f;
			}
		});
		*/
		// Animate Workspace alpha during transition between Workspace and AllApps
		final Class<?> Properties = XposedHelpers.findClass("com.htc.launcher.LauncherViewPropertyAnimator.Properties", lpparam.classLoader);
		findAndHookMethod("com.htc.launcher.LauncherViewPropertyAnimator", lpparam.classLoader, "start", new XC_MethodHook() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				EnumSet m_propertiesToSet = (EnumSet)XposedHelpers.getObjectField(param.thisObject, "m_propertiesToSet");
				Enum SCALE_X = (Enum)XposedHelpers.getStaticObjectField(Properties, "SCALE_X");
				Enum SCALE_Y = (Enum)XposedHelpers.getStaticObjectField(Properties, "SCALE_Y");
				if (m_propertiesToSet.contains(SCALE_X) && m_propertiesToSet.contains(SCALE_Y)) {
					float m_fScaleX = XposedHelpers.getFloatField(param.thisObject, "m_fScaleX");
					float m_fScaleY = XposedHelpers.getFloatField(param.thisObject, "m_fScaleY");
					
					Enum ALPHA = (Enum)XposedHelpers.getStaticObjectField(Properties, "ALPHA");
					if (m_fScaleX == 0.5f && m_fScaleY == 0.5f) {
						m_propertiesToSet.add(ALPHA);
						XposedHelpers.setFloatField(param.thisObject, "m_fAlpha", 0.0f);
					} else if (m_fScaleX == 1.33f && m_fScaleY == 1.33f) {
						m_propertiesToSet.add(ALPHA);
						XposedHelpers.setFloatField(param.thisObject, "m_fAlpha", 1.0f);
					}
				}
			}
		});
		
		// Hide dock
		findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "showAllApps", boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				XposedHelpers.callMethod(param.thisObject, "hideHotseat", param.args[0]);
			}
		});
		
		// Hide page indicator, nothing to indicate in appdrawer
		findAndHookMethod("com.htc.launcher.Workspace", lpparam.classLoader, "showPageIndicator", boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				Object m_launcher = XposedHelpers.getObjectField(param.thisObject, "m_launcher");
				Enum<?> m_state = (Enum<?>)XposedHelpers.getObjectField(m_launcher, "m_state");
				if (m_state != null) {
					boolean isForAllApps = (Boolean)XposedHelpers.callMethod(m_state, "isForAllApps");
					if (isForAllApps) param.setResult(null);
				}
			}
		});
	}
	
	public static void execHook_AppDrawerGridSizes(LoadPackageParam lpparam) {
		try {
			// Override grid size with current value
			findAndHookMethod("com.htc.launcher.pageview.AllAppsDataManager", lpparam.classLoader, "setupGrid", Context.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					int cellX = 3;
					int cellY = 4;
					
					if (gridSizeVal == 0) {
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
					
					XposedHelpers.callMethod(param.thisObject, "setCellCountX", cellX);
					XposedHelpers.callMethod(param.thisObject, "setCellCountY", cellY);
					
					// Calculate item width/height
					if (gridSizeVal > 0) {
						Context ctx = (Context)param.args[0];
						WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
						Display display = wm.getDefaultDisplay();
						Point size = new Point();
						display.getSize(size);
						XposedHelpers.callMethod(param.thisObject, "setItemViewWidth", Math.round(size.x / (cellX + 0.5f)));
						XposedHelpers.callMethod(param.thisObject, "setItemViewHeight", Math.round(size.y / (cellY + 1.5f)));
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
			findAndHookMethod("com.htc.launcher.pageview.AllAppsOptionsManager", lpparam.classLoader, "saveGridSize", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Context m_Context = (Context)XposedHelpers.getObjectField(param.thisObject, "m_Context");
					SharedPreferences.Editor editor = m_Context.getSharedPreferences("launcher.preferences", 0).edit();
					editor.putInt("grid_size_override", gridSizeVal).commit();
				}
			});
			
			// 	Load grid size from Sense launcher preferences
			findAndHookMethod("com.htc.launcher.pageview.AllAppsOptionsManager", lpparam.classLoader, "loadGridSize", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Context m_Context = (Context)XposedHelpers.getObjectField(param.thisObject, "m_Context");
					SharedPreferences prefs = m_Context.getSharedPreferences("launcher.preferences", 0);
					if (prefs.contains("grid_size_override")) gridSizeVal = prefs.getInt("grid_size_override", 0);
				}
			});
			
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
					param.args[2] = 0.85f;
					param.args[3] = true;
					// param.args[1] = Color.argb(153, 0, 0, 0);
					// m_nEditLayoutPageSpacing
					// param.args[4] = 300;
				}
			});
			
			// Make rearrange arrows transparent
			findAndHookMethod("com.htc.launcher.bar.AllAppsDropTargetBar", lpparam.classLoader, "setup", "com.htc.launcher.Launcher", "com.htc.launcher.DragController", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					((LinearLayout)param.thisObject).setBackgroundColor(Color.TRANSPARENT);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	// Add 5x5, 4x6 and 5x6 grid options to dialog
	public static void execHook_AppDrawerGridSizesLayout(final InitPackageResourcesParam resparam) {
		int apps_grid_option = resparam.res.getIdentifier("apps_grid_options", "array", "com.htc.launcher");
		if (apps_grid_option == 0)
		apps_grid_option = resparam.res.getIdentifier("apps_grid_option", "array", "com.htc.launcher");
		String[] gridSizes = resparam.res.getStringArray(apps_grid_option);
		
		final int n = gridSizes.length;
		gridSizes = Arrays.copyOf(gridSizes, n + 3);
		gridSizes[n] = "5 \u00d7 5";
		gridSizes[n + 1] = "4 \u00d7 6";
		gridSizes[n + 2] = "5 \u00d7 6";
		
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
						if (gridSizeVal == 3 || gridSizeVal == 4) {
							itemlabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, 0.9f * itemlabel.getTextSize());
							if (Helpers.isEight()) {
								itemlabel.setSingleLine(true);
								itemlabel.setEllipsize(TruncateAt.END);
							}
						}
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
	
	public static void execHook_HomeScreenGridSize(InitPackageResourcesParam resparam) {
		XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, resparam.res);
		
		try {
			resparam.res.setReplacement(resparam.res.getIdentifier("cell_count_x", "integer", "com.htc.launcher"), 4);
			resparam.res.setReplacement(resparam.res.getIdentifier("cell_count_y", "integer", "com.htc.launcher"), 5);
			resparam.res.setReplacement(resparam.res.getIdentifier("celllayout_top_padding_port", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.celllayout_top_padding_port));
			resparam.res.setReplacement(resparam.res.getIdentifier("workspace_height_gap_port", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.workspace_height_gap_port));
			resparam.res.setReplacement(resparam.res.getIdentifier("celllayout_top_padding", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.celllayout_top_padding_port));
			resparam.res.setReplacement(resparam.res.getIdentifier("workspace_height_gap", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.workspace_height_gap_port));
			
			if (Helpers.isEight()) {
				resparam.res.setReplacement(resparam.res.getIdentifier("app_icon_padding_top", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.app_icon_padding_top_port));
				resparam.res.setReplacement(resparam.res.getIdentifier("app_icon_padding_top_port", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.app_icon_padding_top_port));
				resparam.res.setReplacement(resparam.res.getIdentifier("folder_icon_top_margin_workspace", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.folder_icon_top_margin_workspace_port));
				resparam.res.setReplacement(resparam.res.getIdentifier("folder_icon_top_margin_workspace_port", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.folder_icon_top_margin_workspace_port));
				
				resparam.res.setReplacement(resparam.res.getIdentifier("hotseat_toggle_icon_padding_top", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.hotseat_toggle_icon_padding_top));
				resparam.res.setReplacement(resparam.res.getIdentifier("hotseat_cell_height", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.hotseat_cell_height));
				
				resparam.res.setReplacement(resparam.res.getIdentifier("button_bar_height_without_padding", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.button_bar_height_without_padding_soft));
				resparam.res.setReplacement(resparam.res.getIdentifier("button_bar_height_plus_padding", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.button_bar_height_plus_padding_soft));
				resparam.res.setReplacement(resparam.res.getIdentifier("celllayout_bottom_padding_port", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.celllayout_bottom_padding_port_soft));
				resparam.res.setReplacement(resparam.res.getIdentifier("celllayout_bottom_padding", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.celllayout_bottom_padding_port_soft));
				resparam.res.setReplacement(resparam.res.getIdentifier("workspace_cell_height_port", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.workspace_cell_height_port_soft));
				resparam.res.setReplacement(resparam.res.getIdentifier("workspace_cell_height", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.workspace_cell_height_port_soft));
			} else {
				resparam.res.setReplacement(resparam.res.getIdentifier("button_bar_height_without_padding", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.button_bar_height_without_padding));
				resparam.res.setReplacement(resparam.res.getIdentifier("celllayout_bottom_padding_port", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.celllayout_bottom_padding_port));
				resparam.res.setReplacement(resparam.res.getIdentifier("celllayout_bottom_padding", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.celllayout_bottom_padding_port));
				resparam.res.setReplacement(resparam.res.getIdentifier("workspace_cell_height_port", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.workspace_cell_height_port));
				resparam.res.setReplacement(resparam.res.getIdentifier("workspace_cell_height", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.workspace_cell_height_port));
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		
		// Sense 8
		try {
			resparam.res.setReplacement(resparam.res.getIdentifier("workspace_height_gap_port_4x5", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.workspace_height_gap_port));
			if (Helpers.isEight()) {
				resparam.res.setReplacement(resparam.res.getIdentifier("workspace_cell_height_port_4x5", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.workspace_cell_height_port_soft));				
			} else {
				resparam.res.setReplacement(resparam.res.getIdentifier("workspace_cell_height_port_4x5", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.workspace_cell_height_port));
				resparam.res.setReplacement(resparam.res.getIdentifier("hotseat_extra_height_no_navbar", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.hotseat_extra_height_no_navbar));
			}
		} catch (Throwable t) {}
	}
	
	public static boolean bNoSystemNavBar = false;
	public static void execHook_HomeScreenHidePageIndicator(final LoadPackageParam lpparam, boolean customGrid) {
		try {
			if (customGrid)
			XposedHelpers.findAndHookMethod("com.htc.launcher.Workspace", lpparam.classLoader, "onFitSystemWindows", Rect.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					XposedHelpers.setBooleanField(param.thisObject, "bNoSystemNavBar", false);
				}
			}); else
			XposedHelpers.findAndHookMethod("com.htc.launcher.Workspace", lpparam.classLoader, "hideScrollingIndicator", boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					bNoSystemNavBar = XposedHelpers.getBooleanField(param.thisObject, "bNoSystemNavBar");
					XposedHelpers.setBooleanField(param.thisObject, "bNoSystemNavBar", false);
				}
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					XposedHelpers.setBooleanField(param.thisObject, "bNoSystemNavBar", bNoSystemNavBar);
				}
			});
		} catch (Throwable t) {}
	}
	
	public static void execHook_HomeScreenGapFix(InitPackageResourcesParam resparam) {
		XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, resparam.res);
		try {
			resparam.res.setReplacement(resparam.res.getIdentifier("workspace_height_gap_port", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.workspace_height_gap_fix));
			resparam.res.setReplacement(resparam.res.getIdentifier("workspace_height_gap", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.workspace_height_gap_fix));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		try {
			resparam.res.setReplacement(resparam.res.getIdentifier("workspace_height_gap_port_4x4", "dimen", "com.htc.launcher"), modRes.fwd(R.dimen.workspace_height_gap_fix));
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
		
		try {
			XposedBridge.hookAllConstructors(findClass("com.htc.launcher.WidgetResizeFrame", lpparam.classLoader), new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					XposedHelpers.setIntField(param.thisObject, "mResizeMode", 3);
				}
			});
		} catch (Throwable t) {
			XposedBridge.hookAllConstructors(findClass("com.htc.launcher.AppWidgetResizeFrame", lpparam.classLoader), new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					XposedHelpers.setIntField(param.thisObject, "mResizeMode", 3);
				}
			});
		}
		
		XposedHelpers.findAndHookMethod("com.htc.launcher.LauncherAppWidgetInfo", lpparam.classLoader, "getSupportsSizes", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				param.setResult(null);
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
		
		SwipeListener(Context context) {
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
				XMain.pref.reload();
				switch (Integer.parseInt(XMain.pref.getString("pref_key_prism_swipedownaction", "1"))) {
					case 2: return GlobalActions.expandNotifications(helperContext);
					case 3: return GlobalActions.expandEQS(helperContext);
					case 4: return GlobalActions.lockDevice(helperContext);
					case 5: return GlobalActions.goToSleep(helperContext);
					case 6: return GlobalActions.takeScreenshot(helperContext);
					case 7: return GlobalActions.launchApp(helperContext, 1);
					case 8: return GlobalActions.toggleThis(helperContext, Integer.parseInt(XMain.pref.getString("pref_key_prism_swipedown_toggle", "0")));
					case 12: return GlobalActions.launchShortcut(helperContext, 1);
					case 14: return GlobalActions.openAppDrawer(helperContext);
					default: return false;
				}
			}
			
			if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				XMain.pref.reload();
				switch (Integer.parseInt(XMain.pref.getString("pref_key_prism_swipeupaction", "1"))) {
					case 2: return GlobalActions.expandNotifications(helperContext);
					case 3: return GlobalActions.expandEQS(helperContext);
					case 4: return GlobalActions.lockDevice(helperContext);
					case 5: return GlobalActions.goToSleep(helperContext);
					case 6: return GlobalActions.takeScreenshot(helperContext);
					case 7: return GlobalActions.launchApp(helperContext, 2);
					case 8: return GlobalActions.toggleThis(helperContext, Integer.parseInt(XMain.pref.getString("pref_key_prism_swipeup_toggle", "0")));
					case 12: return GlobalActions.launchShortcut(helperContext, 2);
					case 14: return GlobalActions.openAppDrawer(helperContext);
					default: return false;
				}
			}
			
			return false;
		}
	}
	
	public static void execHook_DockSwipe(final LoadPackageParam lpparam) {
		XposedHelpers.findAndHookMethod("com.htc.launcher.hotseat.Hotseat", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				MotionEvent ev = (MotionEvent)param.args[0];
				if (ev == null) return;
				
				FrameLayout hotSeat = (FrameLayout)param.thisObject;
				Context helperContext = hotSeat.getContext();
				if (helperContext == null) return;
				if (mDetectorHorizontal == null) mDetectorHorizontal = new GestureDetector(helperContext, new SwipeListenerHorizontal(hotSeat));
				mDetectorHorizontal.onTouchEvent(ev);
			}
		});
		
		XposedHelpers.findAndHookMethod("com.htc.launcher.DragLayer", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				MotionEvent ev = (MotionEvent)param.args[0];
				if (ev == null) return;
				
				FrameLayout dragLayer = (FrameLayout)param.thisObject;
				launcher = XposedHelpers.getObjectField(dragLayer, "m_launcher");
				Context helperContext = dragLayer.getContext();
				if (helperContext == null) return;
				if (mDetectorVertical == null) mDetectorVertical = new GestureDetector(helperContext, new SwipeListenerVertical(helperContext));
				if (mDetectorVertical.onTouchEvent(ev)) param.setResult(true);
			}
		});
	}
	
	// Listener for hotizontal swipes on dock
	private static class SwipeListenerHorizontal extends GestureDetector.SimpleOnGestureListener {
		// For HTC One
		private int SWIPE_MIN_DISTANCE_HORIZ = 230;
		private int SWIPE_THRESHOLD_VELOCITY = 100;
		
		final Context helperContext;
		
		SwipeListenerHorizontal(Object cellLayout) {
			helperContext = ((ViewGroup)cellLayout).getContext();
			float density = helperContext.getResources().getDisplayMetrics().density;
			SWIPE_MIN_DISTANCE_HORIZ = Math.round(75 * density);
			SWIPE_THRESHOLD_VELOCITY = Math.round(33 * density);
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1 == null || e2 == null) return false;
			
			if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				XMain.pref.reload();
				switch (Integer.parseInt(XMain.pref.getString("pref_key_prism_swiperightaction", "1"))) {
					case 2: return GlobalActions.expandNotifications(helperContext);
					case 3: return GlobalActions.expandEQS(helperContext);
					case 4: return GlobalActions.lockDevice(helperContext);
					case 5: return GlobalActions.goToSleep(helperContext);
					case 6: return GlobalActions.takeScreenshot(helperContext);
					case 7: return GlobalActions.launchApp(helperContext, 5);
					case 8: return GlobalActions.toggleThis(helperContext, Integer.parseInt(XMain.pref.getString("pref_key_prism_swiperight_toggle", "0")));
					case 12: return GlobalActions.launchShortcut(helperContext, 5);
					case 14: return GlobalActions.openAppDrawer(helperContext);
					default: return false;
				}
			}
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				XMain.pref.reload();
				switch (Integer.parseInt(XMain.pref.getString("pref_key_prism_swipeleftaction", "1"))) {
					case 2: return GlobalActions.expandNotifications(helperContext);
					case 3: return GlobalActions.expandEQS(helperContext);
					case 4: return GlobalActions.lockDevice(helperContext);
					case 5: return GlobalActions.goToSleep(helperContext);
					case 6: return GlobalActions.takeScreenshot(helperContext);
					case 7: return GlobalActions.launchApp(helperContext, 6);
					case 8: return GlobalActions.toggleThis(helperContext, Integer.parseInt(XMain.pref.getString("pref_key_prism_swipeleft_toggle", "0")));
					case 12: return GlobalActions.launchShortcut(helperContext, 6);
					case 14: return GlobalActions.openAppDrawer(helperContext);
					default: return false;
				}
			}
			
			return false;
		}
	}
	
	// Listener for vertical swipes on the bottom of workspace
	private static class SwipeListenerVertical extends GestureDetector.SimpleOnGestureListener {
		// For HTC One
		private int SWIPE_MIN_DISTANCE_VERT = 50;
		private int SWIPE_THRESHOLD_VELOCITY = 100;
		
		float density;
		int screenHeight;
		
		SwipeListenerVertical(Context ctx) {
			density = ctx.getResources().getDisplayMetrics().density;
			screenHeight = ctx.getResources().getDisplayMetrics().heightPixels;
			SWIPE_MIN_DISTANCE_VERT = Math.round(17 * density);
			SWIPE_THRESHOLD_VELOCITY = Math.round(33 * density);
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1 == null || e2 == null) return false;
			
			if (e1.getY() > (screenHeight - density * 100) && e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE_VERT && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				XMain.pref.reload();
				if (XMain.pref.getBoolean("pref_key_prism_homemenu", false)) {
					if (launcher != null) {
						Enum<?> m_state = (Enum<?>)XposedHelpers.getObjectField(launcher, "m_state");
						if (m_state != null && m_state.ordinal() == 0) {
							createAndShowPopup((ViewGroup)XposedHelpers.getObjectField(launcher, "m_workspace"), (Activity)launcher);
							return true;
						}
					}
				}
			}
			
			return false;
		}
	}
	
	private static PopupWindow popup = null;
	
	public static void execHook_HomeMenu(final LoadPackageParam lpparam) {
		XposedHelpers.findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "onKeyDown", int.class, KeyEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				if ((Integer)param.args[0] == KeyEvent.KEYCODE_MENU) {
					Enum<?> m_state = (Enum<?>)XposedHelpers.getObjectField(param.thisObject, "m_state");
					if (m_state != null && m_state.ordinal() == 0) {
						createAndShowPopup((ViewGroup)XposedHelpers.getObjectField(param.thisObject, "m_workspace"), (Activity)param.thisObject);
						param.setResult(true);
					}
				}
			}
		});
	}

	@SuppressLint("InlinedApi")
	public static void createAndShowPopup(ViewGroup m_workspace, final Activity launcherAct) {
		ViewGroup m_workspace_local = m_workspace;
		if (m_workspace_local == null) if (launcherAct != null) m_workspace_local = (ViewGroup)XposedHelpers.getObjectField(launcher, "m_workspace");
		if (m_workspace_local == null) return;
		
		Context ctx;
		if (Helpers.isLP())
			ctx = new ContextThemeWrapper(m_workspace_local.getContext(), android.R.style.Theme_Material_Dialog);
		else
			ctx = m_workspace_local.getContext();
		
		if (popup == null) {
			popup = new PopupWindow(ctx);
			popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
			popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
			popup.setTouchable(true);
			popup.setFocusable(true);
			popup.setOutsideTouchable(true);
		}
		
		ListView options = new ListView(ctx);
		XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
		String[] menu_items = Helpers.xl10n_array(modRes, R.array.home_menu);
		XMain.pref.reload();
		if (!Helpers.isTouchscreenEventsAvailable() || !XMain.pref.getBoolean("touch_lock_active", false))
		menu_items = Arrays.copyOf(menu_items, menu_items.length - 1);
		ListAdapter listAdapter = new PopupAdapter(ctx, menu_items, false);
		options.setAdapter(listAdapter);
		options.setFocusableInTouchMode(true);
		options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				popup.dismiss();
				
				if (position == 0) {
					launcherAct.startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
				} else if (position == 1) {
					launcherAct.startActivity((new Intent("com.htc.personalize.ACTION_HOMEPERSONALIZE")).addCategory(Intent.CATEGORY_DEFAULT));
				} else if (position == 2) {
					launcherAct.startActivity(new Intent(Settings.ACTION_SETTINGS));
				} else if (position == 3) {
					launcherAct.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				} else if (position == 4) {
					OtherMods.startAPM(launcherAct);
				} else if (position == 5) {
					Settings.System.putString(launcherAct.getContentResolver(), "lock_homescreen_dragging", String.valueOf(!Boolean.parseBoolean(Settings.System.getString(launcherAct.getContentResolver(), "lock_homescreen_dragging"))));
				} else if (position == 6) {
					launcherAct.sendBroadcast(new Intent("com.sensetoolbox.six.mods.action.LockDownDevice"));
				}
			}
		});
		options.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
					popup.dismiss();
					return true;
				}
				return false;
			}
		});
		popup.setContentView(options);
		if (Helpers.isLP()) {
			options.setPadding(0, 0, 0, 0);
			options.setDivider(null);
			options.setDividerHeight(0);
			popup.setBackgroundDrawable(new ColorDrawable(0xff404040));
			popup.setWidth(m_workspace_local.getResources().getDisplayMetrics().widthPixels - Math.round(m_workspace_local.getResources().getDisplayMetrics().density * 16));
			popup.setAnimationStyle(m_workspace_local.getResources().getIdentifier("DropDownUpBottomCenter", "style", "com.htc.launcher"));
			options.setDrawSelectorOnTop(true);
		}
		popup.showAtLocation(m_workspace_local, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
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
	
	public static void execHook_invisiLabels(final LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.CellLayout", lpparam.classLoader, "addViewToCellLayout", View.class, int.class, int.class, "com.htc.launcher.CellLayout$LayoutParams", boolean.class, new XC_MethodHook() {
			@Override
			public void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					callMethod(param.args[0], "hideText", true);
					boolean m_bIsHotseat = XposedHelpers.getBooleanField(param.thisObject, "m_bIsHotseat");
					if (!m_bIsHotseat) {
						float density = ((View)param.args[0]).getResources().getDisplayMetrics().density;
						if (XMain.pref.getBoolean("pref_key_controls_smallsoftkeys", false))
							callMethod(param.args[0], "setPadding", 0, Math.round(28 * density), 0, 0);
						else
							callMethod(param.args[0], "setPadding", 0, Math.round(25 * density), 0, 0);
					}
				} catch (Throwable t) {
					//Not an app icon
				}
			}
		});
		
		XposedBridge.hookAllConstructors(findClass("com.htc.launcher.folder.WorkspaceFolderIcon", lpparam.classLoader), new XC_MethodHook() {
			@Override
			public void afterHookedMethod(MethodHookParam param) throws Throwable {
				if (param.thisObject != null)
				setAdditionalInstanceField(param.thisObject, "workspaceFolder", true);
			}
		});
		
		findAndHookMethod("com.htc.launcher.folder.FolderIcon", lpparam.classLoader, "setTextVisible", boolean.class, new XC_MethodHook() {
			@Override
			public void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if (param.thisObject == null) return;
				Boolean workspaceFolder = (Boolean) getAdditionalInstanceField(param.thisObject, "workspaceFolder");
				if (workspaceFolder != null && workspaceFolder) param.args[0] = false;
			}
		});
	}
	
	public static void execHook_hotseatToggleBtn(final LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.hotseat.Hotseat", lpparam.classLoader, "resetLayout", new XC_MethodHook() {
			@Override
			public void afterHookedMethod(final MethodHookParam param) throws Throwable {
				TextView m_toggleButton = (TextView)XposedHelpers.getObjectField(param.thisObject, "m_toggleButton");
				//final Object m_launcher = XposedHelpers.getObjectField(param.thisObject, "m_launcher");
				m_toggleButton.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						XMain.pref.reload();
						Context ctx = v.getContext();
						switch (Integer.parseInt(XMain.pref.getString("pref_key_prism_appslongpressaction", "1"))) {
							case 2: return GlobalActions.expandNotifications(ctx);
							case 3: return GlobalActions.expandEQS(ctx);
							case 4: return GlobalActions.lockDevice(ctx);
							case 5: return GlobalActions.goToSleep(ctx);
							case 6: return GlobalActions.takeScreenshot(ctx);
							case 7: return GlobalActions.launchApp(ctx, 8);
							case 8: return GlobalActions.toggleThis(ctx, Integer.parseInt(XMain.pref.getString("pref_key_prism_appslongpress_toggle", "0")));
							case 12: return GlobalActions.launchShortcut(ctx, 8);
							case 14: return GlobalActions.openAppDrawer(ctx);
							default: return false;
						}
						//if ((Boolean)XposedHelpers.callMethod(m_launcher, "isAllAppsShown"))
						//	XposedHelpers.callMethod(m_launcher, "showWorkspace", false);
						//else
						//	XposedHelpers.callMethod(m_launcher, "showAllApps", false);
					}
				});
			}
		});
	}
	
	private static boolean isLauncherLocked(Context context) {
		return Boolean.parseBoolean(Settings.System.getString(context.getContentResolver(), "lock_homescreen_dragging"));
	}
	
	private static synchronized void showLockedWarning(final Activity act) {
		if (dlg == null) {
			XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
			HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(act);
			builder.setTitle(Helpers.xl10n(modRes, R.string.warning));
			builder.setMessage(Helpers.xl10n(modRes, R.string.locked_warning));
			builder.setCancelable(false);
			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton){
				}
			});
			dlg = builder.create();
		}
		if (!dlg.isShowing()) dlg.show();
	}
	
	private static synchronized void showLockedWarningStock(final Activity act) {
		if (dlgStock == null) {
			XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			builder.setTitle(Helpers.xl10n(modRes, R.string.warning));
			builder.setMessage(Helpers.xl10n(modRes, R.string.locked_warning));
			builder.setCancelable(false);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton){
				}
			});
			dlgStock = builder.create();
		}
		if (!dlgStock.isShowing()) dlgStock.show();
	}
	
	public static void execHook_LauncherLock(final LoadPackageParam lpparam) {
		// Disable dragging inside folders
		XposedBridge.hookAllConstructors(findClass("com.htc.launcher.folder.Folder.FolderDataManager", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mContext", param.args[0]);
			}
		});
		
		XposedHelpers.findAndHookMethod("com.htc.launcher.folder.Folder.FolderDataManager", lpparam.classLoader, "allowedDrag", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Context ctx = null;
				try {
					ctx = (Context)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mContext");
				} catch (Throwable t) {
					RelativeLayout folder = (RelativeLayout)XposedHelpers.getSurroundingThis(param.thisObject);
					ctx = folder.getContext();
				}
				if (ctx != null && isLauncherLocked(ctx)) param.setResult(false);
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
				Activity launcherAct = (Activity)param.thisObject;
				if (launcherAct != null && isLauncherLocked(launcherAct)) param.setResult(false);
			}
		});
		
		// Disable homescreen customization
		XposedHelpers.findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "showAddToHome", boolean.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Activity launcherAct = (Activity)param.thisObject;
				if (launcherAct != null && isLauncherLocked(launcherAct)) {
					if (Helpers.isNewSense())
						showLockedWarningStock(launcherAct);
					else
						showLockedWarning(launcherAct);
					param.setResult(null);
				}
			}
		});
		
		XposedHelpers.findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "onNewIntent", Intent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					Intent intent = (Intent)param.args[0];
					String s = intent.getAction();
					if (s.equals(Intent.ACTION_MAIN)) {
						boolean isAddToHome = intent.getBooleanExtra("personalize_add_to_home", false);
						if (isAddToHome) {
							Activity launcherAct = (Activity)param.thisObject;
							if (launcherAct != null && isLauncherLocked(launcherAct)) {
								if (Helpers.isNewSense())
									showLockedWarningStock(launcherAct);
								else
									showLockedWarning(launcherAct);
								param.setResult(null);
							}
						}
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
	
	public static void execHook_ShakeAction(final LoadPackageParam lpparam) {
		final String shakeMgrKey = "ST_SHAKE_MGR";
		XposedHelpers.findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "onResume", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				ShakeManager shakeMgr = (ShakeManager) getAdditionalInstanceField(param.thisObject, shakeMgrKey);
				if (shakeMgr == null) {
					shakeMgr = new ShakeManager((Context) param.thisObject);
					setAdditionalInstanceField(param.thisObject, shakeMgrKey, shakeMgr);
				}
				Activity launcherActivity = (Activity) param.thisObject;
				SensorManager sensorMgr = (SensorManager) launcherActivity.getSystemService(Context.SENSOR_SERVICE);
				shakeMgr.reset();
				sensorMgr.registerListener(shakeMgr, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
			}
		});
		
		XposedHelpers.findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "onPause", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				if(getAdditionalInstanceField(param.thisObject, shakeMgrKey) == null)
					return;
				Activity launcherActivity = (Activity) param.thisObject;
				SensorManager sensorMgr = (SensorManager) launcherActivity.getSystemService(Context.SENSOR_SERVICE);
				sensorMgr.unregisterListener((ShakeManager) getAdditionalInstanceField(param.thisObject, shakeMgrKey));
			}
		});
	}
	
	private static void changeDockState(MethodHookParam param) {
		try {
			Object m_launcher = XposedHelpers.getObjectField(param.thisObject, "m_launcher");
			if (m_launcher != null) {
				Object m_dragLayer = XposedHelpers.getObjectField(m_launcher, "m_dragLayer");
				Object m_hotseat = XposedHelpers.getObjectField(m_launcher, "m_hotseat");
				if (m_dragLayer != null && m_hotseat != null) {
					ImageView m_NavBarExtraBg = (ImageView)XposedHelpers.getObjectField(m_dragLayer, "m_NavBarExtraBg");
					boolean m_bShown = XposedHelpers.getBooleanField(m_hotseat, "m_bShown");
					if ((Boolean)XposedHelpers.callMethod(param.thisObject, "isFeedPage")) {
						if (m_bShown) XposedHelpers.callMethod(m_hotseat, "hide", true);
						if (m_NavBarExtraBg != null) m_NavBarExtraBg.setVisibility(View.GONE);
					} else {
						if (!m_bShown) XposedHelpers.callMethod(m_hotseat, "show", true);
						if (m_NavBarExtraBg != null) m_NavBarExtraBg.setVisibility(View.VISIBLE);
					}
				}
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_BlinkFeedNoDock(final LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.launcher.Workspace", lpparam.classLoader, "onPageEndMoving", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				changeDockState(param);
			}
		});
		
		findAndHookMethod("com.htc.launcher.Workspace", lpparam.classLoader, "onStopOnFeedPage", int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				changeDockState(param);
			}
		});
		
		findAndHookMethod("com.htc.launcher.Workspace", lpparam.classLoader, "onLeaveFeedPage", int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				changeDockState(param);
			}
		});
		
		findAndHookMethod("com.htc.launcher.hotseat.Hotseat", lpparam.classLoader, "show", boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					Object m_launcher = XposedHelpers.getObjectField(param.thisObject, "m_launcher");
					if (m_launcher != null) {
						Object m_workspace = XposedHelpers.getObjectField(m_launcher, "m_workspace");
						if (m_workspace != null && (Boolean)XposedHelpers.callMethod(m_workspace, "isFeedPage")) param.setResult(null);
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
		
		if (Helpers.isEight()) {
			findAndHookMethod("com.htc.launcher.hotseat.Hotseat", lpparam.classLoader, "hide", boolean.class, new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					FrameLayout hotseat = (FrameLayout) param.thisObject;
					if (hotseat == null)
						return null;
					
					hotseat.animate().cancel();
					int duration = (int) callStaticMethod(findClass("com.htc.launcher.bar.BarController", lpparam.classLoader), "getTransitionOutDuration", new Object[]{});
					boolean animate = (boolean) param.args[0];
					int add = 70;
					
					if (animate) {
						if (hotseat.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
							hotseat.setTranslationX(0.0F);
							hotseat.animate().translationY(hotseat.getMeasuredHeight() + add).setDuration(duration);
						} else {
							hotseat.setTranslationY(0.0F);
							hotseat.animate().translationX(hotseat.getMeasuredWidth() + add).setDuration(duration);
						}
					} else
					if (hotseat.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
						hotseat.setTranslationX(0.0F);
						hotseat.setTranslationY(hotseat.getMeasuredHeight() + add);
					} else {
						hotseat.setTranslationY(0.0F);
						hotseat.setTranslationX(hotseat.getMeasuredWidth() + add);
					}
					setObjectField(param.thisObject, "m_bShown", false);
					
					return null;
				}
			});
		}
	}
	
	public static void execHook_BlinkFeedImmersive(final LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.plugin.news.NewsDetailActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				if (act != null) {
					act.requestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
					act.requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
				}
			}
			
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				if (act != null) {
					View mail_layout_view = act.findViewById(act.getResources().getIdentifier("main_layout", "id", act.getPackageName()));
					if (mail_layout_view != null) mail_layout_view.setFitsSystemWindows(false);
					act.getActionBar().hide();
					act.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
				}
			}
		});
		
		findAndHookMethod("com.htc.plugin.news.NewsDetailActivity", lpparam.classLoader, "onCreateOptionsMenu", Menu.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				MenuItem mShareMenuItem = (MenuItem)XposedHelpers.getObjectField(param.thisObject, "mShareMenuItem");
				if (mShareMenuItem != null) mShareMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
				MenuItem mReadLaterMenuItem = (MenuItem)XposedHelpers.getObjectField(param.thisObject, "mReadLaterMenuItem");
				if (mReadLaterMenuItem != null) mReadLaterMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			}
		});
	}
	
	public static void execHook_ActionBarNoBkg(final LoadPackageParam lpparam) {
		XposedBridge.hookAllConstructors(findClass("com.htc.launcher.masthead.AllAppsActionBar", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				XposedHelpers.callMethod(param.thisObject, "setBackgroundVisible", false);
			}
		});
		
		try {
			findAndHookMethod("com.htc.launcher.masthead.AllAppsActionBar", lpparam.classLoader, "dispatchConfigurationChanged", Configuration.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.callMethod(param.thisObject, "setBackgroundVisible", false);
				}
			});
			
			findAndHookMethod("com.htc.launcher.masthead.AllAppsActionBar", lpparam.classLoader, "initial", Context.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.callMethod(param.thisObject, "setBackgroundVisible", false);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_HotSeatNoBkg(InitPackageResourcesParam resparam) {
		resparam.res.setReplacement("com.htc.launcher", "drawable", "home_launcher_bg", new XResources.DrawableLoader() {
			@Override
			public Drawable newDrawable(XResources res, int id) throws Throwable {
				return new ColorDrawable(Color.TRANSPARENT);
			}
		});
		
		resparam.res.setReplacement("com.htc.launcher", "drawable", "prism_feed_gradient", new XResources.DrawableLoader() {
			@Override
			public Drawable newDrawable(XResources res, int id) throws Throwable {
				return new ColorDrawable(Color.TRANSPARENT);
			}
		});
	}
	
	public static void execHook_InvisiMusicWidget(InitPackageResourcesParam resparam) {
		resparam.res.setReplacement("com.htc.MusicWidget", "drawable", "common_panel_light", new XResources.DrawableLoader() {
			@Override
			public Drawable newDrawable(XResources res, int id) throws Throwable {
				return new ColorDrawable(Color.TRANSPARENT);
			}
		});
		
		resparam.res.hookLayout("com.htc.MusicWidget", "layout", "specific_grid44", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				ViewGroup widget = (ViewGroup)liparam.view;
				if (widget != null) {
					ImageView footer = (ImageView)widget.findViewById(widget.getResources().getIdentifier("footer", "id", "com.htc.MusicWidget"));
					if (footer != null) footer.setAlpha(0f);
				}
			}
		});
	}
	
	public static void execHook_InvisiPeopleWidget(InitPackageResourcesParam resparam) {
		resparam.res.setReplacement("com.htc.htccontactwidgets", "drawable", "common_panel_light", new XResources.DrawableLoader() {
			@Override
			public Drawable newDrawable(XResources res, int id) throws Throwable {
				return new ColorDrawable(Color.TRANSPARENT);
			}
		});
		
		resparam.res.hookLayout("com.htc.htccontactwidgets", "layout", "specific_contact_widget_grid", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				ViewGroup widget = (ViewGroup)liparam.view;
				if (widget != null) {
					ImageView arrow_down = (ImageView)widget.findViewById(widget.getResources().getIdentifier("arrow_down", "id", "com.htc.htccontactwidgets"));
					if (arrow_down != null) arrow_down.setAlpha(0f);
				}
			}
		});
	}
	
	public static void execHook_StockTransitionsLauncher(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.htc.launcher.Launcher", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Activity act = (Activity)param.thisObject;
					if (act != null && act.getWindow() != null) act.getWindow().setWindowAnimations(android.R.style.Animation_Activity);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_StockTransitions() {
		try {
			findAndHookMethod(Activity.class, "overridePendingTransition", int.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Activity act = (Activity)param.thisObject;
					if (act != null && act.getPackageName().equals("com.htc.launcher")) param.setResult(null);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_StockTransitionsAnim(final InitPackageResourcesParam resparam) {
		XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.htc.launcher", "anim", "trans_zoom_open", modRes.fwd(R.anim.activity_open_enter));
		resparam.res.setReplacement("com.htc.launcher", "anim", "trans_zoom_close", modRes.fwd(R.anim.activity_close_exit));
	}
	
	public static void execHook_NoAppDrawerPageIndicator(InitPackageResourcesParam resparam) {
		resparam.res.hookLayout("com.htc.launcher", "layout", "specific_all_apps_pagedview", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				if (liparam.view != null) try {
					View pageInd = liparam.view.findViewById(liparam.view.getResources().getIdentifier("allapps_page_indicator", "id", "com.htc.launcher"));
					if (pageInd != null) ((FrameLayout)pageInd.getParent()).setVisibility(View.GONE);
				} catch (Throwable t) {}
			}
		});
	}
}

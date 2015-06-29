package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.htc.fragment.widget.CarouselFragment;
import com.htc.widget.HtcAlertDialog;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.GlobalActions;
import com.sensetoolbox.six.utils.Helpers;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class OtherMods {
	
	public static void execHook_APM() {
		try {
			String className = "com.htc.app.HtcShutdownThread";
			if (Helpers.isLP()) className = "com.android.internal.policy.impl.HtcShutdown.HtcShutdownThread";
			findAndHookMethod(className, null, "reboot", Context.class, String.class, boolean.class, new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					startAPM((Context)param.args[0]);
					return null;
				}
			});
		} catch(Throwable t) {
			XposedBridge.log(t);
		}
	}
		
	public static void startAPM(Context ctx){
		try {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.setClassName("com.sensetoolbox.six", "com.sensetoolbox.six.APMActivity");
			ctx.startActivity(intent);
		} catch(Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_VolSound() {
		findAndHookMethod("com.htc.view.VolumePanel", null, "onPlaySound", int.class, int.class, XC_MethodReplacement.DO_NOTHING);
	}
	
	public static void execHook_VolSound(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.volume.VolumePanel", lpparam.classLoader, "onPlaySound", int.class, int.class, XC_MethodReplacement.DO_NOTHING);
	}
		
	public static void execHook_MTPNotif(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.providers.media.MtpService", lpparam.classLoader, "onStartCommand", Intent.class, int.class, int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					((Service)param.thisObject).stopForeground(true);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_DNDNotif(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.settings.framework.app.HtcDndCommandService", lpparam.classLoader, "addNotification", int.class, boolean.class, XC_MethodReplacement.DO_NOTHING);
		} catch (Throwable t1) {
			try {
				XposedHelpers.findAndHookMethod("com.android.settings.framework.app.HtcDndCommandService", lpparam.classLoader, "addNotification", int.class, XC_MethodReplacement.DO_NOTHING);
			} catch (Throwable t2) {
				XposedBridge.log(t2);
			}
		}
	}
		
	public static void execHook_PowerSaverNotif(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.htc.htcpowermanager.powersaver.PowerSaverNotificationService", lpparam.classLoader, "addNotification", XC_MethodReplacement.DO_NOTHING);
			XposedHelpers.findAndHookMethod("com.htc.htcpowermanager.powersaver.PowerSaverNotificationService", lpparam.classLoader, "addNtfPowerJacket", int.class, int.class, XC_MethodReplacement.DO_NOTHING);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_InputMethodNotif(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.server.InputMethodManagerService", lpparam.classLoader, "setImeWindowStatus", IBinder.class, int.class, int.class, XC_MethodReplacement.DO_NOTHING);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_VZWWiFiNotif() {
		try {
			XposedHelpers.findAndHookMethod("android.net.wifi.WifiStateMachine", null, "sendVzwStatusNotification", int.class, XC_MethodReplacement.returnConstant(0));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static void hookVolumePlanel(MethodHookParam param) {
		Dialog dlg = (Dialog)XposedHelpers.getObjectField(param.thisObject, "mDialog");
		Dialog dlgEx = (Dialog)XposedHelpers.getObjectField(param.thisObject, "mDialogEx");
			
		float density = dlg.getContext().getResources().getDisplayMetrics().density;
		int orientation = dlg.getContext().getResources().getConfiguration().orientation;
		int bottomMargin = 75;
		if (orientation == 2) bottomMargin = 40;
			
		if (dlg != null) {
			Window dlgWin = dlg.getWindow();
			if (dlgWin != null) {
				WindowManager.LayoutParams dlgWinAttrs = dlgWin.getAttributes();
				dlgWinAttrs.gravity = Gravity.BOTTOM;
				dlgWinAttrs.y = Math.round(bottomMargin * density);
				dlgWin.setAttributes(dlgWinAttrs);
			}
		}
		
		if (dlgEx != null) {
			Window dlgExWin = dlgEx.getWindow();
			if (dlgExWin != null) {
				WindowManager.LayoutParams dlgExWinAttrs = dlgExWin.getAttributes();
				dlgExWinAttrs.gravity = Gravity.BOTTOM;
				dlgExWinAttrs.y = Math.round(bottomMargin * density);
				dlgExWin.setAttributes(dlgExWinAttrs);
			}
		}
	}
		
	public static void execHook_MoveVolume() {
		try {
			XposedHelpers.findAndHookMethod("com.htc.view.VolumePanel", null, "updatePanelRotationPosition", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					hookVolumePlanel(param);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_MoveVolume(LoadPackageParam lpparam) {
		try {
			String methodName = "updatePanelRotationPosition";
			if (Helpers.isSense7()) methodName = "createVolumePanel";
			XposedHelpers.findAndHookMethod("com.android.systemui.volume.VolumePanel", lpparam.classLoader, methodName, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					hookVolumePlanel(param);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static boolean updateScreenOn = false;
	private static boolean updateScreenOff = false;
	private static Object dpc = null;
	private static long length = 250L;
	
	private static void updateAnimDurations() {
		if (dpc == null) return;
		XMain.pref.reload();
		if (XMain.pref.getBoolean("pref_key_other_screenanim_duration_enable", false))
		length = (long)XMain.pref.getInt("pref_key_other_screenanim_duration", 250);
		
		if (XMain.pref_screenoff != 0) {
			ObjectAnimator mColorFadeOffAnimator = (ObjectAnimator)XposedHelpers.getObjectField(dpc, "mColorFadeOffAnimator");
			mColorFadeOffAnimator.setDuration(length);
		}
		
		if (XMain.pref_screenon != 0) {
			ObjectAnimator mColorFadeOnAnimator = (ObjectAnimator)XposedHelpers.getObjectField(dpc, "mColorFadeOnAnimator");
			mColorFadeOnAnimator.setDuration(Math.round(length * 0.8f));
		}
	}
	
	private static BroadcastReceiver mBRAnimDuration = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			try {
				updateAnimDurations();
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
	
	public static void execHook_ScreenColorFade(LoadPackageParam lpparam) {
		XposedHelpers.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.classLoader, "initialize", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				dpc = param.thisObject;
				updateAnimDurations();
				
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				IntentFilter intentfilter = new IntentFilter();
				intentfilter.addAction("com.sensetoolbox.six.mods.action.UpdateAnimDuration");
				mContext.registerReceiver(mBRAnimDuration, intentfilter);
			}
		});
		
		XposedHelpers.findAndHookMethod("com.android.server.display.DisplayPowerState", lpparam.classLoader, "prepareColorFade", Context.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				int mMode = (Integer)param.args[1];
				if (mMode >= 100)
					param.args[1] = mMode - 100;
				else if (XMain.pref_screenoff != 0)
					param.args[1] = XMain.pref_screenoff;
			}
		});
		
		if (XMain.pref_screenon != 0)
		XposedHelpers.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.classLoader, "animateScreenStateChange", int.class, boolean.class, new XC_MethodHook() {
			@Override
			@SuppressLint("InlinedApi")
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				int state = (Integer)param.args[0];
				Object mPowerState = XposedHelpers.getObjectField(param.thisObject, "mPowerState");
				ObjectAnimator mColorFadeOffAnimator = (ObjectAnimator)XposedHelpers.getObjectField(param.thisObject, "mColorFadeOffAnimator");
				ObjectAnimator mColorFadeOnAnimator = (ObjectAnimator)XposedHelpers.getObjectField(param.thisObject, "mColorFadeOnAnimator");
				
				if (state == Display.STATE_ON && !(Boolean)param.args[1]) try {
					param.setResult(null);
					
					if (mColorFadeOnAnimator.isStarted() || mColorFadeOffAnimator.isStarted()) return;
					
					boolean mPendingScreenOff = (Boolean)XposedHelpers.getObjectField(param.thisObject, "mPendingScreenOff");
					if (mPendingScreenOff) {
						XposedHelpers.callMethod(param.thisObject, "setScreenState", Display.STATE_OFF);
						XposedHelpers.setObjectField(param.thisObject, "mPendingScreenOff", false);
					}
					
					if (!(Boolean)XposedHelpers.callMethod(param.thisObject, "setScreenState", Display.STATE_ON)) return;
					
					Object mPowerRequest = XposedHelpers.getObjectField(param.thisObject, "mPowerRequest");
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					if ((Boolean)XposedHelpers.callMethod(mPowerRequest, "isBrightOrDim")) {
						if ((Float)XposedHelpers.callMethod(mPowerState, "getColorFadeLevel") == 1.0F)
							XposedHelpers.callMethod(mPowerState, "dismissColorFade");
						else if ((Boolean)XposedHelpers.callMethod(mPowerState, "prepareColorFade", mContext, XMain.pref_screenon + 100)) {
							mColorFadeOnAnimator.setStartDelay(0);
							mColorFadeOnAnimator.start();
						} else
							mColorFadeOnAnimator.end();
					} else {
						XposedHelpers.callMethod(mPowerState, "setColorFadeLevel", 1.0F);
						XposedHelpers.callMethod(mPowerState, "dismissColorFade");
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
		
		if (XMain.pref_screenoff == 2)
		findAndHookMethod("com.android.server.am.ActivityManagerService", lpparam.classLoader, "setLockScreenShown", boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if ((Boolean)param.args[0]) Thread.sleep(length + 100);
			}
		});
	}
	
	public static void execHook_ScreenColorFadeFix() {
		XposedHelpers.findAndHookMethod("android.view.SurfaceControl", null, "setPosition", float.class, float.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				long mNativeObject = (Long)XposedHelpers.getLongField(param.thisObject, "mNativeObject");
				if (mNativeObject == 0L) param.setResult(null);
			}
		});
		
		XposedHelpers.findAndHookMethod("android.view.SurfaceControl", null, "setMatrix", float.class, float.class, float.class, float.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				long mNativeObject = (Long)XposedHelpers.getLongField(param.thisObject, "mNativeObject");
				if (mNativeObject == 0L) param.setResult(null);
			}
		});
	}
	
	public static void execHook_ScreenAnim() {
		try {
			XResources.setSystemWideReplacement("android", "bool", "config_animateScreenLights", false);
			XposedHelpers.findAndHookMethod("com.android.server.power.DisplayPowerController", null, "setScreenOn", boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					if (updateScreenOff && XMain.pref_screenoff != 0) {
						param.setResult(null);
						updateScreenOff = false;
						
						ObjectAnimator mElectronBeamOffAnimator = (ObjectAnimator)XposedHelpers.getObjectField(param.thisObject, "mElectronBeamOffAnimator");
						Object mPowerState = XposedHelpers.getObjectField(param.thisObject, "mPowerState");
						
						if (!mElectronBeamOffAnimator.isStarted()) {
							float beamLvl = (Float)XposedHelpers.callMethod(mPowerState, "getElectronBeamLevel");
							if (beamLvl == 0.0F) {
								XposedHelpers.callMethod(param.thisObject, "setScreenOn", false);
							} else {
								if ((Boolean)XposedHelpers.callMethod(mPowerState, "prepareElectronBeam", XMain.pref_screenoff) && (Boolean)XposedHelpers.callMethod(mPowerState, "isScreenOn"))
									mElectronBeamOffAnimator.start();
								else
									mElectronBeamOffAnimator.end();
							}
						}
					}
				}
				
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					if ((Boolean)param.args[0] && XMain.pref_screenon != 0) {
						updateScreenOn = true;
						
						Object mPowerState = XposedHelpers.getObjectField(param.thisObject, "mPowerState");
						ObjectAnimator mElectronBeamOnAnimator = (ObjectAnimator)XposedHelpers.getObjectField(param.thisObject, "mElectronBeamOnAnimator");
					
						if (!mElectronBeamOnAnimator.isStarted()) {
							float beamLvl = (Float)XposedHelpers.callMethod(mPowerState, "getElectronBeamLevel");
							if (beamLvl == 1.0F) {
								XposedHelpers.callMethod(mPowerState, "dismissElectronBeam");
							} else {
								if ((Boolean)XposedHelpers.callMethod(mPowerState, "prepareElectronBeam", XMain.pref_screenon) && (Boolean)XposedHelpers.callMethod(mPowerState, "isScreenOn"))
									mElectronBeamOnAnimator.start();
								else
									mElectronBeamOnAnimator.end();
							}
						}
					}
				}
			});
			
			if (XMain.pref_screenoff != 0)
			XposedHelpers.findAndHookMethod("com.android.server.power.DisplayPowerController", null, "animateScreenBrightness", int.class, int.class, int.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					if ((Integer)param.args[0] == 0 && (Integer)param.args[1] == 0 && (Integer)param.args[2] == 0 && (Integer)param.args[3] == -1) {
						updateScreenOff = true;
						param.setResult(null);
					}
				}
			});
			
			if (XMain.pref_screenon != 0) {
				XposedHelpers.findAndHookMethod("com.android.server.power.DisplayPowerState", null, "dismissElectronBeam", new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
						if (updateScreenOn) param.setResult(null);
					}
				});
				
				XposedHelpers.findAndHookMethod("com.android.server.power.DisplayPowerController", null, "updatePowerState", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
						updateScreenOn = false;
					}
				});
			}
			
			XposedHelpers.findAndHookMethod("com.android.server.power.DisplayPowerState", null, "setElectronBeamLevel", float.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					if (updateScreenOn || updateScreenOff) param.setResult(null);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_fastUnlock(final LoadPackageParam lpparam) {
		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				final Object mLockPatternUtils = getObjectField(param.thisObject, "mLockPatternUtils");
				final TextView mPasswordEntry = (TextView)getObjectField(param.thisObject, "mPasswordEntry");
				if (mLockPatternUtils != null && mPasswordEntry != null)
				mPasswordEntry.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {}
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
					
					@Override
					public void afterTextChanged(Editable s) {
						if (mPasswordEntry != null) try {
							if (mPasswordEntry.getText().length() > 3) {
								Boolean isPinCorrect = (Boolean)callMethod(mLockPatternUtils, "checkPassword", mPasswordEntry.getText().toString());
								if (isPinCorrect) XposedHelpers.callMethod(param.thisObject, "verifyPasswordAndUnlock");
							}
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				});
			}
		};
		
		if (Helpers.isLP()) {
			findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardPINView", lpparam.classLoader, "onFinishInflate", hook);
			findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardPasswordView", lpparam.classLoader, "onFinishInflate", hook);
		} else {
			findAndHookMethod("com.htc.lockscreen.unlockscreen.HtcKeyInputUnlockView", lpparam.classLoader, "initView", hook);
		}
	}
	
	public static void execHook_EnhancedInstaller(final LoadPackageParam lpparam) {
		/*
		findAndHookMethod("com.android.packageinstaller.InstallAppProgress", lpparam.classLoader, "initView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					ApplicationInfo mAppInfo = (ApplicationInfo)XposedHelpers.getObjectField(param.thisObject, "mAppInfo");
					Activity install = (Activity)param.thisObject;
					if (mAppInfo != null && install != null) {
						PackageInfo mPkgInfo = install.getPackageManager().getPackageInfo(((PackageItemInfo)mAppInfo).packageName, 8192);
						TextView appName = (TextView)install.findViewById(install.getResources().getIdentifier("app_name", "id", "com.android.packageinstaller"));
						if (appName != null && mPkgInfo.versionName != null) appName.setText(appName.getText() + " " + mPkgInfo.versionName);
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
		*/
		findAndHookMethod("com.android.packageinstaller.PackageInstallerActivity", lpparam.classLoader, "startInstallConfirm", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					Activity pia = (Activity)param.thisObject;
					PackageInfo mPkgInfo = (PackageInfo)XposedHelpers.getObjectField(param.thisObject, "mPkgInfo");
					if (mPkgInfo != null) {
						TextView appName = (TextView)pia.findViewById(pia.getResources().getIdentifier("app_name", "id", "com.android.packageinstaller"));
						if (appName != null && mPkgInfo.versionName != null) appName.setText(appName.getText() + " " + mPkgInfo.versionName);
						
						// Add new tab with package info when updating an app. Not really in place there. Kept for future mods.
						/*
						View pager = pia.findViewById(pia.getResources().getIdentifier("pager", "id", "com.android.packageinstaller"));
						Object tabsAdapter = XposedHelpers.callMethod(pager, "getAdapter");
						
						TabHost tabhost = (TabHost)pia.findViewById(android.R.id.tabhost);
						//final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
						TabSpec tabspec = tabhost.newTabSpec("info").setIndicator("Info"); //modRes.getString(R.string.installer_info)
					
						float density = pia.getResources().getDisplayMetrics().density;
						LinearLayout newTab = new LinearLayout(pia);
						newTab.setOrientation(LinearLayout.VERTICAL);
						
						LinearLayout container = new LinearLayout(pia);
						container.setOrientation(LinearLayout.VERTICAL);
						LinearLayout.LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
						lp.setMargins(Math.round(15.0f * density), Math.round(10.0f * density), Math.round(15.0f * density), Math.round(10.0f * density));
						container.setLayoutParams(lp);
						
						TextView txt = new TextView(container.getContext());
						txt.setText("PACKAGE");
						txt.setTypeface(null, Typeface.BOLD);
						txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
						txt.setPadding(Math.round(10.0f * density), 0, Math.round(10.0f * density), Math.round(4.0f * density));
						
						TextView txt_divider = new TextView(container.getContext());
						txt_divider.setBackgroundColor(Color.argb(255, 70, 70, 70));
						txt_divider.setHeight(Math.round(2 * density));
						
						container.addView(txt);
						container.addView(txt_divider);
					
						TextView info1 = new TextView(container.getContext());
						info1.setText("Name: " + mPkgInfo.applicationInfo.packageName);
						info1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
						info1.setPadding(Math.round(10.0f * density), Math.round(10.0f * density), Math.round(10.0f * density), Math.round(4.0f * density));
						TextView info2 = new TextView(container.getContext());
						info2.setText("Version: " + mPkgInfo.versionName);
						info2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
						info2.setPadding(Math.round(10.0f * density), 0, Math.round(10.0f * density), Math.round(4.0f * density));
						TextView info3 = new TextView(container.getContext());
						info3.setText("Target SDK version: " + String.valueOf(mPkgInfo.applicationInfo.targetSdkVersion));
						info3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
						info3.setPadding(Math.round(10.0f * density), 0, Math.round(10.0f * density), Math.round(4.0f * density));
						
						container.addView(info1);
						container.addView(info2);
						container.addView(info3);
						
						newTab.addView(container);
						XposedHelpers.callMethod(tabsAdapter, "addTab", tabspec, newTab);
						*/
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
	
	public static void execHook_YouTubeNoWatermark(final InitPackageResourcesParam resparam) {
		resparam.res.hookLayout("com.google.android.youtube", "layout", "annotation_overlay", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				try {
					ImageView watermark = (ImageView)liparam.view.findViewById(resparam.res.getIdentifier("branding_watermark", "id", "com.google.android.youtube"));
					if (watermark != null) watermark.setAlpha(0f);
					
					ImageView watermark2 = (ImageView)liparam.view.findViewById(resparam.res.getIdentifier("featured_channel_watermark", "id", "com.google.android.youtube"));
					if (watermark2 != null) watermark2.setAlpha(0f);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
	
	public static void execHook_SafeVolume() {
		findAndHookMethod("android.media.AudioService", null, "checkSafeMediaVolume", int.class, int.class, int.class, XC_MethodReplacement.returnConstant(true));
	}
	
	private static void changeToast(View toastView) {
		try {
			XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
			//TextView toast = (TextView)toastView.findViewById(android.R.id.message);
			//toast.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
			//toast.setTypeface(Typeface.DEFAULT);
			LinearLayout toastLayout = (LinearLayout)toastView;
			toastLayout.setBackground(modRes.getDrawable(R.drawable.toast_frame));
		} catch (Throwable t) {
			//XposedBridge.log(t);
		}
	}
	
	public static void exec_OldStyleToasts() {
		try {
			XResources.hookSystemWideLayout("android", "layout", "transient_notification", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					changeToast(liparam.view);
				}
			});
			
			findAndHookMethod("android.widget.Toast", null, "makeText", Context.class, CharSequence.class, int.class, new XC_MethodHook() {
				@Override
				public void afterHookedMethod(final MethodHookParam param) throws Throwable {
					Toast tst = (Toast)param.getResult();
					if (tst != null) changeToast(tst.getView());
				}
			});
		} catch (Throwable t) {
			//XposedBridge.log(t);
		}
	}
	
	public static void execHook_LargePhoto(final InitPackageResourcesParam resparam, int photoSize) {
		final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, resparam.res);
		try {
			int resId = R.dimen.people_info_top_margin;
			if (photoSize == 2) resId = R.dimen.people_info_top_margin_rect;
			resparam.res.setReplacement("com.android.phone", "dimen", "photo_frame_height", modRes.fwd(resId));
			resparam.res.setReplacement("com.android.phone", "dimen", "lockscreen_10", modRes.fwd(R.dimen.lockscreen_10));
			if (Helpers.isSense7())
			resparam.res.setReplacement("com.android.phone", "dimen", "lockscreen_11", modRes.fwd(R.dimen.lockscreen_11));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		
		if (Helpers.isDualSIM()) try {
			resparam.res.setReplacement("com.android.phone", "dimen", "text_size_incoming_call_slot_name", modRes.fwd(R.dimen.text_size_incoming_call_slot_name));
			resparam.res.setReplacement("com.android.phone", "dimen", "dualsim_incoming_call_slot_name_height", modRes.fwd(R.dimen.dualsim_incoming_call_slot_name_height));
			resparam.res.setReplacement("com.android.phone", "dimen", "incoming_call_slot_name_title_layout_height", modRes.fwd(R.dimen.incoming_call_slot_name_title_layout_height));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static void setPhotoHeight(ImageView mPhoto, int photoSize){
		try {
			final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
			ViewParent mPhotoParent = mPhoto.getParent();
			int photoHeight;
			
			KeyguardManager km = (KeyguardManager)mPhoto.getContext().getSystemService(Context.KEYGUARD_SERVICE);
			float density = mPhoto.getContext().getResources().getDisplayMetrics().density;
			if (photoSize == 2) photoHeight = modRes.getDimensionPixelSize(R.dimen.photo_new_height_rect); else
			if (km.inKeyguardRestrictedInputMode()) {
				photoHeight = modRes.getDimensionPixelSize(R.dimen.photo_new_height_ls);
				if ((Helpers.isEight() || Helpers.isDesire816()) && XMain.pref.getBoolean("pref_key_controls_smallsoftkeys", false)) photoHeight += Math.round(density * 18);
			} else {
				photoHeight = modRes.getDimensionPixelSize(R.dimen.photo_new_height);
				if (Helpers.isEight() || Helpers.isDesire816())
				if (XMain.pref.getBoolean("pref_key_controls_smallsoftkeys", false))
					photoHeight -= Math.round(density * 19.333);
				else
					photoHeight -= Math.round(density * 37.333);
			}
		
			if (mPhotoParent != null)
			if (mPhotoParent instanceof RelativeLayout) {
				RelativeLayout mPhotoFrame = (RelativeLayout)mPhotoParent;
				if (mPhotoFrame.getParent() instanceof LinearLayout) {
					LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams)mPhotoFrame.getLayoutParams();
					params1.height = photoHeight;
					mPhotoFrame.setLayoutParams(params1);
				} else if (mPhotoFrame.getParent() instanceof RelativeLayout) {
					RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)mPhotoFrame.getLayoutParams();
					params1.height = photoHeight;
					mPhotoFrame.setLayoutParams(params1);
				}
			} else if (mPhotoParent instanceof FrameLayout) {
				FrameLayout mPhotoFrame = (FrameLayout)mPhotoParent;
				FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams)mPhotoFrame.getLayoutParams();
				params1.height = photoHeight;
				mPhotoFrame.setLayoutParams(params1);
			} else if (mPhotoParent instanceof LinearLayout) {
				LinearLayout mPhotoFrame = (LinearLayout)mPhotoParent;
				LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams)mPhotoFrame.getLayoutParams();
				params1.height = photoHeight;
				mPhotoFrame.setLayoutParams(params1);
			}
				
			if (mPhoto != null) {
				if (mPhoto.getParent() instanceof LinearLayout) {
					LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams)mPhoto.getLayoutParams();
					params2.height = photoHeight;
					mPhoto.setLayoutParams(params2);
				} else if (mPhoto.getParent() instanceof RelativeLayout) {
					RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)mPhoto.getLayoutParams();
					params2.height = photoHeight;
					mPhoto.setLayoutParams(params2);
				}
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_LargePhotoCode(LoadPackageParam lpparam, final int photoSize) {
		findAndHookMethod("com.android.phone.widget.PhotoImageView", lpparam.classLoader, "setImageDrawable", Drawable.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				setPhotoHeight((ImageView)param.thisObject, photoSize);
			}
		});
		
		findAndHookMethod("com.android.phone.CallCard", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				TextView mName = (TextView)XposedHelpers.getObjectField(param.thisObject, "mName");
				if (mName != null) {
					mName.setSingleLine(false);
					mName.setMaxLines(2);
					mName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 27);
					mName.setPadding(mName.getPaddingLeft(), Math.round(mName.getResources().getDisplayMetrics().density * 5), mName.getPaddingRight(), mName.getPaddingBottom());
				}
			}
		});
		
		findAndHookMethod("com.android.phone.InCallScreen", lpparam.classLoader, "initInCallScreen", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				ViewGroup mInCallPanel = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mInCallPanel");
				if (mInCallPanel != null) {
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mInCallPanel.getLayoutParams();
					params.removeRule(RelativeLayout.BELOW);
					mInCallPanel.setLayoutParams(params);
				}
				
				View mContent = (View)XposedHelpers.getObjectField(param.thisObject, "mContent");
				if (mContent != null) mContent.setFitsSystemWindows(false);
			}
		});
		
		findAndHookMethod("com.android.phone.InCallScreen", lpparam.classLoader, "initActionBar", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					LinearLayout mActionBar = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mActionBar");
					if (mActionBar != null) {
						RelativeLayout newLayout = new RelativeLayout(mActionBar.getContext());
						ViewParent prnt = mActionBar.getParent();
						if (prnt != null && prnt instanceof RelativeLayout) {
							((RelativeLayout)prnt).removeView(mActionBar);
							newLayout.addView(mActionBar);
							((RelativeLayout)prnt).addView(newLayout);
							newLayout.bringToFront();
							
							int resourceId = mActionBar.getResources().getIdentifier("status_bar_height", "dimen", "android");
							if (resourceId > 0)
							newLayout.setPadding(newLayout.getPaddingLeft(), mActionBar.getResources().getDimensionPixelSize(resourceId), newLayout.getPaddingRight(), newLayout.getPaddingBottom());
						}
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
		
		if (Helpers.isDualSIM())
		findAndHookMethod("com.android.phone.InCallScreen", lpparam.classLoader, "createReminderView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					ViewGroup mReminder = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mReminder");
					if (mReminder != null) {
						ViewGroup photo_view_root = (ViewGroup)mReminder.findViewById(mReminder.getResources().getIdentifier("photo_view_root", "id", "com.android.phone"));
						if (photo_view_root != null) {
							int paddingTopRoot = Math.round(photo_view_root.getResources().getDisplayMetrics().density * 25);
							if (Helpers.isLP()) paddingTopRoot = 0;
							photo_view_root.setPadding(
								photo_view_root.getPaddingLeft(),
								paddingTopRoot,
								photo_view_root.getPaddingRight(),
								photo_view_root.getPaddingBottom()
							);
						}
						
						TextView slot_name = (TextView)mReminder.findViewById(mReminder.getResources().getIdentifier("slot_name", "id", "com.android.phone"));
						if (slot_name != null) {
							slot_name.setPadding(slot_name.getPaddingLeft(), 0, slot_name.getPaddingRight(), 0);
							slot_name.setIncludeFontPadding(false);
						}
						
						if (Helpers.isLP() && photo_view_root != null && slot_name != null) {
							int vis = slot_name.getVisibility();
							((ViewGroup)slot_name.getParent()).removeView(slot_name);
							photo_view_root.addView(slot_name, 0);
							RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)slot_name.getLayoutParams();
							lp.topMargin = Math.round(slot_name.getResources().getDisplayMetrics().density * 25);
							slot_name.setShadowLayer(4.0f, 0, 3.0f, Color.argb(153, 0, 0, 0));
							slot_name.setVisibility(vis);
							slot_name.bringToFront();
							slot_name.requestLayout();
							slot_name.invalidate();
						}
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
		
		findAndHookMethod("com.android.phone.widget.PhoneActionBar", lpparam.classLoader, "applyStyleToViews", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					LinearLayout actionBar = (LinearLayout)param.thisObject;
					if (actionBar != null) {
						actionBar.setBackground(null);
						actionBar.setBackgroundResource(0);
						//actionBar.setBackgroundColor(Color.argb(200, 22, 22, 22));
						TextView mPrimaryText = (TextView)XposedHelpers.getObjectField(actionBar, "mPrimaryText");
						TextView mSecondaryText = (TextView)XposedHelpers.getObjectField(actionBar, "mSecondaryText");
						mPrimaryText.setShadowLayer(4.0f, 0, 3.0f, Color.argb(153, 0, 0, 0));
						mSecondaryText.setShadowLayer(4.0f, 0, 3.0f, Color.argb(153, 0, 0, 0));
						mSecondaryText.setTextColor(Color.argb(255, 255, 255, 255));
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
		
		findAndHookMethod("com.android.phone.CallCard", lpparam.classLoader, "onOrientationChanged", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					RelativeLayout callCard = (RelativeLayout)param.thisObject;
					LinearLayout peopleInfoLayout = (LinearLayout)callCard.findViewById(callCard.getResources().getIdentifier("peopleInfoLayout", "id", "com.android.phone"));
					
					if (photoSize == 3) {
						peopleInfoLayout.setBackgroundColor(Color.argb(140, 22, 22, 22));
						peopleInfoLayout.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
						peopleInfoLayout.setPadding(peopleInfoLayout.getPaddingLeft(), Math.round(peopleInfoLayout.getResources().getDisplayMetrics().density * 8), peopleInfoLayout.getPaddingRight(), Math.round(peopleInfoLayout.getResources().getDisplayMetrics().density * 10));
						LinearLayout.LayoutParams paramsPI = (LinearLayout.LayoutParams)peopleInfoLayout.getLayoutParams();
						paramsPI.setMargins(paramsPI.leftMargin, 0, paramsPI.rightMargin, Math.round(peopleInfoLayout.getResources().getDisplayMetrics().density * 143));
						paramsPI.height = LayoutParams.WRAP_CONTENT;
						((LinearLayout)peopleInfoLayout.getParent()).setGravity(Gravity.BOTTOM);
						peopleInfoLayout.setLayoutParams(paramsPI);
					} else {
						peopleInfoLayout.setPadding(peopleInfoLayout.getPaddingLeft(), peopleInfoLayout.getPaddingTop(), peopleInfoLayout.getPaddingRight(), Math.round(peopleInfoLayout.getResources().getDisplayMetrics().density * 45));
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
		
		if (Helpers.isSense7())
		findAndHookMethod("com.htc.lib1.cc.widget.reminder.drag.WorkspaceView", lpparam.classLoader, "setMastheadOnTop", ViewGroup.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				ViewGroup mMasthead = (ViewGroup)param.args[0];
				if (mMasthead != null) mMasthead.setVisibility(View.INVISIBLE);
			}
		});
	}
	
	public static void execHook_RejectCallSilently(final LoadPackageParam lpparam) {
		if (Helpers.isLP()) {
			findAndHookMethod("com.android.phone.CallNotifier", lpparam.classLoader, "addCallLog", "com.android.internal.telephony.Connection", int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					if ((Integer)param.args[1] == 16) param.args[1] = 2;
				}
			});
		} else {
			findAndHookMethod("com.android.phone.CallNotifier", lpparam.classLoader, "addCallLog", "com.android.internal.telephony.Connection", "com.android.internal.telephony.Connection.DisconnectCause", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					try {
						Enum<?> disconnectcause = (Enum<?>)param.args[1];
						if (disconnectcause.ordinal() == 16)
						param.args[1] = XposedHelpers.getStaticObjectField(findClass("com.android.internal.telephony.Connection.DisconnectCause", null), "NORMAL");
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		}
		
		findAndHookMethod("com.android.phone.InCallScreen", lpparam.classLoader, "startDeclineCallReminder", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				Object nMgr = XposedHelpers.callStaticMethod(findClass("com.android.phone.NotificationMgr", lpparam.classLoader), "getDefault");
				int missedNum = (Integer)XposedHelpers.callMethod(nMgr, "getNumberMissedCalls");
				if (missedNum == 0) param.setResult(null);
			}
		});
	}
	
	public static void execHook_EnhancedSecurity() {
		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
					if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) {
						Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
						if (mHandler != null) {
							Runnable mPowerLongPress = (Runnable)XposedHelpers.getObjectField(param.thisObject, "mPowerLongPress");
							Runnable mPowerLongPress_Toast = (Runnable)XposedHelpers.getObjectField(param.thisObject, "mPowerLongPress_Toast");
							Runnable mPowerLongPress_Toast_2KeyHWResetHint = (Runnable)XposedHelpers.getObjectField(param.thisObject, "mPowerLongPress_Toast_2KeyHWResetHint");
							Runnable mPowerLongPress_Toast_2KeyHWResetIndicator = (Runnable)XposedHelpers.getObjectField(param.thisObject, "mPowerLongPress_Toast_2KeyHWResetIndicator");
							if (mPowerLongPress != null) mHandler.removeCallbacks(mPowerLongPress);
							if (mPowerLongPress_Toast != null) mHandler.removeCallbacks(mPowerLongPress_Toast);
							if (mPowerLongPress_Toast_2KeyHWResetHint != null) mHandler.removeCallbacks(mPowerLongPress_Toast_2KeyHWResetHint);
							if (mPowerLongPress_Toast_2KeyHWResetIndicator != null) mHandler.removeCallbacks(mPowerLongPress_Toast_2KeyHWResetIndicator);
						}
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		};
		
		try {
			findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "interceptPowerKeyDown", boolean.class, boolean.class, hook);
		} catch (Throwable t) {
			try {
				findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "interceptPowerKeyDown", boolean.class, hook);
			} catch (Throwable t2) {
				XposedBridge.log(t2);
			}
		}
	}
	
	public static void execHook_AllRotations() {
		try {
			XResources.setSystemWideReplacement("android", "bool", "config_allowAllRotations", true);
			XResources.setSystemWideReplacement("com.htc.framework", "bool", "config_allowAllRotations", true);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static MethodHookParam mNMSParam = null;
	public static boolean isInFullscreen = false;
	private static BroadcastReceiver mBR = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			try {
				String action = intent.getAction();
				if (action != null)
				if (mNMSParam != null && action.equals("com.sensetoolbox.six.CLEARNOTIFICATION")) {
					XposedHelpers.callMethod(mNMSParam.thisObject, "cancelNotificationWithTag", intent.getStringExtra("pkgName"), intent.getStringExtra("tag"), intent.getIntExtra("id", 0), intent.getIntExtra("userId", 0));
				} else if (action.equals("com.sensetoolbox.six.PREFSUPDATED")) {
					sendNotificationData(mNMSParam, true, true);
				} else if (action.equals("com.sensetoolbox.six.CHANGEFULLSCREEN")) {
					isInFullscreen = intent.getBooleanExtra("isInFullscreen", false);
				} else if (action.equals("com.sensetoolbox.six.SENDCONTENTINTENT")) {
					PendingIntent mIntent = intent.getParcelableExtra("contentIntent");
					Object amn = XposedHelpers.callStaticMethod(findClass("android.app.ActivityManagerNative", null), "getDefault");
					XposedHelpers.callMethod(amn, "resumeAppSwitches");
					XposedHelpers.callMethod(amn, "dismissKeyguardOnNextActivity");
					mIntent.send(0);
				}
			} catch (Throwable t) {
				if (mNMSParam != null)
					sendNotificationData(mNMSParam, true, true);
				else
					XposedBridge.log(t);
			}
		}
	};
	
	private static Bitmap getScreenshot(Context ctx) {
		Matrix matrix = new Matrix();
		float[] af = new float[2];
		af[0] = ctx.getResources().getDisplayMetrics().widthPixels;
		af[1] = ctx.getResources().getDisplayMetrics().heightPixels;
		Display display = ((WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		float rotAngle = 0f;
		switch (display.getRotation()) {
			case Surface.ROTATION_270: rotAngle = 270f; break;
			case Surface.ROTATION_180: rotAngle = 180f; break;
			case Surface.ROTATION_90: rotAngle = 90f; break;
			default: rotAngle = 0f;
		}
		if (rotAngle > 0f) {
			matrix.reset();
			matrix.preRotate(-rotAngle);
			matrix.mapPoints(af);
			af[0] = Math.abs(af[0]);
			af[1] = Math.abs(af[1]);
		}
		Bitmap bmp = (Bitmap)XposedHelpers.callStaticMethod(findClass("android.view.SurfaceControl", null), "screenshot", (int)af[0], (int)af[1]);
		if (bmp != null) {
			if (rotAngle > 0f) {
				Bitmap bmp1 = Bitmap.createBitmap(ctx.getResources().getDisplayMetrics().widthPixels, ctx.getResources().getDisplayMetrics().heightPixels, android.graphics.Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(bmp1);
				canvas.translate(bmp1.getWidth() / 2, bmp1.getHeight() / 2);
				canvas.rotate(360f - rotAngle);
				canvas.translate(-af[0] / 2f, -af[1] / 2f);
				canvas.drawBitmap(bmp, 0f, 0f, null);
				canvas.setBitmap(null);
				bmp = bmp1;
			}
			Intent intentScr = new Intent("com.sensetoolbox.six.NEWSCREENSHOT");
			intentScr.putExtra("bmp", bmp);
			ctx.sendBroadcast(intentScr);
		}
		return bmp;
	};
	
	private static boolean isAllowed(String pkgName) {
		HashSet<String> appsList = (HashSet<String>)XMain.pref.getStringSet("pref_key_other_popupnotify_bwlist_apps", new HashSet<String>());
		boolean isInList = appsList.contains(pkgName);
		boolean isWhitelist = XMain.pref.getBoolean("pref_key_other_popupnotify_bwlist", false);
		return ((isWhitelist && isInList) || (!isWhitelist && !isInList));
	}
	
	private static ArrayList<StatusBarNotification> makeSbnsArray(Object nmsObj) {
		boolean lowPriority = XMain.pref.getBoolean("pref_key_other_popupnotify_priority", false);
		@SuppressWarnings("unchecked")
		ArrayList<Object> notifications = (ArrayList<Object>)XposedHelpers.getObjectField(nmsObj, "mNotificationList");
		ArrayList<StatusBarNotification> sbns = new ArrayList<StatusBarNotification>();
		if (notifications != null)
		for (int l = 0; l < notifications.size(); l++) {
			StatusBarNotification sbnrec = (StatusBarNotification)XposedHelpers.getObjectField(notifications.get(l), "sbn");
			if (sbnrec != null && sbnrec.isClearable() && !sbnrec.isOngoing())
			if (sbnrec.getNotification().priority >= 0 || (sbnrec.getNotification().priority < 0 && lowPriority))
			if (isAllowed(sbnrec.getPackageName()))
			sbns.add(sbnrec.clone());
		}
		return sbns;
	}
	
	@SuppressWarnings("deprecation")
	private static void sendSbnsArray(ArrayList<StatusBarNotification> sbns, final Context mContext, boolean asBroadcast) {
		if (asBroadcast) {
			final Intent intent = new Intent("com.sensetoolbox.six.UPDATENOTIFICATIONS");
			intent.putParcelableArrayListExtra("sbns", sbns);
			if (Settings.System.getInt(mContext.getContentResolver(), "popup_notifications_visible", 0) == 1) mContext.sendBroadcast(intent); else
			(new Handler()).postDelayed(new Runnable() {
				public void run() {
					mContext.sendBroadcast(intent);
				}
			}, 300L);
		} else try {
			boolean lightUpScreen = XMain.pref.getBoolean("pref_key_other_popupnotify_lightup", false);
			boolean sleepMode = XMain.pref.getBoolean("pref_key_other_popupnotify_sleepmode", false);

			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			
			PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
			if (pm.isScreenOn() && Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() > 16 * 1024 * 1024) {
				Bitmap bmp = getScreenshot(mContext);
				if (bmp != null) intent.putExtra("bmp", bmp);
			}
			
			boolean isFromPhone = false;
			for (StatusBarNotification sbn: sbns)
			if (sbn != null && sbn.getNotification() != null && sbn.getPackageName().equals("com.android.phone")) {
				isFromPhone = true;
				if (sbn.getNotification().icon == mContext.getResources().getIdentifier("stat_sys_warning", "drawable", "android")) {
					XposedBridge.log("Ignoring notification: " + sbn.getNotification().toString());
					return;
				}
				break;
			}
			
			if (pm.isScreenOn() && sleepMode && !isFromPhone) return;
			if (lightUpScreen) {
				WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "ST PN Light up");
				wl.acquire(1000);
				XposedHelpers.callMethod(pm, "wakeUp", SystemClock.uptimeMillis());
			}
			
			KeyguardManager kgMgr = (KeyguardManager)mContext.getSystemService(Context.KEYGUARD_SERVICE);
			if (kgMgr.isKeyguardLocked())
				intent.setClassName("com.sensetoolbox.six", "com.sensetoolbox.six.DimmedActivityLS");
			else
				intent.setClassName("com.sensetoolbox.six", "com.sensetoolbox.six.DimmedActivity");
			intent.putParcelableArrayListExtra("sbns", sbns);
			
			Bundle animate = ActivityOptions.makeCustomAnimation(mContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
			mContext.startActivity(intent, animate);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static void sendNotificationData(final MethodHookParam param, final boolean isRemove, boolean isForced) {
		try {
			XMain.pref.reload();
			if (!XMain.pref.getBoolean("popup_notify_active", false)) return;
			if (isInFullscreen && XMain.pref.getBoolean("pref_key_other_popupnotify_fullscreen", false)) return;
			
			final Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
			Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
			Object notificationRecord = param.args[0];
			
			if (mContext != null && mHandler != null) {
				TelephonyManager phone = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
				if (phone.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
					boolean isSuitable = false;
					if (notificationRecord != null && !isForced) {
						StatusBarNotification sbn = ((StatusBarNotification)XposedHelpers.getObjectField(notificationRecord, "sbn")).clone();
						if (sbn.isClearable() && !sbn.isOngoing() && isAllowed(sbn.getPackageName())) isSuitable = true;
					}
					
					if (isSuitable || isForced)
					mHandler.post(new Runnable() {
						public void run() {
							ArrayList<StatusBarNotification> sbns = makeSbnsArray(param.thisObject);
							if (isRemove)
								sendSbnsArray(sbns, mContext, true);
							else
								sendSbnsArray(sbns, mContext, false);
						}
					});
				}
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_PopupNotify() {
		findAndHookMethod("com.htc.fragment.widget.CarouselFragment", null, "hideCarousel", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				try {
					CarouselFragment mCarousel = (CarouselFragment)param.thisObject;
					if (mCarousel != null && mCarousel.getActivity().getPackageName().equals("com.sensetoolbox.six")) param.setResult(null);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
		
		findAndHookMethod("com.android.server.NotificationManagerService", null, "notifyPostedLocked", "com.android.server.NotificationManagerService.NotificationRecord", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				sendNotificationData(param, false, false);
			}
		});
		
		findAndHookMethod("com.android.server.NotificationManagerService", null, "notifyRemovedLocked", "com.android.server.NotificationManagerService.NotificationRecord", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				sendNotificationData(param, true, false);
			}
		});
		
		XposedBridge.hookAllConstructors(findClass("com.android.server.NotificationManagerService", null), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context ctx = (Context)param.args[0];
				IntentFilter intentfilter = new IntentFilter();
				intentfilter.addAction("com.sensetoolbox.six.CLEARNOTIFICATION");
				intentfilter.addAction("com.sensetoolbox.six.CHANGEFULLSCREEN");
				intentfilter.addAction("com.sensetoolbox.six.PREFSUPDATED");
				intentfilter.addAction("com.sensetoolbox.six.SENDCONTENTINTENT");
				ctx.registerReceiver(mBR, intentfilter);
				mNMSParam = param;
			}
		});
		
		findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				if (act == null) return;
				int flags = act.getWindow().getAttributes().flags;
				Intent fullscreenIntent = new Intent("com.sensetoolbox.six.CHANGEFULLSCREEN");
				if (flags != 0 && (flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN && !act.getPackageName().equals("com.android.systemui"))
					fullscreenIntent.putExtra("isInFullscreen", true);
				else
					fullscreenIntent.putExtra("isInFullscreen", false);
				act.sendBroadcast(fullscreenIntent);
			}
		});
	}
	
	private static int notificationsCount = 0;
	private static void processNotificationData(final MethodHookParam param) {
		try {
			final Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
			Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
			notificationsCount = 0;
			
			if (mContext != null && mHandler != null)
			mHandler.post(new Runnable() {
				public void run() {
					@SuppressWarnings("unchecked")
					ArrayList<Object> notifications = (ArrayList<Object>)XposedHelpers.getObjectField(param.thisObject, "mNotificationList");
					if (notifications != null)
					for (int l = 0; l < notifications.size(); l++) {
						StatusBarNotification sbnrec = (StatusBarNotification)XposedHelpers.getObjectField(notifications.get(l), "sbn");
						if (sbnrec != null && sbnrec.isClearable() && !sbnrec.isOngoing()) notificationsCount++;
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static SensorManager mSensorManager;
	private static Sensor accSensor;
	private static Sensor magnetSensor;
	private static float[] rotationMatrix = new float[9];
	private static class TiltListener implements SensorEventListener {
		Context ctx;
		float[] gravity;
		float[] magnetic;
		float[] orientation = new float[3];
		double pitch;
		
		int position = 0;
		
		TiltListener(Context context) {
			ctx = context;
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_GRAVITY) gravity = event.values.clone();
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) magnetic = event.values.clone();

			if (gravity != null && magnetic != null && SensorManager.getRotationMatrix(rotationMatrix, null, gravity, magnetic)) {
				SensorManager.getOrientation(rotationMatrix, orientation);
				pitch = Math.toDegrees(orientation[1]);
				int newPos = -1;
				if (Math.abs(pitch) < 20f) newPos = 0;
				else if (Math.abs(pitch) > 35f) newPos = 1;
				
				if (newPos != -1 && newPos != position) {
					position = newPos;
					if (newPos == 1 && notificationsCount > 0 && ctx != null) {
						Vibrator vibe = (Vibrator)ctx.getSystemService(Context.VIBRATOR_SERVICE);
						vibe.vibrate(30);
					}
				}
			}
		}
	}
	
	public static void execHook_HapticNotify() {
		XposedBridge.hookAllConstructors(findClass("com.android.server.NotificationManagerService", null), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context ctx = (Context)param.args[0];
				mSensorManager = (SensorManager)ctx.getSystemService(Context.SENSOR_SERVICE);
				accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
				magnetSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
				
				TiltListener tiltListener = new TiltListener(ctx);
				mSensorManager.registerListener(tiltListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
				mSensorManager.registerListener(tiltListener, magnetSensor, SensorManager.SENSOR_DELAY_NORMAL);
			}
		});
		
		findAndHookMethod("com.android.server.NotificationManagerService", null, "notifyPostedLocked", "com.android.server.NotificationManagerService.NotificationRecord", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				processNotificationData(param);
			}
		});
		
		findAndHookMethod("com.android.server.NotificationManagerService", null, "notifyRemovedLocked", "com.android.server.NotificationManagerService.NotificationRecord", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				processNotificationData(param);
			}
		});
	}
	
	public static void execHook_NoChargerWarning(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.android.settings.NSReceiver", lpparam.classLoader, "showVZWChargerNotification", Context.class, int.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					int type = (Integer)param.args[1];
					if (type == 1) param.setResult(null);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_LEDNotifyTimeout(LoadPackageParam lpparam) {
		XposedBridge.hookAllConstructors(findClass("com.android.server.NotificationManagerService", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				XposedHelpers.setIntField(param.thisObject, "mFlashTimeout", XMain.pref.getInt("pref_key_other_ledtimeout_value", 5));
			}
		});
	}
	
	public static void execHook_LEDOnCharge(LoadPackageParam lpparam) {
		if (Helpers.isLP()) {
			findAndHookMethod("com.android.server.notification.NotificationManagerService", lpparam.classLoader, "onStart", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.setBooleanField(param.thisObject, "mFlashNotifLightDuringCharging", true);
				}
			});
		} else {
			XposedBridge.hookAllConstructors(findClass("com.android.server.NotificationManagerService", lpparam.classLoader), new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.setBooleanField(param.thisObject, "mFlashDuringPlugged", true);
				}
			});
		}
	}
	
	public static void execHook_ContactsNoCornerSystem() {
		XResources.setSystemWideReplacement("com.htc:drawable/common_photo_frame_quick_contact_mask", new XResources.DrawableLoader(){
			@Override
			public Drawable newDrawable(XResources res, int id) throws Throwable {
				return new ColorDrawable(Color.TRANSPARENT);
			}
		});
	}
	
	public static void execHook_ContactsNoCorner(final InitPackageResourcesParam resparam) {
		if (resparam.packageName.equals("com.htc.contacts")) try {
			resparam.res.setReplacement(resparam.packageName, "drawable", "common_photo_frame_quick_contact_mask", Color.TRANSPARENT);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_ExtremePowerSaverRemap(final LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.htc.powersavinglauncher.Workspace", lpparam.classLoader, "addInScreen",
					View.class, long.class, int.class, int.class, int.class, int.class, int.class, boolean.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						XMain.pref.reload();
						if (XMain.pref.getBoolean("eps_remap_active", false)) {
							final View shortcut = (View)param.args[0];
							if (shortcut != null) {
								shortcut.setOnLongClickListener(new OnLongClickListener(){
									@Override
									public boolean onLongClick(View v) {
										XposedHelpers.callStaticMethod(findClass("com.htc.powersavinglauncher.exit.ExitUtil", lpparam.classLoader), "exitPowerSavingMode", shortcut.getContext(), 1);
										return true;
									}
								});
							}
						}
					}
				});
		} catch (Throwable t) {
			findAndHookMethod("com.htc.powersavinglauncher.Workspace", lpparam.classLoader, "a",
				View.class, long.class, int.class, int.class, int.class, int.class, int.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XMain.pref.reload();
					if (XMain.pref.getBoolean("eps_remap_active", false)) {
						final View shortcut = (View)param.args[0];
						if (shortcut != null) {
							shortcut.setOnLongClickListener(new OnLongClickListener(){
								@Override
								public boolean onLongClick(View v) {
									XposedHelpers.callStaticMethod(findClass("com.htc.powersavinglauncher.a.a", lpparam.classLoader), "a", shortcut.getContext(), 1);
									return true;
								}
							});
						}
					}
				}
			});
		}
		
		try {
			findAndHookMethod("com.htc.powersavinglauncher.scene.SceneLoader", lpparam.classLoader, "loadDefaultXMLScene", Context.class, int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					@SuppressWarnings("unchecked")
					ArrayList<Object> favs = (ArrayList<Object>)param.getResult();
					XMain.pref.reload();
					if (XMain.pref.getBoolean("eps_remap_active", false))
					for (int i = 0; i <= 5; i++) try {
						String pkgActName = XMain.pref.getString("eps_remap_cell" + String.valueOf(i + 1), null);
						if (pkgActName != null) {
							String[] pkgActArray = pkgActName.split("\\|");
							Class<?> favCls = XposedHelpers.findClass("com.htc.powersavinglauncher.scene.FavoriteItem", lpparam.classLoader);
							Constructor<?> favCtr = favCls.getConstructor(int.class, int.class, int.class, int.class, int.class, int.class, int.class);
							int cellX = 0, cellY = 0;
							switch (i) {
								case 0: cellX = 0; cellY = 0; break;
								case 1: cellX = 1; cellY = 0; break;
								case 2: cellX = 0; cellY = 1; break;
								case 3: cellX = 1; cellY = 1; break;
								case 4: cellX = 0; cellY = 2; break;
								case 5: cellX = 1; cellY = 2; break;
							}
							Object fav = favCtr.newInstance(1, 0, cellX, cellY, 0, 0, -100);
							XposedHelpers.callMethod(fav, "setAsShortcut", pkgActArray[0], pkgActArray[1]);
							if (i >= favs.size())
								favs.add(fav);
							else
								favs.set(i, fav);
						}
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		} catch (Throwable t) {
			findAndHookMethod("com.htc.powersavinglauncher.b.b", lpparam.classLoader, "b", Context.class, int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					@SuppressWarnings("unchecked")
					ArrayList<Object> favs = (ArrayList<Object>)param.getResult();
					XMain.pref.reload();
					if (XMain.pref.getBoolean("eps_remap_active", false))
					for (int i = 0; i <= 5; i++) try {
						String pkgActName = XMain.pref.getString("eps_remap_cell" + String.valueOf(i + 1), null);
						if (pkgActName != null) {
							String[] pkgActArray = pkgActName.split("\\|");
							Class<?> favCls = XposedHelpers.findClass("com.htc.powersavinglauncher.b.a", lpparam.classLoader);
							Constructor<?> favCtr = favCls.getConstructor(int.class, int.class, int.class, int.class, int.class, int.class, int.class);
							int cellX = 0, cellY = 0;
							switch (i) {
								case 0: cellX = 0; cellY = 0; break;
								case 1: cellX = 1; cellY = 0; break;
								case 2: cellX = 0; cellY = 1; break;
								case 3: cellX = 1; cellY = 1; break;
								case 4: cellX = 0; cellY = 2; break;
								case 5: cellX = 1; cellY = 2; break;
							}
							Object fav = favCtr.newInstance(1, 0, cellX, cellY, 0, 0, -100);
							XposedHelpers.callMethod(fav, "a", pkgActArray[0], pkgActArray[1]);
							if (i >= favs.size())
								favs.add(fav);
							else
								favs.set(i, fav);
						}
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		}
	}
	
	private static LoadPackageParam phoneLPP = null;
	private static BroadcastReceiver mBRUSSD = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			try {
				if (phoneLPP != null) {
					XposedHelpers.setAdditionalStaticField(findClass("com.android.phone.PhoneUtils", phoneLPP.classLoader), "hideNextUSSD", true);
					String ussd = intent.getStringExtra("number");
					Intent ussdIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussd));
					ussdIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(ussdIntent);
				}
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
	
	public static void execHook_USSD(final LoadPackageParam lpparam) {
		try {
			XposedBridge.hookAllConstructors(findClass("com.android.phone.PhoneGlobals", lpparam.classLoader), new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					phoneLPP = lpparam;
					Context ctx = (Context)param.args[0];
					IntentFilter intentfilter = new IntentFilter();
					intentfilter.addAction("com.sensetoolbox.six.USSD_REQ");
					ctx.registerReceiver(mBRUSSD, intentfilter);
				}
			});
			
			findAndHookMethod("com.android.phone.PhoneUtils", lpparam.classLoader, "displayMMIInitiate", Context.class, "com.android.internal.telephony.MmiCode", Message.class, Dialog.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Boolean hideNextUSSD = (Boolean)XposedHelpers.getAdditionalStaticField(findClass("com.android.phone.PhoneUtils", lpparam.classLoader), "hideNextUSSD");
					if (hideNextUSSD != null && hideNextUSSD) {
						Context ctx = (Context)param.args[0];
						Toast.makeText(ctx, "Executing hidden USSD request", Toast.LENGTH_SHORT).show();
						param.setResult(null);
					}
					//Object mmicode = param.args[1];
					//XposedBridge.log((String)XposedHelpers.callMethod(mmicode, "toString"));
				}
			});
			
			findAndHookMethod("com.android.phone.PhoneUtils", lpparam.classLoader, "displayMMIComplete", "com.android.internal.telephony.Phone", Context.class, "com.android.internal.telephony.MmiCode", Message.class, HtcAlertDialog.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Boolean hideNextUSSD = (Boolean)XposedHelpers.getAdditionalStaticField(findClass("com.android.phone.PhoneUtils", lpparam.classLoader), "hideNextUSSD");
					if (hideNextUSSD != null && hideNextUSSD) {
						XposedHelpers.setAdditionalStaticField(findClass("com.android.phone.PhoneUtils", lpparam.classLoader), "hideNextUSSD", false);

						Context ctx = (Context)param.args[1];
						Object mmicode = param.args[2];
						String msg = (String)XposedHelpers.callMethod(mmicode, "getMessage");
						if (msg != null) {
							//Toast.makeText(ctx, "Response to hidden USSD:\n" + msg, Toast.LENGTH_LONG).show();
							Intent ussdRespIntent = new Intent("com.sensetoolbox.six.USSD_RESP");
							ussdRespIntent.putExtra("response", msg);
							ctx.sendBroadcast(ussdRespIntent);
						} else {
							Intent ussdRespIntent = new Intent("com.sensetoolbox.six.USSD_RESP");
							ussdRespIntent.putExtra("response", "");
							ctx.sendBroadcast(ussdRespIntent);
						}
						param.setResult(null);
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_MusicChannel(LoadPackageParam lpparam, Boolean isEnhancer) {
		String className = "com.android.settings.framework.core.umc.HtcUmcWidgetEnabler";
		if (isEnhancer) className = "com.htc.musicenhancer.cronus.CronusUtils";
		findAndHookMethod(className, lpparam.classLoader, "isSupportMusicChannel", XC_MethodReplacement.returnConstant(Boolean.TRUE));
		
		if (!isEnhancer && Helpers.isLP())
		findAndHookMethod("com.android.settings.framework.core.umc.HtcUmcWidgetEnabler", lpparam.classLoader, "onToggleChangeInBackground", boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if ((Boolean)param.args[0]) {
					param.setResult(null);
					XposedHelpers.setBooleanField(param.thisObject, "mUmcState", true);
					XposedHelpers.setBooleanField(param.thisObject, "mUmcStateUpdated", true);
					XposedHelpers.callMethod(param.thisObject, "updateUI");
					XposedHelpers.callMethod(param.thisObject, "setUmcStateInDb", true);
					
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					if (mContext != null) {
						Intent musChannel = new Intent("com.htc.musicenhancer.action.UNIVERSAL_MUSIC_CHANNEL");
						musChannel.setPackage("com.htc.musicenhancer");
						mContext.startService(musChannel);
					}
				}
			}
		});
	}
	
	private static void refreshQSMCView(Object thisObject) {
		Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
		LinearLayout QSMC = (LinearLayout)thisObject;
		int music_channel_state = Settings.System.getInt(mContext.getContentResolver(), "htc_universal_music_channel", 1);
				
		int qsind = mContext.getResources().getIdentifier("quick_setting_indicator", "id", "com.android.systemui");
		View quick_setting_indicator = null;
		if (qsind != 0) quick_setting_indicator = QSMC.findViewById(qsind);
		if (quick_setting_indicator != null)
		XposedHelpers.callMethod(quick_setting_indicator, "setLevel", music_channel_state, 1);
		
		int qsimg = mContext.getResources().getIdentifier("quick_setting_image", "id", "com.android.systemui");
		View quick_setting_image = null;
		if (qsimg != 0) quick_setting_image = QSMC.findViewById(qsimg);
		if (quick_setting_image != null)
		XposedHelpers.callMethod(quick_setting_image, "setOverlayEnable", music_channel_state == 1 ? true : false);
	}
	
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
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange);
			try {
				String uriPart = uri.getLastPathSegment();
				if (uriPart != null && uriPart.equals("htc_universal_music_channel"))
				if (thisObj != null) refreshQSMCView(thisObj);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	}
	
	public static void execHook_MusicChannelEQSTile(LoadPackageParam lpparam) {
		if (Helpers.isLP()) {
			findAndHookMethod("com.android.systemui.statusbar.phone.QSTileHost", lpparam.classLoader, "getQSAvailableList", Context.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					int[] ai = (int[])param.getResult();
					if (ai != null && ai.length > 0 && !Arrays.asList(ai).contains(17)) {
						ai = Arrays.copyOf(ai, ai.length + 1);
						ai[ai.length - 1] = 17;
						param.setResult(ai);
					}
				}
			});
		} else try {
			findAndHookMethod("com.android.systemui.statusbar.phone.QuickSettings", lpparam.classLoader, "getQSAvailableList", Context.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					int[] ai = (int[])param.getResult();
					if (ai != null && ai.length > 0 && !Arrays.asList(ai).contains(17)) {
						ai = Arrays.copyOf(ai, ai.length + 1);
						ai[ai.length - 1] = 17;
						param.setResult(ai);
					}
				}
			});
		} catch (Throwable t) {
			findAndHookMethod("com.android.systemui.statusbar.phone.QuickSettings", lpparam.classLoader, "getQSAvailableList", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					int[] ai = (int[])param.getResult();
					if (ai != null && ai.length > 0 && !Arrays.asList(ai).contains(17)) {
						ai = Arrays.copyOf(ai, ai.length + 1);
						ai[ai.length - 1] = 17;
						param.setResult(ai);
					}
				}
			});
		}
		
		String className = "com.android.systemui.statusbar.quicksetting.QuickSettingMusicChannel";
		if (Helpers.isLP()) className = "com.android.systemui.qs.tiles.QuickSettingMusicChannel";
		findAndHookMethod(className, lpparam.classLoader, "onAttachedToWindow", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				final Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				final LinearLayout QSMC = (LinearLayout)param.thisObject;
				
				mContext.getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, new SystemSettingsObserver(new Handler(), param.thisObject));
				refreshQSMCView(param.thisObject);
				
				QSMC.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						(new AsyncTask<Void,Void,Void>() {
							@Override
							protected Void doInBackground(Void... params) {
								int newState = Settings.System.getInt(mContext.getContentResolver(), "htc_universal_music_channel", 1) == 1 ? 0 : 1;
								Settings.System.putInt(mContext.getContentResolver(), "htc_universal_music_channel", newState);
								
								if (newState == 1) {
									Intent musChannel = new Intent("com.htc.musicenhancer.action.UNIVERSAL_MUSIC_CHANNEL");
									musChannel.setPackage("com.htc.musicenhancer");
									mContext.startService(musChannel);
								}
								
								return null;
							}
						}).execute(new Void[0]);
					}
				});
				
				QSMC.setLongClickable(true);
				QSMC.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						Intent umc = new Intent("com.htc.music.intent.action.UMC_SETTINGS");
						umc.addCategory(Intent.CATEGORY_DEFAULT);
						umc.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mContext.startActivity(umc);
						GlobalActions.collapseDrawer(v.getContext());
						return true;
					}
				});
			}
		});
	}
	
	public static void execHook_MusicChannelEQSTileIcon(final InitPackageResourcesParam resparam) {
		final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, resparam.res);
		resparam.res.setReplacement("com.android.systemui", "drawable", "icon_btn_dummy", modRes.fwd(R.drawable.icon_btn_music_channel_light));
		resparam.res.hookLayout("com.android.systemui", "layout", "quick_settings_tile_music_channel", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				ImageView quick_setting_image = (ImageView)liparam.view.findViewById(resparam.res.getIdentifier("quick_setting_image", "id", "com.android.systemui"));
				if (quick_setting_image != null)
				quick_setting_image.setImageDrawable(modRes.getDrawable(R.drawable.icon_btn_music_channel_dark_xl));
			}
		});
		if (!Helpers.isLP())
		resparam.res.hookLayout("com.android.systemui", "layout", "quick_settings_tile_music_channel_minor", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				ImageView quick_setting_image = (ImageView)liparam.view.findViewById(resparam.res.getIdentifier("quick_setting_image", "id", "com.android.systemui"));
				if (quick_setting_image != null)
				quick_setting_image.setImageDrawable(modRes.getDrawable(R.drawable.icon_btn_music_channel_dark_l));
			}
		});
	}
	
	public static StatusBarTapReceiver sbtReceiver = new StatusBarTapReceiver();
	public static Activity mainAct = null;
	public static Activity reviewAct = null;
	
	public static class StatusBarTapReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mainAct != null) try {
				View details_expanded_scroller = mainAct.findViewById(mainAct.getResources().getIdentifier("details_expanded_scroller", "id", "com.android.vending"));
				if (details_expanded_scroller != null && details_expanded_scroller instanceof ScrollView) ((ScrollView)details_expanded_scroller).smoothScrollTo(0, 0);
				
				View bucket_list_view = mainAct.findViewById(mainAct.getResources().getIdentifier("bucket_list_view", "id", "com.android.vending"));
				if (bucket_list_view != null && bucket_list_view instanceof ListView) ((ListView)bucket_list_view).smoothScrollToPosition(0);
				
				View details_scroller = mainAct.findViewById(mainAct.getResources().getIdentifier("details_scroller", "id", "com.android.vending"));
				if (details_scroller != null && details_scroller instanceof ScrollView) ((ScrollView)details_scroller).smoothScrollTo(0, 0);
				
				View page_content = mainAct.findViewById(mainAct.getResources().getIdentifier("page_content", "id", "com.android.vending"));
				if (page_content != null && page_content instanceof ScrollView) ((ScrollView)page_content).smoothScrollTo(0, 0);
				
				ViewGroup viewpager = (ViewGroup)mainAct.findViewById(mainAct.getResources().getIdentifier("viewpager", "id", "com.android.vending"));
				if (viewpager != null) {
					int pages = viewpager.getChildCount();
					for (int pg = 0; pg < pages; pg++) {
						View page = viewpager.getChildAt(pg);
						if (page != null)
						if (page instanceof ListView) ((ListView)page).smoothScrollToPosition(0); else {
							View bucket_list_view2 = page.findViewById(mainAct.getResources().getIdentifier("bucket_list_view", "id", "com.android.vending"));
							if (bucket_list_view2 != null && bucket_list_view2 instanceof ListView) ((ListView)bucket_list_view2).smoothScrollToPosition(0);
							
							View my_apps_content_list = page.findViewById(mainAct.getResources().getIdentifier("my_apps_content_list", "id", "com.android.vending"));
							if (my_apps_content_list != null && my_apps_content_list instanceof ListView) ((ListView)my_apps_content_list).smoothScrollToPosition(0);
						}
					}
				}
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
			
			if (reviewAct != null) try {
				View all_reviews_list = reviewAct.findViewById(reviewAct.getResources().getIdentifier("all_reviews_list", "id", "com.android.vending"));
				if (all_reviews_list != null && all_reviews_list instanceof ListView) ((ListView)all_reviews_list).smoothScrollToPosition(0);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	}
	
	public static void execHook_PSScroll(LoadPackageParam lpparam) {
		findAndHookMethod("com.google.android.finsky.activities.MainActivity", lpparam.classLoader, "onResume", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				mainAct = (Activity)param.thisObject;
				if (mainAct != null) try {
					mainAct.registerReceiver(sbtReceiver, new IntentFilter("com.htc.intent.action.STATUS_BAR_TAP_EVENT"), "com.htc.permission.APP_PLATFORM", null);
				} catch (Throwable t) {}
			}
		});
		
		findAndHookMethod("com.google.android.finsky.activities.MainActivity", lpparam.classLoader, "onPause", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				mainAct = (Activity)param.thisObject;
				if (mainAct != null) try {
					mainAct.unregisterReceiver(sbtReceiver);
				} catch (Throwable t) {}
			}
		});
		
		findAndHookMethod("com.google.android.finsky.activities.ReviewsActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				reviewAct = (Activity)param.thisObject;
				if (reviewAct != null) try {
					reviewAct.registerReceiver(sbtReceiver, new IntentFilter("com.htc.intent.action.STATUS_BAR_TAP_EVENT"), "com.htc.permission.APP_PLATFORM", null);
				} catch (Throwable t) {}
			}
		});
	}
	
	public static void execHook_ScreenshotViewer(final LoadPackageParam lpparam) {
		findAndHookMethod("com.android.systemui.screenshot.SaveImageInBackgroundTask", lpparam.classLoader, "onPostExecute", "com.android.systemui.screenshot.SaveImageInBackgroundData", new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Object saveimageinbackgrounddata = param.args[0];
					if ((Boolean)XposedHelpers.callMethod(param.thisObject, "isCancelled")) {
						Runnable finisher = (Runnable)XposedHelpers.getObjectField(saveimageinbackgrounddata, "finisher");
						finisher.run();
						XposedHelpers.callMethod(saveimageinbackgrounddata, "clearImage");
						XposedHelpers.callMethod(saveimageinbackgrounddata, "clearContext");
						return null;
					}
					int result = (Integer)XposedHelpers.getObjectField(saveimageinbackgrounddata, "result");
					if (result > 0) {
						XposedHelpers.callStaticMethod(
							findClass("com.android.systemui.screenshot.GlobalScreenshot", lpparam.classLoader),
							"notifyScreenshotError",
							XposedHelpers.getObjectField(saveimageinbackgrounddata, "context"),
							XposedHelpers.getObjectField(param.thisObject, "mNotificationManager")
						);
					} else {
						Context context = (Context)XposedHelpers.getObjectField(saveimageinbackgrounddata, "context");
						Resources resources = context.getResources();
						
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						Uri imageUri = (Uri)XposedHelpers.getObjectField(saveimageinbackgrounddata, "imageUri");
						intent.setDataAndType(imageUri, "image/*");

						Notification.Builder mNotificationBuilder = (Notification.Builder)XposedHelpers.getObjectField(param.thisObject, "mNotificationBuilder");
						mNotificationBuilder.setContentTitle(resources.getString(resources.getIdentifier("screenshot_saved_title", "string", "com.android.systemui")))
											.setContentText(resources.getString(resources.getIdentifier("screenshot_saved_text", "string", "com.android.systemui")))
											.setWhen(System.currentTimeMillis())
											.setAutoCancel(true);
						
						PackageManager manager = context.getPackageManager();
						List<ResolveInfo> info = manager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
						if (info.isEmpty()) {
							XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
							Toast.makeText(context, Helpers.xl10n(modRes, R.string.various_screenopen_noapps), Toast.LENGTH_LONG).show();
							mNotificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0));
						} else {
							mNotificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));
						}
						
						Notification notification = mNotificationBuilder.build();
						notification.flags = 0xffffffdf & notification.flags;
						NotificationManager mNotificationManager = (NotificationManager)XposedHelpers.getObjectField(param.thisObject, "mNotificationManager");
						int mNotificationId = (Integer)XposedHelpers.getObjectField(param.thisObject, "mNotificationId");
						mNotificationManager.notify(mNotificationId, notification);
					}
					Runnable finisher = (Runnable)XposedHelpers.getObjectField(saveimageinbackgrounddata, "finisher");
					finisher.run();
					XposedHelpers.callMethod(saveimageinbackgrounddata, "clearContext");
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
				return null;
			}
		});
	}
	
	public static void execHook_KeyboardNoAutocorrect(LoadPackageParam lpparam) {
		try {
			if (Helpers.isSense7()) {
				XposedHelpers.findAndHookMethod("com.htc.sense.ime.latinim.util.PredictionInfo", lpparam.classLoader, "setIdxEngAdvised", int.class, new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						param.args[0] = 0;
					}
				});
				XposedHelpers.findAndHookMethod("com.htc.sense.ime.latinim.util.PredictionInfo", lpparam.classLoader, "setIdxIMEAdvised", int.class, new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						param.args[0] = 0;
					}
				});
				/*
				XposedHelpers.findAndHookMethod("com.htc.sense.ime.latinim.LatinIMInfo", lpparam.classLoader, "setIdxEngAdvised", int.class, new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						param.args[0] = 0;
					}
				});
				XposedHelpers.findAndHookMethod("com.htc.sense.ime.latinim.LatinIMInfo", lpparam.classLoader, "setIdxIMEAdvised", int.class, new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						param.args[0] = 0;
					}
				});
				*/
			} else {
				XposedHelpers.findAndHookMethod("com.htc.sense.ime.XT9IME.XT9Engine", lpparam.classLoader, "getActiveWordIndex", XC_MethodReplacement.returnConstant(Integer.valueOf(0)));
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_SecureEQS(final LoadPackageParam lpparam) {
		XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.policy.KeyguardMonitor", lpparam.classLoader, "notifyKeyguardState", boolean.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				boolean isShowing = (Boolean)param.args[0];
				boolean isSecure = (Boolean)param.args[1];
				XposedHelpers.setAdditionalStaticField(findClass("com.android.systemui.statusbar.policy.KeyguardMonitor", lpparam.classLoader), "isOnSecureLockscreen", isShowing && isSecure);
			}
		});
		
		XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				MotionEvent ev = (MotionEvent)param.args[0];
				if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
					Boolean isOnSecureLockscreen = (Boolean)XposedHelpers.getAdditionalStaticField(findClass("com.android.systemui.statusbar.policy.KeyguardMonitor", lpparam.classLoader), "isOnSecureLockscreen");
					if (isOnSecureLockscreen != null && isOnSecureLockscreen.booleanValue()) param.setResult(true);
				}
			}
		});
	}
	
	public static void buttonBacklightService(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.android.server.wm.WindowManagerService", lpparam.classLoader, "statusBarVisibilityChanged", int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					Intent intent = new Intent("com.sensetoolbox.six.UPDATEBACKLIGHT");
					
					int sysUiVis = (Integer)param.args[0];
					if (sysUiVis == 67108864 || sysUiVis == 0) return;
					//XposedBridge.log("statusBarVisibilityChanged: " + String.valueOf(sysUiVis));
					if (sysUiVis != 0 && ((sysUiVis & View.SYSTEM_UI_FLAG_FULLSCREEN) == View.SYSTEM_UI_FLAG_FULLSCREEN
						|| (sysUiVis & View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
						|| (sysUiVis & View.SYSTEM_UI_FLAG_LOW_PROFILE) == View.SYSTEM_UI_FLAG_LOW_PROFILE)
						|| ((sysUiVis & View.SYSTEM_UI_FLAG_IMMERSIVE) == View.SYSTEM_UI_FLAG_IMMERSIVE && (sysUiVis & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == View.SYSTEM_UI_FLAG_HIDE_NAVIGATION))
						intent.putExtra("forceDisableBacklight", true);
					
					mContext.sendBroadcast(intent);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void buttonBacklightSystem() {
		try {
			findAndHookMethod(Window.class, "setFlags", int.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Window wnd = (Window)param.thisObject;
					if (wnd != null && wnd.getContext().getPackageName().equals("com.google.android.youtube")) {
						WindowManager.LayoutParams mWindowAttributes = (WindowManager.LayoutParams)XposedHelpers.getObjectField(param.thisObject, "mWindowAttributes");
						if (mWindowAttributes == null) return;
						int i = (Integer)param.args[0];
						int j = (Integer)param.args[1];
						int newFlags = mWindowAttributes.flags & ~j | i & j;
						
						if (newFlags != 0 &&
						(newFlags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != WindowManager.LayoutParams.FLAG_FULLSCREEN &&
						(newFlags & WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN) == WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN &&
						(newFlags & WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR) == WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR) {
								//XposedBridge.log("setFlags FLAG_LAYOUT_*: " + String.valueOf(newFlags));
								Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
								mContext.sendBroadcast(new Intent("com.sensetoolbox.six.UPDATEBACKLIGHT"));
						}
					}
				}
			});
			
			findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Activity act = (Activity)param.thisObject;
					if (act == null) return;
					int newFlags = act.getWindow().getAttributes().flags;
					//XposedBridge.log("onResume flags: " + String.valueOf(newFlags));
					Intent intent = new Intent("com.sensetoolbox.six.UPDATEBACKLIGHT");
					if (newFlags != 0 && (newFlags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN && !act.getPackageName().equals("com.android.systemui"))
					intent.putExtra("forceDisableBacklight", true);
					act.sendBroadcast(intent);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_NoAutoIME() {
		findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Activity act = (Activity)param.thisObject;
					if (act != null) {
						int softMode = act.getWindow().getAttributes().softInputMode;
						if ((softMode & WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) == WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE ||
							(softMode & WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
							act.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
	
	public static void execHook_NoAutoIMEService(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.android.server.InputMethodManagerService", lpparam.classLoader, "showSoftInput", "com.android.internal.view.IInputMethodClient", int.class, "android.os.ResultReceiver", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					if ((Integer)param.args[1] == InputMethodManager.SHOW_IMPLICIT) param.setResult(false);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_scramblePIN(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.htc.lockscreen.unlockscreen.HtcPinKeyboard", lpparam.classLoader, "initView", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					try {
						View mOk = (View)XposedHelpers.getObjectField(param.thisObject, "mOk");
						View mDel = (View)XposedHelpers.getObjectField(param.thisObject, "mDel");
						View[] mNumberButtons = (View[])XposedHelpers.getObjectField(param.thisObject, "mNumberButtons");
						LinearLayout row1 = (LinearLayout)mNumberButtons[1].getParent();
						LinearLayout row2 = (LinearLayout)mNumberButtons[4].getParent();
						LinearLayout row3 = (LinearLayout)mNumberButtons[7].getParent();
						LinearLayout row4 = (LinearLayout)mNumberButtons[0].getParent();
						
						ArrayList<Integer> newOrder = new ArrayList<Integer>();
						for (int i = 0; i <= 9; i++) newOrder.add(i);
						Collections.shuffle(newOrder);
						
						row1.removeAllViews();
						row2.removeAllViews();
						row3.removeAllViews();
						row4.removeAllViews();
						
						row1.addView(mNumberButtons[newOrder.get(0)]);
						row1.addView(mNumberButtons[newOrder.get(1)]);
						row1.addView(mNumberButtons[newOrder.get(2)]);
						row2.addView(mNumberButtons[newOrder.get(3)]);
						row2.addView(mNumberButtons[newOrder.get(4)]);
						row2.addView(mNumberButtons[newOrder.get(5)]);
						row3.addView(mNumberButtons[newOrder.get(6)]);
						row3.addView(mNumberButtons[newOrder.get(7)]);
						row3.addView(mNumberButtons[newOrder.get(8)]);
						row4.addView(mDel);
						row4.addView(mNumberButtons[newOrder.get(9)]);
						row4.addView(mOk);
						
						row1.invalidate();
						row2.invalidate();
						row3.invalidate();
						row4.invalidate();
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_NoFlipToMute(LoadPackageParam lpparam) {
		try {
			if (Helpers.isLP()) {
				findAndHookMethod("com.android.phone.HtcPhoneSensorFunctions", lpparam.classLoader, "handleRotateToSilent", float.class, float.class, XC_MethodReplacement.DO_NOTHING);
			} else {
				findAndHookMethod("com.android.phone.HtcPhoneSensorFunctionsOrient", lpparam.classLoader, "handleRotateToSilent", float.class, float.class, XC_MethodReplacement.DO_NOTHING);
				findAndHookMethod("com.android.phone.HtcPhoneSensorFunctionNonOrient", lpparam.classLoader, "handleRotateToSilent", float.class, float.class, XC_MethodReplacement.DO_NOTHING);
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_NoFlipToMuteSetting(LoadPackageParam lpparam) {
		try {
			String className = "com.android.settings.SoundSettings";
			if (Helpers.isLP()) className = "com.android.settings.notification.NotificationSettings";
			findAndHookMethod(className, lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Object notifySettings = param.thisObject;
					if (notifySettings != null) {
						Object flipToMutePref = XposedHelpers.callMethod(notifySettings, "findPreference", "HtcFlipToMutePreference");
						if (flipToMutePref != null) {
							XposedHelpers.callMethod(flipToMutePref, "setEnabled", false);
							XposedHelpers.callMethod(flipToMutePref, "setSummary", Helpers.xl10n(XModuleResources.createInstance(XMain.MODULE_PATH, null), R.string.disabled_by_toolbox));
						}
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_KeyboardTraceColor(final LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.htc.sense.ime.ezsip.trace.Trace", lpparam.classLoader, "startInput", View.class, new XC_MethodHook(100) {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Object mConfig = XposedHelpers.getObjectField(param.thisObject, "mConfig");
					int mStrokeColor = XposedHelpers.getIntField(mConfig, "mStrokeColor");
					int highlightColor = mStrokeColor;
					
					try {
						if (Helpers.isSense7())
							highlightColor = XposedHelpers.getStaticIntField(findClass("com.htc.sense.ime.NonAndroidSDK.HtcThemeUtilForService", lpparam.classLoader), "sIMEColorHighlight");
						else
							highlightColor = XposedHelpers.getStaticIntField(findClass("com.htc.sense.ime.HTCIMMData", lpparam.classLoader), "mCategoryColor");
					} catch (Throwable t) {
						XposedBridge.log(t);
					}

					//int sMultiplyColor = XposedHelpers.getStaticIntField(findClass("com.htc.sense.ime.NonAndroidSDK.HtcThemeUtilForService", lpparam.classLoader), "sMultiplyColor");
					//int sIMEColorKeyText = XposedHelpers.getStaticIntField(findClass("com.htc.sense.ime.NonAndroidSDK.HtcThemeUtilForService", lpparam.classLoader), "sIMEColorKeyText");
					
					Paint mPaint = (Paint)XposedHelpers.getObjectField(param.thisObject, "mPaint");
					mPaint.setColor(highlightColor);
					mPaint.setAlpha(128);
					mPaint.setAntiAlias(true);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_KeyboardTraceAlpha(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.htc.sense.ime.ezsip.trace.Trace", lpparam.classLoader, "startInput", View.class, new XC_MethodHook(1000) {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Paint mPaint = (Paint)XposedHelpers.getObjectField(param.thisObject, "mPaint");
					mPaint.setAlpha((int) Math.floor(XMain.pref.getInt("pref_key_other_tracealpha", 50) * 2.55f));
					mPaint.setAntiAlias(true);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static Context dialerContext = null;
	public static SharedPreferences mPrefs = null;
	public static HashMap<String, String> queryCache = new HashMap<String, String>();
	public static String queryContactFullName(long id, String origName, LoadPackageParam lpparam) {
		if (dialerContext == null) return "";
		boolean displayOrder = false;
		if (mPrefs == null) try {
			mPrefs = (SharedPreferences)XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.htc.contacts.util.ContactsUtils", lpparam.classLoader), "getDefaultSharedPreferences", dialerContext);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		if (mPrefs != null)
		displayOrder = mPrefs.getBoolean("All contact display order", false);
		if (!displayOrder) return "";
		
		String key = String.valueOf(id) + "_" + origName;
		String fullName = queryCache.get(key);
		if (fullName != null) {
			return fullName;
		} else {
			String firstName = "", middleName = "", lastName = "";
			String[] nameProjection = new String[] {
				ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
				ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
				ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
			};
			
			try (Cursor nameCursor = dialerContext.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, nameProjection,
				ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID + " = ? AND " +
				ContactsContract.CommonDataKinds.StructuredName.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "'",
				new String[] { String.valueOf(id) }, null
			)) {
				if (nameCursor.moveToNext()) {
					firstName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
					middleName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
					lastName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
				}
			}
			
			fullName = "";
			if (lastName == null || lastName.isEmpty()) return ""; else fullName = lastName + " ";
			if (firstName != null && !firstName.isEmpty()) fullName += firstName + " ";
			if (middleName != null && !middleName.isEmpty()) fullName += middleName + " ";
			fullName = fullName.trim();
			queryCache.put(key, fullName);
			return fullName;
		}
	}
	
	public static void execHook_ContactsNameOrder(final LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.htc.contacts.ui.ContactsPreferencesActivity", lpparam.classLoader, "setSortOrder", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					queryCache.clear();
				}
			});
			
			findAndHookMethod("com.htc.contacts.fragment.BrowseCallHistoryFragment.RecentCallsAdapter", lpparam.classLoader, "bindView", View.class, Context.class, Cursor.class, int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					dialerContext = (Context)param.args[1];
					Cursor cursor = (Cursor)param.args[2];
					if (cursor == null) return;
					
					String fullName = "";
					try {
						long id = cursor.getInt(15);
						String origName = cursor.getString(12);
						fullName = queryContactFullName(id, origName, lpparam);
						View recentItem = (View)param.args[0];
						if (fullName.isEmpty() || recentItem == null) return;
						
						Object mListItem2LineText = XposedHelpers.getObjectField(recentItem.getTag(), "mListItem2LineText");
						if (mListItem2LineText != null)
						XposedHelpers.callMethod(mListItem2LineText, "setPrimaryText", fullName);
						
						@SuppressWarnings("unchecked")
						HashMap<String, ?> mContactInfo = (HashMap<String, ?>)XposedHelpers.getObjectField(param.thisObject, "mContactInfo");
						if (mContactInfo != null) {
							Object contactInfo = mContactInfo.get(String.valueOf(cursor.getInt(0)));
							if (contactInfo != null) {
								String name = (String)XposedHelpers.getObjectField(contactInfo, "name");
								if (name != null && !name.isEmpty())
								XposedHelpers.setObjectField(contactInfo, "name", fullName);
							}
						}
						
						String mDisplayName = (String)XposedHelpers.getObjectField(recentItem.getTag(), "mDisplayName");
						if (mDisplayName != null && !mDisplayName.isEmpty())
						XposedHelpers.setObjectField(recentItem.getTag(), "mDisplayName", fullName);
						XposedHelpers.callMethod(XposedHelpers.getSurroundingThis(param.thisObject), "updateNameString", recentItem.getTag());
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		
		try {
			findAndHookMethod("com.htc.htcdialer.BaseSmartSearchList.SearchListAdapter", lpparam.classLoader, "getNameMarked", "com.htc.htcdialer.search.SearchableObject", "com.htc.htcdialer.search.SearchableContact", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					dialerContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				}
			});
			
			findAndHookMethod("com.htc.htcdialer.search.SearchableObject", lpparam.classLoader, "getContactName", "com.htc.htcdialer.search.SearchableObject", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Object sContact = XposedHelpers.callStaticMethod(findClass("com.htc.htcdialer.search.SearchableObject", lpparam.classLoader), "getContact", param.args[0]);
					if (sContact == null) return;
					
					String fullName = "";
					try {
						fullName = queryContactFullName(XposedHelpers.getLongField(sContact, "id"), (String)XposedHelpers.getObjectField(sContact, "name"), lpparam);
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
					
					if (param.getResult() != null && !fullName.isEmpty()) try {
						XposedHelpers.setObjectField(sContact, "name", fullName);
						XposedHelpers.callMethod(sContact, "setNamePattern", fullName);
						String sortKey = (String)XposedHelpers.callMethod(sContact, "extractSortKey", fullName);
						XposedHelpers.setObjectField(sContact, "sortKey", sortKey);
						XposedHelpers.setObjectField(sContact, "namePosition", XposedHelpers.callMethod(sContact, "getNamePosition", fullName));
						String sortChar = (String)XposedHelpers.getObjectField(sContact, "sortChar");
						String s5;
						if (sortChar == null)
							s5 = sortKey;
						else
							s5 = sortChar;
						XposedHelpers.setObjectField(sContact, "sectionIndex", XposedHelpers.callMethod(sContact, "getSectionIndex", s5));
						param.setResult(fullName);
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	/*
	public static void execHook_GlobalEffectNotification() {
		try {
			findAndHookMethod("android.media.htcsoundfx.view.EffectViewNotification", null, "createNotification", Context.class, int.class, String.class, String.class, int.class, String.class, Intent.class, boolean.class, new XC_MethodHook() {
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	*/
}

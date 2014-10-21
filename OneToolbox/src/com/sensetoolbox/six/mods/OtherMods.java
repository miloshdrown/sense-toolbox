package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

import com.htc.fragment.widget.CarouselFragment;
import com.htc.widget.HtcAlertDialog;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class OtherMods {
	
	public static void execHook_APM(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.htc.app.HtcShutdownThread", lpparam.classLoader, "reboot", Context.class, String.class, boolean.class, new XC_MethodReplacement() {
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
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClassName("com.sensetoolbox.six", "com.sensetoolbox.six.DimmedActivity");
			intent.putExtra("dialogType", 1);
			ctx.startActivity(intent);
		} catch(Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_VolSound(LoadPackageParam lpparam) {
		findAndHookMethod("com.htc.view.VolumePanel", lpparam.classLoader, "onPlaySound", int.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(null);
			}
		});
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
		
	public static void execHook_PowerSaverNotif(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.htc.htcpowermanager.powersaver.PowerSaverNotificationService", lpparam.classLoader, "addNotification", XC_MethodReplacement.DO_NOTHING);
			XposedHelpers.findAndHookMethod("com.htc.htcpowermanager.powersaver.PowerSaverNotificationService", lpparam.classLoader, "addNtfPowerJacket", int.class, int.class, XC_MethodReplacement.DO_NOTHING);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_InputMethodNotif() {
		try {
			XposedHelpers.findAndHookMethod("com.android.server.InputMethodManagerService", null, "setImeWindowStatus", IBinder.class, int.class, int.class, XC_MethodReplacement.DO_NOTHING);
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
		
	public static void execHook_MoveVolume(StartupParam startparam) {
		try {
			final Class<?> clsVP = XposedHelpers.findClass("com.htc.view.VolumePanel", null);
			XposedHelpers.findAndHookMethod(clsVP, "updatePanelRotationPosition", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Dialog dlg = (Dialog)XposedHelpers.getObjectField(param.thisObject, "mDialog");
					Dialog dlgEx = (Dialog)XposedHelpers.getObjectField(param.thisObject, "mDialogEx");
						
					float density = dlg.getContext().getResources().getDisplayMetrics().density;
					int orientation = dlg.getContext().getResources().getConfiguration().orientation;
					int bottomMargin = 75;
					if (orientation == 2) bottomMargin = 40;
						
					Window dlgWin = dlg.getWindow();
					Window dlgExWin = dlgEx.getWindow();
					WindowManager.LayoutParams dlgWinAttrs = dlgWin.getAttributes();
					WindowManager.LayoutParams dlgExWinAttrs = dlgExWin.getAttributes();
					dlgWinAttrs.gravity = Gravity.BOTTOM;
					dlgExWinAttrs.gravity = Gravity.BOTTOM;
					dlgWinAttrs.y = Math.round(bottomMargin * density);
					dlgExWinAttrs.y = Math.round(bottomMargin * density);
					dlgWin.setAttributes(dlgWinAttrs);
					dlgExWin.setAttributes(dlgExWinAttrs);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	private static boolean updateScreenOn = false;
	private static boolean updateScreenOff = false;
	
	public static void ScreenAnim() {
		try {
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
								Object displaypowerstate = mPowerState;
								if ((Boolean)XposedHelpers.callMethod(displaypowerstate, "prepareElectronBeam", XMain.pref_screenoff) && (Boolean)XposedHelpers.callMethod(mPowerState, "isScreenOn"))
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
								Object displaypowerstate = mPowerState;
								if ((Boolean)XposedHelpers.callMethod(displaypowerstate, "prepareElectronBeam", XMain.pref_screenon) && (Boolean)XposedHelpers.callMethod(mPowerState, "isScreenOn"))
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
		findAndHookMethod("com.htc.lockscreen.unlockscreen.HtcKeyInputUnlockView", lpparam.classLoader, "initView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				final Object mLockPatternUtils = getObjectField(param.thisObject, "mLockPatternUtils");
				final AutoCompleteTextView mPasswordEntry = (AutoCompleteTextView)getObjectField(param.thisObject, "mPasswordEntry");
				if (mLockPatternUtils != null && mPasswordEntry != null) {
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
									if (isPinCorrect) {
										Method onEditorAction = XposedHelpers.findMethodExact("com.htc.lockscreen.unlockscreen.HtcKeyInputUnlockView", lpparam.classLoader, "onEditorAction", TextView.class, int.class, KeyEvent.class);
										onEditorAction.invoke(param.thisObject, null, 0, null);
									}
								}
							} catch (Throwable t) {
								XposedBridge.log(t);
							}
						}
					});
				}
			}
		});
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
				ImageView watermark = (ImageView)liparam.view.findViewById(resparam.res.getIdentifier("featured_channel_watermark", "id", "com.google.android.youtube"));
				watermark.setAlpha(0f);
			}
		});
	}
	
	public static void execHook_SafeVolume(final LoadPackageParam lpparam) {
		findAndHookMethod("android.media.AudioService", lpparam.classLoader, "checkSafeMediaVolume", int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			public void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});
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
		try {
			final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, resparam.res);
			int resId = R.dimen.people_info_top_margin;
			if (photoSize == 2) resId = R.dimen.people_info_top_margin_rect;
			resparam.res.setReplacement("com.android.phone", "dimen", "photo_frame_height", modRes.fwd(resId));
			resparam.res.setReplacement("com.android.phone", "dimen", "lockscreen_10", modRes.fwd(R.dimen.lockscreen_10));
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
			if (photoSize == 2) photoHeight = modRes.getDimensionPixelSize(R.dimen.photo_new_height_rect); else
			if (km.inKeyguardRestrictedInputMode()) {
				photoHeight = modRes.getDimensionPixelSize(R.dimen.photo_new_height_ls);
				if ((Helpers.isM8() || Helpers.isDesire816()) && XMain.pref.getBoolean("pref_key_controls_smallsoftkeys", false)) photoHeight += 54;
			} else {
				photoHeight = modRes.getDimensionPixelSize(R.dimen.photo_new_height);
				if (Helpers.isM8() || Helpers.isDesire816())
				if (XMain.pref.getBoolean("pref_key_controls_smallsoftkeys", false))
					photoHeight -= 58;
				else
					photoHeight -= 112;
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
	}
	
	public static void execHook_RejectCallSilently(LoadPackageParam lpparam) {
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
	
	public static void execHook_EnhancedSecurity() {
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "interceptPowerKeyDown", boolean.class, new XC_MethodHook() {
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
		});
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
	
	private static void sendSbnsArray(ArrayList<StatusBarNotification> sbns, Context mContext, boolean asBroadcast) {
		if (asBroadcast) {
			Intent intent = new Intent("com.sensetoolbox.six.UPDATENOTIFICATIONS");
			intent.putParcelableArrayListExtra("sbns", sbns);
			intent.putExtra("dialogType", 2);
			mContext.sendBroadcast(intent);
		} else try {
			boolean lightUpScreen = XMain.pref.getBoolean("pref_key_other_popupnotify_lightup", false);
			boolean sleepMode = XMain.pref.getBoolean("pref_key_other_popupnotify_sleepmode", false);

			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			
			PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
			if (pm.isScreenOn()) {
				Bitmap bmp = getScreenshot(mContext);
				if (bmp != null) intent.putExtra("bmp", bmp);
			}
			
			boolean isFromPhone = false;
			for (StatusBarNotification sbn: sbns)
			if (sbn != null &&
				sbn.getNotification() != null &&
				sbn.getPackageName().equals("com.android.phone")) {
				isFromPhone = true;
				break;
			}
			
			if (pm.isScreenOn() && sleepMode && !isFromPhone) return;
			if (lightUpScreen) {
				pm.wakeUp(SystemClock.uptimeMillis());
				pm.userActivity(SystemClock.uptimeMillis(), false);
			}
			
			KeyguardManager kgMgr = (KeyguardManager)mContext.getSystemService(Context.KEYGUARD_SERVICE);
			if (kgMgr.isKeyguardLocked())
				intent.setClassName("com.sensetoolbox.six", "com.sensetoolbox.six.DimmedActivityLS");
			else
				intent.setClassName("com.sensetoolbox.six", "com.sensetoolbox.six.DimmedActivity");
			intent.putParcelableArrayListExtra("sbns", sbns);
			intent.putExtra("dialogType", 2);
			
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
	
	public static void execHook_NoChargerWarning(LoadPackageParam lpparam) {
		findAndHookMethod("com.android.settings.NSReceiver", lpparam.classLoader, "showVZWChargerNotification", Context.class, int.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				int type = (Integer)param.args[1];
				if (type == 1) param.setResult(null);
			}
		});
	}
	
	public static void execHook_LEDNotifyTimeout() {
		XposedBridge.hookAllConstructors(findClass("com.android.server.NotificationManagerService", null), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				XposedHelpers.setIntField(param.thisObject, "mFlashTimeout", XMain.pref.getInt("pref_key_other_ledtimeout_value", 5));
			}
		});
	}
	
	public static void execHook_LEDOnCharge() {
		XposedBridge.hookAllConstructors(findClass("com.android.server.NotificationManagerService", null), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				XposedHelpers.setBooleanField(param.thisObject, "mFlashDuringPlugged", true);
			}
		});
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
					intentfilter.addAction("com.sensetoolbox.six.USSD");
					ctx.registerReceiver(mBRUSSD, intentfilter);
				}
			});
			
			findAndHookMethod("com.android.phone.PhoneUtils", lpparam.classLoader, "displayMMIInitiate", Context.class, "com.android.internal.telephony.MmiCode", Message.class, Dialog.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Boolean hideNextUSSD = (Boolean)XposedHelpers.getAdditionalStaticField(findClass("com.android.phone.PhoneUtils", lpparam.classLoader), "hideNextUSSD");
					if (hideNextUSSD != null && hideNextUSSD) {
						Context ctx = (Context)param.args[0];
						Toast.makeText(ctx, "������� ������� USSD ������", Toast.LENGTH_SHORT).show();
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

						Object mmicode = param.args[2];
						String msg = (String)XposedHelpers.callMethod(mmicode, "getMessage");
						if (msg != null) {
							Context ctx = (Context)param.args[1];
							Toast.makeText(ctx, "����� �� ������� USSD:\n" + msg, Toast.LENGTH_LONG).show();
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
	}
}

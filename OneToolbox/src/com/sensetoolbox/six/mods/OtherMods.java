package com.sensetoolbox.six.mods;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import java.lang.reflect.Method;

import com.sensetoolbox.six.R;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
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
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class OtherMods{

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
	
	public static void exec_OldStyleToasts(final String MODULE_PATH) {
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
			if (km.inKeyguardRestrictedInputMode())
				photoHeight = modRes.getDimensionPixelSize(R.dimen.photo_new_height_ls);
			else
				photoHeight = modRes.getDimensionPixelSize(R.dimen.photo_new_height);
		
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
				}
				
				if (mPhoto != null) {
					RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)mPhoto.getLayoutParams();
					params2.height = photoHeight;
					mPhoto.setLayoutParams(params2);
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
	/*
	public static void execHook_LargePhotoLS(InitPackageResourcesParam resparam, int photoSize) {
		try {
			final XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, resparam.res);
			resparam.res.setReplacement("com.htc.lockscreen", "dimen", "masthead_minHeight", modRes.fwd(R.dimen.masthead_minHeight));
			if (photoSize == 3)
				resparam.res.setReplacement("com.htc.lockscreen", "dimen", "incoming_call_call_id_height", modRes.fwd(R.dimen.incoming_call_call_id_height));
			resparam.res.setReplacement("com.htc.lockscreen", "dimen", "text_size_custom_04", modRes.fwd(R.dimen.text_size_custom_04));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	public static void execHook_LargePhotoLSCode(LoadPackageParam lpparam, final int photoSize) {
		findAndHookMethod("com.htc.lockscreen.ui.MainContainAnimator", lpparam.classLoader, "doTileChange", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					RelativeLayout mCurTile = (RelativeLayout)XposedHelpers.getObjectField(param.thisObject, "mCurTile");
					if (mCurTile != null) {
						FrameLayout mCallPhotoContainer = (FrameLayout)mCurTile.findViewById(mCurTile.getResources().getIdentifier("call_id", "id", "com.htc.lockscreen"));
						if (mCallPhotoContainer != null) {
							RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mCurTile.getLayoutParams();
							params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
							if (photoSize == 3)
								params.setMargins(0, 0, 0, 0);
							else
								params.setMargins(0, Math.round(mCurTile.getResources().getDisplayMetrics().density * 63), 0, 0);
							mCurTile.setLayoutParams(params);
						}
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		findAndHookMethod("com.htc.lockscreen.ui.HeadBar", lpparam.classLoader, "init", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				RelativeLayout headBar = (RelativeLayout)param.thisObject;
				if (headBar != null) headBar.bringToFront();
			}
		});
		
		findAndHookMethod("com.htc.lockscreen.ui.reminder.IncomingCallView", lpparam.classLoader, "init", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					XModuleResources modRes = XModuleResources.createInstance(XMain.MODULE_PATH, null);
					
					if (photoSize == 3) {
						RelativeLayout mTile = (RelativeLayout)XposedHelpers.getObjectField(param.thisObject, "mTile");
						if (mTile != null) {
							RelativeLayout mCallPhotoRoot = (RelativeLayout)mTile.findViewById(mTile.getResources().getIdentifier("photo_view_root", "id", "com.htc.lockscreen"));
							if (mCallPhotoRoot != null) {
								LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mCallPhotoRoot.getLayoutParams();
								params.height = modRes.getDimensionPixelSize(R.dimen.incoming_call_call_id_height);
								mCallPhotoRoot.setLayoutParams(params);
							}
						}
						
						ImageView mCallPhoto = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mCallPhoto");
						if (mCallPhoto != null) {
							FrameLayout.LayoutParams params2 = (FrameLayout.LayoutParams)mCallPhoto.getLayoutParams();
							params2.height = modRes.getDimensionPixelSize(R.dimen.incoming_call_call_id_height);
							mCallPhoto.setLayoutParams(params2);
							
							FrameLayout mCallPhotoFrame = (FrameLayout)mCallPhoto.getParent();
							if (mCallPhotoFrame != null) {
								RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams)mCallPhotoFrame.getLayoutParams();
								params3.removeRule(RelativeLayout.BELOW);
								mCallPhotoFrame.setLayoutParams(params3);
							}
						}
						
						TextView mSlotName = (TextView)XposedHelpers.getObjectField(param.thisObject, "mSlotName");
						if (mSlotName != null) {
							RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams)mSlotName.getLayoutParams();
							params3.setMargins(0, Math.round(modRes.getDisplayMetrics().density * 28), 0, 0);
							mSlotName.setLayoutParams(params3);
							mSlotName.setBackground(null);
							mSlotName.setBackgroundResource(0);
							mSlotName.setGravity(Gravity.CENTER_HORIZONTAL);
							mSlotName.setShadowLayer(4.0f, 0, 3.0f, Color.argb(153, 0, 0, 0));
							mSlotName.bringToFront();
						}
					}
					
					LinearLayout mContactPanel = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mContactPanel");
					if (mContactPanel != null) {
						LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams)mContactPanel.getLayoutParams();
						if (photoSize == 3) params3.setMargins(0, 0, 0, 0);
						TextView text2 = (TextView)mContactPanel.findViewById(mContactPanel.getResources().getIdentifier("text2", "id", "com.htc.lockscreen"));
						if (text2 != null) {
							text2.setSingleLine(false);
							text2.setMaxLines(2);
						}
						mContactPanel.setLayoutParams(params3);
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}
	*/
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
}

package com.sensetoolbox.six.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htc.configuration.HtcWrapConfiguration;
import com.htc.preference.HtcListPreference;
import com.htc.preference.HtcMultiSelectListPreference;
import com.htc.preference.HtcPreference;
import com.htc.preference.HtcPreferenceActivity;
import com.htc.preference.HtcPreferenceCategory;
import com.htc.preference.HtcPreferenceGroup;
import com.htc.preference.HtcPreferenceScreen;
import com.htc.widget.HtcAlertDialog;
import com.sensetoolbox.six.MainActivity;
import com.sensetoolbox.six.PrefsFragment;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.SenseThemes.PackageTheme;
import com.sensetoolbox.six.mods.XMain;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.LruCache;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class Helpers {
	
	static List<PackageTheme> cached_pkgthm = new ArrayList<PackageTheme>();
	static String cached_str;
	public static ArrayList<AppData> installedAppsList = null;
	public static ArrayList<AppData> launchableAppsList = null;
	public static Map<String, String> l10n = null;
	public static String cLang = "";
	public static float strings_total = 614.0f;
	@SuppressLint("SdCardPath")
	public static String dataPath = "/data/data/com.sensetoolbox.six/files/";
	public static String buildVersion = "JENKINSBUILDNUMBERGOESHERE";
	public static LruCache<String, Bitmap> memoryCache = new LruCache<String, Bitmap>((int)(Runtime.getRuntime().maxMemory() / 1024) / 2) {
		@Override
		protected int sizeOf(String key, Bitmap icon) {
			if (icon != null)
				return icon.getAllocationByteCount() / 1024;
			else
				return 130 * 130 * 4 / 1024;
		}
	};
	public static List<Integer> allStyles;
	public static SparseArray<Object[]> colors = new SparseArray<Object[]>();
	public static int mFlashlightLevel = 0;
	public static WakeLock mWakeLock;

	private static synchronized boolean preloadLang(String lang) {
		try {
			if (l10n == null) {
				FileInputStream in_s = new FileInputStream(Helpers.dataPath + "values-" + lang.replace("_", "-r") + "/strings.xml");
				XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
				l10n = new HashMap<String, String>();
				parser.setInput(in_s, null);
				int eventType = parser.getEventType();
				
				while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT)
				if (eventType == XmlPullParser.START_TAG && parser.getName().equalsIgnoreCase("string"))
				l10n.put(parser.getAttributeValue(null, "name"), parser.nextText().replace("\\'", "'").replace("\\\"", "\"").replace("\\n", "\n"));
				
				cLang = lang;
			}
			return true;
		} catch (Exception e) {
			cLang = "not_found";
			//e.printStackTrace();
		}
		return false;
	}
	
	public static String l10n(Context ctx, int resId) {
		if (resId != 0)
			return l10n(ctx, ctx.getResources().getResourceEntryName(resId));
		else
			return "???";
	}
	public static String l10n(Context ctx, String resName) {
		String lang_full = Locale.getDefault().toString();
		String lang = Locale.getDefault().getLanguage();
		boolean allowFallback = true;
		if (lang_full.equals("zh_HK")) allowFallback = false;
		String newStr = null;
		if (!lang.equals("") && !lang.equals("en") && !lang_full.contains("en_") && !cLang.equals("not_found"))
		if (preloadLang(lang_full))
			newStr = l10n.get(resName);
		else if (allowFallback && preloadLang(lang))
			newStr = l10n.get(resName);
		if (newStr != null) return newStr;
		
		int resId = ctx.getResources().getIdentifier(resName, "string", ctx.getPackageName());
		if (resId != 0)
			return ctx.getResources().getString(resId);
		else
			return "???";
	}
	
	public static String xl10n(XModuleResources modRes, int resId) {
		if (resId != 0)
			return xl10n(modRes, modRes.getResourceEntryName(resId));
		else
			return "???";
	}
	public static String xl10n(XModuleResources modRes, String resName) {
		String lang_full = Locale.getDefault().toString();
		String lang = Locale.getDefault().getLanguage();
		boolean allowFallback = true;
		if (lang_full.equals("zh_HK")) allowFallback = false;
		String newStr = null;
		if (!lang.equals("") && !lang.equals("en") && !lang_full.contains("en_") && !cLang.equals("not_found"))
		if (preloadLang(lang_full))
			newStr = l10n.get(resName);
		else if (allowFallback && preloadLang(lang))
			newStr = l10n.get(resName);
		if (newStr != null) return newStr;
		
		int resId = modRes.getIdentifier(resName, "string", "com.sensetoolbox.six");
		if (resId != 0)
			return modRes.getString(resId);
		else
			return "???";
	}
	
	public static String[] xl10n_array(XModuleResources modRes, int resId) {
		TypedArray ids = modRes.obtainTypedArray(resId);
		List<String> array = new ArrayList<String>();
		for (int i = 0; i < ids.length(); i++) {
			int id = ids.getResourceId(i, 0);
			if (id != 0)
				array.add(xl10n(modRes, id));
			else
				array.add("???");
		}
		ids.recycle();
		return array.toArray(new String[array.size()]);
	}
	
	private static ArrayList<HtcPreference> getPreferenceList(HtcPreference p, ArrayList<HtcPreference> list) {
		if (p instanceof HtcPreferenceCategory || p instanceof HtcPreferenceScreen) {
			HtcPreferenceGroup pGroup = (HtcPreferenceGroup) p;
			int pCount = pGroup.getPreferenceCount();
			for (int i = 0; i < pCount; i++)
			getPreferenceList(pGroup.getPreference(i), list);
		}
		list.add(p);
		return list;
	}
	
	public static void applyLang(Activity act, HtcPreferenceFragmentExt frag) {
		ArrayList<HtcPreference> list;
		if (frag == null)
			list = getPreferenceList(((HtcPreferenceActivity)act).getPreferenceScreen(), new ArrayList<HtcPreference>());
		else
			list = getPreferenceList(frag.getPreferenceScreen(), new ArrayList<HtcPreference>());
		
		for (HtcPreference p: list) {
			int titleResId = p.getTitleRes();
			if (titleResId == 0) continue;
			p.setTitle(l10n(act, titleResId));
			
			CharSequence summ = p.getSummary();
			if (summ != null && summ != "") {
				if (titleResId == R.string.array_global_actions_launch || titleResId == R.string.array_global_actions_toggle) {
					p.setSummary(l10n(act, "notselected"));
				} else {
					String titleResName = act.getResources().getResourceEntryName(titleResId);
					String summResName = titleResName.replace("_title", "_summ");
					p.setSummary(l10n(act, summResName));
				}
			}
			
			if (p.getClass() == HtcListPreference.class || p.getClass() == HtcListPreferencePlus.class || p.getClass() == ImageListPreference.class || p.getClass() == HtcMultiSelectListPreference.class) {
				String titleResName = act.getResources().getResourceEntryName(titleResId);
				String entriesResName;
				if (titleResName.equals("controls_vol_up_media_title") || titleResName.equals("controls_vol_down_media_title"))
					entriesResName = "media_action";
				else if (titleResName.equals("controls_vol_up_cam_title") || titleResName.equals("controls_vol_down_cam_title"))
					entriesResName = "cam_actions";
				else if (titleResName.equals("various_popupnotify_clock_title"))
					entriesResName = "various_clock_style";
				else if (titleResName.equals("various_popupnotify_back_title"))
					entriesResName = "various_background_style";
				else if (titleResName.equals("controls_extendedpanel_left_title") || titleResName.equals("controls_extendedpanel_right_title"))
					entriesResName = "extendedpanel_actions";
				else if (titleResName.contains("sense_") || titleResName.contains("controls_"))
					entriesResName = "global_actions";
				else if (titleResName.contains("wakegestures_"))
					entriesResName = "wakegest_actions";
				else if (titleResId == R.string.array_global_actions_toggle)
					entriesResName = "global_toggles";
				else
					entriesResName = titleResName.replace("_title", "");
				
				int arrayId = act.getResources().getIdentifier(entriesResName, "array", act.getPackageName());
				if (arrayId != 0) {
					TypedArray ids = act.getResources().obtainTypedArray(arrayId);
					List<String> newEntries = new ArrayList<String>();
					for (int i = 0; i < ids.length(); i++) {
						int id = ids.getResourceId(i, 0);
						if (id != 0)
							newEntries.add(l10n(act, id));
						else
							newEntries.add("???");
					}
					ids.recycle();
					
					if (p.getClass() == HtcMultiSelectListPreference.class) {
						HtcMultiSelectListPreference lst = ((HtcMultiSelectListPreference)p);
						lst.setEntries(newEntries.toArray(new CharSequence[newEntries.size()]));
						lst.setDialogTitle(l10n(act, titleResId));
					} else {
						HtcListPreference lst = ((HtcListPreference)p);
						lst.setEntries(newEntries.toArray(new CharSequence[newEntries.size()]));
						lst.setDialogTitle(l10n(act, titleResId));
					}
				}
			}
		}
	}
	
	public static void openLangDialog(final Activity act) {
		HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(act);
		alert.setTitle(l10n(act, R.string.toolbox_l10n_title));
		String buildId = "?";
		int timeStamp = 0;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataPath + "version")))) {
			buildId = br.readLine();
			timeStamp = Integer.parseInt(br.readLine());
			Date datetime = new Date((long)timeStamp * 1000);
			SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss zzz", Locale.getDefault());
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			TextView center = createCenteredText(act, R.string.download_current_ver);
			
			DecimalFormatSymbols dotSep = new DecimalFormatSymbols(Locale.getDefault());
			dotSep.setDecimalSeparator('.');
			dotSep.setGroupingSeparator(' ');
			DecimalFormat percentageFormat = new DecimalFormat("0.0", dotSep);
			percentageFormat.setMinimumFractionDigits(0);
			percentageFormat.setMaximumFractionDigits(1);
			percentageFormat.setMinimumIntegerDigits(1);
			percentageFormat.setMaximumIntegerDigits(3);
			
			String l10ncount = "";
			if (l10n != null) {
				String percentage = percentageFormat.format((float)l10n.size() / strings_total * 100.0f);
				l10ncount = "\n" + l10n(act, R.string.toolbox_l10n_ready) + ": " + percentage + "%";
			} else if (cLang.equals("not_found"))
				l10ncount = "\n" + l10n(act, R.string.toolbox_l10n_ready) + ": 0%";
			
			center.setText(center.getText()  + " " + buildId + "\n" + format.format(datetime) + l10ncount);
			alert.setView(center);
		} catch (Exception e) {
			alert.setView(createCenteredText(act, R.string.download_update));
			if (!(e instanceof FileNotFoundException)) e.printStackTrace();
		}
		alert.setNegativeButton(l10n(act, R.string.sense_themes_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});
		alert.setNeutralButton(l10n(act, R.string.remove), new DialogInterface.OnClickListener() {
			void deleteRecursive(File fileOrDirectory) {
				if (fileOrDirectory.isDirectory()) for (File child: fileOrDirectory.listFiles()) deleteRecursive(child);
				fileOrDirectory.delete();
			}
			
			public void onClick(DialogInterface dialog, int whichButton) {
				File tmp = new File(dataPath);
				deleteRecursive(tmp);
				
				HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(act);
				alert.setTitle(l10n(act, R.string.success));
				alert.setView(createCenteredText(act, R.string.download_removed));
				alert.setCancelable(false);
				alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						l10n = null;
						cLang = "";
						act.recreate();
					}
				});
				alert.show();
			}
		});
		alert.setPositiveButton(l10n(act, R.string.toolbox_l10n_btn), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (act != null) {
					final DownloadAndUnZip downloadTask = new DownloadAndUnZip(act);
					downloadTask.execute("http://sensetoolbox.com/l10n/strings_sense6.zip");
				}
			}
		});
		alert.show();
	}

	public static boolean isXposedInstalled(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		boolean installed = false;
		try {
			pm.getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_ACTIVITIES);
			installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			installed = false;
		}
		return installed;
	}
	
	public static String getSenseVersion() {
		return String.valueOf(com.htc.util.phone.ProjectUtils.getSenseVersion());
	}
	
	public static TextView createCenteredText(Context ctx, int resId) {
		TextView centerMsg = new TextView(ctx);
		centerMsg.setText(l10n(ctx, resId));
		centerMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		centerMsg.setPadding(10, 60, 10, 60);
		centerMsg.setTextSize(18.0f);
		centerMsg.setTextColor(ctx.getResources().getColor(android.R.color.primary_text_light));
		return centerMsg;
	}
	
	public static void setTranslucentStatusBar(Activity act) {
		int multiply_color_id = act.getResources().getIdentifier("multiply_color", "attr", "com.htc");
		TypedValue typedValue = new TypedValue();
		act.getTheme().resolveAttribute(multiply_color_id, typedValue, true);
		int color = typedValue.data;
		
		Window actWnd = act.getWindow();
		actWnd.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		
		Drawable bkg = actWnd.getDecorView().getBackground();
		if (bkg instanceof ColorDrawable)
			((ColorDrawable)bkg).setColor(color);
		else if (bkg instanceof StateListDrawable) {
			StateListDrawable bkgState = new StateListDrawable();
			bkgState.addState(new int[] { android.R.attr.state_enabled }, new ColorDrawable(color));
			actWnd.getDecorView().setBackground(bkgState);
		}
	}
	
	public static synchronized int getCurrentTheme(Context context) {
		String current_str = context.getSharedPreferences("one_toolbox_prefs", 1).getString("pkgthm", "");
		if (!current_str.equals(cached_str)) {
			if (current_str != null && !current_str.equals("")) try {
				ObjectMapper mapper = new ObjectMapper();
				cached_pkgthm = mapper.readValue(current_str, mapper.getTypeFactory().constructCollectionType(List.class, PackageTheme.class));
			} catch (Exception e) {}
			cached_str = current_str;
		}
		
		PackageTheme ptOut = null;
		for (PackageTheme pt: cached_pkgthm) if (pt.getPkg() != null)
		if (pt.getPkg().equals("com.android.settings")) {
			ptOut = pt;
			break;
		}
		
		if (ptOut != null)
			return colors.keyAt(ptOut.getTheme());
		else
			return HtcWrapConfiguration.getHtcThemeId(context, 0);
	}
	
	public static PackageTheme getThemeForPackageFromXposed(String pkgName) {
		XMain.pref.reload();
		if (XMain.pref.getBoolean("themes_active", false)) {
			String current_str = XMain.pref.getString("pkgthm", "");
			if (!current_str.equals(XMain.xcached_str)) {
				if (current_str != null && !current_str.equals("")) try {
					XMain.xcached_pkgthm = XMain.mapper.readValue(current_str, XMain.mapper.getTypeFactory().constructCollectionType(List.class, PackageTheme.class));
				} catch (Exception e) {}
				XMain.xcached_str = current_str;
			}
			
			for (PackageTheme pt: XMain.xcached_pkgthm) if (pt.getPkg() != null)
			if (pt.getPkg().equals(pkgName) ||
				(pt.getPkg().equals("com.htc.contacts") && pkgName.equals("com.htc.htcdialer")) ||
				(pt.getPkg().equals("com.android.settings") && Arrays.asList("com.htc.htcpowermanager", "com.htc.sdm", "com.htc.home.personalize", "com.htc.widget.notification", "com.htc.sense.easyaccessservice").contains(pkgName))) {
				return pt;
			}
		}
		return null;
	}
	
	public static BitmapDrawable applySenseTheme(Context context, Drawable img) {
		int category_color_id = context.getResources().getIdentifier("category_color", "attr", "com.htc");
		int multiply_color_id = context.getResources().getIdentifier("multiply_color", "attr", "com.htc");
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(category_color_id, typedValue, true);
		int category_theme = typedValue.data;
		context.getTheme().resolveAttribute(multiply_color_id, typedValue, true);
		int multiply_theme = typedValue.data;
		
		if (context.getClass() == MainActivity.class && category_theme == multiply_theme) category_theme = 0xffdadada;
		
		Bitmap src = ((BitmapDrawable)img).getBitmap();
		Bitmap bitmap = src.copy(Bitmap.Config.ARGB_8888, true);
		return new BitmapDrawable(context.getResources(), shiftRGB(bitmap, category_theme));
	}
	
	public static Bitmap shiftRGB(Bitmap input, int reqColor) {
		int outR = Color.red(reqColor);
		int outG = Color.green(reqColor);
		int outB = Color.blue(reqColor);
		
		int w = input.getWidth();
		int h = input.getHeight();
		
		int[] pix = new int[w * h];
		input.getPixels(pix, 0, w, 0, 0, w, h);
		
		for (int i = 0; i < w * h; i++) {
			int pixColor = pix[i];
			int curR = Color.red(pixColor);
			int curG = Color.green(pixColor);
			int curB = Color.blue(pixColor);
			
			int deltaR = Math.abs(78 - curR);
			int deltaG = Math.abs(167 - curG);
			int deltaB = Math.abs(112 - curB);
			
			if (deltaR < 60 && deltaG < 60 && deltaB < 60) {
				int newR = outR - Math.round(deltaR / 3);
				int newG = outG - Math.round(deltaG / 3);
				int newB = outB - Math.round(deltaB / 3);
				
				if (newR < 0) newR = 0;
				if (newG < 0) newG = 0;
				if (newB < 0) newB = 0;
				
				pix[i] = Color.argb(Color.alpha(pixColor), newR, newG, newB);
			}
		}
		
		input.setPixels(pix, 0, w, 0, 0, w, h);
		return input;
	}
	
	public static Drawable dropIconShadow(Context mContext, Drawable icon) {
		return dropIconShadow(mContext, icon, false);
	}
	
	public static Drawable dropIconShadow(Context mContext, Drawable icon, Boolean force) {
		if (!force)
		if (PrefsFragment.prefs.getInt("pref_key_colorfilter_brightValue", 100) != 200 || PrefsFragment.prefs.getInt("pref_key_colorfilter_satValue", 100) != 0) return icon;
		
		Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
		icon.draw(canvas);
		
		float density = mContext.getResources().getDisplayMetrics().density;
		
		BlurMaskFilter blurFilter = new BlurMaskFilter(Math.round(density * 3), BlurMaskFilter.Blur.OUTER);
		Paint shadowPaint = new Paint();
		shadowPaint.setMaskFilter(blurFilter);
		
		int[] offsetXY = new int[2];
		Bitmap shadowImage = bitmap.extractAlpha(shadowPaint, offsetXY);
		
		int category_color_id = mContext.getResources().getIdentifier("category_color", "attr", "com.htc");
		TypedValue typedValue = new TypedValue();
		mContext.getTheme().resolveAttribute(category_color_id, typedValue, true);
		int category_theme = typedValue.data;
		
		Paint p = new Paint();
		ColorFilter filter = new LightingColorFilter(Color.BLACK, category_theme);
		p.setColorFilter(filter);
		
		Bitmap imageWithShadow = Bitmap.createBitmap(icon.getIntrinsicWidth() + Math.round(density * 6), icon.getIntrinsicHeight() + Math.round(density * 6), Config.ARGB_8888);
		Canvas c = new Canvas(imageWithShadow);
		c.drawBitmap(shadowImage, 0, 0, p);
		c.drawBitmap(bitmap, -offsetXY[0], -offsetXY[1], null);
		
		return new BitmapDrawable(mContext.getResources(), imageWithShadow);
	}
	
	public static boolean checkStorageReadable(Context ctx) {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) || state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(ctx);
			alert.setTitle(l10n(ctx, R.string.warning));
			alert.setView(createCenteredText(ctx, R.string.storage_unavailable));
			alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return false;
		}
	}
	
	public static boolean preparePathForBackup(Context ctx, String path) {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(ctx);
			alert.setTitle(l10n(ctx, R.string.warning));
			alert.setView(createCenteredText(ctx, R.string.storage_read_only));
			alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return false;
		} else if (state.equals(Environment.MEDIA_MOUNTED)) {
			File file = new File(path);
			if (!file.exists() && !file.mkdirs()) {
				HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(ctx);
				alert.setTitle(l10n(ctx, R.string.warning));
				alert.setView(createCenteredText(ctx, R.string.storage_cannot_mkdir));
				alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
				});
				alert.show();
				return false;
			}
			return true;
		} else {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(ctx);
			alert.setTitle(l10n(ctx, R.string.warning));
			alert.setView(createCenteredText(ctx, R.string.storage_unavailable));
			alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return false;
		}
	}
	
	public static boolean isM8() {
		return Build.DEVICE.contains("htc_m8");
	}
	
	public static boolean isE8() {
		return Build.DEVICE.contains("htc_mec");
	}
	
	public static boolean isButterflyS() {
		return Build.DEVICE.contains("dlxpul");
	}
	
	public static boolean isDesire816() {
		return Build.DEVICE.contains("htc_a5");
	}
	
	public static boolean isNotM7() {
		return (!Build.DEVICE.contains("m7")); //|| Build.DEVICE.equals("m7cdug") || Build.DEVICE.equals("m7cdwg")
	}
	
	public static boolean is443plus() {
		return (new Version(Build.VERSION.RELEASE).compareTo(new Version("4.4.3")) >= 0 ? true : false);
	}
	
	public static boolean isWakeGestures() {
		String wake_gestures = "0";
		try (BufferedReader br = new BufferedReader(new FileReader(new File("/sys/android_touch/wake_gestures")))) {
			String line = br.readLine();
			if (line != null) wake_gestures = line.trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (wake_gestures != null && wake_gestures.equals("1"));
	}
	
	public static void processResult(Activity act, int requestCode, int resultCode, Intent data) {
		if (requestCode != 7350) return;
		if (resultCode == Activity.RESULT_OK) {
			Bitmap icon = null;
			Intent.ShortcutIconResource iconResId = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
			if (iconResId != null) try {
				Context ctx = act.createPackageContext(iconResId.packageName, Context.CONTEXT_IGNORE_SECURITY);
				icon = BitmapFactory.decodeResource(ctx.getResources(), ctx.getResources().getIdentifier(iconResId.resourceName, "drawable", iconResId.packageName));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (icon == null) icon = (Bitmap)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

			if (icon != null && PrefsFragment.lastShortcutKey != null) try {
				String dir = act.getFilesDir() + "/shortcuts";
				String fileName = dir + "/" + PrefsFragment.lastShortcutKey + ".png";
				File shortcutsDir = new File(dir);
				shortcutsDir.mkdirs();
				File shortcutFileName = new File(fileName);
				try (FileOutputStream shortcutOutStream = new FileOutputStream(shortcutFileName)) {
					if (icon.compress(CompressFormat.PNG, 100, shortcutOutStream))
					PrefsFragment.prefs.edit().putString(PrefsFragment.lastShortcutKey + "_icon", shortcutFileName.getAbsolutePath()).commit();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (PrefsFragment.lastShortcutKey != null) {
				if (PrefsFragment.lastShortcutKeyContents != null) PrefsFragment.prefs.edit().putString(PrefsFragment.lastShortcutKey, PrefsFragment.lastShortcutKeyContents).commit();
				
				String shortcutName = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
				if (shortcutName != null) PrefsFragment.prefs.edit().putString(PrefsFragment.lastShortcutKey + "_name", shortcutName).commit();
				
				Intent shortcutIntent = (Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
				if (shortcutIntent != null) PrefsFragment.prefs.edit().putString(PrefsFragment.lastShortcutKey + "_intent", shortcutIntent.toUri(0)).commit();
			}
			
			if (PrefsFragment.shortcutDlg != null) PrefsFragment.shortcutDlg.dismiss();
		}
	}
	
	public static void getInstalledApps(Context ctx) {
		final PackageManager pm = ctx.getPackageManager();
		List<ApplicationInfo> packs = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		Helpers.installedAppsList = new ArrayList<AppData>();
		AppData app;
		for (Iterator<ApplicationInfo> iterator = packs.iterator(); iterator.hasNext();) try {
			ApplicationInfo applicationinfo = (ApplicationInfo)iterator.next();
			app = new AppData();
			app.label = applicationinfo.loadLabel(pm).toString();
			app.pkgName = applicationinfo.packageName;
			Helpers.installedAppsList.add(app);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(Helpers.installedAppsList, new Comparator<AppData>() {
			public int compare(AppData app1, AppData app2) {
				return app1.label.compareToIgnoreCase(app2.label);
			}
		});
	}
	
	public static void getLaunchableApps(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> packs = pm.queryIntentActivities(mainIntent, 0);
		Helpers.launchableAppsList = new ArrayList<AppData>();
		AppData app;
		for (Iterator<ResolveInfo> iterator = packs.iterator(); iterator.hasNext();) try {
			ResolveInfo resolveinfo = (ResolveInfo)iterator.next();
			app = new AppData();
			app.pkgName = resolveinfo.activityInfo.applicationInfo.packageName;
			app.actName = resolveinfo.activityInfo.name;
			if (app.actName != null)
				app.label = (String)resolveinfo.activityInfo.loadLabel(pm);
			else
				app.label = resolveinfo.loadLabel(pm).toString();
			Helpers.launchableAppsList.add(app);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(Helpers.launchableAppsList, new Comparator<AppData>() {
			public int compare(AppData app1, AppData app2) {
				return app1.label.compareToIgnoreCase(app2.label);
			}
		});
	}
	
	public static CharSequence getAppName(Context ctx, String pkgActName) {
		PackageManager pm = ctx.getPackageManager();
		String not_selected = Helpers.l10n(ctx, R.string.notselected);
		String[] pkgActArray = pkgActName.split("\\|");
		ApplicationInfo ai = null;

		if (!pkgActName.equals(not_selected))
		if (pkgActArray.length >= 1 && pkgActArray[0] != null) try {
			if (pkgActArray.length >= 2 && pkgActArray[1] != null && !pkgActArray[1].trim().equals("")) {
				return pm.getActivityInfo(new ComponentName(pkgActArray[0], pkgActArray[1]), 0).loadLabel(pm).toString();
			} else if (!pkgActArray[0].trim().equals("")) {
				ai = pm.getApplicationInfo(pkgActArray[0], 0);
				return (ai != null ? pm.getApplicationLabel(ai) : not_selected);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return not_selected;
	}
	
	public static int getStyleId(String styleName) {
		return Resources.getSystem().getIdentifier(styleName, "style", "com.htc");
	}
	
	static {
		allStyles = Arrays.asList(new Integer[] {
			// Theme 1
			getStyleId("HtcDeviceDefault"),
			getStyleId("HtcDeviceDefault.CategoryOne"),
			getStyleId("HtcDeviceDefault.CategoryTwo"),
			getStyleId("HtcDeviceDefault.CategoryThree"),
			getStyleId("HtcDeviceDefault.CategoryFour"),
			// Theme 2
			getStyleId("ThemeOne"),
			getStyleId("ThemeOne.CategoryOne"),
			getStyleId("ThemeOne.CategoryTwo"),
			getStyleId("ThemeOne.CategoryThree"),
			getStyleId("ThemeOne.CategoryFour"),
			// Theme 3
			getStyleId("ThemeTwo"),
			getStyleId("ThemeTwo.CategoryOne"),
			getStyleId("ThemeTwo.CategoryTwo"),
			getStyleId("ThemeTwo.CategoryThree"),
			getStyleId("ThemeTwo.CategoryFour"),
			// Theme 4
			getStyleId("ThemeThree"),
			getStyleId("ThemeThree.CategoryOne"),
			getStyleId("ThemeThree.CategoryTwo"),
			getStyleId("ThemeThree.CategoryThree"),
			getStyleId("ThemeThree.CategoryFour"),
		});
		
		// Theme 1
		//colors.put(allStyles.get(0), new Object[]{ "HtcDeviceDefault", 0xff252525, 0xff4ea770, 0xff141414 });
		colors.put(allStyles.get(1), new Object[]{ "HtcDeviceDefault.CategoryOne", 0xff0086cb, 0xff0086cb, 0xff4b4b4b });
		colors.put(allStyles.get(2), new Object[]{ "HtcDeviceDefault.CategoryTwo", 0xff4ea770, 0xff4ea770, 0xff4b4b4b });
		colors.put(allStyles.get(3), new Object[]{ "HtcDeviceDefault.CategoryThree", 0xffff5d3d, 0xffff5d3d, 0xff787878 });
		colors.put(allStyles.get(4), new Object[]{ "HtcDeviceDefault.CategoryFour", 0xff252525, 0xff4ea770, 0xff4ea770 });

		// Theme 2
		//colors.put(allStyles.get(5), new Object[]{ "ThemeOne", 0xff252525, 0xffff813d, 0xff141414 });
		colors.put(allStyles.get(6), new Object[]{ "ThemeOne.CategoryOne", 0xffffa63d, 0xffffa63d, 0xff4b4b4b });
		colors.put(allStyles.get(7), new Object[]{ "ThemeOne.CategoryTwo", 0xffe74457, 0xffe74457, 0xff4b4b4b });
		colors.put(allStyles.get(8), new Object[]{ "ThemeOne.CategoryThree", 0xfff64541, 0xfff64541, 0xff787878 });
		colors.put(allStyles.get(9), new Object[]{ "ThemeOne.CategoryFour", 0xff252525, 0xffff813d, 0xffff813d });
		
		// Theme 3
		//colors.put(allStyles.get(10), new Object[]{ "ThemeTwo", 0xff252525, 0xff6658cf, 0xff141414 });
		colors.put(allStyles.get(11), new Object[]{ "ThemeTwo.CategoryOne", 0xff0761B9, 0xff0761b9, 0xff4b4b4b });
		colors.put(allStyles.get(12), new Object[]{ "ThemeTwo.CategoryTwo", 0xff07B7B9, 0xff07b7b9, 0xff4b4b4b });
		colors.put(allStyles.get(13), new Object[]{ "ThemeTwo.CategoryThree", 0xffA325A3, 0xffa325a3, 0xff787878 });
		colors.put(allStyles.get(14), new Object[]{ "ThemeTwo.CategoryFour", 0xff252525, 0xff6658cf, 0xff6658cf });

		// Theme 4
		//colors.put(allStyles.get(15), new Object[]{ "ThemeThree", 0xff252525, 0xff4ea770, 0xff141414 });
		//colors.put(allStyles.get(16), new Object[]{ "ThemeThree.CategoryOne", 0xff252525, 0xff4ea770, 0xff4b4b4b });
		//colors.put(allStyles.get(17), new Object[]{ "ThemeThree.CategoryTwo", 0xff252525, 0xff4ea770, 0xff4b4b4b });
		//colors.put(allStyles.get(18), new Object[]{ "ThemeThree.CategoryThree", 0xff252525, 0xff4ea770, 0xff787878 });
		//colors.put(allStyles.get(19), new Object[]{ "ThemeThree.CategoryFour", 0xff252525, 0xff4ea770, 0xff252525 });
	}
	
	public static ArrayList<View> getChildViewsRecursive(View view) {
		if (view instanceof ViewGroup) {
			ArrayList<View> list2 = new ArrayList<View>();
			ViewGroup viewgroup = (ViewGroup)view;
			int i = 0;
			do {
				if (i >= viewgroup.getChildCount()) return list2;
				View view1 = viewgroup.getChildAt(i);
				ArrayList<View> list3 = new ArrayList<View>();
				list3.add(view);
				list3.addAll(getChildViewsRecursive(view1));
				list2.addAll(list3);
				i++;
			} while (true);
		} else {
			ArrayList<View> list1 = new ArrayList<View>();
			list1.add(view);
			return list1;
		}
	}
	
	public static Map<String,String> getParamsAsStringString(Map<?, ?> parameters) throws UnsupportedEncodingException {
		HashMap<String,String> result = new HashMap<String,String>();
		for (final Object key : parameters.keySet()) {
			final Object preliminaryValue = parameters.get(key);
			final Object value = (preliminaryValue == null) ? "" : preliminaryValue;
			result.put(key.toString(), value.toString());
		}
		return result;
	}
	
	public static boolean getHTCHaptic(Context ctx) {
		Boolean isHapticAllowed = true;
		try {
			boolean powersaver = (Settings.System.getInt(ctx.getContentResolver(), "user_powersaver_enable", 0) == 1);
			boolean haptic = (Settings.Secure.getInt(ctx.getContentResolver(), "powersaver_haptic_feedback", 1) == 1);
			if (powersaver && haptic) isHapticAllowed = false;
		} catch (Exception e) {}
		return isHapticAllowed;
	}
	
	public static String getNextAlarm(Context ctx) {
		if (ctx != null)
			return Settings.System.getString(ctx.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
		else
			return null;
	}
	
	public static long getNextAlarmTime(Context ctx) {
		if (ctx != null)
			return Settings.System.getLong(ctx.getContentResolver(), "next_alarm_time", -1);
		else
			return -1;
	}
}

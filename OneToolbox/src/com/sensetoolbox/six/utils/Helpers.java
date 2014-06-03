package com.sensetoolbox.six.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.htc.configuration.HtcWrapConfiguration;
import com.htc.gson.Gson;
import com.htc.gson.reflect.TypeToken;
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
import com.sensetoolbox.six.SenseThemes;
import com.sensetoolbox.six.SenseThemes.PackageTheme;
import com.sensetoolbox.six.mods.XMain;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class Helpers {
	
	static List<PackageTheme> cached_pkgthm = null;
	static String cached_str;
	public static Map<String, String> l10n = null;
	public static String cLang = "";
	@SuppressLint("SdCardPath")
	public static String dataPath = "/data/data/com.sensetoolbox.six/files/";
	public static String buildVersion = "JENKINSBUILDNUMBERGOESHERE";

	private static boolean preloadLang(String lang) {
		try {
			if (l10n == null) {
				FileInputStream in_s = new FileInputStream(Helpers.dataPath + "values-" + lang + "/strings.xml");
				XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
				l10n = new HashMap<String, String>();
				parser.setInput(in_s, null);
				int eventType = parser.getEventType();
				
				while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT)
				if (eventType == XmlPullParser.START_TAG)
				if (parser.getName().equalsIgnoreCase("string"))
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
	
	public static void applyLang(HtcPreferenceActivity act, HtcPreferenceFragmentExt frag) {
		ArrayList<HtcPreference> list;
		if (frag == null)
			list = getPreferenceList(act.getPreferenceScreen(), new ArrayList<HtcPreference>());
		else
			list = getPreferenceList(frag.getPreferenceScreen(), new ArrayList<HtcPreference>());
		
		for (HtcPreference p: list) {
			int titleResId = p.getTitleRes();
			if (titleResId != 0) p.setTitle(l10n(act, titleResId));
			
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
		centerMsg.setPadding(0, 60, 0, 60);
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
	
	public static int getCurrentTheme(Context context) {
		String current_str = context.getSharedPreferences("one_toolbox_prefs", 1).getString("pkgthm", "");
		if (cached_pkgthm == null || !current_str.equals(cached_str)) {
			if (current_str != null && !current_str.equals(""))
				cached_pkgthm = new Gson().fromJson(current_str, new TypeToken<ArrayList<PackageTheme>>(){}.getType());
			else
				cached_pkgthm = new ArrayList<PackageTheme>();
			cached_str = current_str;
		}
		
		PackageTheme ptOut = null;
		for (PackageTheme pt: cached_pkgthm) if (pt.getPkg() != null)
		if (pt.getPkg().equals("com.android.settings")) {
			ptOut = pt;
			break;
		}
		
		if (ptOut != null)
			return SenseThemes.getColors().keyAt(ptOut.getTheme());
		else
			return HtcWrapConfiguration.getHtcThemeId(context, 0);
	}
	
	public static PackageTheme getThemeForPackageFromXposed(String pkgName) {
		XMain.pref.reload();
		if (XMain.pref.getBoolean("themes_active", false)) {			
			String tmp = XMain.pref.getString("pkgthm", null);
			
			List<PackageTheme> pkgthm;
			if (tmp != null)
				pkgthm = new Gson().fromJson(tmp, new TypeToken<ArrayList<PackageTheme>>(){}.getType());
			else
				pkgthm = new ArrayList<PackageTheme>();
			
			for (PackageTheme pt: pkgthm) if (pt.getPkg() != null)
				if (pt.getPkg().equals(pkgName) ||
						(pt.getPkg().equals("com.htc.contacts") && pkgName.equals("com.htc.htcdialer")) ||
						(pt.getPkg().equals("com.android.settings") && (pkgName.equals("com.htc.htcpowermanager") || pkgName.equals("com.htc.sdm") || pkgName.equals("com.htc.home.personalize") || pkgName.equals("com.htc.widget.notification") || pkgName.equals("com.htc.sense.easyaccessservice")))) {
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
	
	public static boolean isNotM7() {
		return !Build.DEVICE.contains("m7");
	}
	
	public static boolean isWakeGestures() {
		String wake_gestures = "0";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("/sys/android_touch/wake_gestures")));
			wake_gestures = br.readLine().trim();
			br.close();
		} catch (Exception e) {}
		if (wake_gestures != null && wake_gestures.equals("1")) return true; else return false; 
	}
	
	public static void processResult(Activity act, int requestCode, int resultCode, Intent data) {
		if (requestCode != 7350) return;
		if (resultCode == Activity.RESULT_OK) {
			Bitmap icon = null;
			Intent.ShortcutIconResource iconResId = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
			if (iconResId != null) try {
				Context ctx = act.createPackageContext(iconResId.packageName, Context.CONTEXT_IGNORE_SECURITY);
				icon = BitmapFactory.decodeResource(ctx.getResources(), ctx.getResources().getIdentifier(iconResId.resourceName, "drawable", iconResId.packageName));
			} catch (Exception e) {}
			if (icon == null) icon = (Bitmap)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

			if (icon != null && PrefsFragment.lastShortcutKey != null) try {
				String dir = act.getFilesDir() + "/shortcuts";
				String fileName = dir + "/" + PrefsFragment.lastShortcutKey + ".png";
				File shortcutsDir = new File(dir);
				shortcutsDir.mkdirs();
				File shortcutFileName = new File(fileName);
				FileOutputStream shortcutOutStream = new FileOutputStream(shortcutFileName);

				if (icon.compress(CompressFormat.PNG, 100, shortcutOutStream))
				PrefsFragment.prefs.edit().putString(PrefsFragment.lastShortcutKey + "_icon", shortcutFileName.getAbsolutePath()).commit();
				
				shortcutOutStream.close();
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
}

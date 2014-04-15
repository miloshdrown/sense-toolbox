package com.sensetoolbox.six.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.htc.configuration.HtcWrapConfiguration;
import com.htc.gson.Gson;
import com.htc.gson.reflect.TypeToken;
import com.htc.widget.HtcAlertDialog;
import com.sensetoolbox.six.MainActivity;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.SenseThemes;
import com.sensetoolbox.six.SenseThemes.PackageTheme;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Environment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class Helpers {
	
	static DocumentBuilderFactory dbf;
	static DocumentBuilder db;
	static Document doc;
	static Element eQS;
	static List<PackageTheme> cached_pkgthm = null;
	// static String cached_str = null;

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
		centerMsg.setText(resId);
		centerMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		centerMsg.setPadding(0, 60, 0, 60);
		centerMsg.setTextSize(18.0f);
		centerMsg.setTextColor(Color.DKGRAY);
		return centerMsg; 
	}
	
	public static void setTranslucentStatusBar(Activity act) {
		int category_color_id = act.getResources().getIdentifier("multiply_color", "attr", "com.htc");
		TypedValue typedValue = new TypedValue();
		act.getTheme().resolveAttribute(category_color_id, typedValue, true);
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
		if (cached_pkgthm == null) {
			if (current_str != null && !current_str.equals(""))
				cached_pkgthm = new Gson().fromJson(current_str, new TypeToken<ArrayList<PackageTheme>>(){}.getType());
			else
				cached_pkgthm = new ArrayList<PackageTheme>();
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
	
	public static boolean checkStorageReadable(Context ctx) {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) || state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(ctx);
			alert.setTitle(R.string.warning);
			alert.setView(Helpers.createCenteredText(ctx, R.string.storage_unavailable));
			alert.setNeutralButton(ctx.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
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
			alert.setTitle(R.string.warning);
			alert.setView(Helpers.createCenteredText(ctx, R.string.storage_read_only));
			alert.setNeutralButton(ctx.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return false;
		} else if (state.equals(Environment.MEDIA_MOUNTED)) {
			File file = new File(path);
			if (!file.exists() && !file.mkdirs()) {
	        	HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(ctx);
				alert.setTitle(R.string.warning);
				alert.setView(Helpers.createCenteredText(ctx, R.string.storage_cannot_mkdir));
				alert.setNeutralButton(ctx.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
				});
				alert.show();
				return false;
		    }
			return true;
		} else {
			HtcAlertDialog.Builder alert = new HtcAlertDialog.Builder(ctx);
			alert.setTitle(R.string.warning);
			alert.setView(Helpers.createCenteredText(ctx, R.string.storage_unavailable));
			alert.setNeutralButton(ctx.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
			return false;
		}
	}
}

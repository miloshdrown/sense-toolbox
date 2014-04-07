package com.sensetoolbox.six.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

public class Helpers {
	
	static DocumentBuilderFactory dbf;
	static DocumentBuilder db;
	static Document doc;
	static Element eQS;

	public Helpers() {
		// TODO Auto-generated constructor stub
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
		centerMsg.setText(resId);
		centerMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		centerMsg.setPadding(0, 60, 0, 60);
		centerMsg.setTextSize(18.0f);
		centerMsg.setTextColor(Color.DKGRAY);
		return centerMsg; 
	}
	
	public static BitmapDrawable applySenseTheme(Context context, Drawable img) {
		int category_color_id = context.getResources().getIdentifier("category_color", "attr", "com.htc");
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(category_color_id, typedValue, true);
		int color_theme = typedValue.data;

		Bitmap src = ((BitmapDrawable)img).getBitmap();
		Bitmap bitmap = src.copy(Bitmap.Config.ARGB_8888, true);
		return new BitmapDrawable(context.getResources(), shiftRGB(bitmap, color_theme));
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
}

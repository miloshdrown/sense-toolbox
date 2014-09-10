package com.sensetoolbox.six.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlend;
import android.renderscript.ScriptIntrinsicBlur;

public class BlurBuilder {
	private static float BITMAP_SCALE = 0.15f;
	private static float BLUR_RADIUS = 13f;

	public static Bitmap blur(Context ctx, Bitmap bmp, boolean dim) {
		if (dim) {
			BITMAP_SCALE = 0.20f;
			BLUR_RADIUS = 10f;
		}
		
		int width = Math.round(bmp.getWidth() * BITMAP_SCALE);
		int height = Math.round(bmp.getHeight() * BITMAP_SCALE);
		Bitmap newBitmap = Bitmap.createScaledBitmap(bmp, width, height, true);

		RenderScript rs = RenderScript.create(ctx);
		ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
		Allocation tmpIn = Allocation.createFromBitmap(rs, newBitmap);
		Allocation tmpOut = Allocation.createTyped(rs, tmpIn.getType());
		blurScript.setRadius(BLUR_RADIUS);
		blurScript.setInput(tmpIn);
		blurScript.forEach(tmpOut);
		tmpOut.copyTo(newBitmap);
		
		if (dim) {
			Bitmap blackBitmap = newBitmap.copy(Bitmap.Config.ARGB_8888, true);
			blackBitmap.eraseColor(Color.argb(255, 150, 150, 150));
			ScriptIntrinsicBlend dimScript = ScriptIntrinsicBlend.create(rs, Element.U8_4(rs));
			
			Allocation tmpInBlack = Allocation.createFromBitmap(rs, blackBitmap);
			//Allocation tmpOutDimmed = Allocation.createFromBitmap(rs, newBitmap);
			dimScript.forEachMultiply(tmpInBlack, tmpOut);
			tmpOut.copyTo(newBitmap);
			blackBitmap.recycle();
		}

		Bitmap stretchedBitmap = Bitmap.createScaledBitmap(newBitmap, bmp.getWidth(), bmp.getHeight(), true);
		bmp.recycle();
		newBitmap.recycle();
		
		return stretchedBitmap;
	}
}
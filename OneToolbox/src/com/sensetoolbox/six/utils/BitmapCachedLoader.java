package com.sensetoolbox.six.utils;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.htc.widget.HtcListItemColorIcon;
import com.htc.widget.HtcListItemTileImage;

public class BitmapCachedLoader extends AsyncTask<Void, Void, Bitmap> {
	private final WeakReference<Object> targetRef;
	private final WeakReference<Object> appInfo;
	private final int type;
	private final Context ctx;
	
	public BitmapCachedLoader(int t, Object target, Object info, Context context) {
		targetRef = new WeakReference<Object>(target);
		appInfo = new WeakReference<Object>(info);
		type = t;
		ctx = context;
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		Bitmap bmp = null;
		Drawable icon = null;
		String pkgName = null;
		int newIconSize = Math.round(ctx.getResources().getDisplayMetrics().density * 45.0f);
		if (type == 1) {
			ApplicationInfo ai = ((ApplicationInfo)appInfo.get());
			icon = ai.loadIcon(ctx.getPackageManager());
			pkgName = ai.packageName;
		} else if (type == 2) {
			ResolveInfo ai = ((ResolveInfo)appInfo.get());
			icon = ai.loadIcon(ctx.getPackageManager());
			pkgName = ai.activityInfo.packageName;
		}
		
		if (pkgName != null && icon != null && BitmapDrawable.class.isInstance(icon)) {
			bmp = ((BitmapDrawable)icon).getBitmap();
			Matrix matrix = new Matrix();
			matrix.postScale(((float)newIconSize) / bmp.getWidth(), ((float)newIconSize) / bmp.getHeight());
			bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
			
			if (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() > 8 * 1024 * 1024)
				Helpers.memoryCache.put(pkgName, bmp);
			else
				Runtime.getRuntime().gc();
		}
		
		return bmp;
	}
	
	@Override
	protected void onPostExecute(Bitmap bmp) {
		if (targetRef != null && bmp != null)
		if (type == 1) {
			HtcListItemColorIcon itemIcon = ((HtcListItemColorIcon)targetRef.get());
			if (itemIcon != null) itemIcon.setColorIconImageBitmap(bmp);
		} else if (type == 2) {
			HtcListItemTileImage itemIcon = ((HtcListItemTileImage)targetRef.get());
			if (itemIcon != null) itemIcon.setTileImageBitmap(bmp);
		}
	}
}

package com.sensetoolbox.six.htc.utils;

import com.sensetoolbox.six.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ColorFrameLayout extends FrameLayout {
	Bitmap sel = BitmapFactory.decodeResource(getResources(), R.drawable.theme_selected);
	Bitmap sel_bf = Bitmap.createScaledBitmap(sel, Math.round(sel.getWidth() / 1.3f), Math.round(sel.getHeight() / 1.3f), false);
	Paint pnt = new Paint();
	
	public ColorFrameLayout(Context context) {
		super(context);
	}
	
	public ColorFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ColorFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		float density = getResources().getDisplayMetrics().density;
		if (this.getTag() != null && (boolean)this.getTag())
		canvas.drawBitmap(sel_bf, Math.round(canvas.getWidth() - sel_bf.getWidth() - density), Math.round(canvas.getHeight() - sel_bf.getHeight() - 5 * density), pnt);
	}
}
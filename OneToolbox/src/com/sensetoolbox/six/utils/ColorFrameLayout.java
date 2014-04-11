package com.sensetoolbox.six.utils;

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
		if ((Boolean)this.getTag())
		canvas.drawBitmap(sel, Math.round(canvas.getWidth() - sel.getWidth() - 6 * density), Math.round(canvas.getHeight() - sel.getHeight() - 12 * density), pnt);
	}
}
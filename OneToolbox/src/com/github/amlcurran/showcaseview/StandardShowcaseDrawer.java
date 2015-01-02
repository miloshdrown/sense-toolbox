/*
 * Copyright 2014 Alex Curran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.amlcurran.showcaseview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;

class StandardShowcaseDrawer implements ShowcaseDrawer {

	protected final Paint eraserPaint;
	protected final Drawable showcaseDrawable = null;
	private final Paint basicPaint;
	protected int backgroundColour;

	public StandardShowcaseDrawer(Resources resources) {
		PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);
		eraserPaint = new Paint();
		eraserPaint.setColor(0xFFFFFF);
		eraserPaint.setAlpha(0);
		eraserPaint.setXfermode(xfermode);
		eraserPaint.setAntiAlias(true);
		basicPaint = new Paint();
	}

	@Override
	public void setShowcaseColour(int color) {
	}

	@Override
	public void drawShowcase(Bitmap buffer, float x, float y, float scaleMultiplier) {
	}

	@Override
	public int getShowcaseWidth() {
		return showcaseDrawable.getIntrinsicWidth();
	}

	@Override
	public int getShowcaseHeight() {
		return showcaseDrawable.getIntrinsicHeight();
	}

	@Override
	public float getBlockedRadius() {
		return 0;
	}

	@Override
	public void setBackgroundColour(int backgroundColor) {
		this.backgroundColour = backgroundColor;
	}

	@Override
	public void erase(Bitmap bitmapBuffer) {
		bitmapBuffer.eraseColor(backgroundColour);
	}

	@Override
	public void drawToCanvas(Canvas canvas, Bitmap bitmapBuffer) {
		canvas.drawBitmap(bitmapBuffer, 0, 0, basicPaint);
	}

}

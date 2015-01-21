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

import com.sensetoolbox.six.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * Created by curraa01 on 13/10/2013.
 */
class NewShowcaseDrawer extends StandardShowcaseDrawer {

	private static final int ALPHA_60_PERCENT = 153;
	private float outerX;
	private float innerX;
	private float outerY;
	private float innerY;

	public NewShowcaseDrawer(Resources resources, int newStyle) {
		super(resources);
		
		if (newStyle == 2) {
			outerX = resources.getDisplayMetrics().widthPixels + 20;
			innerX = resources.getDisplayMetrics().widthPixels;
			outerY = resources.getDimension(R.dimen.showcase_y2_outer);
			innerY = resources.getDimension(R.dimen.showcase_y2_inner);
		} else {
			outerX = resources.getDimension(R.dimen.showcase_x_outer);
			innerX = resources.getDimension(R.dimen.showcase_x_inner);
			outerY = resources.getDimension(R.dimen.showcase_y_outer);
			innerY = resources.getDimension(R.dimen.showcase_y_inner);
		}
	}

	@Override
	public void setShowcaseColour(int color) {
		eraserPaint.setColor(color);
	}

	@Override
	public void drawShowcase(Bitmap buffer, float x, float y, float scaleMultiplier) {
		Canvas bufferCanvas = new Canvas(buffer);
		eraserPaint.setAlpha(ALPHA_60_PERCENT);
		bufferCanvas.drawRoundRect(new RectF(x - outerX/2f, y - outerY/2f, x + outerX/2f , y + outerY/2f), 25, 25, eraserPaint);
		eraserPaint.setAlpha(0);
		bufferCanvas.drawRoundRect(new RectF(x - innerX/2f, y - innerY/2f, x + innerX/2f , y + innerY/2f), 25, 25, eraserPaint);
		bufferCanvas = null;
	}

	@Override
	public int getShowcaseWidth() {
		return (int)outerX;
	}

	@Override
	public int getShowcaseHeight() {
		return 5;
	}

	@Override
	public float getBlockedRadius() {
		return innerY;
	}

	@Override
	public void setBackgroundColour(int backgroundColor) {
		this.backgroundColour = backgroundColor;
	}
}

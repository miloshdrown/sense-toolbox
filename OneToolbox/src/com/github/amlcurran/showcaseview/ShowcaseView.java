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

import java.lang.ref.SoftReference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.targets.Target;
import com.htc.widget.HtcRimButton;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

import static com.github.amlcurran.showcaseview.AnimationFactory.AnimationEndListener;
import static com.github.amlcurran.showcaseview.AnimationFactory.AnimationStartListener;

/**
 * A view which allows you to showcase areas of your app with an explanation.
 */
public class ShowcaseView extends RelativeLayout
		implements View.OnTouchListener, ShowcaseViewApi {

	private static final int HOLO_BLUE = Color.parseColor("#33B5E5");
	
	private static Activity activity = null;
	private final HtcRimButton mEndButton;
	private final HtcRimButton mTranslateButton;
	private final TextDrawer textDrawer;
	private final ShowcaseDrawer showcaseDrawer;
	private final ShowcaseAreaCalculator showcaseAreaCalculator;
	private final AnimationFactory animationFactory;
	private final ShotStateStore shotStateStore;

	// Showcase metrics
	private int showcaseX = -1;
	private int showcaseY = -1;
	private float scaleMultiplier = 1f;

	// Touch items
	private boolean hasCustomClickListener = false;
	private boolean blockTouches = true;
	private boolean hideOnTouch = false;
	private OnShowcaseEventListener mEventListener = OnShowcaseEventListener.NONE;

	private boolean hasAlteredText = false;
	private boolean hasNoTarget = false;
	private boolean shouldCentreText;
	private SoftReference<Bitmap> bitmapBuffer;

	// Animation items
	private long fadeInMillis;
	private long fadeOutMillis;
	private boolean isShowing;

	protected ShowcaseView(Context context, int newStyle) {
		this(context, null, R.styleable.CustomTheme_showcaseViewStyle, newStyle);
	}

	@SuppressLint("InflateParams")
	protected ShowcaseView(Context context, AttributeSet attrs, int defStyle, int newStyle) {
		super(context, attrs, defStyle);

		ApiUtils apiUtils = new ApiUtils();
		animationFactory = new AnimatorAnimationFactory();
		showcaseAreaCalculator = new ShowcaseAreaCalculator();
		shotStateStore = new ShotStateStore(context);

		apiUtils.setFitsSystemWindowsCompat(this);
		getViewTreeObserver().addOnPreDrawListener(new CalculateTextOnPreDraw());
		getViewTreeObserver().addOnGlobalLayoutListener(new UpdateOnGlobalLayout());

		// Get the attributes for the ShowcaseView
		final TypedArray styled = context.getTheme()
				.obtainStyledAttributes(attrs, R.styleable.ShowcaseView, R.attr.showcaseViewStyle,
						R.style.ShowcaseView);

		// Set the default animation times
		fadeInMillis = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		fadeOutMillis = getResources().getInteger(android.R.integer.config_mediumAnimTime);

		mEndButton = (HtcRimButton) LayoutInflater.from(context).inflate(R.layout.showcase_button, null);
		mTranslateButton = (HtcRimButton) LayoutInflater.from(context).inflate(R.layout.showcase_button, null);
		showcaseDrawer = new NewShowcaseDrawer(getResources(), newStyle);
		textDrawer = new TextDrawer(getResources(), showcaseAreaCalculator, getContext());

		updateStyle(styled, false);

		init();
	}

	@SuppressLint("ClickableViewAccessibility")
	private void init() {

		setOnTouchListener(this);

		if (mEndButton.getParent() == null) {
			int margin = (int) getResources().getDimension(R.dimen.button_margin);
			RelativeLayout.LayoutParams lps = (LayoutParams) generateDefaultLayoutParams();
			lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			lps.setMargins(margin, margin, margin, margin);
			mEndButton.setLayoutParams(lps);
			mEndButton.setText(android.R.string.ok);
			if (!hasCustomClickListener) {
				mEndButton.setOnClickListener(hideOnClickListener);
			}
			addView(mEndButton);
			
			RelativeLayout.LayoutParams lps2 = (LayoutParams) generateDefaultLayoutParams();
			lps2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lps2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lps2.setMargins(margin, margin, margin, margin);
			mTranslateButton.setLayoutParams(lps2);
			mTranslateButton.setText(Helpers.l10n(this.getContext(), R.string.about_l10n));
			mTranslateButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Helpers.openLangDialog(activity);
				}
			});
			addView(mTranslateButton);
		}

	}

	private boolean hasShot() {
		return shotStateStore.hasShot();
	}

	void setShowcasePosition(Point point) {
		setShowcasePosition(point.x, point.y);
	}

	void setShowcasePosition(int x, int y) {
		if (shotStateStore.hasShot()) {
			return;
		}
		showcaseX = x;
		showcaseY = y;
		//init();
		invalidate();
	}

	public void setTarget(final Target target) {
		setShowcase(target, false);
	}

	public void setShowcase(final Target target, final boolean animate) {
		postDelayed(new Runnable() {
			@Override
			public void run() {

				if (!shotStateStore.hasShot()) {

					updateBitmap();
					Point targetPoint = target.getPoint();
					if (targetPoint != null) {
						hasNoTarget = false;
						if (animate) {
							animationFactory.animateTargetToPoint(ShowcaseView.this, targetPoint);
						} else {
							setShowcasePosition(targetPoint);
						}
					} else {
						hasNoTarget = true;
						invalidate();
					}

				}
			}
		}, 100);
	}

	private void updateBitmap() {
		if (bitmapBuffer == null || (bitmapBuffer.get() != null && haveBoundsChanged())) {
			if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0)
			bitmapBuffer = new SoftReference<Bitmap>(Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888));
		}
	}

	private boolean haveBoundsChanged() {
		return getMeasuredWidth() != bitmapBuffer.get().getWidth() || getMeasuredHeight() != bitmapBuffer.get().getHeight();
	}

	public boolean hasShowcaseView() {
		return (showcaseX != 1000000 && showcaseY != 1000000) && !hasNoTarget;
	}

	public void setShowcaseX(int x) {
		setShowcasePosition(x, showcaseY);
	}

	public void setShowcaseY(int y) {
		setShowcasePosition(showcaseX, y);
	}

	public int getShowcaseX() {
		return showcaseX;
	}

	public int getShowcaseY() {
		return showcaseY;
	}

	/**
	 * Override the standard button click event
	 *
	 * @param listener Listener to listen to on click events
	 */
	public void overrideButtonClick(OnClickListener listener) {
		if (shotStateStore.hasShot()) {
			return;
		}
		if (mEndButton != null) {
			if (listener != null) {
				mEndButton.setOnClickListener(listener);
			} else {
				mEndButton.setOnClickListener(hideOnClickListener);
			}
		}
		hasCustomClickListener = true;
	}

	public void setOnShowcaseEventListener(OnShowcaseEventListener listener) {
		if (listener != null) {
			mEventListener = listener;
		} else {
			mEventListener = OnShowcaseEventListener.NONE;
		}
	}

	public void setButtonText(CharSequence text) {
		if (mEndButton != null) {
			mEndButton.setText(text);
		}
	}

	private void recalculateText() {
		boolean recalculatedCling = showcaseAreaCalculator.calculateShowcaseRect(showcaseX, showcaseY, showcaseDrawer);
		boolean recalculateText = recalculatedCling || hasAlteredText;
		if (recalculateText) {
			textDrawer.calculateTextPosition(getMeasuredWidth(), getMeasuredHeight(), this, shouldCentreText);
		}
		hasAlteredText = false;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (showcaseX < 0 || showcaseY < 0 || shotStateStore.hasShot() || bitmapBuffer == null || bitmapBuffer.get() == null) {
			super.dispatchDraw(canvas);
			return;
		}

		//Draw background color
		showcaseDrawer.erase(bitmapBuffer.get());

		// Draw the showcase drawable
		if (!hasNoTarget) {
			showcaseDrawer.drawShowcase(bitmapBuffer.get(), showcaseX, showcaseY, scaleMultiplier);
			showcaseDrawer.drawToCanvas(canvas, bitmapBuffer.get());
		}

		// Draw the text on the screen, recalculating its position if necessary
		textDrawer.draw(canvas);

		super.dispatchDraw(canvas);

	}

	@Override
	public void hide() {
		// If the type is set to one-shot, store that it has shot
		shotStateStore.storeShot();
		mEventListener.onShowcaseViewHide(this);
		fadeOutShowcase();
		System.gc();
	}
	
	public void close() {
		hideImmediate();
		System.gc();
	}

	private void fadeOutShowcase() {
		animationFactory.fadeOutView(this, fadeOutMillis, new AnimationEndListener() {
			@Override
			public void onAnimationEnd() {
				setVisibility(View.GONE);
				isShowing = false;
				mEventListener.onShowcaseViewDidHide(ShowcaseView.this);
			}
		});
	}

	@Override
	public void show() {
		isShowing = true;
		mEventListener.onShowcaseViewShow(this);
		fadeInShowcase();
	}

	private void fadeInShowcase() {
		animationFactory.fadeInView(this, fadeInMillis,
				new AnimationStartListener() {
					@Override
					public void onAnimationStart() {
						setVisibility(View.VISIBLE);
					}
				}
		);
	}

	@Override
	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouch(View view, MotionEvent motionEvent) {

		float xDelta = Math.abs(motionEvent.getRawX() - showcaseX);
		float yDelta = Math.abs(motionEvent.getRawY() - showcaseY);
		double distanceFromFocus = Math.sqrt(Math.pow(xDelta, 2) + Math.pow(yDelta, 2));

		if (MotionEvent.ACTION_UP == motionEvent.getAction() &&
				hideOnTouch && distanceFromFocus > showcaseDrawer.getBlockedRadius()) {
			this.hide();
			return true;
		}

		return blockTouches && distanceFromFocus > showcaseDrawer.getBlockedRadius();
	}

	private static void insertShowcaseView(ShowcaseView showcaseView, Activity act) {
		((ViewGroup) act.getWindow().getDecorView()).addView(showcaseView);
		if (!showcaseView.hasShot()) {
			showcaseView.show();
		} else {
			showcaseView.hideImmediate();
		}
	}

	private void hideImmediate() {
		isShowing = false;
		setVisibility(GONE);
	}

	@Override
	public void setContentTitle(CharSequence title) {
		textDrawer.setContentTitle(title);
	}

	@Override
	public void setContentText(CharSequence text) {
		textDrawer.setContentText(text);
	}
/*
	private void setScaleMultiplier(float sM) {
		this.scaleMultiplier = sM;
	}
*/
	public void hideButton() {
		mEndButton.setVisibility(GONE);
	}

	public void showButton() {
		mEndButton.setVisibility(VISIBLE);
	}

	/**
	 * Builder class which allows easier creation of {@link ShowcaseView}s.
	 * It is recommended that you use this Builder class.
	 */
	public static class Builder {

		final ShowcaseView showcaseView;

		public Builder(Activity act) {
			this(act, 0);
		}

		public Builder(Activity act, int useNewStyle) {
			activity = act;
			this.showcaseView = new ShowcaseView(act, useNewStyle);
			this.showcaseView.setTarget(Target.NONE);
		}

		/**
		 * Create the {@link com.github.amlcurran.showcaseview.ShowcaseView} and show it.
		 *
		 * @return the created ShowcaseView
		 */
		public ShowcaseView build() {
			insertShowcaseView(showcaseView, activity);
			return showcaseView;
		}

		/**
		 * Set the title text shown on the ShowcaseView.
		 */
		public Builder setContentTitle(int resId) {
			return setContentTitle(activity.getString(resId));
		}

		/**
		 * Set the title text shown on the ShowcaseView.
		 */
		public Builder setContentTitle(CharSequence title) {
			showcaseView.setContentTitle(title);
			return this;
		}

		/**
		 * Set the descriptive text shown on the ShowcaseView.
		 */
		public Builder setContentText(int resId) {
			return setContentText(activity.getString(resId));
		}

		/**
		 * Set the descriptive text shown on the ShowcaseView.
		 */
		public Builder setContentText(CharSequence text) {
			showcaseView.setContentText(text);
			return this;
		}

		/**
		 * Set the target of the showcase.
		 *
		 * @param target a {@link com.github.amlcurran.showcaseview.targets.Target} representing
		 *               the item to showcase (e.g., a button, or action item).
		 */
		public Builder setTarget(Target target) {
			showcaseView.setTarget(target);
			return this;
		}

		/**
		 * Set the style of the ShowcaseView. See the sample app for example styles.
		 */
		public Builder setStyle(int theme) {
			showcaseView.setStyle(theme);
			return this;
		}

		/**
		 * Set a listener which will override the button clicks.
		 * <p/>
		 * Note that you will have to manually hide the ShowcaseView
		 */
		public Builder setOnClickListener(OnClickListener onClickListener) {
			showcaseView.overrideButtonClick(onClickListener);
			return this;
		}

		/**
		 * Don't make the ShowcaseView block touches on itself. This doesn't
		 * block touches in the showcased area.
		 * <p/>
		 * By default, the ShowcaseView does block touches
		 */
		public Builder doNotBlockTouches() {
			showcaseView.setBlocksTouches(false);
			return this;
		}

		/**
		 * Make this ShowcaseView hide when the user touches outside the showcased area.
		 * This enables {@link #doNotBlockTouches()} as well.
		 * <p/>
		 * By default, the ShowcaseView doesn't hide on touch.
		 */
		public Builder hideOnTouchOutside() {
			showcaseView.setBlocksTouches(true);
			showcaseView.setHideOnTouchOutside(true);
			return this;
		}

		/**
		 * Set the ShowcaseView to only ever show once.
		 *
		 * @param shotId a unique identifier (<em>across the app</em>) to store
		 *               whether this ShowcaseView has been shown.
		 */
		public Builder singleShot(long shotId) {
			showcaseView.setSingleShot(shotId);
			return this;
		}

		public Builder setShowcaseEventListener(OnShowcaseEventListener showcaseEventListener) {
			showcaseView.setOnShowcaseEventListener(showcaseEventListener);
			return this;
		}
	}

	/**
	 * Set whether the text should be centred in the screen, or left-aligned (which is the default).
	 */
	public void setShouldCentreText(boolean sCT) {
		this.shouldCentreText = sCT;
		hasAlteredText = true;
		invalidate();
	}

	/**
	 * @see com.github.amlcurran.showcaseview.ShowcaseView.Builder#setSingleShot(long)
	 */
	private void setSingleShot(long shotId) {
		shotStateStore.setSingleShot(shotId);
	}

	/**
	 * Change the position of the ShowcaseView's button from the default bottom-right position.
	 *
	 * @param layoutParams a {@link android.widget.RelativeLayout.LayoutParams} representing
	 *                     the new position of the button
	 */
	@Override
	public void setButtonPosition(RelativeLayout.LayoutParams layoutParams) {
		mEndButton.setLayoutParams(layoutParams);
	}

	/**
	 * Set the duration of the fading in and fading out of the ShowcaseView
	
	private void setFadeDurations(long fIM, long fOM) {
		this.fadeInMillis = fIM;
		this.fadeOutMillis = fOM;
	}*/
	
	/**
	 * @see com.github.amlcurran.showcaseview.ShowcaseView.Builder#hideOnTouchOutside()
	 */
	@Override
	public void setHideOnTouchOutside(boolean hOT) {
		this.hideOnTouch = hOT;
	}

	/**
	 * @see com.github.amlcurran.showcaseview.ShowcaseView.Builder#doNotBlockTouches()
	 */
	@Override
	public void setBlocksTouches(boolean bT) {
		this.blockTouches = bT;
	}

	/**
	 * @see com.github.amlcurran.showcaseview.ShowcaseView.Builder#setStyle(int)
	 */
	@Override
	public void setStyle(int theme) {
		TypedArray array = getContext().obtainStyledAttributes(theme, R.styleable.ShowcaseView);
		updateStyle(array, true);
	}

	@Override
	public boolean isShowing() {
		return isShowing;
	}

	private void updateStyle(TypedArray styled, boolean invalidate) {
		int backgroundColor = styled.getColor(R.styleable.ShowcaseView_sv_backgroundColor, Color.argb(128, 80, 80, 80));
		int showcaseColor = styled.getColor(R.styleable.ShowcaseView_sv_showcaseColor, HOLO_BLUE);
		String buttonText = styled.getString(R.styleable.ShowcaseView_sv_buttonText);
		if (TextUtils.isEmpty(buttonText)) {
			buttonText = getResources().getString(android.R.string.ok);
		}
		boolean tintButton = styled.getBoolean(R.styleable.ShowcaseView_sv_tintButtonColor, true);

		int titleTextAppearance = styled.getResourceId(R.styleable.ShowcaseView_sv_titleTextAppearance,
				R.style.TextAppearance_ShowcaseView_Title);
		int detailTextAppearance = styled.getResourceId(R.styleable.ShowcaseView_sv_detailTextAppearance,
				R.style.TextAppearance_ShowcaseView_Detail);
		
		int htcTitleStyle = this.getResources().getIdentifier("title_primary_xl", "style", "com.htc");
		if (htcTitleStyle != 0) titleTextAppearance = htcTitleStyle;
		
		int htcDetailStyle = this.getResources().getIdentifier("title_secondary_m", "style", "com.htc");
		if (htcDetailStyle != 0) detailTextAppearance = htcDetailStyle;

		styled.recycle();

		showcaseDrawer.setShowcaseColour(showcaseColor);
		showcaseDrawer.setBackgroundColour(backgroundColor);
		tintButton(showcaseColor, tintButton);
		mEndButton.setText(buttonText);
		textDrawer.setTitleStyling(titleTextAppearance);
		textDrawer.setDetailStyling(detailTextAppearance);
		hasAlteredText = true;

		if (invalidate) {
			invalidate();
		}
	}

	private void tintButton(int showcaseColor, boolean tintButton) {
		if (tintButton) {
			mEndButton.getBackground().setColorFilter(showcaseColor, PorterDuff.Mode.MULTIPLY);
		}
	}

	private class UpdateOnGlobalLayout implements ViewTreeObserver.OnGlobalLayoutListener {

		@Override
		public void onGlobalLayout() {
			if (!shotStateStore.hasShot()) {
				updateBitmap();
			}
		}
	}

	private class CalculateTextOnPreDraw implements ViewTreeObserver.OnPreDrawListener {

		@Override
		public boolean onPreDraw() {
			recalculateText();
			return true;
		}
	}

	private OnClickListener hideOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			hide();
		}
	};

}

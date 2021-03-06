package com.sensetoolbox.six.material.utils;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

public class ColorPickerPreference extends DialogPreference implements DialogInterface.OnClickListener {
	String androidns = "http://schemas.android.com/apk/res/android";
	String toolboxns = "http://schemas.android.com/apk/res/com.sensetoolbox.six";
	SharedPreferences prefs = null;
	String mKey;
	Context mContext;
	float density;
	SeekBar red;
	SeekBar green;
	SeekBar blue;
	SeekBar opacity;
	FrameLayout colorSample;
	ColorDrawable topLayer;
	TextView colorHEX;
	
	int densify(int f) {
		return Math.round(density *f);
	}
	
	public ColorPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		density = context.getResources().getDisplayMetrics().density;
		mKey = attrs.getAttributeValue(androidns, "key");
		this.setDialogTitle(Helpers.l10n(context, this.getTitleRes()));
	}
	
	private void updateSample() {
		int clr = Color.argb(opacity.getProgress(), red.getProgress(), green.getProgress(), blue.getProgress());
		colorHEX.setText(String.format("#%08X", (0xFFFFFFFF & clr)));
		topLayer.setColor(clr);
		colorSample.invalidate();
	}
	
	@Override
	protected View onCreateDialogView() {
		prefs = getPreferenceManager().getSharedPreferences();
		
		LinearLayout layout = new LinearLayout(mContext);
		layout.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(densify(10), densify(10), densify(10), densify(10));
		
		LinearLayout sampleRow = new LinearLayout(mContext);
		sampleRow.setOrientation(LinearLayout.HORIZONTAL);
		sampleRow.setPadding(densify(40), densify(10), densify(10), densify(10));
		LinearLayout.LayoutParams samplelp = new LinearLayout.LayoutParams(0, densify(40));
		samplelp.weight = 1;
		topLayer = new ColorDrawable(Color.TRANSPARENT);
		BitmapDrawable grid = (BitmapDrawable)mContext.getResources().getDrawable(R.drawable.transparency_grid);
		grid.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		LayerDrawable layers = new LayerDrawable(new Drawable[] {
			grid,
			topLayer
		});
		
		colorSample = new FrameLayout(mContext);
		colorSample.setLayoutParams(samplelp);
		colorSample.setBackground(layers);
		
		colorHEX = new TextView(mContext);
		colorHEX.setLayoutParams(samplelp);
		colorHEX.setGravity(Gravity.CENTER);
		
		sampleRow.addView(colorSample);
		sampleRow.addView(colorHEX);
		layout.addView(sampleRow);
		
		OnSeekBarChangeListener osbcl = new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				updateSample();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		};
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		LinearLayout redRow = new LinearLayout(mContext);
		redRow.setOrientation(LinearLayout.HORIZONTAL);
		redRow.setPadding(densify(10), densify(20), 0, 0);
		TextView textR = new TextView(mContext);
		textR.setText("R");
		textR.setWidth(densify(15));
		red = new SeekBar(mContext);
		red.setLayoutParams(lp);
		red.setMax(255);
		redRow.addView(textR);
		redRow.addView(red);
		
		LinearLayout greenRow = new LinearLayout(mContext);
		greenRow.setOrientation(LinearLayout.HORIZONTAL);
		greenRow.setPadding(densify(10), densify(20), 0, 0);
		TextView textG = new TextView(mContext);
		textG.setText("G");
		textG.setWidth(densify(15));
		green = new SeekBar(mContext);
		green.setLayoutParams(lp);
		green.setMax(255);
		greenRow.addView(textG);
		greenRow.addView(green);
		
		LinearLayout blueRow = new LinearLayout(mContext);
		blueRow.setOrientation(LinearLayout.HORIZONTAL);
		blueRow.setPadding(densify(10), densify(20), 0, 0);
		TextView textB = new TextView(mContext);
		textB.setText("B");
		textB.setWidth(densify(15));
		blue = new SeekBar(mContext);
		blue.setLayoutParams(lp);
		blue.setMax(255);
		blueRow.addView(textB);
		blueRow.addView(blue);
		
		LinearLayout opacityRow = new LinearLayout(mContext);
		TextView textA = new TextView(mContext);
		textA.setText("A");
		textA.setWidth(densify(15));
		opacityRow.setOrientation(LinearLayout.HORIZONTAL);
		opacityRow.setPadding(densify(10), densify(20), 0, 0);
		opacity = new SeekBar(mContext);
		opacity.setLayoutParams(lp);
		opacity.setMax(255);
		opacityRow.addView(textA);
		opacityRow.addView(opacity);
		
		layout.addView(redRow);
		layout.addView(greenRow);
		layout.addView(blueRow);
		layout.addView(opacityRow);
		
		View divider = new View(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 2);
		params.setMargins(0, densify(16), 0, densify(7));
		divider.setLayoutParams(params);
		divider.setPadding(0, 0, 0, 0);
		if (Integer.parseInt(Helpers.prefs.getString("pref_key_toolbox_material_background", "1")) == 2)
			divider.setBackgroundResource(R.drawable.inset_list_divider_dark);
		else
			divider.setBackgroundResource(R.drawable.inset_list_divider);
		layout.addView(divider);
		
		TextView presets = new TextView(mContext);
		presets.setText(Helpers.l10n(mContext, R.string.cleanbeam_colortheme_preset));
		presets.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		presets.setPadding(densify(10), densify(4), densify(10), densify(4));
		layout.addView(presets);
		
		LinearLayout presetsContainer = new LinearLayout(mContext);
		presetsContainer.setPadding(densify(10), densify(3), densify(10), densify(8));
		presetsContainer.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		presetsContainer.setOrientation(LinearLayout.HORIZONTAL);
		layout.addView(presetsContainer);
		
		TypedArray presetsArrayIds = mContext.getResources().obtainTypedArray(R.array.headsup_theme_presets);
		int cnt = 1;
		for (int colorNameId = 0; colorNameId < presetsArrayIds.length(); colorNameId++) {
			Button btn = new Button(mContext);
			btn.setText(Helpers.l10n(mContext, presetsArrayIds.getResourceId(colorNameId, 0)));
			btn.setSingleLine(true);
			LinearLayout.LayoutParams llparams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			llparams.rightMargin = densify(10);
			llparams.bottomMargin = densify(3);
			btn.setLayoutParams(llparams);
			btn.setTag(cnt);
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int[] colors = Helpers.getThemeColors(mKey, (Integer)v.getTag());
					ValueAnimator animR = ValueAnimator.ofInt(red.getProgress(), colors[0]).setDuration(500);
					animR.addUpdateListener(new AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator animation) {
							red.setProgress((Integer)animation.getAnimatedValue());
						}
					});
					animR.start();
					
					ValueAnimator animG = ValueAnimator.ofInt(green.getProgress(), colors[1]).setDuration(500);
					animG.addUpdateListener(new AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator animation) {
							green.setProgress((Integer)animation.getAnimatedValue());
						}
					});
					animG.start();
					
					ValueAnimator animB = ValueAnimator.ofInt(blue.getProgress(), colors[2]).setDuration(500);
					animB.addUpdateListener(new AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator animation) {
							blue.setProgress((Integer)animation.getAnimatedValue());
						}
					});
					animB.start();
					
					ValueAnimator animO = ValueAnimator.ofInt(opacity.getProgress(), colors[3]).setDuration(500);
					animO.addUpdateListener(new AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator animation) {
							opacity.setProgress((Integer)animation.getAnimatedValue());
						}
					});
					animO.start();
					
					(new Handler()).postDelayed(new Runnable() {
						@Override
						public void run() {
							updateSample();
						}
					}, 600);
				}
			});
			presetsContainer.addView(btn);
			cnt++;
		}
		presetsArrayIds.recycle();
		
		int[] defaults = Helpers.getDefColors(mKey);
		opacity.setProgress(prefs.getInt(mKey + "_A", defaults[0]));
		red.setProgress(prefs.getInt(mKey + "_R", defaults[1]));
		green.setProgress(prefs.getInt(mKey + "_G", defaults[2]));
		blue.setProgress(prefs.getInt(mKey + "_B", defaults[3]));
		updateSample();
		
		opacity.setOnSeekBarChangeListener(osbcl);
		red.setOnSeekBarChangeListener(osbcl);
		green.setOnSeekBarChangeListener(osbcl);
		blue.setOnSeekBarChangeListener(osbcl);
		
		return layout;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			prefs.edit().putInt(mKey + "_R", red.getProgress()).commit();
			prefs.edit().putInt(mKey + "_G", green.getProgress()).commit();
			prefs.edit().putInt(mKey + "_B", blue.getProgress()).commit();
			prefs.edit().putInt(mKey + "_A", opacity.getProgress()).commit();
		}
	}
}

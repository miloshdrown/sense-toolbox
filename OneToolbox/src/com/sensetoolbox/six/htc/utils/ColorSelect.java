package com.sensetoolbox.six.htc.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.htc.widget.HtcListView;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;

public class ColorSelect extends HtcListView {
	private int selected = 0;
	
	public ColorSelect(Context ctx) {
		this(ctx, 0, false);
	}
	
	public ColorSelect(Context ctx, int selectedTheme) {
		this(ctx, selectedTheme, false);
	}
		
	public ColorSelect(Context ctx, int selectedTheme, boolean isBF) {
		super(ctx);
		selected = selectedTheme;
		final ColorSelect cs = this;
		setAdapter(new ColorArrayAdapter(ctx, selected, isBF));
		this.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
		this.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selected = position;
				((ColorArrayAdapter)cs.getAdapter()).notifyDataSetChanged();
			}
		});
		this.setSelection(selected);
	}
	
	public int getSelectedTheme(){
		return selected;
	}
	
	private class ColorArrayAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		//Context mContext = null;
		boolean isBlinkfeed = false;

		public ColorArrayAdapter(Context context, int i, boolean isBF) {
			//mContext = context;
			selected = i;
			mInflater = LayoutInflater.from(context);
			isBlinkfeed = isBF;
		}
		
		public int getCount() {
			return Helpers.colors.size();
		}
		
		public Object[] getItem(int position) {
			return Helpers.colors.valueAt(position);
		}
		
		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView == null)
				row = mInflater.inflate(R.layout.htc_list_item_colors, parent, false);
			else
				row = convertView;

			Drawable[] layers = new Drawable[2];
			ShapeDrawable borderColor = new ShapeDrawable(new RectShape());
			borderColor.getPaint().setColor(Color.argb(200, 25, 25, 25));
			borderColor.getPaint().setStyle(Style.STROKE);
			borderColor.getPaint().setStrokeWidth(2);
			
			int item0 = (Integer)getItem(position)[1];
			int item1 = (Integer)getItem(position)[2];
			int item2 = (Integer)getItem(position)[3];
			
			ColorFrameLayout colorHeader = (ColorFrameLayout)row.findViewById(R.id.header_color);
			layers[0] = new ColorDrawable(item0);
			layers[1] = borderColor;
			LayerDrawable compositeHeader = new LayerDrawable(layers);
			colorHeader.setBackground(compositeHeader);
			
			ColorFrameLayout colorControls = (ColorFrameLayout)row.findViewById(R.id.controls_color);
			layers[0] = new ColorDrawable(item1);
			layers[1] = borderColor;
			LayerDrawable compositeControls = new LayerDrawable(layers);
			colorControls.setBackground(compositeControls);
			
			TextView colorHeaderTitle = (TextView)row.findViewById(R.id.header_color_title);
			colorHeaderTitle.setText(Helpers.l10n(colorHeaderTitle.getContext(), R.string.sense_theme_color_header));
			if (item0 == 0xff252525 || item0 == 0xff255999 || item0 == 0xff062a30)
				colorHeaderTitle.setTextColor(0xffdfdfdf);
			else
				colorHeaderTitle.setTextColor(0xff161616);
				
			TextView controlsHeaderTitle = (TextView)row.findViewById(R.id.controls_color_title);
			controlsHeaderTitle.setText(Helpers.l10n(controlsHeaderTitle.getContext(), R.string.sense_theme_color_controls));
			if (item1 == 0xff252525 || item1 == 0xff255999 || item1 == 0xff062a30)
				controlsHeaderTitle.setTextColor(0xffdfdfdf);
			else
				controlsHeaderTitle.setTextColor(0xff161616);
			
			TextView blinkfeedHeaderTitle = (TextView)row.findViewById(R.id.blinkfeed_color_title);
			ColorFrameLayout colorBlinkfeed = (ColorFrameLayout)row.findViewById(R.id.blinkfeed_color);
			layers[0] = new ColorDrawable(item2);
			layers[1] = borderColor;
			LayerDrawable compositeBlinkfeed = new LayerDrawable(layers);
			colorBlinkfeed.setBackground(compositeBlinkfeed);
			
			if (item2 <= 0xff4b4b4b)
				blinkfeedHeaderTitle.setTextColor(0xffdfdfdf);
			else
				blinkfeedHeaderTitle.setTextColor(0xff161616);
			
			if (isBlinkfeed)
				blinkfeedHeaderTitle.setText(Helpers.l10n(blinkfeedHeaderTitle.getContext(), R.string.sense_theme_color_bf));
			else
				blinkfeedHeaderTitle.setText(Helpers.l10n(blinkfeedHeaderTitle.getContext(), R.string.sense_theme_color_misc));
						
			if (position == selected) {
				colorHeader.setTag(true);
				colorControls.setTag(true);
				colorBlinkfeed.setTag(true);
			} else {
				colorHeader.setTag(false);
				colorControls.setTag(false);
				colorBlinkfeed.setTag(false);
			}
			
			return row;
		}
	}
}

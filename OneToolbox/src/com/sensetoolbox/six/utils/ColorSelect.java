package com.sensetoolbox.six.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.htc.widget.HtcListView;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.SenseThemes;

public class ColorSelect extends HtcListView {
	private int selected = 0;
	
	public ColorSelect(Context ctx) {
		this(ctx, 0);
	}
		
	public ColorSelect(Context ctx, int selectedTheme) {
		super(ctx);
		selected = selectedTheme;
		final ColorSelect cs = this;
		setAdapter(new ColorArrayAdapter(ctx, SenseThemes.getColors(), selected));
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
		
		final SparseArray<int[]> items;
		private LayoutInflater mInflater;
		Context mContext = null;

		public ColorArrayAdapter(Context context, SparseArray<int[]> colors, int i) {
			mContext = context;
			items = colors;
			selected = i;
			mInflater = LayoutInflater.from(context);
		}
		
		public int getCount() {
			return items.size();
		}
		 
		public int[] getItem(int position) {
			return items.get(items.keyAt(position));
		}
		 
		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView != null)
				row = convertView;
			else
				row = mInflater.inflate(R.layout.htc_list_item_colors, parent, false);

			Drawable[] layers = new Drawable[2];
			ShapeDrawable borderColor = new ShapeDrawable(new RectShape());
			borderColor.getPaint().setColor(Color.argb(200, 25, 25, 25));
			borderColor.getPaint().setStyle(Style.STROKE);
			borderColor.getPaint().setStrokeWidth(2);
			
			int item0 = getItem(position)[0];
			int item1 = getItem(position)[1];
			
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
			if (item0 == 0xff252525)
				colorHeaderTitle.setTextColor(mContext.getResources().getColor(android.R.color.primary_text_dark));
			else
				colorHeaderTitle.setTextColor(mContext.getResources().getColor(android.R.color.primary_text_light));
				
			TextView controlsHeaderTitle = (TextView)row.findViewById(R.id.controls_color_title);
			if (item1 == 0x252525)
				controlsHeaderTitle.setTextColor(mContext.getResources().getColor(android.R.color.primary_text_dark));
			else
				controlsHeaderTitle.setTextColor(mContext.getResources().getColor(android.R.color.primary_text_light));
			
			if (position == selected) {
				colorHeader.setTag(true);
				colorControls.setTag(true);
			} else {
				colorHeader.setTag(false);
				colorControls.setTag(false);
			}
			
			return row;
		}
	}
}

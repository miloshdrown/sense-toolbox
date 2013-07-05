package com.langerhans.one;

import java.util.List;

import com.langerhans.one.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomArrayAdapter extends ArrayAdapter<String> {
	
	private final Activity context;
	private final List<String> objects;

	public CustomArrayAdapter(Activity context, int resource,
			int textViewResourceId, List<String> objects) {
		super(context, resource, textViewResourceId, objects);
		this.context = context;
		this.objects = objects;
	}
	
	 public View getView(int position, View convertView, ViewGroup parent) {

	        LayoutInflater inflator = context.getLayoutInflater();
	        View rowView = inflator.inflate(R.layout.row_layout, null, true);
	        TextView textView = (TextView) rowView.findViewById(R.id.num);
	        textView.setText(Integer.toString(objects.indexOf(objects.get(position))+1));
	        
	        TextView textView2 = (TextView) rowView.findViewById(R.id.text);
	        textView2.setText(objects.get(position));
	        super.getView(position, convertView, parent);
	        return rowView;
	    }

}

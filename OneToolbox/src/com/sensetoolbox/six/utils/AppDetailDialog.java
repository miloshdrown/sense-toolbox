package com.sensetoolbox.six.utils;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcListItemSeparator;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.SenseThemes;
import com.sensetoolbox.six.SenseThemes.PackageTheme;

public class AppDetailDialog extends HtcAlertDialog {
	SenseThemes stContext = null;
	String pkgName = null;
	
	public AppDetailDialog(SenseThemes st, String pkg) {
		super(st);
		stContext = st;
		pkgName = pkg;
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		float density = this.getContext().getResources().getDisplayMetrics().density;
		//int pad5dp = Math.round(5 * density);
		int pad10dp = Math.round(10 * density);
		
		// Get Sense theme colors and theme number
		/*
		ArrayList<ThemeColor> themeColors = HtcWrapConfiguration.getThemeColor(this.getContext());
		int themeId = HtcWrapConfiguration.getHtcThemeId(this.getContext(), 0);
		int theme = 1;
		switch (themeId) {
			case 33751145: theme = 0; break;
			case 33751491: theme = 1; break;
			case 33751511: theme = 2; break;
			case 33751531: theme = 3;
		}
		*/
		LinearLayout globalLayout = new LinearLayout(this.getContext());
		globalLayout.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout.LayoutParams lllp1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		// Theme
		HtcListItemSeparator separator = new HtcListItemSeparator(globalLayout.getContext());
		separator.setLayoutParams(lllp1);
		separator.setText(0, R.string.sense_theme_sep1);
		
		// Theme colors
		LinearLayout themeColorsLayout = new LinearLayout(globalLayout.getContext());
		themeColorsLayout.setLayoutParams(lllp1);
		themeColorsLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		LinearLayout.LayoutParams lllpcs = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, Math.round(density * 190));
		PackageTheme pt = SenseThemes.arrayHasPkg(pkgName);
		int selectedTheme = 0;
		if (pt != null) selectedTheme = pt.getTheme();
		final ColorSelect cs;
		if (pkgName.equals("com.htc.launcher"))
			cs = new ColorSelect(themeColorsLayout.getContext(), selectedTheme, true);
		else
			cs = new ColorSelect(themeColorsLayout.getContext(), selectedTheme);
		cs.setLayoutParams(lllpcs);
		
		themeColorsLayout.addView(cs);
		themeColorsLayout.setPadding(0, pad10dp, 0, pad10dp);

		LinearLayout themeBtnsLayout = new LinearLayout(globalLayout.getContext());
		themeBtnsLayout.setLayoutParams(lllp1);
		themeBtnsLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		// Construct resulting layout
		globalLayout.addView(separator);
		globalLayout.addView(themeColorsLayout);
		globalLayout.addView(themeBtnsLayout);
		
		this.setButton(DialogInterface.BUTTON_POSITIVE, this.getContext().getString(R.string.sense_themes_apply), new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		if (pkgName.equals("replace_all")) {
        			for (PackageTheme pt: SenseThemes.pkgthm) if (pt != null) pt.setTheme(cs.getSelectedTheme());
        			stContext.savePkgs();
    				stContext.notifyThemeChanged();
        		} else {
        			PackageTheme pt = SenseThemes.arrayHasPkg(pkgName);
        			if (pt != null) {
        				pt.setTheme(cs.getSelectedTheme());
        				stContext.savePkgs();
        				stContext.notifyThemeChanged();
        			}
        		}
        	}
        });
		
		if (!pkgName.equals("replace_all"))
		this.setButton(DialogInterface.BUTTON_NEUTRAL, this.getContext().getString(R.string.sense_theme_remove), new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		PackageTheme pt = SenseThemes.arrayHasPkg(pkgName);
        		if (pt != null) {
        			SenseThemes.pkgthm.remove(pt);
        			stContext.savePkgs();
        			stContext.updateListArray();
        			stContext.notifyThemeChanged();
        		}
        	}
        });
		
		this.setButton(DialogInterface.BUTTON_NEGATIVE, this.getContext().getString(R.string.sense_themes_cancel), new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {}
        });
		this.setView(globalLayout);
        super.onCreate(savedInstanceState);
	}
}

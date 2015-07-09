package com.sensetoolbox.six.htc.utils;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcListItemSeparator;
import com.sensetoolbox.six.R;
import com.sensetoolbox.six.htc.SenseThemes;
import com.sensetoolbox.six.htc.SenseThemes.PackageTheme;
import com.sensetoolbox.six.utils.Helpers;

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
		int pad5dp = Math.round(5 * density);
		int pad10dp = Math.round(10 * density);
		
		LinearLayout globalLayout = new LinearLayout(this.getContext());
		globalLayout.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout.LayoutParams lllp1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		// Theme
		HtcListItemSeparator separator = new HtcListItemSeparator(globalLayout.getContext());
		separator.setLayoutParams(lllp1);
		separator.setText(0, Helpers.l10n(this.getContext(), R.string.sense_theme_sep1));
		
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
		
		if (pkgName.equals("com.htc.sense.ime")) {
			HtcListItemSeparator separator2 = new HtcListItemSeparator(globalLayout.getContext());
			separator2.setLayoutParams(lllp1);
			separator2.setText(0, Helpers.l10n(this.getContext(), R.string.sense_theme_sep2));
			
			TextView infoIME = new TextView(globalLayout.getContext());
			infoIME.setLayoutParams(lllp1);
			infoIME.setText(Helpers.l10n(this.getContext(), R.string.sense_theme_infoIME));
			infoIME.setPadding(pad10dp + pad5dp, pad10dp, pad10dp + pad5dp, pad10dp);
			
			globalLayout.addView(separator2);
			globalLayout.addView(infoIME);
		} else if (pkgName.equals("com.htc.launcher")) {
			HtcListItemSeparator separator2 = new HtcListItemSeparator(globalLayout.getContext());
			separator2.setLayoutParams(lllp1);
			separator2.setText(0, Helpers.l10n(this.getContext(), R.string.sense_theme_sep2));
			
			TextView infoSENSE = new TextView(globalLayout.getContext());
			infoSENSE.setLayoutParams(lllp1);
			infoSENSE.setText(Helpers.l10n(this.getContext(), R.string.sense_theme_infoSENSE));
			infoSENSE.setPadding(pad10dp + pad5dp, pad10dp, pad10dp + pad5dp, pad10dp);
			
			globalLayout.addView(separator2);
			globalLayout.addView(infoSENSE);
		}
		
		this.setButton(DialogInterface.BUTTON_POSITIVE, Helpers.l10n(this.getContext(), R.string.sense_themes_apply), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (pkgName.equals("replace_all")) {
					for (PackageTheme pt: SenseThemes.pkgthm) if (pt != null) pt.setTheme(cs.getSelectedTheme());
					stContext.savePkgs();
					stContext.notifyThemeChanged(pkgName);
				} else {
					PackageTheme pt = SenseThemes.arrayHasPkg(pkgName);
					if (pt != null) {
						pt.setTheme(cs.getSelectedTheme());
						stContext.savePkgs();
						stContext.notifyThemeChanged(pkgName);
					}
				}
			}
		});
		
		if (!pkgName.equals("replace_all"))
		this.setButton(DialogInterface.BUTTON_NEUTRAL, Helpers.l10n(this.getContext(), R.string.sense_theme_remove), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				PackageTheme pt = SenseThemes.arrayHasPkg(pkgName);
				if (pt != null) {
					SenseThemes.pkgthm.remove(pt);
					stContext.savePkgs();
					stContext.updateListArray();
					stContext.notifyThemeChanged(pkgName);
				}
			}
		});
		
		this.setButton(DialogInterface.BUTTON_NEGATIVE, Helpers.l10n(this.getContext(), R.string.sense_themes_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});
		this.setView(globalLayout);
		super.onCreate(savedInstanceState);
	}
}

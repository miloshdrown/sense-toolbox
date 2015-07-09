package com.sensetoolbox.six.material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sensetoolbox.six.R;
import com.sensetoolbox.six.utils.Helpers;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class MTouchLock extends MActivityEx {
	MenuItem menuDefine;
	MenuItem menuReboot;
	SharedPreferences prefs;
	Switch OnOffSwitch;
	LinearLayout gridBkg;
	TextView modHint;
	TextView hintSeq;
	List<String> sequence = new ArrayList<String>();
	boolean isRecording = false;
	
	@Override
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_touch_lock);
		
		getActionBar().setTitle(Helpers.l10n(this, R.string.various_touchlock_title));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		prefs = getSharedPreferences("one_toolbox_prefs", 1);
		
		LinearLayout row1 = (LinearLayout)findViewById(R.id.row1);
		LinearLayout row2 = (LinearLayout)findViewById(R.id.row2);
		row1.animate().alpha(0.5f).setDuration(100).start();
		row2.animate().alpha(0.5f).setDuration(100).start();
		
		TextView hint = (TextView)findViewById(R.id.hint);
		hint.setText(Helpers.l10n(this, R.string.touchlock_hint));
		
		sequence = new ArrayList<String>(Arrays.asList(prefs.getString("touch_lock_sequence", "").split(",")));
		sequence.removeAll(Collections.singleton(null));
		sequence.removeAll(Collections.singleton(""));
		
		hintSeq = (TextView)findViewById(R.id.hintSeq);
		updateSequenceText();
		
		LinearLayout cell1 = (LinearLayout)findViewById(R.id.cell1);
		cell1.setOnTouchListener(otl);
		cell1.setTag("1");
		LinearLayout cell2 = (LinearLayout)findViewById(R.id.cell2);
		cell2.setOnTouchListener(otl);
		cell2.setTag("2");
		LinearLayout cell3 = (LinearLayout)findViewById(R.id.cell3);
		cell3.setOnTouchListener(otl);
		cell3.setTag("3");
		LinearLayout cell4 = (LinearLayout)findViewById(R.id.cell4);
		cell4.setOnTouchListener(otl);
		cell4.setTag("4");
				
		TextView cell1text = (TextView)findViewById(R.id.cell1txt);
		cell1text.setText("1");
		TextView cell2text = (TextView)findViewById(R.id.cell2txt);
		cell2text.setText("2");
		TextView cell3text = (TextView)findViewById(R.id.cell3txt);
		cell3text.setText("3");
		TextView cell4text = (TextView)findViewById(R.id.cell4txt);
		cell4text.setText("4");
		
		modHint = (TextView)findViewById(R.id.modhint);
		modHint.setText(Helpers.l10n(this, R.string.touchlock_hint_mod));
		
		TextView experimental = (TextView)findViewById(R.id.experimental);
		experimental.setText(Helpers.l10n(this, R.string.touchlock_root_remind));
		experimental.setTextColor(getResources().getColor(android.R.color.background_light));
		
		FrameLayout experimentalFrame = (FrameLayout)findViewById(R.id.experimentalFrame);
		experimentalFrame.setVisibility(View.VISIBLE);
		
		gridBkg = (LinearLayout)findViewById(R.id.gridBkg);
	}
	
	private void updateSequenceText() {
		if (sequence.isEmpty())
			hintSeq.setText(Helpers.l10n(this, R.string.touchlock_seq) + ": " + Helpers.l10n(this, R.string.touchlock_not_defined));
		else
			hintSeq.setText(Helpers.l10n(this, R.string.touchlock_seq) + ": " + TextUtils.join(" ", sequence));
	}
	
	private void applyState(boolean state) {
		gridBkg.setEnabled(state);
		OnOffSwitch.setChecked(state);
		menuDefine.setEnabled(state);
		
		if (menuDefine.isEnabled())
			menuDefine.getIcon().setAlpha(255);
		else
			menuDefine.getIcon().setAlpha(127);
		
		if (state) {
			gridBkg.setVisibility(View.VISIBLE);
			modHint.setVisibility(View.GONE);
		} else {
			gridBkg.setVisibility(View.GONE);
			modHint.setVisibility(View.VISIBLE);
		}
	}
	
	View.OnTouchListener otl = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (isRecording && OnOffSwitch.isChecked())
			switch (event.getAction()) {
				case 0:
					v.setBackgroundColor(0xff888888);
					if (v.getTag() != null) {
						sequence.add((String)v.getTag());
						updateSequenceText();
						if (sequence.size() >= 12) {
							v.setBackgroundColor(0xff666666);
							defineProc();
						}
					}
					break;
				case 1:
					v.setBackgroundColor(0xff666666);
					break;
			}
			v.performClick();
			return true;
		}
	};
	
	private void defineProc() {
		LinearLayout row1 = (LinearLayout)findViewById(R.id.row1);
		LinearLayout row2 = (LinearLayout)findViewById(R.id.row2);
		if (!isRecording) {
			isRecording = true;
			menuDefine.setIcon(R.drawable.ic_menu_check);
			row1.setAlpha(1.0f);
			row2.setAlpha(1.0f);
			sequence.clear();
		} else {
			isRecording = false;
			menuDefine.setIcon(R.drawable.ic_menu_define_pattern);
			row1.setAlpha(0.5f);
			row2.setAlpha(0.5f);
			prefs.edit().putString("touch_lock_sequence", TextUtils.join(",", sequence)).commit();
		}
		updateSequenceText();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.menu_sub_touch, menu);
		
		menuDefine = menu.getItem(1);
		menuDefine.setTitle(Helpers.l10n(this, R.string.define_lock_seq));
		menuDefine.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				defineProc();
				return true;
			}
		});
		
		menuReboot = menu.getItem(0);
		menuReboot.setTitle(Helpers.l10n(this, R.string.soft_reboot));
		menuReboot.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				AlertDialog.Builder alert = new AlertDialog.Builder(MTouchLock.this);
				alert.setTitle(Helpers.l10n(MTouchLock.this, R.string.soft_reboot));
				alert.setView(Helpers.createCenteredText(MTouchLock.this, R.string.hotreboot_explain_prefs));
				alert.setPositiveButton(Helpers.l10n(MTouchLock.this, R.string.yes) + "!", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						try {
							Command command = new Command(0, false, "setprop ctl.restart zygote");
							RootTools.getShell(true).add(command);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				alert.setNegativeButton(Helpers.l10n(MTouchLock.this, R.string.no) + "!", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
				});
				alert.show();
				return true;
			}
		});
		
		OnOffSwitch = (Switch)menu.getItem(2).getActionView().findViewById(R.id.onoffSwitch);

		boolean state = prefs.getBoolean("touch_lock_active", false);
		applyState(state);
		OnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton toggle, boolean state) {
				prefs.edit().putBoolean("touch_lock_active", state).commit();
				applyState(state);
			}
		});
		
		return true;
	}
}

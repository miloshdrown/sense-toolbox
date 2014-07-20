package com.sensetoolbox.six.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import com.htc.fragment.widget.CarouselFragment;
import com.htc.fragment.widget.CarouselTabSpec;
import com.htc.fragment.widget.CarouselUtil;
import com.sensetoolbox.six.DimmedActivity;
import com.sensetoolbox.six.R;

public class Notifications extends CarouselFragment {
	
	public OnCarouselReadyListener onReadyListener;
	public boolean isLoaded = false;
	
	public interface OnCarouselReadyListener {
		void onReady();
    }
	
	public Notifications() {
		super(DimmedActivity.class.getCanonicalName());
		requestCarouselFeature(CarouselFragment.FEATURE_NO_EDITOR);
		setGId(1);
	}
	
	public void addTab(StatusBarNotification notifyRecord) {
		int id = notifyRecord.getId();
		String pkgName = notifyRecord.getPackageName();
		String tag = notifyRecord.getTag();
		String uniqueTag = pkgName + "_" + String.valueOf(id) + "_" + String.valueOf(tag);
		
		CarouselTabSpec tab = new CarouselTabSpec(uniqueTag, R.string.dummy, NotificationTab.class.getCanonicalName());
		
		final PackageManager pm = getActivity().getPackageManager();
		ApplicationInfo ai;
		try {
		    ai = pm.getApplicationInfo(pkgName, 0);
		} catch (Exception e) {
		    ai = null;
		}
		String appName = (String) (ai != null ? pm.getApplicationLabel(ai) : pkgName);
		
		tab.setAlternativeName(appName);
		Bundle fragBundle = new Bundle();
		fragBundle.putInt("id", id);
		fragBundle.putString("pkgName", pkgName);
		fragBundle.putString("tag", tag);
		fragBundle.putString("appName", appName);
		tab.setArguments(fragBundle);
		getCarouselHost().addTab(getActivity(), tab);
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getCarouselHost().setBackgroundColor(CarouselUtil.Skin.getBackgroundColorDark(getActivity(), null));
		if (onReadyListener != null) onReadyListener.onReady();
		isLoaded = true;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
    public void setOnCarouselReadyListener(OnCarouselReadyListener listener) {
    	this.onReadyListener = listener;
    }
}
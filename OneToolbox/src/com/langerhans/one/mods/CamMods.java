package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;

import java.lang.reflect.Method;

import android.view.KeyEvent;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class CamMods{

	private static Method takeFocus;
	private static Method takePicture;
	private static Method triggerRecord;
	private static Method changeZoom;
	static LoadPackageParam lpparamF;

	public static void execHook_VolKey(final LoadPackageParam lpparam, final int volup, final int voldown) {
		lpparamF = lpparam;
	    takePicture = findMethodExact("com.android.camera.HTCCamera", lpparamF.classLoader, "takePicture", String.class);
	    takeFocus = findMethodExact("com.android.camera.HTCCamera", lpparamF.classLoader, "takeFocus", int.class, int.class);
	    triggerRecord = findMethodExact("com.android.camera.HTCCamera", lpparamF.classLoader, "triggerRecord");
	    changeZoom = findMethodExact("com.android.camera.HTCCamera", lpparamF.classLoader, "changeZoom", int.class);
	    
	    hookKeyUp();
	    
	    findAndHookMethod("com.android.camera.HTCCamera", lpparamF.classLoader, "onKeyDown", int.class, android.view.KeyEvent.class, new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    			KeyEvent key =  (KeyEvent) param.args[1];
    			String currentZoom = "";
    			if (key.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
    			{
    				switch (voldown){
    				case 1:
    					takePicture.invoke(param.thisObject, "HTCCamera");
        				param.setResult(true);
        				return;
    				case 2:
    					takeFocus.invoke(param.thisObject, 540, 960);
        				param.setResult(true);
        				return;
    				case 3:
    					triggerRecord.invoke(param.thisObject);
        				param.setResult(true);
        				return;
    				case 5:
    					takeFocus.invoke(param.thisObject, 540, 960);
    					takePicture.invoke(param.thisObject, "HTCCamera");
        				param.setResult(true);
        				return;
    				case 6:
    					currentZoom = getObjectField(param.thisObject, "zoomValue").toString();
    					changeZoom.invoke(param.thisObject, Integer.valueOf(currentZoom) + 12); //12 = 5 zoom steps from 0-60
    					param.setResult(true);
    					return;
    				case 7:
    					currentZoom = getObjectField(param.thisObject, "zoomValue").toString();
    					changeZoom.invoke(param.thisObject, Integer.valueOf(currentZoom) - 12); //12 = 5 zoom steps from 0-60
    					param.setResult(true);
    					return;
        			default:
        				
    				}
    			}
    			if (key.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP)
    			{
    				switch (volup){
    				case 1:
    					takePicture.invoke(param.thisObject, "HTCCamera");
        				param.setResult(true);
        				return;
    				case 2:
    					takeFocus.invoke(param.thisObject, 540, 960);
        				param.setResult(true);
        				return;
    				case 3:
    					triggerRecord.invoke(param.thisObject);
        				param.setResult(true);
        				return;
    				case 5:
    					takeFocus.invoke(param.thisObject, 540, 960);
    					takePicture.invoke(param.thisObject, "HTCCamera");
        				param.setResult(true);
        				return;
    				case 6:
    					currentZoom = getObjectField(param.thisObject, "zoomValue").toString();
    					changeZoom.invoke(param.thisObject, Integer.valueOf(currentZoom) + 12); //12 = 5 zoom steps from 0-60
    					param.setResult(true);
    					return;
    				case 7:
    					currentZoom = getObjectField(param.thisObject, "zoomValue").toString();
    					changeZoom.invoke(param.thisObject, Integer.valueOf(currentZoom) - 12); //12 = 5 zoom steps from 0-60
    					param.setResult(true);
    					return;
        			default:
        				
    				}
    			}
    			Object keyeventargs = newInstance(findClass("com.android.camera.input.KeyEventArgs", lpparamF.classLoader), key);
				Object keydownevent = getObjectField(param.thisObject, "keyDownEvent");
				Method raise = findMethodExact("com.android.camera.event.Event", lpparamF.classLoader, "raise", Object.class, findClass("com.android.camera.event.EventArgs", lpparam.classLoader));
				raise.invoke(keydownevent, param.thisObject, keyeventargs);
    		}
	    });
	}

	private static void hookKeyUp()
	{
		findAndHookMethod("com.android.camera.HTCCamera", lpparamF.classLoader, "onKeyUp", int.class, android.view.KeyEvent.class, new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    			KeyEvent key =  (KeyEvent) param.args[1];
    			int keycode = key.getKeyCode();
    			if (keycode == KeyEvent.KEYCODE_VOLUME_DOWN || keycode == KeyEvent.KEYCODE_VOLUME_UP)
    			{
    				param.setResult(true);
    			}
    		}
		});	
	}
}

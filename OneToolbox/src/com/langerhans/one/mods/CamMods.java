package com.langerhans.one.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;

import java.lang.reflect.Method;

import android.util.Log;
import android.view.KeyEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class CamMods implements IXposedHookLoadPackage{

	private Method takeFocus;
	private Method takePicture;
	private Method triggerRecord;
	private static XSharedPreferences pref;
	LoadPackageParam lpparamF;
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
	    if (!lpparam.packageName.equals("com.android.camera"))
	        return;
	    
	    pref = new XSharedPreferences("com.langerhans.one", "one_toolbox_prefs");
	    final int voldown = Integer.parseInt(pref.getString("pref_key_cam_voldown", "4"));
	    final int volup = Integer.parseInt(pref.getString("pref_key_cam_volup", "4"));
	    final boolean powerW = pref.getBoolean("pref_key_cam_powerW", false);
	    if (voldown == 4 && volup == 4 && !powerW)
	    	return;
	    
	    this.lpparamF = lpparam;
	    takePicture = findMethodExact("com.android.camera.HTCCamera", lpparamF.classLoader, "takePicture", String.class);
	    takeFocus = findMethodExact("com.android.camera.HTCCamera", lpparamF.classLoader, "takeFocus", int.class, int.class);
	    triggerRecord = findMethodExact("com.android.camera.HTCCamera", lpparamF.classLoader, "triggerRecord");
	    
	    hookKeyUp();
	    
	    findAndHookMethod("com.android.camera.HTCCamera", lpparamF.classLoader, "onKeyDown", int.class, android.view.KeyEvent.class, new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    			KeyEvent key =  (KeyEvent) param.args[1];
    			if (key.getKeyCode() == 25)
    			{
    				Log.d("voldown", String.valueOf(voldown));
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
        			default:
        				
    				}
    			}
    			if (key.getKeyCode() == 24)
    			{
    				Log.d("volup", String.valueOf(voldown));
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
        			default:
        				
    				}
    			}
    			Object keyeventargs = newInstance(findClass("com.android.camera.input.KeyEventArgs", lpparamF.classLoader), key);
				Object keydownevent = getObjectField(param.thisObject, "keyDownEvent");
				Method raise = findMethodExact("com.android.camera.event.Event", lpparamF.classLoader, "raise", Object.class, findClass("com.android.camera.event.EventArgs", lpparam.classLoader));
				raise.invoke(keydownevent, param.thisObject, keyeventargs);
    		}
	    });
	    
	    findAndHookMethod("com.android.camera.component.BatteryWatcher", lpparamF.classLoader, "isLower", int.class, String.class, new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    			if(powerW)
    			{
    				param.args[0] = 0;
        			return;
    			}
    		}
    	});
	}
	
	private void hookKeyUp()
	{
		findAndHookMethod("com.android.camera.HTCCamera", lpparamF.classLoader, "onKeyUp", int.class, android.view.KeyEvent.class, new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    			KeyEvent key =  (KeyEvent) param.args[1];
    			int keycode = key.getKeyCode();
    			if (keycode == 25 || keycode == 24)
    			{
    				param.setResult(true);
    			}
    		}
		});	
	}

}

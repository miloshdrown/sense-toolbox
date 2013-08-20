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
	static LoadPackageParam lpparamF;

	public static void execHook_VolKey(final LoadPackageParam lpparam, final int volup, final int voldown) {
		lpparamF = lpparam;
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
        			default:
        				
    				}
    			}
    			if (key.getKeyCode() == 24)
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
    			if (keycode == 25 || keycode == 24)
    			{
    				param.setResult(true);
    			}
    		}
		});	
	}
}

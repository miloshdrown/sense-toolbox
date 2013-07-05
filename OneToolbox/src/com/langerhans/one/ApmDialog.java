package com.langerhans.one;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.htc.widget.HtcAlertDialog;
import com.htc.widget.HtcAlertDialog.Builder;

public class ApmDialog extends DialogFragment {

	static interface AlertNegativeListener
    {
        public abstract void onNegativeClick();
    }

    static interface AlertPositiveListener
    {
        public abstract void onPositiveClick(int i);
    }
	
    AlertNegativeListener alNegL;
    AlertPositiveListener alPosL;
    OnClickListener negL;
    OnClickListener posL;
    
	public ApmDialog() {
		posL = new OnClickListener() {
            public void onClick(DialogInterface di, int i)
            {
                int j = ((HtcAlertDialog)di).getListView().getCheckedItemPosition();
                alPosL.onPositiveClick(j);
            }
        };
        negL = new OnClickListener() {
            public void onClick(DialogInterface dl, int i)
            {
                alNegL.onNegativeClick();
            }
        };
	}
	
	@Override
	public void onAttach(Activity act)
    {
        super.onAttach(act);
        try
        {
            alPosL = (AlertPositiveListener)act;
            alNegL = (AlertNegativeListener)act;
            return;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException((new StringBuilder()).append(act.toString()).append(" must implement AlertPositiveListener").toString());
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle bundle)
    {
        int i = getArguments().getInt("selected");
        Builder bld = new Builder(getActivity());
        bld.setTitle("Advanced Power Menu");
        bld.setSingleChoiceItems(new String[]{"Normal", "Hot reboot", "Recovery", "Bootloader"}, i, null);
        bld.setPositiveButton("OK", posL);
        bld.setNegativeButton("Cancel", negL);
        return bld.create();
    }
	
	@Override
	public void onPause()
    {
        super.onPause();
        alNegL.onNegativeClick();
    }
}

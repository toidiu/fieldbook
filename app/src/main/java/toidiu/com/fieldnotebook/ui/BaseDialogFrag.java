package toidiu.com.fieldnotebook.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import javax.inject.Inject;

import toidiu.com.fieldnotebook.FieldNotebookApplication;
import toidiu.com.fieldnotebook.utils.MoveFolderHelper;
import toidiu.com.fieldnotebook.utils.Prefs;

/**
 * Created by toidiu on 4/4/16.
 */
public class BaseDialogFrag extends DialogFragment {

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    @Inject
    protected Prefs prefs;
    @Inject
    MoveFolderHelper moveFolderHelper;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((FieldNotebookApplication) getActivity().getApplication()).getAppComponent().inject(this);
    }

}

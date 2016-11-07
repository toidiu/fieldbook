package toidiu.com.fieldnotebook.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.ui.extras.ArchiveInterface;

/**
 * Created by toidiu on 4/4/16.
 */
public class DialogConfirmArchive extends BaseDialogFrag {

    //region Fields----------------------
    public static final String FILE_OBJ_EXTRA = "FILE_OBJ_EXTRA";
    //~=~=~=~=~=~=~=~=~=~=~=~=Views
    @Bind(R.id.cancel)
    View cancelBtn;
    @Bind(R.id.archive)
    View archiveBtn;

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    private ArchiveInterface archiveInterface;
    //endregion

    //region Lifecycle----------------------
    public static DialogConfirmArchive getInstance() {
        return new DialogConfirmArchive();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_archive, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();
        initView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        archiveInterface = (ArchiveInterface) activity;
    }

    //endregion

    //region Init----------------------
    private void initView() {
        archiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                archiveInterface.archiveClicked();
                dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }


    private void initData() {

    }
    //endregion

}

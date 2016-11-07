package toidiu.com.fieldnotebook.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.ui.extras.ShareInterface;

/**
 * Created by toidiu on 4/4/16.
 */
public class DialogDailyLogs extends BaseDialogFrag {

    //region Fields----------------------
    public static final String FILE_OBJ_EXTRA = "FILE_OBJ_EXTRA";
    //~=~=~=~=~=~=~=~=~=~=~=~=Views
    @Bind(R.id.explain1)
    EditText explain1;
    @Bind(R.id.explain2)
    EditText explain2;
    @Bind(R.id.cancel)
    View cancelBtn;
    @Bind(R.id.share)
    View shareBtn;

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    private ShareInterface shareInterface;
    private FileObj fileObj;
    //endregion

    //region Lifecycle----------------------
    public static DialogDailyLogs getInstance(FileObj projFolder) {
        DialogDailyLogs dialog = new DialogDailyLogs();
        Bundle args = new Bundle();
        args.putParcelable(FILE_OBJ_EXTRA, projFolder);
        dialog.setArguments(args);


        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_daily_logs, null);
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

        shareInterface = (ShareInterface) activity;
    }

    //endregion

    //region Init----------------------
    private void initView() {
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //// FIXME: 4/26/16 check edit fields and then share
//                archiveInterface.archiveClicked();
                shareInterface.dialogShareClicked(fileObj);
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
        fileObj = getArguments().getParcelable(FILE_OBJ_EXTRA);
    }
    //endregion

}

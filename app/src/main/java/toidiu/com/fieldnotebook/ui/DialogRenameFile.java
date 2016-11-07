package toidiu.com.fieldnotebook.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.edisonwang.ps.annotations.EventListener;
import com.edisonwang.ps.lib.PennStation;

import butterknife.Bind;
import butterknife.ButterKnife;
import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.actions.RenameFileAction;
import toidiu.com.fieldnotebook.actions.RenameFileAction_.PsRenameFileAction;
import toidiu.com.fieldnotebook.actions.events.RenameFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.RenameFileActionSuccess;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.utils.UtilKeyboard;

/**
 * Created by toidiu on 4/4/16.
 */
@EventListener(producers = {
        RenameFileAction.class
})
public class DialogRenameFile extends BaseDialogFrag {

    //region Fields----------------------
    public static final String FILE_OBJ_EXTRA = "FILE_OBJ_EXTRA";
    @Bind(R.id.submit)
    View submitBtn;
    @Bind(R.id.orig_name)
    TextView origNameView;
    @Bind(R.id.rename)
    EditText renameEdit;
    @Bind(R.id.progress)
    ProgressBar progress;
    //endregion

    //region PennStation----------------------
    private DialogRenameFileEventListener eventListener = new DialogRenameFileEventListener() {
        @Override
        public void onEventMainThread(RenameFileActionSuccess event) {
            dismiss();
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onEventMainThread(RenameFileActionFailure event) {
            progress.setVisibility(View.GONE);
        }
    };
    private FileObj fileObj;
    //endregion

    //region Lifecycle----------------------
    public static DialogRenameFile getInstance(FileObj fileObj) {

        DialogRenameFile dialog = new DialogRenameFile();
        Bundle args = new Bundle();
        args.putParcelable(FILE_OBJ_EXTRA, fileObj);
        dialog.setArguments(args);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_rename_file, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PennStation.registerListener(eventListener);

        initData();
        initView();
    }

    @Override
    public void onDestroy() {
        PennStation.unRegisterListener(eventListener);
        super.onDestroy();
    }
    //endregion

    //region Init----------------------
    private void initView() {
        origNameView.setText(fileObj.title);
        renameEdit.setText(fileObj.title);
        renameEdit.setSelection(renameEdit.getText().length());

        UtilKeyboard.toggleKeyboard(getContext());

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = renameEdit.getText().toString();
                if (newName.isEmpty()) {
                    Toast.makeText(getContext(), "New name can't be empty.", Toast.LENGTH_SHORT).show();
                } else {
                    PennStation.requestAction(PsRenameFileAction.helper(fileObj, newName));
                    progress.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void initData() {
        fileObj = getArguments().getParcelable(FILE_OBJ_EXTRA);
    }
    //endregion

}

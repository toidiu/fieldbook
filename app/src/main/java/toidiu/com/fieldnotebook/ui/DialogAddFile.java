package toidiu.com.fieldnotebook.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.edisonwang.ps.annotations.EventListener;
import com.edisonwang.ps.lib.PennStation;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.actions.BaseAction;
import toidiu.com.fieldnotebook.actions.GetNextPONumberAction;
import toidiu.com.fieldnotebook.actions.GetNextPONumberActionHelper;
import toidiu.com.fieldnotebook.actions.events.GetNextPONumberActionFailure;
import toidiu.com.fieldnotebook.actions.events.GetNextPONumberActionSuccess;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.models.NewFileObj;
import toidiu.com.fieldnotebook.ui.extras.AddFileAdapter;
import toidiu.com.fieldnotebook.ui.extras.FabAddFileInterface;
import toidiu.com.fieldnotebook.ui.extras.FileAddInterface;
import toidiu.com.fieldnotebook.utils.UtilKeyboard;
import toidiu.com.fieldnotebook.utils.UtilNetwork;

/**
 * Created by toidiu on 4/4/16.
 */

@EventListener(producers = {
        GetNextPONumberAction.class
})
public class DialogAddFile extends BaseFullScreenDialogFrag implements FileAddInterface {

    //region Fields----------------------
    public static final String FILE_OBJ_EXTRA = "FILE_OBJ_EXTRA";
    //~=~=~=~=~=~=~=~=~=~=~=~=Views
    @Bind(R.id.overlay)
    View overlay;
    @Bind(R.id.container)
    View container;
    @Bind(R.id.recycler)
    RecyclerView recyclerView;
    @Bind(R.id.file_name)
    EditText fileNameEdit;
    @Bind(R.id.po_number)
    TextView poNumberText;
    @Bind(R.id.po_number_container)
    View poNumberContainer;
    @Bind(R.id.file_name_container)
    View fileNameContainer;
    @Bind(R.id.create)
    View createBtn;
    @Bind(R.id.or_text)
    View orText;
    @Bind(R.id.merge)
    View mergeBtn;
    @Bind(R.id.open_file)
    View openFileBtn;
    @Bind(R.id.progress)
    ProgressBar progress;
//    @Bind(R.id.cancel)
//    View cancelPoCreate;
    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    private FileObj projFolder;
    private FabAddFileInterface addFileInterface;


    //region PennStation----------------------
    DialogAddFileEventListener eventListener = new DialogAddFileEventListener() {
        @Override
        public void onEventMainThread(GetNextPONumberActionFailure event) {
            progress.setVisibility(View.GONE);
            Toast.makeText(context, "Unable to add a Purchase Order. Please check your connection.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEventMainThread(final GetNextPONumberActionSuccess event) {
            progress.setVisibility(View.GONE);
            UtilKeyboard.hideKeyboard(getActivity(), fileNameEdit, fileNameEdit);

            poNumberText.setText(event.nextNumber);

            //show the PO number interface and hide fileName interface
            fileNameContainer.setVisibility(View.GONE);
            poNumberContainer.setVisibility(View.VISIBLE);

            //open pdf file
            openFileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFileInterface.openPoPdfClicked(event.fileObj);

                    UtilKeyboard.hideKeyboard(getActivity(), fileNameEdit, fileNameEdit);
                    dismiss();
                }
            });

            //cancel
//            cancelPoCreate.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    poNumberContainer.setVisibility(View.GONE);
//
//                    reset the view to grayish
//                    poNumberText.setBackgroundColor(getResources().getColor(R.color.white_15));
//                }
//            });

            //Copy number
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {

                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("PO number", poNumberText.getText().toString());
                    clipboard.setPrimaryClip(clip);

                    poNumberText.setBackgroundColor(getResources().getColor(R.color.folder_blue));

                    Toast.makeText(context, "Order # copied.", Toast.LENGTH_SHORT).show();
                }
            };

            mainHandler.postDelayed(myRunnable, 1000);


//            copyPoNumberBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                }
//            });

//            int sdk = android.os.Build.VERSION.SDK_INT;
//            if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
//                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//                clipboard.setText("text to clip");
//            } else {
//                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//                android.content.ClipData clip = android.content.ClipData.newPlainText("text label","text to clip");
//                clipboard.setPrimaryClip(clip);
//            }


        }
    };

    //endregion
    //endregion

    //region Lifecycle----------------------
    public static DialogAddFile getInstance(FileObj projFolder) {
        DialogAddFile dialog = new DialogAddFile();
        Bundle args = new Bundle();
        args.putParcelable(FILE_OBJ_EXTRA, projFolder);
        dialog.setArguments(args);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_file, null);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        addFileInterface = (FabAddFileInterface) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();
        initView();
        PennStation.registerListener(eventListener);
    }

    @Override
    public void onDestroy() {
        PennStation.unRegisterListener(eventListener);
        super.onDestroy();
    }
    //endregion

    //region Init----------------------
    private void initView() {
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //placeholder so click event doesn't propagate
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        AddFileAdapter adapter = new AddFileAdapter(this);
        List<String> addList = new ArrayList<>();
        addList.add(BaseAction.PURCHASE_FOLDER_NAME);
        addList.add(BaseAction.FAB_FOLDER_NAME);
        addList.add(BaseAction.PROJ_REQUEST_NAME);
        addList.add(BaseAction.PHOTOS_FOLDER_NAME);
        addList.add(BaseAction.NOTES_FOLDER_NAME);
        adapter.refreshView(addList);
        recyclerView.setAdapter(adapter);

        fileNameContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileNameContainer.setVisibility(View.GONE);
                UtilKeyboard.hideKeyboard(getActivity(), fileNameEdit, fileNameEdit);
            }
        });

    }

    private void initData() {
        projFolder = getArguments().getParcelable(FILE_OBJ_EXTRA);
    }
    //endregion

    //region Interface----------------------
    @Override
    public void itemClicked(final String type) {
        if (type.equals(BaseAction.PHOTOS_FOLDER_NAME)) {
            String name = "photo" + System.currentTimeMillis() + ".jpg";
            createNewFile(type, name);
            return;
        }

        if (type.equals(BaseAction.FAB_FOLDER_NAME)) {
            orText.setVisibility(View.VISIBLE);
            mergeBtn.setVisibility(View.VISIBLE);
        } else {
            orText.setVisibility(View.GONE);
            mergeBtn.setVisibility(View.GONE);
        }

        fileNameContainer.setVisibility(View.VISIBLE);
        UtilKeyboard.toggleKeyboard(getActivity());
        fileNameEdit.requestFocus();

        mergeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilNetwork.isDeviceOnline(getContext())) {
                    mergePfdHelper.isMerging = true;
                    mergePfdHelper.projFolder = projFolder;
                    addFileInterface.mergePdfClicked();
                    UtilKeyboard.hideKeyboard(getActivity(), fileNameEdit, fileNameEdit);
                    dismiss();
                } else {
                    Snackbar.make(mergeBtn, getString(R.string.no_network_merge), Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = fileNameEdit.getText().toString().trim();
                if (!fileName.isEmpty()) {
                    if (!fileName.endsWith(".pdf")) {
                        fileName = fileName + ".pdf";
                    }
                    createNewFile(type, fileName);
                } else {
                    Snackbar.make(fileNameEdit, "File name can't be empty.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createNewFile(String type, String fileName) {
        NewFileObj newFileObj = null;

        if (type.equals(BaseAction.PURCHASE_FOLDER_NAME)) {
            newFileObj = new NewFileObj(BaseAction.PURCHASE_FOLDER_NAME, BaseAction.PURCHASE_ORDER_ASSET_PDF, BaseAction.MIME_PDF, fileName, projFolder.id, null);
            progress.setVisibility(View.VISIBLE);
            PennStation.requestAction(new GetNextPONumberActionHelper(newFileObj));
            return;
        }

        switch (type) {
            case BaseAction.FAB_FOLDER_NAME:
                newFileObj = new NewFileObj(BaseAction.FAB_FOLDER_NAME, BaseAction.FAB_SHEET_ASSET_PDF, BaseAction.MIME_PDF, fileName, projFolder.id, null);
                break;
            case BaseAction.PROJ_REQUEST_NAME:
                newFileObj = new NewFileObj(BaseAction.PROJ_REQUEST_NAME, BaseAction.PROJECT_LABOR_ASSET_PDF, BaseAction.MIME_PDF, fileName, projFolder.id, null);
                break;
            case BaseAction.PHOTOS_FOLDER_NAME:
                newFileObj = new NewFileObj(BaseAction.PHOTOS_FOLDER_NAME, fileName, BaseAction.MIME_JPEG, fileName, projFolder.id, null);
                break;
            case BaseAction.NOTES_FOLDER_NAME:
                newFileObj = new NewFileObj(BaseAction.NOTES_FOLDER_NAME, BaseAction.NOTES_ASSET_PDF, BaseAction.MIME_PDF, fileName, projFolder.id, null);
                break;
            default:
                break;
        }

        dismissDialogAndEdit(newFileObj);
    }


    //endregion

    //region Helper----------------------
    private void dismissDialogAndEdit(NewFileObj newFileObj) {
        if (newFileObj != null) {
            newFileObj.projTitle = projFolder.title;
            addFileInterface.addItemClicked(newFileObj);
        }

        UtilKeyboard.hideKeyboard(getActivity(), fileNameEdit, fileNameEdit);
        dismiss();
    }
    //endregion
}

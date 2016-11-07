package toidiu.com.fieldnotebook.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.edisonwang.ps.annotations.EventListener;
import com.edisonwang.ps.lib.PennStation;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.actions.ArchiveFileAction;
import toidiu.com.fieldnotebook.actions.ArchiveFileAction_.PsArchiveFileAction;
import toidiu.com.fieldnotebook.actions.DeleteFileAction;
import toidiu.com.fieldnotebook.actions.DeleteFileAction_.PsDeleteFileAction;
import toidiu.com.fieldnotebook.actions.DownloadFileAction;
import toidiu.com.fieldnotebook.actions.DownloadFileAction.ActionEnum;
import toidiu.com.fieldnotebook.actions.DownloadFileAction_.PsDownloadFileAction;
import toidiu.com.fieldnotebook.actions.DownloadMultiFileAction;
import toidiu.com.fieldnotebook.actions.DownloadMultiFileAction_.PsDownloadMultiFileAction;
import toidiu.com.fieldnotebook.actions.DuplicateFileAction;
import toidiu.com.fieldnotebook.actions.DuplicateFileAction_.PsDuplicateFileAction;
import toidiu.com.fieldnotebook.actions.DwgConversionAction;
import toidiu.com.fieldnotebook.actions.FindFolderChildrenAction;
import toidiu.com.fieldnotebook.actions.FindFolderChildrenAction_.PsFindFolderChildrenAction;
import toidiu.com.fieldnotebook.actions.MergePdfAction;
import toidiu.com.fieldnotebook.actions.MergePdfAction_.PsMergePdfAction;
import toidiu.com.fieldnotebook.actions.MoveFileAction;
import toidiu.com.fieldnotebook.actions.MoveFileAction_.PsMoveFileAction;
import toidiu.com.fieldnotebook.actions.RenameFileAction;
import toidiu.com.fieldnotebook.actions.events.ArchiveFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.ArchiveFileActionSuccess;
import toidiu.com.fieldnotebook.actions.events.DeleteFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.DeleteFileActionSuccess;
import toidiu.com.fieldnotebook.actions.events.DownloadFileActionDwgConversion;
import toidiu.com.fieldnotebook.actions.events.DownloadFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.DownloadFileActionSuccess;
import toidiu.com.fieldnotebook.actions.events.DownloadMultiFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.DownloadMultiFileActionSuccess;
import toidiu.com.fieldnotebook.actions.events.DuplicateFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.DuplicateFileActionSuccess;
import toidiu.com.fieldnotebook.actions.events.DwgConversionActionFailure;
import toidiu.com.fieldnotebook.actions.events.DwgConversionActionSuccess;
import toidiu.com.fieldnotebook.actions.events.FindFolderChildrenActionFailure;
import toidiu.com.fieldnotebook.actions.events.FindFolderChildrenActionSuccess;
import toidiu.com.fieldnotebook.actions.events.MergePdfActionFailure;
import toidiu.com.fieldnotebook.actions.events.MergePdfActionSuccess;
import toidiu.com.fieldnotebook.actions.events.MoveFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.MoveFileActionPrime;
import toidiu.com.fieldnotebook.actions.events.MoveFileActionSuccess;
import toidiu.com.fieldnotebook.actions.events.RenameFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.RenameFileActionSuccess;
import toidiu.com.fieldnotebook.models.FileDownloadObj;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.models.LocalFileObj;
import toidiu.com.fieldnotebook.ui.extras.FileClickInterface;
import toidiu.com.fieldnotebook.ui.extras.GenericListAdapter;
import toidiu.com.fieldnotebook.ui.extras.ShareInterface;

@EventListener(producers = {
        FindFolderChildrenAction.class,
        MoveFileAction.class,
        RenameFileAction.class,
        ArchiveFileAction.class,
        DownloadFileAction.class,
        DwgConversionAction.class,
        MergePdfAction.class,
        DeleteFileAction.class,
        DuplicateFileAction.class,
        DownloadMultiFileAction.class
})
public class GenericFileListActivity extends BaseActivity implements FileClickInterface, ShareInterface {

    //region Fields----------------------
    //~=~=~=~=~=~=~=~=~=~=~=~=Constants
    public static final String FILE_OBJ = "FILE_OBJ";
    //~=~=~=~=~=~=~=~=~=~=~=~=View
    @Bind(R.id.recycler)
    RecyclerView recyclerView;
    @Bind(R.id.progress)
    ProgressBar progress;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    //~=~=~=~=~=~=~=~=~=~=~=~=Field
    private FileObj fileObj;
    private GenericListAdapter adapter;
    private Snackbar mergeSnackbar;
    private Menu menu;
    private String actionIdDelete;
    private String actionIdDuplicate;
    private String actionIdDownload;
    private String actionIdFileList;
    //region PennStation----------------------
    GenericFileListActivityEventListener eventListener = new GenericFileListActivityEventListener() {
        @Override
        public void onEventMainThread(ArchiveFileActionFailure event) {
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onEventMainThread(RenameFileActionSuccess event) {
            refreshFileList();
        }

        @Override
        public void onEventMainThread(RenameFileActionFailure event) {

        }

        @Override
        public void onEventMainThread(ArchiveFileActionSuccess event) {
            progress.setVisibility(View.GONE);
        }


        @Override
        public void onEventMainThread(FindFolderChildrenActionFailure event) {
            progress.setVisibility(View.GONE);
        }


        @Override
        public void onEventMainThread(FindFolderChildrenActionSuccess event) {
            progress.setVisibility(View.GONE);
            if (event.getResponseInfo().mRequestId.equals(actionIdFileList)) {
                adapter.refreshView(Arrays.asList(event.results));
            }
        }

        @Override
        public void onEventMainThread(MoveFileActionFailure event) {
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onEventMainThread(DownloadFileActionFailure event) {
            progress.setVisibility(View.GONE);
            Snackbar.make(progress, R.string.error_network, Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void onEventMainThread(DownloadFileActionSuccess event) {
            progress.setVisibility(View.GONE);
            if (event.getResponseInfo().mRequestId.equals(actionIdDownload)) {
                LocalFileObj localFileObj = event.localFileObj;
                if (event.ActionEnum.equals(ActionEnum.EDIT.name())) {
                    openLocalFile(localFileObj, progress);
                } else if (event.ActionEnum.equals(ActionEnum.SHARE.name())) {
                    shareIntentFile(localFileObj);
                } else if (event.ActionEnum.equals(ActionEnum.PRINT.name())) {
                    printIntentFile(localFileObj);
                }
            }
        }

        @Override
        public void onEventMainThread(DownloadFileActionDwgConversion event) {
            progress.setVisibility(View.GONE);
            Snackbar.make(progress, R.string.zamzar_started, Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void onEventMainThread(DeleteFileActionSuccess event) {
            progress.setVisibility(View.GONE);
            if (event.getResponseInfo().mRequestId.equals(actionIdDelete)) {
                refreshFileList();
            }
        }

        @Override
        public void onEventMainThread(DeleteFileActionFailure event) {
            progress.setVisibility(View.GONE);
            Snackbar.make(progress, R.string.error_network, Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void onEventMainThread(DwgConversionActionSuccess event) {
            Snackbar.make(progress, R.string.zamzar_success, Snackbar.LENGTH_SHORT).show();
            if (event.parentId.equals(fileObj.id)) {
                refreshFileList();
            }
        }

        @Override
        public void onEventMainThread(DuplicateFileActionSuccess event) {
            progress.setVisibility(View.GONE);
            if (event.getResponseInfo().mRequestId.equals(actionIdDuplicate)) {
                refreshFileList();
            }
        }

        @Override
        public void onEventMainThread(DuplicateFileActionFailure event) {
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onEventMainThread(DwgConversionActionFailure event) {
            Snackbar.make(progress, R.string.zamzar_failed, Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void onEventMainThread(MoveFileActionSuccess event) {
            progress.setVisibility(View.GONE);
            refreshMenu();
            refreshFileList();
        }

        @Override
        public void onEventMainThread(DownloadMultiFileActionFailure event) {
            progress.setVisibility(View.GONE);
            Snackbar.make(progress, R.string.error_network, Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void onEventMainThread(DownloadMultiFileActionSuccess event) {
            progress.setVisibility(View.GONE);
            multiShareHelper.shareMultiple.clear();
            adapter.notifyDataSetChanged();
            refreshMenu();
            shareIntentMultiFile(event.localFileObj);
        }

        @Override
        public void onEventMainThread(MoveFileActionPrime event) {
            refreshMenu();
        }

        @Override
        public void onEventMainThread(MergePdfActionSuccess event) {
            progress.setVisibility(View.GONE);
            openLocalFile(event.localFileObj, progress);
        }

        @Override
        public void onEventMainThread(MergePdfActionFailure event) {
            progress.setVisibility(View.GONE);
        }
    };

    //endregion
    //endregion

    //region Lifecycle----------------------
    public static Intent getInstance(Context context, FileObj fileObj) {
        Intent intent = new Intent(context, GenericFileListActivity.class);
        intent.putExtra(FILE_OBJ, fileObj);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_activity);
        ButterKnife.bind(this);

        fileObj = getIntent().getParcelableExtra(FILE_OBJ);

        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PennStation.registerListener(eventListener);

        if (renameFileHelper.isValid() && fileObj.id.equals(renameFileHelper.parentId)) {
            renameFileHelper.parentId = null;
            refreshFileList();
        }

        if (fileObj.id.equals(moveFolderHelper.initialParentId) && !moveFolderHelper.moveReady()) {
            refreshFileList();
            moveFolderHelper.resetState();
        }

        if (mergePfdHelper.isMerging) {
            mergeSnackbar = Snackbar.make(progress, "Choose PDF to merge into.", Snackbar.LENGTH_INDEFINITE).setAction("Cancel", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mergePfdHelper.isMerging = false;
                    mergeSnackbar.dismiss();
                    mergeSnackbar = null;
                }
            });
            mergeSnackbar.show();
        }
        refreshMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PennStation.unRegisterListener(eventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_generic, menu);
        this.menu = menu;
        refreshMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_paste:
                PennStation.requestAction(PsMoveFileAction.helper(fileObj.id));
                progress.setVisibility(View.VISIBLE);
                return true;
            case R.id.menu_cancel_share:
                multiShareHelper.shareMultiple.clear();
                adapter.notifyDataSetChanged();
                refreshMenu();
                return true;
            case R.id.menu_multi_share:
                FileDownloadObj[] fileDlArr = new FileDownloadObj[multiShareHelper.shareMultiple.size()];
                int i = 0;
                for (FileObj f : multiShareHelper.shareMultiple) {
                    fileDlArr[i] = new FileDownloadObj(f.parent, f.id, f.title, f.mime);
                    i++;
                }

                PennStation.requestAction(PsDownloadMultiFileAction.helper(fileDlArr));
                progress.setVisibility(View.VISIBLE);
                return true;
            case R.id.menu_archive:
                PennStation.requestAction(PsArchiveFileAction.helper(fileObj.id));
                progress.setVisibility(View.VISIBLE);
                return true;
            default:
                // If we got here, the user's ActionEnum was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //region Init----------------------
    private void initData() {
        refreshFileList();
    }

    //endregion

    private void initView() {
        toolbar.setTitle(fileObj.title);
        setSupportActionBar(toolbar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new GenericListAdapter(this, multiShareHelper.shareMultiple);
        recyclerView.setAdapter(adapter);
    }

    //region View----------------------
    private void refreshMenu() {
        if (menu != null) {
            if (moveFolderHelper.moveReady()) {
                menu.findItem(R.id.menu_paste).setVisible(true);
            } else {
                menu.findItem(R.id.menu_paste).setVisible(false);
            }
            if (multiShareHelper.shareMultiple.size() > 0) {
                menu.findItem(R.id.menu_cancel_share).setVisible(true);
                menu.findItem(R.id.menu_multi_share).setVisible(true);
            } else {
                menu.findItem(R.id.menu_cancel_share).setVisible(false);
                menu.findItem(R.id.menu_multi_share).setVisible(false);
            }
        }
    }

    //endregion

    private void refreshFileList() {
        progress.setVisibility(View.VISIBLE);
        actionIdFileList = PennStation.requestAction(PsFindFolderChildrenAction.helper("", fileObj.id, false));
    }

    //region Interface----------------------
    @Override
    public void folderClicked(FileObj fileObj) {
        startActivity(GenericFileListActivity.getInstance(this, fileObj));
    }

    @Override
    public void renameLongClicked(FileObj fileObj) {
        DialogRenameFile.getInstance(fileObj).show(getSupportFragmentManager(), null);
    }

    @Override
    public void moveLongClicked(FileObj fileObj) {
        moveFolderHelper.startMove(fileObj.id, fileObj.parent);
        PennStation.postLocalEvent(new MoveFileActionPrime());
    }

    @Override
    public void deleteLongClicked(FileObj fileObj) {
        progress.setVisibility(View.VISIBLE);
        actionIdDelete = PennStation.requestAction(PsDeleteFileAction.helper(fileObj.id));
    }

    @Override
    public void shareClicked(FileObj fileObj) {
        dialogShareClicked(fileObj);
    }

    @Override
    public void multiShareClicked(FileObj fileObj) {
        multiShareHelper.shareMultiple.add(fileObj);
        adapter.notifyDataSetChanged();
        refreshMenu();
    }

    @Override
    public void printClicked(FileObj fileObj) {
        progress.setVisibility(View.VISIBLE);
        FileDownloadObj fileDownloadObj = new FileDownloadObj(fileObj.parent, fileObj.id, fileObj.title, fileObj.mime);
        actionIdDownload = PennStation.requestAction(PsDownloadFileAction.helper(fileDownloadObj, ActionEnum.PRINT.name()));
    }

    @Override
    public void duplicateClicked(FileObj fileObj) {
        progress.setVisibility(View.VISIBLE);
        actionIdDuplicate = PennStation.requestAction(PsDuplicateFileAction.helper(fileObj.id, fileObj.title));
    }

    @Override
    public void editClicked(FileObj fileObj) {
        if (multiShareHelper.shareMultiple.size() > 0) {
            if (multiShareHelper.shareMultiple.contains(fileObj)) {
                multiShareHelper.shareMultiple.remove(fileObj);
            } else {
                multiShareHelper.shareMultiple.add(fileObj);
            }
            adapter.notifyDataSetChanged();
            refreshMenu();
            return;
        }
        progress.setVisibility(View.VISIBLE);
        FileDownloadObj fileDownloadObj = new FileDownloadObj(fileObj.parent, fileObj.id, fileObj.title, fileObj.mime);
        actionIdDownload = PennStation.requestAction(PsDownloadFileAction.helper(fileDownloadObj, ActionEnum.EDIT.name()));
    }

    @Override
    public void doMerge() {
        progress.setVisibility(View.VISIBLE);
        PennStation.requestAction(PsMergePdfAction.helper(), longTaskQueue);
        mergePfdHelper.isMerging = false;
        mergeSnackbar.dismiss();
    }

    @Override
    public void dialogShareClicked(FileObj fileObj) {
        progress.setVisibility(View.VISIBLE);
        FileDownloadObj fileDownloadObj = new FileDownloadObj(fileObj.parent, fileObj.id, fileObj.title, fileObj.mime);
        actionIdDownload = PennStation.requestAction(PsDownloadFileAction.helper(fileDownloadObj, ActionEnum.SHARE.name()));
    }
    //endregion

}
package toidiu.com.fieldnotebook.ui;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.edisonwang.ps.annotations.EventListener;
import com.edisonwang.ps.lib.PennStation;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import toidiu.com.fieldnotebook.BuildConfig;
import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.actions.ArchiveFileAction;
import toidiu.com.fieldnotebook.actions.ArchiveFileAction_.PsArchiveFileAction;
import toidiu.com.fieldnotebook.actions.BaseAction;
import toidiu.com.fieldnotebook.actions.ClaimProjAction;
import toidiu.com.fieldnotebook.actions.ClaimProjAction_.PsClaimProjAction;
import toidiu.com.fieldnotebook.actions.DbAddNewFileAction;
import toidiu.com.fieldnotebook.actions.DbAddNewFileAction_.PsDbAddNewFileAction;
import toidiu.com.fieldnotebook.actions.DbFindClaimedProjListAction;
import toidiu.com.fieldnotebook.actions.DbFindClaimedProjListAction_.PsDbFindClaimedProjListAction;
import toidiu.com.fieldnotebook.actions.DownloadFileAction;
import toidiu.com.fieldnotebook.actions.DownloadFileAction_.PsDownloadFileAction;
import toidiu.com.fieldnotebook.actions.FindFolderChildrenAction;
import toidiu.com.fieldnotebook.actions.FindFolderChildrenAction_.PsFindFolderChildrenAction;
import toidiu.com.fieldnotebook.actions.GetUserImgAction;
import toidiu.com.fieldnotebook.actions.GetUserImgAction_.PsGetUserImgAction;
import toidiu.com.fieldnotebook.actions.MergePdfAction;
import toidiu.com.fieldnotebook.actions.MoveFileAction;
import toidiu.com.fieldnotebook.actions.MoveFileAction_.PsMoveFileAction;
import toidiu.com.fieldnotebook.actions.RenameFileAction;
import toidiu.com.fieldnotebook.actions.UnClaimProjAction;
import toidiu.com.fieldnotebook.actions.UnClaimProjAction_.PsUnClaimProjAction;
import toidiu.com.fieldnotebook.actions.events.ArchiveFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.ArchiveFileActionSuccess;
import toidiu.com.fieldnotebook.actions.events.ClaimProjActionFailure;
import toidiu.com.fieldnotebook.actions.events.ClaimProjActionSuccess;
import toidiu.com.fieldnotebook.actions.events.DbAddNewFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.DbAddNewFileActionSuccess;
import toidiu.com.fieldnotebook.actions.events.DbFindClaimedProjListActionFailure;
import toidiu.com.fieldnotebook.actions.events.DbFindClaimedProjListActionSuccess;
import toidiu.com.fieldnotebook.actions.events.DownloadFileActionDwgConversion;
import toidiu.com.fieldnotebook.actions.events.DownloadFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.DownloadFileActionSuccess;
import toidiu.com.fieldnotebook.actions.events.FindFolderChildrenActionFailure;
import toidiu.com.fieldnotebook.actions.events.FindFolderChildrenActionSuccess;
import toidiu.com.fieldnotebook.actions.events.GetUserImgActionFailure;
import toidiu.com.fieldnotebook.actions.events.GetUserImgActionNoFile;
import toidiu.com.fieldnotebook.actions.events.GetUserImgActionSuccess;
import toidiu.com.fieldnotebook.actions.events.MergePdfActionFailure;
import toidiu.com.fieldnotebook.actions.events.MergePdfActionSuccess;
import toidiu.com.fieldnotebook.actions.events.MoveFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.MoveFileActionPrime;
import toidiu.com.fieldnotebook.actions.events.MoveFileActionSuccess;
import toidiu.com.fieldnotebook.actions.events.RenameFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.RenameFileActionSuccess;
import toidiu.com.fieldnotebook.actions.events.UnClaimProjActionFailure;
import toidiu.com.fieldnotebook.actions.events.UnClaimProjActionSuccess;
import toidiu.com.fieldnotebook.models.FileDownloadObj;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.models.LocalFileObj;
import toidiu.com.fieldnotebook.models.NewFileObj;
import toidiu.com.fieldnotebook.ui.extras.ArchiveInterface;
import toidiu.com.fieldnotebook.ui.extras.FabAddFileInterface;
import toidiu.com.fieldnotebook.ui.extras.FileClickInterface;
import toidiu.com.fieldnotebook.ui.extras.ProjDetailAdapter;
import toidiu.com.fieldnotebook.utils.UtilFile;
import toidiu.com.fieldnotebook.utils.UtilImage;
import toidiu.com.fieldnotebook.utils.UtilKeyboard;
import toidiu.com.fieldnotebook.views.SpaceItemDecoration;

@EventListener(producers = {
        FindFolderChildrenAction.class,
        ClaimProjAction.class,
        UnClaimProjAction.class,
        MoveFileAction.class,
        RenameFileAction.class,
        ArchiveFileAction.class,
        GetUserImgAction.class,
        DbAddNewFileAction.class,
        DownloadFileAction.class,
        MergePdfAction.class,
        DbFindClaimedProjListAction.class
})

public class ProjDetailActivity extends BaseActivity implements FileClickInterface, FabAddFileInterface, ArchiveInterface {

    //region Fields----------------------
    //~=~=~=~=~=~=~=~=~=~=~=~=Constants
    public static final String FILE_OBJ = "FILE_OBJ";
    public static final int REQ_CODE_USER_IMG = 23969;
    public static final int REQ_CODE_NEW_IMG = 23970;
    public static final String saveStateImageUri = "saveStateImageUri";
    public static final String saveStateNewFileObj = "saveStateNewFileObj";
    //~=~=~=~=~=~=~=~=~=~=~=~=View
    @Bind(R.id.pm_name)
    TextView pmNameView;
    @Bind(R.id.cliam_btn)
    TextView claimBtn;
    @Bind(R.id.recycler)
    RecyclerView recyclerView;
    @Bind(R.id.progress)
    ProgressBar progress;
    @Bind(R.id.proj_title)
    TextView projTitle;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.profile_img)
    CircleImageView profileImg;
    //~=~=~=~=~=~=~=~=~=~=~=~=Field
    private String actionIdClaimedList;
    private boolean projOwnedByMe = false;
    private String actionIdDownload;
    private FileObj fileObj;
    private ProjDetailAdapter adapter;
    private Uri imagePickerUri;
    private NewFileObj newFileObj;
    private Menu menu;
    private String actionIdFileList;
    //region PennStation----------------------
    ProjDetailActivityEventListener eventListener = new ProjDetailActivityEventListener() {
        @Override
        public void onEventMainThread(DbAddNewFileActionSuccess event) {
            progress.setVisibility(View.GONE);
            NewFileObj newFileObj = event.newFileObj;

            if (!newFileObj.parentName.equals(BaseAction.PHOTOS_FOLDER_NAME)) {
                LocalFileObj localFileObj = new LocalFileObj(newFileObj.title, newFileObj.mime, newFileObj.localFilePath);
                openLocalFile(localFileObj, progress);
            }
        }

        @Override
        public void onEventMainThread(DbAddNewFileActionFailure event) {
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onEventMainThread(ArchiveFileActionFailure event) {
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onEventMainThread(ArchiveFileActionSuccess event) {
            progress.setVisibility(View.GONE);
            archiveFileHelper.wasArhived = true;
            finish();
        }

        @Override
        public void onEventMainThread(MoveFileActionPrime event) {
            refreshMenu();
        }

        @Override
        public void onEventMainThread(MoveFileActionSuccess event) {
            refreshFileList();
            moveFolderHelper.resetState();
            refreshMenu();
        }

        @Override
        public void onEventMainThread(MoveFileActionFailure event) {

        }

        @Override
        public void onEventMainThread(DbFindClaimedProjListActionFailure event) {
        }

        @Override
        public void onEventMainThread(DbFindClaimedProjListActionSuccess event) {
            if (event.getResponseInfo().mRequestId.equals(actionIdClaimedList)) {
                for (FileObj f : event.results) {
                    if (f.id.equals(fileObj.id)) {
                        projOwnedByMe = true;
                        refreshMenu();
                        break;
                    }
                }
            }
        }

        @Override
        public void onEventMainThread(UnClaimProjActionSuccess event) {
            progress.setVisibility(View.GONE);
            claimBtn.setVisibility(View.VISIBLE);
            fileObj.claimUser = null;
            pmNameView.setText("");
            projOwnedByMe = false;
            refreshMenu();
            claimChangedFileHelper.wasClaimedChanged = true;
        }

        @Override
        public void onEventMainThread(UnClaimProjActionFailure event) {
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onEventMainThread(ClaimProjActionSuccess event) {
            progress.setVisibility(View.GONE);
            claimBtn.setVisibility(View.GONE);
            fileObj.claimUser = event.claimUser;
            pmNameView.setText(fileObj.claimUser);
            projOwnedByMe = true;
            refreshMenu();
            claimChangedFileHelper.wasClaimedChanged = true;
        }

        @Override
        public void onEventMainThread(ClaimProjActionFailure event) {
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
        public void onEventMainThread(FindFolderChildrenActionFailure event) {
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onEventMainThread(FindFolderChildrenActionSuccess event) {
            progress.setVisibility(View.GONE);
            if (event.getResponseInfo().mRequestId.equals(actionIdFileList)) {
                adapter.refreshView(Arrays.asList(event.results));
            }
            if (mergePfdHelper.isMerging) {
                navigateToFabFolder();
            }
        }

        @Override
        public void onEventMainThread(GetUserImgActionFailure event) {
        }

        @Override
        public void onEventMainThread(GetUserImgActionNoFile event) {

        }

        @Override
        public void onEventMainThread(GetUserImgActionSuccess event) {
            if (event.userName.equals(fileObj.claimUser)) {
                invalidateAndSetUserImage();
            }
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
                if (event.ActionEnum.equals(DownloadFileAction.ActionEnum.EDIT.name())) {
                    openLocalFile(localFileObj, progress);
                } else if (event.ActionEnum.equals(DownloadFileAction.ActionEnum.SHARE.name())) {
                    shareIntentFile(localFileObj);
                } else if (event.ActionEnum.equals(DownloadFileAction.ActionEnum.PRINT.name())) {
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
        Intent intent = new Intent(context, ProjDetailActivity.class);
        intent.putExtra(FILE_OBJ, fileObj);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.proj_detail_activity);
        ButterKnife.bind(this);

        fileObj = getIntent().getParcelableExtra(FILE_OBJ);

        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PennStation.registerListener(eventListener);

        if (fileObj.parent.equals(moveFolderHelper.initialParentId) && !moveFolderHelper.moveReady()) {
            refreshFileList();
            moveFolderHelper.resetState();
        }

        refreshMenu();
        if (mergePfdHelper.isMerging) {
            navigateToFabFolder();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PennStation.unRegisterListener(eventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
            case R.id.menu_archive:
                DialogConfirmArchive.getInstance().show(getSupportFragmentManager(), null);
                return true;
            case R.id.menu_claim:
                claimProject();
                return true;
            case R.id.menu_unclaim:
                unclaimProject();
                return true;
            default:
                // If we got here, the user's ActionEnum was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_CODE_USER_IMG) {
                //----------------
                String uriString = UtilImage.getImageUriFromIntent(data, imagePickerUri);
                startActivityForResult(UserImageCropActivity.getInstance(this, uriString), UserImageCropActivity.RESULT_CODE);

            } else if (requestCode == UserImageCropActivity.RESULT_CODE) {
                //----------------
                invalidateAndSetUserImage();
            } else if (requestCode == REQ_CODE_NEW_IMG) {
                //----------------

                String uriString = null;
                File file = new File(imagePickerUri.getPath());
                if (data == null && !file.exists()) {
                    Snackbar.make(progress, "Sorry, there was an error while retrieving the image.", Snackbar.LENGTH_SHORT).show();
                    return;
                } else if (data != null) {
                    uriString = UtilImage.getImageUriFromIntent(data, imagePickerUri);
                }

                if (uriString != null && uriString.startsWith("content")) {
                    //save content media to external storage
                    try {
                        InputStream source = getContentResolver().openInputStream(Uri.parse(uriString));
                        org.apache.commons.io.FileUtils.copyInputStreamToFile(source, new File(imagePickerUri.getPath()));
                    } catch (IOException e) {
                        Crashlytics.getInstance().core.logException(e);
                    }
                }

                newFileObj.localFilePath = imagePickerUri.getPath();
                progress.setVisibility(View.VISIBLE);
                PennStation.requestAction(PsDbAddNewFileAction.helper(newFileObj));

            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imagePickerUri != null) {
            outState.putString(saveStateImageUri, imagePickerUri.toString());
        }
        if (newFileObj!=null){
            outState.putParcelable(saveStateNewFileObj, newFileObj);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(saveStateImageUri)) {
            imagePickerUri = Uri.parse(savedInstanceState.getString(saveStateImageUri));
        }
        if (savedInstanceState.containsKey(saveStateNewFileObj)) {
            newFileObj = savedInstanceState.getParcelable(saveStateNewFileObj);
        }
    }

    //endregion

    //region Init----------------------
    private void initData() {
        actionIdClaimedList = PennStation.requestAction(PsDbFindClaimedProjListAction.helper());
        refreshFileList();
    }

    private void initView() {
        projTitle.setText(fileObj.title);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (fileObj.claimUser != null && !fileObj.claimUser.equals("")) {
            claimBtn.setVisibility(View.GONE);
            pmNameView.setText(fileObj.claimUser);
            refreshMenu();
        }

        claimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                claimProject();
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.proj_detail_spacing);
        recyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));

        adapter = new ProjDetailAdapter(this);
        recyclerView.setAdapter(adapter);

        PennStation.requestAction(PsGetUserImgAction.helper(fileObj.claimUser));
        Picasso.with(this).load(UtilImage.getAvatarFile(this, fileObj.claimUser)).placeholder(R.drawable.ic_profile_image).into(profileImg);
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileObj.claimUser.equals(prefs.getUser())) {
                    File externalFile = UtilImage.getTempFile(ProjDetailActivity.this);
                    choosePicture(REQ_CODE_USER_IMG, externalFile);
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogAddFile.getInstance(fileObj).show(getSupportFragmentManager(), null);
            }
        });
    }
    //endregion

    //region View----------------------
    private void refreshMenu() {
        if (menu != null) {
            if (moveFolderHelper.moveReady()) {
                menu.findItem(R.id.menu_paste).setVisible(true);
            } else {
                menu.findItem(R.id.menu_paste).setVisible(false);
            }
            menu.findItem(R.id.menu_archive).setVisible(true);

            if (fileObj.claimUser != null && !fileObj.claimUser.equals("")) {
                menu.findItem(R.id.menu_claim).setVisible(true);
            }

            if (projOwnedByMe) {
                menu.findItem(R.id.menu_unclaim).setVisible(true);
            }else {
                menu.findItem(R.id.menu_unclaim).setVisible(false);
            }
        }
    }

    private void refreshFileList() {
        progress.setVisibility(View.VISIBLE);
        actionIdFileList = PennStation.requestAction(PsFindFolderChildrenAction.helper("", fileObj.id, false));
    }

    private void invalidateAndSetUserImage() {
        Picasso.with(this).invalidate(UtilImage.getAvatarFile(this, fileObj.claimUser));
        Picasso.with(this).load(UtilImage.getAvatarFile(this, fileObj.claimUser))
                .placeholder(R.drawable.ic_profile_image)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(profileImg);
    }
    //endregion

    //region Helper----------------------
    private void claimProject() {
        PennStation.requestAction(PsClaimProjAction.helper(fileObj.id));
        progress.setVisibility(View.VISIBLE);
    }

    private void unclaimProject() {
        PennStation.requestAction(PsUnClaimProjAction.helper(fileObj.id));
        progress.setVisibility(View.VISIBLE);
    }

    private void navigateToFabFolder() {
        if (adapter != null) {
            List<FileObj> list = adapter.getList();
            for (FileObj file : list) {
                if (file.title.matches("(?i)fab sheet.*")) {
                    startActivity(GenericFileListActivity.getInstance(this, file));
                    break;
                }
                Snackbar.make(progress, "No Fab Sheet folder found. Cancelling merge action.", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    public void choosePicture(int requestCode, File externalFile) {
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(externalFile));
        imagePickerUri = Uri.parse(externalFile.getAbsolutePath());
        if (BuildConfig.DEBUG) {
            Log.d("photo location", imagePickerUri.toString());
        }

        String pickTitle = getString(R.string.select_picture);
        Intent chooserIntent = Intent.createChooser(takePhotoIntent, pickTitle);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, requestCode);
    }
    //endregion


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
        //this should only happend in the GenericFileListActivity
    }

    @Override
    public void editClicked(FileObj fileObj) {
        progress.setVisibility(View.VISIBLE);
        FileDownloadObj fileDownloadObj = new FileDownloadObj(fileObj.parent, fileObj.id, fileObj.title, fileObj.mime);
        actionIdDownload = PennStation.requestAction(PsDownloadFileAction.helper(fileDownloadObj, DownloadFileAction.ActionEnum.EDIT.name()));
    }

    @Override
    public void doMerge() {
        //this should only happend in the GenericFileListActivity
    }

    @Override
    public void shareClicked(FileObj fileObj) {
        progress.setVisibility(View.VISIBLE);
        FileDownloadObj fileDownloadObj = new FileDownloadObj(fileObj.parent, fileObj.id, fileObj.title, fileObj.mime);
        actionIdDownload = PennStation.requestAction(PsDownloadFileAction.helper(fileDownloadObj, DownloadFileAction.ActionEnum.SHARE.name()));
    }

    @Override
    public void multiShareClicked(FileObj fileObj) {
        //this should only happend in the GenericFileListActivity
    }

    @Override
    public void printClicked(FileObj fileObj) {
        progress.setVisibility(View.VISIBLE);
        FileDownloadObj fileDownloadObj = new FileDownloadObj(fileObj.parent, fileObj.id, fileObj.title, fileObj.mime);
        actionIdDownload = PennStation.requestAction(PsDownloadFileAction.helper(fileDownloadObj, DownloadFileAction.ActionEnum.PRINT.name()));
    }

    @Override
    public void duplicateClicked(FileObj fileObj) {
        //this should only happend in the GenericFileListActivity
    }

    @Override
    public void addItemClicked(NewFileObj newFileObj) {
        this.newFileObj = newFileObj;
        if (newFileObj.parentName.equals(BaseAction.PHOTOS_FOLDER_NAME)) {
            //get photo
            String fileNAme = newFileObj.title + System.currentTimeMillis();
            File localFile = UtilFile.getLocalFileWithExtension(fileNAme, BaseAction.MIME_JPEG);
            choosePicture(REQ_CODE_NEW_IMG, localFile);
        } else {
            progress.setVisibility(View.VISIBLE);
            PennStation.requestAction(PsDbAddNewFileAction.helper(newFileObj));
        }
    }

    @Override
    public void mergePdfClicked() {
        UtilKeyboard.hideKeyboard(this, progress, null);
        navigateToFabFolder();
    }

    @Override
    public void openPoPdfClicked(FileObj fileObj) {
        progress.setVisibility(View.VISIBLE);
        FileDownloadObj fileDownloadObj = new FileDownloadObj(fileObj.parent, fileObj.id, fileObj.title, fileObj.mime);
        actionIdDownload = PennStation.requestAction(PsDownloadFileAction.helper(fileDownloadObj, DownloadFileAction.ActionEnum.EDIT.name()));
    }

    @Override
    public void archiveClicked() {
        PennStation.requestAction(PsArchiveFileAction.helper(fileObj.id));
        progress.setVisibility(View.VISIBLE);
    }
    //endregion

}
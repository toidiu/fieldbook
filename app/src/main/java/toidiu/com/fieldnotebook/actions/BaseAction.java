package toidiu.com.fieldnotebook.actions;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.edisonwang.ps.annotations.ActionHelper;
import com.edisonwang.ps.annotations.Event;
import com.edisonwang.ps.annotations.EventProducer;
import com.edisonwang.ps.lib.ActionRequest;
import com.edisonwang.ps.lib.ActionResult;
import com.edisonwang.ps.lib.FullAction;
import com.edisonwang.ps.lib.LimitedQueueInfo;
import com.edisonwang.ps.lib.RequestEnv;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Property;
import com.j256.ormlite.dao.Dao;

import org.apache.commons.io.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import toidiu.com.fieldnotebook.FieldNotebookApplication;
import toidiu.com.fieldnotebook.database.DatabaseHelper;
import toidiu.com.fieldnotebook.models.FileDownloadObj;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.models.FileUploadObj;
import toidiu.com.fieldnotebook.models.NewFileObj;
import toidiu.com.fieldnotebook.utils.MergePfdHelper;
import toidiu.com.fieldnotebook.utils.MoveFolderHelper;
import toidiu.com.fieldnotebook.utils.Prefs;
import toidiu.com.fieldnotebook.utils.RenameFileHelper;
import toidiu.com.fieldnotebook.utils.UtilFile;

/**
 * Created by toidiu on 3/28/16.
 */
@ActionHelper
@EventProducer(generated = {
        @Event
})
@com.edisonwang.ps.annotations.Action
public class BaseAction extends FullAction {

    //~=~=~=~=~=~=~=~=~=~=~=~=Constants
    public static final String CLAIM_PROPERTY = "claim";
    public static final String PO_NUMBER_PROPERTY = "po_number";
    public static final String PUBLIC_VISIBILITY = "PUBLIC";

    public static final String DEMO_FOLDER_SETUP = "Fieldbook Demo";
    public static final String ARCHIVE_FOLDER_SETUP = "Archive";
    public static final String PHOTOS_FOLDER_SETUP = "Photos";
    public static final String PO_NUMBER_FOLDER_SETUP = "PoNum - DO NOT DELETE";

    public static final String APPFOLDER_ID = "appfolder";

    public static final String PURCHASE_FOLDER_NAME = "Purchase Orders";
    public static final String PROJ_REQUEST_NAME = "Project Labour Request";
    public static final String FAB_FOLDER_NAME = "Fab Sheets";
    public static final String NOTES_FOLDER_NAME = "Notes";
    public static final String PHOTOS_FOLDER_NAME = "Photos";

    public static final String PURCHASE_ORDER_ASSET_PDF = "PurchaseOrder.pdf";
    public static final String FAB_SHEET_ASSET_PDF = "FabSheet.pdf";
    public static final String PROJECT_LABOR_ASSET_PDF = "ProjectLaborRequest.pdf";
    public static final String NOTES_ASSET_PDF = "Notes.pdf";

    public static final String FOLDER_MIME = "application/vnd.google-apps.folder";

    //    public static final String QUERY_FIELDS = "title";
    public static final String MIME_JPEG = "image/jpeg";
    public static final String MIME_PNG = "image/png";
    public static final String MIME_PDF = "application/pdf";
    public static final String MIME_DWG1 = "application/acad";
    public static final String MIME_DWG2 = "image/vnd.dwg";

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    @Inject
    Context context;
    @Inject
    Prefs prefs;
    @Inject
    GoogleAccountCredential credential;
    @Inject
    Drive driveService;
    @Inject
    MoveFolderHelper moveFolderHelper;
    @Inject
    RenameFileHelper renameFileHelper;
    @Inject
    DatabaseHelper databaseHelper;
    @Inject
    LimitedQueueInfo longTaskQueue;
    @Inject
    MergePfdHelper mergePfdHelper;

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        //do nothing
        return null;
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        //do nothing
        return null;
    }

    @Override
    protected ActionResult preProcess(Context context, ActionRequest request, RequestEnv env) {
        ((FieldNotebookApplication) FieldNotebookApplication.appContext).getAppComponent().inject(this);

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        driveService = new Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("SJ")
                .build();

        return super.preProcess(context, request, env);
    }

    //region Folder Helper----------------------
    protected List<File> executeQueryList(String search) throws IOException {


        FileList result = driveService.files().list()
                .setQ(search)
                .setOrderBy("modifiedDate desc")
                .setMaxResults(300)
                .execute();
        List<File> files = result.getItems();


        return files;
    }

    public List<FileObj> toFileObjs(List<File> files) {
        List<FileObj> retFile = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                FileObj object = new FileObj(f);
                retFile.add(object);
            }
        }
        return retFile;
    }

    protected List<FileObj> getFoldersByName(String folderName, String baseFolderId) {
        String search = "title = '" + folderName + "'"
                + " and " + "'" + baseFolderId + "'" + " in parents"
                + " and " + "mimeType = '" + FOLDER_MIME + "'";

        List<FileObj> dataFromApi = new ArrayList<>();
        try {
            dataFromApi = toFileObjs(executeQueryList(search));
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return dataFromApi;
    }

    protected List<FileObj> getFoldersByNameFuzzy(String folderName, String baseFolderId) {
        String search = "title contains '" + folderName + "'"
                + " and " + "'" + baseFolderId + "'" + " in parents"
                + " and " + "mimeType = '" + FOLDER_MIME + "'";

        List<FileObj> dataFromApi = new ArrayList<>();
        try {
            dataFromApi = toFileObjs(executeQueryList(search));
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return dataFromApi;
    }

    protected boolean claimProj(String fileId) {
        File fileMetadata = new File();
        List<Property> pp = new ArrayList<>();
        Property property = new Property();
        property.setKey(CLAIM_PROPERTY).setValue(credential.getSelectedAccountName());
        pp.add(property);
        fileMetadata.setProperties(pp);

        try {
            File file = driveService.files().update(fileId, fileMetadata)
                    .execute();
            FileObj fileObj = new FileObj(file);

            Dao<FileObj, Integer> dao = databaseHelper.getClaimProjDao();
            List<FileObj> fileObjList = dao.queryForEq("id", fileObj.id);
            if (fileObjList.isEmpty()) {
                dao.create(fileObj);
            }
            return true;
        } catch (Exception e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return false;
    }

    protected boolean unclaimProj(String fileId) {
        File fileMetadata = new File();
        List<Property> pp = new ArrayList<>();
        Property property = new Property();
        property.setKey(CLAIM_PROPERTY).setValue("");
        pp.add(property);
        fileMetadata.setProperties(pp);

        try {
            File file = driveService.files().update(fileId, fileMetadata)
                    .execute();
            Dao<FileObj, Integer> dao = databaseHelper.getClaimProjDao();

            FileObj fileDb = dao.queryForEq("id", fileId).get(0);
            dao.deleteById(fileDb.dbId);

            return true;
        } catch (Exception e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return false;
    }

    @NonNull
    protected boolean checkAndCreateArchive(List<ParentReference> parents, String parentId) {
        List<FileObj> dataFromApi = getFoldersByName(ARCHIVE_FOLDER_SETUP, parentId);

        if (dataFromApi.size() == 0) {
            File archive = new File();
            archive.setTitle(ARCHIVE_FOLDER_SETUP);
            archive.setMimeType(FOLDER_MIME);
            archive.setParents(parents);

            try {
                File file = driveService.files().insert(archive)
                        .execute();
                prefs.setArchiveFolderId(file.getId());
                return true;
            } catch (IOException e) {
                Crashlytics.getInstance().core.logException(e);
                return false;
            }
        } else {
            prefs.setArchiveFolderId(dataFromApi.get(0).id);
            return true;
        }
    }

    @NonNull
    protected boolean checkAndCreatePhotos(List<ParentReference> parents, String parentId) {
        List<FileObj> dataFromApi = getFoldersByName(PHOTOS_FOLDER_SETUP, parentId);

        if (dataFromApi.size() == 0) {
            File photos = new File();
            photos.setTitle(PHOTOS_FOLDER_SETUP);
            photos.setMimeType(FOLDER_MIME);
            photos.setParents(parents);

            try {
                File file = driveService.files().insert(photos).execute();
                prefs.setPhotosFolderId(file.getId());
                return true;
            } catch (IOException e) {
                Crashlytics.getInstance().core.logException(e);
                return false;
            }
        } else {
            prefs.setPhotosFolderId(dataFromApi.get(0).id);
            return true;
        }
    }


    @NonNull
    protected boolean checkAndCreatePONumber(List<ParentReference> parents, String parentId) {
        List<FileObj> dataFromApi = getFoldersByName(PO_NUMBER_FOLDER_SETUP, parentId);

        if (dataFromApi.size() == 0) {
            File ponumber = new File();
            ponumber.setTitle(PO_NUMBER_FOLDER_SETUP);
            ponumber.setMimeType(FOLDER_MIME);
            ponumber.setParents(parents);

            List<Property> pp = new ArrayList<>();
            Property property = new Property();
            property.setKey(PO_NUMBER_PROPERTY).setValue("100000");
            property.setVisibility(PUBLIC_VISIBILITY);
            pp.add(property);
            ponumber.setProperties(pp);


            try {
                File file = driveService.files().insert(ponumber).execute();
                prefs.setPoNumberFolderId(file.getId());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                Crashlytics.getInstance().core.logException(e);
                return false;
            }
        } else {
            prefs.setPoNumberFolderId(dataFromApi.get(0).id);
            return true;
        }
    }

    protected String demoCheckAndCreateBase() {
        List<FileObj> dataFromApi = getFoldersByName(DEMO_FOLDER_SETUP, "root");
        String baseId = null;
        if (dataFromApi.size() == 0) {
            File archive = new File();
            archive.setTitle(DEMO_FOLDER_SETUP);
            archive.setMimeType(FOLDER_MIME);

            try {
                File file = driveService.files().insert(archive).execute();
                prefs.setBaseFolderId(file.getId());
                baseId = file.getId();
            } catch (IOException e) {
                Crashlytics.getInstance().core.logException(e);
            }
        } else {
            prefs.setBaseFolderId(dataFromApi.get(0).id);
            baseId = dataFromApi.get(0).id;
        }

        //create demo projects
        List<ParentReference> parents = new ArrayList<>();
        parents.add(new ParentReference().setId(baseId));
        demoCheckAndCreateProject("#0001-CompanyA", parents);
        demoCheckAndCreateProject("#0002-CompanyB", parents);

        return baseId;
    }

    protected String demoCheckAndCreateProject(String folderName, List<ParentReference> parents) {
        List<FileObj> dataFromApi = getFoldersByName(folderName, parents.get(0).getId());
//
        if (dataFromApi.size() == 0) {
            File folder = new File();
            folder.setTitle(folderName);
            folder.setMimeType(FOLDER_MIME);
            folder.setParents(parents);

            try {
                File file = driveService.files().insert(folder).execute();
                prefs.setBaseFolderId(file.getId());

                //create sub folder
                List<ParentReference> subParents = new ArrayList<>();
                subParents.add(new ParentReference().setId(file.getId()));
                demoCheckAndCreateSubFolders("Notes", subParents);
                demoCheckAndCreateSubFolders("Fab Sheets", subParents);
                demoCheckAndCreateSubFolders("Purchase Orders", subParents);
                demoCheckAndCreateSubFolders("Project Labour Request", subParents);
                demoCheckAndCreateSubFolders("Billing", subParents);
                demoCheckAndCreateSubFolders("Photos", subParents);

                return file.getId();
            } catch (IOException e) {
                Crashlytics.getInstance().core.logException(e);
                return null;
            }
        } else {
            prefs.setBaseFolderId(dataFromApi.get(0).id);
            return dataFromApi.get(0).id;
        }
    }

    protected String demoCheckAndCreateSubFolders(String folderName, List<ParentReference> parents) {
        List<FileObj> dataFromApi = getFoldersByName(folderName, parents.get(0).getId());

        if (dataFromApi.size() == 0) {
            File folder = new File();
            folder.setTitle(folderName);
            folder.setMimeType(FOLDER_MIME);
            folder.setParents(parents);

            try {
                File file = driveService.files().insert(folder).execute();
                prefs.setBaseFolderId(file.getId());
                return file.getId();
            } catch (IOException e) {
                Crashlytics.getInstance().core.logException(e);
                return null;
            }
        } else {
            prefs.setBaseFolderId(dataFromApi.get(0).id);
            return dataFromApi.get(0).id;
        }
    }

    //endregion

    //region File Helper----------------------

    protected FileObj getFileById(String fileId) {
        try {
            File file = driveService.files().get(fileId)
//                    .setFields(QUERY_FIELDS)
                    .execute();

            return new FileObj(file);
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return null;
        }

    }

    protected List<File> getFileByName(String fileName, String parentId) {
        String search = "title = '" + fileName + "'"
                + " and " + "'" + parentId + "'" + " in parents";

        List<File> dataFromApi = new ArrayList<>();
        try {
            dataFromApi = executeQueryList(search);
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return dataFromApi;
    }

    protected boolean fileMoved(String fileId, String newParentId) {
        try {
            // Retrieve the existing parents to remove
            File file = driveService.files().get(fileId)
//                    .setFields("parents")
                    .execute();

            StringBuilder previousParents = new StringBuilder();
            for (ParentReference parent : file.getParents()) {
                previousParents.append(parent);
                previousParents.append(',');
            }

            File file1 = new File();
            List<ParentReference> hh = new ArrayList<>();
            hh.add(new ParentReference().setId(newParentId));
            file1.setParents(hh);

            // Move the file to the new folder
            File execute = driveService.files().update(fileId, file1)
//                    .setAddParents(newParentId)
//                    .setRemoveParents(previousParents.toString())
//                    .setFields(QUERY_FIELDS)
                    .execute();

            return true;
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return false;
        }
    }

    @Nullable
    protected java.io.File downloadOrReturnExistingFile(FileDownloadObj fileDownloadObj, java.io.File localFile) {
        if (localFile == null) {
            return null;
        } else if (localFile.exists()) {
            return localFile;
        } else {
            //download it
            FileOutputStream fileOutputStream = null;
            try {
                java.io.File temp = UtilFile.getTempLocalFile(fileDownloadObj.mime);
                fileOutputStream = new FileOutputStream(temp);
                driveService.files().get(fileDownloadObj.fileId).executeMediaAndDownloadTo(fileOutputStream);
                fileOutputStream.close();

                if (temp.exists()) {
                    FileUtils.copyFile(temp, localFile);
                    temp.delete();
                }
                return localFile;
            } catch (IOException e) {
                Crashlytics.getInstance().core.logException(e);
                return null;
            }
        }
    }

    @Nullable
    protected java.io.File downloadUserImg(java.io.File avatarFile, String fileId) {
        //download it
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(avatarFile);
            driveService.files().get(fileId).executeMediaAndDownloadTo(fileOutputStream);
            return avatarFile;
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return null;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Crashlytics.getInstance().core.logException(e);
                }
            }
        }
    }

    @Nullable
    protected File createFile(FileUploadObj fileUploadObj) {
        //Local file
        java.io.File file = new java.io.File(fileUploadObj.localFilePath);
        if (!file.exists()) {
            Log.e("-------no file exists", "file : " + fileUploadObj.localFilePath);

            //Markme We should never be here and precautions should have been taken to prevent this
            return null;
        }
        FileContent mediaContent = new FileContent(fileUploadObj.mime, file);

        //Drive file
        List<String> parents = new ArrayList<>();
        parents.add(fileUploadObj.parentId);
        File fileMetadata = new File();
        fileMetadata.setTitle(fileUploadObj.fileName);
        fileMetadata.setMimeType(fileUploadObj.mime);

//        fileMetadata.setParents(parents);
        List<ParentReference> df = new ArrayList<>();
        df.add(new ParentReference().setId(fileUploadObj.parentId));
        fileMetadata.setParents(df);


        try {
            return driveService.files().insert(fileMetadata, mediaContent)
                    .execute();
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return null;
        }
    }

    @Nullable
    protected File replaceFile(FileUploadObj fileUploadObj) {
        //Local file
        java.io.File file = new java.io.File(fileUploadObj.localFilePath);
        if (!file.exists()) {
            return null;
        }
        FileContent mediaContent = new FileContent(fileUploadObj.mime, file);

        try {
            File execute = driveService.files()
                    .update(fileUploadObj.fileId, null, mediaContent)
                    .execute();


            return execute;
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return null;
        }
    }

    protected boolean deleteFile(String fileId) {
        try {
            driveService.files().delete(fileId).execute();
            return true;
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return false;
        }
    }

    protected File copyFile(String fileId, String duplicateTitle) {
        File copiedFile = new File();
        copiedFile.setTitle(duplicateTitle);
        try {
            return driveService.files().copy(fileId, copiedFile).execute();
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return null;
    }

    protected File renameFile(String id, String newName, String parent) {
        File fileMetadata = new File();
        fileMetadata.setTitle(newName);

        try {
            File execute = driveService.files().update(id, fileMetadata).execute();
            renameFileHelper.parentId = parent;
            return execute;
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return null;
        }
    }

    protected boolean uploadFile(@Nullable Dao<FileUploadObj, Integer> dao, FileUploadObj fileUploadObj) {
        //id fileId null
        File createdFile = null;
        if (fileUploadObj.fileId == null) {
            //yes
            Log.d("d----------", "uploadFile: 3");
            createdFile = createFile(fileUploadObj);
        } else {
            //no
            //check if file exists on drive
            FileObj fileById = getFileById(fileUploadObj.fileId);
            if (fileById != null) {
                //yes
                Log.d("d----------", "uploadFile: 2");
                createdFile = replaceFile(fileUploadObj);
            } else {
                //no
                createdFile = createFile(fileUploadObj);
                //fixme this might also return a not null value if it fails
                Log.d("d----------", "uploadFile: 1");
            }
        }

        if (createdFile != null) {
            return UtilFile.deleteLocalFile(new java.io.File(fileUploadObj.localFilePath));
        }

        return false;
    }

    protected File uploadAndReturnDriveFile(@Nullable Dao<FileUploadObj, Integer> dao, FileUploadObj fileUploadObj) {
        //id fileId null
        File createdFile = null;
        if (fileUploadObj.fileId == null) {
            //yes
            Log.d("d----------", "uploadFile: 3");
            createdFile = createFile(fileUploadObj);
        } else {
            //no
            //check if file exists on drive
            FileObj fileById = getFileById(fileUploadObj.fileId);
            if (fileById != null) {
                //yes
                Log.d("d----------", "uploadFile: 2");
                createdFile = replaceFile(fileUploadObj);
            } else {
                //no
                createdFile = createFile(fileUploadObj);
                //fixme this might also return a not null value if it fails
                Log.d("d----------", "uploadFile: 1");
            }
        }

        if (createdFile != null) {
            UtilFile.deleteLocalFile(new java.io.File(fileUploadObj.localFilePath));
        }

        return createdFile;
    }


//endregion


    //region Purchase Order Helper----------------------
    protected String getNextPurchaseOrderName(NewFileObj newFileObj, String biggest) throws IOException {
        assert newFileObj.projTitle != null;
        return getNextPOrderNumber(biggest) + "-" + newFileObj.title;
    }

    @NonNull
    protected String getNextPOrderNumber(String biggest) {
        return "e-" + biggest;
    }

    @Nullable
    protected String incrementAndGetPoNumber() {
        try {
            // First retrieve the property from the API.
            Drive.Properties.Get request = driveService.properties().get(prefs.getPoNumberFolderId(), PO_NUMBER_PROPERTY);
            request.setVisibility(PUBLIC_VISIBILITY);
            Property property = request.execute();
            Integer number = Integer.valueOf(property.getValue());

            //update and set new value
            Integer nextNum = number + 1;
            property.setValue(String.valueOf(nextNum));
            Drive.Properties.Update update = driveService.properties().update(prefs.getPoNumberFolderId(), PO_NUMBER_PROPERTY, property);
            update.setVisibility(PUBLIC_VISIBILITY);

            //make sure the value was updated
            String value = update.execute().getValue();
            if (nextNum == Integer.valueOf(value).intValue()) {
                return String.format("%02d", nextNum);
            } else {
                return null;
            }
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            System.out.println("An error occurred: " + e);
            return null;
        }
    }

    //endregion


}

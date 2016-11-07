package toidiu.com.fieldnotebook.actions;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.edisonwang.ps.annotations.Action;
import com.edisonwang.ps.annotations.ActionHelper;
import com.edisonwang.ps.annotations.Event;
import com.edisonwang.ps.annotations.EventProducer;
import com.edisonwang.ps.annotations.Kind;
import com.edisonwang.ps.annotations.ParcelableField;
import com.edisonwang.ps.lib.ActionRequest;
import com.edisonwang.ps.lib.ActionResult;
import com.edisonwang.ps.lib.RequestEnv;
import com.j256.ormlite.dao.Dao;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import toidiu.com.fieldnotebook.actions.events.UploadNewFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.UploadNewFileActionSuccess;
import toidiu.com.fieldnotebook.database.TransactionRunnable;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.models.FileUploadObj;
import toidiu.com.fieldnotebook.models.NewFileObj;
import toidiu.com.fieldnotebook.utils.UtilNetwork;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper()
@EventProducer(generated = {
        @Event(postFix = "Success"),
        @Event(postFix = "Failure", fields = {
                @ParcelableField(name = "errorMsg", kind = @Kind(clazz = String.class))
        })
})

public class UploadNewFileAction extends BaseAction {


    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        if (!UtilNetwork.isDeviceOnline(context)) {
            return new UploadNewFileActionFailure("No connection. Sync cancelled.");
        }

        databaseHelper.runInTransaction(new TransactionRunnable() {
            @Override
            public void run() throws Exception {


                //get items from DB
                Dao<NewFileObj, Integer> newFileObjDao = null;

                newFileObjDao = databaseHelper.getNewFileObjDao();
                List<NewFileObj> newFileObjs = newFileObjDao.queryForAll();


                //find parent folder, upload file, delete from db
                for (NewFileObj obj : newFileObjs) {
                    //find proper folder else put in base of project folder
                    String parentId;
                    List<FileObj> foldersByNameFuzzy = getFoldersByNameFuzzy(obj.parentName, obj.projId);
                    if (foldersByNameFuzzy != null && foldersByNameFuzzy.size() > 0) {
                        parentId = foldersByNameFuzzy.get(0).id;
                    } else {
                        parentId = obj.projId;
                    }

                    //                    //fixme remove this if we are handling the naming in GetNextPONumber
//                    if (obj.parentName.equals(BaseAction.PURCHASE_FOLDER_NAME)) {
//                        //get name of obj.projId folder to prepend to new file
//
//                        //get name of latest purchase order pdf
//                        if (foundBaseFolder) {
//                            obj.title = getNextPurchaseOrderName(obj, parentId);
//                        }
//                    }


                    //upload new file and then delete from db
                    FileUploadObj fileUploadObj = new FileUploadObj(parentId, null, obj.title, obj.localFilePath, obj.mime);
                    if (!new File(fileUploadObj.localFilePath).exists()) {
                        //Markme the file no longer exists so delete it
                        Crashlytics.getInstance().core.logException(new Exception("A file to be uploaded was deleted. " + obj.toString()));
                        Crashlytics.getInstance().core.logException(new Exception("Number of files in newFileObjDao still: " + newFileObjDao.queryForAll().size()));


                        newFileObjDao.deleteById(obj.dbId);
                    } else {
                        boolean uploaded = uploadFile(null, fileUploadObj);
                        if (uploaded) {
                            try {
                                newFileObjDao.deleteById(obj.dbId);
                            } catch (SQLException e) {
                                Crashlytics.getInstance().core.logException(e);
                            }
                        }else {
                            new UploadNewFileActionFailure("Upload failed. Please check your connection.");
                        }
                    }
                }


            }
        });

        return new UploadNewFileActionSuccess();
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        Crashlytics.getInstance().core.logException(e);
        return new UploadNewFileActionFailure("There was an error.");
    }
}

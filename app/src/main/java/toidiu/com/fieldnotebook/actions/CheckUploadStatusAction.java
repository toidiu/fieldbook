package toidiu.com.fieldnotebook.actions;

import android.content.Context;

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

import java.util.List;

import toidiu.com.fieldnotebook.actions.events.CheckUploadStatusActionFailure;
import toidiu.com.fieldnotebook.actions.events.CheckUploadStatusActionSuccess;
import toidiu.com.fieldnotebook.models.FileUploadObj;
import toidiu.com.fieldnotebook.models.NewFileObj;
import toidiu.com.fieldnotebook.utils.UtilNetwork;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper()
@EventProducer(generated = {
        @Event(postFix = "Success", fields = {
                @ParcelableField(name = "isSynced", kind = @Kind(clazz = Boolean.class)),
        }),
        @Event(postFix = "Failure")
})

public class CheckUploadStatusAction extends BaseAction {


    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        if (!UtilNetwork.isDeviceOnline(context)) {
            return null;
        }

        Dao<NewFileObj, Integer> newFileObjDao = databaseHelper.getNewFileObjDao();
        Dao<FileUploadObj, Integer> fileUploadDao = databaseHelper.getFileUploadDao();
        //get items from DB
        List<FileUploadObj> fileUploadObjList = fileUploadDao.queryForAll();
        List<NewFileObj> newFileObjList = newFileObjDao.queryForAll();

        if (fileUploadObjList.isEmpty() && newFileObjList.isEmpty()) {
            return new CheckUploadStatusActionSuccess(true);
        } else {
            return new CheckUploadStatusActionSuccess(false);
        }
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        return new CheckUploadStatusActionFailure();
    }
}

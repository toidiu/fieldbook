package toidiu.com.fieldnotebook.actions;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.edisonwang.ps.annotations.Action;
import com.edisonwang.ps.annotations.ActionHelper;
import com.edisonwang.ps.annotations.Event;
import com.edisonwang.ps.annotations.EventProducer;
import com.edisonwang.ps.annotations.Field;
import com.edisonwang.ps.annotations.Kind;
import com.edisonwang.ps.annotations.ParcelableField;
import com.edisonwang.ps.lib.ActionRequest;
import com.edisonwang.ps.lib.ActionResult;
import com.edisonwang.ps.lib.RequestEnv;

import java.io.File;
import java.sql.SQLException;

import toidiu.com.fieldnotebook.actions.DbAddNewFileAction_.PsDbAddNewFileAction;
import toidiu.com.fieldnotebook.actions.events.DbAddNewFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.DbAddNewFileActionSuccess;
import toidiu.com.fieldnotebook.models.NewFileObj;
import toidiu.com.fieldnotebook.utils.UtilFile;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper(args = {
        @Field(name = "newFileObj", kind = @Kind(clazz = NewFileObj.class), required = true),
})
@EventProducer(generated = {
        @Event(postFix = "Success", fields = {
                @ParcelableField(name = "newFileObj", kind = @Kind(clazz = NewFileObj.class))
        }),
        @Event(postFix = "Failure")
})

public class DbAddNewFileAction extends BaseAction {

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        DbAddNewFileActionHelper helper = PsDbAddNewFileAction.helper(request.getArguments(getClass().getClassLoader()));
        NewFileObj newFileObj = helper.newFileObj();

        if (newFileObj.localFilePath == null) {
            String localFileName = newFileObj.parentName + System.currentTimeMillis();

            if (newFileObj.parentName.equals(BaseAction.PHOTOS_FOLDER_NAME)) {
                //photo file should already have path
            } else {
                File localFile = UtilFile.getLocalFileWithExtension(localFileName, newFileObj.mime);
                localFile = UtilFile.copyAssetsFile(context.getAssets(), newFileObj.assetFileName, localFile);
                newFileObj.localFilePath = localFile.getAbsolutePath();
            }
        }


        try {
            databaseHelper.getNewFileObjDao().create(newFileObj);
            return new DbAddNewFileActionSuccess(newFileObj);
        } catch (SQLException e) {
            Crashlytics.getInstance().core.logException(e);
            return new DbAddNewFileActionFailure();
        }
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        return new DbAddNewFileActionFailure();
    }
}

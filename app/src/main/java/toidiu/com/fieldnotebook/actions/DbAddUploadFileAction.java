package toidiu.com.fieldnotebook.actions;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.edisonwang.ps.annotations.Action;
import com.edisonwang.ps.annotations.ActionHelper;
import com.edisonwang.ps.annotations.Event;
import com.edisonwang.ps.annotations.EventProducer;
import com.edisonwang.ps.annotations.Field;
import com.edisonwang.ps.annotations.Kind;
import com.edisonwang.ps.lib.ActionRequest;
import com.edisonwang.ps.lib.ActionResult;
import com.edisonwang.ps.lib.RequestEnv;

import java.sql.SQLException;

import toidiu.com.fieldnotebook.actions.DbAddUploadFileAction_.PsDbAddUploadFileAction;
import toidiu.com.fieldnotebook.actions.events.DbAddUploadFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.DbAddUploadFileActionSuccess;
import toidiu.com.fieldnotebook.models.FileUploadObj;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper(args = {
        @Field(name = "fileUploadObj", kind = @Kind(clazz = FileUploadObj.class), required = true),
})
@EventProducer(generated = {
        @Event(postFix = "Success"),
        @Event(postFix = "Failure")
})

public class DbAddUploadFileAction extends BaseAction {


    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        DbAddUploadFileActionHelper helper = PsDbAddUploadFileAction.helper(request.getArguments(getClass().getClassLoader()));
        FileUploadObj fileUploadObj = helper.fileUploadObj();

        if (credential.getSelectedAccountName() == null) {
//            return new SetupDriveFailure();
        }

        try {
            databaseHelper.getFileUploadDao().create(fileUploadObj);
            return new DbAddUploadFileActionSuccess();
        } catch (SQLException e) {
            Crashlytics.getInstance().core.logException(e);
            return new DbAddUploadFileActionFailure();
        }
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        return new DbAddUploadFileActionFailure();
    }
}

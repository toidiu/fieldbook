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
import com.google.api.services.drive.model.File;

import toidiu.com.fieldnotebook.actions.RenameFileAction_.PsRenameFileAction;
import toidiu.com.fieldnotebook.actions.events.RenameFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.RenameFileActionSuccess;
import toidiu.com.fieldnotebook.models.FileObj;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper(args = {
        @Field(name = "file", kind = @Kind(clazz = FileObj.class), required = true),
        @Field(name = "newName", kind = @Kind(clazz = String.class), required = true)
})
@EventProducer(generated = {
        @Event(postFix = "Success"),
        @Event(postFix = "Failure")
})

public class RenameFileAction extends BaseAction {

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        RenameFileActionHelper helper = PsRenameFileAction.helper(request.getArguments(getClass().getClassLoader()));
        FileObj fileObj = helper.file();

        File file = renameFile(fileObj.id, helper.newName(), fileObj.parent);
        if (file != null) {
            return new RenameFileActionSuccess();
        } else {
            return new RenameFileActionFailure();
        }
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        Crashlytics.getInstance().core.logException(e);
        return new RenameFileActionFailure();
    }
}

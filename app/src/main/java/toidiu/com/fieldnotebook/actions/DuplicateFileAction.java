package toidiu.com.fieldnotebook.actions;

import android.content.Context;

import com.edisonwang.ps.annotations.Action;
import com.edisonwang.ps.annotations.ActionHelper;
import com.edisonwang.ps.annotations.Event;
import com.edisonwang.ps.annotations.EventProducer;
import com.edisonwang.ps.annotations.Field;
import com.edisonwang.ps.annotations.Kind;
import com.edisonwang.ps.lib.ActionRequest;
import com.edisonwang.ps.lib.ActionResult;
import com.edisonwang.ps.lib.RequestEnv;

import toidiu.com.fieldnotebook.actions.DuplicateFileAction_.PsDuplicateFileAction;
import toidiu.com.fieldnotebook.actions.events.DuplicateFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.DuplicateFileActionSuccess;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper(args = {
        @Field(name = "fileId", kind = @Kind(clazz = String.class), required = true),
        @Field(name = "fileName", kind = @Kind(clazz = String.class), required = true),
})
@EventProducer(generated = {
        @Event(postFix = "Success"),
        @Event(postFix = "Failure")
})

public class DuplicateFileAction extends BaseAction {

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        DuplicateFileActionHelper helper = PsDuplicateFileAction.helper(request.getArguments(getClass().getClassLoader()));

        if (credential.getSelectedAccountName() == null) {
            return new DuplicateFileActionFailure();
        }

        String fileName = helper.fileName();
        String copyFileName;
        int i = fileName.indexOf(".pdf");
        if (i > 0) {
            copyFileName = fileName.substring(0, i) + "-copy.pdf";
        } else {
            copyFileName = fileName + "-copy";
        }


        if (copyFile(helper.fileId(), copyFileName) != null) {
            return new DuplicateFileActionSuccess();
        } else {
            return new DuplicateFileActionFailure();
        }
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        return new DuplicateFileActionFailure();
    }
}

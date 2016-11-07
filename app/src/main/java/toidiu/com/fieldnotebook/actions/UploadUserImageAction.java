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

import java.util.List;

import toidiu.com.fieldnotebook.actions.UploadUserImageAction_.PsUploadUserImageAction;
import toidiu.com.fieldnotebook.actions.events.UploadUserImageActionFailure;
import toidiu.com.fieldnotebook.actions.events.UploadUserImageActionSuccess;
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

public class UploadUserImageAction extends BaseAction {


    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        UploadUserImageActionHelper helper = PsUploadUserImageAction.helper(request.getArguments(getClass().getClassLoader()));
        FileUploadObj fileUploadObj = helper.fileUploadObj();

        File createdFile;
        //check if the file already exists
        List<File> fileByName = getFileByName(fileUploadObj.fileName, prefs.getPhotosFolderId());
        if (fileByName != null && fileByName.size() > 0) {
            //yes
            createdFile = replaceFile(fileUploadObj);
        } else {
            //no
            createdFile = createFile(helper.fileUploadObj());
        }


        if (createdFile != null) {
            return new UploadUserImageActionSuccess();
        } else {
            return new UploadUserImageActionFailure();
        }
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        Crashlytics.getInstance().core.logException(e);
        return new UploadUserImageActionFailure();
    }
}

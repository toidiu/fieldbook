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
import com.edisonwang.ps.lib.PennStation;
import com.edisonwang.ps.lib.RequestEnv;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import toidiu.com.fieldnotebook.actions.DbAddUploadFileAction_.PsDbAddUploadFileAction;
import toidiu.com.fieldnotebook.actions.DownloadFileAction_.PsDownloadFileAction;
import toidiu.com.fieldnotebook.actions.DwgConversionAction_.PsDwgConversionAction;
import toidiu.com.fieldnotebook.actions.events.DownloadFileActionDwgConversion;
import toidiu.com.fieldnotebook.actions.events.DownloadFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.DownloadFileActionSuccess;
import toidiu.com.fieldnotebook.models.FileDownloadObj;
import toidiu.com.fieldnotebook.models.FileUploadObj;
import toidiu.com.fieldnotebook.models.LocalFileObj;
import toidiu.com.fieldnotebook.utils.UtilFile;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper(args = {
        @Field(name = "fileDl", kind = @Kind(clazz = FileDownloadObj.class), required = true),
        @Field(name = "ActionEnum", kind = @Kind(clazz = String.class), required = true),
})
@EventProducer(generated = {
        @Event(postFix = "Success", fields = {
                @ParcelableField(name = "localFileObj", kind = @Kind(clazz = LocalFileObj.class)),
                @ParcelableField(name = "ActionEnum", kind = @Kind(clazz = String.class)),
        }),
        @Event(postFix = "Failure"),
        @Event(postFix = "DwgConversion")
})

public class DownloadFileAction extends BaseAction {

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        DownloadFileActionHelper helper = PsDownloadFileAction.helper(request.getArguments(getClass().getClassLoader()));
        FileDownloadObj fileDlObj = helper.fileDl();
        String actionEnum = helper.ActionEnum();

        File localFile = null;
        if (actionEnum.equals(ActionEnum.EDIT.name())) {
            File fileLocation = UtilFile.getLocalFileWithExtension(fileDlObj.fileId, fileDlObj.mime);
            localFile = downloadOrReturnExistingFile(fileDlObj, fileLocation);
        } else {
            //if we print or share.. see if local file exists and copy it over to cache
            File tempFile = UtilFile.getLocalFileWithExtension(fileDlObj.fileId, fileDlObj.mime);
            //make a file in the cache folder
            File fileLocation = UtilFile.getCachedFile(fileDlObj.fileName, fileDlObj.mime);

            if (tempFile != null || tempFile.exists()) {
                try {
                    FileUtils.copyFile(tempFile, fileLocation);
                } catch (IOException e) {
                    Crashlytics.getInstance().core.logException(e);
                }
            }
            localFile = downloadOrReturnExistingFile(fileDlObj, fileLocation);
        }

        if (localFile != null && localFile.exists()) {
            LocalFileObj localFileObj = new LocalFileObj(localFile.getName(), fileDlObj.mime, localFile.getAbsolutePath());

            if (fileDlObj.mime.equals(BaseAction.MIME_DWG1) && actionEnum.equals(ActionEnum.EDIT.name())) {
                //do the zamzar conversion and dont upload because we dont need to reupload the DWG file
                PennStation.requestAction(PsDwgConversionAction.helper(fileDlObj, localFileObj));
                return new DownloadFileActionDwgConversion();
            } else if (actionEnum.equals(ActionEnum.EDIT.name())) {
                PennStation.requestAction(PsDbAddUploadFileAction.helper(new FileUploadObj(fileDlObj.parentId, fileDlObj.fileId, fileDlObj.fileName, localFile.getAbsolutePath(), fileDlObj.mime)));
            }
            return new DownloadFileActionSuccess(localFileObj, actionEnum);
        } else {
            return new DownloadFileActionFailure();
        }
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        return new DownloadFileActionFailure();
    }

    //~=~=~=~=~=~=~=~=~=~=~=~=Field
    public enum ActionEnum {
        SHARE,
        PRINT,
        EDIT
    }


}

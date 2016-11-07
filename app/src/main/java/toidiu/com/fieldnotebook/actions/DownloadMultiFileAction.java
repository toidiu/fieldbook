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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import toidiu.com.fieldnotebook.actions.DownloadMultiFileAction_.PsDownloadMultiFileAction;
import toidiu.com.fieldnotebook.actions.events.DownloadMultiFileActionFailure;
import toidiu.com.fieldnotebook.actions.events.DownloadMultiFileActionSuccess;
import toidiu.com.fieldnotebook.models.FileDownloadObj;
import toidiu.com.fieldnotebook.models.LocalFileObj;
import toidiu.com.fieldnotebook.utils.UtilFile;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper(args = {
        @Field(name = "fileDlArray", kind = @Kind(clazz = FileDownloadObj[].class), required = true)
})
@EventProducer(generated = {
        @Event(postFix = "Success", fields = {
                @ParcelableField(name = "localFileObj", kind = @Kind(clazz = LocalFileObj[].class))
        }),
        @Event(postFix = "Failure")
})

public class DownloadMultiFileAction extends BaseAction {

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        DownloadMultiFileActionHelper helper = PsDownloadMultiFileAction.helper(request.getArguments(getClass().getClassLoader()));
        FileDownloadObj[] fileDlObjArray = helper.fileDlArray();
//        List<FileDownloadObj> fileDlObjList = Arrays.asList(fileDlObjArray);


        List<LocalFileObj> localFileObjList = new ArrayList<>();

        for (FileDownloadObj fileDl : fileDlObjArray) {


            //if we print or share.. see if local file exists and copy it over to cache
            File tempFile = UtilFile.getLocalFileWithExtension(fileDl.fileId, fileDl.mime);
            //make a file in the cache folder
            File fileLocation = UtilFile.getCachedFile(fileDl.fileName, fileDl.mime);

            if (tempFile != null || tempFile.exists()) {
                try {
                    FileUtils.copyFile(tempFile, fileLocation);
                } catch (IOException e) {
                    Crashlytics.getInstance().core.logException(e);
                }
            }

            File localFile = downloadOrReturnExistingFile(fileDl, fileLocation);
            if (localFile != null && localFile.exists()) {
                localFileObjList.add(new LocalFileObj(localFile.getName(), fileDl.mime, localFile.getAbsolutePath()));
            } else {
                break;
            }
        }

        if (fileDlObjArray.length == localFileObjList.size()) {
            LocalFileObj[] arr = new LocalFileObj[localFileObjList.size()];
            return new DownloadMultiFileActionSuccess(localFileObjList.toArray(arr));
        } else {
            return new DownloadMultiFileActionFailure();
        }
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        return new DownloadMultiFileActionFailure();
    }

}

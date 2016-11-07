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
import com.edisonwang.ps.lib.PennStation;
import com.edisonwang.ps.lib.RequestEnv;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;

import toidiu.com.fieldnotebook.actions.DbAddUploadFileAction_.PsDbAddUploadFileAction;
import toidiu.com.fieldnotebook.actions.events.MergePdfActionFailure;
import toidiu.com.fieldnotebook.actions.events.MergePdfActionSuccess;
import toidiu.com.fieldnotebook.models.FileDownloadObj;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.models.FileUploadObj;
import toidiu.com.fieldnotebook.models.LocalFileObj;
import toidiu.com.fieldnotebook.utils.UtilFile;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper()
@EventProducer(generated = {
        @Event(postFix = "Success", fields = {
                @ParcelableField(name = "localFileObj", kind = @Kind(clazz = LocalFileObj.class))
        }),
        @Event(postFix = "Failure")
})

public class MergePdfAction extends BaseAction {

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        //get original remote file
        FileObj origPdf = mergePfdHelper.origPdf;
        FileDownloadObj fileDlObj = new FileDownloadObj(origPdf.parent, origPdf.id, origPdf.title, origPdf.mime);

        File fileLocation = UtilFile.getLocalFileWithExtension(fileDlObj.fileId, fileDlObj.mime);
        File localFile = downloadOrReturnExistingFile(fileDlObj, fileLocation);

        //make a new fab sheet file in local storage
        File newFabTemplate;
        if (localFile != null && localFile.exists()) {
            newFabTemplate = UtilFile.copyAssetsFile(context.getAssets(), FAB_SHEET_ASSET_PDF, UtilFile.getLocalMergeFile());
        } else {
            return new MergePdfActionFailure();
        }

        //merge the files and clean up
        File localMergeDestinationFile = UtilFile.getLocalMergeDestinationFile();

        PDFMergerUtility mergerUtility = new PDFMergerUtility();
        mergerUtility.setDestinationFileName(localMergeDestinationFile.getAbsolutePath());
        mergerUtility.addSource(localFile);
        mergerUtility.addSource(newFabTemplate);
        mergerUtility.mergeDocuments();

        UtilFile.deleteLocalFile(newFabTemplate);
        FileUtils.copyFile(localMergeDestinationFile, localFile);

        //add merged to be synced late
        PennStation.requestAction(PsDbAddUploadFileAction.helper(new FileUploadObj(fileDlObj.parentId, fileDlObj.fileId, fileDlObj.fileName, localFile.getAbsolutePath(), fileDlObj.mime)));

        //return file to be edited
        LocalFileObj localFileObj = new LocalFileObj(localFile.getName(), fileDlObj.mime, localFile.getAbsolutePath());
        return new MergePdfActionSuccess(localFileObj);

    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        Crashlytics.getInstance().core.logException(e);
        return new MergePdfActionFailure();
    }

}

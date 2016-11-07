package toidiu.com.fieldnotebook.actions;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

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
import com.edisonwang.ps.lib.ResultDeliver;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

import toidiu.com.fieldnotebook.actions.DbAddUploadFileAction_.PsDbAddUploadFileAction;
import toidiu.com.fieldnotebook.actions.DwgConversionAction_.PsDwgConversionAction;
import toidiu.com.fieldnotebook.actions.events.DwgConversionActionFailure;
import toidiu.com.fieldnotebook.actions.events.DwgConversionActionSuccess;
import toidiu.com.fieldnotebook.models.FileDownloadObj;
import toidiu.com.fieldnotebook.models.FileUploadObj;
import toidiu.com.fieldnotebook.models.LocalFileObj;
import toidiu.com.fieldnotebook.utils.UtilFile;
import toidiu.com.fieldnotebook.utils.UtilZamzar;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper(args = {
        @Field(name = "fileDl", kind = @Kind(clazz = FileDownloadObj.class), required = true),
        @Field(name = "localFileObj", kind = @Kind(clazz = LocalFileObj.class), required = true),
})
@EventProducer(generated = {
        @Event(postFix = "Success", fields = {
                @ParcelableField(name = "parentId", kind = @Kind(clazz = String.class))
        }),
        @Event(postFix = "Failure")
})

public class DwgConversionAction extends BaseAction {

    public static final int DELAY_TIME = 5000;
    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    private int errorCnt = 0;
    private PowerManager.WakeLock wakeLock;
    private FileDownloadObj fileDownloadObj;

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        DwgConversionActionHelper helper = PsDwgConversionAction.helper(request.getArguments(getClass().getClassLoader()));
        fileDownloadObj = helper.fileDl();
        File localDwgFile = new File(helper.localFileObj().localPath);

        //wake lock
        PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();

//        UtilZamzar.AskResp askResp = askIsReady(296567);
//        File file = createLocalAndDownloadZamzar(13448787);
//        Log.d("-----zamzar wait", "1");


        Log.d("-----zamzar wait", "1");
        //get info about the account
//        UtilZamzar.info();

        //upload dwg file to start conversion
        Log.d("-----zamzar wait", "2");
        final UtilZamzar.ConvertResp convert = UtilZamzar.convert(localDwgFile);
        if (convert == null) {
            return new DwgConversionActionFailure();
        }

        //ask for status and if successful download file. Try 3 times
        Log.d("-----zamzar wait", "3");
        while (errorCnt <= 3) {
            Thread.sleep(DELAY_TIME);
            Log.d("-----zamzar wait", "after 5 secs");

            UtilZamzar.AskResp askResp = UtilZamzar.askStatus(convert.id);
            if (askResp.status.equals("successful")) {
                Log.d("-----zamzar wait", "4");
                if (askResp.target_files.length <= 0) {
                    Log.d("-----zamzar wait", "5");
                    return new DwgConversionActionFailure();
                }

                try {
                    Log.d("-----zamzar wait", "6");
                    //make local file and download zamzar file
                    File localPdfFile = UtilFile.getLocalFileWithExtension(fileDownloadObj.fileId, MIME_PDF);
                    UtilZamzar.download(askResp.target_files[0].id, localPdfFile);

                    if (localPdfFile != null && localPdfFile.exists()) {
                        UtilFile.deleteLocalFile(localDwgFile);
                        createOrUpdateDriveFile(localPdfFile);
                        Log.d("-----zamzar wait", "7");
                        return new DwgConversionActionSuccess(fileDownloadObj.parentId);
                    }
                } catch (Exception e) {
                    Crashlytics.getInstance().core.logException(e);
                }
            }

            Log.d("-----zamzar wait", "8");
            errorCnt++;
        }

        return new DwgConversionActionFailure();
    }

    private void createOrUpdateDriveFile(File localPdfFile) {
        String pdfFileName = FilenameUtils.removeExtension(fileDownloadObj.fileName) + ".pdf";
        FileUploadObj fileUploadObj = new FileUploadObj(fileDownloadObj.parentId, null, pdfFileName, localPdfFile.getAbsolutePath(), MIME_PDF);

        List<com.google.api.services.drive.model.File> fileList = getFileByName(pdfFileName, fileDownloadObj.parentId);
        com.google.api.services.drive.model.File createdFile;
        if (fileList.isEmpty()) {
            //create
            createdFile = createFile(fileUploadObj);
        } else {
            fileUploadObj.fileId = fileList.get(0).getId();
            //replace
            createdFile = replaceFile(fileUploadObj);
        }
        if (createdFile == null) {
            PennStation.requestAction(PsDbAddUploadFileAction.helper(fileUploadObj));
        }

    }

    @Override
    public void onRequestComplete(ResultDeliver resultDeliver, ActionResult result) {
        super.onRequestComplete(resultDeliver, result);
        wakeLock.release();
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        return new DwgConversionActionFailure();
    }

}

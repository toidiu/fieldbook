package toidiu.com.fieldnotebook.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by toidiu on 7/5/16.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class MyPrintDocumentAdapter extends PrintDocumentAdapter {
    private File file;
    private String jobName;

    //region Lifecycle----------------------
    public MyPrintDocumentAdapter(File file, String jobName) {
        this.file = file;
        this.jobName = jobName;
        Crashlytics.getInstance().core.log(1, "MyPrintDocumentAdapter -" + jobName, file.getAbsolutePath());
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback){
        InputStream input = null;
        OutputStream output = null;

        try {
            input = new FileInputStream(file);
            output = new FileOutputStream(destination.getFileDescriptor());

            byte[] buf = new byte[1024];
            int bytesRead;

            Integer integer = 0;
            while ((bytesRead = input.read(buf)) > 0) {
                integer++;
                output.write(buf, 0, bytesRead);
            }

            Crashlytics.getInstance().core.log(1, "MyPrintDocumentAdapter -" + jobName, integer.toString());

            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.getInstance().core.logException(e);
            //Catch exception
        } finally {
            try {
                input.close();
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.getInstance().core.logException(e);
            }
        }
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras){

        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(jobName).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();
        callback.onLayoutFinished(pdi, true);
    }

}

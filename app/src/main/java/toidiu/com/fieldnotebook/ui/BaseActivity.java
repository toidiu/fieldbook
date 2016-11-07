package toidiu.com.fieldnotebook.ui;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.edisonwang.ps.lib.LimitedQueueInfo;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.FieldNotebookApplication;
import toidiu.com.fieldnotebook.actions.BaseAction;
import toidiu.com.fieldnotebook.models.LocalFileObj;
import toidiu.com.fieldnotebook.utils.ArchiveFileHelper;
import toidiu.com.fieldnotebook.utils.BgImageLoader;
import toidiu.com.fieldnotebook.utils.ClaimChangedFileHelper;
import toidiu.com.fieldnotebook.utils.MergePfdHelper;
import toidiu.com.fieldnotebook.utils.MoveFolderHelper;
import toidiu.com.fieldnotebook.utils.MultiShareHelper;
import toidiu.com.fieldnotebook.utils.Prefs;
import toidiu.com.fieldnotebook.utils.RenameFileHelper;

public class BaseActivity extends AppCompatActivity {

    public static final int OPEN_FILE_REQUEST_CODE = 23415;

    @Inject
    Prefs prefs;
    @Inject
    GoogleAccountCredential credential;
    @Inject
    MoveFolderHelper moveFolderHelper;
    @Inject
    RenameFileHelper renameFileHelper;
    @Inject
    ArchiveFileHelper archiveFileHelper;
    @Inject
    MultiShareHelper multiShareHelper;
    @Inject
    ClaimChangedFileHelper claimChangedFileHelper;
    @Inject
    Context context;
    @Inject
    LimitedQueueInfo longTaskQueue;
    @Inject
    MergePfdHelper mergePfdHelper;

    public static void email(Context context, String emailTo, String emailCC,
                             String subject, String emailText, List<String> filePaths) {
        //need to "send multiple" to get more than one attachment
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");

        //has to be an ArrayList
        ArrayList<Uri> uris = new ArrayList<Uri>();
        //convert from paths to Android friendly Parcelable Uri's
        for (String file : filePaths) {
            File fileIn = new File(file);
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FieldNotebookApplication) getApplication()).getAppComponent().inject(this);
    }

    protected void initBg() {
        final View layout = findViewById(R.id.layout);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Picasso.with(BaseActivity.this).load(R.drawable.app_bg).into(new BgImageLoader(getResources(), layout));
            }
        });
    }

    public void openLocalFile(LocalFileObj localFileObj, @Nullable View view) {
        PackageManager packageManager = getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType(localFileObj.mime);
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if (list.isEmpty()) {
            String msg = localFileObj.mime.equals(BaseAction.MIME_PDF) ? getString(R.string.install_pdf_msg) : getString(R.string.install_img_msg);
            if (view != null) {
                Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        } else {
            File file = new File(localFileObj.localPath);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), localFileObj.mime);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(intent, OPEN_FILE_REQUEST_CODE);
        }
    }

    public void shareIntentFile(LocalFileObj localFileObj) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "I have attached the requested file..");

        File file = new File(localFileObj.localPath);
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Uri uri = Uri.fromFile(file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Send email..."));
    }

    public void shareIntentMultiFile(LocalFileObj[] localFileObj) {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "I have attached the requested file..");

//        File file = new File(localFileObj.localPath);
//        if (!file.exists() || !file.canRead()) {
//            Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }

//        Uri uri = Uri.fromFile(file);

        ArrayList<Uri> uris = new ArrayList<Uri>();
        for (LocalFileObj local : localFileObj) {
            File file = new File(local.localPath);
            if (!file.exists() || !file.canRead()) {
                Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            uris.add(Uri.fromFile(file));
        }


        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(Intent.createChooser(intent, "Send email..."));
    }

    protected void printIntentFile(LocalFileObj localFileObj) {
        File file = new File(localFileObj.localPath);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Intent printIntent = new Intent(this, PrintDialogActivity.class);
            printIntent.setDataAndType(Uri.fromFile(file), localFileObj.mime);
            printIntent.putExtra("title", localFileObj.title);
            startActivity(printIntent);
        } else {
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

            List<PrintJob> printJobs = printManager.getPrintJobs();
            // Set job name, which will be displayed in the print queue
            String jobName = getString(R.string.app_name) +"-"+ file.getName();

            // Start a print job, passing in a PrintDocumentAdapter implementation
            // to handle the generation of a print document
            PrintJob print = printManager.print(jobName, new MyPrintDocumentAdapter(file, jobName), null);//
        }
    }
}
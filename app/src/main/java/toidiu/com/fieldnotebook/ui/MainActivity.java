package toidiu.com.fieldnotebook.ui;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import toidiu.com.fieldnotebook.BuildConfig;
import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.FieldNotebookApplication;
import toidiu.com.fieldnotebook.actions.BaseAction;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.utils.BgImageLoader;
import toidiu.com.fieldnotebook.utils.MoveFolderHelper;
import toidiu.com.fieldnotebook.utils.Prefs;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks {
    //region Fields----------------------
    //~=~=~=~=~=~=~=~=~=~=~=~=Constants
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_ACCOUNT_STORAGE = 1003;
    String[] PERM_REQUEST = {GET_ACCOUNTS, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    //~=~=~=~=~=~=~=~=~=~=~=~=View
    @Bind(R.id.progress)
    ProgressBar progress;
    @Bind(R.id.signin)
    View signinView;
    @Bind(R.id.logo)
    ImageView logoView;
    @Bind(R.id.layout)
    View layout;

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    @Inject
    Prefs prefs;
    @Inject
    GoogleAccountCredential credential;
    @Inject
    MoveFolderHelper moveFolderHelper;
    //endregion

    //region LifeCycle----------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FieldNotebookApplication) getApplication()).getAppComponent().inject(this);
        if (prefs.getBaseFolderId() != null) {
            startBaseProjActivity();
            return;
        }

        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);
        Log.d("log", "-----------1");
        initView();
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Snackbar.make(progress, R.string.error_play_services, Snackbar.LENGTH_INDEFINITE).show();
                } else {
                    getStarted();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        prefs.setUser(accountName);
                        credential.setSelectedAccountName(accountName);
                        getStarted();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getStarted();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        final Snackbar snackbar = Snackbar.make(progress, "Please grant permission to continue.", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Grant Permission", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getStarted();
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
        snackbar.show();
        // Do nothing.
    }

    //endregion

    //region Google Helper----------------------

    private void getStarted() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (credential.getSelectedAccountName() == null) {
            Log.d("log", "-----------2");
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Log.d("log", "-----------3");
            Snackbar.make(progress, R.string.no_network, Snackbar.LENGTH_SHORT).show();
            if (prefs.getBaseFolderId() != null) {
                startBaseProjActivity();
            }
        } else {
            Log.d("log", "-----------4");
            new MakeRequestTask(credential).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_ACCOUNT_STORAGE)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, PERM_REQUEST)) {
            String accountName = prefs.getUser();
            if (accountName != null) {
                Log.d("log1", "-----------1");
                credential.setSelectedAccountName(accountName);
                Log.d("log1", String.valueOf(credential == null));

                if (credential != null) {
                    getStarted();
                }
            } else {
                Log.d("log1", "-----------2");
                // Start a dialog from which the user can choose an account
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            Log.d("log1", "-----------3");
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(this, getString(R.string.permission_msg),
                    REQUEST_PERMISSION_ACCOUNT_STORAGE, PERM_REQUEST);
        }
    }


    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(MainActivity.this, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
    //endregion

    //region Helper----------------------

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    void startBaseProjActivity() {
        startActivity(FindBaseProjActivity.getInstance(this));
        finish();
    }
    //endregion

    //region Init----------------------
    private void initView() {
        Picasso.with(this).load(R.drawable.app_bg).into(new BgImageLoader(getResources(), layout));
        initBg();

        signinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getStarted();
            }
        });
    }

    protected void initBg() {
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Picasso.with(MainActivity.this).load(R.drawable.app_bg)
                        .into(new BgImageLoader(getResources(), layout, true));
            }
        });
    }
    //endregion


    //region MakeCall----------------------
    private class MakeRequestTask extends AsyncTask<Void, Void, List<FileObj>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(transport, jsonFactory, credential).setApplicationName("SJ").build();
        }

        @Override
        protected List<FileObj> doInBackground(Void... params) {
            try {
                Log.d("log", "-----------5");
                return getDataFromApi();
            } catch (Exception e) {
                Crashlytics.getInstance().core.logException(e);
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private List<FileObj> getDataFromApi() throws IOException {
            String search = "title contains '" + "Jobs" + "'"
                    + " and " + "title != '.DS_Store'"
                    + " and " + " sharedWithMe=true "
                    + " and " + "mimeType = '" + BaseAction.FOLDER_MIME + "'";

            List<FileObj> retFile = new ArrayList<>();

            Log.d("log", "-----------6");
            FileList result = mService.files().list()
                    .setQ(search)
                    .setSpaces("drive")
//                    .setFields("nextPageToken, files(id, name, modifiedTime, owners, mimeType, parents, properties)")
                    .execute();


            List<File> files = result.getItems();
            if (files != null) {
                for (File f : files) {
                    FileObj object = new FileObj(f);
                    retFile.add(object);
                }
            }
            return retFile;
        }

        @Override
        protected void onPostExecute(List<FileObj> output) {
            Log.d("log", "-----------7");
            startBaseProjActivity();
        }

        @Override
        protected void onCancelled() {
            progress.setVisibility(View.GONE);
            Log.d("log", "-----------8");

            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    Toast.makeText(MainActivity.this, "test only: play services error", Toast.LENGTH_SHORT).show();
                    showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    Toast.makeText(MainActivity.this, "test only: recoverable error", Toast.LENGTH_SHORT).show();
                    startActivityForResult(((UserRecoverableAuthIOException) mLastError).getIntent(), MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(MainActivity.this, "test only: login error", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(MainActivity.this, "test only: cancelled error", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    //endregion
}

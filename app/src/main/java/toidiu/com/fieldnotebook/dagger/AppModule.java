package toidiu.com.fieldnotebook.dagger;

import android.content.Context;

import com.edisonwang.ps.lib.LimitedQueueInfo;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import toidiu.com.fieldnotebook.database.DatabaseHelper;
import toidiu.com.fieldnotebook.utils.ArchiveFileHelper;
import toidiu.com.fieldnotebook.utils.MergePfdHelper;
import toidiu.com.fieldnotebook.utils.MoveFolderHelper;
import toidiu.com.fieldnotebook.utils.MultiShareHelper;
import toidiu.com.fieldnotebook.utils.Prefs;
import toidiu.com.fieldnotebook.utils.RenameFileHelper;
import toidiu.com.fieldnotebook.utils.ClaimChangedFileHelper;

/**
 * Created by toidiu on 1/13/16.
 */
@Module()
public class AppModule {
    //~=~=~=~=~=~=~=~=~=~=~=~=CONSTANTS

    @Provides
    @Singleton
    Prefs providesPrefs(Context context) {
        return new Prefs(context);
    }

    @Provides
    @Singleton
    GoogleAccountCredential providesCredentials(Context context, Prefs prefs) {
        final String[] SCOPES = {DriveScopes.DRIVE, DriveScopes.DRIVE_APPDATA};

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context.getApplicationContext(), Arrays.asList(SCOPES))
                .setSelectedAccountName(prefs.getUser())
                .setBackOff(new ExponentialBackOff());

        return credential;
    }

    @Provides
    Drive provideDriveService(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//        HttpTransport transport = AndroidHttp.newCompatibleTransport();
//        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        Drive mService = new Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("SJ")
                .build();

//        Drive driveService = new Drive.Builder(
//                transport, jsonFactory, credential)
//                .setApplicationName("SJ")
//                .build();

        return mService;
    }

    @Provides
    @Singleton
    LimitedQueueInfo provideLongTaskQueue() {
        return new LimitedQueueInfo(100, 10, "persistQueue");
    }

    @Provides
    @Singleton
    MoveFolderHelper provideMoveFolderHelper() {
        return new MoveFolderHelper();
    }

    @Provides
    @Singleton
    ArchiveFileHelper providesArchiveHelper() {
        return new ArchiveFileHelper();
    }

    @Provides
    @Singleton
    MultiShareHelper providesMultiShareHelper() {
        return new MultiShareHelper();
    }

    @Provides
    @Singleton
    ClaimChangedFileHelper providesClaimChangedFileHelper() {
        return new ClaimChangedFileHelper();
    }

    @Provides
    @Singleton
    MergePfdHelper providesMergePdfHelper() {
        return new MergePfdHelper();
    }

    @Provides
    @Singleton
    RenameFileHelper providesRenameHelper() {
        return new RenameFileHelper();
    }

    @Provides
    @Singleton
    DatabaseHelper providesDatabaseHelper(Context context) {
        return DatabaseHelper.getInstance(context);
    }

}

package toidiu.com.fieldnotebook.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.BoolRes;
import android.support.annotation.Nullable;

/**
 * Created by toidiu on 1/13/16.
 */

public class Prefs {

    //~=~=~=~=~=~=~=~=~=~=~=~=USER
    public static final long DEFAULT_START_DEMO = 0L;
    public static final String START_DEMO = "START_DEMO";
    public static final String IS_DEMO_MODE = "IS_DEMO_MODE";
    public static final String USER = "USER";
    public static final String BASE_FOLDER_ID = "BASE_FOLDER_ID";
    public static final String ARCHIVE_FOLDER_ID = "ARCHIVE_FOLDER_ID";
    public static final String PHOTOS_FOLDER_ID = "PHOTOS_FOLDER_ID";
    public static final String PO_NUMBER_FOLDER_ID = "PO_NUMBER_FOLDER_ID";

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    private SharedPreferences prefs;

    public Prefs(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //-~-~--~-~--~-~--~-~--~-~--~-~--~-~--~-~--~-~-DEMO
    public Long getStartDemo() {
        return prefs.getLong(START_DEMO, DEFAULT_START_DEMO);
    }

    public Long setStartDemo() {
        long value = System.currentTimeMillis();
        prefs.edit().putLong(START_DEMO, value).apply();
        return value;
    }

    public Boolean isDemoMode(){
        return prefs.getBoolean(IS_DEMO_MODE, true);
    }

    public void setDemoMode(Boolean demo){
        prefs.edit().putBoolean(IS_DEMO_MODE, demo).apply();
    }

    //-~-~--~-~--~-~--~-~--~-~--~-~--~-~--~-~--~-~-USER
    @Nullable
    public String getUser() {
        return prefs.getString(USER, null);
    }

    public void setUser(String user) {
        prefs.edit().putString(USER, user).apply();
    }

    @Nullable
    public String getBaseFolderId() {
        return prefs.getString(BASE_FOLDER_ID, null);
    }

    public void setBaseFolderId(String user) {
        prefs.edit().putString(BASE_FOLDER_ID, user).apply();
    }

    @Nullable
    public String getArchiveFolderId() {
        return prefs.getString(ARCHIVE_FOLDER_ID, null);
    }

    public void setArchiveFolderId(String user) {
        prefs.edit().putString(ARCHIVE_FOLDER_ID, user).apply();
    }


    @Nullable
    public String getPhotosFolderId() {
        return prefs.getString(PHOTOS_FOLDER_ID, null);
    }

    public void setPhotosFolderId(String user) {
        prefs.edit().putString(PHOTOS_FOLDER_ID, user).apply();
    }

    @Nullable
    public String getPoNumberFolderId() {
        return prefs.getString(PO_NUMBER_FOLDER_ID, null);
    }

    public void setPoNumberFolderId(String user) {
        prefs.edit().putString(PO_NUMBER_FOLDER_ID, user).apply();
    }

}


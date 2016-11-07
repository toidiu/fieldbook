package toidiu.com.fieldnotebook.utils;

import android.content.res.AssetManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import toidiu.com.fieldnotebook.FieldNotebookApplication;
import toidiu.com.fieldnotebook.actions.BaseAction;

/**
 * Created by toidiu on 4/13/16.
 */
public class UtilFile {
    //region File Helper----------------------
    public static boolean deleteLocalFile(File file) {
        if (!file.exists() || file.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public static File copyAssetsFile(AssetManager assetManager, String assetFileName, File localFile) {

        try {
            InputStream in = assetManager.open(assetFileName);
            OutputStream out = new FileOutputStream(localFile);
//                    openFileOutput(file.getName(), Context.MODE_WORLD_READABLE);

            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return localFile;
        } catch (Exception e) {
            Crashlytics.getInstance().core.logException(e);
            Log.e("tag", e.getMessage());
            return null;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
    //endregion

    //region Folder Helper----------------------
    @NonNull
    private static File getCustomCacheAppFolder() {
        File cache = FieldNotebookApplication.appContext.getExternalCacheDir();
        cache.mkdirs();
        return cache;
    }

    @NonNull
    public static File getExternalAppFolder() {
        File sj = new File(Environment.getExternalStorageDirectory(), "SJ");
        sj.mkdirs();
        return sj;
    }
    //endregion

    //region Get Files----------------------
    //file in external dir so its accessible by all
    @Nullable
    public static File getLocalFileWithExtension(String idOrName, String mime) {
        String name = null;
        if (mime.equals(BaseAction.MIME_PDF)) {
            name = idOrName + ".pdf";
        } else if (mime.equals(BaseAction.MIME_PNG) || mime.equals(BaseAction.MIME_JPEG)) {
            name = idOrName + ".jpg";
        } else if (mime.equals(BaseAction.MIME_DWG1)) {
            name = idOrName + ".dwg";
        }

        File sj = getExternalAppFolder();
        if (name == null) {
            return null;
        } else {
            return new File(sj, name);
        }
    }

    public static File getLocalMergeFile() {
        String name = "merge.pdf";
        File sj = getExternalAppFolder();
        File file = new File(sj, name);

        //clean up and return fresh file
        deleteLocalFile(file);
        return file;
    }


    public static File getLocalMergeDestinationFile() {
        String name = "mergeDestination.pdf";
        File sj = getExternalAppFolder();
        File file = new File(sj, name);

        //clean up and return fresh file
        deleteLocalFile(file);
        return file;
    }

    //random tamp file
    public static File getTempLocalFile(String mime) {
        String name = System.currentTimeMillis() + "";
        if (mime.equals(BaseAction.MIME_PDF)) {
            name = name + ".pdf";
        } else if (mime.equals(BaseAction.MIME_PNG) || mime.equals(BaseAction.MIME_JPEG)) {
            name = name + ".jpg";
        } else if (mime.equals(BaseAction.MIME_DWG1)) {
            name = name + ".dwg";
        }


        File sj = getExternalAppFolder();
        return new File(sj, name);
    }

    //for print and stuff. live in cache folder so it can be cleaned up by OS
    public static File getCachedFile(String idOrName, String mime) {
        String name = null;
        if (mime.equals(BaseAction.MIME_PDF) && !idOrName.endsWith(".pdf")) {
            name = idOrName + ".pdf";
        } else if (mime.equals(BaseAction.MIME_PNG) && !idOrName.endsWith(".png") || (!idOrName.endsWith(".jpg") && mime.equals(BaseAction.MIME_JPEG))) {
            name = idOrName + ".jpg";
        } else {
            name = idOrName;
        }

        File cacheAppFolder = getCustomCacheAppFolder();
        File file = new File(cacheAppFolder, name);
        if (file.exists()) {
            file.delete();
        }
        return file;
    }

    //endregion

}

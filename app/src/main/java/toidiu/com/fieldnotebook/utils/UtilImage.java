package toidiu.com.fieldnotebook.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by toidiu on 2/26/16.
 */
public class UtilImage {
    public static final int IMAGE_RESOLUTION = 300;

    public static float convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public static File getAvatarFile(Context context, String user) {
        String userFileName = getAvatarFileName(user);
        return new File(context.getFilesDir(), userFileName);
    }

    @NonNull
    public static String getAvatarFileName(String user) {
        return user + ".png";
    }

    public static File getTempFile(Context context) {
        File images = context.getExternalFilesDir("images");
        images.mkdirs();
        return new File(images, "temp.png");
    }

    public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawable) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawable);
        return bitmap;
    }

    public static File saveUserImage(Context context, Bitmap croppedImage, String user) {
        //save image to file
        try {
            File avatarFile = UtilImage.getAvatarFile(context, user);
            FileOutputStream fOut = new FileOutputStream(avatarFile);
            croppedImage.compress(Bitmap.CompressFormat.PNG, 60, fOut);
            fOut.flush();
            fOut.close();
            return avatarFile;
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return null;
        }
    }


    public static String getImageUriFromIntent(Intent data, Uri imagePickerUri) {
        final boolean isCamera;
        if (data.getData() == null) {
            isCamera = true;
        } else {
            final String action = data.getAction();
            if (action == null) {
                isCamera = false;
            } else {
                isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
            }
        }

        Uri selectedImageUri;
        if (isCamera) {
            selectedImageUri = imagePickerUri;
        } else {
            selectedImageUri = data.getData();
        }
        return selectedImageUri.toString();
    }

}

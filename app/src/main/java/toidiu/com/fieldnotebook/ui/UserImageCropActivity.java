package toidiu.com.fieldnotebook.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.actions.BaseAction;
import toidiu.com.fieldnotebook.models.FileUploadObj;
import toidiu.com.fieldnotebook.utils.UtilImage;

/**
 * Created by toidiu on 2/25/16.
 */

//@EventListener(producers = {
//        UploadFileAction.class
//})
public class UserImageCropActivity extends BaseActivity {

    public static final int RESULT_CODE = 48662;
    public static final String PATH_EXTRA = "PATH_EXTRA";
    public static final String RESULT_FILE_PATH = "FILE_PATH";
    //region Field----------------------
    //~=~=~=~=~=~=~=~=~=~=~=~=Constants
//    private static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain");
//    private static final MediaType MEDIA_TYPE_BMP = MediaType.parse("image/bmp; charset=utf-8");
//    private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg; charset=utf-8");
//    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png; charset=utf-8");
//    private static final MediaType MEDIA_TYPE_BINARY = MediaType.parse("application/octet-stream; charset=utf-8");
    //~=~=~=~=~=~=~=~=~=~=~=~=Views
    @Bind(R.id.crop)
    View cropBtn;
    @Bind(R.id.back)
    View back;
    @Bind(R.id.crop_image)
    CropImageView cropImageView;
    @Bind(R.id.progress)
    View progress;
    //~=~=~=~=~=~=~=~=~=~=~=~=Fields

    //region PennStation----------------------
//    UserImageCropActivityEventListener eventListener = new UserImageCropActivityEventListener() {
//        @Override
//        public void onEventMainThread(UploadFileActionFailure event) {
//            progress.setVisibility(View.GONE);
//            Snackbar.make(cropImageView, "Error, please check your connection.", Snackbar.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onEventMainThread(UploadFileActionSuccess event) {
//            progress.setVisibility(View.GONE);
//            finish();
//        }
//    };
    //endregion
    //endregion

    //region Lifecycle----------------------
    public static Intent getInstance(Context context, String path) {
        Intent intent = new Intent(context, UserImageCropActivity.class);
        intent.putExtra(PATH_EXTRA, path);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_img_crop);
        ButterKnife.bind(this);

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        PennStation.registerListener(eventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        PennStation.unRegisterListener(eventListener);
    }
    //endregion

    //region Init----------------------
    private void init() {
        String path = getIntent().getStringExtra(PATH_EXTRA);

        if (path.startsWith("content")) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(path));
                cropImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Crashlytics.getInstance().core.logException(e);
                finish();
            }
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            cropImageView.setImageBitmap(bitmap);
        }
    }

    //endregion

    @SuppressWarnings("unused")
    @OnClick(R.id.crop)
     void clickCrop() {
        progress.setVisibility(View.VISIBLE);

        Bitmap croppedImage = cropImageView.getCroppedImage();
        cropImageView.setImageBitmap(croppedImage);
        croppedImage = UtilImage.getResizedBitmap(croppedImage, UtilImage.IMAGE_RESOLUTION, UtilImage.IMAGE_RESOLUTION);
        File file = UtilImage.saveUserImage(context, croppedImage, prefs.getUser());

        if (file != null) {
            File avatarFile = UtilImage.getAvatarFile(UserImageCropActivity.this, prefs.getUser());
            FileUploadObj fileUploadObj = new FileUploadObj(prefs.getPhotosFolderId(), null, avatarFile.getName(), file.getAbsolutePath(), BaseAction.MIME_JPEG);
//// FIXME: 4/25/16
//            PennStation.requestAction(PsUploadUserImageAction.helper(fileUploadObj));
        }

        setResult(RESULT_OK);
        finish();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.back)
     void clickBack() {
        setResult(RESULT_CANCELED);
        finish();
    }

}

package toidiu.com.fieldnotebook.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.j256.ormlite.field.DatabaseField;

import javax.annotation.Nullable;

/**
 * Created by toidiu on 4/2/16.
 */
@ParcelablePlease
public class NewFileObj implements Parcelable {

    //region Fields----------------------
    @DatabaseField(generatedId = true)
    public Integer dbId;
    @DatabaseField
    public String title;
    @DatabaseField
    public String mime;
    @DatabaseField
    public String projId;
    @DatabaseField
    public String parentName;
    @DatabaseField
    @Nullable
    public String assetFileName;
    @DatabaseField
    public String localFilePath;

    @DatabaseField
    @Nullable
    public String projTitle;
    //endregion

    public static final Creator<NewFileObj> CREATOR = new Creator<NewFileObj>() {
        public NewFileObj createFromParcel(Parcel source) {
            NewFileObj target = new NewFileObj();
            NewFileObjParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public NewFileObj[] newArray(int size) {
            return new NewFileObj[size];
        }
    };


    //region Constructor----------------------
    @SuppressWarnings("unused")
    public NewFileObj() {
    }

    public NewFileObj(String parentName, @Nullable String assetFileName, String mime, String title, String projId, String localFilePath) {
        this.parentName = parentName;
        this.assetFileName = assetFileName;
        this.mime = mime;
        this.title = title;
        this.projId = projId;
        this.localFilePath = localFilePath;
    }
    //endregion

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        NewFileObjParcelablePlease.writeToParcel(this, dest, flags);
    }
}

package toidiu.com.fieldnotebook.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.j256.ormlite.field.DatabaseField;

/**
 * Created by toidiu on 4/12/16.
 */
@ParcelablePlease
public class FileUploadObj implements Parcelable {

    public static final Creator<FileUploadObj> CREATOR = new Creator<FileUploadObj>() {
        public FileUploadObj createFromParcel(Parcel source) {
            FileUploadObj target = new FileUploadObj();
            FileUploadObjParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public FileUploadObj[] newArray(int size) {
            return new FileUploadObj[size];
        }
    };
    @DatabaseField(generatedId = true)
    public Integer dbId;
    @DatabaseField
    public String fileId;
    @DatabaseField
    public String parentId;
    @DatabaseField
    public String fileName;
    @DatabaseField
    public String localFilePath;
    @DatabaseField
    public String mime;

    public FileUploadObj(String parentId, String fileId, String fileName, String localFilePath, String mime) {
        this.parentId = parentId;
        this.fileId = fileId;
        this.fileName = fileName;
        this.localFilePath = localFilePath;
        this.mime = mime;
    }

    public FileUploadObj() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        FileUploadObjParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "dbId " + this.dbId + " "
                + "fileId " + this.fileId + " "
                + "parentId " + this.parentId + " "
                + "fileName " + this.fileName + " "
                + "localFilePath " + this.localFilePath + " "
                + "mime " + this.mime;
    }
}

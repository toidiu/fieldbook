package toidiu.com.fieldnotebook.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Property;
import com.google.api.services.drive.model.User;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.j256.ormlite.field.DatabaseField;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import toidiu.com.fieldnotebook.actions.BaseAction;

/**
 * Created by toidiu on 4/2/16.
 */
@ParcelablePlease
public class FileObj implements Parcelable {

    public static final Creator<FileObj> CREATOR = new Creator<FileObj>() {
        public FileObj createFromParcel(Parcel source) {
            FileObj target = new FileObj();
            FileObjParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public FileObj[] newArray(int size) {
            return new FileObj[size];
        }
    };
    //region Fields----------------------
    @DatabaseField(generatedId = true)
    public Integer dbId;
    @DatabaseField
    public String id;
    @DatabaseField
    public String title;
    @DatabaseField
    public String mime;
    @DatabaseField
    public String owner;
    @DatabaseField
    public String lastModified;
    @DatabaseField
    public String parent;
    //endregion
    @Nullable
    @android.support.annotation.Nullable
    @DatabaseField
    public String claimUser;
    //endregion

    //region Constructor----------------------
    public FileObj(File f) {
        this.id = f.getId();
        this.title = f.getTitle();

        if (title.endsWith(".dwg")) {
            this.mime = BaseAction.MIME_DWG1;
        } else {
            this.mime = f.getMimeType();
        }
        this.lastModified = f.getModifiedDate().toString();

        List<User> owners = f.getOwners();
        if (owners != null && owners.size() > 0) {
            this.owner = owners.get(0).getDisplayName();
        }
        List<ParentReference> parents = f.getParents();
        if (parents != null && parents.size() > 0) {
            this.parent = parents.get(0).getId();
        }

        List<Property> properties = f.getProperties();
        if (properties != null && properties.size() > 0) {
            this.claimUser = f.getProperties().get(0).getValue();
        }
    }

    @SuppressWarnings("unused")
    public FileObj() {
    }

    //region Helper----------------------
    public static Comparator<FileObj> getComparator() {
        Comparator<FileObj> comparator = new Comparator<FileObj>() {

            public int compare(FileObj file1, FileObj file2) {

                String fileName1 = file1.title.toUpperCase();
                String fileName2 = file2.title.toUpperCase();

                //ascending order
                return fileName1.compareTo(fileName2);

                //descending order
                //return fruitName2.compareTo(fruitName1);
            }

        };
        return comparator;
    }

    public static boolean isFolder(@NonNull String mime) {
        return mime.equals(BaseAction.FOLDER_MIME);
    }
    //endregion

    public static boolean isBaseFolder(@NonNull String id, @NonNull String projId) {
        return id.equals(projId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        FileObjParcelablePlease.writeToParcel(this, dest, flags);
    }
}

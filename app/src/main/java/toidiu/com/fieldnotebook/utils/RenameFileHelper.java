package toidiu.com.fieldnotebook.utils;

import android.support.annotation.Nullable;

/**
 * Created by toidiu on 4/7/16.
 */
public class RenameFileHelper {

    @Nullable
    public String parentId;

    public boolean isValid() {
        return parentId != null;
    }
}

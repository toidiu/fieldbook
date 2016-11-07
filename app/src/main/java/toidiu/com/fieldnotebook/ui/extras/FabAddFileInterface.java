package toidiu.com.fieldnotebook.ui.extras;

import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.models.NewFileObj;

/**
 * Created by toidiu on 4/18/16.
 */
public interface FabAddFileInterface {
    void addItemClicked(NewFileObj newFileObj);

    void mergePdfClicked();

    void openPoPdfClicked(FileObj fileObj);
}

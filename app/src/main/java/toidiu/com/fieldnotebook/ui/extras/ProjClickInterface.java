package toidiu.com.fieldnotebook.ui.extras;

import toidiu.com.fieldnotebook.models.FileObj;

/**
 * Created by toidiu on 4/6/16.
 */
public interface ProjClickInterface {
    void folderClicked(FileObj fileObj);

    void fileClicked(FileObj fileObj);

    void addClicked(FileObj fileObj);
}

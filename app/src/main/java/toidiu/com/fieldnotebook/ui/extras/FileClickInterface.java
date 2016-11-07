package toidiu.com.fieldnotebook.ui.extras;

import toidiu.com.fieldnotebook.models.FileObj;

/**
 * Created by toidiu on 4/6/16.
 */
public interface FileClickInterface {
    void folderClicked(FileObj fileObj);

    void renameLongClicked(FileObj fileObj);

    void moveLongClicked(FileObj fileObj);

    void deleteLongClicked(FileObj fileObj);

    void shareClicked(FileObj fileObj);

    void multiShareClicked(FileObj fileObj);

    void printClicked(FileObj fileObj);

    void duplicateClicked(FileObj fileObj);

    void editClicked(FileObj fileObj);

    void doMerge();

}

package toidiu.com.fieldnotebook.utils;

import java.util.HashSet;
import java.util.Set;

import toidiu.com.fieldnotebook.models.FileObj;

/**
 * Created by toidiu on 4/7/16.
 */
public class MultiShareHelper {

    public Set<FileObj> shareMultiple;

    public MultiShareHelper() {
        this.shareMultiple = new HashSet<>();
    }
}

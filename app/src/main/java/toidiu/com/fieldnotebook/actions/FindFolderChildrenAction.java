package toidiu.com.fieldnotebook.actions;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.edisonwang.ps.annotations.ActionHelper;
import com.edisonwang.ps.annotations.Event;
import com.edisonwang.ps.annotations.EventProducer;
import com.edisonwang.ps.annotations.Field;
import com.edisonwang.ps.annotations.Kind;
import com.edisonwang.ps.annotations.ParcelableField;
import com.edisonwang.ps.lib.ActionRequest;
import com.edisonwang.ps.lib.ActionResult;
import com.edisonwang.ps.lib.RequestEnv;

import java.io.IOException;
import java.util.List;

import toidiu.com.fieldnotebook.actions.FindFolderChildrenAction_.PsFindFolderChildrenAction;
import toidiu.com.fieldnotebook.actions.events.FindFolderChildrenActionFailure;
import toidiu.com.fieldnotebook.actions.events.FindFolderChildrenActionSuccess;
import toidiu.com.fieldnotebook.models.FileObj;


/**
 * Created by toidiu on 3/28/16.
 */

@ActionHelper(args = {
        @Field(name = "searchName", kind = @Kind(clazz = String.class), required = true),
        @Field(name = "parentId", kind = @Kind(clazz = String.class), required = true),
        @Field(name = "folderOnly", kind = @Kind(clazz = boolean.class), required = true)
})
@EventProducer(generated = {
        @Event(postFix = "Success", fields = {
                @ParcelableField(name = "results", kind = @Kind(clazz = FileObj[].class))
        }),
        @Event(postFix = "Failure")
})
@com.edisonwang.ps.annotations.Action
public class FindFolderChildrenAction extends BaseAction {

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        FindFolderChildrenActionHelper helper = PsFindFolderChildrenAction.helper(request.getArguments(getClass().getClassLoader()));

        String search = "title contains '" + helper.searchName() + "'"
                + " and " + "title != '.DS_Store'"
                + " and " + "'" + helper.parentId() + "'" + " in parents";
        if (helper.folderOnly()) {
            search += " and " + "mimeType = '" + FOLDER_MIME + "'";
        }

        try {
            List<FileObj> dataFromApi = toFileObjs(executeQueryList(search));
            FileObj[] array = dataFromApi.toArray(new FileObj[dataFromApi.size()]);
//            Arrays.sort(array, FileObj.getComparator());
            return new FindFolderChildrenActionSuccess(array);
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return new FindFolderChildrenActionFailure();
        }

    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        Crashlytics.getInstance().core.logException(e);
        return new FindFolderChildrenActionFailure();
    }
}

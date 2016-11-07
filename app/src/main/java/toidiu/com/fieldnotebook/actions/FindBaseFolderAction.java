package toidiu.com.fieldnotebook.actions;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.edisonwang.ps.annotations.Action;
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

import toidiu.com.fieldnotebook.actions.FindBaseFolderAction_.PsFindBaseFolderAction;
import toidiu.com.fieldnotebook.actions.events.FindBaseFolderActionFailure;
import toidiu.com.fieldnotebook.actions.events.FindBaseFolderActionSuccess;
import toidiu.com.fieldnotebook.models.FileObj;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper(args = {
        @Field(name = "searchName", kind = @Kind(clazz = String.class), required = true)
})
@EventProducer(generated = {
        @Event(postFix = "Success", fields = {
                @ParcelableField(name = "results", kind = @Kind(clazz = FileObj[].class))
        }),
        @Event(postFix = "Failure")
})

public class FindBaseFolderAction extends BaseAction {

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        FindBaseFolderActionHelper helper = PsFindBaseFolderAction.helper(request.getArguments(getClass().getClassLoader()));

        if (credential.getSelectedAccountName() == null) {
            return new FindBaseFolderActionFailure();
        }

        String search = "title contains '" + helper.searchName() + "'"
                + " and " + "title != '.DS_Store'"
//                + " and " + " sharedWithMe=true "
                + " and " + "mimeType = '" + FOLDER_MIME + "'";

        try {
//            List<File> files = executeQueryList(search);



            List<FileObj> dataFromApi = toFileObjs(executeQueryList(search));
            FileObj[] array = dataFromApi.toArray(new FileObj[dataFromApi.size()]);
            return new FindBaseFolderActionSuccess(array);
//            return null;
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return new FindBaseFolderActionFailure();
        }
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        return new FindBaseFolderActionFailure();
    }
}

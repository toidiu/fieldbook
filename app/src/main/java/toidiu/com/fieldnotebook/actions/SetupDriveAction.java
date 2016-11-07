package toidiu.com.fieldnotebook.actions;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.edisonwang.ps.annotations.Action;
import com.edisonwang.ps.annotations.ActionHelper;
import com.edisonwang.ps.annotations.Event;
import com.edisonwang.ps.annotations.EventProducer;
import com.edisonwang.ps.annotations.Field;
import com.edisonwang.ps.annotations.Kind;
import com.edisonwang.ps.lib.ActionRequest;
import com.edisonwang.ps.lib.ActionResult;
import com.edisonwang.ps.lib.RequestEnv;
import com.google.api.services.drive.model.ParentReference;

import java.util.ArrayList;
import java.util.List;

import toidiu.com.fieldnotebook.actions.SetupDriveAction_.PsSetupDriveAction;
import toidiu.com.fieldnotebook.actions.events.SetupDriveActionFailure;
import toidiu.com.fieldnotebook.actions.events.SetupDriveActionSuccess;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper(args = {
        @Field(name = "parentId", kind = @Kind(clazz = String.class), required = true),
})
@EventProducer(generated = {
        @Event(postFix = "Success"),
        @Event(postFix = "Failure")
})

public class SetupDriveAction extends BaseAction {

    //~=~=~=~=~=~=~=~=~=~=~=~=Field
    private String parentId;

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {
        SetupDriveActionHelper helper = PsSetupDriveAction.helper(request.getArguments(getClass().getClassLoader()));
        parentId = helper.parentId();

        if (credential.getSelectedAccountName() == null) {
            return new SetupDriveActionFailure();
        }

        List<ParentReference> parents = new ArrayList<>();
        parents.add(new ParentReference().setId(parentId));

        if (checkAndCreateArchive(parents, parentId) && checkAndCreatePhotos(parents, parentId) && checkAndCreatePONumber(parents, parentId)) {
            prefs.setBaseFolderId(parentId);
            return new SetupDriveActionSuccess();
        } else {
            return new SetupDriveActionFailure();
        }

    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        Crashlytics.getInstance().core.logException(e);
        return new SetupDriveActionFailure();
    }
}

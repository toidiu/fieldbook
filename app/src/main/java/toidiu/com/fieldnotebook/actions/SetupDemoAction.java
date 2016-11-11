package toidiu.com.fieldnotebook.actions;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.edisonwang.ps.annotations.Action;
import com.edisonwang.ps.annotations.ActionHelper;
import com.edisonwang.ps.annotations.Event;
import com.edisonwang.ps.annotations.EventProducer;
import com.edisonwang.ps.annotations.Kind;
import com.edisonwang.ps.annotations.ParcelableField;
import com.edisonwang.ps.lib.ActionRequest;
import com.edisonwang.ps.lib.ActionResult;
import com.edisonwang.ps.lib.RequestEnv;

import toidiu.com.fieldnotebook.actions.events.SetupDemoActionFailure;
import toidiu.com.fieldnotebook.actions.events.SetupDemoActionSuccess;
import toidiu.com.fieldnotebook.actions.events.SetupDriveActionFailure;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper()
@EventProducer(generated = {
        @Event(postFix = "Success", fields = {
                @ParcelableField(name = "baseFolderId", kind = @Kind(clazz = String.class))
        }),
        @Event(postFix = "Failure")
})

public class SetupDemoAction extends BaseAction {

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {

        String demoFolderId = demoCheckAndCreateBase();
        if (demoFolderId != null) {
            prefs.setBaseFolderId(demoFolderId);
            return new SetupDemoActionSuccess(demoFolderId);
        } else {
            return new SetupDemoActionFailure();
        }

    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        Crashlytics.getInstance().core.logException(e);
        return new SetupDriveActionFailure();
    }
}

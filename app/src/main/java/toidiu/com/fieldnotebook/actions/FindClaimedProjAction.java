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
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import toidiu.com.fieldnotebook.actions.events.FindClaimedProjActionFailure;
import toidiu.com.fieldnotebook.actions.events.FindClaimedProjActionSuccess;
import toidiu.com.fieldnotebook.database.TransactionRunnable;
import toidiu.com.fieldnotebook.models.FileObj;


/**
 * Created by toidiu on 3/28/16.
 */
@Action
@ActionHelper()
@EventProducer(generated = {
        @Event(postFix = "Success", fields = {
                @ParcelableField(name = "results", kind = @Kind(clazz = FileObj[].class))
        }),
        @Event(postFix = "Failure")
})
public class FindClaimedProjAction extends BaseAction {

    @Override
    protected ActionResult process(Context context, ActionRequest request, RequestEnv env) throws Throwable {

        String search =
                "properties has { key='" + CLAIM_PROPERTY + "' and value='" + credential.getSelectedAccountName() + "' and visibility='PRIVATE' } "
                        + " and " + "title != '.DS_Store'"
                        + " and " + "'" + prefs.getBaseFolderId() + "'" + " in parents"
                        + " and " + "mimeType = '" + FOLDER_MIME + "'";

        try {
            List<FileObj> dataFromApi = toFileObjs(executeQueryList(search));
            final FileObj[] array = dataFromApi.toArray(new FileObj[dataFromApi.size()]);
            Arrays.sort(array, FileObj.getComparator());

            databaseHelper.runInTransaction(new TransactionRunnable() {
                @Override
                public void run() {
                    try {
                        Dao<FileObj, Integer> fileObjDao = databaseHelper.getClaimProjDao();
                        DeleteBuilder<FileObj, Integer> builder = fileObjDao.deleteBuilder();
                        builder.where().isNotNull("dbId");
                        builder.delete();

                        for (FileObj file : array) {
                            fileObjDao.createOrUpdate(file);
                        }

                    } catch (SQLException e) {
                        Crashlytics.getInstance().core.logException(e);
                    }
                }
            });


            return new FindClaimedProjActionSuccess(array);
        } catch (Exception e) {
            Crashlytics.getInstance().core.logException(e);
            return new FindClaimedProjActionFailure();
        }
    }

    @Override
    protected ActionResult onError(Context context, ActionRequest request, RequestEnv env, Throwable e) {
        return new FindClaimedProjActionFailure();
    }
}

package toidiu.com.fieldnotebook;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.edisonwang.ps.lib.EventService;
import com.edisonwang.ps.lib.PennStation;

import io.fabric.sdk.android.Fabric;
import toidiu.com.fieldnotebook.dagger.ApiModules;
import toidiu.com.fieldnotebook.dagger.AppModule;
import toidiu.com.fieldnotebook.dagger.ApplicationComponent;
import toidiu.com.fieldnotebook.dagger.ContextModule;
import toidiu.com.fieldnotebook.dagger.DaggerApplicationComponent;

/**
 * Created by toidiu on 3/28/16.
 */
public class FieldNotebookApplication extends Application {

    public static Context appContext;
    ApplicationComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.BUILD_TYPE.equals("release")) {
            Fabric.with(this, new Crashlytics());
        }
        appContext = this.getApplicationContext();

        PennStation.init(this, new PennStation.PennStationOptions(EventService.class)); //or extended class.

        appComponent = DaggerApplicationComponent.builder()
                .apiModules(new ApiModules())
                .contextModule(new ContextModule(this.getApplicationContext()))
                .appModule(new AppModule())
                .build();
    }

    public ApplicationComponent getAppComponent() {
        return appComponent;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}

package toidiu.com.fieldnotebook.dagger;

import javax.inject.Singleton;

import dagger.Component;
import toidiu.com.fieldnotebook.actions.BaseAction;
import toidiu.com.fieldnotebook.ui.BaseActivity;
import toidiu.com.fieldnotebook.ui.BaseDialogFrag;
import toidiu.com.fieldnotebook.ui.BaseFullScreenDialogFrag;
import toidiu.com.fieldnotebook.ui.MainActivity;
import toidiu.com.fieldnotebook.views.GenericViewHolder;

/**
 * Created by toidiu on 12/9/15.
 */
@Singleton
@Component(modules = {ApiModules.class, AppModule.class, ContextModule.class})
public interface ApplicationComponent {
    void inject(MainActivity obj);

    void inject(BaseActivity obj);

    void inject(BaseAction obj);

    void inject(BaseDialogFrag obj);

    void inject(BaseFullScreenDialogFrag obj);

    void inject(GenericViewHolder obj);
}

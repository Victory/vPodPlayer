package org.dfhu.vpodplayer.injection;

import android.content.Context;

import org.dfhu.vpodplayer.VPodPlayerApplication;
import org.dfhu.vpodplayer.util.ColorResource;
import org.dfhu.vpodplayer.util.DateUtil;
import org.dfhu.vpodplayer.util.StringsProvider;
import org.dfhu.vpodplayer.util.StringsProviderFromContext;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidModule {
    private final VPodPlayerApplication application;

    public AndroidModule(VPodPlayerApplication application) {
        this.application = application;
    }

    @Provides @Singleton
    public Context applicationContext() {
        return application.getApplicationContext();
    }

    @Provides @Singleton
    ColorResource colorResource(Context context) {
        return new ColorResource(context);
    }

    @Provides @Singleton
    DateUtil dateUtil(Context context) {
        return new DateUtil(context);
    }

    @Provides @Singleton
    public StringsProvider stringsProvider (Context context) {
        return StringsProviderFromContext.getInstance(context);
    }
}

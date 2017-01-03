package org.dfhu.vpodplayer.injection;

import android.content.Context;

import org.dfhu.vpodplayer.VPodPlayerApplication;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {
    private final VPodPlayerApplication application;

    public ContextModule(VPodPlayerApplication application) {
        this.application = application;
    }

    @Provides
    public Context applicationContext() {
        return application.getApplicationContext();
    }
}

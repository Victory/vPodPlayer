package org.dfhu.vpodplayer.injection;

import android.content.Context;

import org.dfhu.vpodplayer.PodPlayer;
import org.dfhu.vpodplayer.VPodPlayerApplication;
import org.dfhu.vpodplayer.util.ColorResource;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidModule {

    private final VPodPlayerApplication application;

    public AndroidModule(VPodPlayerApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    @ForApplication
    public Context provideApplicationContext() {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    public PodPlayer providePodPlayer() {
        return PodPlayer.builder()
                .context(provideApplicationContext())
                .build();
    }

    @Provides
    @Singleton
    public ColorResource provideColorResource() {
        return new ColorResource(provideApplicationContext());
    }
}

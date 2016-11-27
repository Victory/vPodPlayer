package org.dfhu.vpodplayer.injection;

import android.content.Context;

import org.dfhu.vpodplayer.PodPlayer;
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

    @Provides
    @Singleton
    public DateUtil provideDateUtil() {
        return new DateUtil(provideApplicationContext());
    }

    @Provides
    @Singleton
    public StringsProvider provideStringsProvider () {
        return StringsProviderFromContext.getInstance(provideApplicationContext());
    }
}

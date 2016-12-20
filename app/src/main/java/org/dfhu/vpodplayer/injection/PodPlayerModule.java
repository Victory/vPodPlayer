package org.dfhu.vpodplayer.injection;

import android.content.Context;

import org.dfhu.vpodplayer.PodPlayer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PodPlayerModule {

    @Provides
    @Singleton
    public PodPlayer providePodPlayer(Context context) {
        return PodPlayer.builder()
                .context(context)
                .build();
    }
}

package org.dfhu.vpodplayer.injection;

import android.content.Context;

import org.dfhu.vpodplayer.PodPlayer;


import dagger.Module;
import dagger.Provides;

@Module
public class PodPlayerModule {

    @Provides
    public PodPlayer podPlayer(Context context) {
        return PodPlayer.builder()
                .context(context)
                .build();
    }
}

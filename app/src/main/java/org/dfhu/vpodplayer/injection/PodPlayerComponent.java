package org.dfhu.vpodplayer.injection;

import org.dfhu.vpodplayer.fragment.PlayerFragment;

import dagger.Subcomponent;

@PodPlayerScope
@Subcomponent(
        modules = {
                PodPlayerModule.class
        }
)
public interface PodPlayerComponent {
    void inject(PlayerFragment v);
}

package org.dfhu.vpodplayer.injection;

import org.dfhu.vpodplayer.EpisodesRecyclerViewAdapter;
import org.dfhu.vpodplayer.PlayerControlsView;
import org.dfhu.vpodplayer.fragment.ShowListFragment;
import org.dfhu.vpodplayer.service.RefreshAllShowsService;
import org.dfhu.vpodplayer.service.UpdateSubscriptionService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(
        modules = {
                AndroidModule.class
        }
)
public interface AndroidComponent {
    void inject(RefreshAllShowsService v);
    void inject(ShowListFragment v);
    void inject(UpdateSubscriptionService v);
    void inject(EpisodesRecyclerViewAdapter v);
    void inject(PlayerControlsView v);

    PodPlayerComponent plus(PodPlayerModule v);
    EpisodeDownloadComponent plus(EpisodeDownloadModule v);
}

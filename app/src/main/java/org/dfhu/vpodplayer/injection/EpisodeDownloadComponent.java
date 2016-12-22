package org.dfhu.vpodplayer.injection;

import org.dfhu.vpodplayer.fragment.DownloadFragment;
import org.dfhu.vpodplayer.job.UpdateFeedsJobCreator;
import org.dfhu.vpodplayer.service.EpisodeDownloader;

import dagger.Subcomponent;

@EpisodeDownloadScope
@Subcomponent(
        modules = {
                EpisodeDownloadModule.class
        }
)
public interface EpisodeDownloadComponent {
    void inject(DownloadFragment downloadFragment);
    void inject(UpdateFeedsJobCreator v);

    EpisodeDownloader episodeDownloader();
}

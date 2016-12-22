package org.dfhu.vpodplayer.injection;

import android.app.DownloadManager;
import android.content.Context;

import org.dfhu.vpodplayer.service.EpisodeDownloader;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.util.PathsUtility;

import dagger.Module;
import dagger.Provides;

@Module
public class EpisodeDownloadModule {

    @Provides @EpisodeDownloadScope
    EpisodeDownloader episodeDownloader(Context context) {
        DownloadManager downloadManager =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        EpisodeDownloader.DownloadManagerWrapper downloadManagerWrapper =
                new EpisodeDownloader.DownloadManagerWrapper(downloadManager, new Episodes(context));
        PathsUtility pathsUtility =
                new PathsUtility(context.getApplicationContext());
        return new EpisodeDownloader(downloadManagerWrapper, pathsUtility);
    }
}

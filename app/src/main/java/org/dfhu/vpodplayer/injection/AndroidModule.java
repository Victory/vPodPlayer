package org.dfhu.vpodplayer.injection;

import android.app.DownloadManager;
import android.content.Context;

import org.dfhu.vpodplayer.service.EpisodeDownloader;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.util.ColorResource;
import org.dfhu.vpodplayer.util.DateUtil;
import org.dfhu.vpodplayer.util.PathsUtility;
import org.dfhu.vpodplayer.util.StringsProvider;
import org.dfhu.vpodplayer.util.StringsProviderFromContext;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidModule {

    @Provides
    public ColorResource provideColorResource(Context context) {
        return new ColorResource(context);
    }

    @Provides
    public DateUtil provideDateUtil(Context context) {
        return new DateUtil(context);
    }

    @Provides
    public StringsProvider provideStringsProvider (Context context) {
        return StringsProviderFromContext.getInstance(context);
    }

    @Provides
    public EpisodeDownloader providesEpisodeDownloader(Context context) {
        DownloadManager downloadManager =
                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        EpisodeDownloader.DownloadManagerWrapper downloadManagerWrapper =
                new EpisodeDownloader.DownloadManagerWrapper(downloadManager, new Episodes(context));
        PathsUtility pathsUtility =
                new PathsUtility(context.getApplicationContext());
        return new EpisodeDownloader(downloadManagerWrapper, pathsUtility);
    }
}

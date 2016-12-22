package org.dfhu.vpodplayer;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;

import com.evernote.android.job.JobManager;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.dfhu.vpodplayer.broadcastreceiver.DownloadCompleteBroadcastReceiver;
import org.dfhu.vpodplayer.injection.AndroidComponent;
import org.dfhu.vpodplayer.injection.AndroidModule;
import org.dfhu.vpodplayer.injection.DaggerAndroidComponent;
import org.dfhu.vpodplayer.injection.EpisodeDownloadComponent;
import org.dfhu.vpodplayer.injection.EpisodeDownloadModule;
import org.dfhu.vpodplayer.injection.PodPlayerComponent;
import org.dfhu.vpodplayer.injection.PodPlayerModule;
import org.dfhu.vpodplayer.job.UpdateFeedsJob;
import org.dfhu.vpodplayer.job.UpdateFeedsJobCreator;

public class VPodPlayerApplication extends Application {

    private AndroidComponent component;
    private PodPlayerComponent podPlayerComponent;
    private EpisodeDownloadComponent episodeDownloadComponent;

    private RefWatcher refWatcher;

    public static RefWatcher getRefWatcher(Context context) {
        VPodPlayerApplication app = (VPodPlayerApplication) context.getApplicationContext();
        return app.refWatcher;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        // Setup leaky Canary
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);

        // setup injection
        component = DaggerAndroidComponent.builder()
                .androidModule(new AndroidModule(this))
                .build();

        // Setup Update feeds job
        UpdateFeedsJobCreator updateFeedsJobCreator = new UpdateFeedsJobCreator();
        getEpisodeDownloadComponent().inject(updateFeedsJobCreator);
        JobManager.create(this).addJobCreator(updateFeedsJobCreator);
        UpdateFeedsJob.schedule();

        // setup global Reciever for download
        this.registerReceiver(
                new DownloadCompleteBroadcastReceiver(),
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        // Setup the Steho Chorme plugin hook
        Stetho.initializeWithDefaults(this);

    }

    public AndroidComponent component() {
        return component;
    }

    public void releasePodPlayerComponent() {
        podPlayerComponent = null;
    }

    public PodPlayerComponent getPodPlayerComponent() {
        if (podPlayerComponent == null) {
            podPlayerComponent = component.plus(new PodPlayerModule());
        }
        return podPlayerComponent;
    }

    public EpisodeDownloadComponent getEpisodeDownloadComponent() {
        if (episodeDownloadComponent == null) {
            episodeDownloadComponent = component.plus(new EpisodeDownloadModule());
        }
        return episodeDownloadComponent;
    }
}

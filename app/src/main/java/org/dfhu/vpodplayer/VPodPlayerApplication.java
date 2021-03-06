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
import org.dfhu.vpodplayer.fragment.PlayerFragment;
import org.dfhu.vpodplayer.injection.AndroidModule;
import org.dfhu.vpodplayer.injection.ContextModule;
import org.dfhu.vpodplayer.injection.PodPlayerModule;
import org.dfhu.vpodplayer.job.UpdateFeedsJob;
import org.dfhu.vpodplayer.job.UpdateFeedsJobCreator;
import org.dfhu.vpodplayer.service.RefreshAllShowsService;
import org.dfhu.vpodplayer.service.UpdateSubscriptionService;

import dagger.Component;

public class VPodPlayerApplication extends Application {

    @Component(modules = {
            ContextModule.class
    })
    public interface ContextComponent {
        Context applicationContext();
    }

    @Component(
            dependencies = {
                    ContextComponent.class
            },
            modules =  {
                    PodPlayerModule.class
    })
    public interface PodPlayerComponent {
        PodPlayer podPlayer();
    }

    @Component(
            dependencies = {
                    ContextComponent.class,
                    PodPlayerComponent.class
            },
            modules = {
                    AndroidModule.class,
    })
    public interface  ApplicationComponent {
        void inject(VPodPlayerApplication application);
        void inject(PlayerFragment playerFragment);
        void inject(PlayerControlsView playerControlsView);
        void inject(EpisodesRecyclerViewAdapter episodesRecyclerViewAdapter);
        void inject(RefreshAllShowsService refreshAllShowsService);
        void inject(UpdateSubscriptionService updateSubscriptionService);
        void inject(UpdateFeedsJobCreator updateFeedsJobCreator);
    }

    private ApplicationComponent component;

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
        ContextComponent contextComponent = DaggerVPodPlayerApplication_ContextComponent.builder()
                .contextModule(new ContextModule(this))
                .build();

        PodPlayerComponent podPlayerComponent = DaggerVPodPlayerApplication_PodPlayerComponent.builder()
                .contextComponent(contextComponent)
                .build();

        component = DaggerVPodPlayerApplication_ApplicationComponent.builder()
                .podPlayerComponent(podPlayerComponent)
                .contextComponent(contextComponent)
                .build();
        component.inject(this);

        // Setup Update feeds job
        UpdateFeedsJobCreator updateFeedsJobCreator = new UpdateFeedsJobCreator();
        component.inject(updateFeedsJobCreator);
        JobManager.create(this).addJobCreator(updateFeedsJobCreator);
        UpdateFeedsJob.schedule();

        // setup global Reciever for download
        this.registerReceiver(
                new DownloadCompleteBroadcastReceiver(),
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        // Setup the Steho Chorme plugin hook
        Stetho.initializeWithDefaults(this);

    }

    public ApplicationComponent component() {
        return component;
    }
}

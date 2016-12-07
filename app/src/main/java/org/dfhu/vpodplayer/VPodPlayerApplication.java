package org.dfhu.vpodplayer;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.dfhu.vpodplayer.fragment.PlayerFragment;
import org.dfhu.vpodplayer.injection.AndroidModule;
import org.dfhu.vpodplayer.broadcastreceiver.DownloadCompleteBroadcastReceiver;
import org.dfhu.vpodplayer.service.RefreshAllShowsService;
import org.dfhu.vpodplayer.service.UpdateSubscriptionService;

import javax.inject.Singleton;

import dagger.Component;

public class VPodPlayerApplication extends Application {

    @Singleton
    @Component(modules = AndroidModule.class)
    public interface  ApplicationComponent {
        void inject(VPodPlayerApplication application);
        void inject(PlayerFragment playerFragment);
        void inject(PlayerControlsView playerControlsView);
        void inject(EpisodesRecyclerViewAdapter episodesRecyclerViewAdapter);
        void inject(RefreshAllShowsService refreshAllShowsService);
        void inject(UpdateSubscriptionService updateSubscriptionService);
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

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);

        component = DaggerVPodPlayerApplication_ApplicationComponent.builder()
                .androidModule(new AndroidModule(this))
                .build();
        component.inject(this);

        this.registerReceiver(
                new DownloadCompleteBroadcastReceiver(),
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public ApplicationComponent component() {
        return component;
    }
}

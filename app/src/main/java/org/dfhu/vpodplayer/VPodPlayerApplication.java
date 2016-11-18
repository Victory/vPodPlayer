package org.dfhu.vpodplayer;

import android.app.Application;
import android.app.DownloadManager;
import android.content.IntentFilter;

import org.dfhu.vpodplayer.fragment.PlayerFragment;
import org.dfhu.vpodplayer.injection.AndroidModule;
import org.dfhu.vpodplayer.util.ColorResource;
import org.dfhu.vpodplayer.util.DownloadCompleteBroadcastReceiver;

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
    }

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
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

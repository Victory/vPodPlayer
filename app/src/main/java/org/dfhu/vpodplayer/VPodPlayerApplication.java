package org.dfhu.vpodplayer;

import android.app.Application;
import android.content.Context;

import org.dfhu.vpodplayer.fragment.PlayerFragment;
import org.dfhu.vpodplayer.injection.AndroidModule;

import javax.inject.Singleton;

import dagger.Component;

public class VPodPlayerApplication extends Application {

    @Singleton
    @Component(modules = AndroidModule.class)
    public interface  ApplicationComponent {
        void inject(VPodPlayerApplication application);
        void inject(PlayerFragment playerFragment);
    }

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerVPodPlayerApplication_ApplicationComponent.builder()
                .androidModule(new AndroidModule(this))
                .build();
        component.inject(this);
    }

    public ApplicationComponent component() {
        return component;
    }
}

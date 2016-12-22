package org.dfhu.vpodplayer.fragment;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.squareup.leakcanary.RefWatcher;

import org.dfhu.vpodplayer.VPodPlayerApplication;

abstract class VicFragment<T extends Application> extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = VPodPlayerApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    abstract void inject();

    @SuppressWarnings("unchecked")
    public T getRealApplication() {
        return ((T) getActivity().getApplication());
    }

}

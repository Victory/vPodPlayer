package org.dfhu.vpodplayer.fragment;

import android.support.v4.app.Fragment;

import com.squareup.leakcanary.RefWatcher;

import org.dfhu.vpodplayer.VPodPlayerApplication;

public class VicFragment extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = VPodPlayerApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}

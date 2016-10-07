package org.dfhu.vpodplayer.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.dfhu.vpodplayer.PlayerControlsView;
import org.dfhu.vpodplayer.PodPlayer;
import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.VPodPlayerApplication;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.sqlite.Episodes;

import javax.inject.Inject;

public class PlayerFragment extends Fragment {

    @Inject
    PodPlayer podPlayer;

    private boolean isPlaying = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((VPodPlayerApplication) getActivity().getApplication()).component().inject(this);
    }

    @Override
    public void onDestroy() {
        podPlayer.end();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        //if (1 == 1) return view; // while debugging ControlsView

        PlayerControlsView controls = (PlayerControlsView) view.findViewById(R.id.playerControls);
        final Context context = inflater.getContext().getApplicationContext();

        int episodeId = getArguments().getInt("episodeId");
        Episodes db = new Episodes(getActivity().getApplicationContext());
        final Episode episode = db.getById(episodeId);

        Uri uri = Uri.parse(episode.url);
        //Uri uri = Uri.parse("http://192.168.1.6:3000/pm.mp3");

        podPlayer.startPlayingUri(uri);
        isPlaying = true;

        controls.setOnCenterClickListener(new PlayerControlsView.OnCenterClickListener() {
            @Override
            public void click(PlayerControlsView playerControlsView) {

                Log.d("PlayerFragment", "clicked: " + episode.title);
                if (isPlaying) {
                    podPlayer.setPlayWhenReady(false);
                    playerControlsView.setCenterColor(PlayerControlsView.INNER_COLOR_PAUSE);
                } else {
                    podPlayer.setPlayWhenReady(true);
                    playerControlsView.setCenterColor(PlayerControlsView.INNER_COLOR_PLAY);
                }

                isPlaying = !isPlaying;
            }
        });

        controls.setOnPositionListener(new PlayerControlsView.OnPositionListener() {
            @Override
            public void positionChange(double positionPercent) {
                long duration = podPlayer.getDuration();
                double seek = duration * positionPercent;
                podPlayer.seekTo((long) seek);
            }
        });

        return view;
    }
}

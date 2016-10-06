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

        final Button playPauseButton = (Button) view.findViewById(R.id.playPauseButton);
        controls.setOnCenterClickListener(new PlayerControlsView.OnCenterClickListener() {
            @Override
            public void click(MotionEvent event) {

                Log.d("PlayerFragment", "clicked: " + episode.title);
                String msg;
                if (isPlaying) {
                    podPlayer.setPlayWhenReady(false);
                    msg = "Play";
                } else {
                    podPlayer.setPlayWhenReady(true);
                    msg = "Pause";
                }
                playPauseButton.setText(msg);

                isPlaying = !isPlaying;
            }
        });

        /*
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        */

        return view;
    }
}

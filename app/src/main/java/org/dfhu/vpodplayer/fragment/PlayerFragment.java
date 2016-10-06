package org.dfhu.vpodplayer.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

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
    private static SimpleExoPlayer exoPlayer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((VPodPlayerApplication) getActivity().getApplication()).component().inject(this);
    }

    @Override
    public void onDestroy() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        final Context context = inflater.getContext().getApplicationContext();

        int episodeId = getArguments().getInt("episodeId");
        Episodes db = new Episodes(getActivity().getApplicationContext());
        final Episode episode = db.getById(episodeId);

        Uri uri = Uri.parse(episode.url);
        //Uri uri = Uri.parse("http://192.168.1.6:3000/pm.mp3");

        preparePlayer(context, uri);

        final Button playPauseButton = (Button) view.findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            private long curPo;
            @Override
            public void onClick(View v) {

                Log.d("PlayerFragment", "clicked: " + episode.title);
                String msg;
                if (isPlaying) {
                    curPo = exoPlayer.getCurrentPosition();
                    exoPlayer.setPlayWhenReady(false);
                    msg = "Play";
                } else {
                    exoPlayer.setPlayWhenReady(true);
                    msg = "Pause";
                }
                playPauseButton.setText(msg);

                isPlaying = !isPlaying;
            }
        });

        return view;
    }

    public void preparePlayer(Context context, Uri uri) {
        if (exoPlayer != null && exoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) {
            return;
        }
        exoPlayer = podPlayer.getPlayer();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, "vPodPlayer");
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
        exoPlayer.prepare(mediaSource);
    }
}

package org.dfhu.vpodplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;


public class PodPlayer {
    private final Context context;
    private SimpleExoPlayer player;

    private PodPlayer(Context applicationContext, SimpleExoPlayer player) {
        this.player = player;
        this.context = applicationContext;
    }

    public static class Builder {
        private Context context;

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public PodPlayer build() {
            return new PodPlayer(this.context, getExoPlayer(this.context));
        }

        private static SimpleExoPlayer getExoPlayer(Context context) {
            Handler mainHandler = new Handler();
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector =
                    new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);

            LoadControl loadControl = new DefaultLoadControl();

            return ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public void startPlayingUri(Uri uri) {
        if (player == null) {
            player = Builder.getExoPlayer(context);
        }

        if (player.getPlaybackState() == ExoPlayer.STATE_READY) {
            return;
        }

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, "vPodPlayer");
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
        player.prepare(mediaSource);
        //player.setPlayWhenReady(true);
    }

        private static SimpleExoPlayer getExoPlayer(Context context) {
            Handler mainHandler = new Handler();
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector =
                    new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);

            LoadControl loadControl = new DefaultLoadControl();

            return ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
        }
    public void setPlayWhenReady(boolean playWhenReady) {
        player.setPlayWhenReady(playWhenReady);
    }

    /** release and destroy player */
    public void end() {
        if (player != null) {
            player.release();
            player = null;
        }
    }
}

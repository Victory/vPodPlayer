package org.dfhu.vpodplayer;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;


public class PodPlayer {
    private static SimpleExoPlayer player;

    private PodPlayer(Context applicationContext) {
        player = getExoPlayer(applicationContext);
    }

    public static class Builder {
        private Context context;

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public PodPlayer build() {
            return new PodPlayer(this.context);
        }
    }

    public static Builder builder() {
        return new Builder();
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

    public SimpleExoPlayer getPlayer() {
        return player;
    }
}

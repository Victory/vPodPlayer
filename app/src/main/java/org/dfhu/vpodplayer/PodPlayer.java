package org.dfhu.vpodplayer;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

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
    private static final String TAG = PodPlayer.class.getName();

    private final Context context;
    //private final RemoteControlClient remoteControlClient;
    private MediaSessionCompat mediaSession;
    private SimpleExoPlayer player;
    private AudioManager audioManager;

    private PodPlayer(Context applicationContext, SimpleExoPlayer player) {
        this.player = player;
        this.context = applicationContext;
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);

        mediaSession = new MediaSessionCompat(context, TAG);

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                Log.d(TAG, "onMediaButtonEvent() called with: mediaButtonEvent = [" + mediaButtonEvent + "]");

                audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID);

                return super.onMediaButtonEvent(mediaButtonEvent);
            }
        });
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);

        /*
        remoteControlClient = new RemoteControlClient(PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_MEDIA_BUTTON), 0));
        audioManager.registerRemoteControlClient(remoteControlClient);
        */
    }

    public static class Builder {
        private Context context;

        public Builder context(Context applicationContext) {
            this.context = applicationContext;
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

        /*
        RemoteControlClient.MetadataEditor editor = remoteControlClient.editMetadata(true);
        editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "Some album");
        editor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "The Artist");
        editor.apply();
        */

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, "vPodPlayer");
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        player.setPlayWhenReady(playWhenReady);
    }

    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public long getDuration() {
        return player.getDuration();
    }

    public void seekTo(long positionMs) {
        player.seekTo(positionMs);
    }

    /** release and destroy player */
    public void end() {
        if (player != null) {
            player.release();
            player = null;
            //audioManager.unregisterRemoteControlClient(remoteControlClient);
        }
    }
}

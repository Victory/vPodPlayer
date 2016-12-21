package org.dfhu.vpodplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

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

import javax.inject.Singleton;

@Singleton
public class PodPlayer {
    public static final String TAG = PodPlayer.class.getName();

    private final Context context;
    private MediaSessionCompat mediaSession;
    private SimpleExoPlayer player;
    private AudioManager audioManager;

    private PodPlayer(Context applicationContext, final SimpleExoPlayer player) {
        this.player = player;
        this.context = applicationContext;
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);

        ComponentName componentName = new ComponentName(context, RemoveControlReceiver.class);
        mediaSession = new MediaSessionCompat(context, TAG, componentName, null);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            int cnt = 0;

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                Log.d(TAG, "onMediaButtonEvent() called with: mediaButtonEvent = [" + mediaButtonEvent + "]");

                String mediaAction = mediaButtonEvent.getAction();

                if (!Intent.ACTION_MEDIA_BUTTON.equals(mediaAction)) {
                    return false;
                }

                KeyEvent event =
                        (KeyEvent) mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                int keyCode = event.getKeyCode();

                String msg = "other";
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        setPlayWhenReady(!player.getPlayWhenReady());
                        msg = "play/pause";
                        break;
                    case KeyEvent.KEYCODE_MEDIA_STOP:
                        setPlayWhenReady(false);
                        msg = "stop";
                        break;
                    case KeyEvent.KEYCODE_MEDIA_CLOSE:
                        setPlayWhenReady(false);
                        msg = "close";
                        break;
                    case KeyEvent.KEYCODE_MEDIA_EJECT:
                        setPlayWhenReady(false);
                        msg = "eject";
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        msg = "play";
                        setPlayWhenReady(true);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        msg = "pause";
                        setPlayWhenReady(false);
                        break;
                }

                cnt += 1;
                Toast.makeText(context, "cnt: " + cnt + " msg: " + msg + " keycode:" + keyCode, Toast.LENGTH_SHORT).show();
                audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID);


                return true;
            }
        });
        mediaSession.setActive(true);

        setPlaybackStateStopped();
    }

    private void setPlaybackStateStopped() {
        PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE)
                .setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0)
                .build();
        mediaSession.setPlaybackState(playbackState);
    }

    private void setPlaybackStatePaused() {
        PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_STOP)
                .setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0)
                .build();
        mediaSession.setPlaybackState(playbackState);
    }

    private void setPlaybackStatePlaying() {
        PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_STOP)
                .setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0)
                .build();
        mediaSession.setPlaybackState(playbackState);
    }

    public static class RemoveControlReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(context, "Button pushed before", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
            if (!Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
                return;
            }

            Toast.makeText(context, "Button pushed after", Toast.LENGTH_SHORT).show();

        }
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

    /**
     * Start playing the mp3 with the given url
     *
     * @param uri - mp3 to play
     * @return - true if not already playing and start play state is true
     */
    public boolean startPlayingUri(Uri uri) {
        if (player == null) {
            player = Builder.getExoPlayer(context);
        }

        if (player.getPlaybackState() == ExoPlayer.STATE_READY) {
            return false;
        }


        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, "vPodPlayer");
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
        player.prepare(mediaSource);
        setPlayWhenReady(true);

        return true;
    }

    /**
     * Set the title to show on the media title
     * @param title - name of the episode
     * @return - true if title was set
     */
    public boolean setMetaDataTitle(String title) {

        if (mediaSession == null) {
            return false;
        }

        MediaMetadataCompat mediaMetadataCompat = new MediaMetadataCompat.Builder()
                //.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                //.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .build();
        mediaSession.setMetadata(mediaMetadataCompat);

        return true;
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        player.setPlayWhenReady(playWhenReady);

        if (playWhenReady) {
            setPlaybackStatePlaying();
        } else {
            setPlaybackStatePaused();
        }
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

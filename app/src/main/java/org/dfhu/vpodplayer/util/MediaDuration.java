package org.dfhu.vpodplayer.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class MediaDuration {
    public final Context context;

    public MediaDuration(Context context) {
        this.context = context;
    }

    public int get(String localUri) {
        Uri uri = Uri.parse(localUri);
        MediaPlayer mediaPlayer = MediaPlayer.create(context, uri);
        int duration = mediaPlayer.getDuration();
        mediaPlayer.release();
        return duration;
    }
}

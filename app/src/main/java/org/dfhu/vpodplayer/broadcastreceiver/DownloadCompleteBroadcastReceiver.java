package org.dfhu.vpodplayer.broadcastreceiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import org.dfhu.vpodplayer.fragment.DownloadFragment;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.util.MediaDuration;

import rx.Observable;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.util.async.Async;

public class DownloadCompleteBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = DownloadCompleteBroadcastReceiver.class.getName();

    @Override
    public void onReceive(final Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        final long downloadId = bundle.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
        final MediaDuration mediaDuration = new MediaDuration(context);

        Async.start(new Func0<Observable<DownloadFragment.DownloadRow>>() {
            @Override
            public Observable<DownloadFragment.DownloadRow> call() {
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(downloadId);

                DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                Cursor cursor = null;
                try {
                    cursor = dm.query(q);

                    if (!cursor.moveToFirst()) {
                        Log.e("DownloadFragment", "could not find download by id");
                        return Observable.error(new Throwable("Could not find download by id"));
                    }

                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    DownloadFragment.DownloadRow dr = new DownloadFragment.DownloadRow(cursor);
                    DownloadFragment.UpdateProgress.publish(dr);
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        Episodes db = new Episodes(context);
                        Log.d(TAG, "downloadId on BroadcastReceiver:" + downloadId + " downloadRow: " + dr);
                        Episode episode = db.getByDownloadId(downloadId);
                        episode.isDownloaded = 1;
                        episode.localUri = dr.localUri;
                        episode.sizeInBytes = dr.totalSize;
                        episode.duration = mediaDuration.get(episode.localUri);
                        db.addOrUpdate(episode);
                        DownloadFragment.ShowPlayButton.publish(episode);
                    }

                    return Observable.just(dr);

                } finally {
                    if (cursor != null) cursor.close();
                }
            }
        }, Schedulers.io()).subscribe();

    }
}

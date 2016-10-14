package org.dfhu.vpodplayer.fragment;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.sqlite.Episodes;

import java.io.File;

import rx.Observable;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.util.async.Async;


public class DownloadFragment extends Fragment {

    private static final String TAG = DownloadFragment.class.getSimpleName();

    DownloadManager dm;
    Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();

        context.registerReceiver(
                onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        context.registerReceiver(
                onNotificationClicked,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

        if (dm == null) {
            dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            int episodeId = getArguments().getInt("episodeId");
            Episodes db = new Episodes(context);
            final Episode episode = db.getById(episodeId);

            startDownload(episode);
        }

        DownloadManager.Query q = new DownloadManager.Query();
        Cursor c = dm.query(q);

        if (c.moveToFirst()) {
            do {
                DownloadRow downloadRow = new DownloadRow(c);
                downloadRow.toString();
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        context.unregisterReceiver(onComplete);
        context.unregisterReceiver(onNotificationClicked);
        super.onDestroy();
    }

    private void startDownload(Episode episode) {
        Log.d(TAG, "startDownload() called with: episode = [" + episode + "]");
        File[] externalMediaDirs = context.getExternalMediaDirs();

        if (externalMediaDirs.length == 0) {
            return;
        }

        //Uri uri = Uri.parse(episode.url);
        Uri uri = Uri.parse("http://192.168.1.6:3000/pm.mp3");

        File dir = new File(externalMediaDirs[0], "episodes");
        dir = new File(dir, "test");
        if (!dir.mkdirs()) {
            Log.d("DownloadFragment", "Directory already exists: " + dir.getAbsolutePath());
        }

        Uri destinationUri = Uri.fromFile(new File(dir, episode.id + ".mp3"));

        int allowedNetworkTypes = DownloadManager.Request.NETWORK_WIFI;

        // allow Mobile data when emulating
        if (Build.PRODUCT.contains("sdk_google")) {
            allowedNetworkTypes |= DownloadManager.Request.NETWORK_MOBILE;
        }

        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(allowedNetworkTypes)
                .setAllowedOverRoaming(false)
                .setDescription("id: " + episode.id)
                .setTitle(episode.title)
                .setDestinationUri(destinationUri);

        dm.enqueue(request);
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            final long id = bundle.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
            final Context appContext = context;

            Async.start(new Func0<Observable<DownloadRow>>() {
                @Override
                public Observable<DownloadRow> call() {
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(id);

                    Cursor cursor = null;
                    try {
                        cursor = dm.query(q);

                        if (!cursor.moveToFirst()) {
                            Log.e("DownloadFragment", "could not find download by id");
                            return Observable.error(new Throwable("Could not find download by id"));
                        }

                        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        DownloadRow dr = new DownloadRow(cursor);
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            Toast.makeText(appContext, "Downloaded: " + dr.title, Toast.LENGTH_LONG).show();
                        }
                        return Observable.just(dr);

                    } finally {
                        if (cursor != null) cursor.close();
                    }
                }
            }, Schedulers.io()).subscribe();

        }
    };

    BroadcastReceiver onNotificationClicked = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    /** Represents the values in a curursor download */
    private static class DownloadRow {

        public final String destination;
        public final String title;
        public final String description;
        public final String uri;
        public final int status;
        public final Long totalSize;
        public final Long bytesSoFar;
        public final String localUri;
        public final String reason;
        public final long id;

        public DownloadRow(Cursor c) {
            id = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
            destination = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
            description = c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));
            uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
            status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            totalSize = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            bytesSoFar = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            localUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            reason = c.getString(c.getColumnIndex(DownloadManager.COLUMN_REASON));
        }

        @Override
        public String toString() {

            String bsf = (bytesSoFar != null) ? Long.toString(bytesSoFar) : "??";
            String ts = (totalSize != null) ? Long.toString(totalSize) : "??";
            String u = (uri != null) ? uri : "uri unknown";
            String t = (title != null) ? title : "title unknown";
            String s = "other";

            switch (status) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    s = "success";
                    break;
                case DownloadManager.STATUS_FAILED:
                    s = "failed";
                    break;
                case DownloadManager.STATUS_PAUSED:
                    s = "paused";
                    break;
                case DownloadManager.STATUS_PENDING:
                    s = "pending";
                    break;
                case DownloadManager.STATUS_RUNNING:
                    s = "running";
                    break;
            }
            String msg =
                  id + " - " +  u + " - " + s + " - " + bsf + "/" + ts + " " + t;
            return msg;
        }
    }
}

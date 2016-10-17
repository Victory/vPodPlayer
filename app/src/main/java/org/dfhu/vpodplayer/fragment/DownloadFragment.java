package org.dfhu.vpodplayer.fragment;

import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.VPodPlayer;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.sqlite.Episodes;

import java.io.File;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import rx.util.async.Async;


public class DownloadFragment extends Fragment {

    private static final String TAG = DownloadFragment.class.getSimpleName();

    DownloadManager dm;
    Context context;

    CompositeSubscription subs = new CompositeSubscription();

    private static class UpdateProgress {
        private UpdateProgress() {}
        private static PublishSubject<DownloadRow> subject = PublishSubject.create();

        static void publish(DownloadRow v) { subject.onNext(v); }
        static Observable<DownloadRow> getEvents() { return subject; }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_download, container, false);
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

            long downloadId = startDownload(episode);

            if (downloadId > 0) {
                updateUi(downloadId);
                TextView downloadTitle = (TextView) view.findViewById(R.id.downloadTitle);
                downloadTitle.setText(episode.title);
            }

            //debugDownloadQueue();
        }


        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.downloadProgressBar);
        Subscription sub = UpdateProgress.getEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DownloadRow>() {
                    private int lastBytesSoFar = 0;
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted() called updateProgress");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError() called with updateProgress: e = [" + e + "]");
                    }

                    @Override
                    public void onNext(DownloadRow downloadRow) {
                        Log.d(TAG, "onNext() called with: downloadRow updateProgress = [" + downloadRow + "]");

                        final int bytesSoFar = downloadRow.bytesSoFar;
                        // don't update progress if the the num bytes hasn't change
                        if (lastBytesSoFar == bytesSoFar) {
                            Log.d(TAG, "bytes so far has not changed");
                            return;
                        }
                        lastBytesSoFar = bytesSoFar;

                        final int totalSize = downloadRow.totalSize;
                        progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setMax(totalSize);
                                progressBar.setProgress(bytesSoFar);
                            }
                        });
                    }
                });

        subs.add(sub);

        return view;
    }

    private void debugDownloadQueue() {
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        context.unregisterReceiver(onComplete);
        context.unregisterReceiver(onNotificationClicked);
        subs.unsubscribe();
        subs = null;
        super.onDetach();
    }

    private long startDownload(Episode episode) {
        Log.d(TAG, "startDownload() called with: episode = [" + episode + "]");
        File[] externalMediaDirs = context.getExternalMediaDirs();

        if (externalMediaDirs.length == 0) {
            return 0;
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

        return dm.enqueue(request);

    }

    private void updateUi(final long downloadId) {

        Subscription sub = Observable.interval(0, 800, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<Long, Observable<DownloadRow>>() {
                    @Override
                    public Observable<DownloadRow> call(Long aLong) {

                        DownloadManager.Query q = new DownloadManager.Query();
                        q.setFilterById(downloadId);
                        Cursor cursor = dm.query(q);


                        try {
                            if (!cursor.moveToFirst()) {
                                return Observable.error(
                                        new Throwable("Could not find cursor in download manager"));
                            }
                            return Observable.just(new DownloadRow(cursor));
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DownloadRow>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted() progress");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: progress", e);
                    }

                    @Override
                    public void onNext(DownloadRow downloadRow) {
                        Log.d(TAG, "onNext() called with: downloadRow = [" + downloadRow + "]");
                        UpdateProgress.publish(downloadRow);
                    }
                });
        subs.add(sub);
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
                        UpdateProgress.publish(dr);
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        }

                        subs.unsubscribe();

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

        final String destination;
        final String title;
        final String description;
        final String uri;
        final int status;
        final Integer totalSize;
        final Integer bytesSoFar;
        final String localUri;
        final int reason;
        final long id;

        public DownloadRow(Cursor c) {
            id = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
            destination = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
            description = c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));
            uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
            status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            totalSize = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            bytesSoFar = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            localUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
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

            String r = "other";

            switch (reason) {
                case DownloadManager.ERROR_CANNOT_RESUME:
                    r = "cannot resume";
                    break;
                case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                    r = "device not found";
                    break;
                case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                    r = "file already exists";
                    break;
                case DownloadManager.ERROR_FILE_ERROR:
                    r = "file error";
                    break;
                case DownloadManager.ERROR_HTTP_DATA_ERROR:
                    r = "http data error";
                    break;
                case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                    r = "insufficient space";
                    break;
                case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                    r = "too many redirects";
                    break;
            }

            String msg =
                  id + " - " +  u + " - " + s + " - " + r + " - " + bsf + "/" + ts + " " + t + " - " + localUri;
            return msg;
        }
    }
}

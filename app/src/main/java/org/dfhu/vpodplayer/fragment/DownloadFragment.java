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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.dfhu.vpodplayer.EpisodesRecyclerViewAdapter;
import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.util.MediaDuration;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import rx.util.async.Async;


public class DownloadFragment extends VicFragment {

    public static final String TAG = DownloadFragment.class.getSimpleName();

    DownloadManager dm;
    Context context;
    TextView downloadTitle;
    ProgressBar progressBar;
    Button playDownloadButton;

    long downloadId;

    CompositeSubscription subs = new CompositeSubscription();

    public static class UpdateProgress {
        private UpdateProgress() {}
        private static PublishSubject<DownloadRow> subject = PublishSubject.create();

        public static void publish(DownloadRow v) { subject.onNext(v); }
        static Observable<DownloadRow> getEvents() { return subject; }
    }

    public static class ShowPlayButton {
        private ShowPlayButton() {}
        private static PublishSubject<Episode> subject = PublishSubject.create();

        public static void publish(Episode v) { subject.onNext(v); }
        static Observable<Episode> getEvents() { return subject; }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            downloadId = savedInstanceState.getLong("downloadId");
            subscribeToShowPlayButton(downloadId);
            updateUi(downloadId);
        }

        context.registerReceiver(
                onNotificationClicked,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_download, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.downloadProgressBar);
        downloadTitle = (TextView) view.findViewById(R.id.downloadTitle);

        playDownloadButton = (Button) view.findViewById(R.id.playDownloadButton);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        downloadTitle.setText(R.string.initializing);

        progressBar.setIndeterminate(true);
        progressBar.setMax(0);
        progressBar.setProgress(0);

        dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        playDownloadButton.setTag(downloadId);
    }

    @Override
    public void onResume() {
        super.onResume();
        subscribeToUiProgressUpdate();
        // this is a new download not a configuration change
        if (downloadId == 0) {
            deferDownloadQueue();
        } else {
            showPlayButtonIfReadyToPlay();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("downloadId", downloadId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        progressBar = null;
        downloadTitle = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /** async wrapper for queueDownload */
    public void deferDownloadQueue() {
        Subscription sub = Observable.just("start")
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted() called ddq");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: starting queueDownload", e);
                    }

                    @Override
                    public void onNext(String s) {
                        queueDownload();
                    }
                });

        subs.add(sub);
    }

    private void showPlayButtonIfReadyToPlay() {
        Async.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Episodes db = new Episodes(context);
                Episode episode = db.getByDownloadId(downloadId);
                if (episode.isReadyToPlay()) {
                    ShowPlayButton.publish(episode);
                }
                return null;
            }
        }).subscribe();
    }


    /** Checks to see if we are already downloading the file, if not enqueues the file */
    public void queueDownload() {

        int episodeId = getArguments().getInt("episodeId");
        Episodes db = new Episodes(context);
        final Episode episode = db.getById(episodeId);

        if (episode.isReadyToPlay()) {
            Log.d(TAG, "Episode marked ready to play: " + episode);
            return;
        }

        if (episode.downloadId > 0) {
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(episode.downloadId);
            Cursor cursor = dm.query(q);

            if (cursor.moveToFirst()) {
                DownloadRow dr = new DownloadRow(cursor);
                if (dr.status != DownloadManager.STATUS_FAILED) {
                    Log.d(TAG, "onCreateView: already has downloadId " + dr);
                    episode.localUri = dr.localUri;
                    episode.isDownloaded = 1;
                    episode.localUri = dr.localUri;
                    episode.sizeInBytes = dr.totalSize;
                    MediaDuration mediaDuration = new MediaDuration(context);
                    episode.duration = mediaDuration.get(episode.localUri);
                    db.addOrUpdate(episode);
                    showPlayButton(episode);
                    UpdateProgress.publish(dr);
                    return;
                }
            }
        }

        downloadId = startDownload(episode);
        Log.d(TAG, "queueDownload() called: downloadId added: " + downloadId);
        episode.downloadId = downloadId;
        subscribeToShowPlayButton(downloadId);
        db.addOrUpdate(episode);

        if (downloadId > 0) {
            updateUi(downloadId);
        }

        playDownloadButton.setTag(downloadId);
        playDownloadButton.setVisibility(View.GONE);
    }

    private void subscribeToShowPlayButton(final long downloadId) {
        Subscription sub = ShowPlayButton.getEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Episode>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted() called: subscribeToShowPlayButton");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: subscribeToShowPlayButton", e);
                    }

                    @Override
                    public void onNext(Episode episode) {
                        if (playDownloadButton == null) return;

                        if ((Long) playDownloadButton.getTag() == downloadId) {
                            showPlayButton(episode);
                        }
                    }
                });

        subs.add(sub);

    }

    private void subscribeToUiProgressUpdate() {
        Subscription sub = UpdateProgress.getEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DownloadRow>() {
                    private int lastBytesSoFar = 0;
                    private boolean isFirst = true;
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
                        //Log.d(TAG, "onNext() called with: downloadRow updateProgress = [" + downloadRow + "]");
                        if (downloadRow.id != downloadId) {
                            return;
                        }

                        if (isFirst) {
                            downloadTitle.setText(downloadRow.title);
                            isFirst = false;
                        }

                        final int bytesSoFar = downloadRow.bytesSoFar;
                        // don't update progress if the the num bytes hasn't change
                        if (lastBytesSoFar == bytesSoFar) {
                            return;
                        }
                        lastBytesSoFar = bytesSoFar;
                        progressBar.setIndeterminate(false);

                        final int totalSize = downloadRow.totalSize;
                        progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setMax(totalSize);
                                progressBar.setProgress(bytesSoFar);
                            }
                        });

                        if (totalSize == bytesSoFar) {
                            unsubscribe();
                        }
                    }
                });

        subs.add(sub);
    }

    @Override
    public void onDetach() {
        //context.unregisterReceiver(onComplete);
        context.unregisterReceiver(onNotificationClicked);
        subs.unsubscribe();
        subs = null;
        super.onDetach();
    }

    private long startDownload(Episode episode) {
        Log.d(TAG, "startDownload() called with: episode = [" + episode + "]");
        File fileDir = context.getExternalFilesDir(null);

        Uri uri = Uri.parse(episode.url);

        File dir = new File(fileDir, "episodes");
        dir = new File(dir, "show-" + episode.showId);
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

        Subscription sub = Observable.interval(2000, 800, TimeUnit.MILLISECONDS)
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
                        UpdateProgress.publish(downloadRow);
                    }
                });
        subs.add(sub);
    }


    public void showPlayButton(final Episode episode) {
        if (episode.downloadId != downloadId) {
            return;
        }
        playDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EpisodesRecyclerViewAdapter.EpisodeClickBus.publish(episode);
            }
        });

        playDownloadButton.post(new Runnable() {
            @Override
            public void run() {
                playDownloadButton.setVisibility(View.VISIBLE);
            }
        });

    }



    BroadcastReceiver onNotificationClicked = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
        }
    };

    /** Represents the values in a curursor download */
    public static class DownloadRow {

        public final long id;
        public final String localUri;
        public final Integer totalSize;

        final String destination;
        public final String title;
        final String description;
        final String uri;
        final int status;
        final Integer bytesSoFar;
        final int reason;

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

            return id + " - " +  u + " - " + s + " - " + r + " - " +
                    bsf + "/" + ts + " " + t + " - " + localUri;
        }
    }
}

package org.dfhu.vpodplayer.job;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.service.EpisodeDownloader;
import org.dfhu.vpodplayer.service.RefreshAllShowsService;
import org.dfhu.vpodplayer.util.LoggingSubscriber;

import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;

public class UpdateFeedsJob extends Job {

    public static final String TAG = UpdateFeedsJob.class.getName();
    private static final long TARGET_HOUR = 2L;
    private static final long TARGET_MINUTE = 15;
    private static final long WINDOW_LENGTH = 60;
    private static final int WAKE_LOCK_AWAIT_TIME_SECONDS = 60;

    private final EpisodeDownloader episodeDownloader;

    public UpdateFeedsJob(EpisodeDownloader episodeDownloader) {
        this.episodeDownloader = episodeDownloader;
    }

    public static void schedule() {
        schedule(true);
    }

    private static void schedule(boolean updateCurrent) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DailyExecutionWindow executionWindow =
                new DailyExecutionWindow(hour, minute, TARGET_HOUR, TARGET_MINUTE, WINDOW_LENGTH);

        //startMs = 5000L; endMs = 2L * 5000L;
        new JobRequest.Builder(UpdateFeedsJob.TAG)
                .setExecutionWindow(executionWindow.startMs, executionWindow.endMs)
                .setPersisted(true)
                //.setRequiredNetworkType(JobRequest.NetworkType.UNMETERED)
                .setUpdateCurrent(updateCurrent)
                .build()
                .schedule();
    }

    public static class RefreshAllShowsSubscriber extends LoggingSubscriber<RefreshAllShowsService.RefreshResults> {
        private final CountDownLatch latch;
        private final EpisodeDownloader episodeDownloader;

        public RefreshAllShowsSubscriber(@NonNull EpisodeDownloader episodeDownloader) {
            this.latch = new CountDownLatch(1);
            this.episodeDownloader = episodeDownloader;
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        @Override
        public void onNext(RefreshAllShowsService.RefreshResults refreshResults) {
            Log.d(TAG, "onNext() called with: refreshResults = [" + refreshResults + "]");
            for (Episode episode: refreshResults.getNewEpisodes()) {
                episodeDownloader.enqueue(episode);
            }
            latch.countDown();
        }
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {

        try {
            Intent intent = new Intent(getContext(), RefreshAllShowsService.class);
            intent.setData(RefreshAllShowsService.URI_REFRESH_ALL);
            getContext().startService(intent);

            RefreshAllShowsSubscriber refreshAllShowsSubscriber
                = new RefreshAllShowsSubscriber(episodeDownloader);
            RefreshAllShowsService.ServiceCompleteBus.getEvents()
                    .subscribe(refreshAllShowsSubscriber);

            // force staying awake until time out or an event is triggered on the ServiceCompleteBus
            refreshAllShowsSubscriber.getLatch()
                    .await(WAKE_LOCK_AWAIT_TIME_SECONDS, TimeUnit.SECONDS);

            Log.d(TAG, "onRunJob:  Hello Job World" + params);
            return Result.SUCCESS;
        } catch (InterruptedException e) {
            Log.d(TAG, "onRunJob", e);
        } finally {
            schedule(false);
        }

       return Result.FAILURE;
    }
}

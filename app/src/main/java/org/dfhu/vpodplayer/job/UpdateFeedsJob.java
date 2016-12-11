package org.dfhu.vpodplayer.job;

import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class UpdateFeedsJob extends Job {

    public static final String TAG = UpdateFeedsJob.class.getName();

    public static void schedule() {
        schedule(true);
    }

    private static void schedule(boolean updateCurrent) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 1 AM - 6 AM, ignore seconds
        long startMs = TimeUnit.MINUTES.toMillis(60 - minute)
                + TimeUnit.HOURS.toMillis((24 - hour) % 24);
        startMs += TimeUnit.HOURS.toMillis(16);
        long endMs = startMs + TimeUnit.MINUTES.toMillis(20);

        new JobRequest.Builder(UpdateFeedsJob.TAG)
                .setExecutionWindow(startMs, endMs)
                .setPersisted(true)
                .setRequiredNetworkType(JobRequest.NetworkType.UNMETERED)
                .setUpdateCurrent(updateCurrent)
                .build()
                .schedule();
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        try {
            Log.d(TAG, "onRunJob:  Hello Job World" + params);
            return Result.SUCCESS;
        } finally {
            schedule(false);
        }
    }
}

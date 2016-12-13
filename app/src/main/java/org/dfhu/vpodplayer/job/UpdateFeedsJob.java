package org.dfhu.vpodplayer.job;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;


import org.dfhu.vpodplayer.service.RefreshAllShowsService;

import java.util.Calendar;

public class UpdateFeedsJob extends Job {

    public static final String TAG = UpdateFeedsJob.class.getName();
    private static final long TARGET_HOUR = 17;
    private static final long TARGET_MINUTE = 4;
    private static final long WINDOW_LENGTH = 3;

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

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        try {
            Intent intent = new Intent(getContext(), RefreshAllShowsService.class);
            intent.setData(RefreshAllShowsService.URI_REFRESH_ALL);
            getContext().startService(intent);

            Log.d(TAG, "onRunJob:  Hello Job World" + params);
            return Result.SUCCESS;
        } finally {
            schedule(false);
        }
    }
}

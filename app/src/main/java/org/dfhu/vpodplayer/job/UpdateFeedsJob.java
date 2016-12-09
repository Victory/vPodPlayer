package org.dfhu.vpodplayer.job;

import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;

public class UpdateFeedsJob extends Job {

    public static final String TAG = UpdateFeedsJob.class.getName();

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Log.d(TAG, "onRunJob:  Hello Job World" + params);
        return Result.SUCCESS;
    }
}

package org.dfhu.vpodplayer.job;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class UpdateFeedsJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        if (tag.equals(UpdateFeedsJob.TAG)) {
            return new UpdateFeedsJob();
        }
        return null;
    }
}

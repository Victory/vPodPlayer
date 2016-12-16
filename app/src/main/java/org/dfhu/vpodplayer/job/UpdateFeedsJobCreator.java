package org.dfhu.vpodplayer.job;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import org.dfhu.vpodplayer.service.EpisodeDownloader;

import javax.inject.Inject;

import dagger.Lazy;

public class UpdateFeedsJobCreator implements JobCreator {

    @Inject
    Lazy<EpisodeDownloader> episodeDownloader;

    @Override
    public Job create(String tag) {
        if (tag.equals(UpdateFeedsJob.TAG)) {
            return new UpdateFeedsJob(episodeDownloader.get());
        }
        return null;
    }
}

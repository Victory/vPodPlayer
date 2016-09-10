package org.dfhu.vpodplayer.feed;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class FeedParserResult {
    enum Status {
        SUCCESS,
        COULD_NOT_PARSE_XML,
        BAD_XML_FORMAT,
        COULD_NOT_READ_XML,
    }

    @Nullable
    public final Exception e;
    @NonNull
    public final FeedInfo info;
    @NonNull
    public final Status status;

    /**
     * save the status and info, exception is null
     */
    public FeedParserResult(Status status, FeedInfo info) {
        this.e = null;
        this.status = status;
        this.info = info;
    }

    public FeedParserResult(Status status, FeedInfo info, Exception e) {
        this.e = e;
        this.status = status;
        this.info = info;
    }

}

package org.dfhu.vpodplayer.feed;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FeedFetchResult {

    public enum Status {
        COULD_NOT_OPEN_URL,
        COULD_NOT_GET_INPUTSTREAM,
        SUCCESS
    }

    @NonNull
    public final InputStream inputStream;
    @NonNull
    public final Status status;
    @Nullable
    public final Exception exception;

    /**
     * Set the status, leave data empty and store the exception, the inputstream will be empty
     */
    public FeedFetchResult(@NonNull Status status, @NonNull IOException e) {
        this.status = status;
        this.exception = e;
        this.inputStream = new ByteArrayInputStream(new byte[]{});
        // XXX: Should use Dependency injection
        Log.w("Could not fetch feed", e);
    }

    /**
     * Set the data. Status is SUCCESS and exception is null, store the inputStream
     */
    public FeedFetchResult(@NonNull InputStream inputStream) {
        this.status = Status.SUCCESS;
        this.inputStream = inputStream;
        this.exception = null;
    }
}

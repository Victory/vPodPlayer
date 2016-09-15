package org.dfhu.vpodplayer.feed;


import android.support.annotation.NonNull;


import org.dfhu.vpodplayer.util.VicURL;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.functions.Func0;
import rx.util.async.Async;


public class FetchFeed {

    private FetchFeed() {}


    /**
     * Gets the feed data. Assumes that the rss feed is encoded with UTF-8
     *
     * @param feedUrl - URL object for the feed
     * @return - the feed and status
     */
    @NonNull
    public static Observable<FeedFetchResult> fetch(final VicURL feedUrl) {
        return Async.start(new Func0<FeedFetchResult>() {
            @Override
            public FeedFetchResult call() {
                return fetchSync(feedUrl);
            }
        });
    }

    @NonNull
    private static FeedFetchResult fetchSync(VicURL feedUrl) {

        URLConnection connection;
        try {
            connection = feedUrl.openConnection();
        } catch (IOException e) {
            return new FeedFetchResult(FeedFetchResult.Status.COULD_NOT_OPEN_URL, e);
        }

        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            return new FeedFetchResult(FeedFetchResult.Status.COULD_NOT_GET_INPUTSTREAM, e);
        }


        return new FeedFetchResult(inputStream);
    }
}

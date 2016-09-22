package org.dfhu.vpodplayer.feed;


import android.support.annotation.NonNull;


import org.dfhu.vpodplayer.util.VicURL;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
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
    public static Observable<InputStream> getInputStream(final VicURL feedUrl) {

        return Async.start(new Func0<Observable<InputStream>>() {
            @Override
            public Observable<InputStream> call() {
                try {
                    return Observable.just(getInputStreamSync(feedUrl));
                } catch (IOException e) {
                    return Observable.error(new Throwable("just throw"));
                }
            }
        }).flatMap(new Func1<Observable<InputStream>, Observable<InputStream>>() {
            @Override
            public Observable<InputStream> call(Observable<InputStream> inputStreamObservable) {
                return inputStreamObservable;
            }
        });
    }

    @NonNull
    public static InputStream getInputStreamSync(VicURL feedUrl) throws IOException {
        URLConnection connection = feedUrl.openConnection();
        return new BufferedInputStream(connection.getInputStream());
    }
}

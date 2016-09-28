package org.dfhu.vpodplayer.tasks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import org.dfhu.vpodplayer.feed.Feed;
import org.dfhu.vpodplayer.feed.FetchFeed;
import org.dfhu.vpodplayer.feed.JsoupFeed;
import org.dfhu.vpodplayer.util.VicURL;
import org.dfhu.vpodplayer.util.VicURLProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.util.async.Async;

/**
 * Fetches a podcast feed
 */
public class FetchFeedFragment extends Fragment {

    public FetchFeedFragment() {
        super();
        Log.d("test-title", "creating fetch feedback");
    }

    public interface FetchFeedCallbacks {
        void addFetchFeedSubscription(Observable<Feed> observable);
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        // save instance across config changes
        setRetainInstance(true);
        Log.d("test-frag", "onCreate");
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("test-title", "onCreateView FetchFeedFragment: " + this.getId());
        return null;
    }

    public static class InputStreamWithUrl {
        public final String url;
        public final InputStream inputStream;
        public InputStreamWithUrl(String url, InputStream inputStream) {
            this.url = url;
            this.inputStream = inputStream;
        }
    }

    public Observable<Feed> buildObserver(final String url) {

        Observable<VicURL> getVicUrl = Async.start(new Func0<VicURL>() {
            @Override
            public VicURL call() {
                try {
                    return VicURLProvider.newInstance(url);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Invalid url");
                }
            }
        }).subscribeOn(Schedulers.io());

        return getVicUrl.flatMap(new Func1<VicURL, Observable<InputStreamWithUrl>>() {
                    @Override
                    public Observable<InputStreamWithUrl> call(VicURL vicURL) {
                        Log.d("test-title", "fetching inputstream: " + vicURL.getUrlString());
                        try {
                            InputStreamWithUrl inputStreamWithUrl =
                                    new InputStreamWithUrl(
                                            vicURL.getUrlString(),
                                            FetchFeed.getInputStreamSync(vicURL));
                            return Observable.just(inputStreamWithUrl);
                        } catch (IOException e) {
                            return Observable.error(e);
                        }
                    }
                })
                .flatMap(new Func1<InputStreamWithUrl, Observable<Feed>>() {
                    @Override
                    public Observable<Feed> call(InputStreamWithUrl inputStreamWithUrl) {
                        try {
                            Document doc = Jsoup.parse(inputStreamWithUrl.inputStream, "UTF-8", "");
                            JsoupFeed feed = new JsoupFeed(inputStreamWithUrl.url, doc);
                            return Observable.just((Feed) feed);
                        } catch (IOException e) {
                            return Observable.error(e);
                        }

                    }
                });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

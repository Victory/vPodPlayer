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
import org.dfhu.vpodplayer.feed.JsFeed;
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

    /** Holds which implements FetchFeedCallBacks (Generally an Activity) */
    private FetchFeedCallbacks mCallbacks;

    private String mOnDeckUrl;

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

        /*
        if (mOnDeckUrl != null) {
            fetch(mOnDeckUrl);
            mOnDeckUrl = null;
        }
        */

        return null;
    }

    /*
    public void newSubscription(String url) {
        if (mCallbacks != null) {
            fetch(url);
        } else {
            mOnDeckUrl = url;
        }
    }

    public void fetch(String url) {
        Observable<Feed> o = buildObserver(url);
        mCallbacks = (FetchFeedCallbacks) getActivity();
        if (mCallbacks == null) {
            Log.e("life-cycle-race", "Activity is null in FetchFeedFragement");
        }
        mCallbacks.addFetchFeedSubscription(o);
    }
    */

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

        return getVicUrl.flatMap(new Func1<VicURL, Observable<InputStream>>() {
                    @Override
                    public Observable<InputStream> call(VicURL vicURL) {
                        Log.d("test-title", "fetching inputstream: " + vicURL.getUrlString());
                        try {
                            return Observable.just(FetchFeed.getInputStreamSync(vicURL));
                        } catch (IOException e) {
                            return Observable.error(e);
                        }
                    }
                })
                .flatMap(new Func1<InputStream, Observable<Feed>>() {
                    @Override
                    public Observable<Feed> call(InputStream inputStream) {
                        try {
                            Document doc = Jsoup.parse(inputStream, "UTF-8", "");
                            JsFeed feed = new JsFeed(doc);
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
        mCallbacks = null;
    }
}

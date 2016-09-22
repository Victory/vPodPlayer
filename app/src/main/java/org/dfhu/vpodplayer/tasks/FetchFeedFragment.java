package org.dfhu.vpodplayer.tasks;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import org.dfhu.vpodplayer.feed.FeedFetchResult;
import org.dfhu.vpodplayer.feed.FetchFeed;
import org.dfhu.vpodplayer.util.VicURL;
import org.dfhu.vpodplayer.util.VicURLProvider;

import java.net.MalformedURLException;

import rx.Observable;
import rx.functions.Func0;
import rx.util.async.Async;

/**
 * Fetches a podcast feed
 */
public class FetchFeedFragment extends Fragment {

    FetchFeedCallbacks mCallbacks;
    private Observable<VicURL> o;

    public FetchFeedFragment() {
        super();
        Log.d("test-title", "creating fetch feedback");
    }

    public interface FetchFeedCallbacks {
        // The podcast we will fetch
        String BUNDLE_KEY_PODCAST_URL = "PODCAST_URL";
        // key should be true if we want to create and subscribe to a new observable
        String BUNDLE_KEY_ADD_SUBSCRIPTION = "ADD_NEW_SUBSCRIPTION";
        void addFetchFeedSubscription(Observable<VicURL> observable);
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
        Log.d("test-title", "Creating view");
        if (!getArguments().getBoolean(FetchFeedCallbacks.BUNDLE_KEY_ADD_SUBSCRIPTION)) {
            Log.d("test-title", "not activePush");
            return null;
        }

        mCallbacks = (FetchFeedCallbacks) getActivity();

        final String url = getArguments().getString(FetchFeedCallbacks.BUNDLE_KEY_PODCAST_URL);

        o = Async.start(new Func0<VicURL>() {
            @Override
            public VicURL call() {
                try {
                    return VicURLProvider.newInstance(url);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Invalid url");
                }
            }
        });
        mCallbacks.addFetchFeedSubscription(o);

        return null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }
}

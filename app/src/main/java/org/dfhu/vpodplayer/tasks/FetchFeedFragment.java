package org.dfhu.vpodplayer.tasks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.util.async.Async;

/**
 * Fetches a podcast feed
 */
public class FetchFeedFragment extends Fragment {

    FetchFeedCallbacks mCallbacks;
    private boolean hasBeenCalled = false;
    private Observable<String> o;

    public FetchFeedFragment() {
        super();
        Log.d("test-title", "creating fetch feedback");
    }

    public interface FetchFeedCallbacks {
        String BUNDLE_KEY_PODCAST_URL = "PODCAST_URL";
        void addFetchFeedSubscription(Observable<String> observable);
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
        if (!getArguments().getBoolean("activePush")) {
            Log.d("test-title", "not activePush");
            return null;
        }

        /*
        if (hasBeenCalled) {
            Log.d("test-title", "Ignoring as already called");
            return null;
        }
        hasBeenCalled = true;
        */

        mCallbacks = (FetchFeedCallbacks) getActivity();

        o = Async.start(new Func0<String>() {
            @Override
            public String call() {

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return "called me " + Math.floor(Math.random() * 500);
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                Log.d("test-title", "setting hasBeenCalled to false");
                hasBeenCalled = false;
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

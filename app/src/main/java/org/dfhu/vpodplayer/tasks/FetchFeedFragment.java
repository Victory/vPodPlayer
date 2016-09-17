package org.dfhu.vpodplayer.tasks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dfhu.vpodplayer.R;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func0;
import rx.util.async.Async;

/**
 * Fetches a podcast feed
 */
public class FetchFeedFragment extends Fragment {

    private TextView mTestTitle;
    FetchFeedCallbacks mCallbacks;

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
        Log.d("test-frag", "Creating view");

        mTestTitle = (TextView) getActivity().findViewById(R.id.testTitle);
        mCallbacks = (FetchFeedCallbacks) getActivity();
        final String title = getArguments().getString(FetchFeedCallbacks.BUNDLE_KEY_PODCAST_URL);


        Observable<String> o = Async.start(new Func0<String>() {
            @Override
            public String call() {

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return "called me " + Math.floor(Math.random() * 500);
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

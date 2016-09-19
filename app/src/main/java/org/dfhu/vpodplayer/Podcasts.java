package org.dfhu.vpodplayer;

import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.dfhu.vpodplayer.tasks.FetchFeedFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.CompositeSubscription;

public class Podcasts extends AppCompatActivity
        implements FetchFeedFragment.FetchFeedCallbacks {

    //private static final String sTestFeed = "http://www.npr.org/rss/podcast.php?id=510289";

    @BindView(R.id.testTitle)
    TextView testTitle;

    @BindView(R.id.testFetchFeed)
    Button mTestFetchFeed;

    private FetchFeedFragment mFetchFeedFragment;
    private static final String TAG_FETCH_FEED_FRAGMENT = "fetch-feed-fragment";
    private static final CompositeSubscription subs = new CompositeSubscription();

    private final Bundle configChangeBundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcasts);
        ButterKnife.bind(this);
        Log.d("test-title", "on create: " + testTitle.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFetchFeedFragment != null) {
            mFetchFeedFragment.getArguments().remove(BUNDLE_KEY_ADD_SUBSCRIPTION);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        subs.clear();
    }

    @Override
    public void addFetchFeedSubscription(Observable<String> observable) {
        subs.clear();

        Subscription evt =
                observable
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.d("test-title", "doOnSubscribe");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Observer<String>() {
                            @Override
                            public void onCompleted() {
                                Log.d("test-title", "observer complete");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d("test-title", "observer error", e);
                            }

                            @Override
                            public void onNext(String s) {
                                Log.d("test-title", "setTitle: " + s);
                                setTitle(s);
                                mTestFetchFeed.setEnabled(true);
                            }
                        }
                );

        subs.add(evt);
    }

    @OnClick(R.id.testFetchFeed)
    public void fetchFeed() {
       mTestFetchFeed.setEnabled(false);
        FragmentManager fm = getFragmentManager();

        mFetchFeedFragment = new FetchFeedFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_KEY_PODCAST_URL, "testing: " + Math.random());
        args.putBoolean(BUNDLE_KEY_ADD_SUBSCRIPTION, true);
        mFetchFeedFragment.setArguments(args);
        fm.beginTransaction().add(mFetchFeedFragment, TAG_FETCH_FEED_FRAGMENT).commit();
    }

    private void setTitle(String title) {

        configChangeBundle.putString("title", title);
        testTitle.setText(title);
    }
}

package org.dfhu.vpodplayer;

import android.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.dfhu.vpodplayer.feed.Feed;
import org.dfhu.vpodplayer.tasks.FetchFeedFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import rx.subscriptions.CompositeSubscription;

public class Podcasts extends AppCompatActivity
        implements FetchFeedFragment.FetchFeedCallbacks, FeedFetcher {

    //private static final String sTestFeed = "http://www.npr.org/rss/podcast.php?id=510289";

    private static class FetchBus {
       private final Subject<String, String> bus = new SerializedSubject<String, String>(PublishSubject.<String>create());

        public void send(String str) {
           bus.onNext(str);
        }

        public Observable<String> getObservable(final String key) {
            return bus.doOnSubscribe(new Action0() {
                @Override
                public void call() {
                    Log.d("test-title", "getting bus observer: " + key);
                }
            }).doOnUnsubscribe(new Action0() {
                @Override
                public void call() {
                    Log.d("test-title", "unsubscribing bus observer: " + key);
                }
            }).doOnCompleted(new Action0() {
                @Override
                public void call() {
                    Log.d("test-title", "onComplete bus observer: " + key);
                }
            });
        }
    }

    private FetchBus myBus = new FetchBus();

    @BindView(R.id.tool_bar)
    Toolbar toolbar;

    @BindView(R.id.testTitle)
    TextView testTitle;

    private static final String TAG_FETCH_FEED_FRAGMENT = "fetch-feed-fragment";
    private static final CompositeSubscription subs = new CompositeSubscription();

    private Subscription sub;

    private final Bundle configChangeBundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcasts);
        ButterKnife.bind(this);

        final String nameThis = testTitle.toString();
        Log.d("test-title", "on create activity: " + nameThis);
        setSupportActionBar(toolbar);

        sub = myBus.getObservable(nameThis).subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.d("test-title", "busy onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("test-title", "busy onError", e);
            }

            @Override
            public void onNext(String s) {
                Log.d("test-title", "on busy setting title: " + nameThis);
                setTitle(s);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.main_menu, menu);

        bindSubscribeMenuItem(menu);

        return true;
    }

    /** set the binding for subscribe ActionView */
    private void bindSubscribeMenuItem(Menu menu) {
        MenuItem subscribeItem = (MenuItem) menu.findItem(R.id.menu_subscribe);
        final SubscribeActionView subscribeView = (SubscribeActionView) MenuItemCompat.getActionView(subscribeItem);
        subscribeView.setFeedFetcher(this);

        MenuItemCompat.setOnActionExpandListener(subscribeItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                TextView textView = (TextView) findViewById(R.id.subscribe_url);
                textView.setText("");
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh_podcast:
                Toast.makeText(this, "not implemented", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void addFetchFeedSubscription(Observable<Feed> observable) {
        subs.clear();
        Subscription evt;
        evt = observable
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.d("test-title", "subcribing addFetchFeedSubscription");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DoFeed());
        subs.add(evt);
    }

    private void toasty(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    public class DoFeed implements Observer<Feed> {
        @Override
        public void onCompleted() {
            Log.d("test-title", "onComplete");
        }

        @Override
        public void onError(Throwable e) {
            Log.e("test-title", "error: " + e.getMessage());
            toasty(e.getMessage());
        }

        @Override
        public void onNext(Feed r) {
            Log.d("test-title", "onNext: "+ r.getTitle());
            setTitle(r.getTitle());
            myBus.send("bussy" + r.getTitle());
        }
    }

    @Override
    public void triggerFetchFeed(String feedUrl) {
        // prefix with http
        if (!feedUrl.startsWith("http://")) {
            feedUrl = "http://192.168.1.6:3000/" + feedUrl;
        }

        FragmentManager fm = getFragmentManager();
        FetchFeedFragment mFetchFeedFragment = (FetchFeedFragment) fm.findFragmentByTag(TAG_FETCH_FEED_FRAGMENT);
        if (mFetchFeedFragment == null) {
            mFetchFeedFragment = new FetchFeedFragment();
            fm.beginTransaction().add(mFetchFeedFragment, TAG_FETCH_FEED_FRAGMENT).commit();
        }
        addFetchFeedSubscription(mFetchFeedFragment.buildObserver(feedUrl));
    }

    private void setTitle(String title) {
        configChangeBundle.putString("title", title);
        testTitle.setText(title);
    }
}

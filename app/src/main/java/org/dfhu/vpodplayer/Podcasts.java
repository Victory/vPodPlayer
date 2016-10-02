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
import org.dfhu.vpodplayer.fragment.ShowListFragment;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.fragment.FetchFeedFragment;


import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subjects.PublishSubject;

public class Podcasts extends AppCompatActivity
        implements FetchFeedFragment.FetchFeedCallbacks, FeedFetcher {

    private static class FetchBus {
        private FetchBus() {}
        private static PublishSubject<Feed> subject = PublishSubject.create();

        static void publish(Feed v) { subject.onNext(v); }
        static Observable<Feed> getEvents() { return subject; }
    }

    private static class ToastErrorBus {
        private ToastErrorBus() {}
        private static PublishSubject<String> subject = PublishSubject.create();

        static void publish(String v) { subject.onNext(v); }
        static Observable<String> getEvents() { return subject; }
    }

    @BindView(R.id.tool_bar)
    Toolbar toolbar;


    private static final String TAG_FETCH_FEED_FRAGMENT = "fetch-feed-fragment";
    private Subscription fetchSubscription;
    private Subscription toastErrorSubscription;
    private final Bundle configChangeBundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcasts);
        ButterKnife.bind(this);


        final String nameThis = this.toString();
        Log.d("test-title", "on create activity: " + nameThis);
        setSupportActionBar(toolbar);

        subscribeToFetch(nameThis);
        subscribeToToastError();


        if (getSupportFragmentManager().findFragmentByTag("SHOWSTAG") == null) {
            ShowListFragment fragment = new ShowListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment, "SHOWSTAG")
                    .commit();
        }


    }

    private void subscribeToToastError() {
        toastErrorSubscription = ToastErrorBus.getEvents()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        makeToast(s);
                    }
                });
    }


    /**
     * Subscribe the the results of fetching a feed
     * @param nameThis - debugging
     */
    private void subscribeToFetch(final String nameThis) {
        fetchSubscription = FetchBus.getEvents()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Feed>() {
                    @Override
                    public void onCompleted() {
                        Log.d("test-title", "busy onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("test-title", "busy onError", e);
                    }

                    @Override
                    public void onNext(Feed feed) {
                        Log.d("test-title", "on busy setting title: " + nameThis);
                        handleFeed(feed);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fetchSubscription.unsubscribe();
        toastErrorSubscription.unsubscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        bindSubscribeMenuItem(menu);

        return true;
    }

    /** set the binding for subscribe ActionView */
    private void bindSubscribeMenuItem(Menu menu) {
        MenuItem subscribeItem = menu.findItem(R.id.menu_subscribe);
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
        observable
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.d("test-title", "subcribing addFetchFeedSubscription");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DoFeed());
    }

    public static class DoFeed implements Observer<Feed> {
        @Override
        public void onCompleted() {
            Log.d("test-title", "onComplete");
        }

        @Override
        public void onError(Throwable e) {
            Log.e("test-title", "error: " + e.getMessage());
            safeToast("error: " + e.getMessage());
        }

        @Override
        public void onNext(Feed feed) {
            Log.d("test-title", "onNext: "+ feed.getTitle());
            FetchBus.publish(feed);
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

    void handleFeed(Feed feed) {
        configChangeBundle.putString("title", feed.getTitle());
        Show show = new Show();
        show.title = feed.getTitle();
        show.url = feed.getUrl();

        Shows db = new Shows(this);
        if (db.add(show) < 0) {
            safeToast("Could not add show. Already subscribed?");
        }
    }

    public static void safeToast(String s) {
       ToastErrorBus.publish(s);
    }

    public void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}

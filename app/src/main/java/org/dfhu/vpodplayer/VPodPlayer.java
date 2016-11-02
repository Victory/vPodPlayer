package org.dfhu.vpodplayer;

import android.app.FragmentManager;
import android.content.Context;
import android.media.AudioManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
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
import org.dfhu.vpodplayer.feed.SubscribeToFeed;
import org.dfhu.vpodplayer.fragment.DownloadFragment;
import org.dfhu.vpodplayer.fragment.EpisodeListFragment;
import org.dfhu.vpodplayer.fragment.PlayerFragment;
import org.dfhu.vpodplayer.fragment.ShowListFragment;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.fragment.FetchFeedFragment;


import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subjects.PublishSubject;

public class VPodPlayer extends AppCompatActivity
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

    Toolbar toolbar;

    private static final String TAG_FETCH_FEED_FRAGMENT = "fetch-feed-fragment";
    public static final String TAG_MAIN_DISPLAY_FRAGMENT = "TAG_MAIN_DISPLAY_FRAGMENT";
    private List<Subscription> subscriptions = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcasts);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        final String nameThis = this.toString();
        Log.d("test-title", "on create activity: " + nameThis);
        setSupportActionBar(toolbar);
        showHomeButton(true);

        subscribeToFetch(nameThis);
        subscribeToToastError();
        subscribeToShowClicked();
        subscribeToEpisodeClicked();

        if (getSupportFragmentManager().findFragmentByTag(TAG_MAIN_DISPLAY_FRAGMENT) == null) {
            setHomeFragment();
        }
    }

    /**
     * Show or hide the action bar's up home button
     * @param show - true to show, false to hide
     */
    public void showHomeButton(boolean show) {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar == null) {
            return;
        }
        supportActionBar.setHomeButtonEnabled(show);
        supportActionBar.setDisplayHomeAsUpEnabled(show);
    }

    private void subscribeToEpisodeClicked() {
        Subscription sub = EpisodesRecyclerViewAdapter.EpisodeClickBus.getEvents()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Episode>() {
                    @Override
                    public void onCompleted() {
                        Log.d("episodeClickSub", "onComplete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("episodeClickSub", "onError", e);
                    }

                    @Override
                    public void onNext(Episode episode) {
                        Log.d("episodeClickSub", "onNext: " + episode);

                        Fragment fragment = (episode.localUri != null) ?
                                new PlayerFragment() :
                                new DownloadFragment();

                        Bundle args = new Bundle();
                        args.putInt("episodeId", episode.id);
                        fragment.setArguments(args);

                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragmentContainer, fragment, TAG_MAIN_DISPLAY_FRAGMENT)
                                .commit();

                        showHomeButton(true);
                    }
                });
        subscriptions.add(sub);
    }

    private void subscribeToShowClicked() {
        Subscription sub = ShowsRecyclerViewAdapter.ShowClickBus.getEvents()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Show>() {
                    @Override
                    public void onCompleted() {
                        Log.d("showClickSubscription", "onComplete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("showClickSubscription", "onError", e);
                    }

                    @Override
                    public void onNext(Show show) {
                        setEpisodeFragment(show);
                    }
                });
        subscriptions.add(sub);
    }

    public void setEpisodeFragment(Show show) {
        EpisodeListFragment fragment = new EpisodeListFragment();
        Bundle args = new Bundle();
        args.putInt("showId", show.id);
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment, TAG_MAIN_DISPLAY_FRAGMENT)
                .commit();

        showHomeButton(true);
    }

    private void subscribeToToastError() {
        Subscription sub = ToastErrorBus.getEvents()
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
        subscriptions.add(sub);
    }


    /**
     * Subscribe the the results of fetching a feed
     * @param nameThis - debugging
     */
    private void subscribeToFetch(final String nameThis) {
        Subscription sub = FetchBus.getEvents()
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

        subscriptions.add(sub);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (Subscription sub: subscriptions) {
            if (sub != null) {
                sub.unsubscribe();
            }
        }
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
                refresh();
                return true;
            case android.R.id.home:
                setHomeFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refresh() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment instanceof EpisodeListFragment) {
            int showId = fragment.getArguments().getInt("showId");
            getNewEpisodesForShow(showId);
        }
    }

    private void getNewEpisodesForShow(int showId) {
        Shows db = new Shows(getApplicationContext());
        Show show = db.getById(showId);
        triggerFetchFeed(show.url);
    }

    private void setHomeFragment() {
        ShowListFragment fragment = new ShowListFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment, TAG_MAIN_DISPLAY_FRAGMENT)
                .commit();

        showHomeButton(false);
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
        FetchFeedFragment fetchFeedFragment = (FetchFeedFragment) fm.findFragmentByTag(TAG_FETCH_FEED_FRAGMENT);
        if (fetchFeedFragment == null) {
            fetchFeedFragment = new FetchFeedFragment();
            fm.beginTransaction().add(fetchFeedFragment, TAG_FETCH_FEED_FRAGMENT).commit();
        }
        addFetchFeedSubscription(fetchFeedFragment.buildObserver(feedUrl));
    }

    void handleFeed(Feed feed) {
        Shows showsDb = new Shows(this.getApplicationContext());
        Episodes episodeDb = new Episodes(this.getApplicationContext());
        Show show = SubscribeToFeed.subscribe(feed, showsDb, episodeDb);

        setEpisodeFragment(show);
        Toast.makeText(this, "Updated: " + show.title, Toast.LENGTH_SHORT).show();
    }

    public static void safeToast(String s) {
       ToastErrorBus.publish(s);
    }

    public void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}

package org.dfhu.vpodplayer;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import org.dfhu.vpodplayer.feed.SubscriptionManager;
import org.dfhu.vpodplayer.fragment.DownloadFragment;
import org.dfhu.vpodplayer.fragment.EpisodeListFragment;
import org.dfhu.vpodplayer.fragment.PlayerFragment;
import org.dfhu.vpodplayer.fragment.ShowListFragment;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.service.SubscribeToShowService;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.fragment.FetchFeedFragment;

import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subjects.PublishSubject;

public class VPodPlayer extends AppCompatActivity
        implements FetchFeedFragment.FetchFeedCallbacks, FeedFetcher {

    public static final String TAG = VPodPlayer.class.getName();

    private AlertDialog subscribeConfirmationAlertDialog;

    private static class FetchFeedBus {
        private FetchFeedBus() {}
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

    public static class RefreshFragmentBus {
        private RefreshFragmentBus() {}
        private static PublishSubject<Class> subject = PublishSubject.create();

        public static void publish(Class v) { subject.onNext(v); }
        static Observable<Class> getEvents() { return subject; }
    }

    Toolbar toolbar;

    private static final String TAG_FETCH_FEED_FRAGMENT = "TAG_FETCH_FEED_FRAGMENT";
    public static final String TAG_MAIN_DISPLAY_FRAGMENT = "TAG_MAIN_DISPLAY_FRAGMENT";
    private List<Subscription> subscriptions = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcasts);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        showHomeButton(true);

        subscribeToFetch();
        subscribeToToastError();
        subscribeToShowClicked();
        subscribeToEpisodeClicked();
        subscribeToRefreshFragment();
        handleIntentFromBrowserLink();


        if (getSupportFragmentManager().findFragmentByTag(TAG_MAIN_DISPLAY_FRAGMENT) == null) {
            setShowListFragment();
        }
    }

    /**
     * If we get an intent from a browser link, this should be a url
     * this creates an alert dialogue to confirm the subscription
     */
    private void handleIntentFromBrowserLink() {
        final String showUrl = getIntent().getDataString();

        if (showUrl == null || showUrl.isEmpty() || !showUrl.startsWith("http")) {
            return;
        }
        final Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        subscribeConfirmationAlertDialog = builder
                .setTitle("Subscribe to Podcast")
                .setMessage("Would you like to subscribe to: " + showUrl + "?")
                .setPositiveButton("Subscribe", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getIntent().setData(Uri.EMPTY);
                        Intent intent = new Intent(context, SubscribeToShowService.class);
                        intent.setData(SubscribeToShowService.URI_SUBSCRIBE);
                        intent.putExtra("showUrl", showUrl);
                        context.startService(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getIntent().setData(Uri.EMPTY);
                    }
                })
                .show();

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

    private void subscribeToRefreshFragment() {
        Subscription sub = RefreshFragmentBus.getEvents()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Class>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: trying to refresh", e);
                    }

                    @Override
                    public void onNext(Class cls) {
                        Fragment fragment =
                                getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                        if (cls == ShowListFragment.class && fragment instanceof ShowListFragment) {
                            setShowListFragment();
                        } else {
                            safeToast("Not implemented");
                        }
                    }
                });
        subscriptions.add(sub);
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

                        Fragment fragment = (episode.isReadyToPlay()) ?
                                new PlayerFragment() :
                                new DownloadFragment();

                        Bundle args = new Bundle();
                        args.putInt("episodeId", episode.id);
                        args.putInt("showId", episode.showId);
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
                        setEpisodeListFragment(show.id, 0);
                    }
                });
        subscriptions.add(sub);
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
     */
    private void subscribeToFetch() {
        Subscription sub = FetchFeedBus.getEvents()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Feed>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "busy onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "busy onError", e);
                    }

                    @Override
                    public void onNext(Feed feed) {
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
        if (subscribeConfirmationAlertDialog != null) {
            subscribeConfirmationAlertDialog.dismiss();
            subscribeConfirmationAlertDialog = null;
        }
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
                handleHomeButton();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Perform home menu item button action
     */
    private void handleHomeButton() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (fragment instanceof PlayerFragment) {
            int showId = fragment.getArguments().getInt("showId");
            int episodeId = fragment.getArguments().getInt("episodeId");
            setEpisodeListFragment(showId, episodeId);
        } else if (fragment instanceof DownloadFragment) {
            int showId = fragment.getArguments().getInt("showId");
            int episodeId = fragment.getArguments().getInt("episodeId");
            setEpisodeListFragment(showId, episodeId);
        } else {
            setShowListFragment();
        }
    }

    private void refresh() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment instanceof EpisodeListFragment) {
            int showId = fragment.getArguments().getInt("showId");
            getNewEpisodesForShow(showId);
        } else if (fragment instanceof PlayerFragment) {
            ((PlayerFragment) fragment).restartEpisode();
        } else if (fragment instanceof ShowListFragment) {
            ((ShowListFragment) fragment).refreshAllEpisodes();
        } else {
            safeToast("Not implemented");
        }
    }

    /**
     * Display the fragment with the list of episodes for a show
     * @param showId - id of the show to display
     * @param episodeId - id of the episode to scroll to
     */
    public void setEpisodeListFragment(int showId, int episodeId) {
        EpisodeListFragment fragment = new EpisodeListFragment();
        Bundle args = new Bundle();
        args.putInt("showId", showId);
        args.putInt("episodeId", episodeId);
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment, TAG_MAIN_DISPLAY_FRAGMENT)
                .commit();

        showHomeButton(true);
    }

    /**
     * Display the list of shows (the "home" fragment)
     */
    void setShowListFragment() {
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
            FetchFeedBus.publish(feed);
        }
    }

    @Override
    public void triggerFetchFeed(String feedUrl) {
        FragmentManager fm = getFragmentManager();
        FetchFeedFragment fetchFeedFragment = (FetchFeedFragment) fm.findFragmentByTag(TAG_FETCH_FEED_FRAGMENT);
        if (fetchFeedFragment == null) {
            fetchFeedFragment = new FetchFeedFragment();
            fm.beginTransaction().add(fetchFeedFragment, TAG_FETCH_FEED_FRAGMENT).commit();
        }
        addFetchFeedSubscription(fetchFeedFragment.buildObserver(feedUrl));
    }

    private void getNewEpisodesForShow(int showId) {
        Shows db = new Shows(getApplicationContext());
        Show show = db.getById(showId);
        triggerFetchFeed(show.url);
    }

    void handleFeed(Feed feed) {
        Shows showsDb = new Shows(this.getApplicationContext());
        Episodes episodeDb = new Episodes(this.getApplicationContext());
        Show show = SubscriptionManager.subscribe(feed, showsDb, episodeDb);

        setEpisodeListFragment(show.id, 0);
        Toast.makeText(this, "Updated: " + show.title, Toast.LENGTH_SHORT).show();
    }

    public static void safeToast(String s) {
       ToastErrorBus.publish(s);
    }

    public void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}

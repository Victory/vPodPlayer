package org.dfhu.vpodplayer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.dfhu.vpodplayer.fragment.DownloadFragment;
import org.dfhu.vpodplayer.fragment.EpisodeListFragment;
import org.dfhu.vpodplayer.fragment.PlayerFragment;
import org.dfhu.vpodplayer.fragment.ShowListFragment;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.service.ExportService;
import org.dfhu.vpodplayer.service.UpdateSubscriptionService;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.LoggingSubscriber;

import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class VPodPlayer extends AppCompatActivity {

    public static final String TAG = VPodPlayer.class.getName();

    private AlertDialog subscribeConfirmationAlertDialog;

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
        public static Observable<Class> getEvents() { return subject; }
    }

    public static class CloseSubscribeActionViewBus {
        private CloseSubscribeActionViewBus() {}
        private static PublishSubject<Void> subject = PublishSubject.create();

        public static void publish() { subject.onNext(null); }
        public static Observable<Void> getEvents() { return subject; }
    }

    Toolbar toolbar;

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

        subscribeToToastError();
        subscribeToShowClicked();
        subscribeToEpisodeClicked();
        subscribeToRefreshFragment();
        handleIntentFromBrowserLink(getIntent());


        if (getSupportFragmentManager().findFragmentByTag(TAG_MAIN_DISPLAY_FRAGMENT) == null) {
            setShowListFragment();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntentFromBrowserLink(intent);
    }

    /**
     * If we get an intent from a browser link, this should be a url
     * this creates an alert dialogue to confirm the subscription
     * @param intent
     */
    private void handleIntentFromBrowserLink(final Intent intent) {
        final String showUrl = intent.getDataString();

        if (showUrl == null || showUrl.isEmpty() || !showUrl.startsWith("http")) {
            return;
        }
        getIntent().setData(Uri.parse(showUrl));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        subscribeConfirmationAlertDialog = builder
                .setTitle("Subscribe to Podcast")
                .setMessage("Would you like to subscribe to: " + showUrl + "?")
                .setPositiveButton("Subscribe", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        intent.setData(Uri.EMPTY);
                        VPodPlayer.startSubscribeService(getApplicationContext(), showUrl);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        intent.setData(Uri.EMPTY);
                    }
                })
                .show();

    }

    // TODO - this was created to stop duplication and should be refactored
    public static void startSubscribeService (Context context, String showUrl) {
        Intent intent = new Intent(context, UpdateSubscriptionService.class);
        intent.setData(UpdateSubscriptionService.URI_SUBSCRIBE);
        intent.putExtra("showUrl", showUrl);
        context.startService(intent);
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoggingSubscriber<Class>() {
                    @Override
                    public void onNext(Class cls) {
                        Fragment fragment =
                                getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                        if (cls == ShowListFragment.class && fragment instanceof ShowListFragment) {
                            setShowListFragment();
                        } else if (cls == EpisodeListFragment.class && fragment instanceof EpisodeListFragment) {
                            Bundle arguments = fragment.getArguments();
                            setEpisodeListFragment(arguments.getInt("showId"), 0);
                        } else {
                            safeToast("Not implemented subscribeToRefreshFragment");
                        }
                    }
                });
        subscriptions.add(sub);
    }

    private void subscribeToEpisodeClicked() {
        Subscription sub = EpisodesRecyclerViewAdapter.EpisodeClickBus.getEvents()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoggingSubscriber<Episode>() {
                    @Override
                    public void onNext(Episode episode) {
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
                .subscribe(new LoggingSubscriber<Show>() {
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
                .subscribe(new LoggingSubscriber<String>() {
                    @Override
                    public void onNext(String s) {
                        makeToast(s);
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
        final MenuItem subscribeItem = menu.findItem(R.id.menu_subscribe);

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

        CloseSubscribeActionViewBus.getEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        subscribeItem.collapseActionView();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh_podcast:
                handleRefreshButton();
                return true;
            case android.R.id.home:
                handleHomeButton();
                return true;
            case R.id.export:
                handleExportImport();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleExportImport() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder
                .setTitle("ExportService")
                .setMessage("Full URL of Server")
                .setPositiveButton("Export", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String url = input.getText().toString();

                        Context context = getApplicationContext();

                        Intent intent = new Intent(context, ExportService.class);
                        intent.setData(ExportService.URI_EXPORT);
                        intent.putExtra("url", url);
                        context.startService(intent);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
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

    private void handleRefreshButton() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment instanceof EpisodeListFragment) {
            int showId = fragment.getArguments().getInt("showId");
            getNewEpisodesForShow(showId);
        } else if (fragment instanceof PlayerFragment) {
            ((PlayerFragment) fragment).restartEpisode();
        } else if (fragment instanceof ShowListFragment) {
            ((ShowListFragment) fragment).refreshAllEpisodes();
        } else {
            safeToast("Not implemented handleRefreshButton");
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

    private void getNewEpisodesForShow(int showId) {
        Shows db = new Shows(getApplicationContext());
        Show show = db.getById(showId);
        VPodPlayer.startSubscribeService(getApplicationContext(), show.url);
    }

    public static void safeToast(String s) {
       ToastErrorBus.publish(s);
    }

    public void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}

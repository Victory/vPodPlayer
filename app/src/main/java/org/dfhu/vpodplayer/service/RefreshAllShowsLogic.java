package org.dfhu.vpodplayer.service;

import android.support.annotation.NonNull;

import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.feed.SubscribeToFeed;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.LoggingSubscriber;
import org.dfhu.vpodplayer.util.StringsProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Subscriber;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

class RefreshAllShowsLogic {
    private final RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification;
    private final Shows showsDb;
    private final SubscribeToFeed subscribeToFeed;
    private final StringsProvider stringsProvider;
    private final Subscriber<RefreshResults> subscriber;

    @SuppressWarnings("WeakerAccess")
    public static final int NUM_NETWORK_THREADS = 3;

    private static final Object guard = new Object();

    private RefreshAllShowsLogic(
            @NonNull RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification,
            @NonNull Shows showsDb,
            @NonNull SubscribeToFeed subscribeToFeed,
            @NonNull Subscriber<RefreshResults> subscriber,
            @NonNull StringsProvider stringsProvider) {
        this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
        this.showsDb = showsDb;
        this.subscribeToFeed = subscribeToFeed;
        this.stringsProvider = stringsProvider;
        this.subscriber = subscriber;
    }

    static class Builder {
        private RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification = null;
        private Shows showsDb = null;
        private SubscribeToFeed subscribeToFeed = null;
        private StringsProvider stringsProvider = null;
        private Subscriber<RefreshResults> subscriber = null;

        Builder() {
        }

        Builder refreshAllShowsServiceNotification(RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification) {
            this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
            return this;
        }

        Builder showsDb(@NonNull Shows showsDb) {
            this.showsDb = showsDb;
            return this;
        }

        Builder subscribeToFeed(@NonNull SubscribeToFeed subscribeToFeed) {
            this.subscribeToFeed = subscribeToFeed;
            return this;
        }

        Builder stringsProvider(@NonNull StringsProvider stringsProvider) {
            this.stringsProvider = stringsProvider;
            return this;
        }

        Builder subscriber(@NonNull Subscriber<RefreshResults> subscriber) {
            this.subscriber = subscriber;
            return this;
        }

        public RefreshAllShowsLogic build() {
            //noinspection PrivateMemberAccessBetweenOuterAndInnerClass
            return new RefreshAllShowsLogic(
                    this.refreshAllShowsServiceNotification,
                    this.showsDb,
                    this.subscribeToFeed,
                    this.subscriber,
                    this.stringsProvider
            );
        }

    }

    /**
    static void debugSleep() {
        try {
            int sleep = (int) Math.floor(Math.random() * 3000);
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    **/

    void handleIntent() {
        ConnectableObservable<RefreshResults> connectable =
                ConnectableObservable.fromCallable(
                        new RefreshAllShows(refreshAllShowsServiceNotification, showsDb, subscribeToFeed, stringsProvider))
                        .subscribeOn(Schedulers.io())
                .publish();

        connectable.subscribe(new RefreshAllShowsSubscriber(refreshAllShowsServiceNotification, stringsProvider));
        if (subscriber != null) {
            connectable.subscribe(subscriber);
        }
        connectable.connect();
    }

    static class RefreshResults {
        AtomicInteger numShowsUpdated = new AtomicInteger(0);
    }

    private static class RefreshAllShows implements Callable<RefreshResults> {

        private final Shows showsDb;
        private final StringsProvider stringsProvider;
        private final RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification;
        private final SubscribeToFeed subscribeToFeed;

        RefreshAllShows(RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification,
                        Shows showsDb,
                        SubscribeToFeed subscribeToFeed,
                        StringsProvider stringsProvider) {
            this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
            this.showsDb = showsDb;
            this.subscribeToFeed = subscribeToFeed;
            this.stringsProvider = stringsProvider;
        }

        @Override
        public RefreshResults call() throws Exception {
            showNotification(stringsProvider.getString(R.string.ellipsis));
            return updateEachShow();
        }

        private RefreshResults updateEachShow() throws InterruptedException {
            final RefreshResults refreshResults = new RefreshResults();
            final SubscribeToFeed threadSubscribeToFeed = subscribeToFeed;

            final List<Show> showList = showsDb.all();
            ExecutorService pool = Executors.newFixedThreadPool(NUM_NETWORK_THREADS);

            List<Callable<Object>> showsTodo = new ArrayList<>(showList.size());

            for (Show show: showList) {
                final String showUrl = show.url;
                final String showTitle = show.title;
                showsTodo.add(Executors.callable(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (guard) {
                            showNotification(showTitle);
                        }
                        try {
                            threadSubscribeToFeed.fetchNew(showUrl);
                        } catch (IOException e) {
                            // TODO
                            e.printStackTrace();
                            return;
                        }
                        refreshResults.numShowsUpdated.incrementAndGet();
                    }
                }));
            }

            pool.invokeAll(showsTodo);
            pool.awaitTermination(1, TimeUnit.SECONDS);
            return refreshResults;
        }

        void showNotification(String showTitle) {
            String appName = stringsProvider.getString(R.string.app_name);
            String info = stringsProvider.getString(R.string.updatingFeed, showTitle);
            refreshAllShowsServiceNotification.show(appName, info);
        }
    }

    private static class RefreshAllShowsSubscriber extends LoggingSubscriber<RefreshResults> {

        private final RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification;
        private final StringsProvider stringsProvider;

        RefreshAllShowsSubscriber(
                RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification,
                StringsProvider stringsProvider) {
            this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
            this.stringsProvider = stringsProvider;
        }

        @Override
        public void onNext(RefreshResults refreshResults) {
            showNotification(refreshResults);
        }

        private void showNotification(RefreshResults refreshResults) {
            String appName = stringsProvider.getString(R.string.app_name);
            int numUpdated = refreshResults.numShowsUpdated.get();
            String info =
                    stringsProvider.getQuantityString(R.plurals.numShowsUpdated, numUpdated, numUpdated);
            refreshAllShowsServiceNotification.show(appName, info);
        }
    }
}
package org.dfhu.vpodplayer.service;

import android.support.annotation.NonNull;

import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.LoggingSubscriber;
import org.dfhu.vpodplayer.util.StringsProvider;

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
    private final StringsProvider stringsProvider;
    private final Subscriber<RefreshResults> subscriber;

    @SuppressWarnings("WeakerAccess")
    public static final int NUM_NETWORK_THREADS = 3;

    private RefreshAllShowsLogic(
            @NonNull RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification,
            @NonNull Shows showsDb,
            @NonNull StringsProvider stringsProvider, Subscriber<RefreshResults> subscriber) {
        this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
        this.showsDb = showsDb;
        this.stringsProvider = stringsProvider;
        this.subscriber = subscriber;
    }

    static class Builder {
        private RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification;
        private Shows showsDb;
        private StringsProvider stringsProvider;
        private Subscriber<RefreshResults> subscriber;

        Builder refreshAllShowsServiceNotification(RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification) {
            this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
            return this;
        }

        Builder showsDb(@NonNull Shows showsDb) {
            this.showsDb = showsDb;
            return this;
        }

        Builder stringsProvider(StringsProvider stringsProvider) {
            this.stringsProvider = stringsProvider;
            return this;
        }

        Builder subscriber(Subscriber<RefreshResults> subscriber) {
            this.subscriber = subscriber;
            return this;
        }

        public RefreshAllShowsLogic build() {
            //noinspection PrivateMemberAccessBetweenOuterAndInnerClass
            return new RefreshAllShowsLogic(
                    this.refreshAllShowsServiceNotification,
                    this.showsDb,
                    this.stringsProvider,
                    this.subscriber);
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
                ConnectableObservable.fromCallable(new RefreshAllShows(refreshAllShowsServiceNotification, showsDb, stringsProvider))
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

        RefreshAllShows(RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification,
                        Shows showsDb,
                        StringsProvider stringsProvider) {
            this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
            this.showsDb = showsDb;
            this.stringsProvider = stringsProvider;
        }

        @Override
        public RefreshResults call() throws Exception {
            showNotification(stringsProvider.getString(R.string.ellipsis));

            return updateEachShow();
        }

        private RefreshResults updateEachShow() throws InterruptedException {
            final RefreshResults refreshResults = new RefreshResults();
            final List<Show> showList = showsDb.all();
            ExecutorService pool = Executors.newFixedThreadPool(NUM_NETWORK_THREADS);

            List<Callable<Object>> showsTodo = new ArrayList<>(showList.size());

            for (Show show: showList) {
                final String title = show.title;
                showsTodo.add(Executors.callable(new Runnable() {
                    @Override
                    public void run() {
                        showNotification(title);
                        //debugSleep();
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
            String info = stringsProvider.getString(R.string.numShowsUpdated, refreshResults.numShowsUpdated.get());
            refreshAllShowsServiceNotification.show(appName, info);
        }
    }
}

package org.dfhu.vpodplayer.service;

import android.support.annotation.NonNull;
import android.util.Log;

import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.feed.SubscriptionManager;
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

import rx.Subscriber;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

class RefreshAllShowsLogic {
    private final RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification;
    private final Shows showsDb;
    private final SubscriptionManager subscriptionManager;
    private final StringsProvider stringsProvider;
    private final Subscriber<RefreshAllShowsService.RefreshResults> subscriber;

    @SuppressWarnings("WeakerAccess")
    public static final int NUM_NETWORK_THREADS = 3;

    private static final Object notificationGuard = new Object();

    private RefreshAllShowsLogic(
            @NonNull RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification,
            @NonNull Shows showsDb,
            @NonNull SubscriptionManager subscriptionManager,
            Subscriber<RefreshAllShowsService.RefreshResults> subscriber,
            @NonNull StringsProvider stringsProvider) {
        this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
        this.showsDb = showsDb;
        this.subscriptionManager = subscriptionManager;
        this.stringsProvider = stringsProvider;
        this.subscriber = subscriber;
    }

    static class Builder {
        private RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification = null;
        private Shows showsDb = null;
        private SubscriptionManager subscriptionManager;
        private StringsProvider stringsProvider = null;
        private Subscriber<RefreshAllShowsService.RefreshResults> subscriber = null;

        Builder() {
        }

        Builder refreshAllShowsServiceNotification(@NonNull RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification) {
            this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
            return this;
        }

        Builder showsDb(@NonNull Shows showsDb) {
            this.showsDb = showsDb;
            return this;
        }

        Builder subscriptionManager(@NonNull SubscriptionManager subscriptionManager) {
            this.subscriptionManager = subscriptionManager;
            return this;
        }

        Builder stringsProvider(@NonNull StringsProvider stringsProvider) {
            this.stringsProvider = stringsProvider;
            return this;
        }

        Builder subscriber(@NonNull Subscriber<RefreshAllShowsService.RefreshResults> subscriber) {
            this.subscriber = subscriber;
            return this;
        }

        public RefreshAllShowsLogic build() {
            //noinspection PrivateMemberAccessBetweenOuterAndInnerClass
            return new RefreshAllShowsLogic(
                    this.refreshAllShowsServiceNotification,
                    this.showsDb,
                    this.subscriptionManager,
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
        RefreshAllShows refreshAllShows = new RefreshAllShows(
                refreshAllShowsServiceNotification,
                showsDb,
                subscriptionManager,
                stringsProvider);

        ConnectableObservable<RefreshAllShowsService.RefreshResults> connectable =
                ConnectableObservable.fromCallable(refreshAllShows)
                        .subscribeOn(Schedulers.io())
                        .publish();

        connectable.subscribe(new RefreshAllShowsSubscriber(refreshAllShowsServiceNotification, stringsProvider));
        if (subscriber != null) {
            connectable.subscribe(subscriber);
        }
        connectable.connect();
    }

    private static class RefreshAllShows implements Callable<RefreshAllShowsService.RefreshResults> {

        private final Shows showsDb;
        private final StringsProvider stringsProvider;
        private final RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification;
        private final SubscriptionManager subscriptionManager;

        RefreshAllShows(RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification,
                        Shows showsDb,
                        SubscriptionManager subscriptionManager,
                        StringsProvider stringsProvider) {
            this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
            this.showsDb = showsDb;
            this.subscriptionManager = subscriptionManager;
            this.stringsProvider = stringsProvider;
        }

        @Override
        public RefreshAllShowsService.RefreshResults call() throws Exception {
            showNotification(stringsProvider.getString(R.string.ellipsis));
            return updateEachShow();
        }

        private RefreshAllShowsService.RefreshResults updateEachShow() throws InterruptedException {
            final RefreshAllShowsService.RefreshResults refreshResults = new RefreshAllShowsService.RefreshResults();

            final List<Show> showList = showsDb.all();
            ExecutorService pool = Executors.newFixedThreadPool(NUM_NETWORK_THREADS);

            List<Callable<Object>> showsTodo = new ArrayList<>(showList.size());

            for (Show show: showList) {
                final String showTitle = show.title;
                final String showUrl = show.url;
                final SubscriptionManager threadSubscriptionManager = subscriptionManager;
                showsTodo.add(Executors.callable(new Runnable() {
                    @Override
                    public void run() {
                        //noinspection PrivateMemberAccessBetweenOuterAndInnerClass
                        synchronized (notificationGuard) {
                            showNotification(showTitle);
                        }
                        SubscriptionManager.SubscribeResults subscribeResults;
                        try {
                             subscribeResults =
                                    threadSubscriptionManager.updateSubscription(showUrl);
                        } catch (IOException e) {
                            // TODO
                            e.printStackTrace();
                            return;
                        }

                        refreshResults.numShowsUpdated.incrementAndGet();
                        refreshResults.newEpisodes.addAll(subscribeResults.newEpisodes);
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

    private static class RefreshAllShowsSubscriber extends LoggingSubscriber<RefreshAllShowsService.RefreshResults> {

        private final RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification;
        private final StringsProvider stringsProvider;

        RefreshAllShowsSubscriber(
                RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification,
                StringsProvider stringsProvider) {
            this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
            this.stringsProvider = stringsProvider;
        }

        @Override
        public void onNext(RefreshAllShowsService.RefreshResults refreshResults) {
            showNotification(refreshResults);
            RefreshAllShowsService.ServiceCompleteBus.publish(refreshResults);
        }

        private void showNotification(RefreshAllShowsService.RefreshResults refreshResults) {
            String appName = stringsProvider.getString(R.string.app_name);
            int numUpdated = refreshResults.getNewEpisodes().size();
            if (numUpdated == 0) {
                return;
            }
            String info =
                    stringsProvider.getQuantityString(R.plurals.numShowsUpdated, numUpdated, numUpdated);
            refreshAllShowsServiceNotification.show(appName, info);
        }
    }
}

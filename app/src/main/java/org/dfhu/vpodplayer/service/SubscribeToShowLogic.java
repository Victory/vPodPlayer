package org.dfhu.vpodplayer.service;

import android.support.annotation.NonNull;
import android.telecom.Call;

import org.dfhu.vpodplayer.feed.SubscriptionManager;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.LoggingSubscriber;
import org.dfhu.vpodplayer.util.StringsProvider;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

class SubscribeToShowLogic {
    private final SubscriptionManager subscriptionManager;
    private final Shows showsDb;
    private final Episodes episodesDb;
    private final StringsProvider stringsProvider;
    private final Observable<SubscribeResult> subscribeResultObservable;

    private SubscribeToShowLogic(
            @NonNull SubscriptionManager subscriptionManager,
            @NonNull Shows showsDb,
            @NonNull Episodes episodesDb,
            @NonNull Observable<SubscribeResult> subscribeResultObservable,
            @NonNull StringsProvider stringsProvider) {
        this.subscriptionManager = subscriptionManager;
        this.showsDb = showsDb;
        this.episodesDb = episodesDb;
        this.subscribeResultObservable = subscribeResultObservable;
        this.stringsProvider = stringsProvider;
    }

    public static class Builder {
        private Shows showsDb;
        private Episodes episodesDb;
        private SubscriptionManager subscriptionManager;
        private StringsProvider stringsProvider;
        private Observable<SubscribeResult> subscribeResultObservable;

        public Builder showsDb(@NonNull Shows showsDb) {
            this.showsDb = showsDb;
            return this;
        }

        public Builder episodesDb(@NonNull Episodes episodesDb) {
            this.episodesDb = episodesDb;
            return this;
        }

        public Builder subscriptionManager(@NonNull SubscriptionManager subscriptionManager) {
            this.subscriptionManager = subscriptionManager;
            return this;
        }

        public Builder subscriber(@NonNull Observable<SubscribeResult> subscribeResultObservable) {
            this.subscribeResultObservable = subscribeResultObservable;
            return this;
        }

        public Builder stringsProvider(@NonNull StringsProvider stringsProvider) {
            this.stringsProvider = stringsProvider;
            return this;
        }

        public SubscribeToShowLogic build() {
            return new SubscribeToShowLogic(
                    this.subscriptionManager,
                    this.showsDb,
                    this.episodesDb,
                    this.subscribeResultObservable,
                    this.stringsProvider
            );
        }
    }

    public static final class SubscribeResult {
        int numNewEpisodes;
    }

    void handleIntent() {
        SubscribeToShow subscribeToShow = new SubscribeToShow();
        ConnectableObservable<SubscribeResult> connectable =
                ConnectableObservable.fromCallable(subscribeToShow)
                        .subscribeOn(Schedulers.io())
                        .publish();



        SubscribeToShowSubscriber subscribeToShowSubscriber = new SubscribeToShowSubscriber();
        connectable.subscribe(subscribeToShowSubscriber);

        if (subscribeToShowSubscriber != null) {
            connectable.subscribe(subscribeToShowSubscriber);
        }
        connectable.connect();
    }

    static class SubscribeToShow implements Callable<SubscribeResult> {
        @Override
        public SubscribeResult call() throws Exception {
            SubscribeResult subscribeResult = new SubscribeResult();
            subscribeResult.numNewEpisodes += 1;
            return null;
        }
    }

    static class SubscribeToShowSubscriber extends LoggingSubscriber<SubscribeResult> {
        @Override
        public void onNext(SubscribeResult subscribeResult) {
            super.onNext(subscribeResult);
        }
    }
}

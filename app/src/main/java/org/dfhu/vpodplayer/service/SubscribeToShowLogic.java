package org.dfhu.vpodplayer.service;

import android.support.annotation.NonNull;

import org.dfhu.vpodplayer.feed.SubscriptionManager;
import org.dfhu.vpodplayer.util.LoggingSubscriber;
import org.dfhu.vpodplayer.util.StringsProvider;

import java.util.concurrent.Callable;

import rx.Subscriber;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

class SubscribeToShowLogic {
    private final SubscribeToShowService.SubscribeToShowServiceNotification subscribeToShowServiceNotification;
    private final SubscriptionManager subscriptionManager;
    private final StringsProvider stringsProvider;
    private final Subscriber<SubscribeResult> extraResultsSubscriber;
    private final String showUrl;

    private SubscribeToShowLogic(
            @NonNull SubscribeToShowService.SubscribeToShowServiceNotification subscribeToShowServiceNotification,
            @NonNull String showUrl,
            @NonNull SubscriptionManager subscriptionManager,
            @NonNull Subscriber<SubscribeResult> extraResultsSubscriber,
            @NonNull StringsProvider stringsProvider) {
        this.showUrl = showUrl;
        this.subscriptionManager = subscriptionManager;
        this.extraResultsSubscriber = extraResultsSubscriber;
        this.stringsProvider = stringsProvider;
        this.subscribeToShowServiceNotification = subscribeToShowServiceNotification;
    }

    public static class Builder {
        private SubscriptionManager subscriptionManager;
        private StringsProvider stringsProvider;
        private String showUrl;
        private Subscriber<SubscribeResult> extraResultsSubscriber;
        private SubscribeToShowService.SubscribeToShowServiceNotification subscribeToShowServiceNotification;
        Builder showUrl(@NonNull String showUrl) {
            this.showUrl = showUrl;
            return this;
        }


        Builder subscribeToShowServiceNotification(
                @NonNull SubscribeToShowService.SubscribeToShowServiceNotification subscribeToShowServiceNotification) {
           this.subscribeToShowServiceNotification = subscribeToShowServiceNotification;
            return this;
        }

        Builder subscriptionManager(@NonNull SubscriptionManager subscriptionManager) {
            this.subscriptionManager = subscriptionManager;
            return this;
        }

        Builder extraResultsSubscriber(@NonNull Subscriber<SubscribeResult> extraResultsSubscriber) {
            this.extraResultsSubscriber = extraResultsSubscriber;
            return this;
        }

        Builder stringsProvider(@NonNull StringsProvider stringsProvider) {
            this.stringsProvider = stringsProvider;
            return this;
        }

        SubscribeToShowLogic build() {
            return new SubscribeToShowLogic(
                    this.subscribeToShowServiceNotification,
                    this.showUrl,
                    this.subscriptionManager,
                    this.extraResultsSubscriber,
                    this.stringsProvider
            );
        }
    }

    public static final class SubscribeResult {
        int numNewEpisodes;
    }

    void handleIntent() {
        SubscribeToShow subscribeToShow = new SubscribeToShow(subscriptionManager, showUrl, subscribeToShowServiceNotification);
        ConnectableObservable<SubscribeResult> connectable =
                ConnectableObservable.fromCallable(subscribeToShow)
                        .subscribeOn(Schedulers.io())
                        .publish();

        SubscribeToShowSubscriber subscribeToShowSubscriber = new SubscribeToShowSubscriber();
        connectable.subscribe(subscribeToShowSubscriber);

        if (extraResultsSubscriber != null) {
            connectable.subscribe(extraResultsSubscriber);
        }
        connectable.connect();
    }

    static class SubscribeToShow implements Callable<SubscribeResult> {
        private final SubscriptionManager subscriptionManager;
        private final String showUrl;
        private final SubscribeToShowService.SubscribeToShowServiceNotification subscribeToShowServiceNotification;

        public SubscribeToShow(
                SubscriptionManager subscriptionManager,
                String showUrl,
                SubscribeToShowService.SubscribeToShowServiceNotification subscribeToShowServiceNotification) {
            this.subscriptionManager = subscriptionManager;
            this.showUrl = showUrl;
            this.subscribeToShowServiceNotification = subscribeToShowServiceNotification;
        }

        @Override
        public SubscribeResult call() throws Exception {
            SubscribeResult subscribeResult = new SubscribeResult();

            String title = "Subscribing to show";
            subscribeToShowServiceNotification.show(title, showUrl);
            subscriptionManager.subscribeToFeed(showUrl);
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

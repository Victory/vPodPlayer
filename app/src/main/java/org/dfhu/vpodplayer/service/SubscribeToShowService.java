package org.dfhu.vpodplayer.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.VPodPlayer;
import org.dfhu.vpodplayer.VPodPlayerApplication;
import org.dfhu.vpodplayer.feed.FeedFactory;
import org.dfhu.vpodplayer.feed.SubscriptionManager;
import org.dfhu.vpodplayer.fragment.ShowListFragment;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.LoggingSubscriber;
import org.dfhu.vpodplayer.util.StringsProvider;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import rx.Subscriber;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

public class SubscribeToShowService extends IntentService {
    public static final String TAG = SubscribeToShowService.class.getName();

    public static final String URI_SUBSCRIBE_STRING = "addshow://";
    public static final Uri URI_SUBSCRIBE = Uri.parse(URI_SUBSCRIBE_STRING);

    public static final int NOTIFICATIONS_INDEX = 3;

    @Inject
    StringsProvider stringsProvider;

    public SubscribeToShowService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ((VPodPlayerApplication) getApplication()).component().inject(this);

        String dataString = intent.getDataString();
        if (!dataString.equals(URI_SUBSCRIBE_STRING)) {
            Log.e(TAG, "Could not handle intent bad dataString:" + dataString);
            return;
        }

        Logic logic = buildLogic(intent);
        logic.handleIntent();
    }

    /**
     * Build the Service Logic Object, from intent, context, etc..
     * @param intent - this intent
     * @return - new Logic
     */
    @NonNull
    private Logic buildLogic(Intent intent) {
        Context applicationContext = getApplicationContext();
        Episodes episodesDb = new Episodes(applicationContext);
        Shows showsDb = new Shows(applicationContext);

        FeedFactory feedFactory = new FeedFactory();

        SubscriptionManager subscriptionManager = new SubscriptionManager(
                feedFactory,
                showsDb,
                episodesDb);

        String showUrl = intent.getStringExtra("showUrl");
        Notification.Builder notificationBuilder = new Notification.Builder(applicationContext);
        NotificationManager notificationManager =
                (NotificationManager) applicationContext.getSystemService(NOTIFICATION_SERVICE);


        Notifier.NotificationWrapper notificationWrapper =
                new Notifier.NotificationWrapper(notificationBuilder, notificationManager);
        Notifier notifier = new Notifier(notificationWrapper, stringsProvider);

        return new Logic(showUrl, subscriptionManager, notifier);
    }

    static class Logic {
        private final SubscriptionManager subscriptionManager;
        private final Notifier notifier;
        private final String showUrl;
        private Subscriber<Results> extraResultsSubscriber;

        Logic(@NonNull String showUrl,
              @NonNull SubscriptionManager subscriptionManager,
              @NonNull Notifier notifier) {
            this.showUrl = showUrl;
            this.subscriptionManager = subscriptionManager;
            this.notifier = notifier;
        }

        /**
         * The main logic for the service
         */
        void handleIntent() {
            SubscribeToShow subscribeToShow = new SubscribeToShow(subscriptionManager, showUrl, notifier);
            ConnectableObservable<Results> connectable =
                    ConnectableObservable.fromCallable(subscribeToShow)
                            .subscribeOn(Schedulers.io())
                            .publish();

            SubscribeToShowSubscriber subscribeToShowSubscriber = new SubscribeToShowSubscriber(notifier);
            connectable.subscribe(subscribeToShowSubscriber);

            if (extraResultsSubscriber != null) {
                connectable.subscribe(extraResultsSubscriber);
            }
            connectable.connect();
        }

        Logic setExtraResultsSubscriber(Subscriber<Results> extraResultsSubscriber) {
            this.extraResultsSubscriber = extraResultsSubscriber;
            return this;
        }

        static final class Results {
            Show show;
        }

        static class SubscribeToShow implements Callable<Results> {
            private final SubscriptionManager subscriptionManager;
            private final String showUrl;
            private final Notifier notifier;

            SubscribeToShow(
                    SubscriptionManager subscriptionManager,
                    String showUrl,
                    Notifier notifier) {
                this.notifier = notifier;
                this.subscriptionManager = subscriptionManager;
                this.showUrl = showUrl;
            }

            @Override
            public Results call() throws Exception {
                Results subscribeResults = new Results();

                notifier.packString(Notifier.TITLE, R.string.subscribingToShow);
                notifier.packString(Notifier.CONTENT_TEXT, R.string.updatingFeed, showUrl);
                notifier.show(NOTIFICATIONS_INDEX);

                subscribeResults.show = subscriptionManager.subscribeToFeed(showUrl);
                return subscribeResults;
            }
        }

        static class SubscribeToShowSubscriber extends LoggingSubscriber<Results> {
            private final Notifier notifier;

            SubscribeToShowSubscriber(Notifier notifier) {
                this.notifier = notifier;
            }

            @Override
            public void onNext(Results subscribeResults) {
                notifier.packString(Notifier.TITLE, R.string.newShowAdded);
                notifier.packString(Notifier.CONTENT_TEXT, subscribeResults.show.title);
                notifier.show(NOTIFICATIONS_INDEX);
                VPodPlayer.RefreshFragmentBus.publish(ShowListFragment.class);
            }
        }
    }
}

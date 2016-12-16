package org.dfhu.vpodplayer.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.dfhu.vpodplayer.VPodPlayerApplication;
import org.dfhu.vpodplayer.feed.FeedFactory;
import org.dfhu.vpodplayer.feed.SubscriptionManager;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.StringsProvider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public class RefreshAllShowsService extends IntentService {
    public static final String TAG = RefreshAllShowsService.class.getName();

    public static final String URI_REFRESH_ALL_STRING = "refreshall://shows";
    public static final Uri URI_REFRESH_ALL = Uri.parse(URI_REFRESH_ALL_STRING);

    @Inject
    public StringsProvider stringsProvider;

    public RefreshAllShowsService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ((VPodPlayerApplication) getApplication()).component().inject(this);

        String dataString = intent.getDataString();
        if (!dataString.equals(URI_REFRESH_ALL_STRING)) {
            Log.e(TAG, "Could not handle intent bad dataString:" + dataString);
            return;
        }

        RefreshAllShowsLogic logic = buildRefreshAllShowsLogic();
        logic.handleIntent();
    }

    private RefreshAllShowsLogic buildRefreshAllShowsLogic() {
        Context applicationContext = getApplicationContext();
        RefreshAllShowsLogic.Builder builder = new RefreshAllShowsLogic.Builder();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        RefreshAllShowsServiceNotification refreshAllShowsServiceNotification =
                new RefreshAllShowsServiceNotification(notificationManager, applicationContext);
        Shows showsDb = new Shows(applicationContext);
        Episodes episodesDb = new Episodes(applicationContext);

        SubscriptionManager subscriptionManager = new SubscriptionManager(
                new FeedFactory(),
                showsDb,
                episodesDb);

        return builder
                .showsDb(showsDb)
                .subscriptionManager(subscriptionManager)
                .refreshAllShowsServiceNotification(refreshAllShowsServiceNotification)
                .stringsProvider(stringsProvider)
                .build();
    }

    public static class RefreshAllShowsServiceNotification {
        private final NotificationManager notificationManager;
        private final Context applicationContext;

        RefreshAllShowsServiceNotification(NotificationManager notificationManager, Context applicationContext) {
            this.notificationManager = notificationManager;
            this.applicationContext = applicationContext;
        }

        public void show(String title, String contentText) {
            Notification notification = new Notification.Builder(applicationContext)
                    .setContentTitle(title)
                    .setContentText(contentText)
                    .setSmallIcon(android.R.drawable.ic_menu_rotate)
                    .build();

            notificationManager.notify(2, notification);
        }
    }
    public static class RefreshResults {
        AtomicInteger numShowsUpdated = new AtomicInteger(0);
        List<Episode> newEpisodes = Collections.synchronizedList(new LinkedList<Episode>());

        public List<Episode> getNewEpisodes() {
            return newEpisodes;
        }

        public int getNumShowsUpdated() {
            return numShowsUpdated.get();
        }
    }

    public static class ServiceCompleteBus {
        private ServiceCompleteBus() {}
        private static PublishSubject<RefreshResults> subject = PublishSubject.create();

        public static void publish(RefreshResults v) { subject.onNext(v); }
        public static Observable<RefreshResults> getEvents() { return subject; }
    }
}

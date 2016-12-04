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
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.StringsProvider;

import javax.inject.Inject;

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

        Context applicationContext = getApplicationContext();
        Episodes episodesDb = new Episodes(applicationContext);
        Shows showsDb = new Shows(applicationContext);

        FeedFactory feedFactory = new FeedFactory();

        SubscriptionManager subscriptionManager = new SubscriptionManager.Builder()
                .episodesDb(episodesDb)
                .showsDb(showsDb)
                .feedFactory(feedFactory)
                .build();


        String showUrl = intent.getStringExtra("showUrl");

        NotificationManager notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        SubscribeToShowServiceNotification subscribeToShowServiceNotification =
                new SubscribeToShowServiceNotification(notificationManager, applicationContext);

        SubscribeToShowLogic.Builder builder = new SubscribeToShowLogic.Builder();
        SubscribeToShowLogic subscribeToShowLogic = builder
                .showUrl(showUrl)
                .subscriptionManager(subscriptionManager)
                .subscribeToShowServiceNotification(subscribeToShowServiceNotification)
                .stringsProvider(stringsProvider)
                .build();

        subscribeToShowLogic.handleIntent();

    }
    public static class SubscribeToShowServiceNotification {
        private final NotificationManager notificationManager;
        private final Context applicationContext;

        public SubscribeToShowServiceNotification(NotificationManager notificationManager, Context applicationContext) {
            this.notificationManager = notificationManager;
            this.applicationContext = applicationContext;
        }

        public void show(String title, String contentText) {
            Notification notification = new Notification.Builder(applicationContext)
                    .setContentTitle(title)
                    .setContentText(contentText)
                    .setSmallIcon(android.R.drawable.ic_menu_rotate)
                    .build();

            notificationManager.notify(NOTIFICATIONS_INDEX, notification);
        }
    }
}

package org.dfhu.vpodplayer.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.dfhu.vpodplayer.VPodPlayerApplication;
import org.dfhu.vpodplayer.feed.SubscribeToFeed;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.StringsProvider;

import javax.inject.Inject;

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

        Context applicationContext = getApplicationContext();
        RefreshAllShowsLogic.Builder builder = new RefreshAllShowsLogic.Builder();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        RefreshAllShowsServiceNotification refreshAllShowsServiceNotification =
                new RefreshAllShowsServiceNotification(notificationManager, applicationContext);
        Shows showsDb = new Shows(applicationContext);
        Episodes episodesDb = new Episodes(applicationContext);

        SubscribeToFeed subscribeToFeed = new SubscribeToFeed(showsDb, episodesDb);

        RefreshAllShowsLogic logic = builder
                .showsDb(showsDb)
                .subscribeToFeed(subscribeToFeed)
                .refreshAllShowsServiceNotification(refreshAllShowsServiceNotification)
                .stringsProvider(stringsProvider)
                .build();

        logic.handleIntent();
    }

    public static class RefreshAllShowsServiceNotification {
        private final NotificationManager notificationManager;
        private final Context applicationContext;

        public RefreshAllShowsServiceNotification(NotificationManager notificationManager, Context applicationContext) {
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
}
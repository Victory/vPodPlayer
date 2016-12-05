package org.dfhu.vpodplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import org.dfhu.vpodplayer.util.StringsPacker;
import org.dfhu.vpodplayer.util.StringsProvider;

public class Notifier extends StringsPacker {
    private final NotificationManager notificationManager;
    private final Context applicationContext;

    public static final int TITLE = 1;
    public static final int CONTENT_TEXT = 2;

    public Notifier(Context applicationContext, StringsProvider stringsProvider) {
        this.notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        this.applicationContext = applicationContext;
        this.stringsProvider = stringsProvider;
    }

    public Notifier(
            Context applicationContext, StringsProvider stringsProvider, NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
        this.applicationContext = applicationContext;
        this.stringsProvider = stringsProvider;
    }

    static class NotificationBuilderWrapper {
        public static Notification build(Context applicationContext, String title, String contentText) {
            return new Notification.Builder(applicationContext)
                    .setContentTitle(title)
                    .setContentText(title)
                    .setSmallIcon(android.R.drawable.ic_menu_rotate)
                    .build();

        }
    }
   public void show(int notificationIndex) {
        Notification notification = NotificationBuilderWrapper.build(
                applicationContext, getPackedString(TITLE), getPackedString(CONTENT_TEXT));
        notificationManager.notify(notificationIndex, notification);
    }

}

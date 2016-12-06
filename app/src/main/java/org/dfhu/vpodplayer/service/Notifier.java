package org.dfhu.vpodplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.support.annotation.NonNull;

import org.dfhu.vpodplayer.util.StringsPacker;
import org.dfhu.vpodplayer.util.StringsProvider;

class Notifier extends StringsPacker {
    private final NotificationWrapper notificationWrapper;

    static final int TITLE = 1;
    static final int CONTENT_TEXT = 2;

    Notifier(
            @NonNull NotificationWrapper notificationWrapper,
            @NonNull StringsProvider stringsProvider) {
        super(stringsProvider);
        this.notificationWrapper = notificationWrapper;
    }

    static class NotificationWrapper {
        private final Notification.Builder notificationBuilder;
        private final NotificationManager notificationManager;

        NotificationWrapper(
                @NonNull Notification.Builder notificationBuilder,
                @NonNull NotificationManager notificationManager) {
            this.notificationBuilder = notificationBuilder;
            this.notificationManager = notificationManager;
        }

        private Notification build(String title, String contentText) {
            return notificationBuilder
                    .setContentTitle(title)
                    .setContentText(contentText)
                    .setSmallIcon(android.R.drawable.ic_menu_rotate)
                    .build();
        }

        public void show(int notificationIndex, String title, String text) {
            Notification notification = build(title, text);
            notificationManager.notify(notificationIndex, notification);
        }

    }

    public void show(int notificationIndex) {
        notificationWrapper
                .show(notificationIndex, getPackedString(TITLE), getPackedString(CONTENT_TEXT));
    }
}

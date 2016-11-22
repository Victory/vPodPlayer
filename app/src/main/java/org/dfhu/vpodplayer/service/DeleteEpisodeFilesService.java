package org.dfhu.vpodplayer.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.Formatter;
import android.util.Log;

import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class DeleteEpisodeFilesService extends IntentService {
    public static final String TAG = DeleteEpisodeFilesService.class.getName();

    public static final String URI_DELETE_LISTENED_STRING = "editshow://delete-listened";
    public static final Uri URI_DELETE_LISTENED = Uri.parse(URI_DELETE_LISTENED_STRING);
    public DeleteEpisodeFilesService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String dataString = intent.getDataString();
        if (!dataString.equals(URI_DELETE_LISTENED_STRING)) {
            Log.e(TAG, "Could not handle intent bad dataString:" + dataString);
            return;
        }
        final int showId = intent.getExtras().getInt("showId");

        DeleteEpisodes deleteEpisodes = new DeleteEpisodes(showId, getApplicationContext());
        Observable.fromCallable(deleteEpisodes)
                .subscribeOn(Schedulers.io())
                .subscribe(new DeleteEpisodesSubscriber(getApplicationContext()));
    }

    /**
     * Collates data about the deleting process
     */
    private static class DeleteResult {
        final Show show;
        int totalDeleted = 0;
        int totalFree = 0;

        DeleteResult(Show show) {
            this.show = show;
        }
    }

    static class DeleteEpisodes implements Callable<DeleteResult> {

        private final NotificationManager notificationManager;
        private final Context applicationContext;
        private final int showId;

        DeleteEpisodes(int showId, Context applicationContext) {
            this.showId = showId;
            this.notificationManager =
                    (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
            this.applicationContext = applicationContext;
        }

        private void showNotification(String showTitle) {
            String appName = applicationContext.getResources().getString(R.string.app_name);
            Notification notification = new Notification.Builder(applicationContext)
                    .setContentTitle(appName)
                    .setContentText("Deleting listened episodes for: " + showTitle)
                    .setSmallIcon(android.R.drawable.ic_menu_rotate)
                    .build();

            notificationManager.notify(1, notification);
        }

        private Show getShowRow(int showId) {
            Shows db = new Shows(applicationContext);
            return db.getById(showId);
        }

        private DeleteResult deleteEpisodes(Show show) {
            Episodes db = new Episodes(applicationContext);
            List<Episode> episodes = db.allForShow(show.id);
            DeleteResult deleteResult = new DeleteResult(show);
            for (Episode episode: episodes) {
                if (episode.isReadyToDelete()) {
                    File file = new File(Uri.parse(episode.localUri).getPath());
                    if (!file.delete()) {
                        // TODO: handle file that can't be deleted
                        Log.d(TAG, "Could not delete:  " + episode.localUri + " " + episode);
                    } else {
                        deleteResult.totalDeleted += 1;
                        deleteResult.totalFree += episode.sizeInBytes;
                        db.updateToDeleted(episode);
                    }
                }
            }
            return deleteResult;
        }

        @Override
        public DeleteResult call() throws Exception {
            DeleteResult deleteResult;
            Show show = getShowRow(showId);
            showNotification(show.title);
            deleteResult = deleteEpisodes(show);
            return deleteResult;
        }

    }

    static class DeleteEpisodesSubscriber extends Subscriber<DeleteResult> {

        private final Context applicationContext;
        private final NotificationManager notificationManager;

        DeleteEpisodesSubscriber(Context applicationContext) {
            this.applicationContext = applicationContext;
            this.notificationManager =
                    (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);

        }

        private void updateNotification(DeleteResult deleteResult) {

            String prettyFreed = Formatter.formatFileSize(applicationContext, deleteResult.totalFree);
            String msg = "Deleted " + deleteResult.totalDeleted +
                    " freed " + prettyFreed;

             Notification notification = new Notification.Builder(applicationContext)
                    .setContentTitle(deleteResult.show.title)
                    .setContentText(msg)
                    .setSmallIcon(android.R.drawable.ic_menu_rotate)
                    .build();

            notificationManager.notify(1, notification);
        }

        private void errorNotification() {
            String appName = applicationContext.getResources().getString(R.string.app_name);
            String msg = "Error deleting files.";
             Notification notification = new Notification.Builder(applicationContext)
                    .setContentTitle(appName)
                    .setContentText(msg)
                    .setSmallIcon(android.R.drawable.ic_menu_rotate)
                    .build();

            notificationManager.notify(1, notification);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            errorNotification();
            Log.e(TAG, "onError", e);
        }

        @Override
        public void onNext(DeleteResult deleteResult) {
            updateNotification(deleteResult);
        }
    }
}

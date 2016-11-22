package org.dfhu.vpodplayer.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class UnsubscribeService extends IntentService {
    public static final String TAG = UnsubscribeService.class.getName();

    public static final String URI_UNSUBSCRIBE_STRING = "editshow://unsubscribe";
    public static final Uri URI_UNSUBSCRIBE = Uri.parse(URI_UNSUBSCRIBE_STRING);

    public UnsubscribeService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String dataString = intent.getDataString();
        if (!dataString.equals(URI_UNSUBSCRIBE_STRING)) {
            Log.e(TAG, "Could not handle intent bad dataString:" + dataString);
            return;
        }
        final int showId = intent.getExtras().getInt("showId");

        Unsubscriber unsubscriber = new Unsubscriber(showId, getApplicationContext());
        Observable.fromCallable(unsubscriber)
                .subscribeOn(Schedulers.io())
                .subscribe(new UnsubscriberSubscriber(getApplicationContext()));
    }

    static class UnsubscriberResults {
        public int totalDeleted;
        public int totalFree;
    }

    static class Unsubscriber implements Callable<UnsubscriberResults> {
        private final int showId;
        private final Context applicationContext;

        public Unsubscriber(int showId, Context applicationContext) {
            this.showId = showId;
            this.applicationContext = applicationContext;
        }

        @Override
        public UnsubscriberResults call() throws Exception {
            UnsubscriberResults unsubscriberResults = new UnsubscriberResults();
            Shows db = new Shows(applicationContext);
            deleteAllEpisodes(unsubscriberResults);
            db.deleteById(showId);
            return unsubscriberResults;
        }

        private void deleteAllEpisodes(UnsubscriberResults unsubscriberResults) {
            Episodes db = new Episodes(applicationContext);
            List<Episode> episodes = db.allForShow(showId);

            File file;
            for (Episode episode: episodes) {
                if (episode.localUri == null) {
                    continue;
                }

                file = new File(Uri.parse(episode.localUri).getPath());
                if (!file.delete()) {
                        // TODO: handle file that can't be deleted
                        Log.d(TAG, "Could not delete:  " + episode.localUri + " " + episode);
                    } else {
                        unsubscriberResults.totalDeleted += 1;
                        unsubscriberResults.totalFree += episode.sizeInBytes;
                    }
            }
            db.deleteAllForShow(showId);
        }
    }


    static class UnsubscriberSubscriber extends Subscriber<UnsubscriberResults> {
        private final Context applicationContext;

        public UnsubscriberSubscriber(Context applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(UnsubscriberResults unsubscriberResults) {
            Log.d(TAG, "onNext: " + unsubscriberResults);
        }
    }
}

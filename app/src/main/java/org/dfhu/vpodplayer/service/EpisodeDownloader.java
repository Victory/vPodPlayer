package org.dfhu.vpodplayer.service;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.util.PathsUtility;

import java.io.File;

/**
 * Queues episode for downloading, BroadcastReceiver is setup in VPodPlayerApplication
 */
public class EpisodeDownloader {

    public static final String TAG = EpisodeDownloader.class.getName();

    private final DownloadManagerWrapper downloadManagerWrapper;
    private final PathsUtility pathsUtility;

    public EpisodeDownloader(DownloadManagerWrapper downloadManagerWrapper, PathsUtility pathsUtility) {
        this.downloadManagerWrapper = downloadManagerWrapper;
        this.pathsUtility = pathsUtility;
    }

    /** Mockable download manager that can create requests */
    public static class DownloadManagerWrapper {
        private final DownloadManager downloadManager;

        public DownloadManagerWrapper(DownloadManager downloadManager) {
            this.downloadManager = downloadManager;
        }

        private DownloadManager.Request buildRequest(Episode episode, Uri episodeUri, Uri destinationUri) {
            int allowedNetworkTypes = DownloadManager.Request.NETWORK_WIFI;

            // allow Mobile data when emulating
            if (Build.PRODUCT.contains("sdk_google")) {
                allowedNetworkTypes |= DownloadManager.Request.NETWORK_MOBILE;
            }

            return new DownloadManager.Request(episodeUri)
                    .setAllowedNetworkTypes(allowedNetworkTypes)
                    .setAllowedOverRoaming(false)
                    .setDescription("id: " + episode.id)
                    .setTitle(episode.title)
                    .setDestinationUri(destinationUri);
        }

        public long enqueue(Episode episode, Uri episodeUri, Uri destinationUri) {
            //Log.d(TAG, "enqueue() called with: episode = [" + episode + "], episodeUri = [" + episodeUri + "], destinationUri = [" + destinationUri + "]");

            DownloadManager.Request request = buildRequest(episode, episodeUri, destinationUri);
            return downloadManager.enqueue(request);
        }
   }

    public long enqueue(Episode episode) {
        Uri episodeUri = pathsUtility.stringToUri(episode.url);
        File destinationDir =
                pathsUtility.makeExternalFilesDirChildDirs("episodes", "show-" + episode.showId);
        if (!pathsUtility.conditionalCreateDir(destinationDir)) {
            Log.e(TAG, "enqueue: could not enqueue episode: " + episode);
            return -1;
        }
        Uri destinationUri =
                pathsUtility.fileToUri(new File(destinationDir, episode.id + ".mp3"));

        return downloadManagerWrapper.enqueue(episode, episodeUri, destinationUri);
    }
}

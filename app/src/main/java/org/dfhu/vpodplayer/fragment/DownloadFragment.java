package org.dfhu.vpodplayer.fragment;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.sqlite.Episodes;

import java.io.File;

public class DownloadFragment extends Fragment {

    DownloadManager dm;
    Context context;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();

        context.registerReceiver(
                onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        context.registerReceiver(
                onNotificationClicked,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

        if (dm == null) {
            dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            int episodeId = getArguments().getInt("episodeId");
            Episodes db = new Episodes(context);
            final Episode episode = db.getById(episodeId);

            startDownload(episode);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        context.unregisterReceiver(onComplete);
        context.unregisterReceiver(onNotificationClicked);
        super.onDestroy();
    }

    private void startDownload(Episode episode) {
        File[] externalMediaDirs = context.getExternalMediaDirs();

        if (externalMediaDirs.length == 0) {
            return;
        }

        //Uri uri = Uri.parse(episode.url);
        Uri uri = Uri.parse("http://192.168.1.6:3000/pm.mp3");

        File dir = new File(externalMediaDirs[0], "episodes");
        dir = new File(dir, "test");
        if (!dir.mkdirs()) {
            Log.e("DownloadFragment", "Could not create directory: " + dir.getAbsolutePath());
            return;
        }

        Uri destinationUri = Uri.fromFile(new File(dir, episode.id + ".mp3"));

        int allowedNetworkTypes = DownloadManager.Request.NETWORK_WIFI;

        // allow Mobile data when emulating
        if (Build.PRODUCT.contains("sdk_google")) {
            allowedNetworkTypes |= DownloadManager.Request.NETWORK_MOBILE;
        }

        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(allowedNetworkTypes)
                .setAllowedOverRoaming(false)
                .setDescription("id: " + episode.id)
                .setTitle(episode.title)
                .setDestinationUri(destinationUri);

        dm.enqueue(request);
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            DownloadManager.Query q = new DownloadManager.Query();
            long id = bundle.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
            q.setFilterById(id);
            Cursor cursor = dm.query(q);

            if (!cursor.moveToFirst()) {
                Log.e("DownloadFragment", "could not find download by id");
                return;
            }

            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                Toast.makeText(context, "Downloaded: " + title, Toast.LENGTH_LONG).show();
            }
        }
    };

    BroadcastReceiver onNotificationClicked = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
}

package org.dfhu.vpodplayer.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import org.dfhu.vpodplayer.util.JsonExporter;
import org.dfhu.vpodplayer.util.JsonHttpPoster;

import java.io.IOException;

public class ExportService extends IntentService {
    public static final String TAG = ExportService.class.getName();

    public static final String URI_EXPORT_STRING = "export://";
    public static final Uri URI_EXPORT = Uri.parse(URI_EXPORT_STRING);

    public ExportService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String dataString = intent.getDataString();
        if (!dataString.equals(URI_EXPORT_STRING)) {
            Log.e(TAG, "Could not handle intent bad dataString:" + dataString);
            return;
        }

        Logic logic = buildLogic(intent);
        logic.handleIntent();
    }

    private Logic buildLogic(Intent intent) {
        String url = intent.getStringExtra("url");
        JsonHttpPoster jsonHttpPoster = new JsonHttpPoster();
        JsonExporter jsonExporter = null; //new JsonExporter(showsDb, episodesDb);
        Logic logic = new Logic(url, jsonExporter, jsonHttpPoster);
        return logic;
    }

    private static class Logic {
        private final String url;
        private final JsonHttpPoster jsonHttpPoster;
        private final JsonExporter jsonExporter;

        public Logic(String url, JsonExporter jsonExporter, JsonHttpPoster jsonHttpPoster) {
            this.url = url;
            this.jsonHttpPoster = jsonHttpPoster;
            this.jsonExporter = jsonExporter;
        }

        void handleIntent() {
            String json = jsonExporter.export();
            try {
                jsonHttpPoster.post(url, json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

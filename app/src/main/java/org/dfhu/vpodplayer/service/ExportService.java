package org.dfhu.vpodplayer.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

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

    }
}

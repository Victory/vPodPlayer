package org.dfhu.vpodplayer.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.dfhu.vpodplayer.VPodPlayerApplication;
import org.dfhu.vpodplayer.util.StringsProvider;

import javax.inject.Inject;

public class SubscribeToShowService extends IntentService {
    public static final String TAG = SubscribeToShowService.class.getName();

    public static final String URI_SUBSCRIBE_STRING = "addshow://";
    public static final Uri URI_SUBSCRIBE = Uri.parse(URI_SUBSCRIBE_STRING);

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

        SubscribeToShowLogic.Builder builder = new SubscribeToShowLogic.Builder();
        SubscribeToShowLogic subscribeToShowLogic = builder.build();
    }
}

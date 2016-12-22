package org.dfhu.vpodplayer.service;

import android.app.IntentService;
import android.content.Intent;


abstract class VicIntentService<T> extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public VicIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        inject();
    }

    protected final T getRealApplication() {
       return (T) getApplication();
    }

    abstract void inject();

}

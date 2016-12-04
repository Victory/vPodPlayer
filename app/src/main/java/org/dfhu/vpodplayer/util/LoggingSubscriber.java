package org.dfhu.vpodplayer.util;

import android.util.Log;

import rx.Subscriber;

public class LoggingSubscriber<T> extends Subscriber<T> {
    public static final String TAG = LoggingSubscriber.class.getName();

    @Override
    public void onCompleted() {
        Log.d(TAG, "onCompleted() called");
    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "onError: ", e);
    }

    @Override
    public void onNext(T t) {
        if (t == null) {
            Log.d(TAG, "onNext: called with `NULL`");
        } else {
            Log.d(TAG, "onNext: called with " + t.toString());
        }
    }
}

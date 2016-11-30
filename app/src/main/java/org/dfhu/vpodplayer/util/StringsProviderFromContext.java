package org.dfhu.vpodplayer.util;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

@Singleton
public class StringsProviderFromContext implements StringsProvider {
    private Context context;

    private StringsProviderFromContext(@NonNull Context context) {
        this.context = context;
    }

    public static StringsProviderFromContext getInstance(Context context) {
        return new StringsProviderFromContext(context);
    }

    @Override
    public String getString(int resourceId) {
        return context.getResources().getString(resourceId);
    }

    @Override
    public String getString(int resourceId, Object... formatArgs) {
        return context.getResources().getString(resourceId, formatArgs);
    }

    @Override
    public String getQuantityString(int resourceId, int count, Object... formatArgs) {
       return context.getResources().getQuantityString(resourceId, count, formatArgs);
    }
}

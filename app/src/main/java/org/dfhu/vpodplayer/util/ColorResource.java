package org.dfhu.vpodplayer.util;

import android.content.Context;

public class ColorResource {

    private final Context context;

    public ColorResource(Context context) {
        this.context = context;
    }

    /**
     * Get a color suitable for Paint
     * @param resourceId - e.g. R.color.colorAccent
     * @return color (does not look at theme)
     */
    public int get(int resourceId) {
        return context.getResources().getColor(resourceId);
    }
}

package org.dfhu.vpodplayer.util;

import java.util.HashMap;
import java.util.Map;

public abstract class StringsPacker {
    private final Map<Integer, String> packedStrings = new HashMap<Integer, String>();
    protected StringsProvider stringsProvider;

    public void packString(int key, int resourceId) {
        String val = stringsProvider.getString(resourceId);
        packedStrings.put(key, val);
    }

    /** /// commented out until they are needed
    public void packString(int key, int resourceId, Object... formatArgs) {
        String val = stringsProvider.getString(resourceId, formatArgs);
        packedStrings.put(key, val);
    }

    public void packString(int key, int resourceId, int count, Object... formatArgs) {
        String val = stringsProvider.getQuantityString(resourceId, count, formatArgs);
        packedStrings.put(key, val);
    }
    */

    public String getPackedString(int key) {
        return packedStrings.get(key);
    }
}

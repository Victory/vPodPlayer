package org.dfhu.vpodplayer.util;


public interface StringsProvider {
    String getString(int resourceId);
    String getString(int resourceId, Object... formArgs);
    String getQuantityString(int resourceId, int count, Object... formatArgs);
}

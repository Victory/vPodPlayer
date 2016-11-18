package org.dfhu.vpodplayer.sqlite;

import android.database.Cursor;

public class ColumnCursor {
    private final Cursor cursor;

    public ColumnCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public String getStringColumn(String key) {
        return cursor.getString(cursor.getColumnIndex(key));
    }

    public int getIntColumn(String key) {
        return cursor.getInt(cursor.getColumnIndex(key));
    }

    public long getLongColumn(String key) {
        return cursor.getLong(cursor.getColumnIndex(key));
    }
}

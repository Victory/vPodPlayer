package org.dfhu.vpodplayer.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;

class ListHydrator<T> {
    private final Cursor cursor;
    private final SQLiteDatabase db;

    ListHydrator(Cursor cursor, SQLiteDatabase db) {
        this.cursor = cursor;
        this.db = db;
    }

    /**
     * Iterate over all the items, run consumeHydrator.consume(), close the cursor.
     *
     * @param consumeHydrator consumer
     * @return New list of items
     */
    List<T> hydrate(ConsumeHydrator<T> consumeHydrator) {
        try {
            List<T> items = new LinkedList<>();
            if (!cursor.moveToFirst()) {
                return items;
            }

            ColumnCursor columnCursor = new ColumnCursor(cursor);
            do {
                consumeHydrator.consume(columnCursor, items);
            } while (cursor.moveToNext());
            return items;
        } finally {
            cursor.close();
            db.close();
        }
    }
}

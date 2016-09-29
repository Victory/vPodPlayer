package org.dfhu.vpodplayer.sqlite;

import android.database.Cursor;

import java.util.LinkedList;
import java.util.List;

public class ListHydrator<T> {
    private final Cursor cursor;

    public ListHydrator(Cursor cursor) {
        this.cursor = cursor;
    }

    /**
     * Iterate over all the items, run consumeHydrator.consume(), close the cursor.
     *
     * @param consumeHydrator consumer
     * @param <T> Holds the type of object
     * @return New list of items
     */
    public <T> List<T> hydrate(ConsumeHydrator<T> consumeHydrator) {
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
        }
    }
}

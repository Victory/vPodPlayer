package org.dfhu.vpodplayer.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.dfhu.vpodplayer.model.Show;

import java.util.List;

public class Shows extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "shows";

    // Column keys
    private static final String K_ID = "id";
    private static final String K_TITLE = "title";
    private static final String K_DESCRIPTION = "description";
    private static final String K_URL = "url";

    private static final String[] COLUMNS = {
            K_ID,
            K_TITLE,
            K_DESCRIPTION,
            K_URL
    };

    private static final String CREATE =
            "CREATE TABLE `" + DB_NAME + "` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`title` TEXT," +
                    "`description` TEXT," +
                    "`url` TEXT NOT NULL UNIQUE" +
                    ")";

    public Shows(Context context) {
       super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS `" + DB_NAME + "`");
            this.onCreate(db);
        }
    }


    /**
     * Store a show
     * @param show - A podcast to subscribe to
     * @return id of the inserted show
     */
    public long add(Show show) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(K_TITLE, show.title);
        contentValues.put(K_URL, show.url);

        try {
            return db.insert(DB_NAME, null, contentValues);
        } finally {
            db.close();
        }
    }

    /**
     * Get all the shows
     * @return - will return empty list of now shows found
     */
    public List<Show> all() {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM `" + DB_NAME + "` ORDER BY `title`";
        Cursor cursor = db.rawQuery(sql, null);
        ListHydrator<Show> hydrator = new ListHydrator<>(cursor, db);

        return hydrator.hydrate(new ConsumeHydrator<Show>() {
            @Override
            public void consume(ColumnCursor cc, List<Show> items) {
                Show show = new Show();
                show.id = cc.getIntColumn(K_ID);
                show.title = cc.getStringColumn(K_TITLE);
                show.url = cc.getStringColumn(K_URL);
                show.description = cc.getStringColumn(K_DESCRIPTION);
                items.add(show);
            }
        });
    }

    /**
     * Find a show with a url or create and empty show object if none found
     *
     * @param url - the url of the show to find
     * @return - found show, or new Show() if none found
     */
    public Show findShowByUrl(String url) {
        // XXX: can be done in sql
        for (Show show: all()) {
            if (show.url.equals(url)) {
                return show;
            }
        }
        return new Show();
    }
}

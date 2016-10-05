package org.dfhu.vpodplayer.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.dfhu.vpodplayer.model.Episode;

import java.util.List;

public class Episodes extends SQLiteOpenHelper {

    private static final String DB_NAME = "episodes";
    private static final int DB_VERSION = 1;

    // Column keys
    private static final String K_ID = "id";
    private static final String K_SHOW_ID = "showId";
    private static final String K_TITLE = "title";
    private static final String K_DESCRIPTION = "description";
    private static final String K_URL = "url";
    private static final String K_IS_DOWNLOADED = "isDownloaded";
    private static final String K_FILEPATH = "filepath";

    private static final String[] COLUMNS = {
            K_ID,
            K_SHOW_ID,
            K_TITLE,
            K_DESCRIPTION,
            K_URL,
            K_IS_DOWNLOADED,
            K_FILEPATH
    };

    private static final String CREATE =
            "CREATE TABLE `" + DB_NAME + "` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`showId` INTEGER NOT NULL," +
                    "`title` TEXT," +
                    "`description` TEXT," +
                    "`url` TEXT NOT NULL UNIQUE," +
                    "`isDownloaded` INTEGER," +
                    "`filepath` TEXT" +
                    ")";

    public Episodes(Context context) {
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
     * Store and episode for a show
     * @param episode The episode to add, must have a url and showId
     * @return id of the inserted show, -1 if we couldn't add the episode
     */
    public long add(Episode episode) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            return addNoClose(episode, db);
        } finally {
            db.close();
        }
    }


    private long addNoClose(Episode episode, SQLiteDatabase db) {
        if (episode.showId <= 0) {
            Log.d("Episodes", "episode.url == null: " + episode);
            return -1;
        }

        if ( episode.url == null) {
            Log.d("Episodes", "episode.url == null: " + episode);
            return -1;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(K_SHOW_ID, episode.showId);
        contentValues.put(K_TITLE, episode.title);
        contentValues.put(K_URL, episode.url);

        return db.insert(DB_NAME, null, contentValues);
    }

    /** All episodes in the list */
    public void addAllForShow(List<Episode> episodes, int showId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            for (Episode episode : episodes) {
                episode.showId = showId;
                addNoClose(episode, db);
            }
        } finally {
            db.close();
        }
    }

    /**
     * Get all the episodes
     * @return - will return empty list of all episodes for all shows
     */
    public List<Episode> all() {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM `" + DB_NAME + "` ORDER BY `title`";
        Cursor cursor = db.rawQuery(sql, null);
        ListHydrator<Episode> hydrator = new ListHydrator<>(cursor, db);

        return hydrator.hydrate(new ConsumeHydrator<Episode>() {
            @Override
            public void consume(ColumnCursor cc, List<Episode> items) {
                Episode episode = new Episode();
                episode.id = cc.getIntColumn(K_ID);
                episode.showId = cc.getIntColumn(K_SHOW_ID);
                episode.title = cc.getStringColumn(K_TITLE);
                episode.url = cc.getStringColumn(K_URL);
                episode.description = cc.getStringColumn(K_DESCRIPTION);
                items.add(episode);
            }
        });
    }

    /**
     * Get all the episodes
     * @return - will return empty list of all episodes for all shows
     */
    public List<Episode> allForShow(int showId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM `" + DB_NAME + "` WHERE showId = " + showId + " ORDER BY `title`";
        Cursor cursor = db.rawQuery(sql, null);
        ListHydrator<Episode> hydrator = new ListHydrator<>(cursor, db);

        return hydrator.hydrate(new Hydrator());
    }


    /**
     * Get episode by id, or empty episode if none found
     * @param episodeId - the id of the episode
     * @return
     */
    public Episode getById(int episodeId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM `" + DB_NAME + "` WHERE id = " + episodeId + " LIMIT 1";
        Cursor cursor = db.rawQuery(sql, null);
        ListHydrator<Episode> hydrator = new ListHydrator<>(cursor, db);
        List<Episode> episodes = hydrator.hydrate(new Hydrator());
        if (episodes.size() == 0) {
            return new Episode();
        }
        return episodes.get(0);
    }

    private static class Hydrator implements ConsumeHydrator<Episode> {
            @Override
            public void consume(ColumnCursor cc, List<Episode> items) {
                Episode episode = new Episode();
                episode.id = cc.getIntColumn(K_ID);
                episode.showId = cc.getIntColumn(K_SHOW_ID);
                episode.title = cc.getStringColumn(K_TITLE);
                episode.url = cc.getStringColumn(K_URL);
                episode.description = cc.getStringColumn(K_DESCRIPTION);
                items.add(episode);
            }
        }
}

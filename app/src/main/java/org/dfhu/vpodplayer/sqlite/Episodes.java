package org.dfhu.vpodplayer.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.dfhu.vpodplayer.model.Episode;

import java.util.List;

public class Episodes extends SQLiteOpenHelper {

    public static final String TAG = Episodes.class.getName();

    private static final String DB_NAME = "episodes";
    private static final int DB_VERSION = 2;

    // Column keys
    private static final String K_ID = "id";
    private static final String K_SHOW_ID = "showId";
    private static final String K_TITLE = "title";
    private static final String K_DESCRIPTION = "description";
    private static final String K_URL = "url";
    private static final String K_IS_DOWNLOADED = "isDownloaded";
    private static final String K_LOCAL_URI = "localUri";
    private static final String K_PERCENT_LISTENED = "percentListened";
    private static final String K_SIZE_IN_BYTES = "sizeInBytes";
    private static final String K_DOWNLOAD_ID = "downloadId";
    private static final String K_PUB_DATE = "pubDate";
    private static final String K_DURATION = "duration";
    private static final String K_LAST_LISTENED = "lastListened";

    private static final String[] COLUMNS = {
            K_ID,
            K_SHOW_ID,
            K_TITLE,
            K_DESCRIPTION,
            K_URL,
            K_IS_DOWNLOADED,
            K_LOCAL_URI,
            K_DOWNLOAD_ID,
            K_PUB_DATE,
            K_DURATION,
            K_LAST_LISTENED
    };

    private static final String CREATE =
            "CREATE TABLE `" + DB_NAME + "` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`showId` INTEGER NOT NULL," +
                    "`title` TEXT," +
                    "`description` TEXT," +
                    "`url` TEXT NOT NULL UNIQUE," +
                    "`isDownloaded` INTEGER," +
                    "`localUri` TEXT," +
                    "`percentListened` INTEGER," +
                    "`sizeInBytes` INTEGER," +
                    "`downloadId` INTEGER," +
                    "`pubDate` STRING," +
                    "`duration` INTEGER," +
                    "`lastListened` INTEGER" +
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
        if (oldVersion < 2) {
            String sql = "ALTER TABLE `" + DB_NAME + "` ADD COLUMN `lastListened` INTEGER";
            db.execSQL(sql);
        }
    }

    /**
     * Store and episode for a show
     * @param episode The episode to addOrUpdate, must have a url and showId
     * @return id of the inserted show, -1 if we couldn't addOrUpdate the episode
     */
    public long addOrUpdate(Episode episode) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            return addNoClose(episode, db, true);
        } finally {
            db.close();
        }
    }


    /**
     * Add a record but don't close the database
     * @param episode - episode to add
     * @param db - opened database connection
     * @param shouldUpdate - true if we should update an existing record, false if we should ignore
     * @return
     */
    private long addNoClose(Episode episode, SQLiteDatabase db, boolean shouldUpdate) {
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
        contentValues.put(K_LOCAL_URI, episode.localUri);
        contentValues.put(K_IS_DOWNLOADED, episode.isDownloaded);
        contentValues.put(K_SIZE_IN_BYTES, episode.sizeInBytes);
        contentValues.put(K_PERCENT_LISTENED, episode.percentListened);
        contentValues.put(K_DOWNLOAD_ID, episode.downloadId);
        contentValues.put(K_PUB_DATE, episode.pubDate);
        contentValues.put(K_DURATION, episode.duration);
        contentValues.put(K_LAST_LISTENED, episode.lastListened);


        long result;
        try {
            result = db.insertOrThrow(DB_NAME, null, contentValues);
        } catch (SQLException e) {
            if (shouldUpdate) {
                result = db.update(DB_NAME, contentValues, "url = ?", new String[]{episode.url});
            } else {
                return -1;
            }
        }

        return result;
    }

    /**
     * Add episodes for show with showId
     * @param episodes - epiodes to add
     * @param showId
     */
    public void addAllForShow(List<Episode> episodes, int showId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            for (Episode episode : episodes) {
                episode.showId = showId;
                addNoClose(episode, db, false);
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

        return hydrator.hydrate(new Hydrator());
    }

    /**
     * Get all the episodes
     * @return - will return empty list of all episodes for all shows
     */
    public List<Episode> allForShow(int showId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM `" + DB_NAME + "` WHERE showId = " + showId
                + " ORDER BY `pubDate` DESC";

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
        String sql = "SELECT * FROM `" + DB_NAME + "` WHERE `id` = " + episodeId + " LIMIT 1";
        Cursor cursor = db.rawQuery(sql, null);
        ListHydrator<Episode> hydrator = new ListHydrator<>(cursor, db);
        List<Episode> episodes = hydrator.hydrate(new Hydrator());
        if (episodes.size() == 0) {
            return new Episode();
        }
        return episodes.get(0);
    }

    /**
     *  Find an episode by downloadId, returns null if not found
     * @param downloadId
     * @return
     */
    public Episode getByDownloadId(long downloadId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM `" + DB_NAME + "` WHERE `downloadId` = " + downloadId + " LIMIT 1";
        Cursor cursor = db.rawQuery(sql, null);
        ListHydrator<Episode> hydrator = new ListHydrator<>(cursor, db);
        List<Episode> episodes = hydrator.hydrate(new Hydrator());
        if (episodes.size() == 0) {
            return null;
        }
        return episodes.get(0);
    }

    /**
     * Update the percentListened of this episode
     *
     * @param episode - episode with percentListened set to new value
     */
    public void updatePercentListened(Episode episode) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(K_PERCENT_LISTENED, episode.percentListened);
            contentValues.put(K_LAST_LISTENED, System.currentTimeMillis());
            writableDatabase.update(DB_NAME, contentValues, "id = ?", episodeWhereClause(episode.id));
        } catch (Throwable e) {
            Log.e(TAG, "updatePercentListened: could not update", e);
        } finally {
            writableDatabase.close();
        }
    }

    public void updateToDeleted(Episode episode) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.putNull(K_LOCAL_URI);
            contentValues.put(K_IS_DOWNLOADED, false);
            contentValues.put(K_DOWNLOAD_ID, 0);
            writableDatabase.update(DB_NAME, contentValues, "id = ?", episodeWhereClause(episode.id));
        } catch (Throwable e) {
            Log.e(TAG, "updateToDeleted: could not mark deleted: " + episode, e);
        } finally {
            writableDatabase.close();
        }
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
                episode.downloadId = cc.getIntColumn(K_DOWNLOAD_ID);
                episode.localUri = cc.getStringColumn(K_LOCAL_URI);
                episode.sizeInBytes = cc.getIntColumn(K_SIZE_IN_BYTES);
                episode.percentListened = cc.getIntColumn(K_PERCENT_LISTENED);
                episode.isDownloaded = cc.getIntColumn(K_IS_DOWNLOADED);
                episode.pubDate = cc.getStringColumn(K_PUB_DATE);
                episode.duration = cc.getIntColumn(K_DURATION);
                episode.lastListened = cc.getLongColumn(K_LAST_LISTENED);

                items.add(episode);
            }
        }

    /**
     * Return episode where clause for id = ?
     * @param episodeId - id of episode to search
     * @return
     */
    private String[] episodeWhereClause(int episodeId) {
        return new String[]{"" + episodeId};
    }
}

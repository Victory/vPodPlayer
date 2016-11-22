package org.dfhu.vpodplayer.sqlite;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class VicSQLiteOpenHelper extends SQLiteOpenHelper{

    public VicSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public VicSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    /**
     * Return where clause for SOME_INT_COLUMN = ? (e.g. showId = ?, id = ?)
     */
    protected String[] idWhereClause(int id) {
        return new String[]{"" + id};
    }
}

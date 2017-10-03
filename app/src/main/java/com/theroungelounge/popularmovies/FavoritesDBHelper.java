package com.theroungelounge.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.theroungelounge.popularmovies.FavoritesContract.FavoritesEntry;

/**
 * Created by Rounge on 6/8/2017.
 */

public class FavoritesDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "favorites.db";

    public FavoritesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoritesEntry.TABLE_NAME + " (" +
                FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoritesEntry.COLUMN_TITLE + " VARCHAR(80) NOT NULL UNIQUE," +
                FavoritesEntry.COLUMN_VOTE_AVG + " REAL NOT NULL," +
                FavoritesEntry.COLUMN_RELEASE_DATE + " INTEGER NOT NULL," +
                FavoritesEntry.COLUMN_OVERVIEW + " VARCHAR(255) NOT NULL," +
                FavoritesEntry.COLUMN_POSTER_PATH + " VARCHAR(255) NOT NULL," +
                FavoritesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL);";
        Log.v("LOCATION_TABLE_KEY_TAG", SQL_CREATE_FAVORITES_TABLE);

        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoritesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

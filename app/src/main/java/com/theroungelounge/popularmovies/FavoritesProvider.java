package com.theroungelounge.popularmovies;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import static com.theroungelounge.popularmovies.FavoritesContract.FavoritesEntry.TABLE_NAME;

public class FavoritesProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoritesDBHelper favoritesDBHelper;

    static final int FAVORITE = 100;

    public FavoritesProvider() {
    }

    @Override
    public boolean onCreate() {
        favoritesDBHelper = new FavoritesDBHelper(getContext());
        return true;
    }

    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(FavoritesContract.CONTENT_AUTHORITY,
                FavoritesContract.PATH_FAVORITES, FAVORITE);

        return uriMatcher;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case FAVORITE:
                return FavoritesContract.FavoritesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch(sUriMatcher.match(uri)) {
            case FAVORITE: {
                retCursor = favoritesDBHelper.getReadableDatabase().query(
                        TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = favoritesDBHelper.getWritableDatabase();
        Uri returnUri;

        switch(sUriMatcher.match(uri)) {
            case FAVORITE: {
                long _id = db.insert(TABLE_NAME, null, values);
                if(_id > 0) {
                    returnUri = FavoritesContract.FavoritesEntry.buildFavoriteUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = favoritesDBHelper.getWritableDatabase();
        int num_rows;

        switch(sUriMatcher.match(uri)) {
            case FAVORITE: {
                num_rows = db.update(TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(num_rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return num_rows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = favoritesDBHelper.getWritableDatabase();
        int num_rows;
        selection = (selection == null) ? "1" : selection;

        switch(sUriMatcher.match(uri)) {
            case FAVORITE: {
                num_rows = db.delete(FavoritesContract.FavoritesEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(num_rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return num_rows;
    }
}

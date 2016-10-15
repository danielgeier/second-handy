package de.danielgeier.secondhandy;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * Created by daniel on 2016-09-25.
 */

public class OfferProvider extends ContentProvider {
    public static final String AUTHORITY = OffersContract.CONTENT_AUTHORITY;
    /**
     * URI ID for route: /offers
     */
    public static final int ROUTE_OFFERS = 1;
    /**
     * URI ID for route: /entries/{ID}
     */
    public static final int ROUTE_OFFERS_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "entries", ROUTE_OFFERS);
        uriMatcher.addURI(AUTHORITY, "entries/*", ROUTE_OFFERS_ID);
    }

    private OfferDatabase dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new OfferDatabase(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int match = uriMatcher.match(uri);

        switch (match) {
            case ROUTE_OFFERS_ID:
                // Return a single offer, by ID.
                String id = uri.getLastPathSegment();
                builder.where(OffersContract.Offer._ID + "=?", id);

            case ROUTE_OFFERS:
                // Return all known entries.
                builder.table(OffersContract.Offer.TABLE_NAME)
                        .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;

            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case ROUTE_OFFERS:
                return OffersContract.Offer.CONTENT_TYPE;
            case ROUTE_OFFERS_ID:
                return OffersContract.Offer.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    static class OfferDatabase extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Offers.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_TIMESTAMP = " TIMESTAMP";
        private static final String COMMA_SEP = ",";
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + OffersContract.Offer.TABLE_NAME + " (" +
                        OffersContract.Offer._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" + COMMA_SEP +
                        OffersContract.Offer.COLUMN_NAME_TITLE + TYPE_TEXT + COMMA_SEP +
                        OffersContract.Offer.COLUMN_NAME_DESCRIPTION + TYPE_TEXT + COMMA_SEP +
                        OffersContract.Offer.COLUMN_NAME_PRICE + TYPE_TEXT + COMMA_SEP +
                        OffersContract.Offer.COLUMN_NAME_URL + TYPE_TEXT + COMMA_SEP +
                        OffersContract.Offer.COLUMN_NAME_TIMESTAMP + TYPE_TIMESTAMP +
                        " )";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + OffersContract.Offer.TABLE_NAME;

        public OfferDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
package de.danielgeier.secondhandy;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by daniel on 2016-09-25.
 */
public final class OffersContract {
    public static final String CONTENT_AUTHORITY = "de.danielgeier.secondhandy";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_OFFERS = "offers";

    private OffersContract() {}

    public static abstract class Offer implements BaseColumns {
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.secondhandy.offers";

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.secondhandy.offer";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_OFFERS).build();

        public static final String TABLE_NAME = "offer";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }
}

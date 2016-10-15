package de.danielgeier.secondhandy;

import android.content.ContentValues;

/**
 * Created by Daniel on 08.09.2016.
 */
public class Offer {
    public String title;
    public String description;
    public String price;
    public String url;
    long timestamp;

    public Offer(String title, String description, String price, String url, long timestamp) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.url = url;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Offer{" +
                "title='" + title + '\'' +
                ",\ndescription='" + description.substring(0, 100) + '\'' +
                ",\nprice='" + price + '\'' +
                ",\nurl='" + url + '\'' +
                '}';
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(OffersContract.Offer.COLUMN_NAME_TITLE, title);
        values.put(OffersContract.Offer.COLUMN_NAME_DESCRIPTION, description);
        values.put(OffersContract.Offer.COLUMN_NAME_PRICE, price);
        values.put(OffersContract.Offer.COLUMN_NAME_URL, url);
        values.put(OffersContract.Offer.COLUMN_NAME_TIMESTAMP, timestamp);
        return values;
    }
}

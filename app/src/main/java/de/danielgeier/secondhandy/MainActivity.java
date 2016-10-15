package de.danielgeier.secondhandy;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Projection for querying the content provider.
     */
    private static final String[] PROJECTION = new String[]{
            OffersContract.Offer._ID,
            OffersContract.Offer.COLUMN_NAME_TITLE,
            OffersContract.Offer.COLUMN_NAME_DESCRIPTION,
            OffersContract.Offer.COLUMN_NAME_PRICE,
            OffersContract.Offer.COLUMN_NAME_URL,
            OffersContract.Offer.COLUMN_NAME_TIMESTAMP
    };

    private static final int COLUMN_TITLE = 0;
    private static final int COLUMN_DESCRIPTION = 0;
    private static final int COLUMN_PRICE = 0;
    private static final int COLUMN_URL = 0;
    private static final int COLUMN_TIMESTAMP = 0;

    private static final String[] FROM_COLUMNS = new String[]{
            OffersContract.Offer.COLUMN_NAME_TITLE,
            OffersContract.Offer.COLUMN_NAME_TIMESTAMP
    };

    private static final int[] TO_FIELDS = new int[]{
            android.R.id.text1,
            android.R.id.text2
    };

    private ListView listView;
    private SQLiteOpenHelper dbHelper = new OfferProvider.OfferDatabase(this);
    private SimpleCursorAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CreateSearchActivity.class));
            }
        });


        // Create account, if needed
        SyncUtils.CreateSyncAccount(this);

        adapter = new SimpleCursorAdapter(
                this,       // Current context
                android.R.layout.simple_list_item_activated_2,  // Layout for individual rows
                null,                // Cursor
                FROM_COLUMNS,        // Cursor columns to use
                TO_FIELDS,           // Layout fields to use
                0                    // No flags
        );
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                if (i == COLUMN_TIMESTAMP) {
                    // Convert timestamp to human-readable date
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.add(GregorianCalendar.MILLISECOND, i);
                    ((TextView) view).setText(SimpleDateFormat.getDateTimeInstance().format(cal.getTime()));
                    return true;
                } else {
                    // Let SimpleCursorAdapter handle other fields automatically
                    return false;
                }
            }
        });
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        TextView v = new TextView(this);
        v.setText(R.string.loading);
        listView.setEmptyView(v);
        getSupportLoaderManager().initLoader(0, null, this);


        String query = "olympus";
        int priceLow = 0;
        int priceHigh = 5000;

        Date startDate = new Date(1470009600); // 2016-08-01

        new AsyncTask<Void, Void, ArrayList<Offer>>() {
            protected ArrayList<Offer> doInBackground(Void... params) {
                String url = "https://www.blocket.se/stockholm?q=olympus";
                ArrayList<Offer> offers = new ArrayList<>();
                Document doc;
                try {
                    doc = Jsoup.connect(url).get();
                } catch (IOException e) {
                    e.printStackTrace();
                    return offers;
                }

                Elements elems = doc.getElementsByAttributeValue("itemtype", "http://schema.org/Offer");

                for (Element elem : elems) {
                    Element item_link = elem.getElementsByClass("item_link").first();
                    String title = item_link.attr("title");
                    String item_url = item_link.attr("href");
                    String price = elem.getElementsByAttributeValue("itemprop", "price").text();
                    String description;

                    try {
                        Document doc2 = Jsoup.connect(item_url).get();
                        description = doc2.getElementsByClass("body").text().trim();
                    } catch (IOException e) {
                        e.printStackTrace();
                        description = e.toString();
                    }

                    Offer o = new Offer(title, description, price, item_url, new Date().getTime());
                    offers.add(o);
                    dbHelper.getWritableDatabase().insert(OffersContract.Offer.TABLE_NAME,
                            null, o.toContentValues());
                }

                return offers;
            }

            @Override
            protected void onPostExecute(ArrayList<Offer> s) {
                listView.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,
                        s));
            }
        }.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Query the content provider for data.
     * <p>
     * <p>Loaders do queries in a background thread. They also provide a ContentObserver that is
     * triggered when data in the content provider changes. When the sync adapter updates the
     * content provider, the ContentObserver responds by resetting the loader and then reloading
     * it.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // We only have one loader, so we can ignore the value of i.
        // (It'll be '0', as set in onCreate().)
        return new CursorLoader(this,  // Context
                OffersContract.Offer.CONTENT_URI, // URI
                PROJECTION,                // Projection
                null,                           // Selection
                null,                           // Selection args
                OffersContract.Offer.COLUMN_NAME_TIMESTAMP + " desc");
    }

    /**
     * Move the Cursor returned by the query into the ListView adapter. This refreshes the existing
     * UI with the data in the Cursor.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.changeCursor(cursor);
    }

    /**
     * Called when the ContentObserver defined for the content provider detects that data has
     * changed. The ContentObserver resets the loader, and then re-runs the loader. In the adapter,
     * set the Cursor value to null. This removes the reference to the Cursor, allowing it to be
     * garbage-collected.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.changeCursor(null);
    }

}

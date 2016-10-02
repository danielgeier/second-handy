package de.danielgeier.secondhandy;

import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private SQLiteOpenHelper dbHelper = new OfferProvider.OfferDatabase(this);

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

        // Get the magic goin'
        // final TextView mTextView = (TextView) findViewById(R.id.textview);

        listView = (ListView) findViewById(R.id.listView);

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
                    dbHelper.getWritableDatabase().insert(OfferContract.Entry.TABLE_NAME,
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
}

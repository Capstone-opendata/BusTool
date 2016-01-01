package capstone2015project.bustool;

import android.app.ActionBar;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import static android.R.layout.simple_list_item_1;

public class BusstopDbActivity extends AppCompatActivity {
    ListView stopsListView;
    ArrayList<String> stopList;
    private ProgressBar spinner;
    SQLiteHelper BsDb;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busstop_db);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        BsDb = new SQLiteHelper(BusstopDbActivity.this); //<-- Opens database Access
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        final EditText userInput = (EditText)findViewById(R.id.editTextDb);
        final TextView headView = (TextView) findViewById(R.id.headView);
        final RadioButton rButton0=(RadioButton) findViewById(R.id.radioButton0);
        rButton0.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                userInput.setVisibility(View.INVISIBLE);
                //change layout
                RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);p.addRule(RelativeLayout.BELOW,R.id.headView);
                stopsListView.setLayoutParams(p);
                headView.setText(R.string.string_favorites);
                ArrayList array_list = BsDb.getQuery("SELECT * FROM busstops WHERE bs_fav = 1");
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(BusstopDbActivity.this, android.R.layout.simple_list_item_1, array_list);
                stopsListView = (ListView) findViewById(R.id.DbView);
                stopsListView.setAdapter(arrayAdapter);
                stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        Intent i = new Intent(getApplicationContext(), ResultActivity.class);
                        i.putExtra("busStopNumber", BsDb.BsIdList.get(arg2));
                        startActivity(i);
                    }
                });

            }
        });

        final RadioButton rButton1=(RadioButton) findViewById(R.id.radioButton1);
        rButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //When rButton is clicked:
                //Changes layout
                userInput.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);p.addRule(RelativeLayout.BELOW, R.id.editTextDb);
                stopsListView.setLayoutParams(p);
                headView.setText(R.string.string_addfavorites);
                // Db Control
                ArrayList array_list = BsDb.getAllBSs();
                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(BusstopDbActivity.this, android.R.layout.simple_list_item_1, array_list);
                // Db to listView
                stopsListView = (ListView) findViewById(R.id.DbView);
                stopsListView.setAdapter(arrayAdapter);
                stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        // when item on list is clicked
                        BsDb.addFav(BsDb.BsIdList.get(arg2));
                        rButton0.performClick();


                    }
                });
            }
        });

        final RadioButton rButton2=(RadioButton) findViewById(R.id.radioButton2);
        rButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                userInput.setVisibility(View.INVISIBLE);
                RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);p.addRule(RelativeLayout.BELOW, R.id.headView);
                stopsListView.setLayoutParams(p);
                headView.setText(R.string.string_delfavorites);
                ArrayList array_list = BsDb.getQuery("SELECT * FROM busstops WHERE bs_fav = 1");
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(BusstopDbActivity.this, android.R.layout.simple_list_item_1, array_list);
                stopsListView = (ListView) findViewById(R.id.DbView);
                stopsListView.setAdapter(arrayAdapter);
                stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        BsDb.delFav(BsDb.BsIdList.get(arg2));
                        rButton2.performClick();

                    }
                });
            }
        });



        ArrayList array_list = BsDb.getQuery("SELECT * FROM busstops WHERE bs_fav = 1");
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(BusstopDbActivity.this, android.R.layout.simple_list_item_1, array_list);
        stopsListView = (ListView) findViewById(R.id.DbView);
        stopsListView.setAdapter(arrayAdapter);
        stopsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                stopsListView.setSelection(arg2);
            }
        });

        userInput.setVisibility(View.INVISIBLE);
        userInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (userInput.getText().toString().isEmpty()) {
                    userInput.clearFocus();
                } else {
                    String query = "SELECT * FROM busstops WHERE bs_id LIKE '%" + userInput.getText().toString() + "%' OR bs_nm LIKE '" + userInput.getText().toString() + "%' ";
                    ArrayList array_list = BsDb.getQuery(query);
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(BusstopDbActivity.this, android.R.layout.simple_list_item_1, array_list);
                    stopsListView = (ListView) findViewById(R.id.DbView);
                    stopsListView.setAdapter(arrayAdapter);
                    stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            BsDb.addFav(BsDb.BsIdList.get(arg2));
                            rButton0.performClick();
                        }
                    });
                }
            }
        });
        userInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //...
                    // Perform your action on key press here
                    // ...
                    Intent i = new Intent(getApplicationContext(), ResultActivity.class);
                    if (!BsDb.BsIdList.isEmpty()) {
                        // pick first one on the list
                        i.putExtra("busStopNumber", BsDb.BsIdList.get(0).toString());
                        startActivity(i);
                        userInput.setText("");
                    }
                    return true;
                }
                if (keyevent.getAction() == KeyEvent.ACTION_UP) {
                    if (userInput.getText().toString().isEmpty()) {
                        userInput.clearFocus();
                        rButton1.performClick();
                    }
                    return true;
                }
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_BACK)) {
                    userInput.clearFocus();
                    userInput.setText("");
                    rButton1.performClick();
                    return true;
                }
                return false;

            }
        });

        BsDb.close(); //<-- Closes database access!
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "BusstopDb Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://capstone2015project.bustool/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.database, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        BsDb = new SQLiteHelper(BusstopDbActivity.this);;
        switch (item.getItemId()) {
            case R.id.action_updatedb:
                BsDb.DeleteAll();
                final TextView headView = (TextView) findViewById(R.id.headView);
                final RadioGroup group1 = (RadioGroup) findViewById(R.id.radioGroup);
                final ListView lview = (ListView) findViewById(R.id.DbView);
                group1.setVisibility(View.INVISIBLE);
                lview.setVisibility(View.INVISIBLE);
                headView.setText(R.string.string_dl_wait);
                String url = "http://data.foli.fi/gtfs/v0/stops";
                new ProcessJSON().execute(url);
                BsDb.close();
                return true;
            case R.id.action_deletedb:
                RadioButton rbutton = (RadioButton) findViewById(R.id.radioButton0);
                BsDb.DeleteAll();
                rbutton.performClick();
                BsDb.close();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "BusstopDb Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://capstone2015project.bustool/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class ProcessJSON extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... strings) {
            //String stream = null;
            String urlString = strings[0];

            HTTPDataHandler hh = new HTTPDataHandler();
            String stream = hh.GetHTTPData(urlString);

            // Return the data from specified url
            return stream;
        }

        protected void onPostExecute(String stream) {


            if (stream != null) {
                final TextView headView = (TextView) findViewById(R.id.headView);
                final ListView lview = (ListView) findViewById(R.id.DbView);
                final RadioGroup group1 = (RadioGroup) findViewById(R.id.radioGroup);
                final RadioButton button = (RadioButton) findViewById(R.id.radioButton0);
                try {

                    JSONObject stopsObject = new JSONObject(stream);
                    // Get the JSONArray stops
                    JSONArray stopsArray = stopsObject.toJSONArray(stopsObject.names());


                    for (int i = 0; i < stopsArray.length(); i++) {
                        JSONObject stop = stopsArray.getJSONObject(i);

                        String stopNumber = (String) stopsObject.names().get(i);
                        String stopName = stop.getString("stop_name");
                        String lat = stop.getString("stop_lat");
                        String lon = stop.getString("stop_lon");

                        double lati = Double.parseDouble(lat);
                        double longi = Double.parseDouble(lon);

                        BsDb.insertBS(stopNumber, stopName,lati, longi);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                group1.setVisibility(View.VISIBLE);
                lview.setVisibility(View.VISIBLE);
                button.performClick();
            } // if statement end
        } // onPostExecute() end
    } // ProcessJSON class end

}

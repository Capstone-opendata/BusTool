package capstone2015project.buscatchers;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import java.util.ArrayList;

/**
 * Used for handling user's favorites.
 */
public class BusstopDbActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    ListView stopsListView;
    ArrayList<String> stopList;
    SQLiteHelper BsDb;
    private ProgressBar spinner;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * Initializes the activity.
     * @param savedInstanceState saved data of previous state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busstop_db);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
                ArrayList array_list = BsDb.getQuery("SELECT * FROM busstops WHERE bs_fav != 1");
                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(BusstopDbActivity.this, android.R.layout.simple_list_item_1, array_list);
                // Db to listView
                stopsListView = (ListView) findViewById(R.id.DbView);
                stopsListView.setAdapter(arrayAdapter);
                stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        // when item on list is clicked
                        BsDb.addFav(BsDb.BsIdList.get(arg2));
                        rButton1.performClick();


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

                    String query = "SELECT * FROM busstops WHERE bs_id LIKE '%" + userInput.getText().toString() + "%' OR bs_nm LIKE '" + userInput.getText().toString() + "%' ";
                    ArrayList array_list = BsDb.getQuery(query);
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(BusstopDbActivity.this, android.R.layout.simple_list_item_1, array_list);
                    stopsListView = (ListView) findViewById(R.id.DbView);
                    stopsListView.setAdapter(arrayAdapter);
                    stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            BsDb.addFav(BsDb.BsIdList.get(arg2));
                            rButton1.performClick();
                        }
                    });

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
                    rButton0.performClick();
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
                Uri.parse("android-app://capstone2015project.buscatchers/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            this.finish();
        }
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
        BsDb = new SQLiteHelper(BusstopDbActivity.this);
        switch (item.getItemId()) {
            case R.id.action_updatedb:
                TextView headView = (TextView) findViewById(R.id.headView);
                RadioGroup group1 = (RadioGroup) findViewById(R.id.radioGroup);
                ListView lview = (ListView) findViewById(R.id.DbView);
                RadioButton rButton0 = (RadioButton)findViewById(R.id.radioButton0);
                rButton0.performClick();
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
                Uri.parse("android-app://capstone2015project.buscatchers/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the navigation action
            Intent i = new Intent(this, StopToolSelectionActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_nearby) {
            Intent i = new Intent(this, NearbyActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_map) {
            Intent i = new Intent(this, StopsMapActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_favorites) {
            Intent i = new Intent(this, BusstopDbActivity.class);
            startActivity(i);
/*
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
*/
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                ArrayList array_list = BsDb.getQuery("SELECT * FROM busstops WHERE bs_fav = 1");
                BsDb.DeleteAll();
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
                        BsDb.insertBS(stopNumber, stopName, lati, longi, "0");

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int x=0;
                while (x < BsDb.BsIdList.size()){
                    BsDb.addFav(BsDb.BsIdList.get(x));
                    x++;
                }
                group1.setVisibility(View.VISIBLE);
                lview.setVisibility(View.VISIBLE);
                button.performClick();
            } // if statement end
        } // onPostExecute() end
    } // ProcessJSON class end

}

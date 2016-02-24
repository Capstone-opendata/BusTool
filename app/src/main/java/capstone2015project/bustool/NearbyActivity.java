package capstone2015project.bustool;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Retrieves and displays a distance sorted list of nearby bus stops.
 * User's location information is retrieved by GPS.
 */
public class NearbyActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LocationListener {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;   // for permission requesting
    private boolean JSONretrievalStarted = false;   // for stopping multiple data retrievals from foli

    private boolean GPSenabled = false;     // is GPS enabled on user's device

    ListView stopsListView;     // the list view where bus stops are shown
    Location myLocation;        // variable to store user's current location
    LocationManager myLocationManager;  //used for location retrieval
    ArrayList<String> stopList;     //arraylist for storing list items
    private ProgressBar spinner;    // this will be displayed while retrieving data
    private TextView waitText;      // used to display a wait message along with progress spinner

    /**
     * Initializes the activity.
     * @param savedInstanceState saved data of previous state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        spinner = (ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
        waitText = (TextView)findViewById(R.id.NearbyWaitMessage);
        waitText.setVisibility(View.GONE);

        //hard coding myLocation for testing
        myLocation = new Location("MyLoc");
        //myLocation.setLatitude(60.4491652);
        //myLocation.setLongitude(22.2933068);

        // Get ListView object from xml
        stopsListView = (ListView) findViewById(R.id.listView);
        stopList = new ArrayList<String>();

    }

    /**
     * Starts GPS location retrieval.
     */
    protected void onStart() {
        super.onStart();

        if(JSONretrievalStarted == false)
        {
            myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            GPSenabled = myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if(GPSenabled == false)
            {
                // GPS is not enabled on user's device


                showMessageOKCancel(getResources().getString(R.string.Enable_GPS_Message),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                //return;
            }
            else
            {
                // GPS is enabled on user's device
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(NearbyActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ASK_PERMISSIONS);
                    return;
                }
                myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                spinner.setVisibility(View.VISIBLE);
                waitText.setVisibility(View.VISIBLE);
            }// else

        }//if JSONretrievalStarted

        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                if (JSONretrievalStarted)
                {
                    cancel();
                }
            }

            public void onFinish() {
                showMessageOKCancel(getResources().getString(R.string.Nearby_Fail_Message),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (ActivityCompat.checkSelfPermission(NearbyActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NearbyActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(NearbyActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            123);
                                    return;
                                }
                                Location myLocation = myLocationManager
                                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                myLocation.setAccuracy(59);
                                onLocationChanged(myLocation);

                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
            }
        }.start();

    }// onStart

    /**
     * Stops GPS location retrieval.
     */
    public void onStop()
    {
        super.onStop();

        //stopping gps updates here to prevent gps staying on pointlessly in certain situations
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        myLocationManager.removeUpdates(this);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener,
                                     DialogInterface.OnClickListener cancelListener) {

        new AlertDialog.Builder(NearbyActivity.this)
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.OK_button), okListener)
                .setNegativeButton(getResources().getString(R.string.Cancel_button), null)
                .create()
                .show();
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
        getMenuInflater().inflate(R.menu.nearby, menu);
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

    /**
     * Listens to gps coordinate changes and if location update is more accurate than
     * 60 meters, the nearby stops list is generated using that location.
     * @param location location of the user.
     */
    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        System.out.println("My coordinates: " + location.getLatitude() + " ," + location.getLongitude() + " Accuracy: " + location.getAccuracy());
        //myLocationManager.removeUpdates(this);
        //if data retrieval from foli hasn't been started and location's accuracy is better than 60m
        if (JSONretrievalStarted == false && location.getAccuracy() < 60) {
            //String url = "http://data.foli.fi/gtfs/v0/stops";
            //new ProcessJSON().execute(url);
            JSONretrievalStarted = true;
            handleBusStopData();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            myLocationManager.removeUpdates(this);
        }

    }

    /**
     *  Fetches bus stop data from database and uses it
     *  with GPS location to filter nearby stops. Distance filtered stops
     *  are added to NearbyActivity's list.
     */
    private void handleBusStopData()
    {
        Location stopLocation = new Location("StopLoc");

        // using SortedMap to sort the nearby stops
        SortedMap<Integer, String> tempMap = new TreeMap<Integer, String>();

        SQLiteHelper BsDb = new SQLiteHelper(NearbyActivity.this);
        //fetch data
        Cursor res = BsDb.getData("SELECT * FROM busstops WHERE 1"); //limit 2 for testing
        res.moveToFirst();
        //iterate data
        while (!res.isAfterLast()) {
            String id = res.getString(res.getColumnIndex("bs_id"));
            String name = res.getString(res.getColumnIndex("bs_nm"));
            double lat = res.getDouble(res.getColumnIndex("bs_lat"));
            double lon = res.getDouble(res.getColumnIndex("bs_lon"));
            stopLocation.setLatitude(lat);
            stopLocation.setLongitude(lon);

            float distance = myLocation.distanceTo(stopLocation);

            //using hard coded distance filter of 2000m
            if(distance <= 2000)
            {
                String stopDistance = ""+Math.round(distance);
                tempMap.put(Integer.parseInt(stopDistance), id + " " + name + " " + stopDistance + "m");
            }
            res.moveToNext();
        }

        for(Integer distance : tempMap.keySet())
        {
            String stop = tempMap.get(distance);
            stopList.add(stop);
        }

        spinner.setVisibility(View.GONE);
        waitText.setVisibility(View.GONE);
        String[] values = stopList.toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(NearbyActivity.this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);


        // Assign adapter to ListView
        stopsListView.setAdapter(adapter);

        // ListView Item Click Listener
        stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(getApplicationContext(), ResultActivity.class);

                String numberString = (String) stopsListView.getItemAtPosition(position);
                String arr[] = numberString.split("\\s+");
                i.putExtra("busStopNumber",(String) arr[0]);
                i.putExtra("busStopName",(String) arr[1]);
                startActivity(i);

            }

        });

    } // handleBusStopData method end

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}

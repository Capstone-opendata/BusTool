package capstone2015project.bustool;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class NearbyActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LocationListener {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;   // for permission requesting
    private boolean JSONretrievalStarted = false;   // for stopping multiple data retrievals from foli

    private boolean GPSenabled = false;     // is GPS enabled on user's device

    ListView stopsListView;
    Location myLocation;
    LocationManager myLocationManager;
    ArrayList<String> stopList;
    private ProgressBar spinner;    // this will be displayed while retrieving data

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

        //hard coding myLocation for testing
        myLocation = new Location("MyLoc");
        //myLocation.setLatitude(60.4491652);
        //myLocation.setLongitude(22.2933068);

        // Get ListView object from xml
        stopsListView = (ListView) findViewById(R.id.listView);
        stopList = new ArrayList<String>();
        //location of the stops data
        //String url = "http://data.foli.fi/gtfs/v0/stops";
        //new ProcessJSON().execute(url);
    }

    protected void onStart() {
        super.onStart();

        if(JSONretrievalStarted == false)
        {
            myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            GPSenabled = myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if(GPSenabled == false)
            {
                // GPS is not enabled on user's device

                showMessageOKCancel("You need to enable GPS",
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
                    /*
                    if (ActivityCompat.shouldShowRequestPermissionRationale(NearbyActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showMessageOKCancel("You need to allow access to GPS",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(NearbyActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                REQUEST_CODE_ASK_PERMISSIONS);

                                    }
                                }, null);
                        return;
                    }
                    */
                    ActivityCompat.requestPermissions(NearbyActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ASK_PERMISSIONS);
                    return;
                }
                myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                spinner.setVisibility(View.VISIBLE);
            }// else

        }//if JSONretrievalStarted

    }// onStart

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener,
                                     DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(NearbyActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
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

        } else if (id == R.id.nav_favorites) {
/*
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
*/
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        System.out.println("My coordinates: " + location.getLatitude() + " ," + location.getLongitude() + " Accuracy: " + location.getAccuracy());
        //myLocationManager.removeUpdates(this);
        //if data retrieval from foli hasn't been started and location's accuracy is better than 60m
        if (JSONretrievalStarted == false && location.getAccuracy() < 60) {
            String url = "http://data.foli.fi/gtfs/v0/stops";
            new ProcessJSON().execute(url);
            JSONretrievalStarted = true;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            myLocationManager.removeUpdates(this);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    //this class will handle processing the incoming json data
    private class ProcessJSON extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... strings){
            //String stream = null;
            String urlString = strings[0];

            HTTPDataHandler hh = new HTTPDataHandler();
            String stream = hh.GetHTTPData(urlString);

            // Return the data from specified url
            return stream;
        }

        protected void onPostExecute(String stream){


            if(stream !=null){
                try{

                    JSONObject stopsObject = new JSONObject(stream);
                    // Get the JSONArray stops
                    JSONArray stopsArray = stopsObject.toJSONArray(stopsObject.names());

                    Location stopLocation = new Location("StopLoc");

                    // using SortedMap to sort the nearby stops
                    SortedMap<Integer, String> tempMap = new TreeMap<Integer, String>();

                    for(int i = 0; i<stopsArray.length(); i++)
                    {
                        JSONObject stop = stopsArray.getJSONObject(i);

                        stopLocation.setLatitude(Double.parseDouble(stop.getString("stop_lat")));
                        stopLocation.setLongitude(Double.parseDouble(stop.getString("stop_lon")));
                        float distance = myLocation.distanceTo(stopLocation);

                        //System.out.println("DISTANCE "+ distance);
                        //using hard coded distance filter of 500m
                        if(distance <= 500)
                        {
                            String stopNumber = (String)stopsObject.names().get(i);
                            String stopName = stop.getString("stop_name");
                            String stopDistance = ""+Math.round(distance);
                            //stopList.add(stopNumber + " " + stopName + " " + stopDistance + "m");
                            //System.out.println("STOPLIST SIZE "+ stopList.size());

                            tempMap.put(Integer.parseInt(stopDistance), stopNumber + " " + stopName + " " + stopDistance + "m");
                        }

                    }
                    for(Integer distance : tempMap.keySet())
                    {
                        String stop = tempMap.get(distance);
                        stopList.add(stop);
                    }

                }catch(JSONException e){
                    e.printStackTrace();
                }

                spinner.setVisibility(View.GONE);
                //Collections.sort(stopList);
                //Collections.reverse(stopList);
                String[] values = stopList.toArray(new String[0]);
                //System.out.println("VALUES LENGTH "+values.length);



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
                        String arr[] = numberString.split(" ", 2);

                        i.putExtra("busStopNumber",(String) arr[0]);
                        startActivity(i);

                    }

                });

            } // if statement end
        } // onPostExecute() end
    } // ProcessJSON class end
}

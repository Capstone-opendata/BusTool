package capstone2015project.bustool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.ArrayList;

public class StopsMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        NavigationView.OnNavigationItemSelectedListener{

    private static final Object KEY = "";
    private GoogleMap mMap;
    SQLiteHelper BsDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stops_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        BsDb = new SQLiteHelper(StopsMapActivity.this);
        //fetch data
        Cursor res = BsDb.getData("SELECT * FROM busstops WHERE 1"); //limit 2 for testing
        res.moveToFirst();
        //iterate data
        while (!res.isAfterLast()) {
            String id = res.getString(res.getColumnIndex("bs_id"));
            String name = res.getString(res.getColumnIndex("bs_nm"));
            double lat = res.getDouble(res.getColumnIndex("bs_lat"));
            double lon = res.getDouble(res.getColumnIndex("bs_lon"));
            LatLng stops = new LatLng(lat, lon);
            //String ltt = Double.toString(lat);
            mMap.addMarker(new MarkerOptions().position(stops).title(name).snippet(KEY + id));
            res.moveToNext();
        }

        double lat = 60.4491652;
        double lon = 22.2933068;
        LocationManager myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (myLocationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StopsMapActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        123);
                return;
            }
            Location myLocation = myLocationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (myLocation != null) {
                lat = myLocation.getLatitude();
                lon = myLocation.getLongitude();
                //System.out.println("Lat: "+lat + " Lon: "+lon);
            }

        }

        //should not work this way
        //hard coded lon / lat for zoom and should be changed to current location of user
        //double hlat = 60.4491652;
        //double hlon= 22.2933068;

        LatLng currentLoc = new LatLng(lat,lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));
        // action on click
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String s = marker.getSnippet();
        String name = marker.getTitle();
        Intent i = new Intent(getApplicationContext(), ResultActivity.class);
        if (!s.isEmpty()) {
            i.putExtra("busStopNumber", s);
            i.putExtra("busStopName",name);
            startActivity(i);
        }
        //Toast.makeText(getBaseContext(), "Stop Number: " + s,
        //      Toast.LENGTH_SHORT).show();
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
}
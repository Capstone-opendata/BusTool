package capstone2015project.bustool;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.ArrayList;

public class StopsMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

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
        while(!res.isAfterLast()){
            String id = res.getString(res.getColumnIndex("bs_id"));
            String name = res.getString(res.getColumnIndex("bs_nm"));
            double lat = res.getDouble(res.getColumnIndex("bs_lat"));
            double lon = res.getDouble(res.getColumnIndex("bs_lon"));
            LatLng stops = new LatLng(lat, lon);
            //String ltt = Double.toString(lat);
            mMap.addMarker(new MarkerOptions().position(stops).title(name).snippet(KEY + id));
            res.moveToNext();
        }
        //should not work this way
        //hard coded lon / lat for zoom and should be changed to current location of user
        double hlat = 60.4491652;
        double hlon= 22.2933068;
        LatLng currentLoc = new LatLng(hlat,hlon);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 13));
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
}
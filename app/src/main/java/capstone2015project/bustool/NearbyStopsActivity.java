package capstone2015project.bustool;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NearbyStopsActivity extends AppCompatActivity {

    ListView stopsListView ;
    Location myLocation;
    ArrayList<String> stopList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_stops);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //hard coding myLocation for testing
        myLocation = new Location("MyLoc");
        myLocation.setLatitude(60.4491652);
        myLocation.setLongitude(22.2933068);

        // Get ListView object from xml
        stopsListView = (ListView) findViewById(R.id.listView);
        stopList = new ArrayList<String>();
        //location of the stops data
        String url = "http://data.foli.fi/gtfs/v0/stops";
        new ProcessJSON().execute(url);



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
                            stopList.add(stopNumber + " " + stopName + " " + stopDistance + "m");
                            //System.out.println("STOPLIST SIZE "+ stopList.size());
                        }

                    }

                }catch(JSONException e){
                    e.printStackTrace();
                }

                //Collections.sort(stopList);
                //Collections.reverse(stopList);
                String[] values = stopList.toArray(new String[0]);
                //System.out.println("VALUES LENGTH "+values.length);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(NearbyStopsActivity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, values);


                // Assign adapter to ListView
                stopsListView.setAdapter(adapter);

                // ListView Item Click Listener
                stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Intent i = new Intent(getApplicationContext(), BusStopInfoActivity.class);

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

package capstone2015project.bustool;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class BusStopInfoActivity extends AppCompatActivity {

    HttpURLConnection urlConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_info);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bus_stop_info, menu);
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

    public void GetStopData(View view)
    {
        EditText userInput = (EditText) findViewById(R.id.editText);
        String url = "http://data.foli.fi/siri/sm/"+userInput.getText();
        new ProcessJSON().execute(url);
    }


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
            TextView tv = (TextView) findViewById(R.id.textView3);
            tv.setText("Next incoming busses:\n");

            if(stream !=null){
                try{
                    // Get the full HTTP Data as JSONObject
                    JSONObject reader= new JSONObject(stream);

                    // Get the JSONArray busses
                    JSONArray bussesArray = reader.getJSONArray("result");

                    for(int i = 0; i<3; i++)
                    {
                        if(i<bussesArray.length())
                        {
                            // Get the busses array first JSONObject
                            JSONObject busses_object_0 = bussesArray.getJSONObject(i);
                            String busses_0_lineNumber = busses_object_0.getString("lineref");
                            String busses_0_lineDestination = busses_object_0.getString("destinationdisplay");
                            String busses_0_expectedTime = busses_object_0.getString("expectedarrivaltime");

                            long timestamp = System.currentTimeMillis();
                            long eta = Long.parseLong(busses_0_expectedTime)*1000 - timestamp;

                            int seconds = (int) (eta / 1000) % 60 ;
                            int minutes = (int) ((eta / (1000*60)) % 60);

                            String etaString = minutes+"m " +seconds+"s";

                            tv.setText(tv.getText()+"\nLine "+busses_0_lineNumber+" "+busses_0_lineDestination+ " "+etaString);
                        }

                    }



                }catch(JSONException e){
                    e.printStackTrace();
                }

            } // if statement end
        } // onPostExecute() end
    } // ProcessJSON class end

}

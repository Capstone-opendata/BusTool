package capstone2015project.bustool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FirstTimeSetupActivity extends AppCompatActivity {

    SQLiteHelper BsDb;
    Button downloadButton;
    private ProgressBar spinner;    // this will be displayed while retrieving data
    private TextView progressText;      // used to display a progress message along with progress spinner
    public static final String PREFS_NAME = "MyPrefsFile";
    public AsyncTask a=new ProcessJSON();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_setup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        downloadButton = (Button) findViewById(R.id.downloadButton);
        if(a.getStatus()==ProcessJSON.Status.RUNNING){
            downloadButton.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
        }else{
            spinner = (ProgressBar)findViewById(R.id.downloadSpinner);
            spinner.setVisibility(View.GONE);
            progressText = (TextView) findViewById(R.id.downloadProgressText);
            progressText.setVisibility(View.GONE);

    }
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * Function for starting bus stop data download for local database.
     * @param view the current view.
     */
    public void startDownload(View view)
    {
        downloadButton.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);

        fetchDb();
    }

    /**
     *  Starts source data retrieval for local database.
     */
    public void fetchDb()
    {
        String url = "http://data.foli.fi/gtfs/v0/stops";
        a=new ProcessJSON().execute(url);
    }

    private class ProcessJSON extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... strings) {

            String urlString = strings[0];

            HTTPDataHandler hh = new HTTPDataHandler();
            String stream = hh.GetHTTPData(urlString);

            BsDb = new SQLiteHelper(FirstTimeSetupActivity.this);
            if (stream != null) {
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

                        BsDb.insertBS(stopNumber, stopName,lati, longi, "0");

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }



            } // if statement end
            BsDb.close();

            return null;
        }

        protected void onPostExecute(String stream) {

            //download completed, mark first setup done and allow user to proceed.

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstTimeSetupDone", true);

            editor.commit();

            Intent i = new Intent(FirstTimeSetupActivity.this, StopToolSelectionActivity.class);
            startActivity(i);
            finish();
        } // onPostExecute() end
    } // ProcessJSON class end

}

package capstone2015project.bustool;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;

public class ResultActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HttpURLConnection urlConnection;
    Timer timer;
    String busNumber;
    public String busStopNumber;
    public String busStopName;
    SQLiteHelper BsDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
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

        //getting the passed bus number
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            busStopNumber = extras.getString("busStopNumber");
            busStopName = extras.getString("busStopName");
            busNumber = busStopNumber;

            // text field where the searched stop number is displayed
            //TextView stopNumberText = (TextView) findViewById(R.id.stop_number_text);
            //stopNumberText.setText(busStopNumber+": "+busStopName);
            toolbar.setSubtitle(busStopNumber+": "+busStopName);
        }
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
        getMenuInflater().inflate(R.menu.result, menu);
        return true;
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
    protected void onStart()
    {
        super.onStart();

        //start the timed refresh for getting new stop info
        final Handler timerHandler = new Handler();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timerHandler.post(new Runnable() {
                    public void run() {
                        GetStopData();
                    }
                });
            }
        }, 0, 1000 * 60);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        //stopping the timed refresh for bus stop info
        timer.cancel();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_addFavs:
                remember();
                return true;
            case R.id.action_delFavs:
                forget();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void remember(){
        BsDb = new SQLiteHelper(ResultActivity.this);
        Boolean del = BsDb.addFav(busStopNumber);
    }

    public void forget(){
        BsDb = new SQLiteHelper(ResultActivity.this);
        Boolean add = BsDb.delFav(busStopNumber);
    }

    /**
     * Starts SIRI JSON bus stop data retrieval from föli's database
     */
    public void GetStopData()
    {
        String url = "http://data.foli.fi/siri/sm/"+busNumber;
        //EditText userInput = (EditText) findViewById(R.id.editText);
        //String url = "http://data.foli.fi/siri/sm/"+userInput.getText();
        new ProcessJSON().execute(url);
    }

    /**
     * Used to allow refresh button use GetStopData method
     * @param view the current view
     */
    public void ManualRefresh(View view)
    {
        GetStopData();
    }

    /**
     * Used to allow Go To Main Menu button start intent StopToolSelectionActivity
     * @param view the current view
     */
    public void GoToToolSelectionActivity(View view)
    {
        Intent i = new Intent(this, StopToolSelectionActivity.class);
        startActivity(i);
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

            //  table is the 4x3 table which displays incoming bus data
            // increasing the table size requires addition of rows in content_bus_stop_info.xml
            //TableLayout table = (TableLayout)findViewById(R.id.result_table_layout);
            TableLayout scrollableLayout = (TableLayout)findViewById(R.id.ScrollableTableLayout);

            if(stream !=null){
                try{
                    // Get the full HTTP Data as JSONObject
                    JSONObject reader= new JSONObject(stream);
                    //tv.setText("."+reader+".");
                    // Get the JSONArray busses
                    JSONArray bussesArray = reader.getJSONArray("result");

                    // using i<5 means that only 5 next busses will be displayed
                    for(int i = 0; i<20; i++)
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

                            //tv.setText(tv.getText()+"\nLine "+busses_0_lineNumber+" "+busses_0_lineDestination+ " "+etaString);

                            // using row plus 1 means that we dont mess with the title row
                            //TableRow row = (TableRow)table.getChildAt(i+1);
                            TableRow row = (TableRow) LayoutInflater.from(ResultActivity.this).inflate(R.layout.result_row, null);
                            scrollableLayout.addView(row);
                            TextView tvLine = (TextView)row.getChildAt(0);  //the first column of this row
                            TextView tvDest = (TextView)row.getChildAt(1);  //the second
                            TextView tvEta = (TextView)row.getChildAt(2);   //the third

                            // setting the text data in the table cells
                            tvLine.setText(busses_0_lineNumber);
			                //applying styles on first column
                            tvLine.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
                            tvLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            tvLine.setTextColor(Color.parseColor("#FFFFFF"));
                            tvLine.setBackgroundResource(R.drawable.ic_bus_bg);
                            /*counting bus number length to fit in the bg
                            int len = busses_0_lineNumber.length();
                            if(len==3)
                              tvLine.setPadding(15, 20, 0, 0);
                            else
                               tvLine.setPadding(24, 20, 0, 0);
                              */
                            tvLine.setHeight(85);
                            tvLine.setWidth(85);
                            //breaking destination on first word ... must be a better way to do this
                            String[] strArr = busses_0_lineDestination.split("\\s+",0);
				            tvDest.setText(strArr[0]);
				            tvDest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            //tvDest.setMinWidth(380);
                            tvDest.setPadding(10, 0, 10, 0);
                            
				            tvEta.setText(etaString);
				            tvEta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            //tvEta.setMinWidth(320);

                        }

                    }



                }catch(JSONException e){
                    e.printStackTrace();
                }

            } // if statement end
        } // onPostExecute() end
    } // ProcessJSON class end
}

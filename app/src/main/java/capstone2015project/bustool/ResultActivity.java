package capstone2015project.bustool;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Displays information about incoming buses to a certain bus stop.
 */
public class ResultActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AdapterView.OnItemSelectedListener {

    HttpURLConnection urlConnection;
    Timer timer;    // used for automatic result refreshing
    String busNumber;   // used for storing bus stop number
    public String busStopNumber;    //the number of the bus stop eg 449
    public String busStopName;      //the name of the bus stop eg Korvalankatu
    int favorites;
    SQLiteHelper BsDb;
    private Spinner filterSpinner;  //used for selecting which bus lines should be filtered
    private String lastSpinnerSelection;    //used to store filter selection between updates

    /**
     * Initializes the activity.
     * @param savedInstanceState saved data of previous state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final ImageButton fav = (ImageButton) findViewById(R.id.imageButton);
        fav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(favorites==1){
                    forget();
                    fav.setImageDrawable(ContextCompat.getDrawable(ResultActivity.this, R.mipmap.ic_unfavorite_star));
                    favorites=0;
                    Snackbar.make(v, R.string.resact_sb_remove, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else {
                    remember();
                    fav.setImageDrawable(ContextCompat.getDrawable(ResultActivity.this, R.mipmap.ic_favorite_star));
                    favorites=1;
                    Snackbar.make(v, R.string.resact_sb_add, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //giving filter spinner select listener
        filterSpinner = (Spinner) findViewById(R.id.filter_spinner);
        filterSpinner.setOnItemSelectedListener(this);

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

    /**
     * Adds items dynamically to filter spinner.
     * @param busLineList is list of bus line numbers in String format
     */
    private void addItemsOnSpinner(List<String> busLineList) {

        //filterSpinner = (Spinner) findViewById(R.id.filter_spinner);
        List<String> list = new ArrayList<String>();
        list.add(String.valueOf(getResources().getText(R.string.filterSpinnerAll)));
        for(int i = 0; i < busLineList.size(); i++)
        {
            list.add(busLineList.get(i));
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(dataAdapter);
    }

    /**
     * Filters result table rows by given bus line. Filtered bus lines are
     * hidden. If given bus line is null then all result table rows are shown.
     * @param busLine is the bus line number which will be shown in results
     */
    private void filterResultTable(String busLine)
    {

        TableLayout scrollableLayout = (TableLayout)findViewById(R.id.ScrollableTableLayout);
        //if busLine isn't null show only wanted bus line else show all
        if(busLine != null)
        {
            CharSequence line = busLine;
            for(int i = 0; i < scrollableLayout.getChildCount(); i++)
            {
                TableRow row = (TableRow) scrollableLayout.getChildAt(i);
                if(((TextView) row.getChildAt(0)).getText().equals(line))
                {
                    row.setVisibility(View.VISIBLE);
                }
                else
                {
                    row.setVisibility(View.GONE);
                }
            }
        }
        else
        {
            for(int i = 0; i < scrollableLayout.getChildCount(); i++)
            {
                TableRow row = (TableRow) scrollableLayout.getChildAt(i);
                row.setVisibility(View.VISIBLE);
            }
        }
        //setting table to visible because it was set invisible in data retrieval
        scrollableLayout.setVisibility(View.VISIBLE);

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

    /**
     * Starts timed refresh for new bus stop info.
     */
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

    /**
     * Stops timed refresh for new bust stop info.
     */
    @Override
    protected void onStop()
    {
        super.onStop();

        //stopping the timed refresh for bus stop info
        timer.cancel();
    }

    /**
     * Handles action bar's favorite buttons.
     * @param item the pressed item.
     * @return boolean true.
     */
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

    /**
     * Instantiates favorite button accordingly.
     */
    @Override
    protected  void onResume(){
        super.onResume();
        SQLiteHelper BsDb = new SQLiteHelper(ResultActivity.this);
        final Cursor res = BsDb.getData("SELECT * FROM busstops WHERE bs_id='"+busStopNumber+"' ");
        res.moveToFirst();
        ImageButton fav = (ImageButton) findViewById(R.id.imageButton);
        favorites=res.getInt(res.getColumnIndex("bs_fav"));
        if(favorites==1) {;
            fav.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_favorite_star));
        }else{
            fav.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_unfavorite_star));
        }
    }

    /**
     * Adds busStopNumber to favorite database.
     */
    public void remember(){
        BsDb = new SQLiteHelper(ResultActivity.this);
        Boolean del = BsDb.addFav(busStopNumber);
    }

    /**
     * Removes busStopNumber from favorite database.
     */
    public void forget(){
        BsDb = new SQLiteHelper(ResultActivity.this);
        Boolean add = BsDb.delFav(busStopNumber);
    }

    /**
     * Starts SIRI JSON bus stop data retrieval from föli's database
     * and clears the previous data from scrollable tablelayout
     */
    public void GetStopData()
    {
        TableLayout scrollableLayout = (TableLayout)findViewById(R.id.ScrollableTableLayout);
        scrollableLayout.removeAllViews();
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
     * Allows buttons to start intent StopToolSelectionActivity
     * @param view the current view
     */
    public void GoToToolSelectionActivity(View view)
    {
        Intent i = new Intent(this, StopToolSelectionActivity.class);
        startActivity(i);
    }

    /**
     * Filters result page's result table by chosen item in the spinner.
     * @param parent the spinner used for selecting a bus line.
     * @param view the current view.
     * @param position the position in the spinner.
     * @param id the id.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        // if spinner value isn't "All" then filter by given item else remove filter
        if((String) parent.getItemAtPosition(position) != String.valueOf(getResources().getText(R.string.filterSpinnerAll)))
        {
            filterResultTable((String) parent.getItemAtPosition(position));
            lastSpinnerSelection = (String) parent.getItemAtPosition(position);
        }
        else
        {
            filterResultTable(null);
            lastSpinnerSelection = (String) parent.getItemAtPosition(position);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    /**
     * Retrieves JSON information and processes it accordingly for the result activity
     */
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
            //setting table invisible to avoid showing filtering during updates. Set to visible after filtering.
            scrollableLayout.setVisibility(View.GONE);
            if(stream !=null){
                try{
                    // Get the full HTTP Data as JSONObject
                    JSONObject reader= new JSONObject(stream);
                    //tv.setText("."+reader+".");
                    // Get the JSONArray busses
                    JSONArray bussesArray = reader.getJSONArray("result");
                    //store temporarily all line numbers in this list
                    List<String> allLinesList = new ArrayList<String>();

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

                            //if the eta is negative, skip adding it
                            if(eta < 0)
                            {
                                continue;
                            }
                            //adding this bus in the temp list for results filtering
                            allLinesList.add(busses_0_lineNumber);

                            int seconds = (int) (eta / 1000) % 60 ;
                            int minutes = (int) ((eta / (1000*60)) % 60);
                            int hours = (int) (eta / (1000 * 60 * 60)) % 24;

                            String etaString;

                            if (hours < 1)
                            {
                                etaString = minutes+"min " +seconds+"s";
                            }
                            else
                            {
                                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                                etaString = formatter.format(new Date(Long.parseLong(busses_0_expectedTime)*1000));
                            }

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
                            //breaking destination on first word ... must be a better way to do this
                            String[] strArr = busses_0_lineDestination.split("\\s+",0);
				            tvDest.setText(strArr[0]);
				            tvDest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				            tvEta.setText(etaString);
				            tvEta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        }

                    }

                    if(allLinesList != null)
                    {
                        List<String> newList = new ArrayList<String>(new HashSet<String>(allLinesList));
                        Collections.sort(newList);
                        addItemsOnSpinner(newList);
                        if(lastSpinnerSelection != null)
                        {
                            for (int i = 0; i < filterSpinner.getCount(); i++) {
                                if (filterSpinner.getItemAtPosition(i).equals(lastSpinnerSelection)) {
                                    filterSpinner.setSelection(i);
                                }
                            }
                        }
                    }


                }catch(JSONException e){
                    e.printStackTrace();
                }

            } // if statement end
        } // onPostExecute() end
    } // ProcessJSON class end
}
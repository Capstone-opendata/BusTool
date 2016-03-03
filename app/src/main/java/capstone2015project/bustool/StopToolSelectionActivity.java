package capstone2015project.bustool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ContentHandler;
import java.util.ArrayList;

/**
 * Acts as the main menu for different ways to search a bus stop.
 */
public class StopToolSelectionActivity extends AppCompatActivity {
    SQLiteHelper BsDb;
    ListView listViewX;
    boolean textChanged = true;
    private PopupWindow popup;
    private Button close_popup_button;
    private AlertDialog dialog;
    final static String url = "http://data.foli.fi/gtfs/v0/stops";
    private GoogleApiClient client;

    /**
     * Initializes the activity.
     *
     * @param savedInstanceState saved data of previous state.
     *
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_tool_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

/*        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
           fab.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                           .setAction("Action", null).show();
               }
          });*/

        final Button DbButton = (Button) findViewById(R.id.button5); // Database Button
        final Button NbButton = (Button) findViewById(R.id.button3); // Nearby Button
        final Button MpButton = (Button) findViewById(R.id.button4); // Map Button
        final EditText userInput = (EditText) findViewById(R.id.editText_busID);
        final TextView title = (TextView) findViewById(R.id.textView2);
        final TextInputLayout inputLayout = (TextInputLayout) findViewById(R.id.input_layout_busID);
        final ViewFlipper flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        final Thread dlthread = new Thread(new Runnable() {
            @Override
            public void run() {
                fetchDb();
            }
        });

        BsDb = new SQLiteHelper(StopToolSelectionActivity.this);

        if (BsDb.numberOfRows() == 0) {
            DbButton.setVisibility(View.INVISIBLE);
            NbButton.setVisibility(View.INVISIBLE);
            MpButton.setVisibility(View.INVISIBLE);
            userInput.setVisibility(View.INVISIBLE);
            title.setVisibility(View.INVISIBLE);
            inputLayout.setVisibility(View.INVISIBLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            dlthread.run();


        } else {
            flipper.setVisibility(View.INVISIBLE);
        }
        userInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (userInput.getText().toString().isEmpty()) {
                    ArrayList array_list = new ArrayList();
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(StopToolSelectionActivity.this, android.R.layout.simple_list_item_1, array_list);
                    listViewX = (ListView) findViewById(R.id.listViewX);
                    listViewX.setAdapter(arrayAdapter);

                } else {
                    String query = "SELECT * FROM busstops WHERE bs_id LIKE '%" + userInput.getText().toString() + "%' OR bs_nm LIKE '" + userInput.getText().toString() + "%' ";
                    ArrayList array_list = BsDb.getQuery(query);
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(StopToolSelectionActivity.this, android.R.layout.simple_list_item_1, array_list);
                    listViewX = (ListView) findViewById(R.id.listViewX);
                    listViewX.setAdapter(arrayAdapter);
                    listViewX.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            Intent i = new Intent(getApplicationContext(), ResultActivity.class);
                            i.putExtra("busStopNumber", BsDb.BsIdList.get(arg2));
                            Cursor res = BsDb.getData("SELECT bs_nm FROM busstops WHERE bs_id='" + BsDb.BsIdList.get(arg2) + "'");
                            res.moveToFirst();
                            //check
                            String name = res.getString(res.getColumnIndex("bs_nm"));
                            i.putExtra("busStopName", name);
                            startActivity(i);
                        }
                    });
                }
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
                        i.putExtra("busStopNumber", BsDb.BsIdList.get(0));
                        Cursor res = BsDb.getData("SELECT bs_nm FROM busstops WHERE bs_id='" + BsDb.BsIdList.get(0) + "'");
                        res.moveToFirst();
                        //check
                        String name = res.getString(res.getColumnIndex("bs_nm"));
                        i.putExtra("busStopName", name);
                        startActivity(i);
                        userInput.setText("");
                    }
                    return true;
                }
                if (keyevent.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_DEL) && !textChanged) {
                    if (userInput.getText().toString().isEmpty()) {
                        userInput.clearFocus();
                        showFavorites();
                    }
                    return true;
                }
                if (keyevent.getAction() == KeyEvent.ACTION_UP) {
                    textChanged = false;
                }
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_BACK)) {
                    userInput.clearFocus();
                    userInput.setText("");
                    showFavorites();
                    return true;
                }
                return false;

            }
        });
        userInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    p.addRule(RelativeLayout.BELOW, R.id.input_layout_busID);
                    listViewX.setLayoutParams(p);
                    DbButton.setVisibility(View.INVISIBLE);
                    NbButton.setVisibility(View.INVISIBLE);
                    MpButton.setVisibility(View.INVISIBLE);

                } else {
                    DbButton.setVisibility(View.VISIBLE);
                    NbButton.setVisibility(View.VISIBLE);
                    MpButton.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    p.addRule(RelativeLayout.BELOW, R.id.button5);
                    listViewX.setLayoutParams(p);
                    showFavorites();
                }
            }

        });

        BsDb.close();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * Clears userInput text field and calls favorites to be displayed.
     */
    @Override
    protected void onResume() {
        final EditText userInput = (EditText) findViewById(R.id.editText_busID);
        userInput.setText("");
        userInput.clearFocus();
        super.onResume();
        showFavorites();
    }

    /**
     * Clears userInput text field and calls favorites to be displayed.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        final EditText userInput = (EditText) findViewById(R.id.editText_busID);
        userInput.clearFocus();
        userInput.setText("");
        showFavorites();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tool_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            showInfoDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates an info/credit dialog popup.
     */
    private void showInfoDialog() {
        dialog = new AlertDialog.Builder(StopToolSelectionActivity.this)
                .setTitle(StopToolSelectionActivity.this.getResources().getString(R.string.app_name))
                .setMessage(StopToolSelectionActivity.this.getResources().getString(R.string.info_text))
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * changes the active activity to NearbyStopsActivity
     */

    public void GoToNearbyStopsActivity(View view) {
        Intent i = new Intent(StopToolSelectionActivity.this, NearbyActivity.class);
        startActivity(i);
    }

    public void GoToStopsMapActivity(View view) {
        BsDb = new SQLiteHelper(StopToolSelectionActivity.this);
        if (BsDb.numberOfRows() == 0) {
            finish();
            startActivity(getIntent());
        } else {
            Intent i = new Intent(StopToolSelectionActivity.this, StopsMapActivity.class);
            startActivity(i);
        }
        BsDb.close();
    }

    public void GoToDbActivity(View view) {
        BsDb = new SQLiteHelper(StopToolSelectionActivity.this);
        if (BsDb.numberOfRows() == 0) {
            finish();
            startActivity(getIntent());
        } else {
            Intent i = new Intent(StopToolSelectionActivity.this, BusstopDbActivity.class);
            startActivity(i);
        }
        BsDb.close();
    }

    /**
     * Starts source data retrieval for local database.
     */
    public void fetchDb() {
        AsyncTask a=new ProcessJSON(StopToolSelectionActivity.this).execute(url);
        //while(a.getStatus()== AsyncTask.Status.RUNNING){};
        //JSON(x.data);
    }

    /**
     * Creates a list of favorite bus stops from database and displays them.
     */
    public void showFavorites() {
        ArrayList array_list = BsDb.getQuery("SELECT * FROM busstops WHERE bs_fav = 1");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(StopToolSelectionActivity.this, android.R.layout.simple_list_item_1, array_list);
        listViewX = (ListView) findViewById(R.id.listViewX);
        listViewX.setAdapter(arrayAdapter);
        listViewX.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent i = new Intent(getApplicationContext(), ResultActivity.class);
                String numberString = BsDb.BsIdList.get(arg2);

                i.putExtra("busStopNumber", numberString);
                Cursor res = BsDb.getData("SELECT bs_nm FROM busstops WHERE bs_id='" + numberString + "'");
                res.moveToFirst();
                //check
                String name = res.getString(res.getColumnIndex("bs_nm"));
                i.putExtra("busStopName", name);
                startActivity(i);

            }
        });
    }

    public void JSON(String stream) {
        BsDb = new SQLiteHelper(StopToolSelectionActivity.this);
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

                    BsDb.insertBS(stopNumber, stopName, lati, longi, "0");

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } // if statement end
        BsDb.close();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "StopToolSelection Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://capstone2015project.bustool/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "StopToolSelection Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://capstone2015project.bustool/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class ProcessJSON extends AsyncTask<String,Void,String>{
        public String data;
        SQLiteHelper BsDb;
        private Context mContext;
        public ProcessJSON (Context context){
            mContext = context;
        }

        protected String doInBackground(String... strings) {
            //String stream = null;
            String urlString = strings[0];

            HTTPDataHandler hh = new HTTPDataHandler();
            String stream = hh.GetHTTPData(urlString);

            // Return the data from specified url
            BsDb = new SQLiteHelper(mContext);
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

                        BsDb.insertBS(stopNumber, stopName, lati, longi, "0");

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } // if statement end
            BsDb.close();
            return stream;
        }

        protected void onPostExecute(String stream) {
            finish();
            startActivity(getIntent());
        } // onPostExecute() end
    } // ProcessJSON class end
}


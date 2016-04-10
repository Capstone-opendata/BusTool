package capstone2015project.buscatchers;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Acts as the main menu for different ways to search a bus stop.
 */
public class StopToolSelectionActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    SQLiteHelper BsDb;
    ListView listViewX;
    boolean textChanged = true;
    private PopupWindow popup;
    private Button close_popup_button;
    private AlertDialog dialog;

    /**
     * Initializes the activity.
     *
     * @param savedInstanceState saved data of previous state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_tool_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final Button DbButton = (Button) findViewById(R.id.button5); // Database Button
        final Button NbButton = (Button) findViewById(R.id.button3); // Nearby Button
        final Button MpButton = (Button) findViewById(R.id.button4); // Map Button

        final EditText userInput = (EditText) findViewById(R.id.editText_busID);
        BsDb = SQLiteHelper.getInstance(StopToolSelectionActivity.this);
        boolean firstTimeSetup = settings.getBoolean("firstTimeSetupDone", false);
        if (!firstTimeSetup || (BsDb.numberOfRows() == 0)) {
            settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstTimeSetupDone", false);

            editor.commit();
            Intent i = new Intent(StopToolSelectionActivity.this, FirstTimeSetupActivity.class);
            startActivity(i);
            finish();
        }
        if (BsDb.numberOfRows() == 0) {
            DbButton.setText(R.string.setText_db);
            userInput.setEnabled(false);
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
                    //should have used prepared statements for sqlite ...
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
                        i.putExtra("busStopNumber", BsDb.BsIdList.get(0).toString());
                        Cursor res = BsDb.getData("SELECT bs_nm FROM busstops WHERE bs_id='" + BsDb.BsIdList.get(0).toString() + "'");
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
        BsDb = SQLiteHelper.getInstance(StopToolSelectionActivity.this);
        if (BsDb.numberOfRows() == 0) {
            BsDb.DeleteAll();
            fetchDb();
        } else {
            Intent i = new Intent(StopToolSelectionActivity.this, StopsMapActivity.class);
            startActivity(i);
        }
        BsDb.close();
    }

    public void GoToDbActivity(View view) {
        BsDb = SQLiteHelper.getInstance(StopToolSelectionActivity.this);
        if (BsDb.numberOfRows() == 0) {
            BsDb.DeleteAll();
            fetchDb();
        } else {
            Intent i = new Intent(StopToolSelectionActivity.this, FavoritesActivity.class);
            startActivity(i);
        }
        BsDb.close();
    }

    /**
     * Starts source data retrieval for local database.
     */
    public void fetchDb() {
        final EditText userInput = (EditText) findViewById(R.id.editText_busID);
        final Button NbButton = (Button) findViewById(R.id.button3);
        NbButton.setEnabled(false);
        final Button MpButton = (Button) findViewById(R.id.button4);
        MpButton.setEnabled(false);
        final Button DbButton = (Button) findViewById(R.id.button5);
        DbButton.setEnabled(false);
        userInput.setEnabled(false);
        userInput.setText(R.string.string_dl_wait);
        //String url = "http://data.foli.fi/gtfs/v0/stops";
        String url = AppConfig.FOLI_STOPS_URL;
        new ProcessJSON().execute(url);
    }

    /**
     * Creates a list of favorite bus stops from database and displays them.
     */
    public void showFavorites() {
        ArrayList array_list = BsDb.getFavoriteStops(true);
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

    private class ProcessJSON extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... strings) {
            //String stream = null;
            String urlString = strings[0];

            HTTPDataHandler hh = new HTTPDataHandler();
            String stream = hh.GetHTTPData(urlString);

            // Return the data from specified url
            return stream;
        }

        protected void onPostExecute(String stream) {

            BsDb = SQLiteHelper.getInstance(StopToolSelectionActivity.this);
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
                final EditText userInput = (EditText) findViewById(R.id.editText_busID);
                final Button NbButton = (Button) findViewById(R.id.button3);
                NbButton.setEnabled(true);
                final Button MpButton = (Button) findViewById(R.id.button4);
                MpButton.setEnabled(true);
                final Button DbButton = (Button) findViewById(R.id.button5);
                DbButton.setEnabled(true);
                DbButton.setText(R.string.DbButton_text);
                userInput.setEnabled(true);
                userInput.setText("");
            } // if statement end
            BsDb.close();
        } // onPostExecute() end
    } // ProcessJSON class end
}
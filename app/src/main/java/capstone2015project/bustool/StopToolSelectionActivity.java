package capstone2015project.bustool;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StopToolSelectionActivity extends AppCompatActivity {
    SQLiteHelper BsDb;
    ListView listViewX;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_tool_selection);
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

        final EditText userInput = (EditText) findViewById(R.id.editText_busID);
        BsDb = new SQLiteHelper(StopToolSelectionActivity.this);
        if(BsDb.numberOfRows()==0) {
            userInput.setEnabled(false);
            userInput.setText("Downloading... please wait");
            String url = "http://data.foli.fi/gtfs/v0/stops";
            new ProcessJSON().execute(url);
        }else {
            showFavorites();
        }
        userInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //...
                    // Perform your action on key press here
                    // ...
                    Intent i = new Intent(getApplicationContext(), ResultActivity.class);
                    if(!BsDb.BsIdList.isEmpty()){
                        // pick first one on the list
                        i.putExtra("busStopNumber", BsDb.BsIdList.get(0).toString());
                        startActivity(i);
                        userInput.setText("");
                    }
                    return true;
                }
                if ((keyevent.getAction() == KeyEvent.ACTION_UP)) {
                    //...
                    // Perform your action on key press here
                    // ...
                    if(userInput.getText().toString().isEmpty()){
                        userInput.clearFocus();
                        showFavorites();
                    }else {
                        BsDb = new SQLiteHelper(StopToolSelectionActivity.this);
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
                                startActivity(i);
                            }
                        });
                    }
                    return true;
                }
                return false;
            }
        });
        userInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    final Button button5 = (Button) findViewById(R.id.button5);
                    final Button button3 = (Button) findViewById(R.id.button3);
                    final Button button4 = (Button) findViewById(R.id.button4);
                    button5.setVisibility(View.INVISIBLE);
                    button3.setVisibility(View.INVISIBLE);
                    button4.setVisibility(View.INVISIBLE);
                    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    p.addRule(RelativeLayout.BELOW, R.id.input_layout_busID);
                    listViewX.setLayoutParams(p);

                } else {
                    final Button button5 = (Button) findViewById(R.id.button5);
                    final Button button3 = (Button) findViewById(R.id.button3);
                    final Button button4 = (Button) findViewById(R.id.button4);
                    button5.setVisibility(View.VISIBLE);
                    button3.setVisibility(View.VISIBLE);
                    button4.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    p.addRule(RelativeLayout.BELOW, R.id.button5);
                    listViewX.setLayoutParams(p);
                    showFavorites();
                }
            }

        });
    }
    @Override
    protected void onResume(){
        final EditText userInput = (EditText) findViewById(R.id.editText_busID);
        userInput.setText("");
        userInput.clearFocus();
        super.onResume();
        showFavorites();
    }

    //changes the active activity to NearbyStopsActivity
    public void GoToNearbyStopsActivity(View view)
    {
        Intent i = new Intent(StopToolSelectionActivity.this, NearbyActivity.class);
        startActivity(i);
    }

    public void GoToDbActivity(View view)
    {
        Intent i = new Intent(StopToolSelectionActivity.this, BusstopDbActivity.class);
        startActivity(i);
    }

    public void showFavorites(){
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


            if (stream != null) {
                final EditText userInput = (EditText) findViewById(R.id.editText_busID);
                try {

                    JSONObject stopsObject = new JSONObject(stream);
                    // Get the JSONArray stops
                    JSONArray stopsArray = stopsObject.toJSONArray(stopsObject.names());


                    for (int i = 0; i < stopsArray.length(); i++) {
                        JSONObject stop = stopsArray.getJSONObject(i);

                        String stopNumber = (String) stopsObject.names().get(i);
                        String stopName = stop.getString("stop_name");
                        BsDb.insertBS(stopNumber, stopName);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                userInput.setEnabled(true);
                userInput.setText("");
            } // if statement end
        } // onPostExecute() end
    } // ProcessJSON class end
}

package capstone2015project.bustool;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class StopToolSelectionActivity extends AppCompatActivity {

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
        userInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //...
                    // Perform your action on key press here
                    // ...
                    Intent i = new Intent(getApplicationContext(), ResultActivity.class);
                    String numberString = userInput.getText().toString();

                    i.putExtra("busStopNumber", numberString);
                    startActivity(i);
                    return true;
                }
                return false;
            }
        });
    }

    //changes the active activity to NearbyStopsActivity
    public void GoToNearbyStopsActivity(View view)
    {
        Intent i = new Intent(StopToolSelectionActivity.this, NearbyActivity.class);
        startActivity(i);
    }

}

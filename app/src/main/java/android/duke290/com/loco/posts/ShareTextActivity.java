package android.duke290.com.loco.posts;

import android.content.Intent;
import android.duke290.com.loco.R;
import android.duke290.com.loco.database.Creation;
import android.duke290.com.loco.database.DatabaseAction;
import android.duke290.com.loco.discover.MainActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Allows user to type out and share a post.
 */
public class ShareTextActivity extends AppCompatActivity {

    private EditText mEditText;

    final String LATITUDE = "latitude";
    final String LONGITUDE = "longitude";
    final String ADDRESS = "address";

    final String type = "text";

    private double latitude;
    private double longitude;
    private String address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_text);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        // back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mEditText = (EditText) findViewById(R.id.edittext);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra(LATITUDE, 36.0014);
        longitude = intent.getDoubleExtra(LONGITUDE, -78.9382);
        address = intent.getStringExtra(ADDRESS);

    }

    /**
     * Creates a creation containing the user's message, puts it in Firebase Database, and
     * redirects the user back to MainActivity.
     * @param view - button that activates this method
     */
    protected void onShareClick(View view) {
        String message = mEditText.getText().toString();
        String timestamp = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US).format(new Date());
        Creation creation = new Creation(latitude, longitude, address, type, message, "", 0,timestamp);
        DatabaseAction.putCreationInFirebaseDatabase(creation, latitude, longitude);
        Toast.makeText(getApplicationContext(), "Shared successfully!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(ShareTextActivity.this, MainActivity.class));
    }
}

package android.duke290.com.loco.photos;

import android.content.Intent;
import android.duke290.com.loco.R;
import android.duke290.com.loco.database.Creation;
import android.duke290.com.loco.database.DatabaseFetch;
import android.duke290.com.loco.database.DatabaseFetchCallback;
import android.duke290.com.loco.database.SharedLists;
import android.duke290.com.loco.database.User;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Activity for showing photos shared
 */

public class PhotosActivity extends AppCompatActivity implements DatabaseFetchCallback {
    public ImageAdapter mImage_adp;
    private GridView gridview;
    private TextView titletext;
    private String fetchtype;
    private String FETCHTYPE = "fetchtype";
    private String INDIVIDUAL = "individual";
    private String SHARED = "shared";
    private ArrayList<Creation> imagecreations;
    private ArrayList<String> image_paths;
    private ArrayList<String> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        //Setting the toolbar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.photos_toolbar);
        myToolbar.setTitle("Photos");
        setSupportActionBar(myToolbar);

        // back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        gridview = (GridView) findViewById(R.id.grid_view);
//        titletext = (TextView) findViewById(R.id.photos_title);
        image_paths = new ArrayList<>();
        locations = new ArrayList<>();

        Intent intent = getIntent();
        fetchtype = intent.getStringExtra(FETCHTYPE);
        if(fetchtype.equals(SHARED)){
//            titletext.setText(getString(R.string.photostitle));
            imagecreations = new ArrayList<>();
            imagecreations = SharedLists.getInstance().getImageCreations();
            setImages(imagecreations);
        }
        if(fetchtype.equals(INDIVIDUAL)){
//            titletext.setText(getString(R.string.photostitle_ind));
            DatabaseFetch databasefetch = new DatabaseFetch(this);
            databasefetch.fetchByUser();
        }

    }

    /**
     * Sets the back button in the toolbar
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Fetches images from Databse to put in the Photos Activity
     * @param creations: arraylist of data stored in database
     */

    @Override
    public void onDatabaseResultReceived(ArrayList<Creation> creations) {
        imagecreations = new ArrayList<Creation>();
        for (Creation c : creations) {
            if (c.type.equals("image")) {
                imagecreations.add(c);
            }
        }
        setImages(imagecreations);
    }

    @Override
    public void onUserReceived(User user) {

    }

    /**
     * Sets adapter and click listeners for images in the gridview
     * @param imagecreations: arraylist of data in the database to retrieve image paths
     */
    private void setImages(final ArrayList<Creation> imagecreations){
        mImage_adp = new ImageAdapter(this, imagecreations);
        gridview.setAdapter(mImage_adp);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // Show full-size image
                Creation storageplace = (Creation) mImage_adp.getItem(position); // replace with full-size image indatabase
                String path = storageplace.extra_storage_path;
                Intent fullsize = new Intent(PhotosActivity.this, PhotoFullSizeActivity.class);
                fullsize.putExtra("path", path);
                fullsize.putExtra("position", position);
                for (Creation c:imagecreations){
                    image_paths.add(c.extra_storage_path);
                    locations.add(c.address.replace("\r\n", ", ").replace("\n", ", "));
                }
                fullsize.putStringArrayListExtra("imagepaths", image_paths);
                fullsize.putStringArrayListExtra("locations", locations);
                fullsize.putExtra("fetchtype", fetchtype);
                startActivity(fullsize);
            }
        });
    }

}

package android.duke290.com.loco.photos;

import android.content.Intent;
import android.duke290.com.loco.Creation;
import android.duke290.com.loco.R;
import android.duke290.com.loco.SharedLists;
import android.duke290.com.loco.User;
import android.duke290.com.loco.database.DatabaseFetch;
import android.duke290.com.loco.database.DatabaseFetchCallback;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        //Setting the toolbar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.photos_toolbar);
        myToolbar.setTitle("Photos");
        setSupportActionBar(myToolbar);
        gridview = (GridView) findViewById(R.id.grid_view);
        titletext = (TextView) findViewById(R.id.photos_title) ;
        image_paths = new ArrayList<>();

        Intent intent = getIntent();
        fetchtype = intent.getStringExtra(FETCHTYPE);
        if(fetchtype.equals(SHARED)){
            titletext.setText(getString(R.string.photostitle));
            imagecreations = new ArrayList<>();
            imagecreations = SharedLists.getInstance().getImageCreations();
            setImages(imagecreations);
        }
        if(fetchtype.equals(INDIVIDUAL)){
            titletext.setText(getString(R.string.photostitle_ind));
            DatabaseFetch databasefetch = new DatabaseFetch(this);
            databasefetch.fetchByUser();
        }

    }

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

                }
                fullsize.putStringArrayListExtra("imagepaths", image_paths);
                startActivity(fullsize);
            }
        });
    }

}

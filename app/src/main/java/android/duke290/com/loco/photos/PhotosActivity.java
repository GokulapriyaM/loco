package android.duke290.com.loco.photos;

import android.content.Intent;
import android.duke290.com.loco.Creation;
import android.duke290.com.loco.R;
import android.duke290.com.loco.SharedLists;
import android.duke290.com.loco.photos.ImageAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

public class PhotosActivity extends AppCompatActivity {
    public ImageAdapter mImage_adp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        //Setting the toolbar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.photos_toolbar);
        myToolbar.setTitle("Photos");
        setSupportActionBar(myToolbar);

        GridView gridview = (GridView) findViewById(R.id.grid_view);
        mImage_adp = new ImageAdapter(this, SharedLists.getInstance().getImageCreations());
        gridview.setAdapter(mImage_adp);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // Show full-size image
                Creation storageplace = (Creation) mImage_adp.getItem(position); // replace with full-size image indatabase
                String path = storageplace.extra_storage_path;
                Intent fullsize = new Intent(PhotosActivity.this, PhotoFullSizeActivity.class);
                fullsize.putExtra("path", path);
                startActivity(fullsize);
            }
        });
    }
}

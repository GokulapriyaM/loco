package android.duke290.com.loco.photos;

import android.duke290.com.loco.R;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class PhotoFullSizeActivity extends AppCompatActivity {
    ImageView mImage;
    private FirebaseStorage mStorage;
    public ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_slide);
        String path = getIntent().getStringExtra("path");
        int pos = getIntent().getIntExtra("position",0);
        mStorage = FirebaseStorage.getInstance();

        // Getting all images
        ArrayList<String> image_paths = getIntent().getStringArrayListExtra("imagepaths");

        // Get Pager from xml
        mPager = (ViewPager) findViewById(R.id.pager);

        // Set Adapter for GridView
        mPager.setAdapter(new FullScreenImageAdapter(this, image_paths));
        mPager.setCurrentItem(pos);


    }

}

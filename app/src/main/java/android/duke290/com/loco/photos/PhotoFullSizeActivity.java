package android.duke290.com.loco.photos;

import android.duke290.com.loco.R;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;

public class PhotoFullSizeActivity extends AppCompatActivity {
    ImageView mImage;
    private FirebaseStorage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_full_size);
        String path = getIntent().getStringExtra("path");
        mStorage = FirebaseStorage.getInstance();

        mImage = (ImageView) findViewById(R.id.imagefull);
        mImage.setAdjustViewBounds(true);

        Glide.with(getApplicationContext())
                .using(new FirebaseImageLoader())
                .load(mStorage.getReference().child(path))
                .fitCenter()
                .into(mImage);
    }

}

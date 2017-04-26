package android.duke290.com.loco.photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.duke290.com.loco.R;
import android.util.Log;
import android.widget.ImageView;

public class PhotoFullSizeActivity extends AppCompatActivity {
    ImageView mImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_full_size);

        String path = getIntent().getExtras().getString("path");

        mImage = (ImageView) findViewById(R.id.imagefull);
        mImage.setAdjustViewBounds(true);
        Bitmap fullsizeIm = BitmapFactory.decodeFile(path);
        // Testing in imageview
        System.out.println("path check: " +path);
        System.out.println(fullsizeIm);
        mImage.setImageBitmap(fullsizeIm);
    }
}

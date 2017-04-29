package android.duke290.com.loco.photos;

import android.content.Context;
import android.duke290.com.loco.Creation;
import android.duke290.com.loco.R;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Creation> mImageCreations;
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private final String TAG = "ImageAdapter";

    public ImageAdapter (Context c, ArrayList<Creation> imageCreations) {
        mContext = c;
        mImageCreations = imageCreations;
    }

    public Integer[] mThumbs =
            { R.drawable.testimage, R.drawable.testimage
            };

    @Override
    public int getCount () {
        return mImageCreations.size();
    }

    @Override
    public Object getItem (int position) {
        return mImageCreations.get(position);
    }


    @Override
    public long getItemId (int position) {
        return position;
    }


    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView called");
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView (mContext);
            imageView.setScaleType (ImageView.ScaleType.FIT_CENTER);
            imageView.setLayoutParams (new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            imageView = (ImageView) convertView;
        }

        String storage_path = mImageCreations.get(position).extra_storage_path;

        Glide.with(mContext)
                .using(new FirebaseImageLoader())
                .load(mStorage.getReference().child(storage_path))
                .thumbnail(0.05f)
                .override(400, 400)
                .into(imageView);

        return imageView;
    }

}
package android.duke290.com.loco.photos;

import android.app.Activity;
import android.content.Context;
import android.duke290.com.loco.Creation;
import android.duke290.com.loco.R;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Jihane on 4/28/17.
 */

/**
 * Implements an image slider for full-size images
 */

public class FullScreenImageAdapter extends PagerAdapter {
    private Activity mActivity;
    private ArrayList<String> mImages;
    private ArrayList<String> mLocations;
    private LayoutInflater mInflater;
    private FirebaseStorage mStorage;
    private String mFetchType;


    // constructor
    public FullScreenImageAdapter(Activity activity, ArrayList<String> imagepaths, ArrayList<String> locations, String fetchtype){
        mActivity = activity;
        mImages = imagepaths;
        mLocations = locations;
        mFetchType = fetchtype;
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    /**
     * Gets item to put on the full-size slide
     * @param container: group of views for full-size display
     * @param position: position of image to display in full-size
     * @return full-size image layout
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position){
        mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = mInflater.inflate(R.layout.activity_photo_full_size, container, false);

        mStorage = FirebaseStorage.getInstance();
        TouchImageView myimage = (TouchImageView) viewLayout.findViewById(R.id.imagefull);
        TextView img_loc = (TextView) viewLayout.findViewById(R.id.img_location);
        if (mFetchType.equals("individual")) {img_loc.setText(mLocations.get(position));}
        else {img_loc.setVisibility(View.INVISIBLE);}
        //myimage.setImageResource(mImages[position]);
        Glide.with(mActivity)
                .using(new FirebaseImageLoader())
                .load(mStorage.getReference().child(mImages.get(position)))
                .fitCenter()
                .into(myimage);

        ((ViewPager) container).addView(viewLayout);
        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }
}


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

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Jihane on 4/28/17.
 */

public class FullScreenImageAdapter extends PagerAdapter {
    private Activity mActivity;
    private ArrayList<String> mImages;
    private LayoutInflater mInflater;
    private FirebaseStorage mStorage;


    // constructor
    public FullScreenImageAdapter(Activity activity, ArrayList<String> imagepaths){
        mActivity = activity;
        mImages = imagepaths;
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position){
        mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = mInflater.inflate(R.layout.activity_photo_full_size, container, false);

        mStorage = FirebaseStorage.getInstance();
        ImageView myimage = (ImageView) viewLayout.findViewById(R.id.imagefull);
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
        ((ViewPager) container).removeView((LinearLayout) object);

    }
}


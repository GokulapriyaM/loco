package android.duke290.com.loco.photos;

import android.content.Intent;
import android.duke290.com.loco.database.Creation;
import android.duke290.com.loco.database.DatabaseFetch;
import android.duke290.com.loco.database.DatabaseFetchCallback;
import android.duke290.com.loco.database.SharedLists;
import android.duke290.com.loco.database.User;
import android.duke290.com.loco.discover.MainActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.duke290.com.loco.R;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

public class PhotosFragment extends Fragment {

    final static String TAG = "PhotosFragment";

    private View mView;
    private GridView mGridView;
    private ImageAdapter mImageAdp;
    private String mFetchType;
    private String INDIVIDUAL = "individual";
    private String SHARED = "shared";

    public PhotosFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_photos, container, false);
        Log.d(TAG, "layout inflated");
        processCreations(getArguments().getString("fetchtype"));
        return mView;
    }

    public void processCreations(String fetchtype) {
        if (mView == null) return;
        mGridView = (GridView) mView.findViewById(R.id.grid_view);

        mFetchType = fetchtype;
        if (mFetchType.equals(SHARED)) {
            setImages(SharedLists.getInstance().getImageCreations());
        }
        Log.d(TAG, "creations processed");
//        if (mFetchType.equals(INDIVIDUAL) && getActivity() != null) {
//            DatabaseFetch databasefetch = new DatabaseFetch(this);
//            databasefetch.fetchByUser();
//        }
    }

//    /**
//     * Fetches images from Database to put in the Photos Activity
//     * @param creations: arraylist of data stored in database
//     */
//
//    @Override
//    public void onDatabaseResultReceived(ArrayList<Creation> creations) {
//        ArrayList<Creation> imagecreations = new ArrayList<Creation>();
//        for (Creation c : creations) {
//            if (c.type.equals("image")) {
//                imagecreations.add(c);
//            }
//        }
//        setImages(imagecreations);
//    }

//    @Override
//    public void onUserReceived(User user) {
//
//    }

    /**
     * Sets adapter and click listeners for images in the gridview
     * @param imagecreations: arraylist of data in the database to retrieve image paths
     */
    private void setImages(final ArrayList<Creation> imagecreations){
        if (getActivity() != null) {
            mImageAdp = new ImageAdapter(getActivity(), imagecreations);
            mGridView.setAdapter(mImageAdp);
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    // Show full-size image
                    Creation storageplace = (Creation) mImageAdp.getItem(position); // replace with full-size image indatabase
                    String path = storageplace.extra_storage_path;
                    Intent fullsize = new Intent(getActivity(), PhotoFullSizeActivity.class);
                    fullsize.putExtra("path", path);
                    fullsize.putExtra("position", position);

                    ArrayList<String> image_paths = new ArrayList<String>();
                    ArrayList<String> locations = new ArrayList<String>();
                    for (Creation c : imagecreations) {
                        image_paths.add(c.extra_storage_path);
                        locations.add(c.address.replace("\r\n", ", ").replace("\n", ", "));
                    }
                    fullsize.putStringArrayListExtra("imagepaths", image_paths);
                    fullsize.putStringArrayListExtra("locations", locations);
                    fullsize.putExtra("fetchtype", mFetchType);
                    startActivity(fullsize);
                }
            });
        }
    }

    public View getView() {
        return mView;
    }


}

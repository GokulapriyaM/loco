package android.duke290.com.loco.discover;

import android.content.Context;
import android.content.Intent;
import android.duke290.com.loco.RecyclerViewClickListener;
import android.duke290.com.loco.database.Creation;
import android.duke290.com.loco.database.SharedLists;
import android.duke290.com.loco.photos.PhotoFullSizeActivity;
import android.duke290.com.loco.photos.PhotosActivity;
import android.duke290.com.loco.posts.Post;
import android.duke290.com.loco.posts.PostAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.duke290.com.loco.R;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DiscoverFragment extends Fragment implements RecyclerViewClickListener {

    final static String TAG = "DiscoverFragment";

    final static int VIEW_COUNT = 5;

    private static String PHOTOS_VIEW = "PHOTOS_VIEW";
    private static String POSTS_VIEW = "POSTS_VIEW";
    private String SHARED = "shared";

    private View mView;

    private FirebaseStorage mStorage;
    ArrayList<Creation> mImageCreations;
    ArrayList<Creation> mMessageCreations;
    ArrayList<Creation> mRatingCreations;
    ArrayList<StorageReference> mStorageRefs;
    ArrayList<Post> mPosts;
    double mAverageRating;
    int mTotalNumRatings;

    private RecyclerView mPhotosRecyclerView;
    private RecyclerView mPostsRecyclerView;
    TextView mRatingMsg;
    TextView mNumRatingMsg;
    ImageView mRatingImg;

    public DiscoverFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "layout inflated");
        mView = inflater.inflate(R.layout.fragment_discover, container, false);
        Log.d(TAG, "re-getting creations");
        ((MainActivity) getActivity()).getCreations();
        return mView;
    }

    public void processCreations() {
        mStorage = FirebaseStorage.getInstance();
        mImageCreations = SharedLists.getInstance().getImageCreations();
        mMessageCreations = SharedLists.getInstance().getMessageCreations();
        mRatingCreations = SharedLists.getInstance().getRatingCreations();
        mStorageRefs = new ArrayList<StorageReference>();
        mPosts = new ArrayList<Post>();

        // get storage references/posts for first VIEW_COUNT creations
        for (int k1 = 0; k1 < VIEW_COUNT; k1++) {
            if (k1 < mImageCreations.size()) {
                mStorageRefs.add(
                        mStorage.getReference().child(mImageCreations.get(k1).extra_storage_path));
            }
            if (k1 < mMessageCreations.size()) {
                mPosts.add(new Post(mMessageCreations.get(k1).message,
                        mMessageCreations.get(k1).timestamp));
            }
        }

        double rating_total = 0;

        // calculate rating stats
        mTotalNumRatings = mRatingCreations.size();
        for (Creation c : mRatingCreations) {
            rating_total += c.rating;
        }
        mAverageRating = rating_total / mTotalNumRatings;

        if (getActivity() != null) {
            if (!mStorageRefs.isEmpty()) {
                displayPhotos();
            }
            if (!mPosts.isEmpty()) {
                displayPosts();
            }
        }
        displayRatings();

    }

    @Override
    public void recyclerViewListClicked(View v, int position, String type) {
        if (type.equals(PHOTOS_VIEW)) {
            // Show full-size image
            Creation storageplace = mImageCreations.get(position); // replace with full-size image in database
            String path = storageplace.extra_storage_path;
            Intent fullsize = new Intent(getActivity(), PhotoFullSizeActivity.class);
            fullsize.putExtra("path", path);
            fullsize.putExtra("position", position);

            ArrayList<String> image_paths = new ArrayList<String>();
            ArrayList<String> locations = new ArrayList<String>();
            for (Creation c: mImageCreations){
                image_paths.add(c.extra_storage_path);
                locations.add(c.address.replace("\r\n", ", ").replace("\n", ", "));
            }
            fullsize.putStringArrayListExtra("imagepaths", image_paths);
            fullsize.putStringArrayListExtra("locations", locations);
            fullsize.putExtra("fetchtype", SHARED);
            startActivity(fullsize);
        }
    }

    public void displayPhotos() {
        Log.d(TAG, "displaying photos");
        mPhotosRecyclerView = (RecyclerView) mView.findViewById(R.id.photos_recycler_view);
        mPhotosRecyclerView.setLayoutManager(
                new LinearLayoutManager(
                        getActivity().getApplicationContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false));
        mPhotosRecyclerView.setAdapter(
                new PhotoAdapter(getActivity().getApplicationContext(),
                        mStorageRefs, this));
    }

    public void displayPosts() {
        Log.d(TAG, "displaying posts");
        mPostsRecyclerView = (RecyclerView) mView.findViewById(R.id.posts_recycler_view);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(
                        getActivity().getApplicationContext(),
                        LinearLayoutManager.VERTICAL, false) {
                    @Override
                    public boolean canScrollVertically() {
                            return false;
                        }
                };
        mPostsRecyclerView.setLayoutManager(linearLayoutManager);
        mPostsRecyclerView.setAdapter(new PostAdapter(mPosts, true));
    }

    /**
     * Displays the rating message and rating image.
     */
    public void displayRatings() {
        mRatingMsg = (TextView) mView.findViewById(R.id.rating_msg);
        mNumRatingMsg = (TextView) mView.findViewById(R.id.num_ratings);
        mRatingImg = (ImageView) mView.findViewById(R.id.rating_faces);

        if (mTotalNumRatings == 0) {
            Log.d(TAG, "no ratings found");
            mRatingMsg.setText("No ratings yet :(");
            mRatingImg.setImageResource(0);
            return;
        }

        // set rating text msgs
        String avg_rating_str = String.format("%.1f", mAverageRating);
        mRatingMsg.setText("Average Happiness: " + avg_rating_str);
        String plural = "";
        if (mTotalNumRatings > 1) plural = "s";
        mNumRatingMsg.setText(mTotalNumRatings + " rating" + plural);

        // set rating image
        double rounded_rating = Double.parseDouble(avg_rating_str);
        // find biggest rating (factor of 0.5) below rounded_rating and multiply that by 10
        int adj_rating = (((int) (rounded_rating * 10)) / 5) * 5;

        // set image
        Context context = mRatingImg.getContext();
        int id = context.getResources().getIdentifier("rate_face_" + adj_rating, "drawable", context.getPackageName());
        mRatingImg.setImageResource(id);
    }

}

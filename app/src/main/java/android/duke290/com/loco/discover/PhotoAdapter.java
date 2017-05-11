package android.duke290.com.loco.discover;

import android.content.Context;
import android.duke290.com.loco.R;
import android.duke290.com.loco.RecyclerViewClickListener;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Created by kevinkuo on 4/27/17.
 *
 * Adapter used for displaying the photos on the main UI.
 */

public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<StorageReference> mStorageRefs;
    private Context mContext;
    private static RecyclerViewClickListener mItemListener;

    private static final int VIEW_TYPE_EMPTY_LIST_PLACEHOLDER = 0;
    private static final int VIEW_TYPE_OBJECT_VIEW = 1;
    private static String PHOTOS_VIEW = "PHOTOS_VIEW";

    private final static String TAG = "PhotoAdapter";

    /**
     * Holder for each ImageView.
     */
    public static class PhotoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mImageView;

        public PhotoViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.photoitem);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            mItemListener.recyclerViewListClicked(v, this.getLayoutPosition(), PHOTOS_VIEW);
        }
    }

    /**
     * Constructor for PhotoAdapter.
     * @param c - Context object
     * @param myDataset - List of Firebase StorageReference objects
     */
    public PhotoAdapter(Context c, List<StorageReference> myDataset, RecyclerViewClickListener itemListener) {
        mStorageRefs = myDataset;
        mContext = c;
        mItemListener = itemListener;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "mStorageRefs size = " + mStorageRefs.size());
        if (mStorageRefs.size() == 0) {
            Log.d(TAG, "empty list view type returned");
            return VIEW_TYPE_EMPTY_LIST_PLACEHOLDER;
        } else {
            return VIEW_TYPE_OBJECT_VIEW;
        }
    }

    public StorageReference getStorageRef(int position) {
        return mStorageRefs.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View v = null;
        switch(viewType) {
            case VIEW_TYPE_EMPTY_LIST_PLACEHOLDER:
                Log.d(TAG, "recycler view empty, posting empty message");
                v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_empty, parent, false);
                break;
            case VIEW_TYPE_OBJECT_VIEW:
                v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_photo, parent, false);
                break;
        }

        PhotoAdapter.PhotoViewHolder gvh = new PhotoAdapter.PhotoViewHolder(v);
        return gvh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PhotoAdapter.PhotoViewHolder photoHolder = (PhotoAdapter.PhotoViewHolder) holder;
        if (position < mStorageRefs.size()) {
            Glide.with(mContext)
                    .using(new FirebaseImageLoader())
                    .load(mStorageRefs.get(position))
                    .thumbnail(0.1f)
                    .into(photoHolder.mImageView);
        }

    }

    @Override
    public int getItemCount() {
        if (mStorageRefs.size() == 0) return 1;
        return mStorageRefs.size();
    }
}


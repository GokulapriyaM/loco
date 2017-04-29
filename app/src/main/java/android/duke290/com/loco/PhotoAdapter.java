package android.duke290.com.loco;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
 */

public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<StorageReference> mStorageRefs;
    private Context mContext;

    // Provide a reference to the views for each data item
    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public PhotoViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.photoitem);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PhotoAdapter(Context c, List<StorageReference> myDataset) {
        mStorageRefs = myDataset;
        mContext = c;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        PhotoAdapter.PhotoViewHolder gvh = new PhotoAdapter.PhotoViewHolder(v);
        return gvh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PhotoAdapter.PhotoViewHolder photoHolder = (PhotoAdapter.PhotoViewHolder) holder;
        Glide.with(mContext)
                .using(new FirebaseImageLoader())
                .load(mStorageRefs.get(position))
                .thumbnail(0.1f)
                .into(photoHolder.mImageView);

    }

    @Override
    public int getItemCount() {
        return mStorageRefs.size();
    }
}


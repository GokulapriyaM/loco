package android.duke290.com.loco.posts;

import android.duke290.com.loco.R;
import android.duke290.com.loco.posts.Post;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<Post> mDataset;

    private static final int VIEW_TYPE_EMPTY_LIST_PLACEHOLDER = 0;
    private static final int VIEW_TYPE_OBJECT_VIEW = 1;

    private static final String TAG = "PostAdapter";

    // Provide a reference to the views for each data item
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        CardView mCVPostView;
        //      TextView mUsernameView;
        TextView mTimeView;
        TextView mPostView;

        public PostViewHolder(View v) {
            super(v);
            mCVPostView = (CardView) v.findViewById(R.id.cvpostitem);
//          mUsernameView = (TextView) v.findViewById(R.id.username);
            mTimeView = (TextView) v.findViewById(R.id.timestamp);
            mPostView =(TextView) v.findViewById(R.id.userpost);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostAdapter(List<Post> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "mDataset size = " + mDataset.size());
        if (mDataset.size() == 0) {
            Log.d(TAG, "empty list view type returned");
            return VIEW_TYPE_EMPTY_LIST_PLACEHOLDER;
        } else {
            return VIEW_TYPE_OBJECT_VIEW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View v = null;
        switch(viewType) {
            case VIEW_TYPE_EMPTY_LIST_PLACEHOLDER:
                Log.d(TAG, "recycler view empty, posting empty message");
                v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_photo_empty, parent, false);
                TextView empty_msg = (TextView) v.findViewById(R.id.empty_msg);
                empty_msg.setText("Be the first to create a post!");
                break;
            case VIEW_TYPE_OBJECT_VIEW:
                v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_post, parent, false);
                break;
        }
        PostViewHolder gvh = new PostViewHolder(v);
        return gvh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PostViewHolder postholder = (PostViewHolder) holder;
//        postholder.mUsernameView.setText(mDataset.get(position).getUsername());
        if (position < mDataset.size()) {
            Post post = mDataset.get(position);
            postholder.mPostView.setText(post.getPost());
            postholder.mTimeView.setText(post.getTimestamp());
        }

    }

    @Override
    public int getItemCount() {
        if (mDataset.size() == 0) return 1;
        return mDataset.size();
    }
}

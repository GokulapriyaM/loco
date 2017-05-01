package android.duke290.com.loco.posts;

import android.duke290.com.loco.R;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter that displays the posts in the Main UI via a RecyclerView.
 */
public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<Post> mDataset;
    private boolean mFromMainUI;

    private static final int VIEW_TYPE_EMPTY_LIST_PLACEHOLDER = 0;
    private static final int VIEW_TYPE_OBJECT_VIEW = 1;

    private static final String TAG = "PostAdapter";

    /**
     * Holder for the post's CardView, and TextViews for the message and timestamp.
     */
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        CardView mCVPostView;
        TextView mTimeView;
        TextView mPostView;

        public PostViewHolder(View v) {
            super(v);
            mCVPostView = (CardView) v.findViewById(R.id.cvpostitem);
            mTimeView = (TextView) v.findViewById(R.id.timestamp);
            mPostView =(TextView) v.findViewById(R.id.userpost);
        }
    }

    /**
     * Constructor for PostAdapter.
     * @param myDataset - List of Post objects
     * @param fromMainUI - boolean that indicates whether or not the PostAdapter was made in
     *                   MainActivity or not.
     */
    public PostAdapter(List<Post> myDataset, boolean fromMainUI) {
        mDataset = myDataset;
        mFromMainUI = fromMainUI;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "mDataset size = " + mDataset.size());
        if (mDataset.size() == 0 && mFromMainUI) {
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
                        R.layout.item_empty, parent, false);
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
        if (position < mDataset.size()) {
            Post post = mDataset.get(position);
            postholder.mPostView.setText(post.getPost());
            postholder.mTimeView.setText(post.getTimestamp());
        }

    }

    @Override
    public int getItemCount() {
        if (mDataset.size() == 0 && mFromMainUI) return 1;
        return mDataset.size();
    }
}

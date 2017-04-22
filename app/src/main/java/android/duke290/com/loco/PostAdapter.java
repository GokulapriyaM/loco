package android.duke290.com.loco;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jihane on 4/22/17.
 */

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<Post> mDataset;

    // Provide a reference to the views for each data item
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        CardView mCVPostView;
        TextView mUsernameView;
        TextView mTimeView;
        TextView mPostView;

        public PostViewHolder(View v) {
            super(v);
            mCVPostView = (CardView) v.findViewById(R.id.cvpostitem);
            mUsernameView = (TextView) v.findViewById(R.id.username);
            mTimeView = (TextView) v.findViewById(R.id.timestamp);
            mPostView =(TextView) v.findViewById(R.id.userpost);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostAdapter(List<Post> myDataset) {
        mDataset = myDataset;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        PostViewHolder gvh = new PostViewHolder(v);
        return gvh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PostViewHolder postholder = (PostViewHolder) holder;
        postholder.mUsernameView.setText(mDataset.get(position).getUsername());
        postholder.mPostView.setText(mDataset.get(position).getPost());
        postholder.mTimeView.setText(mDataset.get(position).getTimestamp().toString());

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

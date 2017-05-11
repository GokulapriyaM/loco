package android.duke290.com.loco.posts;

import android.content.Context;
import android.duke290.com.loco.database.Creation;
import android.duke290.com.loco.database.SharedLists;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.duke290.com.loco.R;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

public class PostsFragment extends Fragment {

    final static String TAG = "PostsFragment";

    private View mView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private List<Post> mDataset = new ArrayList<Post>();
    private RecyclerView.Adapter mAdapter;
    private String mFetchType;
    private String INDIVIDUAL = "individual";
    private String SHARED = "shared";

    public PostsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_posts, container, false);
        Log.d(TAG, "layout inflated");
        processCreations(getArguments().getString("fetchtype"));
        return mView;
    }

    public void processCreations(String fetchtype) {
        if (mView == null) return;
        // Defining recycler view
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.my_recycler_view);

        // Adding a layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);


        if (fetchtype.equals(SHARED)) {
            setDataset(SharedLists.getInstance().getMessageCreations());
            populateView();
        }
        Log.d(TAG, "creations processed");
    }

    private void setDataset(ArrayList<Creation> creations) {
        // clear mDataset
        mDataset.clear();

        for (Creation c : creations) {
            Post post = new Post(c.message, c.timestamp);
            mDataset.add(post);
        }
    }

    private void populateView() {
        mAdapter = new PostAdapter(mDataset, false);
        mRecyclerView.setAdapter(mAdapter);
    }

}

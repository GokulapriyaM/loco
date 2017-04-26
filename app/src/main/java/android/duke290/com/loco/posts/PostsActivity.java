package android.duke290.com.loco.posts;

import android.duke290.com.loco.Creation;
import android.duke290.com.loco.R;
import android.duke290.com.loco.SharedLists;
import android.duke290.com.loco.posts.Post;
import android.duke290.com.loco.posts.PostAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class PostsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Post> mDataset = new ArrayList<Post>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        //Setting the toolbar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.posts_toolbar);
        myToolbar.setTitle("Posts");
        setSupportActionBar(myToolbar);

        // Defining recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // Adding a layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        setDataset();
        // populateview
        populateView();
    }

    private void setDataset() {
        for (Creation c : SharedLists.getInstance().getMessageCreations()) {
            Post post = new Post(c.message, c.timestamp);
            mDataset.add(post);
        }
    }

    private void populateView() {
        mAdapter = new PostAdapter(mDataset);
        mRecyclerView.setAdapter(mAdapter);
    }


}

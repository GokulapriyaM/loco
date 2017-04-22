package android.duke290.com.loco;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
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
        setSupportActionBar(myToolbar);

        // Defining recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // Adding a layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Setting an adapter
        // Testing with a dataset
        Post post1 = new Post("Username1", "Hi! This is test1", System.currentTimeMillis());
        Post post2 = new Post("Username2", "Hi! This is test2", System.currentTimeMillis());
        mDataset.add(post1);
        mDataset.add(post2);
        mAdapter = new PostAdapter(mDataset);
        mRecyclerView.setAdapter(mAdapter);
    }
}

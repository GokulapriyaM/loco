package android.duke290.com.loco.posts;

import android.content.Intent;
import android.duke290.com.loco.R;
import android.duke290.com.loco.database.Creation;
import android.duke290.com.loco.database.DatabaseFetch;
import android.duke290.com.loco.database.DatabaseFetchCallback;
import android.duke290.com.loco.database.SharedLists;
import android.duke290.com.loco.database.User;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays all the posts created at a certain location.
 */
public class PostsActivity extends AppCompatActivity implements DatabaseFetchCallback{

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Post> mDataset = new ArrayList<Post>();

    private String fetchtype;
    private String FETCHTYPE = "fetchtype";
    private String INDIVIDUAL = "individual";
    private String SHARED = "shared";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        //Setting the toolbar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.posts_toolbar);
        myToolbar.setTitle("Posts");
        setSupportActionBar(myToolbar);

        // back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        fetchtype = intent.getStringExtra(FETCHTYPE);

        // Defining recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // Adding a layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        if(fetchtype.equals(SHARED)){
            setDataset(SharedLists.getInstance().getMessageCreations());
            populateView();
        }
        if(fetchtype.equals(INDIVIDUAL)){
            DatabaseFetch databasefetch = new DatabaseFetch(this);
            databasefetch.fetchByUser();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Creates mDataset (the set of posts) from a list of Creation objects.
     * @param creations - ArrayList of Creation objects
     */
    private void setDataset(ArrayList<Creation> creations) {
        for (Creation c : creations) {
            Post post = new Post(c.message, c.timestamp);
            mDataset.add(post);
        }
    }

    /**
     * Sets the adapter that displays the posts.
     */
    private void populateView() {
        mAdapter = new PostAdapter(mDataset, false);
        mRecyclerView.setAdapter(mAdapter);
    }


    @Override
    public void onDatabaseResultReceived(ArrayList<Creation> creations) {
        ArrayList<Creation> creation_list = new ArrayList<Creation>();
        for (Creation c : creations) {
            if (c.type.equals("text")) {
                creation_list.add(c);
            }
        }
        setDataset(creation_list);
        populateView();
    }

    @Override
    public void onUserReceived(User user) {

    }
}

package android.duke290.com.loco.posts;

import android.content.Intent;
import android.duke290.com.loco.Creation;
import android.duke290.com.loco.R;
import android.duke290.com.loco.SharedLists;
import android.duke290.com.loco.User;
import android.duke290.com.loco.database.DatabaseFetch;
import android.duke290.com.loco.database.DatabaseFetchCallback;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PostsActivity extends AppCompatActivity implements DatabaseFetchCallback{

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Post> mDataset = new ArrayList<Post>();

    private String fetchtype;
    private String FETCHTYPE = "fetchtype";
    private String INDIVIDUAL = "individual";
    private String SHARED = "shared";

    private TextView titletext;

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

//        titletext = (TextView) findViewById(R.id.posts_title);

        Intent intent = getIntent();
        fetchtype = intent.getStringExtra(FETCHTYPE);

        // Defining recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // Adding a layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        if(fetchtype.equals(SHARED)){
//            titletext.setText(getString(R.string.poststitle));
            setDataset(SharedLists.getInstance().getMessageCreations());
            populateView();
        }
        if(fetchtype.equals(INDIVIDUAL)){
//            titletext.setText(getString(R.string.poststitle_ind));
            DatabaseFetch databasefetch = new DatabaseFetch(this);
            databasefetch.fetchByUser();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setDataset(ArrayList<Creation> creations) {
        for (Creation c : creations) {
            Post post = new Post(c.message, c.timestamp);
            mDataset.add(post);
        }
    }

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

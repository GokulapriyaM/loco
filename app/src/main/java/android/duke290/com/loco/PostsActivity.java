package android.duke290.com.loco;

import android.content.Intent;
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

    private ArrayList<String> messages;
    private ArrayList<String> messages_time;
    private final String messagesKey = "messages";
    private final String messagesTimeKey = "messages_time";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        //Setting the toolbar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.posts_toolbar);
        setSupportActionBar(myToolbar);

        Intent intent = getIntent();
        messages = intent.getStringArrayListExtra(messagesKey);
        messages_time = intent.getStringArrayListExtra(messagesTimeKey);

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
        for (int i = 0; i < messages.size(); i++) {
            Post post = new Post(messages.get(i), messages_time.get(i));
            mDataset.add(post);
        }
    }

    private void populateView() {
        mAdapter = new PostAdapter(mDataset);
        mRecyclerView.setAdapter(mAdapter);
    }


}

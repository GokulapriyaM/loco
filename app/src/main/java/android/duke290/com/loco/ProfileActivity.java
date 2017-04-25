package android.duke290.com.loco;

import android.content.Intent;
import android.duke290.com.loco.database.DatabaseFetch;
import android.duke290.com.loco.database.DatabaseFetchCallback;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity implements DatabaseFetchCallback{
    private static final String TAG = "ProfileActivity";

    private Button signOut;
    private TextView nameText;
    private ProgressBar progressBar;
    private ImageView photo1;
    private ImageView photo2;
    private ImageView photo3;
    private TextView post1;
    private TextView post2;
    private TextView post3;


    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth mAuth;

    public User currentUser;
    public DatabaseFetch databasefetch;

    private FirebaseStorage mStorage;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_STORAGE = 1;
    static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    // variables used for Camera
    private Bitmap mBitmap;
    private Uri mUri;
    public String mCurrentPhotoPath;
    private File mFile;
    public ImageView mPicture;
    public TextView mAddPicture;

    private static final String USERS = "users";
    private static final String USERINFO = "userinfo";

    private final int limit = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //mAddPicture = (TextView) findViewById(R.id.addpicture);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("My Profile");
        //toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        mStorage = FirebaseStorage.getInstance();

        //get firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = mAuth.getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user mAuth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                }

            }
        };

        databasefetch = new DatabaseFetch(this);

        signOut = (Button) findViewById(R.id.sign_out);
        nameText = (TextView) findViewById(R.id.name_text);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        photo1 = (ImageView) findViewById(R.id.photo1);
        photo2 = (ImageView) findViewById(R.id.photo2);
        photo3 = (ImageView) findViewById(R.id.photo3);
        post1 = (TextView) findViewById(R.id.post1);
        post2 = (TextView) findViewById(R.id.post2);
        post3 = (TextView) findViewById(R.id.post3);

        showProgressBar();
        setCurrentUser();
        setUserCreation();
        if (progressBar != null) {
            hideProgressBar();
        }

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

    }

    /**
     * set name TextView with username
     */
    private void setNameText(){
        nameText.setText(currentUser.name);
    }


    public void signOut() {
        mAuth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    private void setCurrentUser(){
        databasefetch.getCurrentUser();
    }

    private void setUserCreation(){
        databasefetch.fetchByUser();
    }

    @Override
    public void onUserReceived(User user){
        currentUser = user;
        setNameText();
    }

    public void onMorePostsClick(View view){
        Intent intent = new Intent(ProfileActivity.this, PostsActivity.class);
        startActivity(intent);
    }

    public void onMorePhotosClick(View view){
        Intent intent = new Intent(ProfileActivity
                .this, PhotosActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDatabaseResultReceived(ArrayList<Creation> creations) {
        Log.d(TAG, "onDatabaseResultReceived called");
        Log.d(TAG, "creations.size() = " + creations.size());

        clearUI();

        ArrayList<Creation> messagecreations = new ArrayList<>();
        ArrayList<Creation> image_creation_list = new ArrayList<Creation>();

        ArrayList<String> messages = new ArrayList<>();
        ArrayList<StorageReference> storagerefs = new ArrayList<>();
        for (Creation c : creations) {
            if (c.type.equals("text")) {
                messages.add(c.message);
                messagecreations.add(c);
            }
            if (c.type.equals("image")) {
                image_creation_list.add(c);
                String storage_path = c.extra_storage_path;
                StorageReference storageRef = mStorage.getReference().child(storage_path);
                storagerefs.add(storageRef);
            }
        }

        SharedLists.getInstance().setMessageCreations(messagecreations);
        SharedLists.getInstance().setImageCreations(image_creation_list);

        populateView(messages, storagerefs);
    }

    private void populateView(ArrayList<String> messages, ArrayList<StorageReference> storagerefs){
        int messages_size = messages.size();
        int storagerefs_size = storagerefs.size();

        if(messages_size>=1){
            post1.setText(messages.get(0));
        }
        if(messages_size>=2){
            post2.setText(messages.get(1));
        }
        if(messages_size>=3){
            post3.setText(messages.get(2));
        }

        if(storagerefs_size>=1){
            Glide.with(getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(storagerefs.get(0))
                    .override(75, 75)
                    .into(photo1);
        }
        if(storagerefs_size>=2){
            Glide.with(getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(storagerefs.get(1))
                    .override(75, 75)
                    .into(photo2);
        }
        if(storagerefs_size>=3){
            Glide.with(getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(storagerefs.get(2))
                    .override(75, 75)
                    .into(photo3);
        }
    }

    public void clearUI() {
        post1.setText("");
        post2.setText("");
        post3.setText("");
        photo1.setImageResource(0);
        photo2.setImageResource(0);
        photo3.setImageResource(0);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }

    private void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){ progressBar.setVisibility(View.GONE); }
}



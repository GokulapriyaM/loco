package android.duke290.com.loco;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.duke290.com.loco.cloud.CloudStorageAction;
import android.duke290.com.loco.database.DatabaseAction;
import android.duke290.com.loco.database.DatabaseFetch;
import android.duke290.com.loco.database.DatabaseFetchCallback;
import android.duke290.com.loco.location.Constants;
import android.duke290.com.loco.location.LocationService;
import android.duke290.com.loco.photos.PhotosActivity;
import android.duke290.com.loco.posts.PostsActivity;
import android.duke290.com.loco.posts.ShareTextActivity;
import android.duke290.com.loco.registration.LoginActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

import static android.duke290.com.loco.ProfileActivity.REQUEST_IMAGE_CAPTURE;

/*
 * Everytime onCreate() is called, the activity does the following:
 * Connect to internet -> Gets coordinates -> Gets address -> Displays coordinates/address
 */

public class MainActivity extends AppCompatActivity implements DatabaseFetchCallback{

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;

    // for now, this is always true, but we can change it if needed
    private boolean mAddressRequested = true;

    private AddressResultReceiver mAddressResultReceiver;

    private Location mCurrentLocation;
    private String mAddressOutput;
    private double latitude;
    private double longitude;
    private double mAverageRating;

    private Creation mCreation;

    private DatabaseFetch databaseFetch;
    private FirebaseStorage mStorage;

    private ArrayList<String> mCloudProcessMsgs;

    private CloudResultReceiver mCloudResultReceiver;

    private LocationResultReceiver mLocationResultReceiver;

    private String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private User currentUser;

    final String LATITUDE = "latitude";
    final String LONGITUDE = "longitude";
    final String ADDRESS = "address";

    private ImageView photo1;
    private ImageView photo2;
    private ImageView photo3;
    private TextView post1;
    private TextView post2;
    private TextView post3;
    private TextView mCoordsMsg;
    private TextView mAddressMsg;


    private String mCurrentPhotoPath;

    private final int limit = 3;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;

    private String FETCHTYPE = "fetchtype";
    private String INDIVIDUAL = "individual";
    private String SHARED = "shared";

    private List<String> mStoragePaths;
    private RecyclerView mPhotosRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCloudProcessMsgs = new ArrayList<String>();

        mStorage = FirebaseStorage.getInstance();

        //get firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();

        //Setting the toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.start_toolbar);
        toolbar.setTitle("Discover");
        setSupportActionBar(toolbar);

        navigationView = (NavigationView)findViewById(R.id.navigation_view);

        mPhotosRecyclerView = (RecyclerView) findViewById(R.id.photos_recycler_view);


        // get layout variables
        photo1 = (ImageView) findViewById(R.id.photo1);
        photo2 = (ImageView) findViewById(R.id.photo2);
        photo3 = (ImageView) findViewById(R.id.photo3);
        post1 = (TextView) findViewById(R.id.post1);
        post2 = (TextView) findViewById(R.id.post2);
        post3 = (TextView) findViewById(R.id.post3);
        mCoordsMsg = (TextView) findViewById(R.id.coords_msg);
        mAddressMsg = (TextView) findViewById(R.id.address_msg);

        databaseFetch = new DatabaseFetch(this);

        // set up fab menu and nav drawer
        setUpFabMenu();
        initNavigationDrawer();

        databaseFetch.getCurrentUser();

        // request location permissions if necessary
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);

            return;
        }

        // if location permission granted
        Log.d(TAG, "receivers instantiated");
        mAddressResultReceiver = new AddressResultReceiver(new Handler());
        mLocationResultReceiver = new LocationResultReceiver(new Handler());
        mCloudResultReceiver = new CloudResultReceiver(new Handler());

        if (savedInstanceState != null) {
            // update values from last saved instance
            updateValuesFromBundle(savedInstanceState);
            updateUI();
        }

    }

    private void setUpFabMenu() {
        Log.d(TAG, "setting up fab menu");
        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.our_fab);
        Log.d(TAG, "setting up menu listener");
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                Log.d(TAG, "onMenuItemSelected called");
                Log.d(TAG, "menu item id: " + menuItem.getItemId());
                if (menuItem.getItemId() == R.id.fab_share_text) {
                    Log.d(TAG, "share text button pressed");
                    shareText();
                } else if (menuItem.getItemId() == R.id.fab_share_photo) {
                    Log.d(TAG, "share photo button pressed");
                    try {
                        sharePhoto();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (menuItem.getItemId() == R.id.fab_rate) {
                    Log.d(TAG, "rate button pressed");
                    postRating();
                }
                return false;
            }
        });

    }

    public void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.d(TAG, "updating location values");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains("LOCATION_KEY")) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocation is not null.
                mCurrentLocation = savedInstanceState.getParcelable("LOCATION_KEY");
            }

            if (savedInstanceState.keySet().contains("PROCESS_MSGS")) {
                mCloudProcessMsgs = savedInstanceState.getStringArrayList("PROCESS_MSGS");
            }

        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "saving location values");
        savedInstanceState.putParcelable("LOCATION_KEY", mCurrentLocation);
        savedInstanceState.putStringArrayList("PROCESS_MSGS", mCloudProcessMsgs);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateUI() {
        displayLocation();
        getAddress();
        getCreations();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission granted");

                    mAddressResultReceiver = new AddressResultReceiver(new Handler());
                    mLocationResultReceiver = new LocationResultReceiver(new Handler());
                    mCloudResultReceiver = new CloudResultReceiver(new Handler());

                    ServiceStarter.startLocationService(getApplicationContext(),
                            mLocationResultReceiver);

                } else {
                    Log.d(TAG, "permission not granted");
                }
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart called");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop called");
        super.onStop();
        if (!isChangingConfigurations()) {
            Log.d(TAG, "stopping location service");
            this.stopService(new Intent(this, LocationService.class));
        }

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause called");
        super.onPause();
        if (!isChangingConfigurations()) {
            Log.d(TAG, "stopping location service");
            this.stopService(new Intent(this, LocationService.class));
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        ServiceStarter.startLocationService(getApplicationContext(),
                mLocationResultReceiver);

    }

    protected void getAddress() {
        if (mCurrentLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, "no geocoder available",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (mAddressRequested) {
                ServiceStarter.startAddressIntentService(getApplicationContext(),
                        mAddressResultReceiver, mCurrentLocation);
            }
        }
    }

    class LocationResultReceiver extends ResultReceiver {
        public LocationResultReceiver(Handler handler) { super(handler); }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mCurrentLocation = resultData.getParcelable("LOCATION_KEY");
            getAddress();
            displayLocation();
            getCreations();
        }

    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            Log.d(TAG, "address found");

        }
    }

    class CloudResultReceiver extends ResultReceiver {
        public CloudResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.d(TAG, "Cloud result received");

            // Display the address string
            // or an error message sent from the intent service.
            mCloudProcessMsgs.add(resultData.getString("CLOUD_PROCESS_MSG_KEY"));

            if (resultData.getString("CLOUD_ACTION_TYPE").equals("upload") &&
                    resultCode == Constants.SUCCESS_RESULT) {
                Log.d(TAG, "Cloud upload complete");
                // upload creation to firebase database
                DatabaseAction.putCreationInFirebaseDatabase(mCreation, mCurrentLocation);
                Log.d(TAG, "Creation uploaded");

            }

            displayProcessOutput();

            Log.d(TAG, "Cloud process finished");
        }
    }

    public void displayLocation() {
        latitude = mCurrentLocation.getLatitude();
        longitude = mCurrentLocation.getLongitude();
        mCoordsMsg.setText("Latitude: " + latitude + ", Longitude: " + longitude);
    }

    protected void displayAddressOutput() {
        mAddressMsg.setText(mAddressOutput);
    }

    protected void displayProcessOutput() {
        if (mCloudProcessMsgs.size() == 0) return;
        Log.d(TAG, "displayProcessOutput called");
        Toast.makeText(this, mCloudProcessMsgs.get(mCloudProcessMsgs.size() - 1),
                Toast.LENGTH_LONG).show();
    }

    protected void uploadStreamToFirebaseStorage(InputStream inputStream,
                                                 String storage_path,
                                                 String content_type) {
        Log.d(TAG, "uploading stream to firebase storage (" + content_type + ")");
        CloudStorageAction action = new CloudStorageAction(getApplicationContext(),
                "upload",
                mCloudResultReceiver,
                inputStream,
                storage_path,
                content_type);
        action.doCloudAction();
    }

    protected void shareText() {
        Intent intent = new Intent(MainActivity.this, ShareTextActivity.class);
        intent.putExtra(LATITUDE, latitude);
        intent.putExtra(LONGITUDE, longitude);
        intent.putExtra(ADDRESS, mAddressOutput);
        startActivity(intent);
    }

    // Code for taking a picture
    protected void sharePhoto() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            // Continue only if the File was successfully created
            Log.d(TAG, "Photo file" + photoFile);
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "android.duke290.com.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void postRating() {
        // creating creation
        String timestamp = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US).format(new Date());

        Random r = new Random();
        final int min = 1;
        final int max = 5;
        final int random_rating = r.nextInt((max - min) + 1) + min;

        Log.d(TAG, "Posted rating: " + random_rating);

        mCreation = new Creation(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(), mAddressOutput,
                "rating", "", "", random_rating, timestamp);

        DatabaseAction.putCreationInFirebaseDatabase(mCreation, mCurrentLocation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            // Kevin, this is the thumbnail Bitmap you could use for storage
            //Bitmap mBitmap = (Bitmap) extras.get("data");
            // After storing the image in the database, we can go back to home
            // or go to photos activity and show user image uploaded ...
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    // This is the full-size bitmap
                    Bitmap fullsizeBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                    // Testing in imageview
                    //Log.d(TAG, "is null?"+ mBitmap);
                    //ImageView testimage = (ImageView) findViewById(R.id.testfullsize);
                    //testimage.setImageBitmap(mBitmap);

                    // getting bitmap
                    Bitmap mBitmap = ThumbnailUtils.extractThumbnail(fullsizeBitmap, 180, 240);
                    //testimage.setImageBitmap(mBitmap);

                    // creating creation
                    String image_storage_path = DatabaseAction.createImageStoragePath();
                    String timestamp = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US).format(new Date());


                    mCreation =new

                            Creation(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude(),mAddressOutput,
                            "image","",image_storage_path,0,timestamp);

                    // upload image to firebase storage
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    fullsizeBitmap.compress(Bitmap.CompressFormat.JPEG, 50 ,bos);
                    byte[] bitmapdata = bos.toByteArray();
                    ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

                    Log.d(TAG,"uploading stream to storage");

                    uploadStreamToFirebaseStorage(bs, image_storage_path, "image");

                    // uploading creation to database is handled by CloudStorageReceiver.onReceiveResult

                    return null;
                }
            }.execute();

        }
    }

    // Creating a unique filename with timestamp
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, //* prefix *//*
                ".jpg",        //* suffix *//*
        storageDir     //* directory *//*
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "image path " + mCurrentPhotoPath);
        return image;
    }

//    //When clicking share
//    public void shareClick(View view){
//        // Show a pop-up dialog
//        final Dialog mBottomSheetDialog = new Dialog(this, R.style.MaterialDialogSheet);
//        mBottomSheetDialog.setContentView(R.layout.item_share); // your custom view.
//        mBottomSheetDialog.setCancelable(true);
//        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
//        mBottomSheetDialog.show();
//    }

    private void getCreations(){
        String coordname = DatabaseAction.createCoordName(mCurrentLocation);
        databaseFetch.fetchByCoordinate(coordname);
    }

    // on learn more click
    public void onMorePostsClick(View view){
        Intent intent = new Intent(MainActivity.this, PostsActivity.class);
        intent.putExtra(FETCHTYPE,SHARED);
        startActivity(intent);
    }

    public void onMorePhotosClick(View view){
        Intent intent = new Intent(MainActivity.this, PhotosActivity.class);
        intent.putExtra(FETCHTYPE,SHARED);
        startActivity(intent);
    }

    public void onMapClick(View view){
        Uri gmmIntentUri = Uri.parse("geo:" +latitude +","+longitude);
        showMap(gmmIntentUri);
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    @Override
    public void onDatabaseResultReceived(ArrayList<Creation> creations) {
        Log.d(TAG, "onDatabaseResultReceived called");
        Log.d(TAG, "creations.size() = " + creations.size());

        clearUI();

        ArrayList<Creation> messagecreations = new ArrayList<>();
        ArrayList<Creation> image_creation_list = new ArrayList<Creation>();
        double rating_sum = 0;
        int rating_cnt = 0;

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
            if (c.type.equals("rating")) {
                rating_cnt++;
                rating_sum += c.rating;
                Log.d(TAG, "received rating: " + c.rating);
            }
        }

        if (rating_cnt > 0) {
            Log.d(TAG, "rating_sum = " + rating_sum);
            Log.d(TAG, "rating_cnt = " + rating_cnt);
            mAverageRating = rating_sum / rating_cnt;
            Log.d(TAG, "mAverageRating = " + mAverageRating);
            TextView rating_msg = (TextView) findViewById(R.id.rating_msg);
            rating_msg.setText("Average Rating: " + mAverageRating);
        }

        SharedLists.getInstance().setMessageCreations(messagecreations);
        SharedLists.getInstance().setImageCreations(image_creation_list);

        populateView(messages, storagerefs);
    }

    public void clearUI() {
        post1.setText("");
        post2.setText("");
        post3.setText("");
        photo1.setImageResource(0);
        photo2.setImageResource(0);
        photo3.setImageResource(0);
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

        //        if(storagerefs_size>=1){
//            Glide.with(getApplicationContext())
//                    .using(new FirebaseImageLoader())
//                    .load(storagerefs.get(0))
//                    .thumbnail(0.1f)
//                    .into(photo1);
//        }
//        if(storagerefs_size>=2){
//            Glide.with(getApplicationContext())
//                    .using(new FirebaseImageLoader())
//                    .load(storagerefs.get(1))
//                    .thumbnail(0.1f)
//                    .into(photo2);
//        }
//        if(storagerefs_size>=3){
//            Glide.with(getApplicationContext())
//                    .using(new FirebaseImageLoader())
//                    .load(storagerefs.get(2))
//                    .thumbnail(0.1f)
//                    .into(photo3);
//        }

        ArrayList<StorageReference> shorterStorageRefs = new ArrayList<StorageReference>();
        if (storagerefs.size() <= 5) shorterStorageRefs = storagerefs;
        else {
            for (int k1 = 0; k1 < 5; k1++) {
                shorterStorageRefs.add(storagerefs.get(k1));
            }
        }

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mPhotosRecyclerView.setLayoutManager(layoutManager);
        mPhotosRecyclerView.setAdapter(new PhotoAdapter(getApplicationContext(), shorterStorageRefs));
    }


    @Override
    public void onUserReceived(User user){
        currentUser = user;
        setUserText();
    }

    private void setUserText(){
        View header = navigationView.getHeaderView(0);
        TextView user_name = (TextView)header.findViewById(R.id.user_name);
        TextView user_email = (TextView)header.findViewById(R.id.user_email);
        user_name.setText(currentUser.name);
        user_email.setText(currentUser.email);
    }

    public void initNavigationDrawer() {

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                switch (id){
                    case R.id.home:
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.profile:
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                        break;
                    case R.id.my_photos:
                        drawerLayout.closeDrawers();
                        onMyPhotosClick();
                        break;
                    case R.id.my_posts:
                        drawerLayout.closeDrawers();
                        onMyPostsClick();
                        break;
                    case R.id.signout:
                        mAuth.signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                        break;
                }
                return true;
            }
        });
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close){

            @Override
            public void onDrawerClosed(View v){
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void onMyPhotosClick(){
        Intent intent = new Intent(MainActivity.this, PhotosActivity.class);
        intent.putExtra(FETCHTYPE,INDIVIDUAL);
        startActivity(intent);

    }

    private void onMyPostsClick(){
        Intent intent = new Intent(MainActivity.this, PostsActivity.class);
        intent.putExtra(FETCHTYPE,INDIVIDUAL);
        startActivity(intent);
    }

}


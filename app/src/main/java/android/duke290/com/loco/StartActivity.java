package android.duke290.com.loco;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.duke290.com.loco.database.DatabaseAction;
import android.duke290.com.loco.database.DatabaseFetch;
import android.duke290.com.loco.database.DatabaseFetchCallback;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.duke290.com.loco.ProfileActivity.REQUEST_IMAGE_CAPTURE;

/*
 * Everytime onCreate() is called, the activity does the following:
 * Connect to internet -> Gets coordinates -> Gets address -> Displays coordinates/address
 */

public class StartActivity extends AppCompatActivity implements DatabaseFetchCallback{

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;

    // for now, this is always true, but we can change it if needed
    private boolean mAddressRequested = true;

    private AddressResultReceiver mAddressResultReceiver;

    private Location mCurrentLocation;
    private String mAddressOutput;
    private double latitude;
    private double longitude;

    private DatabaseFetch databaseFetch;
    private FirebaseStorage mStorage;

    private ArrayList<String> mCloudProcessMsgs;
    private ArrayList<InputStream> mCloudDownloadedStreams;
    private ArrayList<String> mCloudDownloadedContentTypes;
    private ArrayList<String> mCloudDownloadedFilenames;

    private ArrayList<String> mOutputMessageList;

    private CloudResultReceiver mCloudResultReceiver;

    private LocationResultReceiver mLocationResultReceiver;

    private String TAG = "StartActivity";
    private FirebaseAuth mAuth;

    final String LATITUDE = "latitude";
    final String LONGITUDE = "longitude";
    final String ADDRESS = "address";

    private boolean photo1_occupied;
    private boolean photo2_occupied;

    private ImageView photo1;
    private ImageView photo2;
    private ImageView photo3;
    private TextView post1;
    private TextView post2;
    private TextView post3;

    private final int limit = 3;

    private ArrayList<String> messages;
    private ArrayList<String> messages_time;
    private final String messagesKey = "messages";
    private final String messagesTimeKey = "messages_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mCloudProcessMsgs = new ArrayList<String>();
        mCloudDownloadedStreams = new ArrayList<InputStream>();
        mCloudDownloadedContentTypes = new ArrayList<String>();
        mOutputMessageList = new ArrayList<String>();
        mCloudDownloadedFilenames = new ArrayList<String>();

        mStorage = FirebaseStorage.getInstance();

        //get firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();

        //Setting the toolbar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.start_toolbar);
        setSupportActionBar(myToolbar);

        // update values from last saved instance
        updateValuesFromBundle(savedInstanceState);

        // request location permissions if necessary
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);

            return;
        }

        mAddressResultReceiver = new AddressResultReceiver(new Handler());
        mLocationResultReceiver = new LocationResultReceiver(new Handler());
        mCloudResultReceiver = new CloudResultReceiver(new Handler());

        databaseFetch = new DatabaseFetch(this);

        photo1 = (ImageView) findViewById(R.id.photo1);
        photo2 = (ImageView) findViewById(R.id.photo2);
        photo3 = (ImageView) findViewById(R.id.photo3);
        post1 = (TextView) findViewById(R.id.post1);
        post2 = (TextView) findViewById(R.id.post2);
        post3 = (TextView) findViewById(R.id.post3);
    }

    public void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.d(TAG, "updating location values");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains("LOCATION_KEY")) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocation is not null.
                mCurrentLocation = savedInstanceState.getParcelable("LOCATION_KEY");
            }

            if (savedInstanceState.keySet().contains("DOWNLOADED_FILENAMES")) {
                mCloudDownloadedFilenames =
                        savedInstanceState.getStringArrayList("DOWNLOADED_FILENAMES");

                // update mCloudDownloadedStreams
                for (String filename : mCloudDownloadedFilenames) {
                    addFilesToDownloadedStreams(filename);
                }
            }

            if (savedInstanceState.keySet().contains("PROCESS_MSGS")) {
                mCloudProcessMsgs = savedInstanceState.getStringArrayList("PROCESS_MSGS");
            }
            if (savedInstanceState.keySet().contains("DOWNLOADED_MSGS")) {
                mOutputMessageList = savedInstanceState.getStringArrayList("DOWNLOADED_MSGS");
            }

            updateUI();
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "saving location values");
        savedInstanceState.putParcelable("LOCATION_KEY", mCurrentLocation);
        savedInstanceState.putStringArrayList("DOWNLOADED_FILENAMES", mCloudDownloadedFilenames);
        savedInstanceState.putStringArrayList("PROCESS_MSGS", mCloudProcessMsgs);
        savedInstanceState.putStringArrayList("DOWNLOADED_MSGS", mOutputMessageList);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateUI() {
        displayProcessOutput();
        displayAddressOutput();
        displayAllDownloadedStorageOutput();
    }

    /*
     * Just indicates whether or not user granted permissions (can change implementation if necesssary)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission granted");

//                    startLocationService();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /*
    Handling menu options clicks
     */

    public boolean photosclick(MenuItem item){
        Intent intentphotos = new Intent(this, PhotosActivity.class);
        this.startActivity(intentphotos);
        return true;
    }

    public boolean postsclick(MenuItem item){
        Intent intentposts = new Intent(this, PostsActivity.class);
        this.startActivity(intentposts);
        return true;
    }

    public boolean profileclick(MenuItem item){
        Intent intentprofile = new Intent(this, ProfileActivity.class);
        this.startActivity(intentprofile);
        return true;
    }

    public boolean signoutclick(MenuItem item){
        signOut();
        return true;
    }

    public boolean homeclick(MenuItem item){
        Intent intenthome = new Intent(this, StartActivity.class);
        this.startActivity(intenthome);
        return true;
    }


    public void signOut() {
        mAuth.signOut();
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

        this.stopService(new Intent(this, LocationService.class));
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause called");
        super.onPause();

        this.stopService(new Intent(this, LocationService.class));
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

            boolean downloaded_items_exist = false;

            // Display the address string
            // or an error message sent from the intent service.
            mCloudProcessMsgs.add(resultData.getString("CLOUD_PROCESS_MSG_KEY"));

            if (resultData.getString("CLOUD_DOWNLOADED_FILENAME") != null) {
                downloaded_items_exist = true;
                Log.d(TAG, "Cloud downloaded something");
                String stg_filename = resultData.getString("CLOUD_DOWNLOADED_FILENAME");
                mCloudDownloadedFilenames.add(stg_filename);
                addFilesToDownloadedStreams(stg_filename);
            }

            mCloudDownloadedContentTypes.add(resultData.getString("CLOUD_DOWNLOADED_CONTENT_TYPE"));
            Log.d(TAG, "Downloaded content type: " + resultData.getString("CLOUD_DOWNLOADED_CONTENT_TYPE"));

            if (downloaded_items_exist) {
                displayDownloadedStorageOutput();
            }

            displayProcessOutput();

            Log.d(TAG, "Cloud process finished");

        }
    }

    private void addFilesToDownloadedStreams(String stg_filename) {
        Context context = getApplicationContext();
        try {
            mCloudDownloadedStreams.add(new BufferedInputStream(new FileInputStream(
                    new File(context.getDir("downloaded_storage_items", Context.MODE_PRIVATE),
                            stg_filename))));
            Log.d(TAG, "getting stuff from : " + stg_filename);
            Log.d(TAG, "new stream added : size of " +
                    "mCloudDownloadedStreams = " + mCloudDownloadedStreams.size());
            Log.d(TAG, "new stream null?:" +
                    (mCloudDownloadedStreams.get(mCloudDownloadedStreams.size() - 1) == null));

        } catch (IOException e) {
            Log.d(TAG, "failed to create inputstream " +
                    "from internal storage file: " + e.getMessage());
        }
    }

    public void displayLocation() {
        latitude = mCurrentLocation.getLatitude();
        longitude = mCurrentLocation.getLongitude();
        Log.d(TAG, "Latitude: " + latitude + ", " + "Longitude: " + longitude);
        TextView location_msg = (TextView) findViewById(R.id.location_msg);
        location_msg.setText("Latitude: " + latitude + ", Longitude: " + longitude);

    }

    protected void displayAddressOutput() {
        TextView address_msg = (TextView) findViewById(R.id.address_msg);
        address_msg.setText(mAddressOutput);
    }

    protected void displayProcessOutput() {
        if (mCloudProcessMsgs.size() == 0) return;
        Log.d(TAG, "displayProcessOutput called");
        TextView process_msg = (TextView) findViewById(R.id.process_msg);
        process_msg.setText(mCloudProcessMsgs.get(mCloudProcessMsgs.size() - 1));
    }

    protected void displayDownloadedStorageOutput() {
        if (mCloudDownloadedStreams.size() == 0) return;
        Log.d(TAG, "displayDownloadedStorageOutput called");

        if (photo1_occupied && photo2_occupied) {
            return;
        }

        ImageView downloaded_img = (ImageView) findViewById(R.id.photo1);
        if (photo1_occupied) {
            downloaded_img = (ImageView) findViewById(R.id.photo2);
            photo2_occupied = true;
        } else {
            photo1_occupied = true;
        }

        Log.d(TAG, "displaying image " + mCloudDownloadedStreams.size());
        downloaded_img.setImageBitmap(
                BitmapFactory.decodeStream(
                        mCloudDownloadedStreams.get(mCloudDownloadedStreams.size() - 1)));

    }

    protected void displayAllDownloadedStorageOutput() {
        ImageView img_1 = (ImageView) findViewById(R.id.photo1);
        ImageView img_2 = (ImageView) findViewById(R.id.photo2);
        if (mCloudDownloadedStreams.size() == 0) return;
        if (mCloudDownloadedStreams.size() >= 1) {
            img_1.setImageBitmap(
                    BitmapFactory.decodeStream(
                            mCloudDownloadedStreams.get(0)));
            if (mCloudDownloadedStreams.size() >= 2) {
                img_2.setImageBitmap(
                        BitmapFactory.decodeStream(
                                mCloudDownloadedStreams.get(1)));
            }
        }
    }

    protected void resetReceivedCloudItems() {
        mCloudProcessMsgs = new ArrayList<String>();
        mCloudDownloadedStreams = new ArrayList<InputStream>();
        mCloudDownloadedContentTypes = new ArrayList<String>();
        mOutputMessageList = new ArrayList<String>();
        mCloudDownloadedFilenames = new ArrayList<String>();
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

    /*
     * downloaded stream is put in mCloudDownloadedStream
     */
    protected void downloadStreamFromFirebaseStorage(String storage_path) {
        Log.d(TAG, "downloading stream from firebase storage");
        CloudStorageAction action = new CloudStorageAction(getApplicationContext(),
                "download",
                mCloudResultReceiver,
                null,
                storage_path,
                null);
        action.doCloudAction();
    }

    protected void uploadClick(View button) {
        Log.d(TAG, "upload button clicked");

        String image_storage_path = DatabaseAction.createImageStoragePath();
        String timestamp = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US).format(new Date());

        Creation new_reference = new Creation(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(), "420 Chapel Drive",
                "image", "cool beans 3", image_storage_path, timestamp);

        // upload test file
        AssetManager assetManager = getAssets();
        InputStream imageStream = null;

        try {
            imageStream = assetManager.open("Images/ola_pic.gif");
        } catch (IOException e) {
            Log.d(TAG, "IOException opening local file");
        }

        uploadStreamToFirebaseStorage(imageStream,
                image_storage_path,
                "image");

        ///

        DatabaseAction.putCreationInFirebaseDatabase(new_reference, mCurrentLocation);

    }



    protected void profileClick(View view){
        startActivity(new Intent(StartActivity.this, ProfileActivity.class));
//        finish(); // commented out so that user can hit back button in ProfileActivity to go
        // back here
    }

    protected void shareTextClick(View view) {
        Intent intent = new Intent(StartActivity.this, ShareTextActivity.class);
        intent.putExtra(LATITUDE, latitude);
        intent.putExtra(LONGITUDE, longitude);
        intent.putExtra(ADDRESS, mAddressOutput);
        startActivity(intent);
    }

    // Code for taking a picture
    protected void sharePhotoClick(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            // Kevin, this is the thumbnail Bitmap you could use for storage
            Bitmap mBitmap = (Bitmap) extras.get("data");
            // After storing the image in the database, we can go back to home
            // or go to photos activity and shoe user image uploaded ...
        }
    }

    //When clicking share
    public void shareClick(View view){
        // Show a pop-up dialog
        final Dialog mBottomSheetDialog = new Dialog(this, R.style.MaterialDialogSheet);
        mBottomSheetDialog.setContentView(R.layout.item_share); // your custom view.
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
        mBottomSheetDialog.show();
    }

    private void getCreations(){
        String coordname = DatabaseAction.createCoordName(mCurrentLocation);
        databaseFetch.fetchByCoordinate(coordname);
    }

    // on learn more click
    public void onMorePostsClick(View view){
        Intent intent = new Intent(StartActivity.this, PostsActivity.class);
        intent.putStringArrayListExtra(messagesKey, messages);
        intent.putStringArrayListExtra(messagesTimeKey, messages_time);
        startActivity(intent);
    }

    public void onMorePhotosClick(View view){
        Intent intent = new Intent(StartActivity.this, PhotosActivity.class);
        startActivity(intent);
    }


    @Override
    public void onDatabaseResultReceived(ArrayList<Creation> creations) {
        messages = new ArrayList<>();
        messages_time = new ArrayList<>();
        ArrayList<StorageReference> storagerefs = new ArrayList<>();
        for (Creation c : creations) {
            if (c.type.equals("text")) {
                messages.add(c.message);
                messages_time.add(c.timestamp);
            }
            if (c.type.equals("image")) {
                String storage_path = c.extra_storage_path;
                StorageReference storageRef = mStorage.getReference().child(storage_path);
                storagerefs.add(storageRef);
            }
        }
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


    @Override
    public void onUserReceived(User user) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

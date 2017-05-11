package android.duke290.com.loco.discover;

import android.Manifest;
import android.app.Dialog;
import android.duke290.com.loco.RatingDialogFragment;
import android.duke290.com.loco.posts.PostDialogFragment;
import android.support.v4.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.duke290.com.loco.R;
import android.duke290.com.loco.cloud.CloudStorageAction;
import android.duke290.com.loco.database.Creation;
import android.duke290.com.loco.database.DatabaseAction;
import android.duke290.com.loco.database.DatabaseFetch;
import android.duke290.com.loco.database.DatabaseFetchCallback;
import android.duke290.com.loco.database.SharedLists;
import android.duke290.com.loco.database.User;
import android.duke290.com.loco.location.Constants;
import android.duke290.com.loco.location.LocationService;
import android.duke290.com.loco.location.ServiceStarter;
import android.duke290.com.loco.photos.PhotosActivity;
import android.duke290.com.loco.photos.PhotosFragment;
import android.duke290.com.loco.posts.PostsActivity;
import android.duke290.com.loco.posts.PostsFragment;
import android.duke290.com.loco.posts.ShareTextActivity;
import android.duke290.com.loco.profile.ProfileActivity;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

/*
 * Everytime onCreate() is called, the activity does the following:
 * Connect to internet -> Gets coordinates -> Gets address -> Displays coordinates/address
 */

public class MainActivity extends AppCompatActivity
        implements DatabaseFetchCallback,
        RatingDialogFragment.RatingDialogListener,
        PostDialogFragment.PostDialogListener {

    private DiscoverFragment mDiscoverFragment;
    private PhotosFragment mPhotosFragment;
    private PostsFragment mPostsFragment;

    private String DISCOVER_FRAG_TAG = "DISCOVER_FRAG_TAG";
    private String PHOTOS_FRAG_TAG = "PHOTOS_FRAG_TAG";
    private String POSTS_FRAG_TAG = "PHOTOS_FRAG_TAG";

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;

    private boolean mAddressRequested = true;

    private AddressResultReceiver mAddressResultReceiver;

    private Location mCurrentLocation;
    private String mAddressOutput;
    private double latitude;
    private double longitude;

    private Creation mCreation;

    private DatabaseFetch databaseFetch;

    private ArrayList<String> mCloudProcessMsgs;

    private CloudResultReceiver mCloudResultReceiver;

    private LocationResultReceiver mLocationResultReceiver;

    private String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private User currentUser;

    final String LATITUDE = "latitude";
    final String LONGITUDE = "longitude";
    final String ADDRESS = "address";

    private TextView mAddressMsg;
    Dialog mBottomSheetDialog;
    TextView mRatingMsg;
    TextView mNumRatingMsg;
    ImageView mRatingImg;

    private String mCurrentPhotoPath;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;

    private String FETCHTYPE = "fetchtype";
    private String INDIVIDUAL = "individual";
    private String SHARED = "shared";

    private int mRating;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            Log.d(TAG, "inserting discover fragment");

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                // restore saved fragment's instance
                if (getSupportFragmentManager().getFragment(
                        savedInstanceState, "DISCOVER_FRAG") != null) {
                    Log.d(TAG, "restoring mDiscoverFragment");
                    mDiscoverFragment =
                            (DiscoverFragment) getSupportFragmentManager().getFragment(
                                    savedInstanceState, "DISCOVER_FRAG");
                }

                if (getSupportFragmentManager().getFragment(
                        savedInstanceState, "PHOTOS_FRAG") != null) {
                    Log.d(TAG, "restoring mPhotosFragment");
                    // restore saved fragment's instance
                    mPhotosFragment =
                            (PhotosFragment) getSupportFragmentManager().getFragment(
                                    savedInstanceState, "PHOTOS_FRAG");
                }

                if (getSupportFragmentManager().getFragment(
                        savedInstanceState, "POSTS_FRAG") != null) {
                    Log.d(TAG, "restoring mPostsFragment");
                    // restore saved fragment's instance
                    mPostsFragment =
                            (PostsFragment) getSupportFragmentManager().getFragment(
                                    savedInstanceState, "POSTS_FRAG");
                }

            } else {
                // load discover fragment
                // Create a new Fragment to be placed in the activity layout
                mDiscoverFragment = new DiscoverFragment();

                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container,
                                mDiscoverFragment,
                                DISCOVER_FRAG_TAG).commit();
            }
        }

        mCloudProcessMsgs = new ArrayList<String>();

        //get firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();

        //Setting the toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.start_toolbar);
        toolbar.setTitle("Discover");
        setSupportActionBar(toolbar);

        navigationView = (NavigationView)findViewById(R.id.navigation_view);

        // get layout variables
        mAddressMsg = (TextView) findViewById(R.id.address_msg);
        mRatingMsg = (TextView) findViewById(R.id.rating_msg);
        mNumRatingMsg = (TextView) findViewById(R.id.num_ratings);
        mRatingImg = (ImageView) findViewById(R.id.rating_faces);

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

        // restore state after orientation change
        if (savedInstanceState != null) {
            // update values from last saved instance
            updateValuesFromBundle(savedInstanceState);
            if (mCurrentLocation != null) {
                updateUI();
            }
        }

//        // restore state after coming from dialog (rating confirmation) fragment
//        if (getIntent() != null) {
//            if (getIntent().getStringExtra("TYPE") != null &&
//                    getIntent().getStringExtra("TYPE").equals("dialog")) {
//                Log.d(TAG, "coming back from dialog fragment");
//                int confirmed = getIntent().getIntExtra("confirmed", 2);
//                mRating = getIntent().getIntExtra("rating", 0);
//                mCurrentLocation = getIntent().getParcelableExtra("LOCATION_KEY");
//
//                if (confirmed == 1) {
//                    confirmReceived();
//                }
//
//                // clear dialog intent variables
//                getIntent().removeExtra("TYPE");
//                getIntent().removeExtra("confirmed");
//                getIntent().removeExtra("rating");
//                getIntent().removeExtra("LOCATION_KEY");
//            }
//        }

    }


    public void showRatingDialog() {
        // Create an instance of the dialog fragment and show it
        RatingDialogFragment dialog = new RatingDialogFragment();
        dialog.show(getSupportFragmentManager(), "RatingDialogFragment");
    }

    public void showPostDialog() {
        // Create an instance of the dialog fragment and show it
        PostDialogFragment dialog = new PostDialogFragment();
        dialog.show(getSupportFragmentManager(), "PostDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onRatingDialogPositiveClick(DialogFragment dialog, double rating) {
        // User confirmed; post rating
        postRating(rating);

    }

    @Override
    public void onRatingDialogNegativeClick(DialogFragment dialog) {
        // User cancelled; do nothing

    }

    @Override
    public void onPostDialogPositiveClick(DialogFragment dialog, String post) {
        // User confirmed; post user's post
        sharePost(post);

    }

    @Override
    public void onPostDialogNegativeClick(DialogFragment dialog) {
        // User cancelled; do nothing

    }

    protected void sharePost(String post) {
        String timestamp = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US).format(new Date());
        Creation creation = new Creation(latitude, longitude, mAddressOutput,
                "text", post, "", 0, timestamp);
        DatabaseAction.putCreationInFirebaseDatabase(creation, latitude, longitude);
        Toast.makeText(getApplicationContext(), "Shared successfully!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Sets up Floating Action Button menu for taking pictures, creating posts,
     * and posting ratings.
     */
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
                    showPostDialog();
                } else if (menuItem.getItemId() == R.id.fab_share_photo) {
                    Log.d(TAG, "share photo button pressed");
                    try {
                        sharePhoto();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (menuItem.getItemId() == R.id.fab_rate) {
                    Log.d(TAG, "rate button pressed");
                    // Show the rating pop-up dialog
//                    mBottomSheetDialog = new Dialog(MainActivity.this, R.style.MaterialDialogSheet);
//                    mBottomSheetDialog.setContentView(R.layout.rating_dialog); // your custom view.
//                    mBottomSheetDialog.setCancelable(true);
//                    mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                    mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
//                    mBottomSheetDialog.show();
                    showRatingDialog();

                }
                return false;
            }
        });

    }

    /**
     * Updates current location and cloud process messages from the last saved instance state.
     * @param savedInstanceState - last saved instance state
     */
    public void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.d(TAG, "updating location values");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains("LOCATION_KEY")) {
                mCurrentLocation = savedInstanceState.getParcelable("LOCATION_KEY");
            }

        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "saving location values");
        savedInstanceState.putParcelable("LOCATION_KEY", mCurrentLocation);
        if (mDiscoverFragment != null &&
                getSupportFragmentManager().findFragmentByTag(DISCOVER_FRAG_TAG) != null) {
            Log.d(TAG, "saving mDiscoverFragment");
            // save fragment instance
            getSupportFragmentManager().putFragment(
                    savedInstanceState, "DISCOVER_FRAG", mDiscoverFragment);
        }

        if (mPhotosFragment != null &&
                getSupportFragmentManager().findFragmentByTag(PHOTOS_FRAG_TAG) != null) {
            Log.d(TAG, "saving mPhotosFragment");
            // save fragment instance
            getSupportFragmentManager().putFragment(
                    savedInstanceState, "PHOTOS_FRAG", mPhotosFragment);
        }

        if (mPostsFragment != null &&
                getSupportFragmentManager().findFragmentByTag(PHOTOS_FRAG_TAG) != null) {
            Log.d(TAG, "saving mPostsFragment");
            // save fragment instance
            getSupportFragmentManager().putFragment(
                    savedInstanceState, "POSTS_FRAG", mPostsFragment);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Updates displayed location, address, and fetches Creations for the current location
     * from the database.
     */
    private void updateUI() {
        updateLocation();
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

                    Log.d(TAG, "starting location service");
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
        Log.d(TAG, "onResume called");
        super.onResume();
        Log.d(TAG, "starting location service");
        ServiceStarter.startLocationService(getApplicationContext(),
                mLocationResultReceiver);

    }

    /**
     * Starts the FetchAddressIntentService to fetch the current address from
     * surrounding geocodes.
     */
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

    /**
     * Class extending ResultReceiver that, when receiving a location result, updates the displayed
     * location, address, and fetches Creation objects for the current location.
     */
    class LocationResultReceiver extends ResultReceiver {
        public LocationResultReceiver(Handler handler) { super(handler); }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mCurrentLocation = resultData.getParcelable("LOCATION_KEY");
            getAddress();
            updateLocation();
            getCreations();
        }

    }

    /**
     * Class extending ResultReceiver that, when receiving an address result, updates the
     * displayed address.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            // edit address string so that it is just two lines
            String edited_adr = "";
            int cnt = 0;
            for (int k1 = 0; k1 < mAddressOutput.length(); k1++) {
                if (mAddressOutput.charAt(k1) == 10) {
                    cnt++;
                    if (cnt > 1) {
                        edited_adr += ", ";
                    } else {
                        edited_adr += (char) 10;
                    }
                } else {
                    edited_adr += mAddressOutput.charAt(k1);
                }
            }

            mAddressOutput = edited_adr;
            displayAddressOutput();

            Log.d(TAG, "address found");

        }
    }

    /**
     * Class extending ResultReceiver that, when receiving a result from CloudStorageAction, uploads
     * the Creation corresponding to the uploaded item to Firebase Storage, and displays the cloud
     * process output.
     */
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

    /**
     * Updates the latitude and longitude global variables.
     */
    public void updateLocation() {
        if (mCurrentLocation != null) {
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();
        }
    }

    /**
     * Displays the address on the UI.
     */
    protected void displayAddressOutput() {
        mAddressMsg.setText(mAddressOutput);
    }

    /**
     * Displays the cloud process message from the last cloud action via a toast.
     */
    protected void displayProcessOutput() {
        if (mCloudProcessMsgs.size() == 0) return;
        Log.d(TAG, "displayProcessOutput called");
        Toast.makeText(this, mCloudProcessMsgs.get(mCloudProcessMsgs.size() - 1),
                Toast.LENGTH_LONG).show();
    }

    /**
     * Calls the CloudStorageAction class to upload an InputStream to Firebase Storage.
     *
     * @param inputStream - InputStream to upload to Firebase Storage
     * @param storage_path - String representing the path in Storage that the InputStream will
     *                     be located in
     * @param content_type - String representing the type of content being uploaded (text, image,
     *                     or rating)
     */
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

    /**
     * Calls the ShareTextActivity class to share a text Creation object.
     */
    protected void shareText() {
        Intent intent = new Intent(MainActivity.this, ShareTextActivity.class);
        intent.putExtra(LATITUDE, latitude);
        intent.putExtra(LONGITUDE, longitude);
        intent.putExtra(ADDRESS, mAddressOutput);
        startActivity(intent);
    }

    /**
     * Allows user to take a photo, which will then be handled by onActivityResult.
     *
     * @exception IOException - IOException for creating a file
     */
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

    /**
     * Creates and uploads a Creation object holding the user's posted rating.
     *
     * @param rating - the user's posted rating.
     */
    public void postRating(double rating) {
        // creating creation
        String timestamp = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US).format(new Date());

        Log.d(TAG, "Posted rating: " + rating);

        mCreation = new Creation(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(), mAddressOutput,
                "rating", "", "", rating, timestamp);

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
                    fullsizeBitmap.compress(Bitmap.CompressFormat.JPEG, 25 ,bos);
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

    /**
     *  Creating a unique filename for the uploaded image with timestamp.
     *
     *  @exception - IOException - for creating a file
     */
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

//    /**
//     * Opens dialog (ConfirmDialogFragment) to confirm rating when user chooses a rating.
//     *
//     * @param button - Button that user clicked on to select rating.
//     */
//    public void onRatingClick(View button) {
//        Log.d(TAG, "here about to open dialog");
//        String button_id = getResources().getResourceName(button.getId());
//        mRating = button_id.charAt(button_id.length() - 1) - '0';
////        openDialog();
//    }

    /**
     * Called when ConfirmDialogFragment sends intent to MainActivity and MainActivity restores
     * its previous state (location and cloud process messages). Calls postRating to post rating.
     *
     */
    public void confirmReceived(){
        postRating(mRating);
    }

//    /**
//     * Calls ConfirmDialogFragment to open dialog to confirm rating.
//     */
//    public void openDialog() {
//        DialogFragment confirmation = new ConfirmDialogFragment();
//        Log.d(TAG, "opening dialog");
//        Bundle args = new Bundle();
//        args.putInt("rating", mRating);
//        if (mCurrentLocation != null) {
//            args.putParcelable("LOCATION_KEY", mCurrentLocation);
//            confirmation.setArguments(args);
//            confirmation.show(getFragmentManager(), "");
//        }
//    }

    /**
     * Calls fetchByCoordinate to get Creation objects for the current location.
     */
    public void getCreations(){
        if (mCurrentLocation != null) {
            String coordname = DatabaseAction.createCoordName(mCurrentLocation);
            databaseFetch.fetchByCoordinate(coordname);
        }
    }

    /**
     * Starts the PostActivity class to display all posts made at the current location.
     * @param view - the button that activates this method
     */
    public void onMorePostsClick(View view){
//        Intent intent = new Intent(MainActivity.this, PostsActivity.class);
//        intent.putExtra(FETCHTYPE,SHARED);
//        startActivity(intent);
        mPostsFragment = new PostsFragment();

        // store values
        Bundle args = new Bundle();
        args.putString("fetchtype", SHARED);
        mPostsFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.fragment_container, mPostsFragment, POSTS_FRAG_TAG);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        Log.d(TAG, "onMorePostsClick: mPostsFragment == null?: " + (mPostsFragment == null));
    }

    /**
     * Starts the PhotosActivity class to display all photos made at the current location.
     * @param view - the button that activates this method
     */
    public void onMorePhotosClick(View view){
//        Intent intent = new Intent(MainActivity.this, PhotosActivity.class);
//        intent.putExtra(FETCHTYPE,SHARED);
//        startActivity(intent);
        mPhotosFragment = new PhotosFragment();

        // store values
        Bundle args = new Bundle();
        args.putString("fetchtype", SHARED);
        mPhotosFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.fragment_container, mPhotosFragment, PHOTOS_FRAG_TAG);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        Log.d(TAG, "onMorePhotosClick: mDiscoverFragment == null?: " + (mDiscoverFragment == null));

    }

    /**
     * Calls showMap() to display the user's current location.
     * @param view - the button that activates this method
     */
    public void onMapClick(View view){
        Uri gmmIntentUri = Uri.parse("geo:" +latitude +","+longitude);
        showMap(gmmIntentUri);
    }

    /**
     * Calls the phone's default map application to display the user's current location.
     * @param geoLocation - Uri that contains the user's current location.
     */
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

        // process creations
        storeCreations(creations);

        Log.d(TAG, "mDiscoverFragment == null?: " + (mDiscoverFragment == null));
        if (mDiscoverFragment != null) {
            Log.d(TAG, "mDiscoverFragment visible?: " + (mDiscoverFragment.isVisible()));
        }
        if (mDiscoverFragment != null && mDiscoverFragment.isVisible()) {
            // fill up discover fragment with creations
            Log.d(TAG, "database updated; updating discover fragment");
            mDiscoverFragment.processCreations();
        }

        if (mPhotosFragment != null && mPhotosFragment.isVisible()) {
            Log.d(TAG, "database updated; updating photos fragment");
            mPhotosFragment.processCreations(SHARED);
        }

        if (mPostsFragment != null && mPostsFragment.isVisible()) {
            Log.d(TAG, "database updated; updating posts fragment");
            mPostsFragment.processCreations(SHARED);
        }


    }

    private void storeCreations(ArrayList<Creation> creations) {
        ArrayList<Creation> messagecreations = new ArrayList<>();
        ArrayList<Creation> image_creation_list = new ArrayList<Creation>();
        ArrayList<Creation> rating_creation_list = new ArrayList<Creation>();

        for (Creation c : creations) {
            if (c.type.equals("text")) {
                messagecreations.add(c);
            }
            if (c.type.equals("image")) {
                image_creation_list.add(c);
            }
            if (c.type.equals("rating")) {
                Log.d(TAG, "received rating: " + c.rating);
                rating_creation_list.add(c);
            }
        }

        // set shared lists for other activities/fragments
        SharedLists.getInstance().setMessageCreations(messagecreations);
        SharedLists.getInstance().setImageCreations(image_creation_list);
        SharedLists.getInstance().setRatingCreations(rating_creation_list);

        Log.d(TAG, "creations stored");
    }

    @Override
    public void onUserReceived(User user){
        currentUser = user;
        setUserText();
    }

    /**
     * Sets the user related info on the navigation drawer.
     */
    private void setUserText(){
        View header = navigationView.getHeaderView(0);
        TextView user_name = (TextView)header.findViewById(R.id.user_name);
        TextView user_email = (TextView)header.findViewById(R.id.user_email);
        user_name.setText(currentUser.name);
        user_email.setText(currentUser.email);
    }

    /**
     * Initializes the navigation drawer.
     */
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
                        Intent profile_intent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(profile_intent);
                        //startActivity(new Intent(MainActivity.this, ProfileActivity.class));
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

    /**
     * Calls the PhotosActivity class to display the user's own photos.
     */
    private void onMyPhotosClick(){
        Intent intent = new Intent(MainActivity.this, PhotosActivity.class);
        intent.putExtra(FETCHTYPE,INDIVIDUAL);
        startActivity(intent);

    }

    /**
     * Calls the PostsActivity class to display the user's own posts.
     */
    private void onMyPostsClick(){
        Intent intent = new Intent(MainActivity.this, PostsActivity.class);
        intent.putExtra(FETCHTYPE,INDIVIDUAL);
        startActivity(intent);
    }

}


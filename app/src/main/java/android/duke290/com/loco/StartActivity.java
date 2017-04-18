package android.duke290.com.loco;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

/*
 * Everytime onCreate() is called, the activity does the following:
 * Connect to internet -> Gets coordinates -> Gets address -> Displays coordinates/address
 */

public class StartActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;

    // for now, this is always true, but we can change it if needed
    private boolean mAddressRequested = true;

    private AddressResultReceiver mAddressResultReceiver;

    private Location mCurrentLocation;
    private String mAddressOutput;

    private ArrayList<String> mCloudProcessMsgs;
    private ArrayList<InputStream> mCloudDownloadedStreams;
    private ArrayList<String> mCloudDownloadedContentTypes;

    private CloudResultReceiver mCloudResultReceiver;

    private LocationResultReceiver mLocationResultReceiver;

    private String TAG = "StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

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

    }

    public void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.d(TAG, "updating location values");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains("LOCATION_KEY")) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocation is not null.
                mCurrentLocation = savedInstanceState.getParcelable("LOCATION_KEY");
            }
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "saving location values");
        savedInstanceState.putParcelable("LOCATION_KEY", mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
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
//        startLocationService();
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
//                startAddressIntentService();
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

            if (resultData.getByteArray("CLOUD_DOWNLOADED_BYTE_ARRAY_KEY") != null) {
                Log.d(TAG, "Cloud downloaded stream not null");
                mCloudDownloadedStreams.add(new ByteArrayInputStream(
                        resultData.getByteArray("CLOUD_DOWNLOADED_BYTE_ARRAY_KEY")));
            }

            mCloudDownloadedContentTypes.add(resultData.getString("CLOUD_DOWNLOADED_CONTENT_TYPE"));
            Log.d(TAG, "Downloaded content type: " + resultData.getString("CLOUD_DOWNLOADED_CONTENT_TYPE"));

            displayCloudOutput();

            Log.d(TAG, "Cloud process finished");

        }
    }

    public void displayLocation() {
        String latitude = String.valueOf(mCurrentLocation.getLatitude());
        String longitude = String.valueOf(mCurrentLocation.getLongitude());
        Log.d(TAG, "Latitude: " + latitude + ", " + "Longitude: " + longitude);
        TextView location_msg = (TextView) findViewById(R.id.location_msg);
        location_msg.setText("Latitude: " + latitude + ", Longitude: " + longitude);

    }

    protected void displayAddressOutput() {
        TextView address_msg = (TextView) findViewById(R.id.address_msg);
        address_msg.setText(mAddressOutput);
    }

    /*
     * must be called once stuff is received, i.e. stuff inside this method cannot be put in
     * another separate method unless it is called by onReceiveResult
     */
    protected void displayCloudOutput() {
        Log.d(TAG, "displayCloudOutput called");
        TextView process_msg = (TextView) findViewById(R.id.process_msg);

        ImageView downloaded_img_1 = (ImageView) findViewById(R.id.photo1);
        ImageView downloaded_img_2 = (ImageView) findViewById(R.id.photo2);

        // ***Important*** reset inputstreams (inputstreams need to be reset after use)

        for (InputStream is : mCloudDownloadedStreams) {
            try {
                is.reset();
            } catch (IOException e) {
                Log.d(TAG, "IOException while reseting inputstreams");
            }
        }

        process_msg.setText(mCloudProcessMsgs.get(mCloudProcessMsgs.size() - 1));

        Log.d(TAG, "mCloudDownloadedStreams size: " +
                (mCloudDownloadedStreams.size()));

        // read file, save to downloaded_msg
        if (mCloudDownloadedStreams.size() >= 1) {
            if (mCloudDownloadedContentTypes.get(0).equals("image")) {
                Log.d(TAG, "Displaying cloud downloaded image (1)");
                downloaded_img_1.setImageBitmap(
                        BitmapFactory .decodeStream(mCloudDownloadedStreams.get(0)));
            }

            if (mCloudDownloadedStreams.size() >= 2) {
                if (mCloudDownloadedContentTypes.get(1).equals("image")) {
                    Log.d(TAG, "Displaying cloud downloaded image (2)");
                    downloaded_img_2.setImageBitmap(
                            BitmapFactory .decodeStream(mCloudDownloadedStreams.get(1)));
                }
            }
        }
    }

    protected void resetReceivedCloudItems() {
        mCloudProcessMsgs = new ArrayList<String>();
        mCloudDownloadedStreams = new ArrayList<InputStream>();
        mCloudDownloadedContentTypes = new ArrayList<String>();
    }

    protected void uploadStreamToFirebaseStorage(InputStream inputStream,
                                                 String storage_path,
                                                 String content_type) {
        Log.d(TAG, "uploading stream to firebase storage (" + content_type + ")");
        ServiceStarter.startCloudIntentService("upload",
                inputStream,
                storage_path,
                content_type,
                getApplicationContext(),
                mCloudResultReceiver);
    }

    /*
     * downloaded stream is put in mCloudDownloadedStream
     */
    protected void downloadStreamFromFirebaseStorage(String storage_path) {
        Log.d(TAG, "downloading stream from firebase storage");
        ServiceStarter.startCloudIntentService("download",
                null,
                storage_path,
                "",
                getApplicationContext(),
                mCloudResultReceiver);
    }

    protected void uploadClick(View button) {
        Log.d(TAG, "upload button clicked");
        resetReceivedCloudItems();

        String image_storage_path = DatabaseAction.createImageStoragePath();

        Creation new_reference = new Creation(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(), "420 Chapel Drive",
                "image", "cool beans 3", image_storage_path);

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

    protected void getClick(View button) {
        Log.d(TAG, "get button clicked");
        resetReceivedCloudItems();

        DatabaseReference ref = DatabaseAction.getDatabaseReferenceForGet(mCurrentLocation);

        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        collectCreations((Map<String,Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

    }

    private void collectCreations(Map<String,Object> creations) {

        ArrayList<String> outputMessageList = new ArrayList<String>();

        ArrayList<String> storage_path_list = new ArrayList<String>();

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : creations.entrySet()){

            //Get user map
            Map singleCreation = (Map) entry.getValue();

            Log.d(TAG, "received creation");
            //Get phone field and append to list
            Log.d(TAG, "adding message: " + singleCreation.get("message").toString());
            outputMessageList.add(singleCreation.get("message").toString());

            if (singleCreation.get("type").equals("image")) {
                storage_path_list.add(singleCreation.get("extra_storage_path").toString());
            }
        }

        Log.d(TAG, "storage paths found");

        downloadStreamFromFirebaseStorage(storage_path_list.get(0));
        downloadStreamFromFirebaseStorage(storage_path_list.get(1));

        displayOutputMessages(outputMessageList);

    }

    /*
     * As messages were never stored in the cloud, they are already available once collectCreations
     * finishes collecting Creations
     */
    private void displayOutputMessages(ArrayList<String> list) {
        TextView out_msgs = (TextView) findViewById(R.id.user1post);

        String concat_output_messages = "";
        for (int k1 = 0; k1 < list.size(); k1++) {
            if (k1 > 0) {
                concat_output_messages += ", ";
            }
            concat_output_messages += list.get(k1);
        }

        out_msgs.setText(concat_output_messages);

    }



}

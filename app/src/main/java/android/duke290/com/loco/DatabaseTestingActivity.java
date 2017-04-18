package android.duke290.com.loco;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;


public class DatabaseTestingActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;

    private String mCloudProcessMsg;

    private InputStream mCloudDownloadedStream;
    private String mCloudDownloadedContentType;
    private CloudResultReceiver mCloudResultReceiver;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private LocationResultReceiver mLocationResultReceiver;
    private Location mCurrentLocation;

    private Button uploadButton, getButton;

    final String USERS = "users";
    final String CONTENT = "content";
    final String CREATIONS = "creations";

    final String decimalRoundTo = "3";

    final String TAG = "DatabaseTestingActivity";

    ArrayList<String> outputCreationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_testing);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        // request location permissions if necessary
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);

            return;
        }

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

                    startLocationService();

                } else {
                    Log.d(TAG, "permission not granted");
                }
                return;
            }

        }
    }

    protected void resetReceivedCloudItems() {
        mCloudProcessMsg = "";
        mCloudDownloadedStream = null;
        mCloudDownloadedContentType = "";
    }




    protected void databaseTestingUploadClick(View button) {
        Log.d(TAG, "upload button clicked");
        //get current user
        final FirebaseUser this_user = mAuth.getCurrentUser();

        String uid = this_user.getUid();

        Long currentTime = System.currentTimeMillis();

        DatabaseReference ref = mDatabase.getReference(USERS).child(uid).child(CONTENT);

        String image_storage_path = "images/" + "img" + uid + currentTime;

        Creation new_reference = new Creation(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(), "420 Chapel Drive",
                "image", "cool beans 3", image_storage_path);

        // upload file

        resetReceivedCloudItems();
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

        Log.d(TAG, "reference for " + mCurrentLocation.getLatitude()
                + mCurrentLocation.getLongitude() + "created");

        ref.child("ref" + currentTime).setValue(new_reference);
        Log.d(TAG, "reference uploaded");

        String coordName = createCoordName();

        DatabaseReference refCreationList = mDatabase.getReference(CREATIONS).child(coordName);

        refCreationList.child("cre" + uid + "" + currentTime).setValue(new_reference);
    }

    protected void databaseTestingGetClick(View button) {
        String coordNameLookup = createCoordName();
        DatabaseReference ref = mDatabase.getReference(CREATIONS).child(coordNameLookup);

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

        outputCreationList = new ArrayList<String>();

        String storage_path = "";

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : creations.entrySet()){

            //Get user map
            Map singleCreation = (Map) entry.getValue();

            Log.d(TAG, "received creation");
            //Get phone field and append to list
            Log.d(TAG, "adding message: " + singleCreation.get("message").toString());
            outputCreationList.add(singleCreation.get("message").toString());

            if (singleCreation.get("type").equals("image")) {
                storage_path = singleCreation.get("extra_storage_path").toString();
            }
        }

        Log.d(TAG, "storage path found: " + storage_path);

        downloadStreamFromFirebaseStorage(storage_path);

        displayCloudOutput();

        displayOutputMessages();

    }

    protected void displayOutputMessages() {
        TextView messages_txts = (TextView) findViewById(R.id.Database_testing_messages);

        String concat_output_messages = "";
        for (int k1 = 0; k1 < outputCreationList.size(); k1++) {
            if (k1 > 0) {
                concat_output_messages += ", ";
            }
            concat_output_messages += outputCreationList.get(k1);
        }
        messages_txts.setText(concat_output_messages);

    }



    private String createCoordName() {
        String latitudeStr = String.format("%."+ decimalRoundTo + "f", mCurrentLocation.getLatitude());
        latitudeStr = latitudeStr.replace(".", ",");
        String longitudeStr = String.format("%."+ decimalRoundTo + "f", mCurrentLocation.getLongitude());
        longitudeStr = longitudeStr.replace(".", ",");
        String coordName = "coord" + latitudeStr + ";" +
                longitudeStr;

        return coordName;
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
        startLocationService();
    }

    protected void displayCloudOutput() {
        TextView process_msg = (TextView) findViewById(R.id.Database_testing_process_msg);
        ImageView downloaded_img = (ImageView) findViewById(R.id.Database_testing_image);

        process_msg.setText(mCloudProcessMsg);

        // read file, save to downloaded_msg
        if (mCloudDownloadedStream != null) {

            if (mCloudDownloadedContentType.equals("image")) {
                Log.d(TAG, "Displaying cloud downloaded image");
                downloaded_img.setImageBitmap(
                        BitmapFactory.decodeStream(mCloudDownloadedStream));
            }
        }

    }

    protected void startLocationService() {
        Log.d(TAG, "starting location service");
        mLocationResultReceiver = new DatabaseTestingActivity.LocationResultReceiver(new Handler());
        Intent intent = new Intent(this, LocationService.class);
        intent.putExtra("LOCATION_RECEIVER", mLocationResultReceiver);
        startService(intent);
    }

    class LocationResultReceiver extends ResultReceiver {
        public LocationResultReceiver(Handler handler) { super(handler); }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.d(TAG, "location received");
            mCurrentLocation = resultData.getParcelable("LOCATION_KEY");
        }

    }

    protected void startCloudIntentService(String action,
                                           InputStream local_stream,
                                           String storage_path,
                                           String content_type) {
        Log.d(TAG, "starting cloud intent service");
        mCloudResultReceiver = new DatabaseTestingActivity.CloudResultReceiver(new Handler());
        Intent intent = new Intent(this, CloudStorageService.class);
        intent.putExtra("CLOUD_STORAGE_OPTION", action);
        intent.putExtra("CLOUD_STORAGE_RECEIVER", mCloudResultReceiver);

        byte[] uplded_b_ar = null;

        if (local_stream != null) {
            try {
                uplded_b_ar = IOUtils.toByteArray(local_stream);
            } catch (IOException e) {
                Log.d(TAG, "IOException when converting downloaded input stream to byte array");
            }
        } else {
            Log.d(TAG, "local_stream to upload is missing (ok if downloading something)");
        }

        intent.putExtra("CLOUD_STORAGE_LOCAL_BYTE_ARRAY", uplded_b_ar);
        intent.putExtra("CLOUD_STORAGE_STORAGE_PATH", storage_path);
        intent.putExtra("CLOUD_STORAGE_CONTENT_TYPE", content_type);

        startService(intent);
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
            mCloudProcessMsg = resultData.getString("CLOUD_PROCESS_MSG_KEY");

            if (resultData.getByteArray("CLOUD_DOWNLOADED_BYTE_ARRAY_KEY") != null) {
                Log.d(TAG, "Cloud downloaded stream not null");
                mCloudDownloadedStream = new ByteArrayInputStream(
                        resultData.getByteArray("CLOUD_DOWNLOADED_BYTE_ARRAY_KEY"));
            }

            mCloudDownloadedContentType = resultData.getString("CLOUD_DOWNLOADED_CONTENT_TYPE");
            Log.d(TAG, "Downloaded content type: " + mCloudDownloadedContentType);

            displayCloudOutput();

            Log.d(TAG, "Cloud process finished");

        }
    }

    protected void uploadStreamToFirebaseStorage(InputStream inputStream,
                                                 String storage_path,
                                                 String content_type) {
        Log.d(TAG, "uploading stream to firebase storage (" + content_type + ")");
        startCloudIntentService("upload", inputStream, storage_path, content_type);
    }

    /*
     * downloaded stream is put in mCloudDownloadedStream
     */
    protected void downloadStreamFromFirebaseStorage(String storage_path) {
        Log.d(TAG, "downloading stream from firebase storage");
        startCloudIntentService("download", null, storage_path, "");
    }

}

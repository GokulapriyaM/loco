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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

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

    private String mCloudProcessMsg;
    private InputStream mCloudDownloadedStream;
    private String mCloudDownloadedContentType;

    private CloudResultReceiver mCloudResultReceiver;

    private LocationResultReceiver mLocationResultReceiver;

    private String TAG = "StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // update values from last saved instance
        updateValuesFromBundle(savedInstanceState);

        // request location permissions if necessary
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);

            return;
        }

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

                    startLocationService();

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

    protected void getAddress() {
        if (mCurrentLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, "no geocoder available",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (mAddressRequested) {
                startAddressIntentService();
            }
        }
    }

    protected void startAddressIntentService() {
        mAddressResultReceiver = new AddressResultReceiver(new Handler());
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mAddressResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);
        startService(intent);
    }

    protected void startLocationService() {
        mLocationResultReceiver = new LocationResultReceiver(new Handler());
        Intent intent = new Intent(this, LocationService.class);
        intent.putExtra("LOCATION_RECEIVER", mLocationResultReceiver);
        startService(intent);
    }

    protected void startCloudIntentService(String action,
                                           InputStream local_stream,
                                           String storage_path,
                                           String content_type) {
        Log.d(TAG, "starting cloud intent service");
        mCloudResultReceiver = new CloudResultReceiver(new Handler());
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

    protected void displayCloudOutput() {
        TextView process_msg = (TextView) findViewById(R.id.process_msg);
        TextView downloaded_msg = (TextView) findViewById(R.id.downloaded_msg);
        ImageView downloaded_img = (ImageView) findViewById(R.id.downloaded_image);

        process_msg.setText(mCloudProcessMsg);

        // read file, save to downloaded_msg
        if (mCloudDownloadedStream != null) {

            if (mCloudDownloadedContentType.equals("text")) {
                Log.d(TAG, "Displaying cloud downloaded text");
                String msg = "";
                try {
                    Scanner s = new Scanner(mCloudDownloadedStream).useDelimiter("\\A");
                    msg = s.hasNext() ? s.next() : "";
                    Log.d(TAG, "Downloaded message:" + msg);

                    mCloudDownloadedStream.close();

                    downloaded_msg.setText(msg);

                } catch (IOException e) {
                    Log.d(TAG, "InputStream creation from downloaded file failed");
                }
            } else if (mCloudDownloadedContentType.equals("image")) {
                Log.d(TAG, "Displaying cloud downloaded image");
                downloaded_img.setImageBitmap(
                        BitmapFactory.decodeStream(mCloudDownloadedStream));
            }
        }

    }

    protected void resetReceivedCloudItems() {
        mCloudProcessMsg = "";
        mCloudDownloadedStream = null;
        mCloudDownloadedContentType = "";
    }

    protected void uploadTextClick(View button) {
        resetReceivedCloudItems();
        AssetManager assetManager = getAssets();
        InputStream textStream = null;

        try {
            textStream = assetManager.open("Text/message.txt");
        } catch (IOException e) {
            Log.d(TAG, "IOException opening local file");
        }

        uploadStreamToFirebaseStorage(textStream,
                "Coordinates/123/message.txt",
                "text");
    }

    protected void downloadTextClick(View button) {
        resetReceivedCloudItems();
        downloadStreamFromFirebaseStorage("Coordinates/123/message.txt");
    }

    protected void uploadImageClick(View button) {
        resetReceivedCloudItems();
        AssetManager assetManager = getAssets();
        InputStream imageStream = null;

        try {
            imageStream = assetManager.open("Images/testimage.png");
        } catch (IOException e) {
            Log.d(TAG, "IOException opening local file");
        }

        uploadStreamToFirebaseStorage(imageStream,
                "Coordinates/123/image.gif",
                "image");
    }

    protected void downloadImageClick(View button) {
        resetReceivedCloudItems();
        downloadStreamFromFirebaseStorage("Coordinates/123/image.gif");
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

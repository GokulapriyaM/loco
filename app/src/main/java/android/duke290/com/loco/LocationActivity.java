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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/*
 * Everytime onCreate() is called, the activity does the following:
 * Connect to internet -> Gets coordinates -> Gets address -> Displays coordinates/address
 */

public class LocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener, LocationListener {

    GoogleApiClient mGoogleApiClient = null;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;

    // for now, these are always true, but we can change them if needed
    private boolean mRequestingLocationUpdates = true;
    private boolean mAddressRequested = true;

    private LocationRequest mLocationRequest;
    private AddressResultReceiver mAddressResultReceiver;

    private Location mCurrentLocation;
    private String mAddressOutput;

    private String mCloudProcessMsg;
    private InputStream mCloudDownloadedStream;
    private String mCloudDownloadedContentType;

    private CloudResultReceiver mCloudResultReceiver;

    private String TAG = "LocationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // update values from last saved instance
        updateValuesFromBundle(savedInstanceState);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                    .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // request location permissions if necessary
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);

            return;
        }

    }

    public void updateValuesFromBundle(Bundle savedInstanceState) {
        System.out.println("updating location values");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains("REQUESTING_LOCATION_UPDATES_KEY")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        "REQUESTING_LOCATION_UPDATES_KEY");
            }

            if (savedInstanceState.keySet().contains("LOCATION_KEY")) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mCurrentLocation = savedInstanceState.getParcelable("LOCATION_KEY");
            }
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println("saving location values");
        savedInstanceState.putBoolean("REQUESTING_LOCATION_UPDATES_KEY",
                mRequestingLocationUpdates);
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
                    System.out.println("permission granted");

                    if (mGoogleApiClient.isConnected()) {
                        System.out.println("starting location updates after permissions");
                        startLocationUpdates();
                    }

                } else {

                    System.out.println("permission not granted");
                }
                return;
            }

        }
    }

    @Override
    protected void onStart() {
        System.out.println("onStart called");
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        System.out.println("onStop called");
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        System.out.println("onPause called");
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (mRequestingLocationUpdates) {
            mLocationRequest = new LocationRequest();
            startLocationUpdates();
        }

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

    protected void startLocationUpdates() {
        System.out.println("startLocationUpdates called");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    /*
     * Actions for when a new location is received
     */
    public void onLocationChanged(Location location) {
        System.out.println("onLocationChanged called");
        mCurrentLocation = location;
        getAddress();
        displayLocation();
    }

    public void displayLocation() {
        String latitude = String.valueOf(mCurrentLocation.getLatitude());
        String longitude = String.valueOf(mCurrentLocation.getLongitude());
        System.out.println("Latitude: " + latitude);
        System.out.println("Longitude: " + longitude);
        TextView location_msg = (TextView) findViewById(R.id.location_msg);
        location_msg.setText("Latitude: " + latitude + ", Longitude: " + longitude);

    }



    public void onDisconnected() {

    }

    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void onConnectionSuspended(int cause) {

    }

    protected void startAddressIntentService() {
        mAddressResultReceiver = new AddressResultReceiver(new Handler());
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mAddressResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);
        startService(intent);
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

            System.out.println("address found");

        }
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

    protected InputStream getAssetsFile(String file_path) {
        AssetManager assetManager = getAssets();

        InputStream inputStream = null;

        try {
            inputStream = assetManager.open("Text/message.txt");
        } catch (IOException e) {
            Log.d(TAG, "Assets file not found");
        }

        if (inputStream != null) {
            Log.d(TAG, "Assets file found!");
        }

        return inputStream;

    }
}

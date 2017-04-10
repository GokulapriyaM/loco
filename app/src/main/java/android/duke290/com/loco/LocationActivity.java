package android.duke290.com.loco;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

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
    private AddressResultReceiver mResultReceiver;

    private Location mCurrentLocation;
    private String mAddressOutput;

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
                startIntentService();
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

    protected void startIntentService() {
        mResultReceiver = new AddressResultReceiver(new Handler());
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
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

    protected void displayAddressOutput() {
        TextView address_msg = (TextView) findViewById(R.id.address_msg);
        address_msg.setText(mAddressOutput);
    }
}

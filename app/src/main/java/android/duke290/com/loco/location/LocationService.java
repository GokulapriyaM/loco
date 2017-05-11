package android.duke290.com.loco.location;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Service that gets the location of the device in coordinates.
 */

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    String TAG = "LocationService";

    ResultReceiver mReceiver;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    Location mPastLocation;

    private static int UPDATE_INTERVAL = 1000 * 5; // /< location update interval
    private static int FASTEST_INTERVAL = 1000 * 5; // /< fastest location update interval

    private static double CLOSE_DISTANCE = 0.0002;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate called");

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        mGoogleApiClient.connect();

        mReceiver = intent.getParcelableExtra("LOCATION_RECEIVER");

        return START_STICKY;
    }

    /**
     * Delivers location result to receiver.
     *
     * @param resultCode - code indicating success or failure
     * @param location - Location object
     *
     */
    public void deliverResultToReceiver(int resultCode, Location location) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("LOCATION_KEY", location);
        mReceiver.send(resultCode, bundle);
    }

    /**
     * Called when location of the device changes.
     *
     * @param location - Location object indicating the current location of the device.
     */
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged called");
        Log.d(TAG, "location accuracy: " + location.getAccuracy());
        Log.d(TAG, "latitude: " + location.getLatitude()
                + ", longitude: " + location.getLongitude());

        if (mPastLocation == null ||
                !isClose(mPastLocation, location)) {
            mPastLocation = location;
            Log.d(TAG, "delivering location result");
            deliverResultToReceiver(Constants.SUCCESS_RESULT, location);
        } else {
            Log.d(TAG, "not delivering location result");
        }
    }

    /**
     * Indicates whether or not two locations are close based on
     * the CLOSE_DISTANCE global variable.
     *
     * @param loc1 - Location object
     * @param loc2 - Location object
     * @return True if the locations are close, false otherwise.
     */
    public boolean isClose(Location loc1 , Location loc2) {
        double latitude_diff = Math.abs(loc1.getLatitude() - loc2.getLatitude());
        double longitude_diff = Math.abs(loc1.getLongitude() - loc2.getLongitude());
        Log.d(TAG, "latitude_diff = " + latitude_diff + ", longitude_diff = " + longitude_diff);
        return latitude_diff <= CLOSE_DISTANCE && longitude_diff <= CLOSE_DISTANCE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mGoogleApiClient.disconnect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setInterval(FASTEST_INTERVAL);
        startLocationUpdates();

    }

    /**
     * Makes the service start checking for the device's location.
     */
    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates called");
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Called when google api client connection fails.
     * @param connectionResult
     */
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Called when google api client connection is suspended.
     * @param cause
     */
    public void onConnectionSuspended(int cause) {

    }
}

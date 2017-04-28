package android.duke290.com.loco.location;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    String TAG = "LocationService";

    ResultReceiver mReceiver;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    Location mPastLocation;

    private static int UPDATE_INTERVAL = 1000 * 5; // /< location update interval
    private static int FASTEST_INTERVAL = 1000 * 5; // /< fastest location update interval

    private static double CLOSE_DISTANCE = 0.00001;

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate called");

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                    .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        mGoogleApiClient.connect();

        mReceiver = intent.getParcelableExtra("LOCATION_RECEIVER");

        mPastLocation = null;

        return START_STICKY;
    }

    public void deliverResultToReceiver(int resultCode, Location location) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("LOCATION_KEY", location);
        mReceiver.send(resultCode, bundle);
    }

    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged called");
        Log.d(TAG, "location accuracy: " + location.getAccuracy());

        if (mPastLocation == null ||
                !isClose(mPastLocation, location)) {
            Log.d(TAG, "delivering location result");
            deliverResultToReceiver(Constants.SUCCESS_RESULT, location);
        } else {
            Log.d(TAG, "not delivering location result");
        }
    }

    public boolean isClose(Location loc1 , Location loc2) {
        if (Math.abs(loc1.getLatitude() - loc2.getLatitude()) <= CLOSE_DISTANCE ||
                Math.abs(loc1.getLongitude() - loc2.getLongitude()) <= CLOSE_DISTANCE) {
            return true;
        }
        return false;
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

    protected void startLocationUpdates() {
        System.out.println("startLocationUpdates called");
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void onConnectionSuspended(int cause) {

    }
}

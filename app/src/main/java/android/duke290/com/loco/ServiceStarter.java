package android.duke290.com.loco;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.ResultReceiver;

/**
 * Created by kevinkuo on 4/18/17.
 */

public class ServiceStarter {

    final static String TAG = "ServiceStarter";

    protected static void startAddressIntentService(Context context, ResultReceiver receiver, Location current_location) {
        Intent intent = new Intent(context, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, receiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, current_location);
        context.startService(intent);
    }

    protected static void startLocationService(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, LocationService.class);
        intent.putExtra("LOCATION_RECEIVER", receiver);
        context.startService(intent);
    }

}

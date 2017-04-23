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

//    protected static void startCloudIntentService(String action,
//                                           InputStream local_stream,
//                                           String storage_path,
//                                           String content_type,
//                                           Context context,
//                                           ResultReceiver receiver) {
//        Log.d(TAG, "starting cloud intent service");
//        Intent intent = new Intent(context, CloudStorageService.class);
//        intent.putExtra("CLOUD_STORAGE_OPTION", action);
//        intent.putExtra("CLOUD_STORAGE_RECEIVER",receiver);
//
//        byte[] uplded_b_ar = null;
//
//        if (local_stream != null) {
//            try {
//                uplded_b_ar = IOUtils.toByteArray(local_stream);
//            } catch (IOException e) {
//                Log.d(TAG, "IOException when converting downloaded input stream to byte array");
//            }
//        } else {
//            Log.d(TAG, "local_stream to upload is missing (ok if downloading something)");
//        }
//
//        intent.putExtra("CLOUD_STORAGE_LOCAL_BYTE_ARRAY", uplded_b_ar);
//        intent.putExtra("CLOUD_STORAGE_STORAGE_PATH", storage_path);
//        intent.putExtra("CLOUD_STORAGE_CONTENT_TYPE", content_type);
//
//        context.startService(intent);
//    }




}

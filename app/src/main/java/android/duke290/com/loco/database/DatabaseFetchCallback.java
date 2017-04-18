package android.duke290.com.loco.database;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public interface DatabaseFetchCallback {
    void onDatabaseResultReceived(ArrayList<String> messages, ArrayList<StorageReference> storagerefs);
}

package android.duke290.com.loco.database;

import android.duke290.com.loco.User;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public interface DatabaseFetchCallback {
    void onDatabaseResultReceived(ArrayList<String> messages, ArrayList<StorageReference> storagerefs);
    void onUserReceived(User user);
}

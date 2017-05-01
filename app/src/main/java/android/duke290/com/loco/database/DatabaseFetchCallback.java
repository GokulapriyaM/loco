package android.duke290.com.loco.database;

import java.util.ArrayList;

/**
 * callback interface for databasefetch
 */
public interface DatabaseFetchCallback {
    void onDatabaseResultReceived(ArrayList<Creation> creations);
    void onUserReceived(User user);
}

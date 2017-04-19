package android.duke290.com.loco.database;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;

public class DatabaseFetch {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;

    final String USERS = "users";
    final String CONTENT = "content";
    final String CREATIONS = "creations";

    final String decimalRoundTo = "3";

    final String TAG = "DatabaseFetch";

    private DatabaseFetchCallback mActivityClass;

    public DatabaseFetch(DatabaseFetchCallback activityClass) {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mActivityClass = activityClass;
    }

    public void fetchByCoordinate(String coordNameLookup) {
        DatabaseReference ref = mDatabase.getReference(CREATIONS).child(coordNameLookup);

        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        collectCreations((Map<String, Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

    }

    public void fetchByUser(String uid) {
        DatabaseReference ref = mDatabase.getReference(USERS).child(uid).child(CONTENT);
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        collectCreations((Map<String, Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }

    private void collectCreations(Map<String, Object> creations) {
        ArrayList<String> messages = new ArrayList<>();
        ArrayList<StorageReference> storagepaths = new ArrayList<>();

        //iterate through each user, ignoring their reference id
        for (Map.Entry<String, Object> entry : creations.entrySet()) {
            //Get creation map
            Map singleCreation = (Map) entry.getValue();
            Log.d(TAG, "received creation");
            String message = singleCreation.get("message").toString();
            if (!message.equals("")) {
                messages.add(message);
            }

            if (singleCreation.get("type").equals("image")) {
                String storage_path = singleCreation.get("extra_storage_path").toString();
                StorageReference storageRef = mStorage.getReference().child(storage_path);
                storagepaths.add(storageRef);
            }
        }
        Log.d(TAG, "Messages: " + messages.toString());
        Log.d(TAG, "Storagepaths: " + storagepaths.toString());
        mActivityClass.onDatabaseResultReceived(messages, storagepaths);
    }


}

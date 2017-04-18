package android.duke290.com.loco;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class DatabaseFetch {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    final String USERS = "users";
    final String CONTENT = "content";
    final String CREATIONS = "creations";

    final String decimalRoundTo = "3";

    final String TAG = "DatabaseTestingActivity";

    private ArrayList<String> messages;
    private ArrayList<String> storagepaths;


    public DatabaseFetch() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
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
        DatabaseReference ref = mDatabase.getReference(USERS).child(CONTENT).child(uid);
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
        messages = new ArrayList<>();
        storagepaths = new ArrayList<>();

        //iterate through each user, ignoring their reference id
        for (Map.Entry<String, Object> entry : creations.entrySet()) {
            //Get creatioin map
            Map singleCreation = (Map) entry.getValue();
            Log.d(TAG, "received creation");
            if (singleCreation.get("type").equals("text")) {
                String message = singleCreation.get("message").toString();
                messages.add(message);
            }

            if (singleCreation.get("type").equals("image")) {
                String storage_path = singleCreation.get("extra_storage_path").toString();
                storagepaths.add(storage_path);
            }
        }
    }

    public ArrayList<String> getMessages(){
        return messages;
    }

    public ArrayList<String> getStoragepaths(){
        return storagepaths;
    }

}

package android.duke290.com.loco.database;

import android.duke290.com.loco.Creation;
import android.duke290.com.loco.User;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class DatabaseFetch {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    FirebaseUser mFirebaseuser;
    private String uid;

    final String USERS = "users";
    final String USERINFO = "userinfo";
    final String CONTENT = "content";
    final String CREATIONS = "creations";

    final String decimalRoundTo = "3";

    final String TAG = "DatabaseFetch";


    private DatabaseFetchCallback mActivityClass;

    public DatabaseFetch(DatabaseFetchCallback activityClass) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseuser = mAuth.getCurrentUser();
        uid = mFirebaseuser.getUid();
        mDatabase = FirebaseDatabase.getInstance();
        mActivityClass = activityClass;
    }

    public void getCurrentUser(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(USERS).child(uid).child(USERINFO);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                mActivityClass.onUserReceived(currentUser);
                Log.d(TAG, "Got user: "+currentUser + " username: " + currentUser.name);
                Log.d(TAG, "Got key: "+dataSnapshot.getKey());
                Log.d(TAG, "Got value: "+dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "The read failed: " + databaseError.getCode());
            }
        });
    }

    public void fetchByUser() {
        Query ref = mDatabase.getReference(USERS).child(uid).child(CONTENT).orderByKey();
        fetch(ref);
    }

    public void fetchByUser(int limit) {
        Query ref = mDatabase.getReference(USERS).child(uid).child(CONTENT)
                .orderByKey().limitToLast(limit);
        fetch(ref);
    }


    public void fetchByCoordinate(String coordNameLookup){
        Query ref = mDatabase.getReference(CREATIONS).child(coordNameLookup).orderByKey();
        fetch(ref);
    }

    public void fetchByCoordinate(String coordNameLookup, int limit) {
        Query ref = mDatabase.getReference(CREATIONS).child(coordNameLookup)
                .orderByKey().limitToLast(limit);
        fetch(ref);
    }

    private void fetch(Query ref) {
        Log.d(TAG, "fetch by coordinates");
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange called");
                        ArrayList<Creation> creations = new ArrayList<>();

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            creations.add(child.getValue(Creation.class));
                        }
                        Collections.reverse(creations);
                        mActivityClass.onDatabaseResultReceived(creations);
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

    }


}

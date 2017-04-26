package android.duke290.com.loco.database;

import android.duke290.com.loco.Creation;
import android.location.Location;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseAction {

    final static String USERS = "users";
    final static String CONTENT = "content";
    final static String CREATIONS = "creations";

    final static String decimalRoundTo = "3";

    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    static String uid;

    final static String TAG = "DatabaseAction";

    public static void putCreationInFirebaseDatabase(Creation reference, Location location) {
        final FirebaseUser this_user = mAuth.getCurrentUser();
        uid = this_user.getUid();
        DatabaseReference ref = mDatabase.getReference(USERS).child(uid).child(CONTENT);
        ref.child("ref" + System.currentTimeMillis()).setValue(reference);
        String coordName = createCoordName(location);
        DatabaseReference refCreationList = mDatabase.getReference(CREATIONS).child(coordName);
        refCreationList.child("cre" + "" + System.currentTimeMillis() + uid).setValue(reference);
    }

    public static void putCreationInFirebaseDatabase(Creation reference, double latitude, double longitude) {
        final FirebaseUser this_user = mAuth.getCurrentUser();
        uid = this_user.getUid();
        DatabaseReference ref = mDatabase.getReference(USERS).child(uid).child(CONTENT);
        ref.child("ref" + System.currentTimeMillis()).setValue(reference);
        String coordName = createCoordName(latitude, longitude);
        DatabaseReference refCreationList = mDatabase.getReference(CREATIONS).child(coordName);
        refCreationList.child("cre" + "" + System.currentTimeMillis() + uid).setValue(reference);
    }


    public static String createImageStoragePath() {
        final FirebaseUser this_user = mAuth.getCurrentUser();
        uid = this_user.getUid();
        return "images/" + "img" + uid + System.currentTimeMillis();
    }

    public static String createCoordName(Location location) {
        String latitudeStr = String.format("%."+ decimalRoundTo + "f", location.getLatitude());
        latitudeStr = latitudeStr.replace(".", ",");
        String longitudeStr = String.format("%."+ decimalRoundTo + "f", location.getLongitude());
        longitudeStr = longitudeStr.replace(".", ",");
        String coordName = "coord" + latitudeStr + ";" +
                longitudeStr;

        return coordName;
    }

    public static String createCoordName(double latitute, double longitude) {
        String latitudeStr = String.format("%." + decimalRoundTo + "f", latitute);
        latitudeStr = latitudeStr.replace(".", ",");
        String longitudeStr = String.format("%." + decimalRoundTo + "f", longitude);
        longitudeStr = longitudeStr.replace(".", ",");
        String coordName = "coord" + latitudeStr + ";" +
                longitudeStr;

        return coordName;
    }

    public static DatabaseReference getDatabaseReferenceForGet(Location location) {
        String coordNameLookup = createCoordName(location);
        return mDatabase.getReference(CREATIONS).child(coordNameLookup);
    }

}

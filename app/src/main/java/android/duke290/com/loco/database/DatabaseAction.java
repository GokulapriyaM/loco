package android.duke290.com.loco.database;

import android.duke290.com.loco.Creation;
import android.location.Location;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * List of methods for interacting with Firebase Database.
 */
public class DatabaseAction {

    final static String USERS = "users";
    final static String CONTENT = "content";
    final static String CREATIONS = "creations";

    final static String decimalRoundTo = "3";

    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    static String uid;

    final static String TAG = "DatabaseAction";

    /**
     * Places a Creation object into Firebase Database.
     *
     * @param reference - Creation object being put into the database
     * @param location - Location that the Creation was created in
     */
    public static void putCreationInFirebaseDatabase(Creation reference, Location location) {
        final FirebaseUser this_user = mAuth.getCurrentUser();
        uid = this_user.getUid();
        DatabaseReference ref = mDatabase.getReference(USERS).child(uid).child(CONTENT);
        ref.child("ref" + System.currentTimeMillis()).setValue(reference);
        String coordName = createCoordName(location);
        DatabaseReference refCreationList = mDatabase.getReference(CREATIONS).child(coordName);
        refCreationList.child("cre" + "" + System.currentTimeMillis() + uid).setValue(reference);
    }

    /**
     * Places a Creation object into Firebase Database.
     *
     * @param reference - Creation object being put into the database
     * @param latitude - Latitude that the Creation was created in
     * @param longitude - Longitude that the Creation was created in
     */
    public static void putCreationInFirebaseDatabase(Creation reference, double latitude, double longitude) {
        final FirebaseUser this_user = mAuth.getCurrentUser();
        uid = this_user.getUid();
        DatabaseReference ref = mDatabase.getReference(USERS).child(uid).child(CONTENT);
        ref.child("ref" + System.currentTimeMillis()).setValue(reference);
        String coordName = createCoordName(latitude, longitude);
        DatabaseReference refCreationList = mDatabase.getReference(CREATIONS).child(coordName);
        refCreationList.child("cre" + "" + System.currentTimeMillis() + uid).setValue(reference);
    }

    /**
     * Creates an path that indicates where to find the image in Firebase Storage.
     *
     * @return - a path that indicates where the image is stored in Firebase Storage.
     */
    public static String createImageStoragePath() {
        final FirebaseUser this_user = mAuth.getCurrentUser();
        uid = this_user.getUid();
        return "images/" + "img" + uid + System.currentTimeMillis();
    }

    /**
     * Creates a key based on location coordinates.
     *
     * @param location - Location object
     * @return - Key based on location.
     */
    public static String createCoordName(Location location) {
        String latitudeStr = String.format("%."+ decimalRoundTo + "f", location.getLatitude());
        latitudeStr = latitudeStr.replace(".", ",");
        String longitudeStr = String.format("%."+ decimalRoundTo + "f", location.getLongitude());
        longitudeStr = longitudeStr.replace(".", ",");
        String coordName = "coord" + latitudeStr + ";" +
                longitudeStr;

        return coordName;
    }

    /**
     * Creates a key based on location coordinates.
     *
     * @param latitute - Latitude that the device is located in
     * @param longitude - Longitude that the device is located in
     * @return - Key based on location.
     */
    public static String createCoordName(double latitute, double longitude) {
        String latitudeStr = String.format("%." + decimalRoundTo + "f", latitute);
        latitudeStr = latitudeStr.replace(".", ",");
        String longitudeStr = String.format("%." + decimalRoundTo + "f", longitude);
        longitudeStr = longitudeStr.replace(".", ",");
        String coordName = "coord" + latitudeStr + ";" +
                longitudeStr;

        return coordName;
    }

}

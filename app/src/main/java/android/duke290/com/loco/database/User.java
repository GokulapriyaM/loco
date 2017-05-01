package android.duke290.com.loco.database;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String name;
    public String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * user constructor with two strings parameters
     * @param name
     * @param email
     */
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    /**
     * user constructor with another User object parameter
     * @param u
     */
    public User(User u){
        this.name = u.name;
        this.email = u.email;
    }

}
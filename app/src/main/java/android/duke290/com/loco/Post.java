package android.duke290.com.loco;


import java.sql.Time;
import java.sql.Timestamp;

/**
 * Created by Jihane on 4/22/17.
 */

public class Post {
    // Defining each post by a username, a text, and a timestamp
    private String mUsername;
    private String mPost;
    private Timestamp mTimestamp;

    public Post(String username, String post, long time) {
        mUsername = username;
        mPost = post;
        mTimestamp = new Timestamp(time);
    }

    public String getUsername() {return mUsername;}

    public String getPost() {return mPost;}

    public Timestamp getTimestamp() {return mTimestamp;}
}

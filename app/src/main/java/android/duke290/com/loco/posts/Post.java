package android.duke290.com.loco.posts;


public class Post {
    // Defining each post by a text and a timestamp
    private String mPost;
    private String mTimestamp;

    public Post(String post, String timestamp) {
        mPost = post;
        mTimestamp = timestamp;
    }


    public String getPost() {return mPost;}

    public String getTimestamp() {
        return mTimestamp;
    }
}

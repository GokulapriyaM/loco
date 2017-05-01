package android.duke290.com.loco.posts;

/**
 * Class that defines a post.
 */
public class Post {
    // Defining each post by a text and a timestamp
    private String mPost;
    private String mTimestamp;

    /**
     * Constructor for Post.
     * @param post - the post's message
     * @param timestamp - the time that the post was made
     */
    public Post(String post, String timestamp) {
        mPost = post;
        mTimestamp = timestamp;
    }

    /**
     * Retrieves the post's message.
     * @return mPost - the post's message.
     */
    public String getPost() {return mPost;}

    /**
     * Retrieves the post's timestamp.
     * @return mPost - the post's timestamp.
     */
    public String getTimestamp() {
        return mTimestamp;
    }
}

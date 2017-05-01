package android.duke290.com.loco.database;

import java.util.ArrayList;

/**
 * Class used to store a set of Creations of type "image"
 * and a set of Creations of type "message."
 */
public class SharedLists {
    private static final SharedLists list_holder = new SharedLists();

    private ArrayList<Creation> image_creation_list;
    private ArrayList<Creation> message_creation_list;

    public ArrayList<Creation> getImageCreations() {
        return image_creation_list;
    }

    /**
     * Sets image_creation_list.
     *
     * @param image_creations - ArrayList of Creation objects.
     */
    public void setImageCreations(ArrayList<Creation> image_creations) {
        image_creation_list = image_creations;
    }

    /**
     * Returns message_creation_list.
     *
     * @return - message_creation_list
     */
    public ArrayList<Creation> getMessageCreations() {
        return message_creation_list;
    }

    /**
     * Sets message_creation_list.
     *
     * @param messageCreations - ArrayList of Creation objects.
     */
    public void setMessageCreations(ArrayList<Creation> messageCreations) {
        message_creation_list = messageCreations;
    }

    /**
     * Gets an instance of SharedLists.
     *
     * @return - Instance of SharedLists
     */
    public static SharedLists getInstance() {
        return list_holder;
    }

}

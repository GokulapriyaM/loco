package android.duke290.com.loco;

import java.util.ArrayList;


public class SharedLists {
    private static final SharedLists list_holder = new SharedLists();

    private ArrayList<Creation> image_creation_list;
    private ArrayList<Creation> message_creation_list;

    public ArrayList<Creation> getImageCreations() {
        return image_creation_list;
    }

    public void setImageCreations(ArrayList<Creation> image_creations) {
        image_creation_list = image_creations;
    }

    public ArrayList<Creation> getMessageCreations() {
        return message_creation_list;
    }

    public void setMessageCreations(ArrayList<Creation> messageCreations) {
        message_creation_list = messageCreations;
    }

    public static SharedLists getInstance() { return list_holder; };

}

package android.duke290.com.loco;

import java.util.ArrayList;

/**
 * Created by kevinkuo on 4/23/17.
 */

public class SharedLists {
    private static final SharedLists list_holder = new SharedLists();

    private ArrayList<Creation> image_creation_list;

    public ArrayList<Creation> getImageCreations() {
        return image_creation_list;
    }

    public void setImageCreations(ArrayList<Creation> image_creations) {
        image_creation_list = image_creations;
    }

    public static SharedLists getInstance() { return list_holder; };

}

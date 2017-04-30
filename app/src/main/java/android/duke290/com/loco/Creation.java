package android.duke290.com.loco;

public class Creation {
    public double latitude;
    public double longitude;
    public String address;
    public String type;
    public String message;
    public String extra_storage_path;
    public String timestamp; //format: MM/dd/yyyy hh:mm a
    public double rating;

    /**
     *  Default constructor required for Firebase Database
     */
    public Creation() {

    }

    /**
     * Object to be stored in database about things user shared
     * @param latitude
     * @param longitude
     * @param address geocoded address
     * @param type text, image or rating
     * @param message
     * @param extra_storage_path storage path for images
     * @param rating
     * @param timestamp
     */
    public Creation(double latitude, double longitude,
                    String address, String type, String message,
                    String extra_storage_path, double rating, String timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.type = type;
        this.message = message;
        this.extra_storage_path = extra_storage_path;
        this.rating = rating;
        this.timestamp = timestamp;
    }

}

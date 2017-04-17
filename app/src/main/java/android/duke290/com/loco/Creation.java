package android.duke290.com.loco;

public class Creation {
    public double latitude;
    public double longitude;
    public String address;
    public String type;
    public String message;
    public String extra_storage_path;

    public Creation() {

    }

    public Creation(double latitude, double longitude,
                    String address, String type, String message,
                    String extra_storage_path) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.type = type;
        this.message = message;
        this.extra_storage_path = extra_storage_path;
    }

}

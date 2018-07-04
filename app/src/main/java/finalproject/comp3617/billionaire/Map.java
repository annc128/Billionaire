package finalproject.comp3617.billionaire;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Map implements Serializable {
    @SerializedName("name")
    private String name;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;


    public String getName() {
        return this.name;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

}

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
//
//    public void setName(String name){
//        this.name = name;
//    }
//
//    public void setLatitude(double latitude){
//        this.latitude = latitude;
//    }
//
//    public void setLongitude(double longitude){
//        this.longitude = longitude;
//    }

    public String getName() {
        return this.name;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

//    @Override
//    public  String toString(){
//        StringBuilder sb = new StringBuilder();
//        sb.append("\"name\": ").append(name).append(",\n")
//                .append("\"latitude\": ").append(latitude).append(",\n")
//                .append("\"longitude\": ").append(longitude).append('\n');
//
//        return sb.toString();
//    }
}

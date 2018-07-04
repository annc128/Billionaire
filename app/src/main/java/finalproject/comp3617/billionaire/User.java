package finalproject.comp3617.billionaire;

public class User {
    private String userID;
    private double latitude;
    private double longitude;
    private double money;

    public User() {
    }

    public User(String userID, double latitude, double longitude, double money) {
        this.userID = userID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.money = money;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }
}

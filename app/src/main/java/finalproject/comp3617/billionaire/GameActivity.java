package finalproject.comp3617.billionaire;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;


public class GameActivity extends AppCompatActivity{
    private float monitorWidth = 0;
    private float monitorHeight = 0;
    private ImageView person;
    private LocationManager locationManager;
    private String locationProvider;
    private static String TAG = "MAP";
    private List<Map> listMaps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        RelativeLayout gameRl = findViewById(R.id.gameRl);
        TableLayout tlSquare = findViewById(R.id.tlSquare);
        TableLayout tlText = findViewById(R.id.tlSquare);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted or not
                tryToGetLocationValue();
            } else {
                //Request Location Permission
                askLocationPermission();
            }
        }
        else {
            // not required any runtime permission for below M
            tryToGetLocationValue();
        }
        //get monitor height and width
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        monitorWidth = metrics.widthPixels;
        monitorHeight = metrics.heightPixels;
        Toast.makeText(GameActivity.this, "手機銀幕大小為 " + metrics.widthPixels + " X " + metrics.heightPixels, Toast.LENGTH_SHORT).show();
        String jsonResponse = loadJSONFromAsset();
        Map[] mapJsonResponse = new MapJsonResponse().parseJSON(jsonResponse);

        listMaps = Arrays.asList(mapJsonResponse);
        for (int i = 0; i <= 2; i++) {
            /* Create a new row to be added. */
            TableRow trSquare = new TableRow(this);
            TableRow trText = new TableRow(this);
            trSquare.setLayoutParams(new TableRow.LayoutParams(metrics.widthPixels, metrics.heightPixels / 3));
            trText.setLayoutParams(new TableRow.LayoutParams(metrics.widthPixels, metrics.heightPixels / 3));
            /* Create a Button to be the row-content. */
            for (int j = 0; j <= 3; j++) {
                ImageView square = new ImageView(this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(metrics.widthPixels / 4, metrics.heightPixels / 3);
                TextView text = new TextView(this);
                text.setText(listMaps.get(i + j).getName());
                if (i == 1 && (j == 1 || j == 2)) {
                    square.setImageResource(R.drawable.ic_crop_square_24dp);
                    square.setLayoutParams(lp);
                    square.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
                    trSquare.addView(square);
                } else {
                    square.setImageResource(R.drawable.ic_crop_square_24dp);
                    square.setLayoutParams(lp);
                    trSquare.addView(square);
                }
                trText.addView(text);
            }

            /* Add row to TableLayout. */
            //tr.setBackgroundResource(R.drawable.sf_gradient_03);

            tlSquare.addView(trSquare, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            tlText.addView(trText, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        }

        person = new ImageView(this);
        person.setImageResource(R.drawable.ic_directions_walk_black_24dp);
        person.setX(monitorWidth / 4 / 3);
        person.setY(monitorHeight / 3 / 2);

        gameRl.addView(person);

    }
    /**
     * LocationListern監聽器
     * 參數：地理位置提供器、監聽位置變化的時間間隔、位置變化的距離間隔、LocationListener監聽器
     */

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider + ".." + Thread.currentThread().getName());
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider + ".." + Thread.currentThread().getName());
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: " + ".." + Thread.currentThread().getName());
            //如果位置發生變化,重新顯示
            showLocation(location);

            if (truncateDouble(location.getLatitude()) == truncateDouble(listMaps.get(0).getLatitude()) && truncateDouble(location.getLongitude()) == truncateDouble(listMaps.get(0).getLongitude())) {
                person.setX(monitorWidth / 4 / 2 / 2 - 5);
                person.setY(monitorHeight / 3 / 2);
            }
        }
    };

    private void showLocation(Location location) {
        Log.d(TAG,"定位成功------->"+"location------>經度為：" + location.getLatitude() + "\n緯度為" + location.getLongitude());
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission. This info convince to users to allow
                // Show alert message to users to need location access allpw location permission
                showAlert();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    private void showAlert(){
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Needed")
                .setMessage("This app needs the Location permission, please accept to use location functionality")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Prompt the user once explanation has been shown to ask for permission again
                        ActivityCompat.requestPermissions(GameActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION );
                    }
                })
                .create()
                .show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission is granted
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                        tryToGetLocationValue();
                    }
                } else {
                    // permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
    private void tryToGetLocationValue(){
        locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);


        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);//低精度，如果設置為高精度，依然獲取不了location。
        criteria.setAltitudeRequired(false);//不要求海拔
        criteria.setBearingRequired(false);//不要求方位
        criteria.setCostAllowed(true);//允許有花費
        criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗

        //從可用的位置提供器中，匹配以上標準的最佳提供器
        locationProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: 沒有權限 ");
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        Log.d(TAG, "onCreate: " + (location == null) + "..");
        if (location != null) {
            Log.d(TAG, "onCreate: location");
            //不為空,顯示地理位置經緯度
            showLocation(location);
        }
        //監視地理位置變化
        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
    }

    private void mapParsedResponse() {
        String jsonResponse = loadJSONFromAsset();
        Map[] mapJsonResponse = new MapJsonResponse().parseJSON(jsonResponse);

        List<Map> listMaps = Arrays.asList(mapJsonResponse);

        for (Map map : listMaps) {
            System.out.println("technicaljungle ---- Name -> " + map.getName()
                    + " -- Latitude -- " + map.getLatitude() + "--- Longitude --" + map.getLongitude());
        }
    }

    //Load JSON file from Assets folder.
    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("maps.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private double truncateDouble(double input) {
        BigDecimal bd = new BigDecimal(input);
        return bd.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}

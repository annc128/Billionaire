package finalproject.comp3617.billionaire;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class GameActivity extends AppCompatActivity implements BuyDialog.BuyDialogListener {
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseUser user;
    private ConstraintLayout gameCL;
    private ConstraintSet constraintSet;
    private ImageView ivMe, ivEnemy;
    private ImageButton ibtDice;
    private TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv10;
    private int destination = 0;
    private int lastLocation;
    private int nowLocation;
    private int enemyLocation = 0;
    private int enemyLastLocation = 0;
    private double myMoney = 1000;
    private double enemyMoney = 1000;
    private boolean starting = true;
    private LocationManager locationManager;
    private String locationProvider;
    private String uid;
    private static String TAG = "MAP";
    private List<Map> listMaps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        findView();
        attachCharacter();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                uid = profile.getUid();
            }
        }
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        myRef.child("users").child("competitor").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(String.class).equals("computer")) {

                    enemyPosition();
                }
                String value = dataSnapshot.getValue(String.class);

                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

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
        MapJsonResponse myJson = new MapJsonResponse(GameActivity.this);
        String jsonResponse = myJson.loadJSONFromAsset("ya"); // error
        Map[] mapJsonResponse = myJson.parseJSON(jsonResponse);

        listMaps = Arrays.asList(mapJsonResponse);
        tv1.setText(listMaps.get(0).getName());
        tv2.setText(listMaps.get(1).getName());
        tv3.setText(listMaps.get(2).getName());
        tv4.setText(listMaps.get(3).getName());
        tv5.setText(listMaps.get(4).getName());
        tv6.setText(listMaps.get(5).getName());
        tv7.setText(listMaps.get(6).getName());
        tv8.setText(listMaps.get(7).getName());
        tv9.setText(listMaps.get(8).getName());
        tv10.setText(listMaps.get(9).getName());



        constraintSet = new ConstraintSet();
        ibtDice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                lastLocation = destination;
                destination = nowLocation + random.nextInt(6) + 1;
                destination = checkIfNull(destination);

                Toast.makeText(GameActivity.this, "your destination is " + listMaps.get(destination).getName(), Toast.LENGTH_LONG).show();
                ibtDice.setVisibility(View.INVISIBLE);
            }
        });
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
            position(location);
            enemyPosition();

            // Check if the player is at the starting point
            if (starting == true && destination != nowLocation) {
                Toast.makeText(GameActivity.this, "Please go to " + listMaps.get(destination).getName(), Toast.LENGTH_LONG).show();
            } else if (starting == true && destination == nowLocation) {
                Toast.makeText(GameActivity.this, "Ready to go!", Toast.LENGTH_LONG).show();
                ibtDice.setVisibility(View.VISIBLE);
                starting = false;
            }

            if (destination == nowLocation && destination != 0 && !ibtDice.isShown()) {
                checkToll("me");
                myRef.child("users").child("maps").child(Integer.toString(nowLocation)).child("Owner").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class);
                        if (value.equals("")) {
                            BuyDialog dialog = BuyDialog.instance("On sale");
                            FragmentManager fm = getFragmentManager();
                            dialog.show(fm, "BuyDialog");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });

                Random random = new Random();
                ibtDice.setVisibility(View.VISIBLE);
                enemyLastLocation = enemyLocation;
                enemyLocation += random.nextInt(6) + 1;
                enemyLocation = checkIfNull(enemyLocation);
                checkToll("pig");
                myRef.child("users").child("maps").child(Integer.toString(enemyLocation)).child("Owner").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class);
                        if (value.equals("")) {
                            myRef.child("users").child("maps").child(Integer.toString(enemyLocation)).child("Owner").setValue(uid);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });
                enemyPosition();
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

    private void findView() {
        gameCL = findViewById(R.id.gameCl);
        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        tv3 = findViewById(R.id.tv3);
        tv4 = findViewById(R.id.tv4);
        tv5 = findViewById(R.id.tv5);
        tv6 = findViewById(R.id.tv6);
        tv7 = findViewById(R.id.tv7);
        tv8 = findViewById(R.id.tv8);
        tv9 = findViewById(R.id.tv9);
        tv10 = findViewById(R.id.tv10);
        ibtDice = findViewById(R.id.ibtDice);
    }

    private void attachCharacter() {
        ivMe = new ImageView(this);
        ivMe.setImageResource(R.drawable.hello);
        ivMe.setId(R.id.ivMe);
        gameCL.addView(ivMe);
        ivMe.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
        ivMe.getLayoutParams().width = (int) getResources().getDimension(R.dimen.imageview_width);
        ivEnemy = new ImageView(GameActivity.this);
        ivEnemy.setImageResource(R.drawable.pig);
        ivEnemy.setId(R.id.ivEnemy);
        gameCL.addView(ivEnemy);
        ivEnemy.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
        ivEnemy.getLayoutParams().width = (int) getResources().getDimension(R.dimen.imageview_width);
    }

    private double truncateDouble(double input) {
        BigDecimal bd = new BigDecimal(input);
        return bd.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private void position(Location location) {
        for (int i = 0; i < listMaps.size(); i++) {
            if (truncateDouble(location.getLatitude()) == truncateDouble(listMaps.get(i).getLatitude()) && truncateDouble(location.getLongitude()) == truncateDouble(listMaps.get(i).getLongitude())) {
                nowLocation = i;
                moveCharacter(ivMe, i);
            }
        }
    }

    private void enemyPosition() {
        moveCharacter(ivEnemy, enemyLocation);
    }

    private void moveCharacter(ImageView im, int myId) {
        switch (myId) {
            case 0:
                myId = R.id.tv1;
                break;
            case 1:
                myId = R.id.tv2;
                break;
            case 2:
                myId = R.id.tv3;
                break;
            case 3:
                myId = R.id.tv4;
                break;
            case 4:
                myId = R.id.tv5;
                break;
            case 5:
                myId = R.id.tv6;
                break;
            case 6:
                myId = R.id.tv7;
                break;
            case 7:
                myId = R.id.tv8;
                break;
            case 8:
                myId = R.id.tv9;
                break;
            case 9:
                myId = R.id.tv10;
                break;
            default:
                myId = R.id.tv10;
                break;
        }
        constraintSet.clone(gameCL);
        constraintSet.connect(im.getId(), ConstraintSet.BOTTOM, myId, ConstraintSet.BOTTOM, 8);
        constraintSet.connect(im.getId(), ConstraintSet.END, myId, ConstraintSet.END, 8);
        constraintSet.connect(im.getId(), ConstraintSet.START, myId, ConstraintSet.START, 8);
        constraintSet.connect(im.getId(), ConstraintSet.TOP, myId, ConstraintSet.TOP, 8);
        constraintSet.applyTo(gameCL);
        if (myId == R.id.tv10) {
            try {
                Thread.sleep(2000);
                Toast.makeText(GameActivity.this, "finished", Toast.LENGTH_LONG).show();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkToll(final String person) {
        int begin;
        int now;
        if (person.equals("me")) {
            begin = lastLocation;
            now = nowLocation;
        } else {
            begin = enemyLastLocation;
            now = enemyLocation;
        }
        for (int i = begin; i <= now; i++) {
            myRef.child("users").child("maps").child(Integer.toString(i)).child("Owner").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String value = dataSnapshot.getValue(String.class);
                    if (!value.equals("") && !value.equals(uid) && person.equals("me")) {
                        myMoney -= 100;
                        Log.d(TAG, Double.toString(myMoney));
                    } else if (!value.equals("") && !value.equals("pig") && person.equals("pig")) {
                        enemyMoney -= 100;
                        Log.d(TAG, Double.toString(enemyMoney));
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }
    }

    private int checkIfNull(int value) {
        if (value >= listMaps.size()) {
            value = listMaps.size() - 1;
        }
        return value;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        myRef.child("users").child("maps").child(Integer.toString(nowLocation)).child("Owner").setValue(uid);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }
}

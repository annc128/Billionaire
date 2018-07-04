package finalproject.comp3617.billionaire;

import android.Manifest;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.ActivityCompat;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public abstract class GameBaseActivity extends AppCompatActivity implements BuyDialog.BuyDialogListener, EasyPermissions.PermissionCallbacks {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    protected FirebaseDatabase database;
    protected DatabaseReference myRef;
    protected DatabaseReference ref;
    protected FirebaseUser user;
    protected ConstraintLayout gameCL;
    protected ConstraintSet constraintSet;
    protected ImageView ivMe, ivEnemy;
    protected ImageButton ibtDice;
    protected TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv10, tvInfo, tvMyMoney, tvEnemyMoney;
    protected int destination = 0;
    protected int lastLocation;
    protected int nowLocation = -1;
    protected int enemyLocation = -1;
    protected int enemyLastLocation = 0;
    protected double myMoney;
    protected double enemyMoney;
    protected boolean starting = true;
    protected boolean isHost;
    protected boolean isFinished;
    protected LocationManager locationManager;
    protected String locationProvider;
    protected String uid;
    protected String host;
    protected String refMe;
    protected String refEnemy;
    static protected String myMap;

    protected static String TAG = "MAP";
    protected List<Map> listMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_base);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        host = getIntent().getStringExtra("HOST");

        myMap = getIntent().getStringExtra("MAP");
        isHost = getIntent().getBooleanExtra("ISHOST", false);
        if (isHost) {
            refMe = "user1";
            refEnemy = "user2";
        } else {
            refMe = "user2";
            refEnemy = "user1";
        }
        double iniMoney = getIntent().getDoubleExtra("MONEY", 0);
        myMoney = iniMoney;
        enemyMoney = iniMoney;
        findView();
        methodRequiresPermission();
        attachCharacter();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                uid = profile.getUid();
            }
        }
        ref = database.getReference();


        setValuesToView();
        tvMyMoney.setText(Double.toString(iniMoney));
        tvEnemyMoney.setText(Double.toString(iniMoney));

        constraintSet = new ConstraintSet();
        ibtDice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                lastLocation = destination;
                destination = nowLocation + random.nextInt(6) + 1;
                destination = checkIfNull(destination);

                Toast.makeText(GameBaseActivity.this, "your destination is " + listMaps.get(destination).getName(), Toast.LENGTH_SHORT).show();
                ibtDice.setVisibility(View.INVISIBLE);
                tvInfo.setText(listMaps.get(destination).getName());
                tvInfo.setVisibility(View.VISIBLE);
            }
        });
    }

    @AfterPermissionGranted(MY_PERMISSIONS_REQUEST_LOCATION)
    private void methodRequiresPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            tryToGetLocationValue();
        } else {
            EasyPermissions.requestPermissions(this, "This app needs the Location permission, please accept to use location functionality",
                    MY_PERMISSIONS_REQUEST_LOCATION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    private void tryToGetLocationValue() {
        locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        locationProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: doesn't have permission");
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        Log.d(TAG, "onCreate: " + (location == null) + "..");
        if (location != null) {
            Log.d(TAG, "onCreate: location");
        }
        if (!isFinished) {
            locationManager.requestLocationUpdates(locationProvider, 0, 0, createLocationListener());
        }

    }

    protected abstract LocationListener createLocationListener();

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
        tvMyMoney = findViewById(R.id.tvMyMoney);
        tvEnemyMoney = findViewById(R.id.tvEnemyMoney);
        tvInfo = findViewById(R.id.tvInfo);
        ibtDice = findViewById(R.id.ibtDice);
    }

    private void setValuesToView() {
        MapJsonResponse myJson = new MapJsonResponse(GameBaseActivity.this);
        String jsonResponse = myJson.loadJSONFromAsset(myMap);
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
    }

    private void attachCharacter() {
        ivEnemy = new ImageView(GameBaseActivity.this);
        ivEnemy.setImageResource(R.drawable.pig);
        ivEnemy.setId(R.id.ivEnemy);
        gameCL.addView(ivEnemy);
        ivEnemy.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
        ivEnemy.getLayoutParams().width = (int) getResources().getDimension(R.dimen.imageview_width);
        ivMe = new ImageView(this);
        ivMe.setImageResource(R.drawable.hello);
        ivMe.setId(R.id.ivMe);
        gameCL.addView(ivMe);
        ivMe.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
        ivMe.getLayoutParams().width = (int) getResources().getDimension(R.dimen.imageview_width);
    }

    protected double truncateDouble(double input) {
        BigDecimal bd = new BigDecimal(input);
        return bd.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    protected void position(Location location) {
        for (int i = 0; i < listMaps.size(); i++) {
            if (truncateDouble(location.getLatitude()) == truncateDouble(listMaps.get(i).getLatitude()) && truncateDouble(location.getLongitude()) == truncateDouble(listMaps.get(i).getLongitude())) {
                nowLocation = i;
                moveCharacter(ivMe, i);
            }
        }
    }

    protected abstract void enemyPosition();

    protected void moveCharacter(ImageView im, int myId) {
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
        if (myId == R.id.tv10 && (!starting)) {
            myId = R.id.parent;
            im.getLayoutParams().height = 1000;
            im.getLayoutParams().width = 1000;
            isFinished = true;
            ibtDice.setVisibility(View.INVISIBLE);
            //Toast.makeText(GameBaseActivity.this, "finished", Toast.LENGTH_SHORT).show();

        }
        constraintSet.clone(gameCL);
        constraintSet.connect(im.getId(), ConstraintSet.BOTTOM, myId, ConstraintSet.BOTTOM, 8);
        constraintSet.connect(im.getId(), ConstraintSet.END, myId, ConstraintSet.END, 8);
        constraintSet.connect(im.getId(), ConstraintSet.START, myId, ConstraintSet.START, 8);
        constraintSet.connect(im.getId(), ConstraintSet.TOP, myId, ConstraintSet.TOP, 8);
        constraintSet.applyTo(gameCL);

    }

    protected abstract void checkToll(final String person);

    protected int checkIfNull(int value) {
        if (value >= listMaps.size()) {
            value = listMaps.size() - 1;
        }
        return value;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        myMoney -= 100;
        myRef.child(host).child(refMe).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User value = dataSnapshot.getValue(User.class);
                Log.d(TAG, Double.toString(value.getMoney()));
                double money_now = value.getMoney() - (double) 100;
                Log.d(TAG, Double.toString(money_now));
                value.setMoney(money_now);
                myRef.child(host).child(refMe).setValue(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        myRef.child(host).child("maps").child(Integer.toString(nowLocation)).child("owner").setValue(uid);
        enemyPosition();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        enemyPosition();
        dialog.dismiss();
    }
}

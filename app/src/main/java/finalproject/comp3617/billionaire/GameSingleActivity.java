package finalproject.comp3617.billionaire;

import android.app.FragmentManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class GameSingleActivity extends GameBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_game_single);

    }

    @Override
    protected LocationListener createLocationListener() {

        return new LocationListener() {
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
                tvMyMoney.setText(Double.toString(myMoney));
                tvEnemyMoney.setText(Double.toString(enemyMoney));

                // Check if the player is at the starting point
                if (starting == true && destination != nowLocation) {
                    tvInfo.setText(listMaps.get(destination).getName());
                    tvInfo.setVisibility(View.VISIBLE);
                } else if (starting == true && destination == nowLocation) {
                    Toast.makeText(GameSingleActivity.this, "Ready to go!", Toast.LENGTH_SHORT).show();
                    ibtDice.setVisibility(View.VISIBLE);
                    tvInfo.setVisibility(View.INVISIBLE);
                    enemyLocation = 0;
                    starting = false;
                }

                if (destination == nowLocation && destination != 0 && !ibtDice.isShown()) {
                    checkToll("me");
                    myRef.child(host).child("maps").child(Integer.toString(nowLocation)).child("owner").addListenerForSingleValueEvent(new ValueEventListener() {
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
                    tvInfo.setVisibility(View.INVISIBLE);
                    enemyLastLocation = enemyLocation;
                    enemyLocation += random.nextInt(6) + 1;
                    enemyLocation = checkIfNull(enemyLocation);
                    checkToll("pig");
                    myRef.child(host).child("maps").child(Integer.toString(enemyLocation)).child("owner").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String value = dataSnapshot.getValue(String.class);
                            if (value.equals("")) {
                                myRef.child(host).child("maps").child(Integer.toString(enemyLocation)).child("owner").setValue("pig");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });

                }
            }
        };
    }

    @Override
    protected void checkToll(final String person) {
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
            myRef.child(host).child("maps").child(Integer.toString(i)).child("owner").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String value = dataSnapshot.getValue(String.class);

                    if (!value.equals("") && !value.equals(uid) && person.equals("me")) {
                        myMoney -= 100;

                        Toast.makeText(GameSingleActivity.this, "Your Money:" + Double.toString(myMoney), Toast.LENGTH_SHORT).show();
                    } else if (!value.equals("") && !value.equals("pig") && person.equals("pig")) {
                        enemyMoney -= 100;

                        Toast.makeText(GameSingleActivity.this, "Money of Enemy:" + Double.toString(enemyMoney), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }
    }

    protected void enemyPosition() {
        moveCharacter(ivEnemy, enemyLocation);
    }
}

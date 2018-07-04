package finalproject.comp3617.billionaire;

import android.app.FragmentManager;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class GameGuestActivity extends GameBaseActivity {
    User user_me;
    User user_enemy = new User();
    private String inviteCode;
    private boolean isWaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myRef.child(host).child("user2").child("userID").setValue(uid);
        // setContentView(R.layout.activity_game_multi);

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

                myRef.child(host).child("user2").child("latitude").setValue(location.getLatitude());
                myRef.child(host).child("user2").child("longitude").setValue(location.getLongitude());
                position(location);
                enemyPosition();

                // Check if the player is at the starting point
                if (starting == true && destination != nowLocation) {
                    tvInfo.setText(listMaps.get(destination).getName());
                    ibtDice.setVisibility(View.INVISIBLE);
                    tvInfo.setVisibility(View.VISIBLE);
                } else if (starting == true && destination == nowLocation) {
                    Toast.makeText(GameGuestActivity.this, "Ready to go!", Toast.LENGTH_SHORT).show();
                    ibtDice.setVisibility(View.VISIBLE);
                    tvInfo.setVisibility(View.INVISIBLE);
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
                    ibtDice.setVisibility(View.VISIBLE);
                    tvInfo.setVisibility(View.INVISIBLE);
                    enemyPosition();
                }
            }
        };
    }

    @Override
    protected void checkToll(final String person) {
        for (int i = lastLocation; i <= nowLocation; i++) {
            myRef.child(host).child("maps").child(Integer.toString(i)).child("owner").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String value = dataSnapshot.getValue(String.class);
                    if (!value.equals("") && !value.equals(uid)) {
                        myRef.child(host).child("user2").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User value = dataSnapshot.getValue(User.class);
                                Log.d(TAG, Double.toString(value.getMoney()));
                                double money_now = value.getMoney() - (double) 100;
                                Log.d(TAG, Double.toString(money_now));
                                value.setMoney(money_now);
                                myRef.child(host).child("user2").setValue(value);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Log.w(TAG, "Failed to read value.", error.toException());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }
    }

    @Override
    protected void enemyPosition() {
        myRef.child(host).child("user1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_enemy = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        for (int i = 0; i < listMaps.size(); i++) {
//            if (user_enemy.getLatitude() == 0 && user_enemy.getLongitude() == 0) {
//                ivEnemy.setVisibility(View.INVISIBLE);
//            }
            if (truncateDouble(user_enemy.getLatitude()) == truncateDouble(listMaps.get(i).getLatitude()) && truncateDouble(user_enemy.getLongitude()) == truncateDouble(listMaps.get(i).getLongitude())) {
                enemyLocation = i;
                moveCharacter(ivEnemy, i);
            }
        }
    }


}

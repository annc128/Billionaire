package finalproject.comp3617.billionaire;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class GameMultiActivity extends GameBaseActivity {
    User user_enemy = new User();
    private String inviteCode;
    private boolean isWaiting;
    private boolean isHost;
    private String refMe;
    private String refEnemy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();
        isHost = getIntent().getBooleanExtra("ISHOST", false);
        inviteCode = getIntent().getStringExtra("INVITECODE");
        myRef.child(host).child("user2").child("userID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if (value.equals("waiting")) {
                    isWaiting = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        if (isHost) {
            refMe = "user1";
            refEnemy = "user2";
        } else {
            refMe = "user2";
            refEnemy = "user1";
        }

        if (isHost && isWaiting) {
            checkGuestIn();
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.inviting_line));
            intent.putExtra(Intent.EXTRA_TEXT, "Host Name:" + host + "\nInvite Code:" + inviteCode);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
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
                if (isWaiting) {
                    checkGuestIn();
                }
                myRef.child(host).child(refMe).child("latitude").setValue(location.getLatitude());
                myRef.child(host).child(refMe).child("longitude").setValue(location.getLongitude());
                position(location);
                enemyPosition();

                // Check if the player is at the starting point
                if (starting == true && destination != nowLocation && !isWaiting) {
                    tvInfo.setText(listMaps.get(destination).getName());
                    ibtDice.setVisibility(View.INVISIBLE);
                    tvInfo.setVisibility(View.VISIBLE);
                } else if (starting == true && destination == nowLocation) {
                    Toast.makeText(GameMultiActivity.this, "Ready to go!", Toast.LENGTH_SHORT).show();
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
                        myRef.child(host).child(refMe).addValueEventListener(new ValueEventListener() {
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
        myRef.child(host).child(refEnemy).addValueEventListener(new ValueEventListener() {
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
            if (user_enemy.getLatitude() == 0 && user_enemy.getLongitude() == 0) {
                ivEnemy.setVisibility(View.INVISIBLE);
            } else {
                ivEnemy.setVisibility(View.VISIBLE);
            }
            if (truncateDouble(user_enemy.getLatitude()) == truncateDouble(listMaps.get(i).getLatitude()) && truncateDouble(user_enemy.getLongitude()) == truncateDouble(listMaps.get(i).getLongitude())) {
                enemyLocation = i;
                moveCharacter(ivEnemy, i);
            }
        }
    }

    private void checkGuestIn() {
        myRef.child(host).child(refEnemy).child("userID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if (value.equals("waiting")) {
                    ibtDice.setVisibility(View.INVISIBLE);
                    tvInfo.setText("waiting for another player!");
                    tvInfo.setVisibility(View.VISIBLE);
                    isWaiting = true;
                } else {
                    isWaiting = false;
                    ibtDice.setVisibility(View.VISIBLE);
                    tvInfo.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

}

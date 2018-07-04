package finalproject.comp3617.billionaire;


import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements InvitedDialog.InviteInputListener {
    private static final String TAG = "Main";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private RadioGroup rgPlayers;
    private Button btGo;
    private Button btMap;
    private EditText etMoney;
    private EditText etNickname;
    private EditText etCode;
    private Spinner spTransportation;
    private Spinner spMap;
    private boolean isMapSelected;
    private FirebaseUser user;
    private String uid;
    private boolean isGuest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources res = getResources();
        int players = res.getInteger(R.integer.players);

        rgPlayers = (findViewById(R.id.rgPlayers));
        btGo = findViewById(R.id.btGo);
        btMap = findViewById(R.id.btMap);
        etMoney = findViewById(R.id.etMoney);
        etNickname = findViewById(R.id.etNickname);
        etCode = findViewById(R.id.etCode);
        spTransportation = findViewById(R.id.spTransportation);
        spMap = findViewById(R.id.spMap);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                uid = profile.getUid();
            }
        }
        // add players radio buttons
        for (int i = 1; i <= players; i++) {
            RadioButton rbPlayer = new RadioButton(this);
            rbPlayer.setId(i-1);
            rbPlayer.setText(Integer.toString(i));
            rgPlayers.addView(rbPlayer);
        }

        // add transportation spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transportations, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTransportation.setAdapter(adapter);
        // transportation spinner selected
        spTransportation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "你選的是" + spTransportation.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // add map spinner
        adapter = ArrayAdapter.createFromResource(this,
                R.array.maps, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMap.setAdapter(adapter);
        // map spinner selected
        spMap.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spMap.getItemAtPosition(position).equals("Please select a map")) {
                    isMapSelected = true;
                    Intent goToMap = new Intent(MainActivity.this, MapsActivity.class);

                    goToMap.putExtra("MAP", spMap.getItemAtPosition(position).toString());
                    startActivity(goToMap);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InvitedDialog dialog = new InvitedDialog();
                dialog.show(getFragmentManager(), "inviteDialog");
            }
        });
        btMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int players = rgPlayers.getCheckedRadioButtonId();
                String inviteCode = etCode.getText().toString();
                String warning = "";
                if (etNickname.getText().toString().equals("")) {
                    warning += "Please enter your nickname!";
                }
                if (players == -1) {
                    warning += "\nPlease select the number of players!";
                }
                if (players == 1 && inviteCode.equals("")) {
                    warning += "\nPlease enter a invite code";
                }
                if (!isMapSelected) {
                    warning += "\nPlease select a map!";
                }
                if (etMoney.getText().toString().equals("")) {
                    warning += "\nPlease enter initial money!";
                }
                Toast.makeText(MainActivity.this, warning, Toast.LENGTH_SHORT).show();

                if (!etNickname.getText().toString().equals("") && players != -1 && (isMapSelected) && !etMoney.getText().toString().equals("")) {
                    Class<?> cls = null;
                    double iniMoney = Double.parseDouble(etMoney.getText().toString());
                    User user1 = new User(uid, 0, 0, iniMoney);
                    User user2 = null;
                    MapJsonResponse myJson = new MapJsonResponse(MainActivity.this);
                    String jsonResponse = myJson.loadJSONFromAsset();
                    Map[] mapJsonResponse = myJson.parseJSON(jsonResponse);
                    if (players == 0) {
                        user2 = new User("computer", 0, 0, iniMoney);
                        cls = GameSingleActivity.class;
                    }
                    if (players == 1 && !etCode.getText().toString().equals("")) {
                        user2 = new User("waiting", 0, 0, iniMoney);
                        cls = GameMultiActivity.class;
                    }
                    myRef.child(etNickname.getText().toString()).child("inviteCode").setValue(etCode.getText().toString());
                    myRef.child(etNickname.getText().toString()).child("user1").setValue(user1);
                    myRef.child(etNickname.getText().toString()).child("user2").setValue(user2);
                    for (int i = 0; i < mapJsonResponse.length; i++) {
                        myRef.child(etNickname.getText().toString()).child("maps").child(Integer.toString(i)).setValue(mapJsonResponse[i]);
                        myRef.child(etNickname.getText().toString()).child("maps").child(Integer.toString(i)).child("owner").setValue("");
                    }

                    Intent goToGame = new Intent(MainActivity.this, cls);
                    goToGame.putExtra("HOST", etNickname.getText().toString());
                    goToGame.putExtra("INVITECODE", etCode.getText().toString());
                    startActivity(goToGame);
                }
            }
        });
    }

    @Override
    public void onDialogPositiveClick(String hostname, String inviteCode) {
        final String hname = hostname;
        final String icode = inviteCode;
        myRef.child(hostname).child("inviteCode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if (value.equals(icode)) {
                    Intent goToGuest = new Intent(MainActivity.this, GameGuestActivity.class);
                    goToGuest.putExtra("HOST", hname);
                    startActivity(goToGuest);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error trying to get invite code" + databaseError);
            }
        });
    }


}

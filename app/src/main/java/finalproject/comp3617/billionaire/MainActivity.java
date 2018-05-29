package finalproject.comp3617.billionaire;


import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources res = getResources();
        int players = res.getInteger(R.integer.players);

        RadioGroup rgPlayers = (findViewById(R.id.rgPlayers));
        Button btGo = (findViewById(R.id.btGo));
        final Spinner spTransportation = findViewById(R.id.spTransportation);
        final Spinner spMap = findViewById(R.id.spMap);

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
                Toast.makeText(MainActivity.this, "你選的是" + spMap.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToMap = new Intent(MainActivity.this, GameActivity.class);
                startActivity(goToMap);
            }
        });


    }


}

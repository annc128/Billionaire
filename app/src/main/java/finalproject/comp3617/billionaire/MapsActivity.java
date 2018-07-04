package finalproject.comp3617.billionaire;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean isStartPointSet = false;
    private String myMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        myMap = getIntent().getStringExtra("MAP");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String googleError = null;
        switch (MapsInitializer.initialize(MapsActivity.this)) { // or GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx)
            case ConnectionResult.SERVICE_MISSING:
                googleError = "Failed to connect to google mapping service: Service Missing";
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                googleError = "Failed to connect to google mapping service: Google Play services out of date. Service Version Update Required";
                break;
            case ConnectionResult.SERVICE_DISABLED:
                googleError = "Failed to connect to google mapping service: Service Disabled. Possibly app is missing API key or is not a signed app permitted to use API key.";
                break;
            case ConnectionResult.SERVICE_INVALID:
                googleError = "Failed to connect to google mapping service: Service Invalid. Possibly app is missing API key or is not a signed app permitted to use API key.";
                break;
//            case ConnectionResult.DATE_INVALID: googleError = "Failed to connect to google mapping service: Date Invalid"; break;
        }
        if (googleError != null)
            Log.d("MyApp", googleError);
        MapJsonResponse myJson = new MapJsonResponse(MapsActivity.this);
        String jsonResponse = myJson.loadJSONFromAsset(myMap);
        Map[] mapJsonResponse = myJson.parseJSON(jsonResponse);

        List<Map> listMaps = Arrays.asList(mapJsonResponse);

        for (Map map : listMaps) {
            String name = map.getName();
            Log.d("MAP", name);
            LatLng location = new LatLng(map.getLatitude(), map.getLongitude());
            mMap.addMarker(new MarkerOptions().position(location).draggable(true).title("Marker in " + name));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(11));
        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(final Marker marker) {
                if (!isStartPointSet) {
                    AlertDialog dialog = new AlertDialog.Builder(MapsActivity.this).setTitle("Start point")
                            .setNegativeButton("No", null).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                    isStartPointSet = true;
                                }
                            })
                            .setMessage("Do you want to set " + marker.getTitle() + " as start point?").create();
                    dialog.show();
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(MapsActivity.this).setTitle("End point")
                            .setNegativeButton("No", null).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                    Intent goToGame = new Intent(MapsActivity.this, GameActivity.class);
                                    startActivity(goToGame);
                                }
                            })
                            .setMessage("Do you want to set " + marker.getTitle() + " as end point?").create();
                    dialog.show();
                }
                return true;
            }
        });
    }
}

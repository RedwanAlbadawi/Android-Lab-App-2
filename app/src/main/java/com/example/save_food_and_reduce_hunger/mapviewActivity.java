package com.example.save_food_and_reduce_hunger;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class mapviewActivity extends FragmentActivity implements OnMapReadyCallback {
    GoogleMap map;
    double lat;
    double longi;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE=101;
    double donorLat,donorLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapview);


        // uncomment when deploy app...

        /*Bundle b = getIntent().getExtras();
        donorLat = b.getDouble("latitude");
        donorLng = b.getDouble("longitude");*/

        //////////////////////////////////////



        // using for testing purpose..remove when deploy application.
        donorLat = 33.652350;
        donorLng = 73.064565;

        /////////////////////////////////////////////////////////////

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();





        lat=33.5969;
        longi=73.0528;
    }

    private void fetchLastLocation() {


        if (ActivityCompat.checkSelfPermission(mapviewActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mapviewActivity.this,

                Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }


        Task<Location> task=fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                currentLocation=location;

                SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(mapviewActivity.this);



            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // check gps unable or not

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            OnGPS();

        }

        else {
            //already on
            map = googleMap;
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            LatLng donorLatLng = new LatLng(donorLat,donorLng);
            googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
            googleMap.addMarker(new MarkerOptions().position(donorLatLng).title("Donor Location"));
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(currentLatLng,donorLatLng);
            Polyline polyline = map.addPolyline(polylineOptions);
            CameraPosition googlePlex = CameraPosition.builder()
                    .target(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()))
                    .zoom(14)
                    .bearing(0)
                    .tilt(45)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 5000, null);

        }

    }


    private void OnGPS() {

        final AlertDialog.Builder builder= new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }


}



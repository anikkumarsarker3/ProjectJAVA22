package com.example.projectjava22;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,LocationListener {
    boolean isPermissionGranted;
    Location selectedLocation;
    private MediaPlayer mediaPlayer;
    Double lat,lon;
    GoogleMap googleMap;
    ImageView imageViewSearch;
    EditText inputLocation;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationManager locationManager;
    String location;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewSearch = findViewById(R.id.imageViewSearch);
        inputLocation = findViewById(R.id.inputLocation);
        cancel = findViewById(R.id.cancelB);


        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        checkPermission();
        //if (isPermissionGranted) {
        if (checkGooglePlayServices()) {
            Toast.makeText(MainActivity.this, "Opening Maps", Toast.LENGTH_LONG).show();
            SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.container, supportMapFragment).commit();
            supportMapFragment.getMapAsync(this);

            if (isPermissionGranted) {
                try {
                    CheckGps();
                    LocationUpdates();
                }
                catch (Exception exx){

                }
                //CheckGps();

            }


        } else {
            Toast.makeText(MainActivity.this, "Google Playservices Not Available", Toast.LENGTH_SHORT).show();
             //}
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                location = null;
                lat = 999999.0;
                lon = 999999.0;
                Intent intent = new Intent(MainActivity.this,FirstActivity.class);
                Toast.makeText(MainActivity.this, "Cancel Alarm Successfully", Toast.LENGTH_LONG).show();
                startActivity(intent);

            }
        });
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                location = inputLocation.getText().toString();

                if(location == null)
                {
                    Toast.makeText(MainActivity.this, "Try Any Location Name",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Geocoder geocoder =new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        List<Address>addressList= geocoder.getFromLocationName(location,1);
                        if (addressList.size()>0)
                        {
                            googleMap.clear();
                            LatLng latLng =new LatLng(addressList.get(0).getLatitude(),addressList.get(0).getLongitude());
                            try {
                                lat = addressList.get(0).getLatitude();
                                lon = addressList.get(0).getLongitude();
                                selectedLocation.setLatitude(lat);
                                selectedLocation.setLongitude(lon);
                            }catch (NullPointerException ex){
                                ex.printStackTrace();
                            }
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.title("My Search position");
                            markerOptions.position(latLng);
                            googleMap.addMarker(markerOptions);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                            googleMap.animateCamera(cameraUpdate);

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(MainActivity.this, "Push The Location", Toast.LENGTH_SHORT).show();
                inputLocation.setText("");

            }
        });


    }

    @SuppressLint("MissingPermission")
    private void LocationUpdates() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000, 1 ,MainActivity.this);

    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError(result)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, 201, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    Toast.makeText(MainActivity.this, "User Cancalled Dialog", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }

        return false;
    }

    private void checkPermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                isPermissionGranted = true;
                LocationUpdates();
                //Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (requestCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Now GPS is Enable", Toast.LENGTH_SHORT).show();
            }
            if (requestCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "Denied GPS is Enable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng latLng = new LatLng(23.5, 90.354);
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.title("My position");
//        markerOptions.position(latLng);
        //googleMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 7);
        googleMap.animateCamera(cameraUpdate);


        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);


        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                try {
                    CheckGps();
                }
                catch (Exception exx){

                }

                return true;
            }
        });
    }

    private void CheckGps() {

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).setAlwaysShow(true);

        Task<LocationSettingsResponse> locationSettingsRequestTask = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());

        locationSettingsRequestTask.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already enable", Toast.LENGTH_SHORT).show();
                    GetCurrentLocationUpdate();


                } catch (ApiException e) {
                    if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        try {
                            resolvableApiException.startResolutionForResult(MainActivity.this, 101);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (e.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                        Toast.makeText(MainActivity.this, "Setting not available", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }

    private void GetCurrentLocationUpdate() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Toast.makeText(MainActivity.this,"Loc:"+ locationResult.getLastLocation().getLatitude()+":"+locationResult.getLastLocation().getLongitude(),Toast.LENGTH_SHORT).show();
                try {
                    Double lal, log ;
                    lal = locationResult.getLastLocation().getLatitude();
                    log = locationResult.getLastLocation().getLongitude();
                    if (Math.abs(lal - lat)<0.041 && Math.abs(lon - log)<0.041)
                    {
                        mediaPlayer.start();
                    }
                    else{
                        mediaPlayer.pause();
                    }

                    }catch (NullPointerException ex){
                        ex.printStackTrace();
                    }
            }
        }, Looper.getMainLooper());

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }
//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.noneMap)
        {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
        if (item.getItemId()==R.id.NormalMap)
        {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        if (item.getItemId()==R.id.SatelliteMap)
        {
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        if (item.getItemId()==R.id.MapHybrid)
        {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        if (item.getItemId()==R.id.MapTerrain)
        {
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        Toast.makeText(this, "Location" + location.getLatitude()+":"+location.getLongitude(), Toast.LENGTH_SHORT).show();

        try {
            Double lal, log ;
            lal = location.getLatitude();
            log = location.getLongitude();
            if (Math.abs(lal - lat)<0.041 && Math.abs(lon - log)<0.041)
            {
                mediaPlayer.start();
            }
            else{
                mediaPlayer.pause();
            }

        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {

        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
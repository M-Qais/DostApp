package com.zillion.dost;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Booking extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    //play service...
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mlocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLAstLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference serviceProviders;
    GeoFire mGeoFire;
    Marker mcurrent;
    Switch location_switch_booking;
    SupportMapFragment mapFragment;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();

        ///initview

        location_switch_booking = findViewById(R.id.location_switch);
        location_switch_booking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    startLocationUpdate();
                    displayLocation();
                    Snackbar.make(mapFragment.getView(), "You are Online", Snackbar.LENGTH_SHORT)
                            .show();

                } else {
                    // do something, the isChecked will be
                    // true if the switch is in the On position

                    stopLocationUpdates();
                    mcurrent.remove();
                    Snackbar.make(mapFragment.getView(), "You are Offline", Snackbar.LENGTH_SHORT)
                            .show();

                }


            }
        });
        //Geofire
        serviceProviders = FirebaseDatabase.getInstance().getReference("Drivers");
        mGeoFire = new GeoFire(serviceProviders);
        setUpLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       switch (requestCode)
       {
           case MY_PERMISSION_REQUEST_CODE:
               if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
               {

                   if(checkplayservices()){
                       buildGoogleApiClient();
                       createLocationRequest();
                       if(location_switch_booking.isChecked())
                           displayLocation();
                   }
               }


       }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //request run time permission...
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION

                    },MY_PERMISSION_REQUEST_CODE);

        }
        else

        {
            if(checkplayservices()){
                buildGoogleApiClient();
                createLocationRequest();
                if(location_switch_booking.isChecked())
                    displayLocation();
            }



        }

    }

    private void createLocationRequest() {

        mlocationRequest=new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTERVAL);
        mlocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkplayservices() {

        int resultCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!=ConnectionResult.SUCCESS){

            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RES_REQUEST).show();
            else{

                Toast.makeText(this, "This device is not supoorted ", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        // code here to show dialog
        super.onBackPressed();
        mAuth.signOut();// optional depending on your needs
    }

    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);

    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLAstLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLAstLocation != null) {

            if (location_switch_booking.isChecked()) {

                final double latitude = mLAstLocation.getLatitude();
                final double longitude = mLAstLocation.getLongitude();

                //update to firebase
                mGeoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //add marker...

                        if (mcurrent != null)
                            mcurrent.remove();
                        mcurrent = mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.driver))
                                .position(new LatLng(latitude, longitude))
                                .title("you"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
                        rotateMarker(mcurrent, -360, mMap);
                    }
                });

            }
            else{
                Log.d("check","cannot get your location");
                Toast.makeText(this, "Cannot get your Location..", Toast.LENGTH_SHORT).show();


            }


        }
    }

    private void rotateMarker(final Marker mcurrent, final float i, GoogleMap mMap) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = mcurrent.getRotation();
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                float rot = t * i + (1 - t) * startRotation;
                mcurrent.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {

                    handler.postDelayed(this, 16);
                }
            }
        });

    }

    private void startLocationUpdate() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mlocationRequest,this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLAstLocation = location;
        displayLocation();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        startLocationUpdate();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

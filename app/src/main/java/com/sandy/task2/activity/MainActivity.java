package com.sandy.task2.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sandy.task2.R;
import com.sandy.task2.utils.PermissionUtils;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    /**
     * FusedLocationProviderApi Save request parameters
     */
    private LocationRequest mLocationRequest;


    /**
     * Provide callbacks for location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * An object representing the current location
     */
    private Location mCurrentLocation;

    //A client that handles connection / connection failures for Google locations
    // (changed from play-services 11.0.0)
    private FusedLocationProviderClient mFusedLocationClient;

    private String provider;
    private GoogleMap mMap;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.floatingActionButton)
    FloatingActionButton floatingActionButton;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ProgressBar)
    ProgressBar ProgressBar;

    double curr_latitue,curr_longi,dest_lati,dest_longi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        provider = getIntent().getStringExtra("provider");

        checkMyPermissionLocation();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(googleMap -> {
            /*if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }*/
            mMap = googleMap;
            mMap.getUiSettings().setZoomControlsEnabled(true);
            //mMap.setMyLocationEnabled(true);
        });

        floatingActionButton.setOnClickListener(v -> showDirections());
    }



    private void checkMyPermissionLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Permission Check
            PermissionUtils.requestPermission(this);
        } else {
            //If you're authorized, start setting your location
            initGoogleMapLocation();
        }
    }

    private void initGoogleMapLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        /*
         * Location Setting API to
         */
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);
        /*
         * Callback returning location result
         */
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                super.onLocationResult(result);
                //mCurrentLocation = locationResult.getLastLocation();
                mCurrentLocation = result.getLocations().get(0);


                if(mCurrentLocation!=null)
                {
                    Log.e("Location(Lat)==",""+mCurrentLocation.getLatitude());
                    Log.e("Location(Long)==",""+mCurrentLocation.getLongitude());

                    curr_latitue = mCurrentLocation.getLatitude();

                    curr_longi = mCurrentLocation.getLongitude();

                    Log.w("MainActivity","curr_latitue : "+curr_latitue+"curr_longi : "+curr_longi);
                }

                MarkerOptions options = new MarkerOptions();
                assert mCurrentLocation != null;
                options.position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                options.icon(icon);
                Marker marker = mMap.addMarker(options);

                assert marker != null;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
                ProgressBar.setVisibility(View.GONE);
                /*
                 * To get location information consistently
                 * mLocationRequest.setNumUpdates(1) Commented out
                 * Uncomment the code below
                 */
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }

            //Locatio nMeaning that all relevant information is available
            @Override
            public void onLocationAvailability(@NonNull LocationAvailability availability) {
                //boolean isLocation = availability.isLocationAvailable();
            }
        };
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        //To get location information only once here
        mLocationRequest.setNumUpdates(3);
        if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
            //Accuracy is a top priority regardless of battery consumption
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }else{
            //Acquired location information based on balance of battery and accuracy (somewhat higher accuracy)
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        /*
         * Stores the type of location service the client wants to use. Also used for positioning.
         */
        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        Task<LocationSettingsResponse> locationResponse = mSettingsClient.checkLocationSettings(mLocationSettingsRequest);
        locationResponse.addOnSuccessListener(this, locationSettingsResponse -> {
            Log.e("Response", "Successful acquisition of location information!!");
            //
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        });
        //When the location information is not set and acquired, callback
        locationResponse.addOnFailureListener(this, e -> {
            int statusCode = ((ApiException) e).getStatusCode();
            switch (statusCode) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.e("onFailure", "Location environment check");
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    String errorMessage = "Check location setting";
                    Log.e("onFailure", errorMessage);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //If the request code does not match
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PermissionUtils.REQUEST_CODE) {
            return;
        }
        if (PermissionUtils.isPermissionGranted(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION}, grantResults)) {
            //If you have permission, go to the code to get the location value
            initGoogleMapLocation();
        } else {
            Toast.makeText(this, "Stop apps without permission to use location information", Toast.LENGTH_SHORT).show();
            checkMyPermissionLocation();
            //finish();
        }
    }

    /**
     * Remove location information
     */
    @Override
    public void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void showDirections() {


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
// ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.directions_popup_layout, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setCancelable(false);

        ImageView img_close = dialogView.findViewById(R.id.img_close);

        EditText edt_destn = dialogView.findViewById(R.id.edt_destn);

        Button btn_submit = dialogView.findViewById(R.id.btn_submit);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        btn_submit.setOnClickListener(v -> {
            getLocationFromAddress(MainActivity.this,edt_destn.getText().toString());

            alertDialog.dismiss();
        });

        img_close.setOnClickListener(v -> alertDialog.dismiss());
    }

    public void getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address != null) {
                Address location = address.get(0);
                p1 = new LatLng(location.getLatitude(), location.getLongitude() );

                dest_lati = location.getLatitude();

                dest_longi = location.getLongitude();

                Log.w("MainActivity","dest_lati : "+dest_lati+"dest_longi : "+dest_longi);

                gotoMapsActivity();

            }


        } catch (IOException ex) {

            ex.printStackTrace();
        }

    }

    private void gotoMapsActivity() {

        Intent intent = new Intent(MainActivity.this, MapsActivity.class);

        intent.putExtra("curr_latitue",curr_latitue);

        intent.putExtra("curr_longi",curr_longi);

        intent.putExtra("dest_lati",dest_lati);

        intent.putExtra("dest_longi",dest_longi);

        startActivity(intent);

        finish();
    }

}

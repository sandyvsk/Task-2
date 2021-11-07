package com.sandy.task2.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sandy.task2.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback ,GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener{

    private GoogleMap mMap;

    MarkerOptions origin, destination;

    double curr_latitue,curr_longi,dest_lati,dest_longi;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ProgressBar)
    ProgressBar ProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            curr_latitue = extras.getDouble("curr_latitue");

            curr_longi = extras.getDouble("curr_longi");

            dest_lati = extras.getDouble("dest_lati");

            dest_longi = extras.getDouble("dest_longi");
        }

        Log.w("MapsActivity","curr_latitue : "+curr_latitue+"curr_longi : "+curr_longi);

        Log.w("MapsActivity","dest_lati : "+dest_lati+"dest_longi : "+dest_longi);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Setting marker to draw route between these two points
        origin = new MarkerOptions().position(new LatLng(curr_latitue, curr_longi)).snippet("origin");

        destination = new MarkerOptions().position(new LatLng(dest_lati, dest_longi)).snippet("destination");

    }


    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this tutorial, we add polylines and polygons to represent routes and areas on the map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.addMarker(origin);
        mMap.addMarker(destination);


        Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                .add(new LatLng(curr_latitue, curr_longi), new LatLng(dest_lati, dest_longi))
                .width(25)
                .color(Color.RED)
                .geodesic(true));
/*
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin.getPosition(), 10));*/

        // Position the map's camera
        // and set the zoom factor so most of Australia shows on the screen.

        ProgressBar.setVisibility(View.GONE);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin.getPosition(), 14));

        // Set listeners for click events.
        googleMap.setOnPolylineClickListener(this);
        googleMap.setOnPolygonClickListener(this);

    }

    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {

    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

    }
}
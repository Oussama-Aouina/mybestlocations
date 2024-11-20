package com.oussamaaouina.mybestlocation.ui.slideshow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oussamaaouina.mybestlocation.R;
import com.oussamaaouina.mybestlocation.databinding.ActivityMapsBinding;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {
    private GoogleMap googleMap;
    private ActivityMapsBinding binding;
    private double longitude, latitude;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        longitude = Double.parseDouble(getIntent().getStringExtra("longitude"));
        latitude = Double.parseDouble(getIntent().getStringExtra("latitude"));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMapClickListener(this);
        LatLng position = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        MarkerOptions options = new MarkerOptions().position(position).title("Save");
        // enable zoom in and zoom out
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        // set the color of the marker
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        marker = googleMap.addMarker(options);
        googleMap.setOnMarkerClickListener(this);


    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // when clicking on a place in the map add a new marker
        marker.setPosition(latLng);

    }


    @Override

    public boolean onMarkerClick(Marker marker) {
        // Save the marker's position
        LatLng position = marker.getPosition();
        Log.e("MarkerClick", "Position: " + position);

        // Save in the SharedPreferences
//        SharedPreferences sharedPreferences = getSharedPreferences("LocationPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putFloat("marker_lat", (float) position.latitude);
//        editor.putFloat("marker_lng", (float) position.longitude);
//        editor.apply();

        // Log the values being sent
        Log.e("MarkerClick", "Sending lat: " + position.latitude + ", lng: " + position.longitude);

        // Navigate to the slideshow fragment
        setResult(1, new Intent().setData(Uri.parse("marker_lat=" + position.latitude + "&marker_lng=" + position.longitude)));
        finish();
        return true;
    }
}

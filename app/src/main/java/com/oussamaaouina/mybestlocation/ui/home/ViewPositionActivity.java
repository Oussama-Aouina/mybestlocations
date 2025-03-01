package com.oussamaaouina.mybestlocation.ui.home;

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

public class ViewPositionActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private ActivityMapsBinding binding;
    private double longitude, latitude;
    private Marker marker;

    @Override
    public void onCreate(Bundle savedBundel){
        super.onCreate(savedBundel);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        longitude = Double.parseDouble(getIntent().getStringExtra("longitude"));
        latitude = Double.parseDouble(getIntent().getStringExtra("latitude"));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        LatLng position = new LatLng(latitude, longitude);
        Log.d("MapCoordinates", "Latitude: " + latitude + ", Longitude: " + longitude);

        // Move the camera to the position and set zoom level
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 5));

        MarkerOptions options = new MarkerOptions()
                .position(position)
                .title("Save")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        // Enable zoom controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Add the marker
        marker = googleMap.addMarker(options);

        if (marker != null) {
            Log.d("MapMarker", "Marker added successfully at: " + position);
        } else {
            Log.d("MapMarker", "Failed to add marker.");
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}

package com.oussamaaouina.mybestlocation.ui.slideshow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.oussamaaouina.mybestlocation.Config;
import com.oussamaaouina.mybestlocation.JSONParser;
import com.oussamaaouina.mybestlocation.R;
import com.oussamaaouina.mybestlocation.databinding.FragmentSlideshowBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;

public class AddLocationFragment extends Fragment implements LocationListener {

    FusedLocationProviderClient fusedLocationProviderClient;
    private FragmentSlideshowBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button add = binding.addBtn;
        Button map = binding.mapBtn;
        Button back = binding.backBtn;

        showLocation();

        // Handle "Show Location" button click
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.nav_home);

            }
        });

        // Handle "Save Position" button click
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate phone number format before proceeding
                String phoneNumber = binding.textNumero.getText().toString().trim();

                // Create parameters map with validated input
                LinkedHashMap<String, String> params = new LinkedHashMap<>();
                params.put("longitude", binding.textLongitude.getText().toString().trim());
                params.put("latitude", binding.textLatitude.getText().toString().trim());
                try {
                    params.put("numero", URLEncoder.encode(phoneNumber, "UTF-8")); // URL encode the phone number
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                params.put("pseudo", binding.textPseudo.getText().toString().trim());

                // Validate all required fields
                if (validateFields(params)) {
                    Upload u = new Upload(params);
                    u.execute();
                } else {
                    Toast.makeText(getContext(), "Please fill all fields correctly", Toast.LENGTH_LONG).show();
                }
            }
        });



        // Handle "Back to Map" button click
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent maps = new Intent(getActivity(), MapsActivity.class);
                maps.putExtra("longitude", binding.textLongitude.getText().toString());
                maps.putExtra("latitude", binding.textLatitude.getText().toString());
                startActivityForResult(maps, 1);
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            String dataString = data.getDataString();
            Log.e("position_received:", dataString);
            String[] parts = dataString.split("&");
            // Extract the values from the parts
            String marker_lat = parts[0].split("=")[1];
            String marker_lng = parts[1].split("=")[1];
            binding.textLatitude.setText(marker_lat);
            binding.textLongitude.setText(marker_lng);
        }

    }


    // show realtime location
    @SuppressLint("MissingPermission")
    public void showLocation() {

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e("location", "gps is enabled");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);

        } else {
            Toast.makeText(getContext(), "Please turn on your GPS location", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Log.e("location", "location: " + location.getLatitude() + " " + location.getLongitude());
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            Log.e("location address", "address:" + addresses.get(0).getAddressLine(0));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        binding.textLatitude.setText(String.valueOf(location.getLatitude()));
                        binding.textLongitude.setText(String.valueOf(location.getLongitude()));
                    }

                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister the location listener
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        binding = null; // Set binding to null to avoid memory leaks
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.e("Location change:", location.toString());
        if (binding != null) { // Check if binding is not null
            binding.textLatitude.setText(String.valueOf(location.getLatitude()));
            binding.textLongitude.setText(String.valueOf(location.getLongitude()));
        } else {
            Log.e("SlideshowFragment", "Binding is null, cannot update UI.");
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

    AlertDialog alert;

    // saving the informations
    class Upload extends AsyncTask<Void, Void, Boolean> {
        private LinkedHashMap<String, String> params;
        private WeakReference<Context> contextRef;

        public Upload(LinkedHashMap<String, String> params) {
            this.params = params;
            this.contextRef = new WeakReference<>(getContext());
        }

        @Override
        protected void onPreExecute() {
            if (getActivity() != null && !getActivity().isFinishing()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Upload");
                builder.setMessage("Uploading...");
                alert = builder.create();
                alert.show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... objects) {
            // Validate parameters
            if (params == null || params.isEmpty()) {
                return false;
            }

            // Check for empty values
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                    Log.e("Upload", "Empty value for key: " + entry.getKey());
                    return false;
                }
            }

            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeHttpRequest(Config.url_add, "POST", params);

            try {
                return response != null && response.getInt("success") == 1;
            } catch (JSONException e) {
                Log.e("Upload", "Error parsing response", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // Safely dismiss dialog
            if (alert != null && alert.isShowing()) {
                Context context = contextRef.get();
                if (context != null && !((Activity) context).isFinishing()) {
                    alert.dismiss();
                }
            }

            if (success) {
                // Clear fields only if successful
                if (binding != null) {
                    binding.textLongitude.setText("");
                    binding.textLatitude.setText("");
                    binding.textNumero.setText("");
                    binding.textPseudo.setText("");

                    // Navigate back
                    NavController navController = Navigation.findNavController(requireActivity(),
                            R.id.nav_host_fragment_content_main);
                    navController.navigate(R.id.nav_home);

                    Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                }
            } else {
                Context context = contextRef.get();
                if (context != null) {
                    Toast.makeText(context, "Upload failed. Please check your connection and try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private boolean validateFields(LinkedHashMap<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.trim().isEmpty()) {
                return false;
            }

            // Special validation for phone number
            if (entry.getKey().equals("numero")) {
                // Allow +, digits, and common phone number characters
                if (!value.matches("^[+\\d\\-\\s()]*$")) {
                    Toast.makeText(getContext(), "Invalid phone number format", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        return true;
    }

}
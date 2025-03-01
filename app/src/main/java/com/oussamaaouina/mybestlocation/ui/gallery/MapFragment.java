package com.oussamaaouina.mybestlocation.ui.gallery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oussamaaouina.mybestlocation.Config;
import com.oussamaaouina.mybestlocation.JSONParser;
import com.oussamaaouina.mybestlocation.Position;
import com.oussamaaouina.mybestlocation.R;
import com.oussamaaouina.mybestlocation.ui.home.PositionRecyclerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Position> positions = new ArrayList<>();
    private EditText searchBox;
    private ImageView add_postion, refresh, list_button;

    @SuppressLint("WrongViewCast")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_list);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize the search box
        searchBox = root.findViewById(R.id.search_location);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMarkers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Initialize the buttons
        add_postion = root.findViewById(R.id.add_location_button_map);
        refresh = root.findViewById(R.id.refresh_button_map);
        list_button = root.findViewById(R.id.list_button);

        // Set click listeners for the buttons
        add_postion.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_slideshow);
        });

        refresh.setOnClickListener(v -> {
            new DownloadPositionsTask().execute();
            Toast.makeText(getActivity(), "Positions refreshed", Toast.LENGTH_SHORT).show();
        });

        list_button.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_home);
        });

        // Start data download
        new DownloadPositionsTask().execute();

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        // Set a marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            Position position = (Position) marker.getTag();
            if (position != null) {
                showPositionDialog(position);
            }
            return false;
        });
    }

    private void filterMarkers(String query) {
        if (mMap == null || query.isEmpty()) return;

        mMap.clear(); // Clear all existing markers
        for (Position position : positions) {
            if (position.pseudo.toLowerCase().contains(query.toLowerCase()) ||
                    position.numero.contains(query)) {
                addMarkerToMap(position); // Re-add markers that match the query
            }
        }
    }

    private void showPositionDialog(Position position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.position_recycler, null);
        builder.setView(dialogView);

        ImageView callIcon = dialogView.findViewById(R.id.btn_call_contact);
        ImageView msgIcon = dialogView.findViewById(R.id.btn_msg_contact);
        ImageView mapIcon = dialogView.findViewById(R.id.btn_map);
        ImageView deleteIcon = dialogView.findViewById(R.id.btn_delete_position);
        TextView tvpseudo = dialogView.findViewById(R.id.tv_pseudo_contact);
        TextView tvnum = dialogView.findViewById(R.id.tv_num_contact);
        TextView tvlongitude = dialogView.findViewById(R.id.tv_longitude);
        TextView tvlatuitude = dialogView.findViewById(R.id.tv_latitude);

        tvpseudo.setText(position.pseudo);
        tvnum.setText(position.numero);
        tvlongitude.setText(position.longitude);
        tvlatuitude.setText(position.latitude);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        callIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + position.numero));
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                } else {
                    Toast.makeText(requireContext(), "Call permission not granted", Toast.LENGTH_SHORT).show();
                }

            }
        });

        msgIcon.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           SmsManager smsManager = SmsManager.getDefault();
                                           smsManager.sendTextMessage('+'+position.numero, null, "Hello " + position.pseudo, null, null);
                                           Toast.makeText(requireContext(), "Message sent to " + position.pseudo, Toast.LENGTH_SHORT).show();
                                           alertDialog.dismiss();
                                       }});

        mapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a Uri for Google Maps directions
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + position.latitude + "," + position.longitude);

                // Create an Intent to open Google Maps
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                // Verify that Google Maps is installed
                if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(requireContext(), "Google Maps is not installed", Toast.LENGTH_SHORT).show();
                }

                alertDialog.dismiss();
            }
        });

        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = position.id;
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Confirmation")
                        .setMessage("Are you sure you want to delete this location?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            new Delete(position.id).execute(); // Execute delete task
                            Toast.makeText(requireContext(), "Position deleted", Toast.LENGTH_SHORT).show();
                            // i want it now to refresh the list of positions and revisit this fragment again

                            new DownloadPositionsTask().execute();
                            alertDialog.dismiss();
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                            navController.navigate(R.id.nav_gallery);

                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });


//        builder.setTitle(position.pseudo);
//        builder.setMessage("\nNumber: " + position.numero +
//                "\nLongitude: " + position.longitude +
//                "\nLatitude: " + position.latitude);
//
//        builder.setPositiveButton("Call", (dialog, which) -> {
//            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + position.numero));
//            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
//                startActivity(callIntent);
//            } else {
//                Toast.makeText(requireContext(), "Call permission not granted", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        builder.setNeutralButton("Message", (dialog, which) -> {
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(position.numero, null, "Hello " + position.pseudo, null, null);
//            Toast.makeText(requireContext(), "Message sent to " + position.pseudo, Toast.LENGTH_SHORT).show();
//        });
//
//        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
//        builder.show();
    }



    private void addMarkerToMap(Position position) {
        LatLng location = new LatLng(Double.parseDouble(position.latitude), Double.parseDouble(position.longitude));
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(position.pseudo)
                .snippet(position.numero) // Optional, add extra info if needed
                .snippet("Lat: " + position.latitude + ", Lng: " + position.longitude) // Optional, add extra info if needed
                .visible(true));

        if (marker != null) {
            marker.setTag(position);
            marker.showInfoWindow(); // Show the info window immediately
        }
    }

    private class DownloadPositionsTask extends AsyncTask<Void, Void, List<Position>> {

        @Override
        protected List<Position> doInBackground(Void... voids) {
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(Config.url_getAll);
            List<Position> downloadedPositions = new ArrayList<>();
            try {
                int success = response.getInt("success");
                if (success == 1) {
                    JSONArray positionsArray = response.getJSONArray("positions");
                    for (int i = 0; i < positionsArray.length(); i++) {
                        JSONObject obj = positionsArray.getJSONObject(i);
                        int id = obj.getInt("id");
                        String pseudo = obj.getString("pseudo");
                        String longitude = obj.getString("longitude");
                        String latitude = obj.getString("latitude");
                        String numero = obj.getString("numero");
                        Position position = new Position(id, pseudo, longitude, latitude, numero);
                        downloadedPositions.add(position);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return downloadedPositions;
        }

        @Override
        protected void onPostExecute(List<Position> downloadedPositions) {
            positions.clear();
            positions.addAll(downloadedPositions);

            // Add markers for all positions
            for (Position position : positions) {
                addMarkerToMap(position);
            }
        }
    }
    AlertDialog alert;

    private class Delete extends AsyncTask {

        int id;
        HashMap<String, String> params;

        public Delete(int id) {
            this.id = id;
            this.params = new HashMap<>();
            this.params.put("id", id + "");
        }

        @Override
        protected void onPreExecute() {
            // UI Thread
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Delete");
            builder.setMessage("Deleting...");
            alert = builder.create();
            alert.show();
        }


        @Override
        protected Object doInBackground(Object[] objects) {
            // Code de thread secondaire (background)
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeHttpRequest(Config.url_delete, "POST", params);
            try {
                int success = response.getInt("success");
                Log.e("response", "==" + success);
                if (success == 1) {
                    Log.e("response", "===" + response);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            // UI Thread (Thread principal)
            super.onPostExecute(o);
            alert.dismiss();
        }
    }
}
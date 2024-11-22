package com.oussamaaouina.mybestlocation.ui.home;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.oussamaaouina.mybestlocation.Config;
import com.oussamaaouina.mybestlocation.JSONParser;
import com.oussamaaouina.mybestlocation.Position;
import com.oussamaaouina.mybestlocation.R;
import com.oussamaaouina.mybestlocation.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeFragment extends Fragment {
    ArrayList<Position> data = new ArrayList<Position>();
    ArrayList<Position> filteredList = new ArrayList<Position>();

    RecyclerView rv;
    EditText searchContact;
    ImageView addContact, logout;
    PositionRecyclerAdapter recyclerAdapter;



    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        rv = root.findViewById(R.id.rv_positions);
        searchContact = root.findViewById(R.id.search_position);
        addContact = root.findViewById(R.id.add_contact_button);
        logout = root.findViewById(R.id.logout_button);

        // excuting the download when the view created
        Download de = new Download();
        de.execute();

        // refresh button to redownload the data
        binding.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Download d = new Download();
                d.execute();
            }
        });

        // add button to add new position
        binding.addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.nav_slideshow);
            }
        });


        binding.rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Check if the touch event is outside the search box
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    if (searchContact.isFocused()) {
                        // Check if the touch is outside the search box
                        int[] location = new int[2];
                        searchContact.getLocationOnScreen(location);
                        float x = event.getRawX() + searchContact.getLeft() - location[0];
                        float y = event.getRawY() + searchContact.getTop() - location[1];

                        if (x < 0 || x > searchContact.getWidth() || y < 0 || y > searchContact.getHeight()) {
                            // Clear focus from the search box
                            searchContact.clearFocus();
                            // Hide the keyboard
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(searchContact.getWindowToken(), 0);
                            }
                        }
                    }
                }
                return false; // Return false to allow other touch events to be processed
            }
        });


        ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS}, 1);

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    AlertDialog alert;

    class Download extends AsyncTask {
        @Override
        protected void onPreExecute() {
            // UI Thread
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Download");
            builder.setMessage("Downloading...");
            alert = builder.create();
            alert.show();

        }

        @Override
        protected Object doInBackground(Object[] objects) {
            // Code de thread secondaire (background)
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // problem: pas d'acces a l'interface graphique
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(Config.url_getAll);

            try {
                int success = response.getInt("success");
                Log.e("response", "==" + success);
                if (success == 1) {
                    data.clear();
                    JSONArray positions = response.getJSONArray("positions");
                    Log.e("response", "==" + response);
                    for (int i = 0; i < positions.length(); i++) {
                        JSONObject obj = positions.getJSONObject(i);
                        int id = obj.getInt("id");
                        String pseudo = obj.getString("pseudo");
                        String longitude = obj.getString("longitude");
                        String latitude = obj.getString("latitude");
                        String numero = obj.getString("numero");
                        Position p = new Position(id, pseudo, longitude, latitude, numero);
                        data.add(p);
                    }

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
            filteredList.clear();
            filteredList.addAll(data);
            recyclerAdapter = new PositionRecyclerAdapter(getActivity(), filteredList);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1, RecyclerView.VERTICAL, false);
            rv.setAdapter(recyclerAdapter);
            rv.setLayoutManager(gridLayoutManager);
            searchContact.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Do nothing
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterLocations(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    filterLocations(s.toString());
                }
            });


        }
    }

    class Delete extends AsyncTask {

        int id;
        HashMap<String,String> params;
        public Delete(int id) {
            this.id = id;
            this.params = new HashMap<>();
            this.params.put("id", id + "");
        }
        @Override
        protected void onPreExecute() {
            // UI Thread
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Delete");
            builder.setMessage("Deleting...");
            alert = builder.create();
            alert.show();
        }


        @Override
        protected Object doInBackground(Object[] objects) {
            // Code de thread secondaire (background)
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeHttpRequest(Config.url_delete,"POST",params);
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


        }}


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Toast.makeText(this.getContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Permission denied
                Toast.makeText(this.getContext(), "Permission denied", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private void filterLocations(String query) {
        filteredList.clear(); // Clear the previous filtered list
        if (query.isEmpty()) {
            // If query is empty, add all contacts back to the filtered list
            filteredList.addAll(data);
        } else {
            // Convert query to lowercase for case-insensitive search
            String lowerCaseQuery = query.toLowerCase();

            for (Position poistion : data) {
                // Check if the query matches the name, pseudo, or number (case-insensitive)
                if (
                        poistion.pseudo.toLowerCase().contains(lowerCaseQuery) ||
                                poistion.numero.toLowerCase().contains(lowerCaseQuery)) {

                    filteredList.add(poistion); // Add matching contacts to the filtered list
                }
            }
        }

        // Notify the adapter that the data has changed
        recyclerAdapter.notifyDataSetChanged();
    }

}
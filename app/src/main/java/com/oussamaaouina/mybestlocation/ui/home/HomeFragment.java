package com.oussamaaouina.mybestlocation.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.oussamaaouina.mybestlocation.Config;
import com.oussamaaouina.mybestlocation.JSONParser;
import com.oussamaaouina.mybestlocation.Position;
import com.oussamaaouina.mybestlocation.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    ArrayList<Position> data = new ArrayList<>();
    ArrayAdapter<Position> adapter;
    ListView listView;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Download de = new Download();
        de.execute();
        binding.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Download d = new Download();
              d.execute();
            }
        });
        final TextView textView = binding.textHome;
        listView = binding.listLocations;

        ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

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
                if(success == 1 ){
                    JSONArray positions=response.getJSONArray("positions");
                    Log.e("response", "==" + response);
                    for (int i = 0; i < positions.length(); i++) {
                        JSONObject obj = positions.getJSONObject(i);
                        int id = obj.getInt("id");
                        String pseudo = obj.getString("pseudo");
                        String longitude = obj.getString("longitude");
                        String latitude = obj.getString("latitude");
                        String numero = obj.getString("numero");
                        Position p = new Position(id,pseudo,longitude,latitude,numero);
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
            listView.setAdapter(new ArrayAdapter(getActivity(),
                    android.R.layout.simple_list_item_1,
                    data));

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if(grantResults.length > 0){
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Toast.makeText(this.getContext(),"Permission granted",Toast.LENGTH_SHORT).show();
                }
            }
            else {
                // Permission denied
                Toast.makeText(this.getContext(), "Permission denied", Toast.LENGTH_SHORT).show();

            }
        }

    }
}
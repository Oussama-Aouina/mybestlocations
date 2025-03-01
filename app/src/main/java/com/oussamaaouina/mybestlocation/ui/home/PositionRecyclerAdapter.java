package com.oussamaaouina.mybestlocation.ui.home;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.oussamaaouina.mybestlocation.Config;
import com.oussamaaouina.mybestlocation.JSONParser;
import com.oussamaaouina.mybestlocation.Position;
import com.oussamaaouina.mybestlocation.R;
import com.oussamaaouina.mybestlocation.ui.slideshow.MapsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PositionRecyclerAdapter extends RecyclerView.Adapter<PositionRecyclerAdapter.MyViewHolder> {
    Context con;
    ArrayList<Position> data;

    public PositionRecyclerAdapter(Context con, ArrayList<Position> data) {
        this.con = con;
        this.data = data;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // creation des view holders selon la taille de l'ecran + 2


        //creation d'un view
        LayoutInflater inflater = LayoutInflater.from(con);
        View v = inflater.inflate(R.layout.position_recycler, null);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Position bestlocation = data.get(position);

        holder.tvpseudo.setText(bestlocation.pseudo);
        holder.tvnum.setText(bestlocation.numero);

        holder.tvlongitude.setText(bestlocation.longitude);
        holder.tvlatuitude.setText(bestlocation.latitude);

        // Set click listeners for the message,call, delete, and edit icons
        holder.msgIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessageDialog(bestlocation);
            }
        });
        //set the clicklistner for the phone call
        holder.callIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = holder.getAdapterPosition();//indice de l'element selectionné
                Position c = data.get(index);
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + c.numero));
                con.startActivity(intent);
            }
        });

        //set the click listner to delete contact
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = holder.getAdapterPosition();
                Position c = data.get(index);
                new AlertDialog.Builder(con)
                        .setTitle("Delete Confirmation")
                        .setMessage("Are you sure you want to delete this location?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            new Delete(c.id).execute(); // Execute delete task
                            Toast.makeText(con, "Position deleted", Toast.LENGTH_SHORT).show();
                            data.remove(index);
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();

                notifyDataSetChanged();

            }
        });


        // view in map
        holder.mapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = holder.getAdapterPosition();
                Position c = data.get(index);
                Intent maps = new Intent(con, ViewPositionActivity.class);
                maps.putExtra("longitude", c.longitude.toString());
                maps.putExtra("latitude", c.latitude.toString());
                con.startActivity(maps);
            }
        });
    }


    private void showMessageDialog(Position bestlocation) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(con);
        LayoutInflater inflater = LayoutInflater.from(con);
        View dialogView = inflater.inflate(R.layout.send_message_dialog, null); // Ensure this matches the correct XML file
        dialogBuilder.setView(dialogView);

        // Make sure these IDs match the ones in your XML layout
        EditText msgContent = dialogView.findViewById(R.id.dialog_msg_content);
        Button sendButton = dialogView.findViewById(R.id.dialog_send_button);
        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_msg);
        TextView receiverName = dialogView.findViewById(R.id.dialog_msg_reciever);
        // Create and show the dialog
        receiverName.setText(bestlocation.pseudo);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        // Set onClickListener for send button
        sendButton.setOnClickListener(v -> {
            // Handle sending the message here, e.g., get the message text
            String message = msgContent.getText().toString();
            // Do something with the message (send it, store it, etc.)
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage('+'+bestlocation.numero, null, message, null, null);
            Toast.makeText(con, "Message: " + message, Toast.LENGTH_SHORT).show();
            alertDialog.dismiss();
        });

        // Set onClickListener for cancel button
        cancelButton.setOnClickListener(v -> alertDialog.dismiss());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvpseudo;
        TextView tvnum;
        TextView tvlongitude;
        TextView tvlatuitude;
        ImageView callIcon, deleteIcon, mapIcon, msgIcon;

        public MyViewHolder(@NonNull View v) {
            super(v);

            this.tvpseudo = v.findViewById(R.id.tv_pseudo_contact);
            this.tvnum = v.findViewById(R.id.tv_num_contact);
            this.callIcon = v.findViewById(R.id.btn_call_contact); // Assuming this is the ID of your call icon
            this.mapIcon = v.findViewById(R.id.btn_map);
            this.deleteIcon = v.findViewById(R.id.btn_delete_position);
            this.msgIcon = v.findViewById(R.id.btn_msg_contact);
            this.tvlongitude = v.findViewById(R.id.tv_longitude);
            this.tvlatuitude = v.findViewById(R.id.tv_latitude);
        }
    }

    AlertDialog alert;

    class Delete extends AsyncTask {

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
            AlertDialog.Builder builder = new AlertDialog.Builder(con);
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

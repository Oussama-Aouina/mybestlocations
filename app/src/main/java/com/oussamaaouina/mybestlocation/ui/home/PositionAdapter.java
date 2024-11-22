package com.oussamaaouina.mybestlocation.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.oussamaaouina.mybestlocation.Position;
import com.oussamaaouina.mybestlocation.R;

import java.util.ArrayList;
import java.util.List;

public class PositionAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Position> data;

    public PositionAdapter(Context context, ArrayList<Position> data) {
        this.context = context;
        this.data = data;
    }

    public void updateData(ArrayList<Position> newPositions) {
        this.data = newPositions;
        notifyDataSetChanged();
    }

    public void removePosition(Position position) {
        data.remove(position);
        notifyDataSetChanged();
    }



    public ArrayList<Position> getPositions() {
        return data;
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Position bestlocation = data.get(position);
        // Check if convertView is null, reuse it if it's not null
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.position_recycler, parent, false);
        }
        //creation d'un view
        LayoutInflater inflater =LayoutInflater.from(context);
        View v= inflater.inflate(R.layout.position_recycler,null);

        //recuperation des holders:

        TextView tvpseudo = v.findViewById(R.id.tv_pseudo_contact);
        TextView tvnum = v.findViewById(R.id.tv_num_contact);
        TextView tvlongitude = v.findViewById(R.id.tv_longitude);
        TextView tvlatuitude = v.findViewById(R.id.tv_latitude);

        //affectation des holders

        tvpseudo.setText(bestlocation.pseudo);
        tvnum.setText(bestlocation.numero);
        tvlongitude.setText(bestlocation.longitude);
        tvlatuitude.setText(bestlocation.latitude);

        return v;
    }
}

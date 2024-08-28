package com.example.ecg_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<Patient> list;

    public MyAdapter(Context context, ArrayList<Patient> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Patient patient = list.get(position);
        holder.patientName.setText(patient.getPatientName());
        holder.age.setText(patient.getPatientAge());
        holder.mobileNumber.setText(patient.getMobileNumber());
        holder.place.setText(patient.getPlace());
        holder.result.setText(patient.getResult());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends  RecyclerView.ViewHolder{
        TextView patientName, age, mobileNumber, place, result;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.patient_name);
            age = itemView.findViewById(R.id.patient_age);
            mobileNumber = itemView.findViewById(R.id.mobile_number);
            place = itemView.findViewById(R.id.patient_place);
            result = itemView.findViewById(R.id.result);
        }
    }
}

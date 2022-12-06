package com.example.scanme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.MyViewHolder> {

    private Context context;
    private Activity activity;
    private ArrayList input, result, id;

    ResultAdapter(Activity activity, Context context, ArrayList id, ArrayList input, ArrayList result){
        this.activity = activity;
        this.context = context;
        this.id = id;
        this.input = input;
        this.result = result;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_result, parent, false);
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.txtinput.setText(String.valueOf(input.get(position)));
        holder.txtresult.setText(formatNumberCurrency(String.valueOf(result.get(position))));
    }

    @Override
    public int getItemCount() {
        return id.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtinput, txtresult;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtinput = itemView.findViewById(R.id.txtinput);
            txtresult = itemView.findViewById(R.id.txtresult);
        }

    }
    private static String formatNumberCurrency(String numberr) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        return formatter.format(Double.parseDouble(numberr));
    }

}

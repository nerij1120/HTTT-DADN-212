package com.example.httt;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class HistoryRecViewAdapter extends RecyclerView.Adapter<HistoryRecViewAdapter.ViewHolder>{
    private static final String TAG = "HistoryRecViewAdapter";

    private ArrayList<History> history = new ArrayList<>();
    private Context mContext;

    public HistoryRecViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Called");
        holder.txtDate.setText(history.get(position).getDate());
        holder.txtTime.setText(history.get(position).getTime());
        holder.txtTemp.setText(Double.toString(history.get(position).getTemp()) + "\u00B0C");
        holder.txtHumid.setText(Integer.toString(history.get(position).getHumid()) + "%");
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public void setHistory(ArrayList<History> history) {
        this.history = history;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private MaterialCardView parent;
        private TextView txtDate,txtTime, txtTemp, txtHumid;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            txtDate = itemView.findViewById(R.id.date);
            txtTime = itemView.findViewById(R.id.time);
            txtTemp = itemView.findViewById(R.id.txtTempHistory);
            txtHumid = itemView.findViewById(R.id.txtHumid);
        }
    }
}

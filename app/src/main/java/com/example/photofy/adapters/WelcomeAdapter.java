package com.example.photofy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photofy.R;
import com.example.photofy.models.Welcome;

import java.util.List;

public class WelcomeAdapter extends RecyclerView.Adapter<WelcomeAdapter.ViewHolder>{

    public static final String TAG = "WelcomeAdapter";
    private final Context context;
    private final List<Welcome> screens;

    public WelcomeAdapter(Context context, List<Welcome> screens) {
        this.context = context;
        this.screens = screens;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_welcome, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Welcome screen = screens.get(position);
        holder.bind(screen);
    }

    @Override
    public int getItemCount() {
        return screens.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivInstruction;
        private final TextView tvMain;
        private final TextView tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivInstruction = itemView.findViewById(R.id.ivInstruction);
            tvMain = itemView.findViewById(R.id.tvMain);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }

        public void bind(Welcome screen) {
            ivInstruction.setImageResource(screen.getImage());
            tvMain.setText(screen.getMainText());
            tvDescription.setText(screen.getDescriptionText());
        }
    }
}

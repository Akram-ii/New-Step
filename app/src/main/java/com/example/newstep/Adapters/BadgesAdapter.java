package com.example.newstep.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Models.BadgeModel;
import com.example.newstep.R;

import java.util.List;

public class BadgesAdapter extends RecyclerView.Adapter<BadgesAdapter.BadgeViewHolder> {

    private List<BadgeModel> badgeList;

    public BadgesAdapter(List<BadgeModel> badgeList) {
        this.badgeList = badgeList;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        BadgeModel badge = badgeList.get(position);
        holder.badgeName.setText(badge.getName());
        holder.badgeImage.setImageResource(badge.getImageId());
        if(badge.getPoints()==10000){
            holder.desc.setText(badge.getPoints()+"+ Points");
        }else{
            holder.desc.setText(badge.getPoints()+" Points");

        }
    }

    @Override
    public int getItemCount() {
        return badgeList.size();
    }

    public static class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView badgeImage;
        TextView badgeName,desc;

        public BadgeViewHolder(View itemView) {
            super(itemView);
            badgeImage = itemView.findViewById(R.id.badgeImage);
            badgeName = itemView.findViewById(R.id.badgeName);
            desc = itemView.findViewById(R.id.badgeDesc);
        }
    }
}

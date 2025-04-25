package com.example.newstep.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newstep.ConvActivity;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.UserModelViewHolder> {

    private List<UserModel> userList;
    private Context context;

    public LeaderBoardAdapter(List<UserModel> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.leaderboard_item, parent, false);
        return new UserModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserModelViewHolder holder, int position) {
        UserModel model = userList.get(position);

        holder.username.setText(model.getUsername());
        holder.pts.setText(model.getPoints() + "");
        holder.rank.setText((position + 1)+"th");

        if(position==0){
            holder.rank.setText("1st");
        }
        else if(position==1){
            holder.rank.setText("2nd");
        } else if(position==1){
            holder.rank.setText("3rd");
        }
        Glide.with(holder.pfp)
                .load(model.getProfileImage())
                .circleCrop()
                .placeholder(R.drawable.pfp_purple)
                .error(R.drawable.pfp_purple)
                .into(holder.pfp);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView username, pts, rank;
        ImageView pfp;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            pfp = itemView.findViewById(R.id.profile_pic_image);
            pts = itemView.findViewById(R.id.points);
            rank = itemView.findViewById(R.id.rank);
        }
    }
}

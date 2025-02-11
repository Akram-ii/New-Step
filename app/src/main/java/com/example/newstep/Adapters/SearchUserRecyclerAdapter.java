package com.example.newstep.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.ConvActivity;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {
    Context context;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();

    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context=context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {

        Log.d("RecyclerDebug", "Binding user: " + model.getUsername());
        holder.username.setText(model.getUsername());
        holder.date.setText("Member since "+model.getRegisterDate());

        FirebaseUtil.loadPfp(model.getId(),holder.pfp);


        if (model.getId().equals(user.getUid())) {
            Log.d("RecyclerDebug", "Current user UID: " + user.getUid());
            holder.username.setText(model.getUsername() + " (me)");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model.getId().equals(FirebaseUtil.getCurrentUserId())){
                    return;
                }
                Intent intent=new Intent(context.getApplicationContext(), ConvActivity.class);
                intent.putExtra("username",model.getUsername());
                intent.putExtra("userId",model.getId());
                intent.putExtra("token",model.getToken());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }
    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row,parent,false);
        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder{
        TextView username,date;
        ImageView pfp;
        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.usernameTextView);
            date=itemView.findViewById(R.id.userDateTextView);
            pfp=itemView.findViewById(R.id.profile_pic_image);
        }
    }

}

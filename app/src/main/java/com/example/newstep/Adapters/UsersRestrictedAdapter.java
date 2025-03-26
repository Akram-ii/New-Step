package com.example.newstep.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.util.Util;
import com.example.newstep.ConvActivity;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.Utilities;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UsersRestrictedAdapter extends FirestoreRecyclerAdapter<UserModel, UsersRestrictedAdapter.UserModelViewHolder> {
    Context context;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();

    public UsersRestrictedAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context=context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
holder.username.setText(model.getUsername());
holder.nbReports.setText(String.valueOf(model.getNb_reports()));
if(model.getNb_reports()==1){
    holder.times.setText(" time");
}else{
    holder.times.setText(" times");
}
if(!model.getIsBannedComments()){
    holder.separator.setVisibility(View.GONE);
    holder.banComments.setVisibility(View.GONE);
    holder.unbanComments.setVisibility(View.GONE);
}else{
    holder.banComments.setVisibility(View.VISIBLE);
    holder.unbanComments.setVisibility(View.VISIBLE);
    holder.dateBanComment.setText(": "+Utilities.getRelativeTime(model.getWhenBannedComments()));
}

if(!model.getIsBannedPosts()){
    holder.separator.setVisibility(View.GONE);
    holder.banPosts.setVisibility(View.GONE);
    holder.unbanPosts.setVisibility(View.GONE);
}else{
    holder.banPosts.setVisibility(View.VISIBLE);
    holder.unbanPosts.setVisibility(View.VISIBLE);
    holder.dateBanPosts.setText(": "+Utilities.getRelativeTime(model.getWhenBannedPosts()));
   }

holder.unbanComments.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        new AlertDialog.Builder(context)
                .setTitle("Allow user to comment again ?")
                .setMessage("This action cannot be undone")
                .setPositiveButton("Allow", (dialog, which) -> {
                   allowComments(model.getId(),model.getUsername(),model.getIsBannedPosts());
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
});
holder.unbanPosts.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        new AlertDialog.Builder(context)
                .setTitle("Allow user to post again ?")
                .setMessage("This action cannot be undone")
                .setPositiveButton("Allow", (dialog, which) -> {
                    allowPosts(model.getId(),model.getUsername(),model.getIsBannedComments());
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();

    }
});

    }

    private void allowPosts(String id,String username,boolean isBannedComments) {
        if(isBannedComments) {
            FirebaseUtil.allUserCollectionRef().document(id)
                    .update("isBannedPosts", false).addOnSuccessListener(v -> {
                        Toast.makeText(context, "successfully allowed " + username + " to post again", Toast.LENGTH_LONG).show();
                        notifyDataSetChanged();
                    }).addOnFailureListener(v -> Toast.makeText(context, "Error: " + v.getMessage(), Toast.LENGTH_LONG).show());
        }
        else{
            FirebaseUtil.allUserCollectionRef().document(id)
                    .update("isBannedPosts", false,"isRestricted",false).addOnSuccessListener(v -> {
                        Toast.makeText(context, "successfully allowed " + username + " to post again", Toast.LENGTH_LONG).show();
                        notifyDataSetChanged();
                    }).addOnFailureListener(v -> Toast.makeText(context, "Error: " + v.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private void allowComments(String id,String username,boolean isBannedPosts) {
        if(isBannedPosts) {
            FirebaseUtil.allUserCollectionRef().document(id)
                    .update("isBannedComments", false).addOnSuccessListener(v -> {
                        Toast.makeText(context, "successfully allowed " + username + " to comment again", Toast.LENGTH_LONG).show();
                        notifyDataSetChanged();
                    }).addOnFailureListener(v -> Toast.makeText(context, "Error: " + v.getMessage(), Toast.LENGTH_LONG).show());
        }else{
            FirebaseUtil.allUserCollectionRef().document(id)
                    .update("isBannedComments", false,"isRestricted",false).addOnSuccessListener(v -> {
                        Toast.makeText(context, "successfully allowed " + username + " to comment again", Toast.LENGTH_LONG).show();
                        notifyDataSetChanged();
                    }).addOnFailureListener(v -> Toast.makeText(context, "Error: " + v.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_user_restricted,parent,false);
        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder{
        TextView username,dateBanPosts,dateBanComment,nbReports,times;
        LinearLayout unbanPosts,unbanComments,banPosts,banComments;
        View separator;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.usernameTextView);
            nbReports=itemView.findViewById(R.id.nb_reports);
            dateBanComment=itemView.findViewById(R.id.time_ban_comment);
            dateBanPosts=itemView.findViewById(R.id.time_ban_posts);
            unbanComments=itemView.findViewById(R.id.unban_comments);
            unbanPosts=itemView.findViewById(R.id.unban_posts);
            banPosts=itemView.findViewById(R.id.post_ban);
            banComments=itemView.findViewById(R.id.comment_ban);
            separator=itemView.findViewById(R.id.sep);
            times=itemView.findViewById(R.id.times_textview);

        }
    }

}

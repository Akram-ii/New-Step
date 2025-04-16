package com.example.newstep.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.Utilities;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class UsersBanAdapter extends FirestoreRecyclerAdapter<UserModel,UsersBanAdapter.BanViewHolder> {

    private Context context;
    public UsersBanAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options,Context context) {
        super(options);
        this.context=context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UsersBanAdapter.BanViewHolder holder, int position, @NonNull UserModel model) {
        holder.BUn.setText(model.getUsername());
        if(model.getNb_reports()==1){
            holder.nbR.setText("one time");
        }
        else{
            holder.nbR.setText(String.valueOf(model.getNb_reports())+"times");
        }
        holder.banAccountTime.setText(" :"+Utilities.getRelativeTime(model.getWhenBanned()));
        holder.unbanAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Are you sure you want to unban this user ?")
                        .setMessage("This action cannot be undone")
                        .setPositiveButton("UnBan", (dialog, which) -> {
                            unbanAccount(model.getId(), model.getUsername());
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });


    }

    @NonNull
    @Override
    public UsersBanAdapter.BanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_user_banned,parent,false);
        return new BanViewHolder(view);
    }


    class BanViewHolder extends RecyclerView.ViewHolder{
        TextView BUn,nbR,banAccountTime;
        LinearLayout unbanAccount;
        public BanViewHolder(@NonNull View itemView) {
            super(itemView);
            BUn=itemView.findViewById(R.id.bannedUsername);
            nbR= itemView.findViewById(R.id.nbR);
            banAccountTime=itemView.findViewById(R.id.time_ban_account);
            unbanAccount=itemView.findViewById(R.id.unban_account);


        }
    }
    private void unbanAccount(String Userid, String username) {
        FirebaseUtil.allUserCollectionRef().document(Userid)
                .update("isBanned", false)
                .addOnSuccessListener(v -> {
                    Toast.makeText(context, "Successfully unbanned " + username, Toast.LENGTH_LONG).show();
                    notifyDataSetChanged();
                })
                .addOnFailureListener(v -> {
                    Toast.makeText(context, "Error: " + v.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}

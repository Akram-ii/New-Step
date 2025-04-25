package com.example.newstep.Adapters;


import static com.google.common.reflect.Reflection.getPackageName;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newstep.Models.ChatroomModel;
import com.example.newstep.Models.HabitModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FieldValue;

import java.lang.reflect.Field;


public class AllGroupsAdapter extends FirestoreRecyclerAdapter<ChatroomModel,AllGroupsAdapter.ChatroomModelViewHolder> {
Context context;
    public AllGroupsAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context){
    super(options);
    this.context=context;
}
    @Override
    protected void onBindViewHolder(@NonNull AllGroupsAdapter.ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        if(model.getIcon()!=null){
            String iconName = model.getIcon();
            String hexColor = model.getIconColor();

            int resId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
            if (resId != 0) {
                holder.pfp.setImageResource(resId);
            }


            try {
                int color = Color.parseColor(hexColor);
                holder.pfp.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                holder.groupName.setTextColor(Color.parseColor(hexColor));
            } catch (IllegalArgumentException e) {

            }
        }

        FirebaseUtil.allUserCollectionRef().document(model.getOwnerId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                   holder.ownerUsername.setText("Created by "+documentSnapshot.getString("username"));
                })
                .addOnFailureListener(e -> e.printStackTrace());
        if(model.getNumber_members()==1){
        holder.nbr_members.setText(model.getNumber_members() + " member");}
        else {
            holder.nbr_members.setText(model.getNumber_members() + " members");
        }
        holder.groupName.setText(model.getGroupName());

if("Private".equals(model.getPrivacy())){
    holder.privateIc.setVisibility(View.VISIBLE);
}
if("Public".equals(model.getPrivacy())){
    holder.privateIc.setVisibility(View.GONE);
}
    holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(model.checkUser(FirebaseUtil.getCurrentUserId())){
                Toast.makeText(context,"You are already member of this group",Toast.LENGTH_SHORT).show();
            }
            else {
                if("Private".equals(model.getPrivacy())){
                Toast.makeText(context,"This Group is Private you need to contact the owner so they can let you in",Toast.LENGTH_LONG).show();
                }else{
                new AlertDialog.Builder(context).setMessage("Do you want to join this group chat ?")
                        .setPositiveButton("Join", (dialog, which) -> {
                            FirebaseUtil.allChatroomCollectionRef().document(model.getChatroomId())
                                    .update("userIds", FieldValue.arrayUnion(FirebaseUtil.getCurrentUserId()),"number_members",FieldValue.increment(1))
                                    .addOnSuccessListener(v->{
                                        Toast.makeText(context,"Joined "+model.getGroupName()+",check My Chats",Toast.LENGTH_SHORT).show();
                                        notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> Log.d("error adding user: " , e.getMessage()));

                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                        })
                        .show();

            }
        }}
    });

holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
    @Override
    public boolean onLongClick(View view) {
        viewGroupDetails(model.getGroupName(),model.getDesc());
        return true;
    }
});
    }

    private void viewGroupDetails(String grpName, String grpDesc) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popUpView = inflater.inflate(R.layout.popup_view_group, null);
        dimBackground(0.5f);
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        View rootLayout = ((Activity) context).findViewById(android.R.id.content);
        popupWindow.showAtLocation(rootLayout, Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams layoutParams = ((Activity) context).getWindow().getAttributes();
            layoutParams.alpha = 1.0f;
            ((Activity) context).getWindow().setAttributes(layoutParams);
        });
        popupWindow.setOnDismissListener(() -> dimBackground(1.0f));
        TextView name=popUpView.findViewById(R.id.group_name);
        TextView desc=popUpView.findViewById(R.id.group_desc);
        ImageButton back=popUpView.findViewById(R.id.back);
        back.setOnClickListener(v->{popupWindow.dismiss();});
        name.setText(grpName);
        desc.setText(grpDesc);
    }

    @NonNull
    @Override
    public AllGroupsAdapter.ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.group_row,parent,false);
        return new AllGroupsAdapter.ChatroomModelViewHolder(view);
    }
    class ChatroomModelViewHolder extends RecyclerView.ViewHolder{
       private TextView groupName,nbr_members,ownerUsername;
       private ImageView pfp,privateIc;
        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName=itemView.findViewById(R.id.group_name);
            nbr_members=itemView.findViewById(R.id.nbr_members);
            ownerUsername=itemView.findViewById(R.id.owner_username);
            pfp=itemView.findViewById(R.id.group_pic_image);
            privateIc=itemView.findViewById(R.id.privateIc);
        }

    }
    private void dimBackground(float alpha) {
        WindowManager.LayoutParams layoutParams = ((Activity) context).getWindow().getAttributes();
        layoutParams.alpha = alpha;
        ((Activity) context).getWindow().setAttributes(layoutParams);
    }
}

package com.example.newstep.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.util.Util;
import com.example.newstep.ConvActivity;
import com.example.newstep.ConvGroupActivity;
import com.example.newstep.Models.ChatroomModel;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.Utilities;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.w3c.dom.Text;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {
    Context context;



    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context=context;

    }

    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        Log.d("Chatroom", "userIds: " + model.getUserIds().toString());
        if (model.getIsGroup() == 0) {

        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {
                    UserModel otherUser = task.getResult().toObject(UserModel.class);

                    Boolean lastMsgSentByMe = model.getLastMsgSenderId().equals(FirebaseUtil.getCurrentUserId());


                    holder.username.setText(otherUser.getUsername());
                    holder.pfp.setImageResource(R.drawable.pfp_purple);
                    if (lastMsgSentByMe) {
                        holder.mostRecentMsg.setText("Me: " + model.getLastMsgSent());
                    } else {
                        holder.mostRecentMsg.setText(otherUser.getUsername() + ": " + model.getLastMsgSent());
                    }
                    holder.timestampRecentMsg.setText(Utilities.getRelativeTime(model.getLastMsgTimeStamp()));
                    FirebaseUtil.loadPfp(otherUser.getId(), holder.pfp);


                    if (model.getLastMsgSenderId().equals(FirebaseUtil.getCurrentUserId()) || model.getUnseenMsg() == 0) {
                        holder.unseenMsg.setVisibility(View.GONE);
                    } else {
                        holder.mostRecentMsg.setTextColor(Color.parseColor("#8578A8"));
                        holder.mostRecentMsg.setTypeface(null, Typeface.BOLD);
                        holder.unseenMsg.setVisibility(View.VISIBLE);
                    }

                    FirebaseUtil.allUserCollectionRef().document(otherUser.getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.e("verif", "Error fetching document: " + error.getMessage());
                                return;
                            }
                            if (value != null && value.exists()) {
                                Long availabilityLong = value.getLong("availability");
                                if (availabilityLong != null) {
                                    int a = availabilityLong.intValue();
                                    if (a == 1) {
                                        holder.activity.setVisibility(View.VISIBLE);
                                        holder.activity.setImageResource(R.drawable.online_user);
                                    } else {
                                        holder.activity.setVisibility(View.VISIBLE);
                                        holder.activity.setImageResource(R.drawable.offline_user);
                                    }
                                } else {
                                }
                            } else {
                            }
                        }
                    });

                    holder.unseenMsg.setText("+ " + model.getUnseenMsg());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (model.getLastMsgSenderId() != FirebaseUtil.getCurrentUserId()) {
                                Log.d("unseenMsg: ", "");
                                model.setUnseenMsg(0);
                            }
                            Intent intent = new Intent(context.getApplicationContext(), ConvActivity.class);
                            intent.putExtra("username", otherUser.getUsername());
                            intent.putExtra("userId", otherUser.getId());
                            intent.putExtra("availability", otherUser.getAvailability());
                            intent.putExtra("token", otherUser.getToken());
                            intent.putExtra("lastMsgId", model.getLastMsgSenderId());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    });
                } else {
                    FirebaseUtil.allChatroomCollectionRef().document(model.getChatroomId()).delete();
                    Log.e("Chatroom", "Error: Document not found or task unsuccessful");
                }
            }
        });
    }else{ //group chat
            holder.pfp.setImageResource(R.drawable.icon_group);
       holder.username.setText(model.getGroupName());
        holder.activity.setVisibility(View.GONE);
        holder.timestampRecentMsg.setText(Utilities.getRelativeTime(model.getLastMsgTimeStamp()));
        if(model.getLastMsgSenderId()==FirebaseUtil.getCurrentUserId()){
            holder.mostRecentMsg.setText("Me: "+model.getLastMsgSent());
        }else{
            if (model.getLastMsgSenderId() == null) {
                holder.mostRecentMsg.setText("Unknown: " + model.getLastMsgSent());
            }
            else{
            FirebaseUtil.allUserCollectionRef().document(model.getLastMsgSenderId()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                        holder.mostRecentMsg.setText(documentSnapshot.getString("username")+": "+model.getLastMsgSent());
                        } else {
                            Log.d("Firestore", "User not found");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error getting user", e));
        }}
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context.getApplicationContext(), ConvGroupActivity.class);
                intent.putExtra("groupName", model.getGroupName());
                intent.putExtra("chatroomId", model.getChatroomId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
    @Override
    public boolean onLongClick(View view) {
        if(model.getOwnerId().equals(FirebaseUtil.getCurrentUserId())){
            editGroup(model.getChatroomId(),model.getGroupName(), model.getDesc());
        }else{
            viewGroupDetails(model.getGroupName(),model.getDesc());
        }
        return true;
    }
});
    }
    }

    private void viewGroupDetails(String grpName,String grpDesc) {
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

    private void editGroup(String id,String grpName,String grpDesc) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popUpView = inflater.inflate(R.layout.popup_create_group, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        View rootLayout = ((Activity) context).findViewById(android.R.id.content);
        dimBackground(0.5f);
        popupWindow.showAtLocation(rootLayout, Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams layoutParams = ((Activity) context).getWindow().getAttributes();
            layoutParams.alpha = 1.0f;
            ((Activity) context).getWindow().setAttributes(layoutParams);
        });
        popupWindow.setOnDismissListener(() -> dimBackground(1.0f));
        TextView title=popUpView.findViewById(R.id.title);
        EditText groupNameInput = popUpView.findViewById(R.id.groupNameInput);
        EditText groupDescInput=popUpView.findViewById(R.id.group_desc);
        Button btnCancel = popUpView.findViewById(R.id.btnCancel);
        Button btnSave= popUpView.findViewById(R.id.btnAddGroup);
btnSave.setText("Save");
title.setText("Edit your group");
btnCancel.setOnClickListener(v -> popupWindow.dismiss());
groupDescInput.setText(grpDesc);
groupNameInput.setText(grpName);

btnSave.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        String groupName = groupNameInput.getText().toString().trim();
        String groupDesc = groupDescInput.getText().toString().trim();
        if (groupName.isEmpty()) {
            Toast.makeText(context, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
        }else if (groupName.length()>25) {
            Toast.makeText(context, "Group name is too long", Toast.LENGTH_SHORT).show();
        }else
        if(groupDesc.isEmpty()){
            Toast.makeText(context, "Group description cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else {

            FirebaseUtil.allChatroomCollectionRef().document(id).update("groupName",groupName,"desc",groupDesc)
                    .addOnSuccessListener(v->{
                       Toast.makeText(context,"Group updated !",Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(v->{
                        popupWindow.dismiss();
                        Toast.makeText(context,"Couldn't edit Group ",Toast.LENGTH_SHORT).show();
                        popupWindow.dismiss();
                    });
        }
    }
});
btnCancel.setOnClickListener(v->{popupWindow.dismiss();});

    }

    private void dimBackground(float alpha) {
        WindowManager.LayoutParams layoutParams = ((Activity) context).getWindow().getAttributes();
        layoutParams.alpha = alpha;
        ((Activity) context).getWindow().setAttributes(layoutParams);
    }


    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.recent_chat_row,parent,false);
        return new ChatroomModelViewHolder(view);
    }

    class ChatroomModelViewHolder extends RecyclerView.ViewHolder{
        TextView username,timestampRecentMsg,mostRecentMsg,unseenMsg;
        ImageView pfp;
        ImageView activity;
        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.usernameTextView);
            activity=itemView.findViewById(R.id.activity);
            timestampRecentMsg=itemView.findViewById(R.id.lastMsg_timestampTextView);
            mostRecentMsg=itemView.findViewById(R.id.lastMsgSentTextView);
            unseenMsg=itemView.findViewById(R.id.unseenMsg);
            pfp=itemView.findViewById(R.id.profile_pic_image);
        }

    }
    public interface OnDataLoadedListener {
        void onDataLoaded();
    }
}


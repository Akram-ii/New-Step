package com.example.newstep.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.transition.Hold;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.w3c.dom.Text;

import java.util.List;

import kotlin.reflect.KVisibility;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {
    Context context;



    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context=context;

    }

    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        Log.d("ChatroomType", "Chatroom " + model.getChatroomId() + ": isGroup=" + model.getIsGroup());
        Log.d("Chatroom", "userIds " + model.getIconColor());

        if (model.getIsGroup() == 0) {

        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {
                    UserModel otherUser = task.getResult().toObject(UserModel.class);

                    Boolean lastMsgSentByMe = model.getLastMsgSenderId().equals(FirebaseUtil.getCurrentUserId());


                    holder.username.setText(otherUser.getUsername());
                    if (lastMsgSentByMe) {
                        holder.mostRecentMsg.setText("Me: " + model.getLastMsgSent());
                    } else {
                        holder.mostRecentMsg.setText(otherUser.getUsername() + ": " + model.getLastMsgSent());
                    }
                    holder.timestampRecentMsg.setText(Utilities.getRelativeTime(model.getLastMsgTimeStamp()));
                    if(model.getIsGroup()==0) {
                        holder.pfp.setImageDrawable(null);
                        FirebaseUtil.loadPfp(otherUser.getId(), holder.pfp);
                    }

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
                            intent.putExtra("privacy", otherUser.getPrivacy());
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
    }else if (model.getIsGroup() == 1){ //group chat
            if(model.getIcon()!=null){
                String iconName = model.getIcon();
                String hexColor = model.getIconColor();

                int resId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                if (resId != 0) {
                    holder.pfp.setImageResource(resId);
                }

                if(model.getIsGroup()==1){

                try {
                    int color = Color.parseColor(hexColor);
                    holder.pfp.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    holder.username.setTextColor(Color.parseColor(hexColor));
                } catch (IllegalArgumentException e) {

                }}
            }
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
                intent.putExtra("icon", model.getIcon());
                intent.putExtra("iconImage", model.getIconColor());

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
    @Override
    public boolean onLongClick(View view) {
        if(model.getOwnerId().equals(FirebaseUtil.getCurrentUserId())){
            editGroup(model.getChatroomId(),model.getGroupName(), model.getDesc(),model.getPrivacy(), model.getIcon(), model.getIconColor());

        }else{
            viewGroupDetails(model.getGroupName(),model.getDesc(),model.getChatroomId());

        }
        return true;
    }
});
    }
    }

    private void viewGroupDetails(String grpName,String grpDesc,String id) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View popUpView = inflater.inflate(R.layout.popup_view_group, null);
        builder.setView(popUpView);

        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.PopupWindowAnimation;
        }
        dialog.show();

        TextView name=popUpView.findViewById(R.id.group_name);
        TextView desc=popUpView.findViewById(R.id.group_desc);
        ImageButton back=popUpView.findViewById(R.id.back);
        ImageView leave=popUpView.findViewById(R.id.leave_btn);
        leave.setVisibility(View.VISIBLE);
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveGroup(id );
                dialog.dismiss();
            }
        });
        back.setOnClickListener(v->{dialog.dismiss();});
name.setText(grpName);
desc.setText(grpDesc);

    }



    private void editGroup(String id, String grpName, String grpDesc, String privacy, String grpIcon, String grpIconColor) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View popUpView = inflater.inflate(R.layout.popup_create_group, null);
        builder.setView(popUpView);

        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.PopupWindowAnimation;
        }
        dialog.show();

       RadioGroup radioGroup = popUpView.findViewById(R.id.radioGroup);
       ChipGroup chipGroupIcon = popUpView.findViewById(R.id.chipGroupIcon);
       ChipGroup chipGroupColor = popUpView.findViewById(R.id.chipGroupColor);
       RadioButton radioPublic = popUpView.findViewById(R.id.radioPublic);
       RadioButton radioPrivate = popUpView.findViewById(R.id.radioPrivate);
       EditText groupNameInput = popUpView.findViewById(R.id.groupNameInput);
       EditText groupDescInput = popUpView.findViewById(R.id.group_desc);
       Button btnCancel = popUpView.findViewById(R.id.btnCancel);
       Button btnSave = popUpView.findViewById(R.id.btnAddGroup);
        TextView title =popUpView.findViewById(R.id.title);
        TextView editIconText=popUpView.findViewById(R.id.text);
        TextView editColerText =popUpView.findViewById(R.id.text2);
        ImageView delete_group=popUpView.findViewById(R.id.delete_group_btn);
        delete_group.setVisibility(View.VISIBLE);

        delete_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deleteGroup(id);
                dialog.dismiss();
            }
        });

       btnSave.setText("save");
       groupNameInput.setText(grpName);
       groupDescInput.setText(grpDesc);
        title.setText("Edit your Group");
        editIconText.setText("Edit the Group Icon");
        editColerText.setText("Edit the Icon Color");


       if ("Public".equals(privacy)) {
           radioPublic.setChecked(true);
       } else {
           radioPrivate.setChecked(true);
       }


        selectChipByColor(chipGroupColor, grpIconColor);
        selectChipByIconTag(chipGroupIcon, grpIcon);



       btnSave.setOnClickListener(v -> {
           String groupName = groupNameInput.getText().toString().trim();
           String groupDesc = groupDescInput.getText().toString().trim();

           if (groupName.isEmpty()) {
               Toast.makeText(context, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
               return;
           } else if (groupName.length() > 25) {
               Toast.makeText(context, "Group name is too long", Toast.LENGTH_SHORT).show();
               return;
           } else if (groupDesc.isEmpty()) {
               Toast.makeText(context, "Group description cannot be empty", Toast.LENGTH_SHORT).show();
               return;
           }

           int selectedId = radioGroup.getCheckedRadioButtonId();
           String privacySetting;
           if (selectedId == R.id.radioPublic) {
               privacySetting = "Public";
           } else {
               privacySetting = "Private";
           }

           int iconId = chipGroupIcon.getCheckedChipId();
           int colorId = chipGroupColor.getCheckedChipId();
           String iconName = context.getResources().getResourceEntryName(iconId);
           String colorName = context.getResources().getResourceEntryName(colorId);


           String colorHexCode = Utilities.hexCodeForColor(colorName);
           for (int i = 0; i < chipGroupColor.getChildCount(); i++) {
               Chip chip = (Chip) chipGroupColor.getChildAt(i);
               ColorStateList chipColor = chip.getChipBackgroundColor();
               if (chipColor != null && chipColor.getDefaultColor() == Color.parseColor(colorHexCode)) {
                   chip.setChecked(true);
                   break;
               }
           }
           for (int i = 0; i < chipGroupIcon.getChildCount(); i++) {
               Chip chip = (Chip) chipGroupIcon.getChildAt(i);
               if (chip.getTag() != null && chip.getTag().toString().equals(grpIcon)) {
                   chip.setChecked(true);
                   break;
               }
           }
           updateGroupInFirestore(id, groupName, groupDesc, privacySetting, iconName, colorHexCode);

           dialog.dismiss();
       });


       popUpView.setOnTouchListener((v, event) -> {
           if (event.getAction() == MotionEvent.ACTION_DOWN) {
               dialog.dismiss();
               return true;
           }
           return false;
       });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
   }

    private void selectChipByColor(ChipGroup chipGroup, String colorHex) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            ColorStateList chipColor = chip.getChipBackgroundColor();
            if (chipColor != null && chipColor.getDefaultColor() == Color.parseColor(colorHex)) {
                chip.setChecked(true);
                break;
            }
        }
    }
    private void selectChipByIconTag(ChipGroup chipGroup, String iconTag) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.getTag() != null && chip.getTag().toString().equals(iconTag)) {
                chip.setChecked(true);
                break;
            }
        }
    }


    private void updateGroupInFirestore(String id, String groupName, String groupDesc, String privacy, String iconName, String iconColor) {
        FirebaseUtil.allChatroomCollectionRef().document(id)
                .update("groupName", groupName, "desc", groupDesc, "privacy", privacy, "icon", iconName, "iconColor", iconColor)
                .addOnSuccessListener(v -> {
                    Toast.makeText(context, "Group updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(v -> {
                    Toast.makeText(context, "Couldn't edit Group", Toast.LENGTH_SHORT).show();
                });
    }


    private void leaveGroup(String chatroomId) {
        if (chatroomId == null || chatroomId.isEmpty()) {
            Toast.makeText(context, "Invalid group ID", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave this group?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseUtil.getChatroomRef(chatroomId).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            ChatroomModel chatroomModel = task.getResult().toObject(ChatroomModel.class);
                            if (chatroomModel != null) {
                                List<String> userIds = chatroomModel.getUserIds();
                                if (userIds != null && userIds.contains(FirebaseUtil.getCurrentUserId())) {
                                    userIds.remove(FirebaseUtil.getCurrentUserId());

                                        FirebaseUtil.getChatroomRef(chatroomId).update("userIds", userIds)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(context, "You have left the group", Toast.LENGTH_SHORT).show();
                                                    notifyDataSetChanged();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Failed to leave group", Toast.LENGTH_SHORT).show();
                                                });

                                }
                            }
                        } else {
                            Toast.makeText(context, "Failed to fetch group data", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void deleteGroup(String chatroomId) {
        if (chatroomId == null || chatroomId.isEmpty()) {
            Toast.makeText(context, "Invalid group ID", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete this group?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseUtil.allChatroomCollectionRef().document(chatroomId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Group deleted successfully", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to delete group", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
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


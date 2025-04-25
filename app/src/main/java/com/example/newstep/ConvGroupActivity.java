package com.example.newstep;



import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
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
import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Adapters.ChatRecyclerAdapter;
import com.example.newstep.Adapters.SearchUserRecyclerAdapter;
import com.example.newstep.Models.ChatMsgModel;
import com.example.newstep.Models.ChatroomModel;
import com.example.newstep.Models.UserModel;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.NotifOnline;
import com.example.newstep.Util.Utilities;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Objects;
import java.util.Random;

public class ConvGroupActivity extends AppCompatActivity {
    TextView groupNameTextView;
    EditText msgEditText;
    ImageButton backButton, sendButton;
    ImageView pfp;
    RecyclerView recyclerView, recyclerviewChattingWith;
    String chatroomId, currentUser,currentUserName;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;
    SearchUserRecyclerAdapter adapter2;
    ImageButton add_user, remove_user;

    Boolean userSentAMsg = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conv_group);

        pfp=findViewById(R.id.pfp);
        groupNameTextView = findViewById(R.id.groupnameTextView);
        msgEditText = findViewById(R.id.msg_EditText);
        backButton = findViewById(R.id.back_ImageButton);
        sendButton = findViewById(R.id.send_ImageButton);
        recyclerView = findViewById(R.id.msgsRecyclerView);
        recyclerviewChattingWith = findViewById(R.id.chattinWith);
        add_user = findViewById(R.id.addMemberButton);
        remove_user = findViewById(R.id.removeMemberButton);
        currentUser = FirebaseUtil.getCurrentUserId();
        currentUserName=FirebaseUtil.getCurrentUsername(this);

        Intent intent = getIntent();
        String groupName = intent.getStringExtra("groupName");
        String icon=intent.getStringExtra("icon");
        String iconColor=intent.getStringExtra("iconImage");
        chatroomId = intent.getStringExtra("chatroomId");


if(icon!=null){
    int resId = getResources().getIdentifier(icon, "drawable",getPackageName());
    if (resId != 0) {
        pfp.setImageResource(resId);
}
    try {
        int color = Color.parseColor(iconColor);
        pfp.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        groupNameTextView.setTextColor(color);
    } catch (IllegalArgumentException e) {}
}
        groupNameTextView.setText(groupName);

        FirebaseUtil.getChatroomRef(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ChatroomModel chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel != null && chatroomModel.getOwnerId().equals(currentUser)) {
                    remove_user.setVisibility(View.VISIBLE);
                    groupNameTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                           editGroup(chatroomId,chatroomModel.getGroupName(),chatroomModel.getDesc(),chatroomModel.getPrivacy(),chatroomModel.getIcon(),chatroomModel.getIconColor(),true);
                        }
                    });
                    pfp.setOnClickListener(v->{editGroup(chatroomId,chatroomModel.getGroupName(),chatroomModel.getDesc(),chatroomModel.getPrivacy(),chatroomModel.getIcon(),chatroomModel.getIconColor(),true);});
                }else{
                    groupNameTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editGroup(chatroomId,chatroomModel.getGroupName(),chatroomModel.getDesc(),chatroomModel.getPrivacy(),chatroomModel.getIcon(),chatroomModel.getIconColor(),false);
                        }
                    });
                    pfp.setOnClickListener(v->{editGroup(chatroomId,chatroomModel.getGroupName(),chatroomModel.getDesc(),chatroomModel.getPrivacy(),chatroomModel.getIcon(),chatroomModel.getIconColor(),false);});
                }

            }
        });

        getOrCreateChatroomModel();


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = msgEditText.getText().toString().trim();
                if (!message.isEmpty()) {
                    userSentAMsg = true;
                    sendMessageToGroup(message, groupName);


                };

            }
        });




        add_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddMemberDialog();
            }
        });

        remove_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRemoveMemberDialog();
            }
        });
    }


    private void getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomRef(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel == null) {

                    Toast.makeText(this, "!this chatroom does not exist", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    setupRecyclerView();
                    setupRecyclerOtherUsers();
                }
            } else {
                Toast.makeText(this, "error to load this chatroom", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupRecyclerView() {
        Query query = FirebaseUtil.getChatroomMsgRef(chatroomId).orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ChatMsgModel> options = new FirestoreRecyclerOptions.Builder<ChatMsgModel>()
                .setQuery(query, ChatMsgModel.class)
                .build();
        adapter = new ChatRecyclerAdapter(options, this, chatroomId);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void sendMessageToGroup(String message , String gN) {
        chatroomModel.setLastMsgSent(message);
        chatroomModel.setLastMsgSenderId(FirebaseUtil.getCurrentUserId());
        chatroomModel.setLastMsgTimeStamp(Timestamp.now());
        FirebaseUtil.getChatroomRef(chatroomId).set(chatroomModel);

        ChatMsgModel chatMsgModel = new ChatMsgModel(message, FirebaseUtil.getCurrentUserId(), Timestamp.now());
        FirebaseUtil.getChatroomMsgRef(chatroomId).add(chatMsgModel).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String generatedId = task.getResult().getId();
                task.getResult().update("messageId", generatedId)
                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Message ID added successfully"))
                        .addOnFailureListener(e -> Log.e("Firestore", "Failed to add message ID", e));
                msgEditText.setText("");
            }
        });
        sendNotificationsToGroupMembers(message,gN);
    }
    public void sendMessageNotificationsToUser(String idRecever, String message,String groupName){
        FirebaseUtil.allUserCollectionRef().document(idRecever).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()&& documentSnapshot!= null){
                String ReceverToken= documentSnapshot.getString("token");
                NotifOnline notifOnline= new NotifOnline(ReceverToken,"new message from "+ currentUserName+" in "+groupName+ " group chat ",message,ConvGroupActivity.this);
                notifOnline.sendNotif();
            }
        });


    }
    private void sendNotificationsToGroupMembers(String message,String groupName) {
        FirebaseUtil.getChatroomRef(chatroomId).get().addOnSuccessListener(documentSnapshot -> {
            ChatroomModel chatroomModel = documentSnapshot.toObject(ChatroomModel.class);
            if (chatroomModel != null) {
                List<String> userIds = chatroomModel.getUserIds();
                if (userIds != null && !userIds.isEmpty()) {



                    for (int i=0 ; i< userIds.size();i++) {
                        String userId =userIds.get(i);
                        if (!userId.equals(currentUser)) {

                            sendMessageNotificationsToUser(userId, message, groupName);
                        }
                    }

                }
            }
        });
    }
    private void setupRecyclerOtherUsers() {
        if (recyclerviewChattingWith == null) {
            Log.e("ConvGroupActivity", "recyclerviewChattingWith is null");
            return;
        }

        FirebaseUtil.getChatroomRef(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ChatroomModel chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel != null) {
                    List<String> userIds = chatroomModel.getUserIds();
                    if (userIds == null || userIds.isEmpty()) {
                        Log.e("ConvGroupActivity", "userIds is null or empty");
                        return;
                    }

                    Query query = FirebaseUtil.allUserCollectionRef().whereIn("id", userIds);
                    FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                            .setQuery(query, UserModel.class)
                            .build();
                    adapter2 = new SearchUserRecyclerAdapter(options, this);
                    recyclerviewChattingWith.setLayoutManager(new LinearLayoutManager(this));
                    recyclerviewChattingWith.setAdapter(adapter2);
                    adapter2.startListening();
                }
            } else {
                Log.e("ConvGroupActivity", "Failed to fetch chatroom data");
            }
        });
    }





    private void openAddMemberDialog() {
        FirebaseUtil.allChatroomCollectionRef()
                .whereArrayContains("userIds", FirebaseUtil.getCurrentUserId())
                .whereEqualTo("isGroup", 0)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> talkedToUserIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        List<String> userIds = (List<String>) document.get("userIds");
                        if (userIds != null) {
                            for (String userId : userIds) {
                                if (!userId.equals(FirebaseUtil.getCurrentUserId()) && !talkedToUserIds.contains(userId) ) {
                                    talkedToUserIds.add(userId);
                                }
                            }
                        }

                            }
                    if(talkedToUserIds.isEmpty()){
                        Toast.makeText(this,"You have no one to add",Toast.LENGTH_SHORT).show();
                    }else{
                        fetchUsers(talkedToUserIds);
                    }
                });
    };

private void fetchUsers(List<String> userIds){
    FirebaseUtil.allUserCollectionRef()
            .whereIn("id", userIds)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<UserModel> talkedToUsers = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    UserModel user = document.toObject(UserModel.class);
                    talkedToUsers.add(user);
                }
                Log.d("UserFetch", "Retrieved users: " + talkedToUsers.size());

                ContextThemeWrapper themedContext = new ContextThemeWrapper(this, R.style.add_rmove_Dialog);

                AlertDialog.Builder builder = new AlertDialog.Builder(themedContext);
                builder.setTitle("Add Members");

                String[] usersNames = new String[talkedToUsers.size()];
                for (int i = 0; i < talkedToUsers.size(); i++) {
                    usersNames[i] = talkedToUsers.get(i).getUsername();
                }

                builder.setMultiChoiceItems(usersNames, null, (dialog, which, isChecked) -> {
                    UserModel selectedFriend = talkedToUsers.get(which);
                    if (isChecked) {
                        addMemberToGroup(selectedFriend.getId());
                        NotifOnline notif=new NotifOnline(selectedFriend.getToken(),"You have been added to a group chat",
                                currentUserName+" added you to "+groupNameTextView.getText().toString(),this);
                        notif.sendNotif();
                    }
                });

                builder.setPositiveButton("Done", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();
            });
}
    private void openRemoveMemberDialog() {
        FirebaseUtil.getChatroomRef(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ChatroomModel chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel != null) {
                    List<String> currentMembers = chatroomModel.getUserIds();
                    for(int i=0; i<currentMembers.size() ;i++){
                        if(Objects.equals(currentMembers.get(i), currentUser)) {
                            currentMembers.remove(i);
                        }
                    }
                    if(currentMembers.isEmpty()){
                        Toast.makeText(this,"!No members to remove ",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUtil.allUserCollectionRef().whereIn("id", currentMembers).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful() && task2.getResult() != null) {
                            List<UserModel> membersList = task2.getResult().toObjects(UserModel.class);

                            ContextThemeWrapper themedContext = new ContextThemeWrapper(this, R.style.add_rmove_Dialog);

                            AlertDialog.Builder builder = new AlertDialog.Builder(themedContext);
                            builder.setTitle("Remove Members");

                            String[] memberNames = new String[membersList.size()];
                            for (int i = 0; i < membersList.size()  ; i++) {
                                memberNames[i] = membersList.get(i).getUsername();


                            }

                            builder.setMultiChoiceItems(memberNames, null, (dialog, which, isChecked) -> {
                                UserModel selectedMember = membersList.get(which);
                                if (isChecked) {
                                    removeMemberFromGroup(selectedMember.getId());
                                    NotifOnline notif=new NotifOnline(selectedMember.getToken(),"You have been removed from a group chat",
                                            currentUserName+" removed you from "+groupNameTextView.getText().toString()+" group chat",this);
                                    notif.sendNotif();
                                }
                            });

                            builder.setPositiveButton("Done", (dialog, which) -> dialog.dismiss());

                            AlertDialog dialog = builder.create();

                            dialog.show();


                        } else {
                            Log.e("Firestore", "Failed to fetch members list", task2.getException());

                        }
                    });
                }
            } else {
                Log.e("Firestore", "Failed to fetch chatroom data", task.getException());
            }
        });
    }



    private void addMemberToGroup(String userId) {
        FirebaseUtil.getChatroomRef(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ChatroomModel chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel != null) {
                    List<String> userIds = chatroomModel.getUserIds();
                    if (!userIds.contains(userId)) {
                        userIds.add(userId);
                        chatroomModel.setUserIds(userIds);
                        chatroomModel.setNumber_members(userIds.size());
                        FirebaseUtil.getChatroomRef(chatroomId).set(chatroomModel)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Member added successfully");
                                    setupRecyclerOtherUsers();
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "Failed to add member", e));
                    }
                }
            }
        });
    }

    private void removeMemberFromGroup(String userId) {
        FirebaseUtil.getChatroomRef(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ChatroomModel chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel != null) {
                    List<String> userIds = chatroomModel.getUserIds();
                    if (userIds.contains(userId)) {
                        userIds.remove(userId);
                        chatroomModel.setUserIds(userIds);
                        chatroomModel.setNumber_members(userIds.size());
                        FirebaseUtil.getChatroomRef(chatroomId).set(chatroomModel)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Member removed successfully");
                                    setupRecyclerOtherUsers();
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "Failed to remove member", e));
                    }
                }
            }
        });
    }
    private void editGroup(String id, String grpName, String grpDesc, String privacy, String grpIcon, String grpIconColor, boolean isOwner) {
        LayoutInflater inflater = LayoutInflater.from(ConvGroupActivity.this);
        View popUpView = inflater.inflate(R.layout.popup_create_group, null);

        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        final PopupWindow popupWindow = new PopupWindow(popUpView, width, height, true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = ConvGroupActivity.this.getWindow().getAttributes();
        layoutParams.alpha = 0.5f;
        ConvGroupActivity.this.getWindow().setAttributes(layoutParams);


        popupWindow.showAtLocation(ConvGroupActivity.this.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);


        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams originalParams = ConvGroupActivity.this.getWindow().getAttributes();
            originalParams.alpha = 1.0f;
            ConvGroupActivity.this.getWindow().setAttributes(originalParams);
        });

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


        groupNameInput.setText(grpName);
        groupDescInput.setText(grpDesc);
        editIconText.setText("Edit the Group Icon");
        editColerText.setText("Edit the Icon Color");
        btnSave.setText("save");

        if ("Public".equals(privacy)) {
            radioPublic.setChecked(true);

        } else {
            radioPrivate.setChecked(true);

        }


        selectChipByColor(chipGroupColor, grpIconColor);
        selectChipByIconTag(chipGroupIcon, grpIcon);




        if (!isOwner) {
            title.setText("See Group_Update Style");
            groupNameInput.setEnabled(false);
            groupDescInput.setEnabled(false);
            groupNameInput.setBackgroundResource(R.drawable.quote_background);
            groupDescInput.setBackgroundResource(R.drawable.quote_background );
            radioPublic.setEnabled(false);
            radioPrivate.setEnabled(false);
            if ("Public".equals(privacy)) {
                radioPrivate.setVisibility(View.GONE);
            } else {

               radioPublic.setVisibility(View.GONE);
            }

        }else{
            title.setText("Edit your Group");
        }

        btnCancel.setOnClickListener(v -> popupWindow.dismiss());

        btnSave.setOnClickListener(v -> {

            String groupName;
            String groupDesc;

            if (isOwner) {
                groupName = groupNameInput.getText().toString().trim();
                groupDesc = groupDescInput.getText().toString().trim();
            } else {
                groupName = grpName;
                groupDesc = grpDesc;
            }


            if (isOwner) {
                if (groupName.isEmpty()) {
                    Toast.makeText(ConvGroupActivity.this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                } else if (groupName.length() > 25) {
                    Toast.makeText(ConvGroupActivity.this, "Group name is too long", Toast.LENGTH_SHORT).show();
                    return;
                } else if (groupDesc.isEmpty()) {
                    Toast.makeText(ConvGroupActivity.this, "Group description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

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
            if (iconId == View.NO_ID ) {
                Toast.makeText(ConvGroupActivity.this, "Please select icon", Toast.LENGTH_SHORT).show();
                return;
            }
            String iconName = getResources().getResourceEntryName(iconId);
            String colorName = getResources().getResourceEntryName(colorId);

            String colorHexCode = Utilities.hexCodeForColor(colorName);


            updateGroupInFirestore(id, groupName, groupDesc, privacySetting, iconName, colorHexCode);


            popupWindow.dismiss();
        });

        popUpView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                popupWindow.dismiss();
                return true;
            }
            return false;
        });

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
                    Toast.makeText(ConvGroupActivity.this, "Group updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(v -> {
                    Toast.makeText(ConvGroupActivity.this, "Couldn't edit Group", Toast.LENGTH_SHORT).show();
                });
        groupNameTextView.setText(groupName);


    }

    @Override
    public void onBackPressed() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("lastFragment", "ChatsFragment").apply();
        Intent intent = new Intent(ConvGroupActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
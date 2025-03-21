package com.example.newstep;



import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;


import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;



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
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;






import java.util.List;

import java.util.Objects;

public class ConvGroupActivity extends AppCompatActivity {
    TextView groupNameTextView;
    EditText msgEditText;
    ImageButton backButton, sendButton;
    RecyclerView recyclerView, recyclerviewChattingWith;
    String chatroomId, currentUser;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;
    SearchUserRecyclerAdapter adapter2;
    ImageButton add_user, remove_user;

    Boolean userSentAMsg = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conv_group);


        groupNameTextView = findViewById(R.id.groupnameTextView);
        msgEditText = findViewById(R.id.msg_EditText);
        backButton = findViewById(R.id.back_ImageButton);
        sendButton = findViewById(R.id.send_ImageButton);
        recyclerView = findViewById(R.id.msgsRecyclerView);
        recyclerviewChattingWith = findViewById(R.id.chattinWith);
        add_user = findViewById(R.id.addMemberButton);
        remove_user = findViewById(R.id.removeMemberButton);
        currentUser = FirebaseUtil.getCurrentUserId();


        Intent intent = getIntent();
        String groupName = intent.getStringExtra("groupName");
        chatroomId = intent.getStringExtra("chatroomId");


        groupNameTextView.setText(groupName);

        FirebaseUtil.getChatroomRef(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ChatroomModel chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel != null && chatroomModel.getOwnerId().equals(currentUser)) {
                    remove_user.setVisibility(View.VISIBLE);
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
                    sendMessageToGroup(message);

                }

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

    private void sendMessageToGroup(String message) {
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
        FirebaseUtil.allUserCollectionRef().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<UserModel> usersList = task.getResult().toObjects(UserModel.class);
                for(int i=0;i<usersList.size();i++){
                    if(Objects.equals(usersList.get(i).getId(), currentUser)) {
                        usersList.remove(i);
                    }
                }
                ContextThemeWrapper themedContext = new ContextThemeWrapper(this, R.style.add_rmove_Dialog);

                AlertDialog.Builder builder = new AlertDialog.Builder(themedContext);
                builder.setTitle("Add Members");

                String[] usersNames = new String[usersList.size()];
                for (int i = 0; i < usersList.size(); i++) {
                    usersNames[i] = usersList.get(i).getUsername();
                }

                builder.setMultiChoiceItems(usersNames, null, (dialog, which, isChecked) -> {
                    UserModel selectedFriend = usersList.get(which);
                    if (isChecked) {
                        addMemberToGroup(selectedFriend.getId());
                    }
                });

                builder.setPositiveButton("Done", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                Log.e("Firestore", "Failed to fetch friends list", task.getException());
            }
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
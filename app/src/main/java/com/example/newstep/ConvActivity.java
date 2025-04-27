package com.example.newstep;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Adapters.ChatRecyclerAdapter;
import com.example.newstep.Adapters.SearchUserRecyclerAdapter;
import com.example.newstep.Models.ChatMsgModel;
import com.example.newstep.Models.ChatroomModel;
import com.example.newstep.Models.UserModel;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.NotifOnline;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.Arrays;

public class ConvActivity extends AppCompatActivity {
    TextView otherUsername;
    EditText msg;

    ImageButton back, sendMSG;
    ImageView pfp, activity;
    String chatroomId;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;
    String currentUserName;
    String privacy;
    RecyclerView recyclerView, recyclerViewOther;
    UserModel otherUser;

    Boolean userSentAMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conv);



        userSentAMsg = false;
        otherUser = FirebaseUtil.getUserModelFromIntent(getIntent());
        otherUsername = findViewById(R.id.usernameTextView);

        msg = findViewById(R.id.msg_EditText);
        pfp = findViewById(R.id.pfp);

        activity = findViewById(R.id.activity);
        recyclerView = findViewById(R.id.msgsRecyclerView);
        privacy=getIntent().getStringExtra("privacy");
        Log.d( "afhajdfha: ",privacy+"");
        back = findViewById(R.id.back_ImageButton);
        sendMSG = findViewById(R.id.send_ImageButton);
        otherUsername.setText(otherUser.getUsername());
        currentUserName=FirebaseUtil.getCurrentUsername(ConvActivity.this);
        chatroomId= FirebaseUtil.getChatroomId(FirebaseUtil.getCurrentUserId(),otherUser.getId());
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.getCurrentUserId(), otherUser.getId());
        getOrCreateChatroomModel();
        back.setOnClickListener(v -> {
            onBackPressed();
        });

        FirebaseUtil.loadPfp(otherUser.getId(), pfp);


        sendMSG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(privacy.equals("private")){
                    Toast.makeText(ConvActivity.this,"This user does'nt allow messages",Toast.LENGTH_SHORT).show();

                }else{
                String message = msg.getText().toString().trim();
                if (message == "" || message.isEmpty()) {

                } else {

                    userSentAMsg = true;
                    sendMessageToUser(message);

                }}

            }
        });
        FirebaseUtil.allUserCollectionRef().document(otherUser.getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null && value.exists()) {
                    Long availabilityLong = value.getLong("availability");
                    if (availabilityLong != null) {
                        int a = availabilityLong.intValue();
                        if (a == 1) {
                            activity.setVisibility(View.VISIBLE);
                            activity.setImageResource(R.drawable.online_user);
                        } else {
                            activity.setVisibility(View.VISIBLE);
                            activity.setImageResource(R.drawable.offline_user);
                        }
                    } else {
                    }
                } else {
                }
            }
        });
        SetupRecyclerView();
        recyclerView.getLayoutManager().scrollToPosition(adapter.getItemCount() - 1);

    }
    public void sendMessageNotificationToUser(String idRecever, String message) {
        FirebaseUtil.allUserCollectionRef().document(idRecever).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot != null) {
                String ReceverToken = documentSnapshot.getString("token");
                NotifOnline notifOnline = new NotifOnline(ReceverToken, "new message from " + currentUserName, message, ConvActivity.this);
                notifOnline.sendNotif();
            }
        });
    }

    private void getUsername() {

        FirebaseUtil.allUserCollectionRef().document(FirebaseUtil.getCurrentUserId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        currentUserName = documentSnapshot.getString("username");

                    } else {
                        currentUserName = "";
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching username: ", e);
                    currentUserName = "";
                });
    }


    private void SetupRecyclerView() {

        Query query = FirebaseUtil.getChatroomMsgRef(chatroomId).orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ChatMsgModel> options = new FirestoreRecyclerOptions.Builder<ChatMsgModel>().setQuery(query, ChatMsgModel.class).build();
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

    private void getOrCreateChatroomModel() {

        FirebaseUtil.getChatroomRef(chatroomId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    chatroomModel = task.getResult().toObject(ChatroomModel.class);
                    if (chatroomModel == null) {
                        chatroomModel = new ChatroomModel(chatroomId
                                , Arrays.asList(FirebaseUtil.getCurrentUserId()
                                , otherUser.getId())
                                , Timestamp.now(), "", "", 0,0,0,"","","","");
                        FirebaseUtil.getChatroomRef(chatroomId).set(chatroomModel);
                    } else if (!chatroomModel.getLastMsgSenderId().equals(FirebaseUtil.getCurrentUserId())) {
                        chatroomModel.setUnseenMsg(0);
                        FirebaseUtil.getChatroomRef(chatroomId).set(chatroomModel);
                        Log.d(TAG, "deleted unseen conv: ");
                    }
                }
            }
        });
    }

    private void sendMessageToUser(String message1) {
        FirebaseUtil.allChatroomCollectionRef().document(chatroomId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null && value.exists()) {
                    chatroomModel.setUnseenMsg(value.getLong("unseenMsg").intValue() + 1);
                }
            }
        });
        chatroomModel.setLastMsgSent(message1);
        chatroomModel.setLastMsgSenderId(FirebaseUtil.getCurrentUserId());
        chatroomModel.setLastMsgTimeStamp(Timestamp.now());
        FirebaseUtil.getChatroomRef(chatroomId).set(chatroomModel);
        ChatMsgModel chatMsgModel = new ChatMsgModel(message1, FirebaseUtil.getCurrentUserId(), Timestamp.now());
        FirebaseUtil.getChatroomMsgRef(chatroomId).add(chatMsgModel).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    String generatedId = task.getResult().getId();
                    task.getResult().update("messageId", generatedId)
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Message ID added successfully"))
                            .addOnFailureListener(e -> Log.e("Firestore", "Failed to add message ID", e));
                    msg.setText("");

                }
            }
        });

        sendMessageNotificationToUser(otherUser.getId(),message1);

    }

    @Override
    public void onBackPressed() {
        if (getIntent().getStringExtra("lastMsgId") != FirebaseUtil.getCurrentUserId()) {
            if (!userSentAMsg) {
                FirebaseUtil.allChatroomCollectionRef().document(chatroomId).update("unseenMsg", 0);
            }

        }

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("lastFragment", "ChatsFragment").apply();
        Intent intent = new Intent(ConvActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

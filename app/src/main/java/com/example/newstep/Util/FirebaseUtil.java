package com.example.newstep.Util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.newstep.Models.ChatroomModel;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FirebaseUtil {
    public static UserModel getUserModelFromIntent(Intent intent){
        UserModel userModel=new UserModel();
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setId(intent.getStringExtra("userId"));

        userModel.setAvailability(intent.getIntExtra("availability",0));
        return userModel;
    }
    public static String getCurrentUserId(){
        FirebaseUser UserId=FirebaseAuth.getInstance().getCurrentUser();
        if(UserId!=null){
            return UserId.getUid();
        }
        else{
            return null;
        }
    }
    public static CollectionReference allUserCollectionRef(){
        return FirebaseFirestore.getInstance().collection("Users");
    }
    public static DocumentReference getChatroomRef(String chatroomId){
        return FirebaseFirestore.getInstance().collection("Chatrooms").document(chatroomId);
    }
    public  static CollectionReference getChatroomMsgRef(String chatroomId){
        return getChatroomRef(chatroomId).collection("chats");
    }
    public static String getChatroomId(String userid1,String userid2) {
        if (userid1.hashCode() < userid2.hashCode()) {
            return userid1+"_"+userid2;
        } else {
            return userid2  +"_"+userid1;
        }}

public static String getCurrentUsername(Context context){
    SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
     return sharedPreferences.getString("currUserName", "default_value");
}

    public static CollectionReference allChatroomCollectionRef(){
        return FirebaseFirestore.getInstance().collection("Chatrooms");
    }
    public static CollectionReference allCommentReportCollectionRef(){
        return FirebaseFirestore.getInstance().collection("ReportComments");
    }
    public static CollectionReference allPostsReportCollectionRef(){
        return FirebaseFirestore.getInstance().collection("reportPosts");
    }
    public static CollectionReference allcontactCollectionRef(){
        return FirebaseFirestore.getInstance().collection("contact");
    }
    public static CollectionReference allPostsCollectionRef(){
        return FirebaseFirestore.getInstance().collection("posts");
    }
    public static CollectionReference CommentsCollectionRef(String postId){
        return FirebaseFirestore.getInstance().collection("posts").document(postId).collection("comments");

    }
    public static DocumentReference getOtherUserFromChatroom(List<String> userIds){


        DocumentReference docRef;

        if (userIds.get(0).equals(FirebaseUtil.getCurrentUserId())) {
            docRef = allUserCollectionRef().document(userIds.get(1));
        } else {
            docRef = allUserCollectionRef().document(userIds.get(0));
        }

        Log.d("Chatroom", "Document Reference: " + docRef.getPath()); // Log the document reference
        return docRef;
    }

    public static void loadPfp(String userId, ImageView pfp) {
        FirebaseUtil.allUserCollectionRef().document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String uriString = documentSnapshot.getString("profileImage");

                        if (uriString != null && !uriString.isEmpty()) {
                            Uri uri = Uri.parse(uriString);
                            Log.d("Firestore", "Profile picture URI: " + uri.toString());
                            Picasso.get()
                                    .load(uri)
                                    .placeholder(R.drawable.pfp_purple)
                                    .transform(new CircleTransform())
                                    .into(pfp);
                        } else {
                            pfp.setImageResource(R.drawable.pfp_blue);
                            Log.d("Firestore", "No profile picture");
                        }
                    } else {
                        Log.d("Firestore", "User document does not exist.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting document: " + e.getMessage());
                });


        pfp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = v.getContext();

                FirebaseFirestore.getInstance().collection("Users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                UserModel user = new UserModel();

                                user.setId(userId);
                                user.setUsername(documentSnapshot.getString("username"));
                                user.setProfileImage(documentSnapshot.getString("profileImage"));
                                user.setBio(documentSnapshot.getString("bio"));
                                user.setRegisterDate(documentSnapshot.getString("registerDate"));
                                user.setCoverImage(documentSnapshot.getString("coverImage"));

                                UserProfilePopup.show(context, v, user);
                            } else {
                                Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("PopupError", "Failed to fetch data", e);
                        });
            }
        });
    }



}

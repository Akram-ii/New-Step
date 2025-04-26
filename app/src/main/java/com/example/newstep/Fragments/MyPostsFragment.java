package com.example.newstep.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.newstep.Adapters.PostAdapter;
import com.example.newstep.Models.PostModel;
import com.example.newstep.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;


public class MyPostsFragment extends Fragment {


     private RecyclerView recyclerView;
    private CommentDialogListener listener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_posts, container, false);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        recyclerView = view.findViewById(R.id.profilePostRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadProfilposts();
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CommentDialogListener) {
            listener = (CommentDialogListener) context; // ربط `listener` بـ `Activity`
        } else {
            throw new RuntimeException(context.toString() + " يجب أن تنفذ CommentDialogListener");
        }
    }




    private void setupRecyclerView(List<PostModel> posts) {
        PostAdapter adapter = new PostAdapter(getContext(), posts);

        recyclerView.setAdapter(adapter);
    }



    /*
    private void loadProfilposts() {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {

            String currentUserId = currentUser.getUid();


            firestore.collection("posts")
                    .whereEqualTo("userId", currentUserId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e("FirestoreError", "Error loading posts: ", error);
                            return;
                        }

                        List<PostModel> profilePosts = new ArrayList<>();
                        if (value != null) {
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                String postId = doc.getId();
                                String content = doc.getString("content");
                                String userId = doc.getString("userId");
                                String cat = doc.getString("category");
                                String userName = doc.getString("username");
                                Long likes = doc.contains("likes") ? doc.getLong("likes") : 0;
                                Long dislikes = doc.contains("dislikes") ? doc.getLong("dislikes") : 0;
                                Timestamp timestampPost = doc.getTimestamp("timestamp");
                                String profileImageUrl = doc.getString("profileImage");

                                List<String> likedBy = (List<String>) doc.get("likedBy");
                                List<String> dislikedBy = (List<String>) doc.get("dislikedBy");

                                if (content != null && userId != null && userName != null && timestampPost != null) {
                                    PostModel post = new PostModel(userId,postId, content, likes.intValue(), userName, dislikes.intValue(), timestampPost, profileImageUrl);
                                    PostModel post = new PostModel(postId, userId,content, likes.intValue(), userName, dislikes.intValue(), timestampPost, profileImageUrl,cat);

                                    post.setLikedBy(likedBy != null ? likedBy : new ArrayList<>());
                                    post.setDislikedBy(dislikedBy != null ? dislikedBy : new ArrayList<>());

                                    profilePosts.add(post);
                                }
                            }
                            setupRecyclerView(profilePosts);
                        }

                    });

        } else {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
        }
    }

     */




    private void loadProfilposts() {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {

            String currentUserId = currentUser.getUid();

            firestore.collection("posts")
                    .whereEqualTo("userId", currentUserId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e("FirestoreError", "Error loading posts: ", error);
                            return;
                        }

                        List<PostModel> profilePosts = new ArrayList<>();
                        if (value != null) {
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                String postId = doc.getId();
                                String content = doc.getString("content");
                                String userId = doc.getString("userId");
                                String userName = doc.getString("username");
                                String cat = doc.getString("cat");
                                Long likes = doc.contains("likes") ? doc.getLong("likes") : 0;
                                Long dislikes = doc.contains("dislikes") ? doc.getLong("dislikes") : 0;
                                Timestamp timestampPost = doc.getTimestamp("timestamp");

                                List<String> likedBy = (List<String>) doc.get("likedBy");
                                List<String> dislikedBy = (List<String>) doc.get("dislikedBy");

                                if (content != null && userId != null && userName != null && timestampPost != null) {


                                    firestore.collection("Users")
                                            .document(userId)
                                            .get()
                                            .addOnSuccessListener(userDoc -> {
                                                String profileImageUrl = userDoc.getString("profileImage");
                                                String name = userDoc.getString("username");


                                                PostModel post = new PostModel(userId, postId, content,
                                                        likes.intValue(), name, dislikes.intValue(),
                                                        timestampPost, profileImageUrl,cat);

                                                post.setLikedBy(likedBy != null ? likedBy : new ArrayList<>());
                                                post.setDislikedBy(dislikedBy != null ? dislikedBy : new ArrayList<>());

                                                profilePosts.add(post);
                                                setupRecyclerView(profilePosts);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("UserFetchError", "Failed to fetch user profile image", e);
                                            });
                                }
                            }
                        }

                    });

        } else {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
        }
    }


    public interface CommentDialogListener {
        void showCommentDialog(String postId);
    }




}
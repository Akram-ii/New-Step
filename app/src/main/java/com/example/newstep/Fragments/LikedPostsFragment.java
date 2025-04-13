package com.example.newstep.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.newstep.Adapters.CommentAdapter;
import com.example.newstep.Adapters.PostAdapter;
import com.example.newstep.Models.Comment;
import com.example.newstep.Models.PostModel;
import com.example.newstep.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class LikedPostsFragment extends Fragment {


    private RecyclerView recyclerView;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_liked_posts, container, false);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        recyclerView = view.findViewById(R.id.recyclerViewLikedPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            loadProfillikesPosts();


    }



    private void setupRecyclerView(List<PostModel> posts) {
        PostAdapter adapter = new PostAdapter(getContext(), posts, postId -> {

        });

        recyclerView.setAdapter(adapter);
    }



    private void loadProfillikesPosts() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            firestore.collection("posts")
                    .whereArrayContains("likedBy", currentUserId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e("FirestoreError", "Error loading liked posts: ", error);
                            return;
                        }

                        List<PostModel> likedPosts = new ArrayList<>();
                        if (value != null) {
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                String postId = doc.getId();
                                String content = doc.getString("content");
                                String userId = doc.getString("userId");
                                String userName = doc.getString("username");
                                Long likes = doc.contains("likes") ? doc.getLong("likes") : 0;
                                Long dislikes = doc.contains("dislikes") ? doc.getLong("dislikes") : 0;
                                Timestamp timestampPost = doc.getTimestamp("timestamp");
                                String profileImageUrl = doc.getString("profileImage");

                                List<String> likedBy = (List<String>) doc.get("likedBy");
                                List<String> dislikedBy = (List<String>) doc.get("dislikedBy");

                                if (content != null && userId != null && userName != null && timestampPost != null) {
                                    PostModel post = new PostModel(postId, content, likes.intValue(), userName, dislikes.intValue(), timestampPost, profileImageUrl);

                                    post.setLikedBy(likedBy != null ? likedBy : new ArrayList<>());
                                    post.setDislikedBy(dislikedBy != null ? dislikedBy : new ArrayList<>());

                                    likedPosts.add(post);
                                }
                            }
                            setupRecyclerView(likedPosts);
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
        }
    }



}
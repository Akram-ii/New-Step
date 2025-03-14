
package com.example.newstep.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Adapters.CommentAdapter;
import com.example.newstep.Adapters.PostAdapter;
import com.example.newstep.Models.Comment;
import com.example.newstep.Models.PostModel;
import com.example.newstep.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommunityFragment extends Fragment {

    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<PostModel> postList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.post_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList, this::showCommentDialog);

        recyclerView.setAdapter(postAdapter);
        ;


        loadPosts();

        // Set up the "Add Post" button
        FloatingActionButton buttonOpenPopup = view.findViewById(R.id.buttonOpenPopup);
        buttonOpenPopup.setOnClickListener(v -> showPopupWindow());
    }


    private void loadPosts() {

        firestore.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FirestoreError", "Error loading posts: ", error);
                        return;
                    }

                    postList.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            String postId = doc.getId();
                            String content = doc.getString("content");
                            String userId = doc.getString("userId");
                            String userName = doc.getString("username");
                            Long likes = doc.contains("likes") ? doc.getLong("likes") : 0;
                            Long dislikes = doc.contains("dislikes") ? doc.getLong("dislikes") : 0;
                            Timestamp timestampPost = doc.getTimestamp("timestamp");


                            List<String> likedBy = (List<String>) doc.get("likedBy");
                            List<String> dislikedBy = (List<String>) doc.get("dislikedBy");

                            if (content != null && userId != null && userName != null && timestampPost != null) {
                                PostModel post = new PostModel(postId, content, likes.intValue(), userName, dislikes.intValue(), timestampPost);


                                post.setLikedBy(likedBy != null ? likedBy : new ArrayList<>());
                                post.setDislikedBy(dislikedBy != null ? dislikedBy : new ArrayList<>());

                                postList.add(post);
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    }

                });
    }


    private void showPopupWindow() {
        // Vérifier que l'activité est toujours en cours d'exécution
        if (getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) {
            return; // Ne pas afficher la pop-up si l'activité est en train de se terminer
        }

        // Inflater le layout de la pop-up
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.popup_create_post, null);

        // Configurer la fenêtre pop-up
        PopupWindow popupWindow = new PopupWindow(
                popUpView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Diminuer l'opacité de l'arrière-plan de l'activité lorsque la pop-up est ouverte
        WindowManager.LayoutParams layoutParams = requireActivity().getWindow().getAttributes();
        layoutParams.alpha = 0.5f; // Réduire l'opacité à 50%
        requireActivity().getWindow().setAttributes(layoutParams);

        // Afficher la pop-up au centre de l'écran
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);

        // Restaurer l'opacité de l'arrière-plan après la fermeture de la pop-up
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams originalParams = requireActivity().getWindow().getAttributes();
            originalParams.alpha = 1.0f; // Rétablir l'opacité à 100%
            requireActivity().getWindow().setAttributes(originalParams);
        });

        // Initialiser les boutons de la pop-up
        Button buttonCancel = popUpView.findViewById(R.id.buttonCancel);
        Button buttonPost = popUpView.findViewById(R.id.buttonPost);

        // Fermer la pop-up lorsqu'on clique sur Annuler
        buttonCancel.setOnClickListener(v -> popupWindow.dismiss());

        // Gérer la publication du post lorsqu'on clique sur Publier
        buttonPost.setOnClickListener(v -> {
            EditText editTextPostContent = popUpView.findViewById(R.id.editTextPostContent);
            String postContent = editTextPostContent.getText().toString().trim();

            if (!postContent.isEmpty()) {
                createPost(postContent, popupWindow); // Créer le post
                popupWindow.dismiss();
            } else {
                // Afficher un message d'erreur si le post est vide
                Toast.makeText(requireContext(), "Post cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void createPost(String content, PopupWindow popupWindow) {
        // Obtenir l'utilisateur actuel via Firebase Authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();


        if (currentUser == null) {
            Toast.makeText(requireContext(), "You need to log in", Toast.LENGTH_SHORT).show();
            return;
        }


        String userId = currentUser.getUid();


        firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String username = documentSnapshot.getString("username");


                        Map<String, Object> post = new HashMap<>();
                        post.put("content", content);
                        post.put("userId", userId);
                        post.put("username", username);


                        post.put("timestamp", Timestamp.now());


                        firestore.collection("posts")
                                .add(post)
                                .addOnSuccessListener(documentReference -> {

                                    Toast.makeText(requireContext(), "Post created successfully!", Toast.LENGTH_SHORT).show();
                                    popupWindow.dismiss();
                                })
                                .addOnFailureListener(e ->

                                        Toast.makeText(requireContext(), "error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    } else {

                        Toast.makeText(requireContext(), "user not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->

                        Toast.makeText(requireContext(), "error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showCommentDialog(String postId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        if (currentUser == null) {
            Toast.makeText(requireContext(), "you need to log in", Toast.LENGTH_SHORT).show();
            return;
        }
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_comments, null);
        dialog.setContentView(view);
        view.setBackgroundResource(R.drawable.popupbg);

        dialog.show();

        RecyclerView recyclerViewComments = view.findViewById(R.id.recyclerViewComments);
        EditText editTextComment = view.findViewById(R.id.editTextComment);
        Button buttonSendComment = view.findViewById(R.id.buttonSendComment);

        recyclerViewComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<Comment> commentList = new ArrayList<>();
        CommentAdapter commentAdapter = new CommentAdapter(requireContext(), commentList);
        recyclerViewComments.setAdapter(commentAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        editTextComment.setEnabled(false);
        buttonSendComment.setEnabled(false);


        if (currentUser != null) {
            editTextComment.setEnabled(true);
            buttonSendComment.setEnabled(true);
        }


        db.collection("posts").document(postId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    commentList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            commentList.add(comment);
                        }
                    }
                    commentList.sort((c1, c2) -> Integer.compare(c2.getLikes().size(), c1.getLikes().size()));
                    commentAdapter.notifyDataSetChanged();
                });


        buttonSendComment.setOnClickListener(v -> {
            String text = editTextComment.getText().toString().trim();
            if (!text.isEmpty()) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String commentId = UUID.randomUUID().toString();


                db.collection("Users").document(userId).get()
                        .addOnSuccessListener(userDoc -> {
                            String username = userDoc.exists() ? userDoc.getString("username") : "unknown user";


                            Comment comment = new Comment(commentId, userId, postId, text, System.currentTimeMillis(), username);



                            db.collection("posts").document(postId).collection("comments")
                                    .document(commentId).set(comment)
                                    .addOnSuccessListener(aVoid -> {
                                        editTextComment.setText("");
                                    });
                        });
            }
        });

        dialog.show();
    }
}








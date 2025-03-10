



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

import com.example.newstep.Adapters.PostAdapter;
import com.example.newstep.Models.PostModel;
import com.example.newstep.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);


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

    // Méthode pour créer un post et l'ajouter à Firestore
    private void createPost(String content, PopupWindow popupWindow) {
        // Obtenir l'utilisateur actuel via Firebase Authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();


        // Vérifier si l'utilisateur est connecté
        if (currentUser == null) {
            Toast.makeText(requireContext(), "You need to log in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupérer l'ID de l'utilisateur actuel
        String userId = currentUser.getUid();

        // Récupérer le username depuis la collection "Users" dans Firestore
        firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Vérifier si le document utilisateur existe
                    if (documentSnapshot.exists()) {
                        // Récupérer le champ "username" du document utilisateur
                        String username = documentSnapshot.getString("username");

                        // Créer une structure de données pour le post
                        Map<String, Object> post = new HashMap<>();
                        post.put("content", content); // Contenu du post
                        post.put("userId", userId); // ID de l'utilisateur
                        post.put("username", username); // Nom d'utilisateur

                        // Horodatage du post
                        post.put("timestamp", Timestamp.now());


                        // Ajouter le post à la collection "posts" dans Firestore
                        firestore.collection("posts")
                                .add(post)
                                .addOnSuccessListener(documentReference -> {
                                    // Afficher un message de succès
                                    Toast.makeText(requireContext(), "Post created successfully!", Toast.LENGTH_SHORT).show();
                                    popupWindow.dismiss(); // Fermer la pop-up après succès
                                })
                                .addOnFailureListener(e ->
                                        // Afficher un message d'erreur en cas d'échec
                                        Toast.makeText(requireContext(), "error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        // Afficher un message d'erreur si le document utilisateur n'existe pas
                        Toast.makeText(requireContext(), "user not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        // Afficher un message d'erreur en cas d'échec de la récupération du document
                        Toast.makeText(requireContext(), "error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    // Rest of your code (showPopupWindow, createPost, etc.)
}








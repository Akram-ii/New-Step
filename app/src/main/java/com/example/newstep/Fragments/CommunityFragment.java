    
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
    import android.widget.ArrayAdapter;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.ImageView;
    import android.widget.PopupWindow;
    import android.widget.Spinner;
    import android.widget.TextView;
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
    import com.example.newstep.Util.NotifOnline;
    import com.google.android.material.bottomsheet.BottomSheetDialog;
    import com.google.android.material.chip.Chip;
    import com.google.android.material.chip.ChipGroup;
    import com.google.android.material.floatingactionbutton.FloatingActionButton;
    import com.google.firebase.Timestamp;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.Query;

    import org.apache.commons.logging.LogFactory;

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

        private static final org.apache.commons.logging.Log log = LogFactory.getLog(CommunityFragment.class);
        private FirebaseFirestore firestore;
        ImageView filter;
        private RecyclerView recyclerView;
        private PostAdapter postAdapter;
        private List<PostModel> postList;
        private List<String> selectedCategories;
    
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
    
            selectedCategories=new ArrayList<>();
            filter=view.findViewById(R.id.filter);

            recyclerView = view.findViewById(R.id.post_recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            postList = new ArrayList<>();
            postAdapter = new PostAdapter(getContext(), postList, this::showCommentDialog);
            recyclerView.setAdapter(postAdapter);

    
    
            loadPosts();
    
            // Set up the "Add Post" button
            FloatingActionButton buttonOpenPopup = view.findViewById(R.id.buttonOpenPopup);
            buttonOpenPopup.setOnClickListener(v -> showPopupWindow());
            filter.setOnClickListener(v ->  {selectedCategories.clear();
                    showPopupWindowFilter();});
        }
    
        private void showPopupWindowFilter() {
            if (getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) {
                return;
            }
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popUpView = inflater.inflate(R.layout.popup_filter, null);
            PopupWindow popupWindow = new PopupWindow(
                    popUpView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams layoutParams = requireActivity().getWindow().getAttributes();
            layoutParams.alpha = 0.5f;
            requireActivity().getWindow().setAttributes(layoutParams);
            popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);
            popupWindow.setOnDismissListener(() -> {
                WindowManager.LayoutParams originalParams = requireActivity().getWindow().getAttributes();
                originalParams.alpha = 1.0f;
                requireActivity().getWindow().setAttributes(originalParams);
            });
    Button done;
    ImageView cancel;
    TextView text;
    ImageButton nothing,all;
    ChipGroup chipGroup;
    chipGroup=popUpView.findViewById(R.id.chipGroup);
    all=popUpView.findViewById(R.id.all);
    text=popUpView.findViewById(R.id.selectText);
    nothing=popUpView.findViewById(R.id.nothing);
    done=popUpView.findViewById(R.id.filterNow);
    cancel=popUpView.findViewById(R.id.backBtn);
    cancel.setOnClickListener(v -> {popupWindow.dismiss();});
    all.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            for(int i=0;i<chipGroup.getChildCount();i++){
                View child = chipGroup.getChildAt(i);
                if(child instanceof Chip){
                    ((Chip) child).setChecked(true);
                }
            }
        }
    });

            nothing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for(int i=0;i<chipGroup.getChildCount();i++){
                        View child = chipGroup.getChildAt(i);
                        if(child instanceof Chip){
                            ((Chip) child).setChecked(false);
                        }
                    }
                }
            });
        chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {

                if(checkedIds.size()!=group.getChildCount()){
                    nothing.setVisibility(View.GONE);
                    all.setVisibility(View.VISIBLE);
                    text.setText("All");
                }else{
                    if(checkedIds.size()==group.getChildCount()){
                        nothing.setVisibility(View.VISIBLE);
                        all.setVisibility(View.GONE);
                        text.setText("None");
                    }
                }
            }
        });

        done.setOnClickListener(v->{
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                if (chip.isChecked()) {
                    selectedCategories.add(chip.getText().toString());
                    Log.d("filtere, cat: ",""+chip.getText().toString());
                }
            }
            if(!selectedCategories.isEmpty()){
            setupRecycler(selectedCategories);
                popupWindow.dismiss();
            }
            else{
                Toast.makeText(getContext(),"You need to select at least one category",Toast.LENGTH_SHORT).show();
            }
            });
        }
    
        private void setupRecycler(List<String> list) {
            firestore.collection("posts").whereIn("category",list).orderBy("timestamp", Query.Direction.DESCENDING)
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
                                String cat = doc.getString("category");
                                Long likes = doc.contains("likes") ? doc.getLong("likes") : 0;
                                Long dislikes = doc.contains("dislikes") ? doc.getLong("dislikes") : 0;
                                Timestamp timestampPost = doc.getTimestamp("timestamp");
    
    
                                List<String> likedBy = (List<String>) doc.get("likedBy");
                                List<String> dislikedBy = (List<String>) doc.get("dislikedBy");
    
                                if (content != null && userId != null && userName != null && timestampPost != null) {
                                    PostModel post = new PostModel(postId,userId, content, likes.intValue(), userName, dislikes.intValue(), timestampPost,cat);
    
    
                                    post.setLikedBy(likedBy != null ? likedBy : new ArrayList<>());
                                    post.setDislikedBy(dislikedBy != null ? dislikedBy : new ArrayList<>());
    
                                    postList.add(post);
                                }
                            }
                            postAdapter.notifyDataSetChanged();
                        }
    
                    });
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
                                String cat = doc.getString("category");
                                Long likes = doc.contains("likes") ? doc.getLong("likes") : 0;
                                Long dislikes = doc.contains("dislikes") ? doc.getLong("dislikes") : 0;
                                Timestamp timestampPost = doc.getTimestamp("timestamp");
    
    
                                List<String> likedBy = (List<String>) doc.get("likedBy");
                                List<String> dislikedBy = (List<String>) doc.get("dislikedBy");
    
                                if (content != null && userId != null && userName != null && timestampPost != null) {
                                    PostModel post = new PostModel(postId,userId, content, likes.intValue(), userName, dislikes.intValue(), timestampPost,cat);
    
    
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
            Spinner spinnerCategory= popUpView.findViewById(R.id.spinner_category);
            List<String> categories = new ArrayList<>();
            categories.add("My Experience");
            categories.add("Ask for help");
            categories.add("Motivation");
            categories.add("Reflection");
            categories.add("Progress Update");
            categories.add("Challenges");
            categories.add("Advices");
            categories.add("Other");
    
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1,categories);
            adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            spinnerCategory.setAdapter(adapter);
    
    
            // Fermer la pop-up lorsqu'on clique sur Annuler
            buttonCancel.setOnClickListener(v -> popupWindow.dismiss());
    
            // Gérer la publication du post lorsqu'on clique sur Publier
            buttonPost.setOnClickListener(v -> {
                EditText editTextPostContent = popUpView.findViewById(R.id.editTextPostContent);
                String postContent = editTextPostContent.getText().toString().trim();
    
                if (!postContent.isEmpty()) {
                    String selectCategory =spinnerCategory.getSelectedItem().toString();
                    createPost(postContent, selectCategory,popupWindow); // Créer le post
                    popupWindow.dismiss();
                } else {
                    // Afficher un message d'erreur si le post est vide
                    Toast.makeText(requireContext(), "Post cannot be empty", Toast.LENGTH_SHORT).show();
                }
    
    
    
    
            });
        }
    
    
        private void createPost(String content,String category, PopupWindow popupWindow) {
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
                            post.put("category",category);
    
    
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
    
    
                                            db.collection("posts").document(postId)
                                                    .get()
                                                    .addOnSuccessListener(postDoc -> {
                                                        if (postDoc.exists()) {
                                                            String postOwnerId = postDoc.getString("userId");
    
    
                                                            if (postOwnerId != null && !userId.equals(postOwnerId)) {
    
    
                                                                db.collection("Users").document(postOwnerId)
                                                                        .get()
                                                                        .addOnSuccessListener(ownerDoc -> {
                                                                            if (ownerDoc.exists()) {
                                                                                String ownerFCMToken = ownerDoc.getString("token");
    
    
                                                                                String title = "You have a new comment on your post";
                                                                                String body = username + "commented on your post";
    
                                                                                NotifOnline notif = new NotifOnline(ownerFCMToken, title, body, requireContext());
                                                                                notif.sendNotif();
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
    
    
                                        });
                            });
                }
            });
    
            dialog.show();
        }
    }
    
    
    
    
    
    
    

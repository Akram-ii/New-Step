package com.example.newstep.Fragments;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Adapters.AllGroupsAdapter;
import com.example.newstep.Models.ChatroomModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class GroupsFragment extends Fragment {

    private MaterialButton createGroupBtn;
    private RecyclerView recyclerView;
    private AllGroupsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        recyclerView = rootView.findViewById(R.id.groupsRecycler);
        createGroupBtn = rootView.findViewById(R.id.createGroupBtn);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        createGroupBtn.setOnClickListener(v -> showCreateGroupPopup(v));

        setupRecycler();

        return rootView;
    }

    private void setupRecycler() {
        Query query = FirebaseUtil.allChatroomCollectionRef()
                .whereEqualTo("isGroup", 1)
                .orderBy("number_members", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatroomModel> options =
                new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                        .setQuery(query, ChatroomModel.class)
                        .setLifecycleOwner(this)
                        .build();

        adapter = new AllGroupsAdapter(options, requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Vérifie si l'adapter n'est pas null avant de démarrer
        if (adapter != null) {
            adapter.startListening();
        }
    }

    private void showCreateGroupPopup(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_create_group, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setElevation(10);
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        TextInputEditText groupNameInput = popupView.findViewById(R.id.groupNameInput);
        MaterialButton btnCancel = popupView.findViewById(R.id.btnCancel);
        MaterialButton btnAddGroup = popupView.findViewById(R.id.btnAddGroup);

        btnCancel.setOnClickListener(v -> popupWindow.dismiss());

        btnAddGroup.setOnClickListener(v -> {
            String groupName = groupNameInput.getText().toString().trim();

            if (groupName.isEmpty()) {
                Toast.makeText(getContext(), "Le nom du groupe ne peut pas être vide", Toast.LENGTH_SHORT).show();
                return;
            }

            createGroupInFirestore(groupName, popupWindow);
        });

        popupView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                popupWindow.dismiss();
                return true;
            }
            return false;
        });
    }

    private void createGroupInFirestore(String groupName, PopupWindow popupWindow) {
        // Vérification si l'utilisateur est connecté
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Utilisateur non authentifié", Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerId = auth.getCurrentUser().getUid();

        // Génération de l'ID du groupe
        DocumentReference newGroupRef = db.collection("Chatroom").document();
        String chatroomId = newGroupRef.getId(); // Récupération de l'ID généré

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("chatroomId", chatroomId);
        groupData.put("name", groupName);  // Correction: "name" au lieu de "groupName"
        groupData.put("isGroup", 1);
        groupData.put("number_members", 1);
        groupData.put("ownerId", ownerId);
        groupData.put("lastMsgSenderId", "");
        groupData.put("lastMsgSent", "");
        groupData.put("lastMsgTimeStamp", null);
        groupData.put("unseenMsg", 0);

        newGroupRef.set(groupData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CreateGroup", "Groupe ajouté avec ID: " + chatroomId);
                    Toast.makeText(getContext(), "Groupe créé avec succès", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e("CreateGroup", "Erreur lors de l'ajout du groupe", e);
                    Toast.makeText(getContext(), "Erreur lors de la création du groupe", Toast.LENGTH_SHORT).show();
                });
    }
}


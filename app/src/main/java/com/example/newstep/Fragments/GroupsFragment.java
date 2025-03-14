
package com.example.newstep.Fragments;



import android.os.Bundle;

import android.view.Gravity;

import android.view.LayoutInflater;

import android.view.View;

import android.view.ViewGroup;

import android.widget.ArrayAdapter;

import android.widget.AutoCompleteTextView;

import android.widget.Button;

import android.widget.EditText;

import android.widget.PopupWindow;

import android.widget.Toast;



import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;



import com.example.newstep.Adapters.AllGroupsAdapter;

import com.example.newstep.Models.ChatroomModel;

import com.example.newstep.R;

import com.example.newstep.Util.FirebaseUtil;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.Query;



import java.util.Collections;

import java.util.HashMap;

import java.util.Map;

import java.util.UUID;



public class GroupsFragment extends Fragment {



    private RecyclerView recyclerView;

    private AllGroupsAdapter adapter;

    private FloatingActionButton addGroupBtn;



    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);



        recyclerView = rootView.findViewById(R.id.groupsRecycler);

        addGroupBtn = rootView.findViewById(R.id.addGroupBtn);





        addGroupBtn.setOnClickListener(v -> showCreateGroupPopup());





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

    }



    private void showCreateGroupPopup() {

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.popup_create_group, null);



        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);





        EditText groupNameInput = popupView.findViewById(R.id.groupNameInput);

        AutoCompleteTextView categoryDropdown = popupView.findViewById(R.id.groupCategoryDropdown);

        Button addGroupButton = popupView.findViewById(R.id.btnAddGroup);

        Button cancelBtn = popupView.findViewById(R.id.btnCancel);





        String[] categories = {"My Health", "My Money", "Everything", "Something Else"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categories);

        categoryDropdown.setAdapter(adapter);





        cancelBtn.setOnClickListener(v -> popupWindow.dismiss());





        addGroupButton.setOnClickListener(v -> {

            String groupName = groupNameInput.getText().toString().trim();

            String category = categoryDropdown.getText().toString().trim();



            if (groupName.isEmpty()) {

                Toast.makeText(getContext(), "Enter Group Name", Toast.LENGTH_SHORT).show();

                return;

            }

            if (category.isEmpty()) {

                Toast.makeText(getContext(), "Select Category", Toast.LENGTH_SHORT).show();

                return;

            }



            addGroupToFirebase(groupName, category, popupWindow);

        });

    }



    private void addGroupToFirebase(String groupName, String category, PopupWindow popupWindow) {

        String chatroomId = UUID.randomUUID().toString();



        Map<String, Object> groupData = new HashMap<>();

        groupData.put("chatroomId", chatroomId);

        groupData.put("groupName", groupName);

        groupData.put("category", category);

        groupData.put("isGroup", 1);

        groupData.put("ownerId", FirebaseUtil.getCurrentUserId());

        groupData.put("number_members", 1);

        groupData.put("userIds", Collections.singletonList(FirebaseUtil.getCurrentUserId()));

        groupData.put("lastMsgTimeStamp", System.currentTimeMillis());



        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("chatrooms").document(chatroomId).set(groupData)

                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(getContext(), "Group added successfully!", Toast.LENGTH_SHORT).show();

                    popupWindow.dismiss();

                })

                .addOnFailureListener(e -> {

                    Toast.makeText(getContext(), "Failed to add group", Toast.LENGTH_SHORT).show();

                });

    }

}
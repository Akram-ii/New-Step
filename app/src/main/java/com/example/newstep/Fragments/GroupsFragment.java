package com.example.newstep.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Adapters.AllGroupsAdapter;
import com.example.newstep.Models.ChatroomModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsFragment extends Fragment {

    private FloatingActionButton addBtn;
    private Button chat;
    private RecyclerView recyclerView;
    private AllGroupsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        recyclerView = rootView.findViewById(R.id.groupsRecycler);
        addBtn = rootView.findViewById(R.id.addBtn);
        chat = rootView.findViewById(R.id.chatsBtn);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        addBtn.setOnClickListener(v -> showCreateGroupPopup(v));
        chat.setOnClickListener(v -> setupChatBtn());

        setupRecycler();

        return rootView;
    }

    private void setupRecycler() {
        Query query = FirebaseUtil.allChatroomCollectionRef()
                .whereEqualTo("isGroup", 1)
                .orderBy("number_members", Query.Direction.DESCENDING);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d("FirestoreDebug", "Groups Found: " + queryDocumentSnapshots.size());
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Log.d("FirestoreDebug", "Group: " + doc.getData());
            }
        }).addOnFailureListener(e -> Log.e("FirestoreDebug", "Error fetching groups", e));

        FirestoreRecyclerOptions<ChatroomModel> options =
                new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                        .setQuery(query, ChatroomModel.class)
                        .setLifecycleOwner(this)
                        .build();

        adapter = new AllGroupsAdapter(options, requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void showCreateGroupPopup(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_create_group, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
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
        EditText groupNameInput = popupView.findViewById(R.id.groupNameInput);
        EditText groupDescInput=popupView.findViewById(R.id.group_desc);
        Button btnCancel = popupView.findViewById(R.id.btnCancel);
        Button btnAddGroup = popupView.findViewById(R.id.btnAddGroup);

        btnCancel.setOnClickListener(v -> popupWindow.dismiss());

        btnAddGroup.setOnClickListener(v -> {
            String groupName = groupNameInput.getText().toString().trim();
            String groupDesc = groupDescInput.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(getContext(), "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }else if (groupName.length()>25) {
                Toast.makeText(getContext(), "Group name is too long", Toast.LENGTH_SHORT).show();
                return;
            }else
                if(groupDesc.isEmpty()){
                Toast.makeText(getContext(), "Group description cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            createGroupInFirestore(groupName,groupDesc, popupWindow);
        });

        popupView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                popupWindow.dismiss();
                return true;
            }
            return false;
        });
    }

    private void createGroupInFirestore(String groupName,String desc, PopupWindow popupWindow) {

        String ownerId = auth.getCurrentUser().getUid();
        DocumentReference newGroupRef = db.collection("Chatrooms").document();
        String chatroomId = newGroupRef.getId();

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("chatroomId", chatroomId);
        List<String> userIds=new ArrayList<>();
        userIds.add(FirebaseUtil.getCurrentUserId());
        groupData.put("userIds",userIds);
        groupData.put("desc",desc);
        groupData.put("groupName", groupName);
        groupData.put("isGroup", 1);
        groupData.put("number_members", 1);
        groupData.put("ownerId", ownerId);
        groupData.put("lastMsgSenderId",FirebaseUtil.getCurrentUserId());
        groupData.put("lastMsgSent", "");
        groupData.put("lastMsgTimeStamp", null);
        groupData.put("unseenMsg", 0);

        newGroupRef.set(groupData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Group created !", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error:"+e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setupChatBtn() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof ChatsFragment) {
            Log.d("ChatsFragment", "Already in ChatsFragment. No need to replace.");
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        ChatsFragment chatsFragment = new ChatsFragment();
        transaction.replace(R.id.fragment_container, chatsFragment, ChatsFragment.class.getSimpleName());
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }
}

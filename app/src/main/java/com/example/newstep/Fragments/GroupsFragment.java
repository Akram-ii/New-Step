package com.example.newstep.Fragments;

import static com.example.newstep.Util.Utilities.hexCodeForColor;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.newstep.Adapters.AllGroupsAdapter;
import com.example.newstep.Models.ChatroomModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.Utilities;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsFragment extends Fragment {

    private FloatingActionButton addBtn;
    private RecyclerView recyclerView;
    private AllGroupsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ImageView filter_groups;
    private String selectedIcon,selectedColor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.groupsRecycler);
        addBtn = view.findViewById(R.id.addBtn);
        filter_groups=view.findViewById(R.id.filter_group);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        addBtn.setOnClickListener(v -> showCreateGroupPopup(v));
        filter_groups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFilterGroupsPopup();
            }
        });

        setupRecycler();

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
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
        // Diminuer l'opacité de l'arrière-plan de l'activité lorsque la pop-up est ouverte
        WindowManager.LayoutParams layoutParams = requireActivity().getWindow().getAttributes();

        requireActivity().getWindow().setAttributes(layoutParams);

        // Afficher la pop-up au centre de l'écran
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);

        // Restaurer l'opacité de l'arrière-plan après la fermeture de la pop-up
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams originalParams = requireActivity().getWindow().getAttributes();
            originalParams.alpha = 1.0f; // Rétablir l'opacité à 100%
            requireActivity().getWindow().setAttributes(originalParams);
        });
        RadioGroup radioGroup=popupView.findViewById(R.id.radioGroup);
        ChipGroup chipGroupIcon=popupView.findViewById(R.id.chipGroupIcon);
        ChipGroup chipGroupColor=popupView.findViewById(R.id.chipGroupColor);
        RadioButton radioPublic = popupView.findViewById(R.id.radioPublic);
        RadioButton radioPrivate = popupView.findViewById(R.id.radioPrivate);
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
            popupWindow.dismiss();
            int selectedId = radioGroup.getCheckedRadioButtonId();
            String privacySetting = "Public";
            if (selectedId == R.id.radioPublic) {
                privacySetting = "Public";
            } else if (selectedId == R.id.radioPrivate) {
                privacySetting = "Private";
            }
            int iconId=chipGroupIcon.getCheckedChipId();
            int colorId=chipGroupColor.getCheckedChipId();
            String iconName=getResources().getResourceEntryName(iconId);
            String colorName=getResources().getResourceEntryName(colorId);
            String colorHexCode= Utilities.hexCodeForColor(colorName);
            createGroupInFirestore(groupName,groupDesc,privacySetting,iconName,colorHexCode);
        });

        popupView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                popupWindow.dismiss();
                return true;
            }
            return false;
        });
    }

    private String hexCodeForColor(String colorName) {
        switch(colorName){
            case "pink":
                return "#C9A6D6";
            case "purple":
                return "#877DE0";
            case "blue":
                return "#6A96E6";
            case "green":
                return "#91B2BD";
            case "gray":
                return "#6C757D";
            case "darkBlue":
                return "#3C3C64";

            default :
                return "#D7BDE2";
        }
    }

    private void createGroupInFirestore(String groupName,String desc,String privacy,String icon,String color) {

        String ownerId = auth.getCurrentUser().getUid();
        DocumentReference newGroupRef = db.collection("Chatrooms").document();
        String chatroomId = newGroupRef.getId();

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("chatroomId", chatroomId);
        List<String> userIds=new ArrayList<>();
        userIds.add(FirebaseUtil.getCurrentUserId());
        groupData.put("userIds",userIds);
        groupData.put("desc",desc);
        groupData.put("privacy",privacy);
        groupData.put("groupName", groupName);
        groupData.put("isGroup", 1);
        groupData.put("icon", icon);
        groupData.put("iconColor", color);
        groupData.put("number_members", 1);
        groupData.put("ownerId", ownerId);
        groupData.put("lastMsgSenderId",FirebaseUtil.getCurrentUserId());
        groupData.put("lastMsgSent", "");
        groupData.put("lastMsgTimeStamp", null);
        groupData.put("creationTimestamp", Timestamp.now());
        groupData.put("unseenMsg", 0);

        newGroupRef.set(groupData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Group created !", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error:"+e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void showFilterGroupsPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.popup_filter_groups, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.PopupWindowAnimation;
        }
        dialog.show();
        EditText searchGroupName = view.findViewById(R.id.searchGroupName);
        ChipGroup privacyChipGroup = view.findViewById(R.id.privacyChipGroup);
        MaterialButton filter = view.findViewById(R.id.btnFilterNow);
        ImageView back= view.findViewById(R.id.back_popup);






        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchName = searchGroupName.getText().toString().trim();
                List<String> selectedFilters = getSelectedPrivacy(privacyChipGroup);
                applyFilters(searchName, selectedFilters);
                dialog.dismiss();
            }
        });
        searchGroupName.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                Drawable[] drawables = searchGroupName.getCompoundDrawables();
                Drawable drawableStart = drawables[0]; // drawableStart = left

                if (drawableStart != null) {
                    int drawableWidth = drawableStart.getBounds().width();


                    if (event.getX() <= (drawableWidth + 40)) {
                        searchGroupName.setText("");
                        return true;
                    }
                }
            }
            return false;
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


    }





    private List<String> getSelectedPrivacy(ChipGroup chipGroup) {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selected.add(chip.getText().toString());
            }
        }
        return selected;
    }
    private void applyFilters(String searchName, List<String> selectedFilters) {
        Query query = FirebaseUtil.allChatroomCollectionRef()
                .whereEqualTo("isGroup", 1);

        if (!TextUtils.isEmpty(searchName)) {
            query = query.whereGreaterThanOrEqualTo("groupName", searchName)
                    .whereLessThanOrEqualTo("groupName", searchName + "\uf8ff");
        }



        if (selectedFilters.contains("Public")) {
            query = query.whereEqualTo("privacy", "Public");
        } else if (selectedFilters.contains("Private")) {
            query = query.whereEqualTo("privacy", "Private");
        }

        if (selectedFilters.contains("Popular")) {
            query = query.whereGreaterThan("number_members", 5);
        }
        if (selectedFilters.contains("Very Popular")) {
            query = query.whereGreaterThan("number_members", 30);
        }
        if (selectedFilters.contains("Most Popular")) {
            query = query.whereGreaterThan("number_members", 50);
        }
        if (selectedFilters.contains("Active Today")) {


            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfDay = calendar.getTimeInMillis();


            Timestamp timestampStartOfDay = new Timestamp(new Date(startOfDay));


            Calendar endOfDayCalendar = Calendar.getInstance();
            endOfDayCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endOfDayCalendar.set(Calendar.MINUTE, 59);
            endOfDayCalendar.set(Calendar.SECOND, 59);
            endOfDayCalendar.set(Calendar.MILLISECOND, 999);
            long endOfDay = endOfDayCalendar.getTimeInMillis();


            Timestamp timestampEndOfDay = new Timestamp(new Date(endOfDay));


            query = query.whereGreaterThanOrEqualTo("lastMsgTimeStamp", timestampStartOfDay)
                    .whereLessThanOrEqualTo("lastMsgTimeStamp", timestampEndOfDay);
        }
        if (selectedFilters.contains("Newest")) {
            query = query.orderBy("creationTimestamp", Query.Direction.DESCENDING);
        } else if (selectedFilters.contains("Oldest")) {
            query = query.orderBy("creationTimestamp", Query.Direction.ASCENDING);
        }

        if (selectedFilters.contains("My Groups")) {
            query = query.whereEqualTo("ownerId", FirebaseUtil.getCurrentUserId());
        }



            FirestoreRecyclerOptions<ChatroomModel> options =
                new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                        .setQuery(query, ChatroomModel.class)
                        .setLifecycleOwner(this)
                        .build();

        adapter.stopListening();
        adapter.updateOptions(options);
        recyclerView.scrollToPosition(0);
        adapter.notifyDataSetChanged();
        adapter.startListening();
    }







    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }





}

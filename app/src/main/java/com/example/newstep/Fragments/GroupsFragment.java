package com.example.newstep.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.newstep.Adapters.AllGroupsAdapter;
import com.example.newstep.Adapters.RecentChatRecyclerAdapter;
import com.example.newstep.Models.ChatroomModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

public class GroupsFragment extends Fragment {
private Button chat;
private RecyclerView recyclerView;
private AllGroupsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);
        recyclerView=rootView.findViewById(R.id.groupsRecycler);
        chat=rootView.findViewById(R.id.chatsBtn);
chat.setOnClickListener(v->{
    setupChatBtn();
});

    //code t3 fragment group ykon lhna

        rootView.setVisibility(View.VISIBLE);
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
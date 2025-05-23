package com.example.newstep.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.example.newstep.Adapters.RecentChatRecyclerAdapter;
import com.example.newstep.Models.ChatroomModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;


public class PrivateChatsFragment extends Fragment {

    private FloatingActionButton addPerson;
    Fragment secondFragment;
    private RecyclerView recyclerView;
    FragmentTransaction transaction;

    RecentChatRecyclerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_private_chats, container, false);


        return rootView;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addPerson = view.findViewById(R.id.addPerson);
        transaction = getParentFragmentManager().beginTransaction();
        recyclerView = view.findViewById(R.id.recyclerViewChats);
        secondFragment = new SearchFragment();
        addPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                Fragment existingFragment = fragmentManager.findFragmentByTag(SearchFragment.class.getSimpleName());
                if (existingFragment != null) {
                    fragmentManager.beginTransaction().remove(existingFragment).commit();
                }

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                SearchFragment searchFragment = new SearchFragment();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.fragment_container, searchFragment, SearchFragment.class.getSimpleName());
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();
            }
        });

        SetupRecyclerView();
    }

    public void SetupRecyclerView() {
        if (recyclerView == null) {
            Log.e("PrivateChatsFragment", "RecyclerView is null, skipping SetupRecyclerView");
            return;
        }

        Query query = FirebaseUtil.allChatroomCollectionRef()
                .whereArrayContains("userIds", FirebaseUtil.getCurrentUserId())
                .orderBy("lastMsgTimeStamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(query, ChatroomModel.class).build();

        adapter = new RecentChatRecyclerAdapter(options, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.requestLayout();
        adapter.startListening();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            adapter.startListening();
        }
    }
}

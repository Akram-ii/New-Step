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
import android.widget.RelativeLayout;

import com.example.newstep.Adapters.RecentChatRecyclerAdapter;
import com.example.newstep.Models.ChatroomModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;


public class ChatsFragment extends Fragment {

    Button group;
    private FloatingActionButton addPerson;
    Fragment secondFragment;
    private RecyclerView recyclerView;
    FragmentTransaction transaction;

    RecentChatRecyclerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);

        group=rootView.findViewById(R.id.groupsBtn);
        if (group == null) {
            Log.e("GroupsFragment", "Error: groupsBtn not found in fragment_chats.xml!");
        } else {
            Log.d("GroupsFragment", "groupsBtn found! Setting click listener.");
            group.setOnClickListener(v -> setupGroupBtn());
        }
        addPerson = rootView.findViewById(R.id.addPerson);
        transaction = getParentFragmentManager().beginTransaction();
        recyclerView = rootView.findViewById(R.id.recyclerViewChats);
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
                transaction.replace(R.id.fragment_container, searchFragment, SearchFragment.class.getSimpleName());
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();
            }
        });

        SetupRecyclerView();
        return rootView;

    }

    private void setupGroupBtn() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        Fragment existingFragment = fragmentManager.findFragmentByTag(GroupsFragment.class.getSimpleName());
        if (existingFragment != null) {
            fragmentManager.beginTransaction().remove(existingFragment).commit();
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        GroupsFragment searchFragment = new GroupsFragment();
        transaction.replace(R.id.fragment_container, searchFragment, GroupsFragment.class.getSimpleName());
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }
    private void SetupRecyclerView() {

        Query query = FirebaseUtil.allChatroomCollectionRef()
                .whereArrayContains("userIds", FirebaseUtil.getCurrentUserId())
              ;

        FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>().setQuery(query, ChatroomModel.class).build();
        adapter = new RecentChatRecyclerAdapter(options, requireContext());
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

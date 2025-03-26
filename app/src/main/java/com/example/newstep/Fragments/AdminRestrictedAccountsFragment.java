package com.example.newstep.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.newstep.Adapters.SearchUserRecyclerAdapter;
import com.example.newstep.Adapters.UsersRestrictedAdapter;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class AdminRestrictedAccountsFragment extends Fragment {
RecyclerView v;
UsersRestrictedAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.fragment_admin_restricted_accounts, container, false);
v=rootView.findViewById(R.id.v);
 setupRecycler();
        return rootView;
    }

    private void setupRecycler() {
        Query query = FirebaseUtil.allUserCollectionRef()
                .whereEqualTo("isRestricted",Boolean.TRUE);

        FirestoreRecyclerOptions<UserModel> options=new FirestoreRecyclerOptions.Builder<UserModel>().setQuery(query,UserModel.class).build();
        adapter=new UsersRestrictedAdapter(options,requireContext());
        v.setLayoutManager(new LinearLayoutManager(getContext()));
        v.setAdapter(adapter);
        adapter.startListening();
    }
}
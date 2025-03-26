package com.example.newstep.Fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.newstep.Adapters.UsersBanAdapter;
import com.example.newstep.Adapters.UsersRestrictedAdapter;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;


public class AdminBannedAccountsFragment extends Fragment {




    public AdminBannedAccountsFragment() {

    }


    RecyclerView rba;
    UsersBanAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(  @Nullable LayoutInflater inflater,  @Nullable ViewGroup container,
                               @Nullable  Bundle savedInstanceState) {

        View view =inflater.inflate(R.layout.fragment_admin_banned_accounts, container, false);
        rba=view.findViewById(R.id.rba);
        setupRecyclerView();
        return view;


    }


    private void setupRecyclerView() {
        Query query = FirebaseUtil.allUserCollectionRef()
                .whereEqualTo("isBanned",Boolean.TRUE);

        FirestoreRecyclerOptions<UserModel> options=new FirestoreRecyclerOptions.Builder<UserModel>().setQuery(query,UserModel.class).build();
        adapter=new UsersBanAdapter(options,requireContext());
        rba.setLayoutManager(new LinearLayoutManager(getContext()));
        rba.setAdapter(adapter);
        adapter.startListening();
    }
}
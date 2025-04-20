package com.example.newstep.Fragments;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.app.AlertDialog;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;

public class AdminBannedAccountsFragment extends Fragment {




    public AdminBannedAccountsFragment() {

    }


    RecyclerView rba;
    UsersBanAdapter adapter;
    private ImageView btnfilterbanned;


    private Query.Direction sortOrder = Query.Direction.DESCENDING;
    private String sortField = "whenBanned";

    @Nullable
    @Override
    public View onCreateView(  @Nullable LayoutInflater inflater,  @Nullable ViewGroup container,
                               @Nullable  Bundle savedInstanceState) {
        if (inflater == null) {
            return null;
        }
        View view =inflater.inflate(R.layout.fragment_admin_banned_accounts, container, false);
        rba=view.findViewById(R.id.rba);
        btnfilterbanned = view.findViewById(R.id.btnfilterbanned);
        if (btnfilterbanned != null) {
            btnfilterbanned.setOnClickListener(v -> showFilterPopup());
        }
        rba.setLayoutManager(new LinearLayoutManager(getContext()));
        btnfilterbanned.setOnClickListener(v -> showFilterPopup());
        setupRecyclerView();
        return view;


    }

    private void setupRecyclerView() {
        if (adapter != null) {
            adapter.stopListening();
            rba.setAdapter(null);
        }
        Query query = FirebaseUtil.allUserCollectionRef()
                .whereEqualTo("isBanned",Boolean.TRUE).orderBy(sortField, sortOrder);

        FirestoreRecyclerOptions<UserModel> options=new FirestoreRecyclerOptions.Builder<UserModel>().setQuery(query,UserModel.class).build();
        adapter=new UsersBanAdapter(options,requireContext());
        rba.setLayoutManager(new LinearLayoutManager(getContext()));
        rba.setAdapter(adapter);
        adapter.startListening();
    }
    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
            if (rba.getAdapter() == null) {
                rba.setAdapter(adapter);
            }
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
    private void showFilterPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.popup_filter, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        Button sortByReportsAsc = view.findViewById(R.id.sortByReportsAsc);
        Button sortByReportsDesc = view.findViewById(R.id.sortByReportsDesc);
        Button sortByNewest = view.findViewById(R.id.sortByNewest);
        Button sortByOldest = view.findViewById(R.id.sortByOldest);
        View.OnClickListener filterClickListener = v -> {
            int id = v.getId();

            if (id == R.id.sortByReportsAsc) {
                sortField = "nb_reports";
                sortOrder = Query.Direction.ASCENDING;
            } else if (id == R.id.sortByReportsDesc) {
                sortField = "nb_reports";
                sortOrder = Query.Direction.DESCENDING;
            } else if (id == R.id.sortByNewest) {
                sortField = "whenBanned";
                sortOrder = Query.Direction.DESCENDING;
            } else if (id == R.id.sortByOldest) {
                sortField = "whenBanned";
                sortOrder = Query.Direction.ASCENDING;
            }
            dialog.dismiss();

            new Handler().postDelayed(this::setupRecyclerView, 200);
        };
        sortByReportsAsc.setOnClickListener(filterClickListener);
        sortByReportsDesc.setOnClickListener(filterClickListener);
        sortByNewest.setOnClickListener(filterClickListener);
        sortByOldest.setOnClickListener(filterClickListener);
    }

}
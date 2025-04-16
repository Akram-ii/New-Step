package com.example.newstep.Fragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Adapters.UsersRestrictedAdapter;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class AdminRestrictedAccountsFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersRestrictedAdapter adapter;
    private ImageView btnfilterrestricted;

    private Query.Direction sortOrder = Query.Direction.DESCENDING;
    private String sortField = "whenBannedComments"; // valeur par défaut

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_restricted_accounts, container, false);

        recyclerView = view.findViewById(R.id.rba); // ID du RecyclerView dans le layout
        btnfilterrestricted = view.findViewById(R.id.btnfilterrestricted); // bouton filtre déjà existant
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnfilterrestricted.setOnClickListener(v -> showFilterPopup());

        setupRecycler();

        return view;
    }

    private void setupRecycler() {
        if (adapter != null) {
            adapter.stopListening();
            recyclerView.setAdapter(null);
        }

        Query query = FirebaseUtil.allUserCollectionRef()
                .whereEqualTo("isRestricted", true)
                .orderBy(sortField, sortOrder);

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class)
                .build();

        adapter = new UsersRestrictedAdapter(options, requireContext());
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void showFilterPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.popup_filter_2, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


        Button sortByReportsAsc = view.findViewById(R.id.sortByReportsAsc);
        Button sortByReportsDesc = view.findViewById(R.id.sortByReportsDesc);
        Button sortByOldestBanComment = view.findViewById(R.id.sortByoldestbancomment);
        Button sortByNewestBanComment = view.findViewById(R.id.sortBynewestbancomment);
        Button sortByOldestBanPost = view.findViewById(R.id.sortByoldestbanpost);
        Button sortByNewestBanPost = view.findViewById(R.id.sortBynewestbanpost);

        View.OnClickListener filterClickListener = v -> {
            int id = v.getId();

            if (id == R.id.sortByReportsAsc) {
                sortField = "nb_reports";
                sortOrder = Query.Direction.ASCENDING;
            } else if (id == R.id.sortByReportsDesc) {
                sortField = "nb_reports";
                sortOrder = Query.Direction.DESCENDING;
            } else if (id == R.id.sortByoldestbancomment) {
                sortField = "whenBannedComments";
                sortOrder = Query.Direction.ASCENDING;
            } else if (id == R.id.sortBynewestbancomment) {
                sortField = "whenBannedComments";
                sortOrder = Query.Direction.DESCENDING;
            } else if (id == R.id.sortByoldestbanpost) {
                sortField = "whenBannedPosts";
                sortOrder = Query.Direction.ASCENDING;
            } else if (id == R.id.sortBynewestbanpost) {
                sortField = "whenBannedPosts";
                sortOrder = Query.Direction.DESCENDING;
            }

            dialog.dismiss();
            new Handler().postDelayed(this::setupRecycler, 200);
        };


        sortByReportsAsc.setOnClickListener(filterClickListener);
        sortByReportsDesc.setOnClickListener(filterClickListener);
        sortByOldestBanComment.setOnClickListener(filterClickListener);
        sortByNewestBanComment.setOnClickListener(filterClickListener);
        sortByOldestBanPost.setOnClickListener(filterClickListener);
        sortByNewestBanPost.setOnClickListener(filterClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
            if (recyclerView.getAdapter() == null) {
                recyclerView.setAdapter(adapter);
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}

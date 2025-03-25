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


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Adapters.ReportCommentAdapter;
import com.example.newstep.Models.ReportComment;
import com.example.newstep.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AdminCommentsFragment extends Fragment {

    private RecyclerView recyclerViewReports;
    private ReportCommentAdapter adapter;
    private FirebaseFirestore firestore;
    private ImageView buttonFilter;
    private Query.Direction sortOrder = Query.Direction.DESCENDING;
    private String sortField = "lastReportTime";

    public AdminCommentsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_comments, container, false);

        recyclerViewReports = view.findViewById(R.id.recyclerViewReports);
        buttonFilter = view.findViewById(R.id.buttonFilter);
        buttonFilter.setOnClickListener(v -> showFilterPopup());

        recyclerViewReports.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore = FirebaseFirestore.getInstance();

        // 🔥 Charge les reports
        loadReports();

        return view;
    }

    private void loadReports() {
        if (adapter != null) {
            adapter.stopListening();
            recyclerViewReports.setAdapter(null);
        }

        Query query = firestore.collection("ReportComments")
                .orderBy(sortField, sortOrder);

        FirestoreRecyclerOptions<ReportComment> options =
                new FirestoreRecyclerOptions.Builder<ReportComment>()
                        .setQuery(query, ReportComment.class)
                        .build();

        adapter = new ReportCommentAdapter(options, getContext());
        recyclerViewReports.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
            if (recyclerViewReports.getAdapter() == null) {
                recyclerViewReports.setAdapter(adapter);
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
                sortField = "nbReports";
                sortOrder = Query.Direction.ASCENDING;
            } else if (id == R.id.sortByReportsDesc) {
                sortField = "nbReports";
                sortOrder = Query.Direction.DESCENDING;
            } else if (id == R.id.sortByNewest) {
                sortField = "lastReportTime";
                sortOrder = Query.Direction.DESCENDING;
            } else if (id == R.id.sortByOldest) {
                sortField = "lastReportTime";
                sortOrder = Query.Direction.ASCENDING;
            }

            dialog.dismiss();


            new Handler().postDelayed(this::loadReports, 200);
        };

        sortByReportsAsc.setOnClickListener(filterClickListener);
        sortByReportsDesc.setOnClickListener(filterClickListener);
        sortByNewest.setOnClickListener(filterClickListener);
        sortByOldest.setOnClickListener(filterClickListener);
    }
}

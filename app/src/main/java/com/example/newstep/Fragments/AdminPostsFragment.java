package com.example.newstep.Fragments;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.newstep.Adapters.ReportPostAdapter;
import com.example.newstep.Models.ReportPost;
import com.example.newstep.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.Handler;

import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class AdminPostsFragment extends Fragment {

    private RecyclerView reportPostsRecyclerView;
    private ReportPostAdapter adapter;
    private FirebaseFirestore db;
    private ImageView buttonFilter;
    private Query.Direction sortOrder = Query.Direction.DESCENDING;
    private String sortField = "lastReportPostTime";

    public AdminPostsFragment() {

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_admin_posts, container, false);
        db = FirebaseFirestore.getInstance();
        reportPostsRecyclerView = rootView.findViewById(R.id.recycler_view_post_admin);
        reportPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        buttonFilter = rootView.findViewById(R.id.buttonFilter);
        buttonFilter.setOnClickListener(v -> showFilterPopup());

        SetUpRecyclerView();

        return rootView;
    }

    private void SetUpRecyclerView() {
        if (adapter != null) {
            adapter.stopListening();
            reportPostsRecyclerView.setAdapter(null);
        }

        Query query = db.collection("reportPosts").orderBy(sortField, sortOrder);

        FirestoreRecyclerOptions<ReportPost> options = new FirestoreRecyclerOptions.Builder<ReportPost>()
                .setQuery(query, ReportPost.class)
                .build();

        adapter = new ReportPostAdapter(options, getContext());
        reportPostsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
            if (reportPostsRecyclerView.getAdapter() == null) {
                reportPostsRecyclerView.setAdapter(adapter);
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
                sortField = "reportCount";
                sortOrder = Query.Direction.ASCENDING;
            } else if (id == R.id.sortByReportsDesc) {
                sortField = "reportCount";
                sortOrder = Query.Direction.DESCENDING;
            } else if (id == R.id.sortByNewest) {
                sortField = "lastReportPostTime";
                sortOrder = Query.Direction.DESCENDING;
            } else if (id == R.id.sortByOldest) {
                sortField = "lastReportPostTime";
                sortOrder = Query.Direction.ASCENDING;
            }

            dialog.dismiss();


            new Handler().postDelayed(this::SetUpRecyclerView, 200);
        };

        sortByReportsAsc.setOnClickListener(filterClickListener);
        sortByReportsDesc.setOnClickListener(filterClickListener);
        sortByNewest.setOnClickListener(filterClickListener);
        sortByOldest.setOnClickListener(filterClickListener);
    }
}

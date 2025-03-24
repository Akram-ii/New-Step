package com.example.newstep.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    public AdminCommentsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_comments, container, false);

        recyclerViewReports = view.findViewById(R.id.recyclerViewReports);
        recyclerViewReports.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore = FirebaseFirestore.getInstance();


        loadReports();

        return view;
    }

    private void loadReports() {
        Query query = firestore.collection("ReportComments")
                .orderBy("lastReportTime", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ReportComment> options =
                new FirestoreRecyclerOptions.Builder<ReportComment>()
                        .setQuery(query, ReportComment.class)
                        .build();

        adapter = new ReportCommentAdapter(options, getContext());
        recyclerViewReports.setAdapter(adapter);
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
}

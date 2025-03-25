package com.example.newstep.Fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
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


public class AdminPostsFragment extends Fragment {


    private RecyclerView reportPostsRecyclerView;
    private ReportPostAdapter adapter;
    private FirebaseFirestore db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_admin_posts, container, false);
        db = FirebaseFirestore.getInstance();
        reportPostsRecyclerView = rootView.findViewById(R.id.recycler_view_post_admin);
        reportPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


       SetUpRecyclerView();

        return rootView;
    }
    private void SetUpRecyclerView() {
        Query query = db.collection("reportPosts").orderBy("lastReportPostTime", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ReportPost> options = new FirestoreRecyclerOptions.Builder<ReportPost>()
                .setQuery(query, ReportPost.class)
                .build();


        adapter = new ReportPostAdapter(options, getContext());
        reportPostsRecyclerView.setAdapter(adapter);
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
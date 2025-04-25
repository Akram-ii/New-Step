package com.example.newstep.Fragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.newstep.Adapters.ReportCommentAdapter;
import com.example.newstep.Adapters.ReportsAdapter;
import com.example.newstep.Models.ReportComment;
import com.example.newstep.Models.ReportsModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class AdminContactFragment extends Fragment {


    private RecyclerView recyReports;
    private ReportsAdapter adapter;
    private FirebaseFirestore firestore;
    private ImageView buttonFilter;
    private Query.Direction sortOrder = Query.Direction.DESCENDING;
    private String sortField = "Rtime";
    private String selectedCategory = null;

    public AdminContactFragment() {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootview = inflater.inflate(R.layout.fragment_admin_contact, container, false);

        recyReports = rootview.findViewById(R.id.recyclerReports);
        buttonFilter = rootview.findViewById(R.id.buttonFilterID);
        buttonFilter.setOnClickListener(v -> showFilterPopup());

        firestore = FirebaseFirestore.getInstance();
        recyReports.setLayoutManager(new LinearLayoutManager(getContext()));

        loadReports();


        return rootview;
    }



    private void loadReports() {
        if (adapter != null) {
            adapter.stopListening();
            recyReports.setAdapter(null);
        }

        CollectionReference ref = firestore.collection("contact");
        Query query;


        if (selectedCategory != null ) {
            query = ref.whereEqualTo("Rcat", selectedCategory);
        }

        else if (sortField != null ) {
            query = ref.orderBy(sortField, sortOrder);
        }

        else {
            query = ref.orderBy(sortField, sortOrder);
        }

        FirestoreRecyclerOptions<ReportsModel> options =
                new FirestoreRecyclerOptions.Builder<ReportsModel>()
                        .setQuery(query, ReportsModel.class)
                        .build();

        adapter = new ReportsAdapter(options, getContext());
        recyReports.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
            if (recyReports.getAdapter() == null) {
                recyReports.setAdapter(adapter);
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
        View view = inflater.inflate(R.layout.popup_filter2, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        Button sortByNewest = view.findViewById(R.id.sortByNewest);
        Button sortByOldest = view.findViewById(R.id.sortByOldest);
        Button cat1 = view.findViewById(R.id.cat_Tech);
        Button cat2 = view.findViewById(R.id.cat_Account);
        Button cat3 = view.findViewById(R.id.cat_fed_sugg);
        Button cat4 = view.findViewById(R.id.cat_ReportProbleme);
        Button cat5 = view.findViewById(R.id.cat_Featur);
        Button cat6 = view.findViewById(R.id.cat_other);

        sortByNewest.setOnClickListener(v -> {
            sortField = "Rtime";
            sortOrder = Query.Direction.DESCENDING;
            selectedCategory = null;
            dialog.dismiss();
            new Handler().postDelayed(this::loadReports, 200);
        });

        sortByOldest.setOnClickListener(v -> {
            sortField = "Rtime";
            sortOrder = Query.Direction.ASCENDING;
            selectedCategory = null;
            dialog.dismiss();
            new Handler().postDelayed(this::loadReports, 200);
        });



        cat1.setOnClickListener(v -> {
            selectedCategory = "Technical Issue";
            dialog.dismiss();
            new Handler().postDelayed(this::loadReports, 200);
        });

        cat2.setOnClickListener(v -> {
            selectedCategory = "Account Help";
            dialog.dismiss();
            new Handler().postDelayed(this::loadReports, 200);
        });


        cat3.setOnClickListener(v -> {
            selectedCategory = "Feedback or Suggestions";
            dialog.dismiss();
            new Handler().postDelayed(this::loadReports, 200);
        });

        cat4.setOnClickListener(v -> {
            selectedCategory = "Report a Problem";
            dialog.dismiss();
            new Handler().postDelayed(this::loadReports, 200);
        });

        cat5.setOnClickListener(v -> {
            selectedCategory = "Feature Request";
            dialog.dismiss();
            new Handler().postDelayed(this::loadReports, 200);
        });

        cat6.setOnClickListener(v -> {
            selectedCategory = "Other";
            dialog.dismiss();
            new Handler().postDelayed(this::loadReports, 200);
        });



    }




}
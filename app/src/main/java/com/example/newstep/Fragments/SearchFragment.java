package com.example.newstep.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.newstep.Adapters.SearchUserRecyclerAdapter;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class SearchFragment extends Fragment {

    EditText username;
    ImageButton search,reset;
    RecyclerView searchResults;
    SearchUserRecyclerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        search=rootView.findViewById(R.id.search_icon);
        username=rootView.findViewById(R.id.user_EditText);
        searchResults=rootView.findViewById(R.id.search_RecyclerView);
        reset=rootView.findViewById(R.id.reset_ImageButton);
        reset.setOnClickListener(v->{
            username.setText("");
            setupSearchRecyclerView("jedmnfo9wm.atqZ");
            //hedi ghir 3ajal UI beh ndirou clear lel recycler view ki yros 3la reset
            //ki ydir query mch 7a ylga 7ata wa7d esmou ybda kimak psq random, la probabilite enou 3abd ydir username ada hia 1/(26^15)=0.0000000000000000000000596 :)
        });
        username.requestFocus();
        search.setOnClickListener(v->{
            String txtUsername=username.getText().toString();
            if(txtUsername.isEmpty()){
                Toast.makeText(getContext(),"Enter a username",Toast.LENGTH_SHORT).show();
            }else{
                setupSearchRecyclerView(username.getText().toString());}
        });

        return rootView;
    }

    private void setupSearchRecyclerView(String username) {
        Query query = FirebaseFirestore.getInstance().collection("Users")
                .orderBy("username")
                .whereGreaterThanOrEqualTo("username", username)
                .whereLessThan("username", username + "\uf8ff");
        FirestoreRecyclerOptions<UserModel> options=new FirestoreRecyclerOptions.Builder<UserModel>().setQuery(query,UserModel.class).build();
        adapter=new SearchUserRecyclerAdapter(options,requireContext());
        searchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResults.setAdapter(adapter);
        adapter.startListening();

    }

    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
        }
    }

}
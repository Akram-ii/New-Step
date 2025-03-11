package com.example.newstep.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.newstep.R;
public class GroupsFragment extends Fragment {
private Button chat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        chat=rootView.findViewById(R.id.chatsBtn);
chat.setOnClickListener(v->{
    setupChatBtn();
});

    //code t3 fragment group ykon lhna

        rootView.setVisibility(View.VISIBLE);
        return rootView;
    }

    private void setupChatBtn() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof ChatsFragment) {
            Log.d("ChatsFragment", "Already in ChatsFragment. No need to replace.");
            return; // Prevent replacing with itself
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        ChatsFragment chatsFragment = new ChatsFragment();
        transaction.replace(R.id.fragment_container, chatsFragment, ChatsFragment.class.getSimpleName());
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }


}
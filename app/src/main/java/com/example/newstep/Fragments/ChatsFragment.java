package com.example.newstep.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import com.example.newstep.Adapters.MyPagerChats;
import com.example.newstep.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class ChatsFragment extends Fragment {

ViewPager2 viewPager;
TabLayout tableLayout;
MyPagerChats pagerAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView= inflater.inflate(R.layout.fragment_chats, container, false);
        viewPager = rootView.findViewById(R.id.viewPager);
        tableLayout = rootView.findViewById(R.id.chatsTabLayout);
        pagerAdapter = new MyPagerChats(this);

        viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(tableLayout, viewPager, (tab, position) -> {

            if (position == 0) {
                tab.setText("My Chats");
            } else if (position == 1) {
                tab.setText("Group Chats");
            }

        }).attach();

    return rootView;
    }
}

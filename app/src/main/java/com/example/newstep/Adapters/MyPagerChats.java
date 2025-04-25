package com.example.newstep.Adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.newstep.Fragments.PrivateChatsFragment;
import com.example.newstep.Fragments.GroupsFragment;

public class MyPagerChats extends FragmentStateAdapter {

    private final Fragment[] fragments;

    public MyPagerChats(@NonNull Fragment fragment) {
        super(fragment);
        fragments = new Fragment[]{
                new PrivateChatsFragment(),
                new GroupsFragment()
        };
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }

    public Fragment getFragmentAt(int position) {
        return fragments[position];
    }

}

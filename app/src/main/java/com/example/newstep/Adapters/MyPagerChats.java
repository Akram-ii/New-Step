package com.example.newstep.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.newstep.Fragments.PrivateChatsFragment;
import com.example.newstep.Fragments.GroupsFragment;

public class MyPagerChats extends FragmentStateAdapter {

    public MyPagerChats(@NonNull Fragment parentFragment) {
        super(parentFragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PrivateChatsFragment();
            case 1:
                return new GroupsFragment();
            default:
                throw new IllegalArgumentException("Invalid position in MyPagerChats: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

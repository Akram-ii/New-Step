package com.example.newstep.Adapters;

import android.support.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.newstep.Fragments.LikedPostsFragment;
import com.example.newstep.Fragments.MyPostsFragment;

public class ProfileAdapter extends FragmentStateAdapter {

    public ProfileAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0:
                return new MyPostsFragment();
            case 1:
                return new LikedPostsFragment();
            default:
                return new MyPostsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }





}

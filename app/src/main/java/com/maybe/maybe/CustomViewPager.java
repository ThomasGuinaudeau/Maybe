package com.maybe.maybe;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.maybe.maybe.fragments.CategoryFragment;
import com.maybe.maybe.fragments.MainFragment;
import com.maybe.maybe.fragments.PlayerFragment;

public class CustomViewPager extends FragmentStateAdapter {
    public static final int CAT_POS = 0;
    public static final int MAIN_POS = 1;
    public static final int PLAY_POS = 2;
    public static final int NUM_PAGES = 3;

    private SparseArray<Fragment> registeredFragments;

    public CustomViewPager(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        registeredFragments = new SparseArray<>();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = new Fragment();
        if (position == CAT_POS) fragment = CategoryFragment.newInstance();
        else if (position == MAIN_POS) fragment = MainFragment.newInstance();
        else if (position == PLAY_POS) fragment = PlayerFragment.newInstance();
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
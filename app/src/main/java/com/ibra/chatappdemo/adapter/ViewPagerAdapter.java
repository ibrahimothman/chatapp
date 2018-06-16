package com.ibra.chatappdemo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private Fragment[]fragments;

    public ViewPagerAdapter(FragmentManager fm,Fragment[]fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0) return "Chat";
        else if(position == 1) return "Freinds";
        else if(position == 2) return "Request";
        else return null;
    }
}

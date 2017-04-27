package com.microchip.pcs.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.microchip.pcs.ViewFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {
 
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            // Top Rated fragment activity
            return new ViewFragment();
//        case 1:
            // Games fragment activity
//            try {
//                Thread.sleep(500);
//            } catch (Exception e) {
//                Log.d("TAG",e.toString());
//            }
//            return new SettingFragment();
//        case 2:
//            // Movies fragment activity
//            return new MoviesFragment();
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
//        return 3;
        return 1;
    }
 
}
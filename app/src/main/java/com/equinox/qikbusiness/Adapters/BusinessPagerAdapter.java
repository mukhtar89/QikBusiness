package com.equinox.qikbusiness.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.equinox.qikbusiness.Fragments.BusinessFragment;
import com.equinox.qikbusiness.Models.Place;

import java.util.List;

/**
 * Created by mukht on 11/11/2016.
 */
public class BusinessPagerAdapter extends FragmentPagerAdapter {

    private List<Place> placeList;

    public BusinessPagerAdapter(FragmentManager fm, List<Place> placeList) {
        super(fm);
        this.placeList = placeList;
    }

    @Override
    public Fragment getItem(int position) {
        return BusinessFragment.newInstance(placeList.get(position));
    }

    @Override
    public int getCount() {
        return placeList.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return BusinessPagerAdapter.POSITION_NONE;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return placeList.get(position).getName();
    }
}

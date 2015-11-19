package com.hci.geotagger.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hci.geotagger.R;

/**
 * Created by sreddy on 11/1/15.
 */
public class TagsFragment extends Fragment {
public TagsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_tags, container, false);

        return rootView;
    }

}

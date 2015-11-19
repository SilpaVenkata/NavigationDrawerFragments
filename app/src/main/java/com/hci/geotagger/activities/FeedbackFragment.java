package com.hci.geotagger.activities;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.hci.geotagger.R;

public class FeedbackFragment extends Fragment {
    public FeedbackFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;
        try
        {
            rootView = inflater.inflate(R.layout.fragment_feedback, container, false);
            WebView browser = (WebView)rootView.findViewById(R.id.webView);
            String strURL = "https://docs.google.com/forms/d/1CRSXqdzy3C98JgCbXEwHLj374sTHVnTMiA04blExXp8/viewform";
            browser.loadUrl(strURL);
        }
        catch(Exception ex)
        {

        }
        return rootView;
    }

}
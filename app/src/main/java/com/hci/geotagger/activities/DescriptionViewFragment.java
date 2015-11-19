//package com.teampterodactyl.fragments;
package com.hci.geotagger.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hci.geotagger.R;

/**
 * DescriptionViewFragment implements the fragment that allows for the viewing of
 * a tag's description in the TagViewActivity. When the user clicks the description 
 * button, this view will be displayed. The description contains the tag's description,
 * the date the tag was created, and the location of the tag.
 * 
 * @author: Spencer Kordecki
 * @version: January 20, 2014
 */
//suppressing so I can override default constructor and avoid setting method for IDescriptionViewFragment
public class DescriptionViewFragment extends Fragment
{
	private View fragmentView; //view to be returned 
	private IDescriptionViewCallback callback;
	
	/**
	 * Creates the view that the user will see when the fragment is created, similar 
	 * to onCreate methods seen in activities. Callback called here so that class that instantiated this fragment
	 * can fill in content to view after it has been instantiated. Attempting to fill in information before he view has
	 * been created will cause a crash, so the callback is neccessary. 
	 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
    {
        
    	fragmentView = inflater.inflate(R.layout.fragment_description_view, container, false);
    
    	callback.onCreateDescriptionViewCallback();
    	
    	return fragmentView;
    }
    
	/**
	*  IDescriptionViewCallback Interface should be used by classes instantiating the DescriptionViewFragment class. 
	 * This allows the class to fill in text, images, and other information once the DescriptionView has actually been 
	 * created. Trying to add text and other data to a newly instantiated DescriptionView may cause a crash if the view has 
	 * yet been fully created. Callback methods are called in this classes onCreateView method and should be 
	 * implemented in the class that instantiated the view to add information to the newly created class.
	 *
	 */
    
    public interface IDescriptionViewCallback
    {
    	public void onCreateDescriptionViewCallback();
    }
    
    public DescriptionViewFragment(IDescriptionViewCallback callback)
    {
    	this.callback = callback;
    }
    
    public DescriptionViewFragment()
    {
    	
    }
   
    
    @Override
    public View getView() 
    {
    	return fragmentView;
    };
    
}
